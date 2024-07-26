/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Mutable;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the previous custom persistence layer (before JPA).
 *
 * @param <T> the concrete type of the persistent bean in Silverpeas.
 * @deprecated
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "SqlNoDataSourceInspection"})
@Deprecated
public class SilverpeasBeanDAOImpl<T extends SilverpeasBean> implements SilverpeasBeanDAO<T> {

  private final List<PropertyDescriptor> validProperties;
  private final Class<T> silverpeasBeanClass;
  // how to make connection with the database
  private final String tableName;

  public SilverpeasBeanDAOImpl(Class<T> beanClassName) throws PersistenceException {
    try {
      silverpeasBeanClass = beanClassName;
      T bean = silverpeasBeanClass.getConstructor().newInstance();
      BeanInfo infos = Introspector.getBeanInfo(silverpeasBeanClass);
      validProperties = Arrays.stream(infos.getPropertyDescriptors()).filter(p -> {
        final String type = p.getPropertyType().getName();
        if (!isTypeValid(type)) {
          SilverLogger.getLogger(this).debug(
              "as {0} is not a valid type, {1} is not taken into account for {2}", type,
                  p.getName(), silverpeasBeanClass.getSimpleName());
          return false;
        }
        if (p.getWriteMethod() == null) {
          // Some properties must be skipped (those without a setter)...
          SilverLogger.getLogger(this).debug(
              "as {0} is a property without dedicated setter, it isn't taken into account for {1}",
              p.getName(), silverpeasBeanClass.getSimpleName());
          return false;
        }
        return true;
      }).collect(Collectors.toList());
      tableName = bean.getTableName();
      Objects.requireNonNull(tableName);
    } catch (NoSuchMethodException | InvocationTargetException | IntrospectionException |
             IllegalAccessException | InstantiationException ex) {
      throw new PersistenceException("Cannot initialize bean " + beanClassName, ex);
    }
  }

  @Override
  public void remove(WAPrimaryKey pk) throws PersistenceException {
    remove(null, pk);
  }

  @Override
  public void remove(Connection connection, WAPrimaryKey pk) throws PersistenceException {
    final String updateStatement = "delete from " + tableName + " where id = ?";
    perform(connection, con -> {
      try (PreparedStatement prepStmt = con.prepareStatement(updateStatement)) {
        prepStmt.setInt(1, Integer.parseInt(pk.getId()));
        prepStmt.executeUpdate();
      } catch (SQLException e) {
        throw new PersistenceException(
            "Cannot remove bean with id (" + pk.getId() + ", " + pk.getInstanceId() + ")", e);
      }
    });
  }

  @Override
  public void removeBy(BeanCriteria criteria) throws PersistenceException {
    removeBy(null, criteria);
  }

  @Override
  public void removeBy(Connection connection, BeanCriteria criteria)
      throws PersistenceException {
    String query = "delete from " + tableName;
    perform(connection, con -> {
      try (PreparedStatement prepStmt =
               criteria.withConnection(con).applyTo(query)) {
        prepStmt.executeUpdate();
      } catch (Exception e) {
        throw new PersistenceException("Cannot remove bean by criteria", e);
      }
    });
  }

  @Override
  public void update(T bean) throws PersistenceException {
    update(null, bean);
  }

  @Override
  public void update(Connection connection, T bean) throws PersistenceException {
    StringBuilder statement = new StringBuilder();
    for (PropertyDescriptor property : validProperties) {
      if (statement.length() > 0) {
        statement.append(", ");
      }
      statement.append(property.getName()).append(" = ?");
    }
    String updateStatement = "UPDATE " + tableName + " SET " + statement + " WHERE id = ?";
    perform(connection, con -> {
      try (PreparedStatement prepStmt = con.prepareStatement(updateStatement)) {
        int count = prepareStatementSetProperties(prepStmt, bean);
        // for the where clause
        prepStmt.setInt(count, Integer.parseInt(bean.getPK().getId()));
        prepStmt.executeUpdate();

      } catch (Exception e) {
        throw new PersistenceException(
            "Cannot update bean with id (" + bean.getPK().getId() + ", " +
                bean.getPK().getInstanceId() + ")", e);
      }
    });
  }

  @Override
  public WAPrimaryKey add(T bean) throws PersistenceException {
    return add(null, bean);
  }

  @Override
  public WAPrimaryKey add(Connection connection, T bean) throws PersistenceException {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();
    for (PropertyDescriptor property : validProperties) {
      if (columns.length() > 0) {
        columns.append(", ");
        values.append(", ");
      }
      columns.append(property.getName());
      values.append("?");
    }
    columns.append(", id");
    values.append(", ? ");
    String insertStatement =
        "INSERT INTO " + tableName + " (" + columns + ") " + " values (" + values + ")";
    final Mutable<WAPrimaryKey> pk = Mutable.empty();
    perform(connection, con -> {
      try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
        int count = prepareStatementSetProperties(prepStmt, bean);
        // for the where clause
        int id = DBUtil.getNextId(getTableName(bean.getPK()), "id");
        prepStmt.setInt(count, id);
        prepStmt.executeUpdate();
        bean.getPK().setId(id + "");
        pk.set(bean.getPK());
      } catch (Exception e) {
        throw new PersistenceException("Cannot save bean", e);
      }
    });
    return pk.get();
  }

  @Override
  public T findByPrimaryKey(WAPrimaryKey pk) throws PersistenceException {
    return findByPrimaryKey(null, pk);
  }

  @Override
  public T findByPrimaryKey(Connection connection, WAPrimaryKey pk) throws PersistenceException {
    String selectStatement =
        "SELECT  " + getColumnNames() + " FROM " + tableName + " WHERE id = ?";
    Mutable<T> entity = Mutable.empty();
    perform(connection, con -> {
      try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
        prepStmt.setInt(1, Integer.parseInt(pk.getId()));
        try (ResultSet rs = prepStmt.executeQuery()) {
          if (rs.next()) {
            T result = getSilverpeasBeanFromResultSet(rs);
            entity.set(result);
          }
        }
      } catch (Exception e) {
        throw new PersistenceException(
            "Cannot find bean with id (" + pk.getId() + ", " + pk.getInstanceId() + ")", e);
      }
    });
    return entity.orElse(null);
  }

  @Override
  public Collection<T> findBy(BeanCriteria criteria)
      throws PersistenceException {
    return findBy(null, criteria);
  }

  @Override
  public Collection<T> findBy(Connection connection, BeanCriteria criteria)
      throws PersistenceException {
    Mutable<List<T>> entities = Mutable.of(new ArrayList<>());
    perform(connection, con -> {
      String selectStatement = "SELECT DISTINCT " + getColumnNames() + " FROM " + tableName;
      try (PreparedStatement prepStmt = criteria.withConnection(con).applyTo(selectStatement);
           ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          T bean = getSilverpeasBeanFromResultSet(rs);
          entities.get().add(bean);
        }
      } catch (Exception e) {
        throw new PersistenceException("Cannot find any beans satisfying the criteria " +
            criteria.toString(), e);
      }
    });
    return entities.get();
  }

  /*--------------------------------------------------------------------------------------------------------------------*/

  @FunctionalInterface
  private interface DataSourceTask {
    void execute(final Connection connection) throws PersistenceException;
  }

  private void perform(final Connection connection, final DataSourceTask task)
      throws PersistenceException {
    Connection con = getConnection(connection);
    try {
      task.execute(con);
    } finally {
      if (connection == null) {
        DBUtil.close(con);
      }
    }
  }

  private Connection getConnection(final Connection connection) throws PersistenceException {
    if (connection != null) {
      return connection;
    }
    try {
      return DBUtil.openConnection();
    } catch (SQLException e) {
      throw new PersistenceException("Datasource connection opening failure!", e);
    }
  }

  /**
   * getColumnNames
   */
  private String getColumnNames() {
    StringBuilder statement = new StringBuilder();
    for (PropertyDescriptor property : validProperties) {
      if (statement.length() > 0) {
        statement.append(", ");
      }
      statement.append(property.getName());
    }
    statement.append(", id");
    return statement.toString();
  }

  private T getSilverpeasBeanFromResultSet(ResultSet rs)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      InstantiationException, SQLException, ParseException {

    Constructor<T> constructor = silverpeasBeanClass.getDeclaredConstructor();
    T bean = constructor.newInstance();
    int count = 1;
    String instanceId = null;
    for (PropertyDescriptor property : validProperties) {
      String type = property.getPropertyType().getName();
      Object[] parameters;
      if (isInteger(type)) {
        parameters = getIntParams(rs, count);
        count++;
      } else if (isLong(type)) {
        parameters = getLongParams(rs, count);
        count++;
      } else if (isBoolean(type)) {
        parameters = getBooleanParams(rs, count);
        count++;
      } else if (isString(type)) {
        parameters = getStringParams(rs, count);
        count++;
      } else if (isDate(type)) {
        parameters = getDateParams(rs, count);
        count++;
      } else if (isFloat(type)) {
        parameters = getFloatParams(rs, count);
        count++;
      } else if (isDouble(type)) {
        parameters = getDoubleParams(rs, count);
        count++;
      } else {
        parameters = null;
      }
      if (parameters != null) {
        property.getWriteMethod().invoke(bean, parameters);
      }
      if (parameters != null && property.getName().equals("instanceId")) {
        instanceId = (String) parameters[0];
      }
    }

    setBeanPK(instanceId, rs, bean, count);
    return bean;
  }

  private Object[] getDoubleParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    double value = rs.getDouble(idx);
    if (!rs.wasNull()) {
      parameters = new Double[]{value};
    }
    return parameters;
  }

  private Object[] getFloatParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    float value = rs.getFloat(idx);
    if (!rs.wasNull()) {
      parameters = new Float[]{value};
    }
    return parameters;
  }

  private Object[] getDateParams(final ResultSet rs, final int idx)
      throws SQLException, ParseException {
    Object[] parameters = null;
    String value = rs.getString(idx);
    if (value != null) {
      parameters = new Date[]{DateUtil.parse(value)};
    }
    return parameters;
  }

  private Object[] getStringParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    String value = rs.getString(idx);
    if (value != null) {
      parameters = new String[]{value};
    }
    return parameters;
  }

  private Object[] getBooleanParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    boolean value = rs.getBoolean(idx);
    if (!rs.wasNull()) {
      parameters = new Boolean[]{value};
    }
    return parameters;
  }

  private Object[] getLongParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    long value = rs.getLong(idx);
    if (!rs.wasNull()) {
      parameters = new Long[]{value};
    }
    return parameters;
  }

  private Object[] getIntParams(final ResultSet rs, final int idx)
      throws SQLException {
    Object[] parameters = null;
    int value = rs.getInt(idx);
    if (!rs.wasNull()) {
      parameters = new Integer[]{value};
    }
    return parameters;
  }

  private void setBeanPK(final String instanceId, final ResultSet rs, final T bean, final int count)
      throws SQLException {
    String id = rs.getInt(count) + "";
    IdPK pk = new IdPK(id);
    pk.setComponentName(instanceId);
    bean.setPK(pk);
  }

  private boolean isInteger(String type) {
    return "int".equals(type) || "java.lang.Integer".equals(type);
  }

  private boolean isLong(String type) {
    return "long".equals(type) || "java.lang.Long".equals(type);
  }

  private boolean isBoolean(String type) {
    return "boolean".equals(type) || "java.lang.Boolean".equals(type);
  }

  private boolean isString(String type) {
    return "java.lang.String".equals(type);
  }

  private boolean isDate(String type) {
    return "java.util.Date".equals(type);
  }

  private boolean isDouble(String type) {
    return "double".equals(type) || "java.lang.Double".equals(type);
  }

  private boolean isFloat(String type) {
    return "float".equals(type) || "java.lang.Float".equals(type);
  }

  private String getTableName(WAPrimaryKey pk) {
    String result;
    if (tableName != null) {
      result = tableName;
    } else {
      result = pk.getTableName();
    }
    return result;
  }

  private boolean isTypeValid(String javaTypeName) {
    return isInteger(javaTypeName) || isLong(javaTypeName) || isString(javaTypeName) ||
        isDate(javaTypeName) || isFloat(javaTypeName) || isDouble(javaTypeName) ||
        isBoolean(javaTypeName);
  }

  private int prepareStatementSetProperties(PreparedStatement prepStmt, T bean)
      throws IllegalAccessException, SQLException, InvocationTargetException {
    int count = 1;
    for (PropertyDescriptor property : validProperties) {
      String type = property.getPropertyType().getName();
      if (isInteger(type)) {
        setInt(prepStmt, bean, count, property);
        count++;
      } else if (isLong(type)) {
        Long l = (Long) property.getReadMethod().invoke(bean);
        setLong(prepStmt, count, l);
        count++;
      } else if (isBoolean(type)) {
        Boolean l = (Boolean) property.getReadMethod().invoke(bean);
        setBoolean(prepStmt, count, l);
        count++;
      } else if (isString(type)) {
        setString(prepStmt, bean, count, property);
        count++;
      } else if (isDate(type)) {
        setDate(prepStmt, bean, count, property);
        count++;
      } else if (isFloat(type)) {
        setFloat(prepStmt, bean, count, property);
        count++;
      } else if (isDouble(type)) {
        setDouble(prepStmt, bean, count, property);
        count++;
      }
    }
    return count;
  }

  private void setDouble(final PreparedStatement prepStmt, final T bean, final int count,
      final PropertyDescriptor property)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    Double d = (Double) property.getReadMethod().invoke(bean);
    prepStmt.setDouble(count, Objects.requireNonNullElse(d, 0d));
  }

  private void setFloat(final PreparedStatement prepStmt, final T bean, final int count,
      final PropertyDescriptor property)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    Float f = (Float) property.getReadMethod().invoke(bean);
    prepStmt.setFloat(count, Objects.requireNonNullElse(f, 0f));
  }

  private void setDate(final PreparedStatement prepStmt, final T bean, final int count,
      final PropertyDescriptor property)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    Date date = (Date) property.getReadMethod().invoke(bean);
    if (date == null) {
      prepStmt.setNull(count, Types.VARCHAR);
    } else {
      prepStmt.setString(count, DateUtil.date2SQLDate(date));
    }
  }

  private void setString(final PreparedStatement prepStmt, final T bean, final int count,
      final PropertyDescriptor property)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    String string = (String) property.getReadMethod().invoke(bean);
    if (string == null) {
      prepStmt.setNull(count, Types.VARCHAR);
    } else {
      prepStmt.setString(count, string);
    }
  }

  private void setBoolean(final PreparedStatement prepStmt, final int count, final Boolean l)
      throws SQLException {
    prepStmt.setBoolean(count, Objects.requireNonNullElse(l, false));
  }

  private void setLong(final PreparedStatement prepStmt, final int count, final Long l)
      throws SQLException {
    prepStmt.setLong(count, Objects.requireNonNullElse(l, 0L));
  }

  private void setInt(final PreparedStatement prepStmt, final T bean, final int count,
      final PropertyDescriptor property)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    Integer integer = (Integer) property.getReadMethod().invoke(bean);
    prepStmt.setInt(count, Objects.requireNonNullElse(integer, -1));
  }
}