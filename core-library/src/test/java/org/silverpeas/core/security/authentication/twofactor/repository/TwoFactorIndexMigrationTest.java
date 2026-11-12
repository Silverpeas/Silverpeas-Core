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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorIndexMigrationTest {

  @ParameterizedTest(name = "{0}: existing model-usage index [{1}]")
  @MethodSource("upgradeScenarios")
  void upgradeShouldPreserveExistingIndexesAndCreateTwoFactorIndexes(String database,
      String existingIndex) throws Exception {
    Path script = Path.of("../core-configuration/src/main/config/migrations/db", database,
        "busCore/up049/create_index.sql");
    // All four index scripts use standard CREATE INDEX statements executable on H2.
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
         var statement = connection.createStatement()) {
      statement.execute("create table st_instance_modelused (instanceId varchar(50), modelId varchar(50))");
      if (!existingIndex.isEmpty()) {
        statement.execute("create index " + existingIndex +
            " on st_instance_modelused (instanceId, modelId)");
      }
      Set<String> previousIndexes = indexNames(connection, "ST_INSTANCE_MODELUSED");
      statement.execute("create table ST_User_2FA_Recovery (userId int)");
      statement.execute("create table ST_User_2FA_Trusted_Device (userId int, tokenHash varchar(64))");

      statement.execute(Files.readString(script));

      assertEquals(previousIndexes, indexNames(connection, "ST_INSTANCE_MODELUSED"));
      assertEquals(Set.of("IDX_USER_2FA_RECOVERY_USER"),
          indexNames(connection, "ST_USER_2FA_RECOVERY"));
      assertEquals(List.of("USERID"), indexColumns(connection, "ST_USER_2FA_RECOVERY"));
      assertEquals(Set.of("IDX_USER_2FA_TRUSTED_DEVICE_USER_TOKEN"),
          indexNames(connection, "ST_USER_2FA_TRUSTED_DEVICE"));
      assertEquals(List.of("USERID", "TOKENHASH"),
          indexColumns(connection, "ST_USER_2FA_TRUSTED_DEVICE"));
    }
  }

  private static Stream<Arguments> upgradeScenarios() {
    return Stream.of("h2", "postgresql", "oracle", "mssql").flatMap(database ->
        Stream.of("IND_st_instance_modelused", "ind_instance", "IDX_st_instance_modelused", "")
            .map(index -> Arguments.of(database, index)));
  }

  private Set<String> indexNames(Connection connection, String table) throws SQLException {
    Set<String> names = new HashSet<>();
    try (var indexes = connection.getMetaData().getIndexInfo(null, null, table, false, false)) {
      while (indexes.next()) {
        names.add(indexes.getString("INDEX_NAME"));
      }
    }
    return names;
  }

  private List<String> indexColumns(Connection connection, String table) throws SQLException {
    List<String> columns = new ArrayList<>();
    try (var indexes = connection.getMetaData().getIndexInfo(null, null, table, false, false)) {
      while (indexes.next()) {
        columns.add(indexes.getString("COLUMN_NAME"));
      }
    }
    return columns;
  }
}
