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

package org.silverpeas.core.persistence.jdbc.sql;

import org.silverpeas.core.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlExecutorProvider.getJdbcSqlExecutor;

/**
 * This class permits to build easily a SQL query with parameters.
 * It permits also to execute directly the query.
 * @author Yohann Chastagnier
 */
public class JdbcSqlQuery {

  private final Configuration configuration = new Configuration();
  private final StringBuilder sqlQuery = new StringBuilder();
  private final Collection<Object> allParameters = new ArrayList<>();

  /**
   * Indicates if the specified value is defined in point of view of SQL.
   * @param sqlValue the value to verify.
   * @return true if defined, false otherwise.
   */
  public static boolean isSqlDefined(String sqlValue) {
    return StringUtil.isDefined(sqlValue) && !sqlValue.trim().equals("-1") &&
        !sqlValue.trim().equals("unknown");
  }

  /**
   * Gets from a entity list the unique entity expected.
   * @param entities the entity list.
   * @return the unique entity result.
   * @throws IllegalArgumentException if it exists more than one entity in the specified
   * list.
   */
  public static <ENTITY> ENTITY unique(List<ENTITY> entities) {
    if (entities.isEmpty()) {
      return null;
    }
    if (entities.size() == 1) {
      return entities.get(0);
    }
    throw new IllegalArgumentException(
        "wanted to get a unique entity from a list that contains more than one...");
  }

  /**
   * Creates a new instance of the SQL builder initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery create(String sqlPart, Object... paramValue) {
    return new JdbcSqlQuery().addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the SQL builder initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery create(String sqlPart, Collection<?> paramValue) {
    return new JdbcSqlQuery().addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the SQL builder initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createSelect(String sqlPart, Object... paramValue) {
    return new JdbcSqlQuery().addSqlPart("select").addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the SQL builder initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createSelect(String sqlPart, Collection<?> paramValue) {
    return new JdbcSqlQuery().addSqlPart("select").addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the SQL builder initialized for count.
   * @param tableName the table name aimed by the count.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createCountFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("select count(*) from").addSqlPart(tableName);
  }

  /**
   * Creates a new instance of the SQL builder initialized for table creation.
   * @param tableName the table name aimed by the insert.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createTable(String tableName) {
    return new JdbcSqlQuery().addSqlPart("create table").addSqlPart(tableName).addSqlPart(" (");
  }

  /**
   * Creates a new instance of the SQL builder initialized for insert.
   * @param tableName the table name aimed by the insert.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createInsertFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("insert into").addSqlPart(tableName).addSqlPart(" (");
  }

  /**
   * Creates a new instance of the SQL builder initialized for update.
   * @param tableName the table name aimed by the update.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createUpdateFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("update ").addSqlPart(tableName).addSqlPart(" set");
  }

  /**
   * Creates a new instance of the SQL builder initialized for delete.
   * @param tableName the table name aimed by the delete.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createDeleteFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("delete from").addSqlPart(tableName);
  }

  /**
   * Creates a new instance of the SQL builder initialized for delete.
   * @param tableName the table name aimed by the drop.
   * @return the instance of the new builder.
   */
  public static JdbcSqlQuery createDropFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("drop table").addSqlPart(tableName);
  }


  private JdbcSqlQuery() {
    // Hidden constructor
  }

  /**
   * Gets the built SQL query.
   * @return the SQL query.
   */
  public String getSqlQuery() {
    return sqlQuery.toString();
  }

  /**
   * Gets the parameters to apply to the SQL query.
   * @return the parameters to apply to the SQL query.
   */
  public Collection<?> getParameters() {
    return allParameters;
  }

  /**
   * Calling this method to configure some parameters around execution, result, etc.
   * @param config the configuration instance.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery configure(Consumer<Configuration> config) {
    config.accept(this.configuration);
    return this;
  }

  /**
   * Gets the configuration of the query.
   * @return the configuration.
   */
  Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Centralization in order to populate the prepare statement parameters (FOR TABLE CREATION ONLY).
   * @param fieldName the name of the field to define.
   * @param definition the definition of the field.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addField(String fieldName, String definition) {
    if (sqlQuery.charAt(sqlQuery.length() - 2) != ' ' &&
        sqlQuery.charAt(sqlQuery.length() - 1) != '(') {
      sqlQuery.append(", ");
    }
    return addSqlPart(fieldName).addSqlPart(definition);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery where(String sqlPart, Object... paramValue) {
    return where(sqlPart, Arrays.asList(paramValue));
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValues the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery where(String sqlPart, Collection<?> paramValues) {
    return addSqlPart("where " + sqlPart, paramValues);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery and(String sqlPart, Object... paramValue) {
    return and(sqlPart, Arrays.asList(paramValue));
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValues the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery and(String sqlPart, Collection<?> paramValues) {
    return addSqlPart("and " + sqlPart, paramValues);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery or(String sqlPart, Object... paramValue) {
    return or(sqlPart, Arrays.asList(paramValue));
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValues the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery or(String sqlPart, Collection<?> paramValues) {
    return addSqlPart("or " + sqlPart, paramValues);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addSqlPart(String sqlPart, Object... paramValue) {
    return addSqlPart(sqlPart, Arrays.asList(paramValue));
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValues the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addSqlPart(String sqlPart, Collection<?> paramValues) {
    allParameters.addAll(paramValues);
    if (sqlQuery.length() > 0) {
      char lastChar = sqlQuery.charAt(sqlQuery.length() - 1);
      if (lastChar != ' ' && lastChar != '(') {
        sqlQuery.append(" ");
      }
    }
    sqlQuery.append(sqlPart.trim().replaceAll("[ ]+", " "));
    return this;
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery in(Collection<?> parameters) {
    sqlQuery.append(" in");
    addListOfParameters(parameters, true);
    return this;
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery in(Object... parameters) {
    sqlQuery.append(" in");
    addListOfParameters(Arrays.asList(parameters), true);
    return this;
  }

  /**
   * Centralization in order to build easily a SQL values clause.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  private JdbcSqlQuery valuesForInsert() {
    sqlQuery.append(") values");
    addListOfParameters(allParameters, false);
    return this;
  }

  /**
   * Centralization in order to build easily a SQL values clause.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  private JdbcSqlQuery finalizeTableCreation() {
    sqlQuery.append(")");
    return this;
  }

  private JdbcSqlQuery addListOfParameters(Collection<?> parameters,
      final boolean addToParameters) {
    StringBuilder params = new StringBuilder();
    if (parameters != null) {
      for (Object ignored : parameters) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
      }
      if (addToParameters) {
        allParameters.addAll(parameters);
      }
    }
    sqlQuery.append(" (").append(params.toString()).append(")");
    return this;
  }

  /**
   * Centralization in order to populate the prepare statement parameters for insertion.
   * @param paramName the name of the parameter to add into update fields part.
   * @param paramValue the value of the parameter.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addInsertParam(String paramName, Object paramValue) {
    return addSaveParam(paramName, paramValue, true);
  }

  /**
   * Centralization in order to populate the prepare statement parameters for update.
   * @param paramName the name of the parameter to add into update fields part.
   * @param paramValue the value of the parameter.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addUpdateParam(String paramName, Object paramValue) {
    return addSaveParam(paramName, paramValue, false);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param paramName the name of the parameter to add into update fields part.
   * @param paramValue the value of the parameter.
   * @param isInsert indicates if the SQL built is an INSERT or an UPDATE one.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery addSaveParam(String paramName, Object paramValue, final boolean isInsert) {
    if (allParameters.size() > 0) {
      sqlQuery.append(", ");
    } else if (!isInsert) {
      sqlQuery.append(" ");
    }
    sqlQuery.append(paramName.trim());
    if (!isInsert) {
      sqlQuery.append(" = ?");
    }
    allParameters.add(paramValue);
    return this;
  }

  /**
   * Select executor.
   * @throws java.sql.SQLException
   */
  public <ROW_ENTITY> List<ROW_ENTITY> execute(SelectResultRowProcess<ROW_ENTITY> process)
      throws SQLException {
    return getJdbcSqlExecutor().select(this, process);
  }

  /**
   * Select executor.
   * @throws java.sql.SQLException
   */
  public <ROW_ENTITY> ROW_ENTITY executeUnique(SelectResultRowProcess<ROW_ENTITY> process)
      throws SQLException {
    return unique(execute(process));
  }

  /**
   * This method has to be called before the SQL query is being executed.
   */
  void finalizeBeforeExecution() {
    String computedSqlQuery = getSqlQuery();
    if (computedSqlQuery.startsWith("insert into ")) {
      valuesForInsert();
    } else if (computedSqlQuery.startsWith("create table ")) {
      finalizeTableCreation();
    }
  }

  /**
   * Modify executor.
   * @throws java.sql.SQLException
   */
  public long execute() throws SQLException {
    String sqlQuery = getSqlQuery();
    if (sqlQuery.startsWith("select count(*)")) {
      return getJdbcSqlExecutor().selectCount(this);
    }
    return getJdbcSqlExecutor().executeModify(Collections.singletonList(this));
  }

  /**
   * Context of execution that has to be taken into account.
   */
  public static class Configuration {
    private int limit = 0;

    int getResultLimit() {
      return limit;
    }

    public Configuration withResultLimit(final int limit) {
      this.limit = limit;
      return this;
    }
  }
}
