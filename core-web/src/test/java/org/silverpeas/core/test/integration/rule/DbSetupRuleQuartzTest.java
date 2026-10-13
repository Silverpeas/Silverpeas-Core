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
package org.silverpeas.core.test.integration.rule;

import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DbSetupRuleQuartzTest {
  @Test
  void shouldReplayInitialTablesOnH2WithoutDuplicatingQuartzConstraints() throws Exception {
    for (boolean lowerCase : new boolean[]{false, true}) {
      String url = "jdbc:h2:mem:" + UUID.randomUUID() + (lowerCase ? ";DATABASE_TO_LOWER=TRUE" : "");
      try (var connection = DriverManager.getConnection(url)) {
        String[] statements;
        try (var script = getClass().getResourceAsStream("/dbsetup_default_tables.sql")) {
          assertNotNull(script);
          statements = new String(script.readAllBytes(), StandardCharsets.UTF_8).split(";");
        }
        assertEquals(statements.length,
            DbSetupRule.filterExistingQuartzConstraints(connection, statements).length);
        for (int run = 0; run < 2; run++) {
          String[] filtered = DbSetupRule.filterExistingQuartzConstraints(connection, statements);
          if (run > 0) {
            assertTrue(filtered.length < statements.length);
          }
          try (var statement = connection.createStatement()) {
            for (String sql : filtered) {
              if (!sql.isBlank()) {
                statement.execute(sql);
              }
            }
          }
        }
      }
    }
  }
}
