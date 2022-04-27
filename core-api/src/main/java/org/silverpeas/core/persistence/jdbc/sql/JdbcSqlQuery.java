/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jdbc.sql;

import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlExecutorProvider.getJdbcSqlExecutor;

/**
 * This class permits to build easily a SQL query with parameters.
 * It permits also to execute directly the query.
 * @author Yohann Chastagnier
 */
public class JdbcSqlQuery {

  // The value of 1500 has been chosen after some production problem with some kind of databases...
  public static final int SPLIT_BATCH = 1500;
  private static final int SPACE_OFFSET_DETECTION = 2;
  private static final int OPEN_PARENTHESIS_OFFSET_DETECTION = 1;
  private static final String NOT_IN_OPERATOR = " NOT IN ";
  private static final String IN_OPERATOR = " IN ";
  private final Configuration configuration = new Configuration();
  private final StringBuilder sqlQuery = new StringBuilder();
  private final Collection<Object> allParameters = new ArrayList<>();

  private JdbcSqlQuery() {
    // Hidden constructor
  }

  /**
   * Indicates if the specified value is defined in point of view of SQL.
   * @param sqlValue the value to verify.
   * @return true if defined, false otherwise.
   */
  public static boolean isSqlDefined(String sqlValue) {
    return StringUtil.isDefined(sqlValue) && !"-1".equals(sqlValue.trim()) &&
        !"unknown".equals(sqlValue.trim());
  }

  /**
   * Gets from a entity list the unique entity expected.
   * @param <E> the type of the entities.
   * @param entities the entity list.
   * @return the unique entity result.
   * @throws IllegalArgumentException if it exists more than one entity in the specified
   * list.
   */
  public static <E> E unique(List<E> entities) {
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
   * Creates a new instance of the JDBC SQL query initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery create(String sqlPart, Object... paramValue) {
    return new JdbcSqlQuery().addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the JDBC SQL query initialized with the given sql part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return @return the instance of the new sql query.
   */
  public static JdbcSqlQuery create(String sqlPart, Collection<?> paramValue) {
    return new JdbcSqlQuery().addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the JDBC SQL query to select some fields of the items to find
   * according to the specified SQL part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createSelect(String sqlPart, Object... paramValue) {
    return new JdbcSqlQuery().addSqlPart("SELECT").addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the JDBC SQL query to select some fields of the items to find
   * according to the specified SQL part.
   * @param sqlPart the sql part to append.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createSelect(String sqlPart, Collection<?> paramValue) {
    return new JdbcSqlQuery().addSqlPart("SELECT").addSqlPart(sqlPart, paramValue);
  }

  /**
   * Creates a new instance of the SQL query to count the items that are in the specified table.
   * @param tableName the table name aimed by the count.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createCountFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("SELECT COUNT(*) FROM").addSqlPart(tableName);
  }

  /**
   * Creates a new instance of the SQL query to create the specified table.
   * @param tableName the table name aimed by the insert.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createTable(String tableName) {
    return new JdbcSqlQuery().addSqlPart("CREATE TABLE").addSqlPart(tableName).addSqlPart(" (");
  }

  /**
   * Creates a new instance of the SQL query to insert one or more items in the specified table.
   * @param tableName the table name aimed by the insert.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createInsertFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("INSERT INTO").addSqlPart(tableName).addSqlPart(" (");
  }

  /**
   *  Creates a new instance of the SQL query to update some items in the specified table.
   * @param tableName the table name aimed by the update.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createUpdateFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("UPDATE ").addSqlPart(tableName).addSqlPart(" SET");
  }

  /**
   * Creates a new instance of the SQL query to delete some items in the specified table.
   * @param tableName the table name aimed by the delete.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createDeleteFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("DELETE FROM").addSqlPart(tableName);
  }

  /**
   * Creates a new instance of the SQL query to drop the specified table.
   * @param tableName the table name aimed by the drop.
   * @return the instance of the new sql query.
   */
  public static JdbcSqlQuery createDropFor(String tableName) {
    return new JdbcSqlQuery().addSqlPart("DROP TABLE").addSqlPart(tableName);
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
  public Collection<Object> getParameters() {
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
    if (sqlQuery.charAt(sqlQuery.length() - SPACE_OFFSET_DETECTION) != ' ' &&
        sqlQuery.charAt(sqlQuery.length() - OPEN_PARENTHESIS_OFFSET_DETECTION) != '(') {
      sqlQuery.append(", ");
    }
    return addSqlPart(fieldName).addSqlPart(definition);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery union() {
    return addSqlPart("UNION ");
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery join(String sqlPart) {
    return addSqlPart("JOIN " + sqlPart);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery outerJoin(String sqlPart) {
    return addSqlPart("LEFT OUTER JOIN " + sqlPart);
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlPart the SQL part that contains the parameter.
   * @param paramValue the value of parameters included into the given sqlPart.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery on(String sqlPart, Object... paramValue) {
    return addSqlPart("ON " + sqlPart, Arrays.asList(paramValue));
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
    return addSqlPart("WHERE " + sqlPart, paramValues);
  }

  /**
   * The query is about the concerned SQL table.
   * @param tableNames the name of the table(s) concerned by the query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery from(String... tableNames) {
    return addSqlPart("FROM " + Arrays.stream(tableNames).collect(Collectors.joining(",")));
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
    return addSqlPart("AND " + sqlPart, paramValues);
  }

  /**
   * The specified parameter, in a conjunction filter, must not be null when requesting the
   * data source.
   * @param parameter the parameter that has to be not null.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery andNotNull(final String parameter) {
    return addSqlPart("AND " + parameter + " IS NOT NULL");
  }

  /**
   * The specified parameter, in a conjunction filter, must be null when requesting the
   * data source.
   * @param parameter the parameter that has to be not null.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery andNull(final String parameter) {
    return addSqlPart("AND " + parameter + " IS NULL");
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
    return addSqlPart("OR " + sqlPart, paramValues);
  }

  /**
   * The specified parameter, in a disjunction filter, must not be null when requesting the
   * data source.
   * @param parameter the parameter that has to be not null.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery orNotNull(final String parameter) {
    return addSqlPart("OR " + parameter + " IS NOT NULL");
  }

  /**
   * The specified parameter, in a conjunction filter, must null when requesting the
   * data source.
   * @param parameter the parameter that has to be not null.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery orNull(final String parameter) {
    return addSqlPart("OR " + parameter + " IS NULL");
  }

  /**
   * Orders the result of the query by the specified statement. If the statement isn't defined,
   * then no ordering will be done.
   * @param sqlPart the SQL part that contains the statement over which the result of the query
   * should be ordered.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery orderBy(String sqlPart) {
    if (StringUtil.isDefined(sqlPart)) {
      return addSqlPart("ORDER BY " + sqlPart);
    }
    return this;
  }

  /**
   * Group the result of the query by the specified columns. If the statement isn't defined,
   * then no group by will be done.
   * @param sqlPart the SQL part that contains the statement over which the result of the query
   * should be grouped.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery groupBy(String sqlPart) {
    if (StringUtil.isDefined(sqlPart)) {
      return addSqlPart("GROUP BY " + sqlPart);
    }
    return this;
  }

  /**
   * Limits the count of result returned by the query. This overrides any previous value of the
   * limit property in the configuration.
   * @param count the size of results to return.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery limit(int count) {
    this.configuration.withResultLimit(count);
    return this;
  }

  /**
   * Sets the offset from which each result should be processed by the row processor. The other
   * results returned by the query will be ignored.
   * This overrides any previous value of the offset property in the configuration.
   * @param offset the offset from which the row processing has to start.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery offset(int offset) {
    this.configuration.withOffset(offset);
    return this;
  }

  /**
   * Configures the query execution in order to retrieve only items of pagination.<br>
   * Be careful to execute a SQL query containing an {@code ORDER BY} clause!!!
   * @param pagination the pagination criterion to apply.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery withPagination(PaginationCriterion pagination) {
    if (pagination != null && pagination.isDefined()) {
      offset((pagination.getPageNumber() - 1) * pagination.getItemCount());
      limit(pagination.getItemCount());
      if (!pagination.isOriginalSizeNeeded()) {
        this.configuration.ignoreRealOriginalSize();
      }
    }
    return this;
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
  private JdbcSqlQuery addSqlPart(String sqlPart, Collection<?> paramValues) {
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
   * <p>If one element exists into list, an equality is performed instead of a in</p>
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery in(Collection<?> parameters) {
    if (CollectionUtil.isEmpty(parameters)) {
      throw new IllegalArgumentException(
          "cannot apply in clause because no value to set ! query=" + sqlQuery);
    }
    if (parameters.size() == 1) {
      addSqlPart(" = ?", parameters);
    } else {
      sqlQuery.append(IN_OPERATOR);
      addListOfParameters(parameters, true);
    }
    return this;
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * <p>If one element exists into list, an equality is performed instead of a in</p>
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery in(Object... parameters) {
    if (ArrayUtils.isEmpty(parameters)) {
      throw new IllegalArgumentException(
          "cannot apply in clause because no value to set ! query=" + sqlQuery);
    }
    if (parameters.length == 1) {
      addSqlPart("= ?", parameters);
    } else {
      sqlQuery.append(IN_OPERATOR);
      addListOfParameters(Arrays.asList(parameters), true);
    }
    return this;
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * <p>If one element exists into list, a non equality is performed instead of a not in</p>
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery notIn(Collection<?> parameters) {
    if (CollectionUtil.isEmpty(parameters)) {
      throw new IllegalArgumentException(
          "cannot apply not in clause because no value to set ! query=" + sqlQuery);
    }
    if (parameters.size() == 1) {
      addSqlPart("<> ?", parameters);
    } else {
      sqlQuery.append(NOT_IN_OPERATOR);
      addListOfParameters(parameters, true);
    }
    return this;
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * <p>If one element exists into list, a non equality is performed instead of a not in</p>
   * @param parameters the parameters to append to the given SQL query.
   * @return the instance of {@link JdbcSqlQuery} that represents the SQL query.
   */
  public JdbcSqlQuery notIn(Object... parameters) {
    if (ArrayUtils.isEmpty(parameters)) {
      throw new IllegalArgumentException(
          "cannot apply not in clause because no value to set ! query=" + sqlQuery);
    }
    if (parameters.length == 1) {
      addSqlPart("<> ?", parameters);
    } else {
      sqlQuery.append(NOT_IN_OPERATOR);
      addListOfParameters(Arrays.asList(parameters), true);
    }
    return this;
  }

  /**
   * Centralization in order to build easily a SQL values clause.
   */
  private void valuesForInsert() {
    if (!isSqlQueryCompleted()) {
      sqlQuery.append(") values");
      addListOfParameters(allParameters, false);
    }
  }

  /**
   * Centralization in order to build easily a SQL values clause.
   */
  private void finalizeTableCreation() {
    if (!isSqlQueryCompleted()) {
      sqlQuery.append(")");
    }
  }

  /**
   * Indicates if the query seems completed.
   * @return true if query is completed, false otherwise.
   */
  private boolean isSqlQueryCompleted() {
    final char lastChar = sqlQuery.charAt(sqlQuery.length() - 1);
    int openParenthesisCount = 0;
    int closeParenthesisCount = 0;
    for (int i = 0; i < sqlQuery.length(); i++) {
      final char currentChar = sqlQuery.charAt(i);
      if (currentChar == '(') {
        openParenthesisCount++;
      } else if (currentChar == ')') {
        closeParenthesisCount++;
      }
    }
    return lastChar == ';' || openParenthesisCount == closeParenthesisCount;
  }

  private void addListOfParameters(Collection<?> parameters,
      final boolean addToParameters) {
    if (parameters == null) {
      return;
    }
    // Oracle has a hard limitation with SQL lists with 'in' clause: it cannot take more than 1000
    // elements. So we split it in several SQL lists so that they contain less than 1000 elements.
    final int threshold = 1000;
    int end = -1;
    String negation = StringUtil.EMPTY;
    if (parameters.size() > threshold) {
      end = sqlQuery.lastIndexOf(NOT_IN_OPERATOR);
      negation = " NOT ";
      if ((end + NOT_IN_OPERATOR.length()) < sqlQuery.length()) {
        end = sqlQuery.lastIndexOf(IN_OPERATOR);
        negation = "";
        if ((end + IN_OPERATOR.length()) < sqlQuery.length()) {
          end = -1;
        }
      }
    }
    if (end > -1) {
      int from = end - 1;
      while (sqlQuery.charAt(from) != ' ') {
        from--;
      }
      String currentSqlPart = sqlQuery.substring(from + 1, end);
      sqlQuery.delete(from + 1, sqlQuery.length()).append(negation).append("(");

      List<?> listOfParameters = new ArrayList<>(parameters);
      for (int i = 0; i < listOfParameters.size(); i += threshold) {
        String params = sublistOfParameters(i, i + threshold, listOfParameters);
        sqlQuery.append(currentSqlPart).append(" IN (").append(params).append(") OR ");
      }
      final int lengthOfORLink = 4;
      sqlQuery.replace(sqlQuery.length() - lengthOfORLink, sqlQuery.length(), ")");

    } else {
      String params = parameters.stream().map(p -> "?").collect(Collectors.joining(","));
      sqlQuery.append(" (").append(params).append(")");
    }
    if (addToParameters) {
      allParameters.addAll(parameters);
    }
  }

  private String sublistOfParameters(final int fromIndex, final int toIndex,
      final List<?> listOfParameters) {
    int limit = toIndex;
    if (limit > listOfParameters.size()) {
      limit = listOfParameters.size();
    }
    return listOfParameters.subList(fromIndex, limit)
        .stream()
        .map(p -> "?")
        .collect(Collectors.joining(","));
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
    if (!allParameters.isEmpty()) {
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
   * Split executor.
   * @param <I> the type of list of discriminant data.
   * @param <T> the type of the entity into result.
   * @param discriminantData a discriminant list of data.
   * @return a mapping between given discriminant identifiers and the corresponding data.
   * @throws java.sql.SQLException on SQL error.
   */
  public static <I, T> Map<I, List<T>> executeBySplittingOn(final Collection<I> discriminantData,
      final SplitExecuteProcess<I, T> process) throws SQLException {
    final Map<I, List<T>> result = new HashMap<>(discriminantData.size());
    for (Collection<I> d : CollectionUtil.split(discriminantData, SPLIT_BATCH)) {
      process.execute(d, result);
    }
    return result;
  }

  /**
   * Split executor.
   * @param <I> the type of list of discriminant data.
   * @param <T> the type of the entity into result.
   * @param discriminantData a discriminant list of data.
   * @return a stream between given discriminant identifiers and the corresponding data.
   * @throws java.sql.SQLException on SQL error.
   */
  public static <I, T> Stream<T> streamBySplittingOn(final Collection<I> discriminantData,
      final SplitListProcess<I, List<T>> process) throws SQLException {
    Stream<T> result = Stream.empty();
    for (Collection<I> d : CollectionUtil.split(discriminantData, SPLIT_BATCH)) {
      result = Stream.concat(result, process.execute(d).stream());
    }
    return result;
  }

  /**
   * Split executor giving a result sorted exactly like the discriminantData parameter is sorted.
   * @param <I> the type of list of discriminant data.
   * @param <T> the type of the entity into result.
   * @param discriminantData a discriminant list of data.
   * @param idGetter permits to get the id from T entity in order to sort the result.
   * @return a stream between given discriminant identifiers and the corresponding data.
   * @throws java.sql.SQLException on SQL error.
   */
  public static <I, T> Stream<T> streamBySplittingOn(final Collection<I> discriminantData,
      final SplitListProcess<I, List<T>> process, Function<T, I> idGetter) throws SQLException {
    final Map<I, T> indexedResult = streamBySplittingOn(discriminantData, process)
        .collect(toMap(idGetter, r -> r));
    return discriminantData.stream().map(indexedResult::get).filter(Objects::nonNull);
  }

  /**
   * Select executor.
   * @param <R> the type of the items in the list.
   * @param process the process to execute on the ResultSet objects.
   * @return a slice of the list of entities matching the query. The slice is computed from the
   * query configuration {@link Configuration}.
   * @throws java.sql.SQLException on SQL error.
   */
  public <R> ListSlice<R> execute(SelectResultRowProcess<R> process)
      throws SQLException {
    return executeWith(null, process);
  }

  /**
   * Select executor.
   * @param <R> the type of the items in the list.
   * @param connection existing connection.
   * @param process the process to execute on the ResultSet objects.
   * @return a slice of the list of entities matching the query. The slice is computed from the
   * query configuration {@link Configuration}.
   * @throws java.sql.SQLException on SQL error.
   */
  public <R> ListSlice<R> executeWith(Connection connection, SelectResultRowProcess<R> process)
      throws SQLException {
    if (connection == null) {
      return getJdbcSqlExecutor().select(this, process);
    } else {
      return getJdbcSqlExecutor().select(connection, this, process);
    }
  }

  /**
   * Select executor.
   * @param <R> the type of the entity.
   * @param process the process to execute on the ResultSet objects.
   * @return the entity matching the query.
   * @throws java.sql.SQLException on SQL error.
   */
  public <R> R executeUnique(SelectResultRowProcess<R> process)
      throws SQLException {
    return executeUniqueWith(null, process);
  }

  /**
   * Select executor.
   * @param <R> the type of the entity.
   * @param connection existing connection.
   * @param process the process to execute on the ResultSet objects.
   * @return the entity matching the query.
   * @throws java.sql.SQLException on SQL error.
   */
  public <R> R executeUniqueWith(Connection connection, SelectResultRowProcess<R> process)
      throws SQLException {
    return unique(executeWith(connection, process));
  }

  /**
   * This method has to be called before the SQL query is being executed.
   */
  void finalizeBeforeExecution() {
    String computedSqlQuery = getSqlQuery();
    if (computedSqlQuery.startsWith("INSERT INTO ")) {
      valuesForInsert();
    } else if (computedSqlQuery.startsWith("CREATE TABLE ")) {
      finalizeTableCreation();
    }
  }

  /**
   * Modify executor.
   * @return the number of entities that were implied in the modification.
   * @throws java.sql.SQLException on SQL error.
   */
  public long execute() throws SQLException {
    return executeWith(null);
  }

  /**
   * Modify executor.
   * @param connection existing connection.
   * @return the number of entities that were implied in the modification.
   * @throws java.sql.SQLException on SQL error.
   */
  public long executeWith(Connection connection) throws SQLException {
    final int lengthOfStartStatement = 13;
    String builtSqlQuery = getSqlQuery().trim();
    if ("SELECT COUNT(".equalsIgnoreCase(builtSqlQuery.substring(0, lengthOfStartStatement))) {
      if (connection == null) {
        return getJdbcSqlExecutor().selectCount(this);
      } else {
        return getJdbcSqlExecutor().selectCount(connection, this);
      }
    }
    if (connection == null) {
      return getJdbcSqlExecutor().executeModify(Collections.singletonList(this));
    } else {
      return getJdbcSqlExecutor().executeModify(connection, Collections.singletonList(this));
    }
  }

  /**
   * Configuration of execution that has to be taken into account.
   */
  public static class Configuration {
    private int limit = 0;
    private int offset = 0;
    private boolean needRealOriginalSize = true;

    int getResultLimit() {
      return limit;
    }

    int getOffset() {
      return offset;
    }

    boolean isFirstResultScrolled() {
      return offset > 0;
    }

    boolean isResultCountLimited() {
      return limit > 0;
    }

    public boolean isNeedRealOriginalSize() {
      return needRealOriginalSize;
    }

    public Configuration withResultLimit(final int limit) {
      if (limit < 0) {
        throw new IllegalArgumentException("Invalid limit: expected positive value");
      }
      this.limit = limit;
      return this;
    }

    public Configuration withOffset(final int offset) {
      if (offset < 0) {
        throw new IllegalArgumentException("Invalid offset: expected positive value");
      }
      this.offset = offset;
      return this;
    }

    public Configuration ignoreRealOriginalSize() {
      this.needRealOriginalSize = false;
      return this;
    }
  }
}
