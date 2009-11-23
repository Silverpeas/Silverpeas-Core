/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.sql.*;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.exception.*;
import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.webactiv.persistence.*;

public class SilverpeasBeanDAOImpl implements SilverpeasBeanDAO {
  private PropertyDescriptor[] properties = null;
  private Class silverpeasBeanClass = null;

  // how to make connection with the database
  private int m_ConnectionType = SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  private String m_DatasourceName = null;
  private JdbcData m_JdbcData = null;
  private String m_TableName = null;

  /**
   * SilverpeasBeanDAOImpl
   */
  public SilverpeasBeanDAOImpl(String beanClassName)
      throws PersistenceException {
    try {
      silverpeasBeanClass = Class.forName(beanClassName);

      Object object = silverpeasBeanClass.newInstance();

      if (!(object instanceof SilverpeasBean))
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
            SilverpeasException.ERROR, "persistence.EX_ISNOT_SILVERPEASBEAN",
            "classe= " + beanClassName, null);

      BeanInfo infos = Introspector.getBeanInfo(silverpeasBeanClass);
      properties = infos.getPropertyDescriptors();

      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace
            .info(
            "persistence",
            "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
            "root.MSG_GEN_PARAM_VALUE", "new(" + beanClassName
            + "), property Name = " + properties[i].getName()
            + ", type = " + type);
        if (isTypeValid(type) == false) {
          SilverTrace
              .warn(
              "persistence",
              "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
              "persistence.MSG_WARN_PROPERTIE_NOT_MANAGED", "");
        }
      }

      SilverpeasBeanIntf sb = (SilverpeasBean) object;

      m_ConnectionType = sb._getConnectionType();
      switch (m_ConnectionType) {
        case SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE: {
          m_DatasourceName = sb._getDatasourceName();
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_JDBC_CLASSIC: {
          m_JdbcData = sb._getJdbcData();
          break;
        }
      }

      m_TableName = sb._getTableName();

    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.SilverpeasBeanDAOImpl( String beanClassName )",
          SilverpeasException.ERROR, "persistence.EX_CANT_INITIALISE_CLASS",
          "classe= " + beanClassName, e);
    }
  }

  public void remove(WAPrimaryKey pk) throws PersistenceException {
    remove(null, pk);
  }

  /**
   * remove
   */
  public void remove(Connection connection, WAPrimaryKey pk)
      throws PersistenceException {
    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;

    PreparedStatement prepStmt = null;

    try {
      String updateStatement = "delete from " + getTableName(pk)
          + " where id = ?";

      prepStmt = con.prepareStatement(updateStatement);
      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement
          + ", id= " + pk.getId());

      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null)
          closeConnection(con);
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.remove(WAPrimaryKey pk)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  public void removeWhere(WAPrimaryKey pk, String p_WhereClause)
      throws PersistenceException {
    removeWhere(null, pk, p_WhereClause);
  }

  /**
   * removeWhere
   */
  public void removeWhere(Connection connection, WAPrimaryKey pk,
      String p_WhereClause) throws PersistenceException {

    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;

    PreparedStatement prepStmt = null;

    try {
      String updateStatement = "delete from " + getTableName(pk) + " where "
          + p_WhereClause;

      prepStmt = con.prepareStatement(updateStatement);
      SilverTrace
          .info(
          "persistence",
          "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement
          + ", id= " + pk.getId() + ", whereClause= " + p_WhereClause);

      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
          SilverpeasException.ERROR, "persistence.EX_CANT_REMOVE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null)
          closeConnection(con);
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.removeWhere( WAPrimaryKey pk, String p_WhereClause )",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  public void update(SilverpeasBeanIntf bean) throws PersistenceException {
    update(null, bean);
  }

  /**
   * update
   */
  public void update(Connection connection, SilverpeasBeanIntf bean)
      throws PersistenceException {
    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;

    PreparedStatement prepStmt = null;

    try {
      String statement = null;

      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace.info("persistence",
            "SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
            "root.MSG_GEN_PARAM_VALUE", "property Name = "
            + properties[i].getName() + ", type = " + type);

        if (isTypeValid(type) == true) {
          if (statement == null)
            statement = properties[i].getName() + " = ? ";
          else
            statement += ", " + properties[i].getName() + " = ? ";
        }
      }

      String updateStatement = "update " + getTableName(bean.getPK()) + " set "
          + statement + " where id = ?";

      prepStmt = con.prepareStatement(updateStatement);
      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + updateStatement
          + ", id= " + bean.getPK().getId());

      int count = prepareStatementSetProperties(prepStmt, bean);

      // for the where clause
      prepStmt.setInt(count, Integer.parseInt(bean.getPK().getId()));
      prepStmt.executeUpdate();

    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.update(SilverpeasBean bean) ",
          SilverpeasException.ERROR, "persistence.EX_CANT_UPDATE_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null)
          closeConnection(con);
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.update(SilverpeasBean bean)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }

  }

  public WAPrimaryKey add(SilverpeasBeanIntf bean) throws PersistenceException {
    return add(null, bean);
  }

  /**
   * add
   */
  public WAPrimaryKey add(Connection connection, SilverpeasBeanIntf bean)
      throws PersistenceException {

    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;
    PreparedStatement prepStmt = null;

    try {
      String columns = null;
      String statement = null;

      for (int i = 0; i < properties.length; i++) {
        String type = properties[i].getPropertyType().getName();
        SilverTrace.info("persistence",
            "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
            "root.MSG_GEN_PARAM_VALUE", "property Name = "
            + properties[i].getName() + ", type = " + type);

        if (isTypeValid(type) == true) {
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

      String insertStatement = "insert into " + getTableName(bean.getPK())
          + " (" + columns + ") " + " values (" + statement + ")";

      prepStmt = con.prepareStatement(insertStatement);
      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + insertStatement
          + ", id= " + bean.getPK().getId());

      int count = prepareStatementSetProperties(prepStmt, bean);

      // for the where clause
      int id = DBUtil.getNextId(getTableName(bean.getPK()), "id");
      prepStmt.setInt(count, id);
      prepStmt.executeUpdate();

      bean.getPK().setId(id + "");
      return bean.getPK();

    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
          SilverpeasException.ERROR, "persistence.EX_CANT_ADD_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(prepStmt);
        if (connection == null)
          closeConnection(con);
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.add(SilverpeasBean bean)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  public SilverpeasBeanIntf findByPrimaryKey(WAPrimaryKey pk)
      throws PersistenceException {
    return findByPrimaryKey(null, pk);
  }

  /**
   * findByPrimaryKey
   */
  public SilverpeasBeanIntf findByPrimaryKey(Connection connection,
      WAPrimaryKey pk) throws PersistenceException {

    PreparedStatement prepStmt = null;
    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;
    ResultSet rs = null;

    try {
      String selectStatement = "select  " + getColumnNames() + " from "
          + getTableName(pk) + " where id = ?";

      SilverTrace.info("persistence",
          "SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + selectStatement
          + ", id= " + pk.getId());
      prepStmt = con.prepareStatement(selectStatement);

      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prepStmt.executeQuery();

      if (rs.next()) {
        SilverpeasBeanIntf bean = getSilverpeasBeanFromResultSet(pk, rs);
        return bean;
      } else
        return null;
    } catch (Exception e) {
      throw new PersistenceException(
          "SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
          SilverpeasException.ERROR, "persistence.EX_CANT_FIND_OBJECT", "", e);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (connection == null)
          closeConnection(con);
      } catch (Exception e) {
        throw new PersistenceException(
            "SilverpeasBeanDAOImpl.findByPrimaryKey(WAPrimaryKey pk)",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

  public Collection findByWhereClause(WAPrimaryKey pk, String whereClause)
      throws PersistenceException {
    return findByWhereClause(null, pk, whereClause);
  }

  /**
   * findByWhereClause
   */
  public Collection findByWhereClause(Connection connection, WAPrimaryKey pk,
      String whereClause) throws PersistenceException {

    PreparedStatement prepStmt = null;
    Connection con = null;
    if (connection == null)
      con = getConnection();
    else
      con = connection;

    ResultSet rs = null;

    try {

      String selectStatement = "select distinct " + getColumnNames() + " from "
          + getTableName(pk);

      if (whereClause != null)
        selectStatement += " where " + whereClause;

      SilverTrace
          .info(
          "persistence",
          "SilverpeasBeanDAOImpl.findByWhereClause(WAPrimaryKey pk, String whereClause)",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + selectStatement
          + ", id= " + pk.getId() + ", whereClause= " + whereClause);
      prepStmt = con.prepareStatement(selectStatement);

      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        SilverpeasBeanIntf bean = getSilverpeasBeanFromResultSet(pk, rs);
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
        if (connection == null)
          closeConnection(con);
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
          con = DBUtil.makeConnection(m_DatasourceName);
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_JDBC_CLASSIC: {
          Class.forName(m_JdbcData.JDBCdriverName);
          con = DriverManager.getConnection(m_JdbcData.JDBCurl,
              m_JdbcData.JDBClogin, m_JdbcData.JDBCpassword);
          break;
        }
        case SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS: {
          con = DBUtil.makeConnection(JNDINames.PERSISTENCE_DB_DATASOURCE);
          break;
        }
        default /* CONNECTION_TYPE_EJBDATASOURCE_SILVERPEAS */
        : {
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
      if (dbConnect != null)
        dbConnect.close();
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
        if (statement == null)
          statement = properties[i].getName();
        else
          statement += ", " + properties[i].getName();
      }
    }
    statement += ", id";
    return statement;
  }

  /**
   * getSilverpeasBeanFromResultSet
   */
  private SilverpeasBeanIntf getSilverpeasBeanFromResultSet(WAPrimaryKey pk,
      ResultSet rs) throws Exception {

    SilverpeasBeanIntf bean = (SilverpeasBeanIntf) silverpeasBeanClass
        .newInstance();
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
            SilverTrace
                .error(
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

    if (m_TableName != null) {
      result = m_TableName;
    } else {
      result = pk.getTableName();
    }

    return result;
  }

  /**
   * isTypeValid
   */
  private boolean isTypeValid(String p_JavaTypeName) {

    boolean result = false;

    if ((p_JavaTypeName.equals("int"))
        || (p_JavaTypeName.equals("java.lang.Integer"))
        || (p_JavaTypeName.equals("long"))
        || (p_JavaTypeName.equals("java.lang.Long"))
        || (p_JavaTypeName.equals("java.lang.String"))
        || (p_JavaTypeName.equals("java.util.Date"))
        || (p_JavaTypeName.equals("float"))
        || (p_JavaTypeName.equals("java.lang.Float"))
        || (p_JavaTypeName.equals("double"))
        || (p_JavaTypeName.equals("java.lang.Double"))
        || (p_JavaTypeName.equals("boolean"))
        || (p_JavaTypeName.equals("java.lang.Boolean"))) {
      result = true;
    }

    return result;
  }

  /**
   * prepareStatementSetProperties
   */
  private int prepareStatementSetProperties(PreparedStatement prepStmt,
      SilverpeasBeanIntf bean) throws IllegalAccessException, SQLException,
      InvocationTargetException {

    int count = 1;
    String type;

    for (int i = 0; i < properties.length; i++) {
      type = properties[i].getPropertyType().getName();
      if ((type.equals("int")) || (type.equals("java.lang.Integer"))) {
        Integer integer = (Integer) properties[i].getReadMethod().invoke(bean,
            null);
        if (integer == null)
          prepStmt.setInt(count, -1);
        else
          prepStmt.setInt(count, integer.intValue());
        count++;
      } else if ((type.equals("long")) || (type.equals("java.lang.Long"))) {
        Long l = (Long) properties[i].getReadMethod().invoke(bean, null);
        if (l == null)
          prepStmt.setLong(count, 0);
        else
          prepStmt.setLong(count, l.longValue());
        count++;
      } else if ((type.equals("boolean")) || (type.equals("java.lang.Boolean"))) {
        Boolean l = (Boolean) properties[i].getReadMethod().invoke(bean, null);
        if (l == null)
          prepStmt.setBoolean(count, false);
        else
          prepStmt.setBoolean(count, l.booleanValue());
        count++;
      } else if (type.equals("java.lang.String")) {
        String string = (String) properties[i].getReadMethod().invoke(bean,
            null);
        if (string == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, string);
        }
        count++;
      } else if (type.equals("java.util.Date")) {
        java.util.Date date = (java.util.Date) properties[i].getReadMethod()
            .invoke(bean, null);
        if (date == null) {
          prepStmt.setNull(count, Types.VARCHAR);
        } else {
          prepStmt.setString(count, DateUtil.date2SQLDate(date));
        }
        count++;
      } else if ((type.equals("float")) || (type.equals("java.lang.Float"))) {
        Float f = (Float) properties[i].getReadMethod().invoke(bean, null);
        if (f == null)
          prepStmt.setFloat(count, 0);
        else
          prepStmt.setFloat(count, f.floatValue());
        count++;
      } else if ((type.equals("double")) || (type.equals("java.lang.Double"))) {
        Double d = (Double) properties[i].getReadMethod().invoke(bean, null);
        if (d == null)
          prepStmt.setDouble(count, 0);
        else
          prepStmt.setDouble(count, d.doubleValue());
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