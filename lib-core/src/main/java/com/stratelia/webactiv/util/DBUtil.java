/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.MultilangMessage;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.pool.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtil {

  private static DBUtil instance;

  /**
   * @return the DateFieldLength
   */
  public static int getDateFieldLength() {
    return getInstance().dateFieldLength;
  }

  /**
   * @return the TextMaxiLength
   */
  public static int getTextMaxiLength() {
    return getInstance().textMaxiLength;
  }

  /**
   * @return the TextAreaLength
   */
  public static int getTextAreaLength() {
    return getInstance().textAreaLength;
  }

  /**
   * @return the TextFieldLength
   */
  public static int getTextFieldLength() {
    return getInstance().textFieldLength;
  }

  private Connection connectionForTest;

  private DBUtil(Connection connectionForTest) {
    this.connectionForTest = connectionForTest;
  }

  public static DBUtil getInstance() {
    synchronized (DBUtil.class) {
      if (instance == null) {
        instance = new DBUtil(null);
      }
    }
    return instance;
  }

  public static DBUtil getInstanceForTest(Connection connectionForTest) {
    clearTestInstance();
    synchronized (DBUtil.class) {
      if (connectionForTest != null) {
        instance = new DBUtil(connectionForTest);
        
      }
    }
    return instance;
  }

  public static void clearTestInstance() {
    synchronized (DBUtil.class) {
      instance = new DBUtil(null);
      dsStock.clear();
    }
  }

  /**
   * TextFieldLength is the maximum length to store an html textfield input in db.
   */
  private volatile int textFieldLength = 1000;
  /**
   * TextAreaLength is the maximum length to store an html textarea input in db.
   */
  private volatile int textAreaLength = 2000;
  /**
   * TextMaxiLength is the maximum length to store in db. This length is to use with fields that can
   * contain a lot of information. This is the case of publication's model for exemple. TODO : In
   * the near future, these fields will have to be put in BLOB (Binary Large OBject).
   */
  private volatile int textMaxiLength = 4000;
  /**
   * DateFieldLength is the length to use for date storage.
   */
  private volatile int dateFieldLength = 10;
  // Static for the makeConnection
  private InitialContext ic = null;
  private static Map<String, DataSource> dsStock = new HashMap<String, DataSource>(5);

  /**
   * fabrique une nouvelle connection
   * @param dbName le nom de la base de donnée
   * @return a new connection to the database.
   * @throws UtilException
   */
  public static Connection makeConnection(String dbName) {
    return getInstance().openConnection(dbName);
  }

  private synchronized Connection openConnection(String dbName) {
    SilverTrace.debug("util", "DBUtil makeConnection", "DBUtil : makeConnection : entree");
    DataSource ds = null;
    if (ic == null) {
      try {
        ic = new InitialContext();
      } catch (NamingException e) {
        throw new UtilException("DBUtil.makeConnection", "util.MSG_CANT_GET_INITIAL_CONTEXT", e);
      }
    }
    try {
      ds = dsStock.get(dbName);
      if (ds == null) {
        ds = (DataSource) ic.lookup(dbName);
        dsStock.put(dbName, ds);
      }
    } catch (NamingException e) {
      throw new UtilException("DBUtil.makeConnection", new MultilangMessage(
          "util.MSG_BDD_REF_NOT_FOUND", dbName).toString(), e);
    }

    try {
      return ds.getConnection();
    } catch (SQLException e) {
      throw new UtilException("DBUtil.makeConnection", new MultilangMessage(
          "util.MSG_BDD_REF_CANT_GET_CONNECTION", dbName).toString(), e);
    }
  }

  /**
   * Return a new unique Id for a table.
   * @param tableName the name of the table.
   * @param idName the name of the column.
   * @return a unique id.
   * @throws UtilException
   */
  public static int getNextId(String tableName, String idName) throws UtilException {
    Connection privateConnection = null;
    boolean testingMode = false;
    try {
      // On ne peux pas utiliser une simple connection du pool
      // on utilise une connection extérieure au contexte transactionnel des ejb
      synchronized (DBUtil.class) {
        if (getInstance().connectionForTest != null) {
          privateConnection = getInstance().connectionForTest;
          testingMode = true;
        } else {
          privateConnection = ConnectionPool.getConnection();
        }
      }
      privateConnection.setAutoCommit(false);
      return getNextId(privateConnection, tableName, idName);
    } catch (Exception exe) {
      SilverTrace.debug("util", "DBUtil.getNextId", "impossible de recupérer le prochain id", exe);
      if (privateConnection != null) {
        try {
          privateConnection.rollback();
        } catch (SQLException e) {
          SilverTrace.error("util", "DBUtil.getNextId", "util.MSG_IMOSSIBLE_UNDO_TRANS", e);
        }
      }
      throw new UtilException("DBUtil.getNextId", new MultilangMessage(
          "util.MSG_CANT_GET_A_NEW_UNIQUE_ID", tableName, idName).toString(), exe);
    } finally {
      try {
        if (privateConnection != null && !testingMode) {
          privateConnection.close();
        }
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.getNextId", "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * Return a new unique Id for a table.
   * @param connection the JDBC connection.
   * @param tableName the name of the table.
   * @param idName the name of the column.
   * @return a unique id.
   * @throws SQLException
   */
  public static int getNextId(Connection connection, String tableName, String idName) throws
      SQLException {
    return getMaxId(connection, tableName, idName);
  }

  protected static int getMaxId(Connection privateConnection, String tableName, String idName)
      throws SQLException {
    int max = 0;
    // tentative d'update
    SilverTrace.debug("util", "DBUtil.getNextId", "dBName = " + tableName);
    try {
      max = updateMaxFromTable(privateConnection, tableName);
      privateConnection.commit();
      return max;
    } catch (Exception e) {
      // l'update n'a rien fait, il faut recuperer une valeur par defaut.
      // on recupere le max (depuis la table existante du composant)
      SilverTrace.debug("util", "DBUtil.getNextId",
          "impossible d'updater, if faut recuperer la valeur initiale", e);
    }
    max = getMaxFromTable(privateConnection, tableName, idName);
    PreparedStatement createStmt = null;
    try {
      // on enregistre le max
      String createStatement = "INSERT INTO UniqueId (maxId, tableName) VALUES (?, ?)";
      createStmt = privateConnection.prepareStatement(createStatement);
      createStmt.setInt(1, max);
      createStmt.setString(2, tableName.toLowerCase());
      createStmt.executeUpdate();
      return max;
    } catch (Exception e) {
      // impossible de creer, on est en concurence, on reessaye l'update.
      SilverTrace.debug("util", "DBUtil.getNextId",
          "impossible de creer, if faut reessayer l'update", e);
    } finally {
      close(createStmt);
      privateConnection.commit();
    }
    max = updateMaxFromTable(privateConnection, tableName);
    privateConnection.commit();
    return max;
  }

  private static int updateMaxFromTable(Connection con, String tablename) throws SQLException {
    String tableName = tablename.toLowerCase();
    int max = 0;
    PreparedStatement prepStmt = null;
    int count = 0;
    try {
      prepStmt = con.prepareStatement("UPDATE UniqueId SET maxId = maxId + 1 WHERE tableName = ?");
      prepStmt.setString(1, tableName);
      count = prepStmt.executeUpdate();
    } finally {
      prepStmt.close();
    }

    if (count == 1) {
      PreparedStatement selectStmt = null;
      ResultSet rs = null;
      try {
        // l'update c'est bien passe, on recupere la valeur
        selectStmt = con.prepareStatement("SELECT maxId FROM UniqueId WHERE tableName = ?");
        selectStmt.setString(1, tableName);
        rs = selectStmt.executeQuery();
        if (!rs.next()) {
          SilverTrace.error("util", "DBUtil.getNextId", "util.MSG_NO_RECORD_FOUND");
          throw new RuntimeException("Erreur Interne DBUtil.getNextId()");
        } else {
          max = rs.getInt(1);
        }
      } finally {
        close(rs, selectStmt);
      }
      return max;
    } else {
      throw new SQLException("Update impossible : Ligne non existante");
    }
  }

  public static int getMaxFromTable(Connection con, String tableName, String idName)
      throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      int maxFromTable = 0;
      String nextPKStatement = "SELECT MAX(" + idName + ") " + "FROM " + tableName;
      prepStmt = con.prepareStatement(nextPKStatement);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        maxFromTable = rs.getInt(1);
      }
      return maxFromTable + 1;
    } finally {
      close(rs, prepStmt);
    }
  }

  public static String convertToBD(String date) {
    String jour = "";
    String mois = "";
    String annee = "";
    try {
      jour = date.substring(0, 2);
      mois = date.substring(3, 5);
      annee = date.substring(6);
    } catch (Exception e) {
      throw new EJBException("DBUtil.convertToBD " + e);
    }
    return (annee + '/' + mois + '/' + jour);
  }

  public static String convertToClient(String date) {
    String jour = "";
    String mois = "";
    String annee = "";
    try {
      annee = date.substring(0, 4);
      mois = date.substring(5, 7);
      jour = date.substring(8);
    } catch (Exception e) {
      throw new EJBException("DBUtil.convertToClient " + e);
    }
    return (jour + '/' + mois + '/' + annee);
  }

  // Close JDBC ResultSet and Statement
  public static void close(ResultSet rs, Statement st) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_RESULTSET", e);
      }
    }
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_STATEMENT", e);
      }
    }
  }

  // Close JDBC Statement
  public static void close(Statement st) {
    close(null, st);
  }

  // Close JDBC ResultSet
  public static void close(ResultSet rs) {
    close(rs, null);
  }

  public static void close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_CONNECTION", e);
      }
    }
  }

  public static void rollback(Connection connection) {
    if (connection != null) {
      try {
        if (!connection.getAutoCommit() && !connection.isClosed()) {
          connection.rollback();
        }
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_ROLLBACK_CONNECTION", e);
      }
    }
  }
}
