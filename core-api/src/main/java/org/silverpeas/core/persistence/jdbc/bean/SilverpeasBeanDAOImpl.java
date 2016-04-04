/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.exception.SilverpeasException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Deprecated
public class SilverpeasBeanDAOImpl<T extends SilverpeasBeanIntf> implements SilverpeasBeanDAO<T> {

  private final PropertyDescriptor[] properties;
  private Class<T> silverpeasBeanClass;
  // how to make connection with the database
  private int connectionType = CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  private String datasourceName = null;
  private JdbcData jdbcConnectionParameters = null;
  private String tableName = null;

  @SuppressWarnings("unchecked")
  public SilverpeasBeanDAOImpl(String beanClassName) throws PersistenceException {
    try {
      silverpeasBeanClass = (Class<T>) Class.forName(beanClassName);
      T object = silverpeasBeanClass.newInstance();

      if (!(object instanceof SilverpeasBean)) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
            SilverpeasException.ERROR, "persistence.EX_ISNOT_SILVERPEASBEAN",
            "classe= " + beanClassName, null);
      }
      BeanInfo infos = Introspector.getBeanInfo(silverpeasBeanClass);
      properties = infos.getPropertyDescriptors();

      for (PropertyDescriptor property : properties) {
        String type = property.getPropertyType().getName();
        if (!isTypeValid(type)) {
          SilverTrace.warn("persistence",
              "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
              "persistence.MSG_WARN_PROPERTIE_NOT_MANAGED", "");
        }
      }
      connectionType = object._getConnectionType();
      switch (connectionType) {
        case CONNECTION_TYPE_DATASOURCE: {
          datasourceName = object._getDatasourceName();
          break;
        }
        case CONNECTION_TYPE_JDBC_CLASSIC: {
          jdbcConnectionParameters = object._getJdbcData();
          break;
        }
      }
      tableName = object._getTableName();
    } catch (IntrospectionException ex) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl(String beanClassName)",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, ex);
    } catch (ClassNotFoundException ex) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl(String beanClassName)",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, ex);
    } catch (InstantiationException ex) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl(String beanClassName)",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, ex);
    } catch (IllegalAccessException ex) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl(String beanClassName)",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, ex);
    }
  }

  @Override
  public void remove(WAPrimaryKey pk) throws PersistenceException {
    remove(null, pk);
  }

  @Override
  public void remove(Connection connection, WAPrimaryKey pk) throws PersistenceException {
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    PreparedStatement prepStmt = null;
    try {
      String updateStatement = "delete from " + getTableName(pk) + " where id = ?";
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      DBUtil.close(prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }
  }

  @Override
  public void removeWhere(WAPrimaryKey pk, String whereClause) throws PersistenceException {
    removeWhere(null, pk, whereClause);
  }

  @Override
  public void removeWhere(Connection connection, WAPrimaryKey pk, String whereClause)
      throws PersistenceException {
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    PreparedStatement prepStmt = null;
    try {
      String updateStatement = "delete from " + getTableName(pk) + " where " + whereClause;

      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.removeWhere(WAPrimaryKey pk, String p_WhereClause)",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      DBUtil.close(prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }
  }

  @Override
  public void update(T bean) throws PersistenceException {
    update(null, bean);
  }

  @Override
  public void update(Connection connection, T bean) throws PersistenceException {
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    PreparedStatement prepStmt = null;

    try {
      String statement = null;
      for (PropertyDescriptor property : properties) {
        String type = property.getPropertyType().getName();
        if (isTypeValid(type) == true) {
          if (statement == null) {
            statement = property.getName() + " = ? ";
          } else {
            statement += ", " + property.getName() + " = ? ";
          }
        }
      }

      String updateStatement =
          "UPDATE " + getTableName(bean.getPK()) + " SET " + statement + " WHERE id = ?";

      prepStmt = con.prepareStatement(updateStatement);
      int count = prepareStatementSetProperties(prepStmt, bean);

      // for the where clause
      prepStmt.setInt(count, Integer.parseInt(bean.getPK().getId()));
      prepStmt.executeUpdate();

    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.update(SilverpeasBean bean) ",
          SilverpeasException.ERROR, "persistence.EX_CANT_UPDATE_OBJECT", "", e);
    } finally {
      DBUtil.close(prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }

  }

  @Override
  public WAPrimaryKey add(T bean) throws PersistenceException {
    return add(null, bean);
  }

  @Override
  public WAPrimaryKey add(Connection connection, T bean) throws PersistenceException {
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    PreparedStatement prepStmt = null;
    try {
      String columns = null;
      String statement = null;
      for (PropertyDescriptor property : properties) {
        String type = property.getPropertyType().getName();
        if (isTypeValid(type)) {
          if (columns == null) {
            columns = property.getName();
            statement = " ? ";
          } else {
            columns += ", " + property.getName();
            statement += ", ? ";
          }
        }
      }
      columns += ", id";
      statement += ", ? ";
      String insertStatement =
          "INSERT INTO " + getTableName(bean.getPK()) + " (" + columns + ") " + " values (" +
              statement + ")";
      prepStmt = con.prepareStatement(insertStatement);
      int count = prepareStatementSetProperties(prepStmt, bean);
      // for the where clause
      int id = DBUtil.getNextId(getTableName(bean.getPK()), "id");
      prepStmt.setInt(count, id);
      prepStmt.executeUpdate();
      bean.getPK().setId(id + "");
      return bean.getPK();

    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
          SilverpeasException.ERROR, "persistence.EX_CANT_ADD_OBJECT", "", e);
    } finally {
      DBUtil.close(prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }
  }

  @Override
  public T findByPrimaryKey(WAPrimaryKey pk) throws PersistenceException {
    return findByPrimaryKey(null, pk);
  }

  @Override
  public T findByPrimaryKey(Connection connection, WAPrimaryKey pk) throws PersistenceException {
    PreparedStatement prepStmt = null;
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    ResultSet rs = null;
    try {
      String selectStatement =
          "SELECT  " + getColumnNames() + " FROM " + getTableName(pk) + " WHERE id = ?";

      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        return getSilverpeasBeanFromResultSet(pk, rs);
      }
      return null;
    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
          SilverpeasException.ERROR, "persistence.EX_CANT_FIND_OBJECT", "", e);
    } finally {
      DBUtil.close(rs, prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }
  }

  @Override
  public Collection<T> findByWhereClause(WAPrimaryKey pk, String whereClause)
      throws PersistenceException {
    return findByWhereClause(null, pk, whereClause);
  }

  @Override
  public Collection<T> findByWhereClause(Connection connection, WAPrimaryKey pk, String whereClause)
      throws PersistenceException {
    PreparedStatement prepStmt = null;
    Connection con;
    if (connection == null) {
      con = getConnection();
    } else {
      con = connection;
    }
    ResultSet rs = null;
    try {
      String selectStatement = "SELECT DISTINCT " + getColumnNames() + " FROM " + getTableName(pk);
      if (whereClause != null) {
        selectStatement += " WHERE " + whereClause;
      }

      prepStmt = con.prepareStatement(selectStatement);

      rs = prepStmt.executeQuery();
      List<T> list = new ArrayList<T>();
      while (rs.next()) {
        T bean = getSilverpeasBeanFromResultSet(pk, rs);
        list.add(bean);
      }
      return list;
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.findByWhereClause(WAPrimaryKey pk, String whereClause)",
          SilverpeasException.ERROR, "persistence.EX_CANT_FIND_OBJECT", "", e);
    } finally {
      DBUtil.close(rs, prepStmt);
      if (connection == null) {
        DBUtil.close(con);
      }
    }

  }

  /*--------------------------------------------------------------------------------------------------------------------*/

  /**
   * getConnection
   */
  private Connection getConnection() throws PersistenceException {
    try {
      Connection con;
      switch (connectionType) {
        case CONNECTION_TYPE_DATASOURCE: {
          con = DBUtil.openConnection();
          break;
        }
        case CONNECTION_TYPE_JDBC_CLASSIC: {
          System.out.println("WARNING COMMECTION TYPE JDBC BASIC");
          Class.forName(jdbcConnectionParameters.JDBCdriverName);
          con = DriverManager
              .getConnection(jdbcConnectionParameters.JDBCurl, jdbcConnectionParameters.JDBClogin,
                  jdbcConnectionParameters.JDBCpassword);
          break;
        }
        case CONNECTION_TYPE_DATASOURCE_SILVERPEAS: {
          con = DBUtil.openConnection();
          break;
        }
        default: {
          con = DBUtil.openConnection();
          break;
        }
      }
      return con;
    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "", e);
    }
  }

  /**
   * getColumnNames
   */
  private String getColumnNames() throws Exception {

    String statement = null;

    for (PropertyDescriptor property : properties) {
      String type = property.getPropertyType().getName();
      if (isTypeValid(type) == true) {
        if (statement == null) {
          statement = property.getName();
        } else {
          statement += ", " + property.getName();
        }
      }
    }
    statement += ", id";
    return statement;
  }

  /**
   * getSilverpeasBeanFromResultSet
   */
  private T getSilverpeasBeanFromResultSet(WAPrimaryKey pk, ResultSet rs) throws Exception {

    T bean = silverpeasBeanClass.newInstance();
    int count = 1;

    for (PropertyDescriptor property : properties) {
      String type = property.getPropertyType().getName();
      if (isInteger(type)) {
        int value = rs.getInt(count);
        if (!rs.wasNull()) {
          Object[] parameters = new Integer[]{value};
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (isLong(type)) {
        long value = rs.getLong(count);
        if (!rs.wasNull()) {
          Object[] parameters = new Long[]{value};
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (isBoolean(type)) {
        boolean value = rs.getBoolean(count);
        if (!rs.wasNull()) {
          Object[] parameters = new Boolean[]{value};
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (isString(type)) {
        String value = rs.getString(count);
        if (value != null) {
          Object[] parameters = new String[1];
          parameters[0] = value;
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (isDate(type)) {
        String value = rs.getString(count);
        if (value != null) {
          Object[] parameters = new Date[1];
          try {
            parameters[0] = DateUtil.parse(value);
          } catch (Exception e) {
            SilverTrace.error("persistence",
                "SilverpeasBeanDAOImpl.getSilverpeasBeanFromResultSet(WAPrimaryKey pk, " +
                    "ResultSet rs)", "root.EX_CANT_PARSE_DATE",
                "property Name = " + property.getName() + ", date= " + value);
            throw e;
          }
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (isFloat(type)) {
        float value = rs.getFloat(count);
        if (!rs.wasNull()) {
          Object[] parameters = new Float[]{value};
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if ((type.equals("double")) || (type.equals("java.lang.Double"))) {
        double value = rs.getDouble(count);
        if (!rs.wasNull()) {
          Object[] parameters = new Double[]{value};
          property.getWriteMethod().invoke(bean, parameters);
        }
        count++;
      }

    }

    Class<? extends WAPrimaryKey> pkClass = pk.getClass();
    String id = rs.getInt(count) + "";
    Class<?> types[] = new Class[2];
    types[0] = String.class;
    types[1] = WAPrimaryKey.class; // pkClass;
    Constructor<? extends WAPrimaryKey> construct = pkClass.getConstructor(types);
    Object[] parameters = new Object[2];
    parameters[0] = id;
    parameters[1] = pk;
    WAPrimaryKey maPk = construct.newInstance(parameters);
    bean.setPK(maPk);
    return bean;
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

  /**
   * getTableName
   */
  private String getTableName(WAPrimaryKey pk) {
    String result;
    if (tableName != null) {
      result = tableName;
    } else {
      result = pk.getTableName();
    }
    return result;
  }

  /**
   * isTypeValid
   */
  private boolean isTypeValid(String javaTypeName) {
    return isInteger(javaTypeName) || isLong(javaTypeName) || isString(javaTypeName) ||
        isDate(javaTypeName) || isFloat(javaTypeName) || isDouble(javaTypeName) ||
        isBoolean(javaTypeName);
  }

  /**
   * prepareStatementSetProperties
   */
  private int prepareStatementSetProperties(PreparedStatement prepStmt, T bean)
      throws IllegalAccessException, SQLException, InvocationTargetException {
    int count = 1;

    for (PropertyDescriptor property : properties) {
      String type = property.getPropertyType().getName();
      if (isInteger(type)) {
        Integer integer = (Integer) property.getReadMethod().invoke(bean);
        if (integer == null) {
          prepStmt.setInt(count, -1);
        } else {
          prepStmt.setInt(count, integer);
        }
        count++;
      } else if (isLong(type)) {
        Long l = (Long) property.getReadMethod().invoke(bean);
        if (l == null) {
          prepStmt.setLong(count, 0);
        } else {
          prepStmt.setLong(count, l);
        }
        count++;
      } else if (isBoolean(type)) {
        Boolean l = (Boolean) property.getReadMethod().invoke(bean);
        if (l == null) {
          prepStmt.setBoolean(count, false);
        } else {
          prepStmt.setBoolean(count, l);
        }
        count++;
      } else if (isString(type)) {
        String string = (String) property.getReadMethod().invoke(bean);
        if (string == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, string);
        }
        count++;
      } else if (isDate(type)) {
        Date date = (Date) property.getReadMethod().invoke(bean);
        if (date == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, DateUtil.date2SQLDate(date));
        }
        count++;
      } else if (isFloat(type)) {
        Float f = (Float) property.getReadMethod().invoke(bean);
        if (f == null) {
          prepStmt.setFloat(count, 0);
        } else {
          prepStmt.setFloat(count, f);
        }
        count++;
      } else if (isDouble(type)) {
        Double d = (Double) property.getReadMethod().invoke(bean);
        if (d == null) {
          prepStmt.setDouble(count, 0);
        } else {
          prepStmt.setDouble(count, d);
        }
        count++;
      }
    }
    return count;
  }
}