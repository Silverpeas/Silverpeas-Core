package com.stratelia.webactiv.util.pool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.Properties;

/**
 * Cette classe est un wrapper de connection, qui delegue systematique toute ses
 * méthodes a une autre connection. La connection est passée en parametre dans
 * le constructeur. La connection est alors encapsulée. Cette classe est
 * nécessaire pour l'implementation du pattern du decorateur. Voir aussi
 * PooledConnectionWrapper, qui est un veritable exemple de décoration.
 */
public class ConnectionWrapper implements Connection {
  protected Connection connection;

  public ConnectionWrapper(Connection toDelegate) {
    connection = toDelegate;
  }

  public java.sql.Statement createStatement() throws SQLException {
    return connection.createStatement();
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String string)
      throws SQLException {
    return connection.prepareStatement(string);
  }

  public java.sql.CallableStatement prepareCall(java.lang.String string)
      throws SQLException {
    return connection.prepareCall(string);
  }

  public java.lang.String nativeSQL(java.lang.String string)
      throws SQLException {
    return connection.nativeSQL(string);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    connection.setAutoCommit(autoCommit);
  }

  public boolean getAutoCommit() throws SQLException {
    return connection.getAutoCommit();
  }

  public void commit() throws SQLException {
    connection.commit();
  }

  public void rollback() throws SQLException {
    connection.rollback();
  }

  public void close() throws SQLException {
    connection.close();
  }

  public boolean isClosed() throws SQLException {
    return connection.isClosed();
  }

  public java.sql.DatabaseMetaData getMetaData() throws SQLException {
    return connection.getMetaData();
  }

  public void setReadOnly(boolean readOnly) throws SQLException {
    connection.setReadOnly(readOnly);
  }

  public boolean isReadOnly() throws SQLException {
    return connection.isReadOnly();
  }

  public void setCatalog(java.lang.String catalog) throws SQLException {
    connection.setCatalog(catalog);
  }

  public java.lang.String getCatalog() throws SQLException {
    return connection.getCatalog();
  }

  public void setTransactionIsolation(int trans) throws SQLException {
    connection.setTransactionIsolation(trans);
  }

  public int getTransactionIsolation() throws SQLException {
    return connection.getTransactionIsolation();
  }

  public java.sql.SQLWarning getWarnings() throws SQLException {
    return connection.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    connection.clearWarnings();
  }

  public java.sql.Statement createStatement(int int1, int int2)
      throws SQLException {
    return connection.createStatement(int1, int2);
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String string,
      int int1, int int2) throws SQLException {
    return connection.prepareStatement(string, int1, int2);
  }

  public java.sql.CallableStatement prepareCall(java.lang.String string,
      int int1, int int2) throws SQLException {
    return connection.prepareCall(string, int1, int2);
  }

  public java.util.Map getTypeMap() throws SQLException {
    return connection.getTypeMap();
  }

  public void setTypeMap(java.util.Map map) throws SQLException {
    connection.setTypeMap(map);
  }

  public void setHoldability(int holdability) throws SQLException {
    connection.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    return connection.getHoldability();
  }

  public java.sql.Savepoint setSavepoint() throws SQLException {
    return connection.setSavepoint();
  }

  public java.sql.Savepoint setSavepoint(String savePoint) throws SQLException {
    return connection.setSavepoint(savePoint);
  }

  public void rollback(java.sql.Savepoint sp) throws SQLException {
    connection.rollback(sp);
  }

  public void releaseSavepoint(java.sql.Savepoint sp) throws SQLException {
    connection.releaseSavepoint(sp);
  }

  public java.sql.Statement createStatement(int p1, int p2, int p3)
      throws SQLException {
    return connection.createStatement(p1, p2, p3);
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String p1,
      int p2, int p3, int p4) throws SQLException {
    return connection.prepareStatement(p1, p2, p3, p4);
  }

  public java.sql.CallableStatement prepareCall(java.lang.String p1, int p2,
      int p3, int p4) throws SQLException {
    return connection.prepareCall(p1, p2, p3, p4);
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2)
      throws SQLException {
    return connection.prepareStatement(p1, p2);
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String p1,
      int[] p2) throws SQLException {
    return connection.prepareStatement(p1, p2);
  }

  public java.sql.PreparedStatement prepareStatement(java.lang.String p1,
      String[] p2) throws SQLException {
    return connection.prepareStatement(p1, p2);
  }

  public Array createArrayOf(String typeName, Object[] elements)
      throws SQLException {
    return this.connection.createArrayOf(typeName, elements);
  }

  public Blob createBlob() throws SQLException {
    return this.connection.createBlob();
  }

  public Clob createClob() throws SQLException {
    return this.connection.createClob();
  }

  public NClob createNClob() throws SQLException {
    return this.connection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return this.connection.createSQLXML();
  }

  public Struct createStruct(String typeName, Object[] attributes)
      throws SQLException {
    return this.connection.createStruct(typeName, attributes);
  }

  public Properties getClientInfo() throws SQLException {
    return this.connection.getClientInfo();
  }

  public String getClientInfo(String name) throws SQLException {
    return this.connection.getClientInfo(name);
  }

  public boolean isValid(int timeout) throws SQLException {
    return this.connection.isValid(timeout);
  }

  public void setClientInfo(Properties properties)
      throws SQLClientInfoException {
    this.connection.setClientInfo(properties);
  }

  public void setClientInfo(String name, String value)
      throws SQLClientInfoException {
    this.connection.setClientInfo(name, value);
  }

  public boolean isWrapperFor(Class clazz) throws SQLException {
    return this.connection.isWrapperFor(clazz);
  }

  public Object unwrap(Class clazz) throws SQLException {
    return this.connection.unwrap(clazz);
  }

}
