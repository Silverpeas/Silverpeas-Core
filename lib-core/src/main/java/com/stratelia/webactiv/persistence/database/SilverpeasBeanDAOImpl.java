/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.persistence.database;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.JdbcData;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanIntf;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.beans.BeanInfo;
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
import java.util.List;

public class SilverpeasBeanDAOImpl<T extends SilverpeasBeanIntf> implements SilverpeasBeanDAO<T> {

  private final PropertyDescriptor[] properties;
  private Class<T> silverpeasBeanClass;
  // how to make connection with the database
  private int m_ConnectionType = SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  private String datasourceName = null;
  private JdbcData m_JdbcData = null;
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

      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace.info("persistence",
            "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
            "root.MSG_GEN_PARAM_VALUE", "new(" + beanClassName + "), property Name = "
            + properties[i].getName() + ", type = " + type);
        if (!isTypeValid(type)) {
          SilverTrace.warn("persistence",
              "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
              "persistence.MSG_WARN_PROPERTIE_NOT_MANAGED", "");
        }
      }
      m_ConnectionType = object._getConnectionType();
      switch (m_ConnectionType) {
        case SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE: {
          datasourceName = object._getDatasourceName();
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_JDBC_CLASSIC: {
          m_JdbcData = object._getJdbcData();
          break;
        }
      }
      tableName = object._getTableName();
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, e);
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
      SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement + ", id= " + pk.getId());
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException("SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  @Override
  public void removeWhere(WAPrimaryKey pk, String whereClause) throws PersistenceException {
    removeWhere(null, pk, whereClause);
  }

  @Override
  public void removeWhere(Connection connection, WAPrimaryKey pk, String whereClause) throws
      PersistenceException {
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
      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement + ", id= " + pk.getId()
          + ", whereClause= " + whereClause);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
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
      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
            "root.MSG_GEN_PARAM_VALUE", "property Name = " + properties[i].getName()
            + ", type = " + type);

        if (isTypeValid(type) == true) {
          if (statement == null) {
            statement = properties[i].getName() + " = ? ";
          } else {
            statement += ", " + properties[i].getName() + " = ? ";
          }
        }
      }

      String updateStatement = "update " + getTableName(bean.getPK()) + " set " + statement + " where id = ?";

      prepStmt = con.prepareStatement(updateStatement);
      SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement
          + ", id= " + bean.getPK().getId());

      int count = prepareStatementSetProperties(prepStmt, bean);

      // for the where clause
      prepStmt.setInt(count, Integer.parseInt(bean.getPK().getId()));
      prepStmt.executeUpdate();

    } catch (Exception e) {
      throw new PersistenceException("SilverpeasBeanDAOImpl.update(SilverpeasBean bean) ",
          SilverpeasException.ERROR, "persistence.EX_CANT_UPDATE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException("SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
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
      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
            "root.MSG_GEN_PARAM_VALUE", "property Name = " + properties[i].getName()
            + ", type = " + type);
        if (isTypeValid(type)) {
          if (columns == null) {
            columns = properties[i].getName();
            statement = " ? ";
          } else {
            columns += ", " + properties[i].getName();
            statement += ", ? ";
          }
        }
      }
      columns += ", id";
      statement += ", ? ";
      String insertStatement = "insert into " + getTableName(bean.getPK()) + " (" + columns
          + ") " + " values (" + statement + ")";
      prepStmt = con.prepareStatement(insertStatement);
      SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + insertStatement + ", id= "
          + bean.getPK().getId());

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
      try {
        DBUtil.close(prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException("SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
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
      String selectStatement = "select  " + getColumnNames() + " from " + getTableName(pk) + " where id = ?";

      SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + selectStatement + ", id= " + pk.getId());
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
      try {
        DBUtil.close(rs, prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException("SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  @Override
  public Collection<T> findByWhereClause(WAPrimaryKey pk, String whereClause) throws
      PersistenceException {
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
      String selectStatement = "select distinct " + getColumnNames() + " from " + getTableName(pk);
      if (whereClause != null) {
        selectStatement += " where " + whereClause;
      }

      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.findByWhereClause(WAPrimaryKey pk, String whereClause)",
          "root.MSG_GEN_PARAM_VALUE",
          "queryStr = " + selectStatement + ", id= " + pk.getId() + ", whereClause= " + whereClause);
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
      try {
        DBUtil.close(rs, prepStmt);
        if (connection == null) {
          closeConnection(con);
        }
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.findByWhereClause(WAPrimaryKey pk, String whereClause)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }

  }

  /*--------------------------------------------------------------------------------------------------------------------*/
  /**
   * getConnection
   */
  private Connection getConnection() throws PersistenceException {
    try {
      Connection con = null;
      switch (m_ConnectionType) {
        case SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE: {
          con = DBUtil.makeConnection(datasourceName);
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_JDBC_CLASSIC: {
          Class.forName(m_JdbcData.JDBCdriverName);
          con = DriverManager.getConnection(m_JdbcData.JDBCurl, m_JdbcData.JDBClogin,
              m_JdbcData.JDBCpassword);
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS: {
          con = DBUtil.makeConnection(JNDINames.PERSISTENCE_DB_DATASOURCE);
          break;
        }
        default /* CONNECTION_TYPE_EJBDATASOURCE_SILVERPEAS */: {
          con = DBUtil.makeConnection(JNDINames.PERSISTENCE_EJB_DATASOURCE);
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
   * closeConnection
   */
  private void closeConnection(Connection dbConnect) {

    try {
      if (dbConnect != null) {
        dbConnect.close();
      }
    } catch (SQLException se) {
      SilverTrace.error("persistence",
          "SilverpeasBeanDAOImpl.closeConnection(Connection dbConnect)",
          "root.EX_CONNECTION_CLOSE_FAILED", "", se);
    }
  }

  /**
   * getColumnNames
   */
  private String getColumnNames() throws Exception {

    String statement = null;

    for (int i = 0; i < properties.length; i++) {
      String type = properties[i].getPropertyType().getName();
      SilverTrace.info("persistence", "SilverpeasBeanDAOImpl.getColumnNames()",
          "root.MSG_GEN_PARAM_VALUE", "property Name = "
          + properties[i].getName() + ", type = " + type);

      if (isTypeValid(type) == true) {
        if (statement == null) {
          statement = properties[i].getName();
        } else {
          statement += ", " + properties[i].getName();
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

    T bean = (T) silverpeasBeanClass.newInstance();
    int count = 1;

    for (int i = 0; i < properties.length; i++) {
      String type = properties[i].getPropertyType().getName();
      if ((type.equals("int")) || (type.equals("java.lang.Integer"))) {
        int value = rs.getInt(count);
        if (!rs.wasNull()) {
          Integer[] parameters = new Integer[1];
          parameters[0] = new Integer(value);
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if ((type.equals("long")) || (type.equals("java.lang.Long"))) {
        long value = rs.getLong(count);
        if (!rs.wasNull()) {
          Long[] parameters = new Long[1];
          parameters[0] = new Long(value);
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if ((type.equals("boolean")) || (type.equals("java.lang.Boolean"))) {
        boolean value = rs.getBoolean(count);
        if (!rs.wasNull()) {
          Boolean[] parameters = new Boolean[1];
          parameters[0] = new Boolean(value);
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (type.equals("java.lang.String")) {
        String value = rs.getString(count);
        if (value != null) {
          String[] parameters = new String[1];
          parameters[0] = value;
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if (type.equals("java.util.Date")) {
        String value = rs.getString(count);
        if (value != null) {
          java.util.Date[] parameters = new java.util.Date[1];
          try {
            parameters[0] = DateUtil.parse(value);
          } catch (Exception e) {
            SilverTrace.error(
                "persistence",
                "SilverpeasBeanDAOImpl.getSilverpeasBeanFromResultSet(WAPrimaryKey pk, ResultSet rs)",
                "root.EX_CANT_PARSE_DATE", "property Name = "
                + properties[i].getName() + ", date= " + value);
            throw e;
          }
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if ((type.equals("float")) || (type.equals("java.lang.Float"))) {
        float value = rs.getFloat(count);
        if (!rs.wasNull()) {
          Float[] parameters = new Float[1];
          parameters[0] = new Float(value);
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      } else if ((type.equals("double")) || (type.equals("java.lang.Double"))) {
        double value = rs.getDouble(count);
        if (!rs.wasNull()) {
          Double[] parameters = new Double[1];
          parameters[0] = new Double(value);
          properties[i].getWriteMethod().invoke(bean, parameters);
        }
        count++;
      }

    }

    Class pkClass = pk.getClass();
    String id = rs.getInt(count) + "";
    Class types[] = new Class[2];
    types[0] = id.getClass();
    types[1] = Class.forName("com.stratelia.webactiv.util.WAPrimaryKey"); // pkClass;
    Constructor construct = pkClass.getConstructor(types);
    Object[] parameters = new Object[2];
    parameters[0] = id;
    parameters[1] = pk;
    WAPrimaryKey maPk = (WAPrimaryKey) construct.newInstance(parameters);

    bean.setPK(maPk);
    return bean;
  }

  /**
   * getTableName
   */
  private String getTableName(WAPrimaryKey pk) {

    String result = "";

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
    return "int".equals(javaTypeName) || "java.lang.Integer".equals(javaTypeName)
        || "long".equals(javaTypeName) || "java.lang.Long".equals(javaTypeName)
        || "java.lang.String".equals(javaTypeName) || "java.util.Date".equals(javaTypeName)
        || "float".equals(javaTypeName) || "java.lang.Float".equals(javaTypeName)
        || "double".equals(javaTypeName) || "java.lang.Double".equals(javaTypeName)
        || "boolean".equals(javaTypeName) || "java.lang.Boolean".equals(javaTypeName);
  }

  /**
   * prepareStatementSetProperties
   */
  private int prepareStatementSetProperties(PreparedStatement prepStmt, T bean) throws
      IllegalAccessException, SQLException, InvocationTargetException {
    int count = 1;

    for (int i = 0; i < properties.length; i++) {
      String type = properties[i].getPropertyType().getName();
      if (("int".equals(type)) || ("java.lang.Integer".equals(type))) {
        Integer integer = (Integer) properties[i].getReadMethod().invoke(bean, null);
        if (integer == null) {
          prepStmt.setInt(count, -1);
        } else {
          prepStmt.setInt(count, integer.intValue());
        }
        count++;
      } else if (("long".equals(type)) || ("java.lang.Long".equals(type))) {
        Long l = (Long) properties[i].getReadMethod().invoke(bean, null);
        if (l == null) {
          prepStmt.setLong(count, 0);
        } else {
          prepStmt.setLong(count, l.longValue());
        }
        count++;
      } else if (("boolean".equals(type)) || ("java.lang.Boolean".equals(type))) {
        Boolean l = (Boolean) properties[i].getReadMethod().invoke(bean, null);
        if (l == null) {
          prepStmt.setBoolean(count, false);
        } else {
          prepStmt.setBoolean(count, l.booleanValue());
        }
        count++;
      } else if ("java.lang.String".equals(type)) {
        String string = (String) properties[i].getReadMethod().invoke(bean,
            null);
        if (string == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, string);
        }
        count++;
      } else if ("java.util.Date".equals(type)) {
        java.util.Date date = (java.util.Date) properties[i].getReadMethod().invoke(bean, null);
        if (date == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, DateUtil.date2SQLDate(date));
        }
        count++;
      } else if (("float".equals(type)) || ("java.lang.Float".equals(type))) {
        Float f = (Float) properties[i].getReadMethod().invoke(bean, null);
        if (f == null) {
          prepStmt.setFloat(count, 0);
        } else {
          prepStmt.setFloat(count, f.floatValue());
        }
        count++;
      } else if (("double".equals(type)) || ("java.lang.Double".equals(type))) {
        Double d = (Double) properties[i].getReadMethod().invoke(bean, null);
        if (d == null) {
          prepStmt.setDouble(count, 0);
        } else {
          prepStmt.setDouble(count, d.doubleValue());
        }
        count++;
      } else {
        SilverTrace.debug("persistence",
            "SilverpeasBeanDAO.prepareStatementSetProperties",
            "persistence.MSG_WARN_PROPERTIE_NOT_MANAGED", type);
      }
    }
    return count;
  }
}