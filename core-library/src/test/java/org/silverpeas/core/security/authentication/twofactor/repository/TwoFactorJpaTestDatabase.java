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

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;

/**
 * Real JPA persistence against the shipped H2 upgrade schema, with isolated transactions.
 */
class TwoFactorJpaTestDatabase implements AutoCloseable {
  private final SessionFactory factory;
  private final EntityManager entityManager;

  TwoFactorJpaTestDatabase() throws Exception {
    String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    try (var connection = DriverManager.getConnection(url); var statement = connection.createStatement()) {
      statement.execute("create table ST_User (id int primary key)");
      statement.execute("insert into ST_User values (0), (42), (43)");
      // Reproduce the legacy index from schema 049 before running its upgrade scripts.
      statement.execute("create table st_instance_modelused (instanceId varchar(50), modelId varchar(50))");
      Path legacyIndex = Path.of("../core-configuration/src/main/config/migrations/db/h2/busCore/049/create_index_production.sql");
      statement.execute(Files.readString(legacyIndex));
      Path scripts = Path.of("../core-configuration/src/main/config/migrations/db/h2/busCore/up049");
      for (String name : new String[]{"create_table.sql", "create_constraint.sql", "create_index.sql"}) {
        for (String sql : Files.readString(scripts.resolve(name)).split(";")) {
          if (!sql.isBlank()) {
            statement.execute(sql);
          }
        }
      }
    }
    factory = new Configuration().addAnnotatedClass(TwoFactorAuthenticationEntity.class)
        .addAnnotatedClass(RecoveryCodeEntity.class).addAnnotatedClass(TrustedDeviceEntity.class)
        .setProperty("hibernate.connection.url", url)
        .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
        .setProperty("hibernate.hbm2ddl.auto", "validate")
        .buildSessionFactory();
    entityManager = factory.createEntityManager();
    entityManager.getTransaction().begin();
  }

  EntityManager entityManager() {
    return entityManager;
  }

  EntityManager newEntityManager() {
    return factory.createEntityManager();
  }

  @Override
  public void close() {
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
    entityManager.close();
    factory.close();
  }
}
