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

package org.silverpeas.core.persistence.jdbc;

/**
 * Title: userPanelPeas
 * Description: this is an object pair of pair object
 * Copyright:    Copyright (c) 2002
 * Company:      Silverpeas
 * @author J-C Groccia
 * @version 1.0
 */

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongText {
  public final static int PART_SIZE_MAX = 1998;

  static public int addLongText(String theText) throws SQLException {
    int theId = DBUtil.getNextId("ST_LongText", "id");
    PreparedStatement stmt = null;
    Connection privateConnection = null;
    int orderNum = 0;
    String partText = null;
    String theQuery = "insert into ST_LongText (id, orderNum, bodyContent) values (?, ?, ?)";

    try {
      privateConnection = openConnection();
      stmt = privateConnection.prepareStatement(theQuery);
      if ((theText == null) || (theText.length() <= 0)) {
        stmt.setInt(1, theId);
        stmt.setInt(2, orderNum);
        stmt.setString(3, "");
        stmt.executeUpdate();
      } else {
        while (orderNum * PART_SIZE_MAX < theText.length()) {
          if ((orderNum + 1) * PART_SIZE_MAX < theText.length())
            partText = theText.substring(orderNum * PART_SIZE_MAX,
                (orderNum + 1) * PART_SIZE_MAX);
          else
            partText = theText.substring(orderNum * PART_SIZE_MAX);
          stmt.setInt(1, theId);
          stmt.setInt(2, orderNum);
          stmt.setString(3, partText);
          stmt.executeUpdate();
          orderNum++;
        }
      }
    } finally {
      DBUtil.close(stmt);
      closeConnection(privateConnection);
    }
    return theId;
  }

  static public String getLongText(int longTextId) throws UtilException {
    PreparedStatement stmt = null;
    Connection privateConnection = null;
    ResultSet rs = null;
    StringBuilder valret = new StringBuilder();
    String theQuery = "select bodyContent from ST_LongText where id = ? order by orderNum";

    try {
      privateConnection = openConnection();
      stmt = privateConnection.prepareStatement(theQuery);
      stmt.setInt(1, longTextId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        valret.append(rs.getString(1));
      }
      return valret.toString();
    } catch (Exception e) {
      throw new UtilException("LongText.getLongText()",
          SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", Integer
          .toString(longTextId), e);
    } finally {
      DBUtil.close(rs, stmt);
      closeConnection(privateConnection);
    }
  }

  static public void removeLongText(int longTextId) throws UtilException {
    PreparedStatement stmt = null;
    Connection privateConnection = null;
    String theQuery = "delete from ST_LongText where id = ?";

    try {
      privateConnection = openConnection();
      stmt = privateConnection.prepareStatement(theQuery);
      stmt.setInt(1, longTextId);
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new UtilException("LongText.removeLongText()",
          SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", Integer
          .toString(longTextId), e);
    } finally {
      DBUtil.close(stmt);
      closeConnection(privateConnection);
    }
  }

  static protected Connection openConnection() throws UtilException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
    } catch (Exception e) {
      throw new UtilException("LongText.openConnection()",
          SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", e);
    }

    return con;
  }

  static protected void closeConnection(Connection con) {
    try {
      con.close();
    } catch (Exception e) {
      SilverTrace.error("util", "LongText.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }
}
