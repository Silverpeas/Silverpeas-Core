/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception.  You should have received a copy of the text describi
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Criteria on the beans the DAO has to satisfy while performing its persistence operation.
 *
 * @author mmoquillon
 */
public class BeanCriteria {

  private static final String AND = " AND ";
  private static final String OR = " OR ";

  /**
   * SQL operator a field has to satisfy against a value.
   */
  public enum OPERATOR {
    /**
     * The field of a bean has to be equal to a given value.
     */
    EQUALS(" = "),
    /**
     * The field of a bean has to be different than the given value.
     */
    NOT_EQUALS(" <> "),
    /**
     * The field of a bean has to greater than the given value.
     */
    GREATER(" > "),
    /**
     * The fields of a bean has to be greater or equal than the given value.
     */
    GREATER_OR_EQUAL(" >= "),
    /**
     * The textual field of a bean has to match a given pattern.
     */
    LIKE(" LIKE "),
    /**
     * The value of the field of a bean must be included within a given list of possible values.
     */
    IN(" IN ");

    private final String op;

    OPERATOR(String operator) {
      this.op = operator;
    }

    @Override
    public String toString() {
      return op;
    }
  }

  private static final String WHERE = " WHERE ";

  private final StringBuilder filter = new StringBuilder();
  private String orderBy = "";
  private final List<Object> params = new ArrayList<>();

  private BeanCriteria() {
  }

  /**
   * Constructs an empty criteria object.
   *
   * @return a {@link BeanCriteria} instance.
   */
  public static BeanCriteria emptyCriteria() {
    return new BeanCriteria();
  }

  /**
   * Constructs a criteria by specifying the given first criterion on beans. The property must be
   * equal to the specified value.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValue the value to which the property has to be equal. Shouldn't be a collection
   * of values.
   * @return a new {@link BeanCriteria} instance with a first criterion.
   */
  public static BeanCriteria addCriterion(String propertyName, Object propertyValue) {
    return addCriterion(propertyName, OPERATOR.EQUALS, propertyValue);
  }

  /**
   * Constructs a criteria by specifying the given first criterion on beans. The property must match
   * the specified value according to the given operator.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the value.
   * @param propertyValue the value to be satisfied by the property according to the operator. For a
   * collection of values, please use instead {@link BeanCriteria#addCriterion(String, Collection)}
   * @return a new {@link BeanCriteria} instance with a first criterion.
   */
  public static BeanCriteria addCriterion(String propertyName, OPERATOR operator,
      Object propertyValue) {
    if (operator == OPERATOR.IN && propertyValue instanceof Collection) {
      return addCriterion(propertyName, (Collection<?>) propertyValue);
    }
    return emptyCriteria().and(propertyName, operator, propertyValue);
  }

  /**
   * Constructs a criteria by specifying the given first criterion on beans. The property must be
   * included in the specified set of values.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValues a collection of possible values of the property.
   * @return a new {@link BeanCriteria} instance with a first criterion.
   */
  public static BeanCriteria addCriterion(String propertyName, Collection<?> propertyValues) {
    return emptyCriteria().and(propertyName, propertyValues);
  }

  /**
   * Is this criteria empty? A criteria is empty whether there is no criterion set.
   *
   * @return true if there is no criteria set.
   */
  public boolean isEmpty() {
    return filter.length() == 0;
  }

  /**
   * Adds a new criterion that has also to be satisfied. The property must be equal to the specified
   * value.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValue the value to which the property has to be equal.
   * @return itself.
   */
  public BeanCriteria and(String propertyName, Object propertyValue) {
    return and(propertyName, OPERATOR.EQUALS, propertyValue);
  }

  /**
   * Adds a new criterion that has to be also satisfied. The property must be equal to the specified
   * value on which the given SQL function has been previously applied.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValue the value to which the property has to be equal.
   * @param sqlFunction a database function to apply on the value with which the property is
   * compared. For example, LOWER.
   * @return itself.
   */
  public BeanCriteria andWithFunction(String propertyName, Object propertyValue,
      String sqlFunction) {
    return and(propertyName, OPERATOR.EQUALS, propertyValue, sqlFunction);
  }

  /**
   * Adds a new criterion that has to be also satisfied. The property must match the specified value
   * according to the given operator.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the value.
   * @param propertyValue the value to be satisfied by the property according to the operator. For a
   * collection of values, please use instead {@link #and(String, Collection)}
   * @return itself.
   */
  public BeanCriteria and(String propertyName, OPERATOR operator, Object propertyValue) {
    if (operator == OPERATOR.IN && propertyValue instanceof Collection) {
      return and(propertyName, (Collection<?>) propertyValue);
    }
    return and(propertyName, operator, propertyValue, null);
  }

  /**
   * Adds a new criterion that has to be also satisfied. The property must match the specified value
   * according to the given operator and on which the specified SQL function has been previously
   * applied.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the value.
   * @param propertyValue the value to be satisfied by the property according to the operator. The
   * value shouldn't be a collection of values. For this, please use instead
   * {@link #and(String, Collection)}
   * @param sqlFunction a database function to apply on the value with which the property is
   * compared by the given operator. For example, LOWER.
   * @return itself.
   */
  public BeanCriteria and(String propertyName, OPERATOR operator, Object propertyValue,
      String sqlFunction) {
    return computeCriterion(AND, propertyName, operator, propertyValue, sqlFunction);
  }

  /**
   * Adds a new criterion that has to be also satisfied. The property must be included in the
   * specified set of values.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValues a collection of possible values of the property.
   * @return itself.
   */
  public BeanCriteria and(String propertyName, Collection<?> propertyValues) {
    if (!propertyValues.isEmpty()) {
      StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(AND);
      setPropertyInPropertyValues(clause, propertyName, propertyValues);
    }
    return this;
  }

  /**
   * Adds a new criterion that can be satisfied if the previous any ones weren't. The property could
   * be equal to the given value.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValue the value to which the property has to be equal.
   * @return itself.
   */
  public BeanCriteria or(String propertyName, Object propertyValue) {
    return or(propertyName, OPERATOR.EQUALS, propertyValue);
  }

  /**
   * Adds a new criterion that can be satisfied if the previous any ones weren't. The property could
   * be equal to the specified value on which the given SQL function has been previously applied.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValue the value to which the property has to be equal.
   * @param sqlFunction a database function to apply on the value with which the property is
   * compared. For example, LOWER.
   * @return itself.
   */
  public BeanCriteria orWithFunction(String propertyName, Object propertyValue,
      String sqlFunction) {
    return or(propertyName, OPERATOR.EQUALS, propertyValue, sqlFunction);
  }

  /**
   * Adds a new criterion that can been satisfied if the previous any ones weren't. The property
   * could match the given value according to the specified operator.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the value.
   * @param propertyValue the value to be satisfied by the property according to the operator.
   * @return itself.
   */
  public BeanCriteria or(String propertyName, OPERATOR operator, Object propertyValue) {
    return or(propertyName, operator, propertyValue, null);
  }

  /**
   * Adds a new criterion that can be satisfied if the previous any one weren't. The property could
   * match the specified value according to the given operator and on which the specified SQL
   * function has been previously applied.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the value.
   * @param propertyValue the value to be satisfied by the property according to the operator.
   * @param sqlFunction a database function to apply on the value with which the property is
   * compared by the given operator. For example, LOWER.
   * @return itself.
   */
  public BeanCriteria or(String propertyName, OPERATOR operator, Object propertyValue,
      String sqlFunction) {
    return computeCriterion(OR, propertyName, operator, propertyValue, sqlFunction);
  }

  /**
   * Adds a new criterion that can been satisfied if the previous any ones weren't. The property
   * could to be included in the specified set of values.
   *
   * @param propertyName the name of a property of the bean.
   * @param propertyValues a set of possible values of the property.
   * @return itself.
   */
  public BeanCriteria or(String propertyName, Set<?> propertyValues) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(OR);
    setPropertyInPropertyValues(clause, propertyName, propertyValues);
    return this;
  }

  /**
   * Adds a new criterion that has to be also satisfied. The property must match the value(s)
   * computed by the specified subquery on which has been also applied the given criteria.
   *
   * @param propertyName the name of a property of the bean.
   * @param operator the operator to apply on the property against the computed value.
   * @param query the query to use to compute the value(s) the property has to satisfy according the
   * operator.
   * @param queryCriteria some criteria to apply on the subquery.
   * @return itself.
   */
  public BeanCriteria andSubQuery(String propertyName, OPERATOR operator, String query,
      BeanCriteria queryCriteria) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(AND);
    clause.append(propertyName)
        .append(operator.toString())
        .append("(SELECT ")
        .append(query)
        .append(" ")
        .append(queryCriteria.toString())
        .append(")");
    params.addAll(queryCriteria.params);
    return this;
  }

  /**
   * Adds a set of criteria to satisfy with this current one.
   *
   * @param criteria another set of criteria to satisfy.
   * @return itself.
   */
  public BeanCriteria and(BeanCriteria criteria) {
    return append(criteria, AND);
  }

  /**
   * Adds a set of criteria that can been satisfied whether this current one isn't satisfied.
   *
   * @param criteria another set of criteria to satisfy.
   * @return itself.
   */
  public BeanCriteria or(BeanCriteria criteria) {
    return append(criteria, OR);
  }

  /**
   * Specifies the results of the application of the criteria on the beans to get have to be ordered
   * in a descending way by the specified bean properties.
   *
   * @param propertyNames one or several property names.
   */
  public void setDescOrderBy(String... propertyNames) {
    orderBy = " ORDER BY " + String.join(" DESC, ", propertyNames) + " DESC";
  }

  /**
   * Specifies the results of the application of the criteria on the beans to get have to be ordered
   * in an ascending way (default one) by the specified bean properties.
   *
   * @param propertyNames one or several property names.
   */
  public void setAscOrderBy(String... propertyNames) {
    orderBy = " ORDER BY " + String.join(" ASC, ", propertyNames) + " ASC";
  }

  @Override
  public String toString() {
    return filter + (StringUtil.isDefined(orderBy) ? orderBy : "");
  }

  @NonNull
  private BeanCriteria computeCriterion(String or, String propertyName, OPERATOR operator,
      Object propertyValue, String sqlFunction) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(or);
    String funcApplied = StringUtil.isDefined(sqlFunction) ? sqlFunction + "(?)" : "?";
    clause.append(propertyName)
        .append(operator.toString())
        .append(funcApplied);
    params.add(propertyValue);
    return this;
  }

  private BeanCriteria append(@NonNull BeanCriteria criteria, @NonNull String junctionOperator) {
    if (!criteria.isEmpty()) {
      StringBuilder clause = isEmpty() ? filter.append(WHERE).append("(") :
          filter.append(junctionOperator).append("(");
      clause.append(criteria.filter.substring(WHERE.length())).append(")");
      this.params.addAll(criteria.params);
    }
    return this;
  }

  private void setPropertyInPropertyValues(StringBuilder clause, String propertyName,
      Collection<?> propertyValues) {
    if (propertyValues.size() >= 1000) {
      clause.append("(");
      for (Object property : propertyValues) {
        clause.append(propertyName).append(" = ? ").append(OR);
        params.add(property);
      }
      clause.replace(clause.length() - OR.length(), clause.length(), ")");
    } else {
      clause.append(propertyName)
          .append(OPERATOR.IN)
          .append(" (");
      for (Object property : propertyValues) {
        clause.append("?,");
        params.add(property);
      }
      clause.replace(clause.length() - 1, clause.length(), ")");
    }
  }

  CriteriaApplication withConnection(Connection connection) {
    return new CriteriaApplication(connection);
  }

  /**
   * Application of the criteria onto a base query of beans.
   */
  class CriteriaApplication {

    private final Connection connection;

    private CriteriaApplication(Connection connection) {
      this.connection = connection;
    }

    /**
     * Applies the registered criteria to the specified SQL query and returns the resulting
     * {@link PreparedStatement} to execute.
     * @param query the base SQL query onto which the criteria will be applied.
     * @return a {@link PreparedStatement} built from both the specified query and the criteria.
     * @throws SQLException if an error occurs while preparing the SQL statement.
     */
    PreparedStatement applyTo(String query) throws SQLException {
      String finalQuery = query + BeanCriteria.this;
      //noinspection SqlSourceToSinkFlow
      PreparedStatement statement = connection.prepareStatement(finalQuery);
      for (int i = 0; i < params.size(); i++) {
        Object paramValue = params.get(i);
        if (paramValue instanceof String) {
          statement.setString(i + 1, (String) paramValue);
        } else if (paramValue instanceof Integer) {
          statement.setInt(i + 1, (int) paramValue);
        } else if (paramValue instanceof Long) {
          statement.setLong(i + 1, (long) paramValue);
        } else {
          throw new SilverpeasRuntimeException("Unsupported type: " +
              paramValue.getClass().getName());
        }
      }
      return statement;
    }
  }
}
  