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
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwoFactorIndexMigrationTest {

  @Test
  void upgradeAndFreshInstallShouldHaveTheSameModelUsageIndex() throws Exception {
    Path scripts = Path.of("../core-configuration/src/main/config/migrations/db/h2/busCore");
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
         var statement = connection.createStatement()) {
      statement.execute("create table st_instance_modelused (instanceId varchar(50), modelId varchar(50))");
      statement.execute(Files.readString(scripts.resolve("049/create_index_production.sql")));
      // Run the complete upgrade script, including the two new 2FA indexes.
      statement.execute("create table ST_User_2FA_Recovery (userId int)");
      statement.execute("create table ST_User_2FA_Trusted_Device (userId int, tokenHash varchar(64))");
      statement.execute(Files.readString(scripts.resolve("up049/create_index.sql")));
      Set<String> upgraded = indexNames(connection);
      assertTrue(upgraded.contains("IDX_ST_INSTANCE_MODELUSED"));
      assertFalse(upgraded.contains("IND_ST_INSTANCE_MODELUSED"));

      statement.execute("drop index IDX_st_instance_modelused");
      statement.execute(Files.readString(scripts.resolve("050/create_index_production.sql")));
      assertEquals(upgraded, indexNames(connection));
    }
  }

  private Set<String> indexNames(Connection connection) throws Exception {
    Set<String> names = new HashSet<>();
    try (var indexes = connection.getMetaData().getIndexInfo(
        null, null, "ST_INSTANCE_MODELUSED", false, false)) {
      while (indexes.next()) {
        names.add(indexes.getString("INDEX_NAME"));
      }
    }
    return names;
  }
}
