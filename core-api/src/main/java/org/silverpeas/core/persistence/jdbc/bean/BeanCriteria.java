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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Criteria on the bean the DAO has to satisfy when performing its persistence operation.
 * @author mmoquillon
 */
public class BeanCriteria {

  public enum OPERATOR {
    EQUALS(" = "),
    LIKE(" LIKE "),
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
  private final List<Object> params = new ArrayList<>();

  private BeanCriteria() {
  }

  public static BeanCriteria emptyCriteria() {
    return new BeanCriteria();
  }

  public static BeanCriteria addCriterion(String propertyName, Object propertyValue) {
    return addCriterion(propertyName, propertyValue, OPERATOR.EQUALS);
  }

  public static BeanCriteria addCriterion(String propertyName, Object propertyValue,
      OPERATOR operator) {
    BeanCriteria criteria = new BeanCriteria();
    criteria.filter.append(WHERE)
        .append(propertyName)
        .append(operator.toString())
        .append("?");
    criteria.params.add(propertyValue);
    return criteria;
  }

  public boolean isEmpty() {
    return filter.length() == 0;
  }

  public BeanCriteria and(String propertyName, Object propertyValue) {
    return and(propertyName, propertyValue, OPERATOR.EQUALS);
  }

  public BeanCriteria and(String propertyName, Object propertyValue, OPERATOR operator) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(" AND ");
    clause.append(propertyName)
        .append(operator.toString())
        .append("?");
    params.add(propertyValue);
    return this;
  }

  public BeanCriteria or(String propertyName, Object propertyValue) {
    return or(propertyName, propertyValue, OPERATOR.EQUALS);
  }

  public BeanCriteria or(String propertyName, Object propertyValue, OPERATOR operator) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(" OR ");
    clause.append(propertyName)
        .append(operator.toString())
        .append("?");
    params.add(propertyValue);
    return this;
  }

  public BeanCriteria andSubQuery(String propertyName, String query, BeanCriteria queryCriteria) {
    return andSubQuery(propertyName, query, queryCriteria, OPERATOR.EQUALS);
  }

  public BeanCriteria andSubQuery(String propertyName, String query,
      BeanCriteria queryCriteria, OPERATOR operator) {
    StringBuilder clause = isEmpty() ? filter.append(WHERE) : filter.append(" OR ");
    clause.append(propertyName)
        .append(operator.toString())
        .append("(SELECT ")
        .append(query)
        .append(queryCriteria.toString())
        .append(")");
    params.addAll(queryCriteria.params);
    return this;
  }

  public BeanCriteria and(BeanCriteria criteria) {
    StringBuilder clause = isEmpty() ? filter.append(" WHERE (") : filter.append(" AND (");
    clause.append(criteria.toString())
        .append(")");
    params.addAll(criteria.params);
    return this;
  }

  public BeanCriteria or(BeanCriteria criteria) {
    StringBuilder clause = isEmpty() ? filter.append(" WHERE (") : filter.append(" OR (");
    clause.append(criteria.toString())
        .append(")");
    params.addAll(criteria.params);
    return this;
  }

  public void setOrderBy(String ... propertyNames) {
    filter.append(" ORDER BY ")
        .append(String.join(", ", propertyNames))
        .append(" DESC");
  }

 CriteriaApplication withConnection(Connection connection) {
    return new CriteriaApplication(connection);
 }

 class CriteriaApplication {

   private final Connection connection;

   private CriteriaApplication(Connection connection) {
      this.connection = connection;
    }

    public PreparedStatement applyTo(String query) throws SQLException {
      String finalQuery = query + filter;
      PreparedStatement statement = connection.prepareStatement(finalQuery);
      for (int i = 0; i < params.size(); i++) {
        Object paramValue = params.get(i);
        if (paramValue instanceof String) {
          statement.setString(i + 1, (String) paramValue);
        } else if (paramValue instanceof Integer) {
          statement.setInt(i + 1, (int) paramValue);
        }
      }
      return statement;
    }
 }
}
  