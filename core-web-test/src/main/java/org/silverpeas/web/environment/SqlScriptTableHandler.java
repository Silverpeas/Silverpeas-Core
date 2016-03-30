/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.environment;

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQueries;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yohann Chastagnier
 */
public class SqlScriptTableHandler implements TableHandler {

  private static final Pattern TABLE_CREATION_NAME_PATTERN =
      Pattern.compile("(?i)create[\\s]+table[\\s]+([\\w_\\-0-9]+)");
  private static final Pattern IF_NOT_EXISTS_TABLE_CREATION_NAME_PATTERN =
      Pattern.compile("(?i)create[\\s]+table[\\s]+if[\\s]+not[\\s]+exists[\\s]+([\\w_\\-0-9]+)");

  private String tableNameToCheckExistence = null;
  private Set<String> initQueries = new LinkedHashSet<>();
  private Set<String> dropTableQueries = new LinkedHashSet<>();

  protected SqlScriptTableHandler(String... scriptFileNames) {
    initializeDatabase(scriptFileNames);
  }

  /**
   * Initializing the dataBase
   * @param scriptFileNames
   */
  private void initializeDatabase(String... scriptFileNames) {
    try {
      for (String scriptFileName : scriptFileNames) {
        try (InputStream scriptFileIS = TableHandler.class.getResourceAsStream(scriptFileName)) {
          StringTokenizer tokenizer = new StringTokenizer(IOUtils.toString(scriptFileIS), ";");
          while (tokenizer.hasMoreTokens()) {
            String currentQuery = tokenizer.nextToken();
            initQueries.add(currentQuery);
            Matcher matcher = IF_NOT_EXISTS_TABLE_CREATION_NAME_PATTERN.matcher(currentQuery);
            boolean ifNotExistsMatched = matcher.find();
            boolean matched = false;
            if (!ifNotExistsMatched) {
              matcher = TABLE_CREATION_NAME_PATTERN.matcher(currentQuery);
              matched = matcher.find();
            }
            if (ifNotExistsMatched || matched) {
              String currentTableName = matcher.group(1);
              if (tableNameToCheckExistence == null && matched) {
                tableNameToCheckExistence = currentTableName;
              }
              dropTableQueries.add("drop table " + currentTableName);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean tablesExist() {
    try {
      return JdbcSqlQuery.createCountFor("INFORMATION_SCHEMA.TABLES")
          .where("lower(TABLE_NAME) = lower(?)", tableNameToCheckExistence).execute() > 0;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createTables() {
    Transaction.performInOne(() -> JdbcSqlQueries.from(initQueries).execute());
  }

  @Override
  public void dropTables() {
    Transaction.performInOne(() -> JdbcSqlQueries.from(dropTableQueries).execute());
  }
}
