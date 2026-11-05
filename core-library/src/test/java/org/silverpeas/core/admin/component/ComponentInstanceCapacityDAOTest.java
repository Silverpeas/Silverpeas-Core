/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component;

import java.sql.DriverManager;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.dao.ComponentInstanceCapacityDAO;
import static org.junit.jupiter.api.Assertions.*;

class ComponentInstanceCapacityDAOTest {
  private final ComponentInstanceCapacityDAO dao = new ComponentInstanceCapacityDAO();

  private String database() throws Exception {
    String url = "jdbc:h2:mem:capacity-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000";
    try (var connection = DriverManager.getConnection(url); var sql = connection.createStatement()) {
      sql.execute("CREATE TABLE ST_Space(id INT PRIMARY KEY, orderNum INT)");
      sql.execute("CREATE TABLE ST_ComponentInstance(id INT PRIMARY KEY, spaceId INT, componentName VARCHAR(50), componentStatus VARCHAR(30))");
      sql.execute("INSERT INTO ST_Space VALUES (1,0),(2,0)");
      sql.execute("INSERT INTO ST_ComponentInstance VALUES (1,1,'app',NULL),(2,1,'other',NULL),(3,2,'app',NULL),(4,1,'app','R')");
    }
    return url;
  }

  @Test
  void onlyActiveDirectInstancesOfTheSameApplicationCount() throws Exception {
    try (var connection = DriverManager.getConnection(database())) {
      connection.setAutoCommit(false);
      assertEquals(1, dao.lockAndCount(connection, 1, "app", -1));
      assertEquals(0, dao.lockAndCount(connection, 1, "app", 1));
      assertEquals(1, dao.lockAndCount(connection, 2, "app", -1));
      connection.rollback();
    }
  }

  @Test
  void concurrentCreationWaitsForCommitThenSeesTheNewInstance() throws Exception {
    String url = database();
    try (var first = DriverManager.getConnection(url); var workers = Executors.newSingleThreadExecutor()) {
      first.setAutoCommit(false);
      assertEquals(0, dao.lockAndCount(first, 1, "newapp", -1));
      var started = new CountDownLatch(1);
      var second = workers.submit(() -> {
        try (var connection = DriverManager.getConnection(url)) {
          connection.setAutoCommit(false);
          started.countDown();
          int count = dao.lockAndCount(connection, 1, "newapp", -1);
          connection.rollback();
          return count;
        }
      });
      assertTrue(started.await(2, TimeUnit.SECONDS));
      assertThrows(TimeoutException.class, () -> second.get(150, TimeUnit.MILLISECONDS));
      try (var sql = first.createStatement()) {
        sql.execute("INSERT INTO ST_ComponentInstance VALUES(5,1,'newapp',NULL)");
      }
      first.commit();
      assertEquals(1, second.get(3, TimeUnit.SECONDS));
    }
  }
  @Test
  void platformCountsIncludeEverySpaceAndIgnoreTheBasket() throws Exception {
    try (var connection = DriverManager.getConnection(database())) {
      connection.setAutoCommit(false);
      assertEquals(2, dao.lockAndCountPlatform(connection, "app", -1));
      assertEquals(1, dao.lockAndCountPlatform(connection, "app", 1));
      assertEquals(java.util.List.of("app1","app3"), dao.platformInstanceIds(connection,"app"));
      connection.rollback();
    }
  }

  @Test
  void platformWritersInDifferentSpacesSerializeOnTheSameLock() throws Exception {
    String url = database();
    try (var first = DriverManager.getConnection(url); var workers = Executors.newSingleThreadExecutor()) {
      first.setAutoCommit(false);
      assertEquals(0, dao.lockAndCountPlatform(first, "global", -1));
      var started = new CountDownLatch(1);
      var second = workers.submit(() -> {
        try (var connection = DriverManager.getConnection(url)) {
          connection.setAutoCommit(false); started.countDown();
          int count = dao.lockAndCountPlatform(connection, "global", -1);
          connection.rollback(); return count;
        }
      });
      assertTrue(started.await(2,TimeUnit.SECONDS));
      assertThrows(TimeoutException.class, () -> second.get(150,TimeUnit.MILLISECONDS));
      try (var sql = first.createStatement()) { sql.execute("INSERT INTO ST_ComponentInstance VALUES(5,2,'global',NULL)"); }
      first.commit();
      assertEquals(1,second.get(3,TimeUnit.SECONDS));
    }
  }

}
