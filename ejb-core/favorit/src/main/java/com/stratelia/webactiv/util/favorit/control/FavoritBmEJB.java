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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.favorit.control;

import java.util.*;
import javax.ejb.*;
import java.sql.*;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.favorit.ejb.NodeActorLinkDAO;
import com.stratelia.webactiv.util.favorit.model.FavoritRuntimeException;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class FavoritBmEJB implements SessionBean {

  private final String rootTableName = "favorit";
  private String dbName = JNDINames.FAVORIT_DATASOURCE;

  /**
   * Constructor declaration
   * @see
   */
  public FavoritBmEJB() {
    SilverTrace.info("favorit", "FavoritBmEJB.FavoritBmEJB",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception e) {
      throw new FavoritRuntimeException("root.MSG_GEN_CONNECTION_OPEN_FAILED",
          e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("favorit", "FavoritBmEJB.freeConnection",
            "root.MSG_GEN_CONNECTION_CLOSE_FAILED");
      }
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param node
   * @see
   */
  public void addFavoritNode(String userId, NodePK node) {
    SilverTrace.info("favorit", "FavoritBmEJB.addFavoritNode",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", node = "
        + node.toString());
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.add(con, rootTableName, userId, node);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_ADD_FAVORIT", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param node
   * @see
   */
  public void removeFavoritNode(String userId, NodePK node) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritNode",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", node = "
        + node.toString());
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.remove(con, rootTableName, userId, node);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_REMOVE_FAVORIT", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @see
   */
  public void removeFavoritByUser(String userId) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritByUser",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.removeByUser(con, rootTableName, userId);
    } catch (Exception e) {
      throw new FavoritRuntimeException(
          "favorit.CANNOT_REMOVE_FAVORIT_BY_USER", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param node
   * @param path
   * @see
   */
  public void removeFavoritByNodePath(NodePK node, String path) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritByNodePath",
        "root.MSG_GEN_ENTER_METHOD", "node = " + node.toString() + ", path = "
        + path);
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.removeByNodePath(con, rootTableName, node, path);
    } catch (Exception e) {
      throw new FavoritRuntimeException(
          "favorit.CANNOT_REMOVE_FAVORIT_BY_NODE", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @see
   */
  public Collection getFavoritNodePKs(String userId) {
    SilverTrace.info("favorit", "FavoritBmEJB.getFavoritNodePKs",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    Connection con = null;
    Collection result = null;

    try {
      con = getConnection();
      result = NodeActorLinkDAO.getNodePKsByActor(con, rootTableName, userId);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_GET_FAVORIT_BY_USER", e);
    } finally {
      freeConnection(con);
    }
    return result;
  }

  // NEWD DLE
  /**
   * Method declaration deprecated
   * @param userId
   * @param space
   * @param componentName
   * @return
   * @see
   */
  /*
   * public Collection getFavoritNodePKsBySpaceAndComponent(String userId, String space, String
   * componentName) { SilverTrace.info("favorit",
   * "FavoritBmEJB.getFavoritNodePKsBySpaceAndComponent", "root.MSG_GEN_ENTER_METHOD", "userId = " +
   * userId + ", space = " + space + ", componentName = " + componentName); Connection con = null;
   * Collection result = null; try { con = getConnection(); result =
   * NodeActorLinkDAO.getNodePKsByActorSpaceAndComponent(con, rootTableName, userId, space,
   * componentName); } catch (Exception e) { throw new FavoritRuntimeException
   * ("favorit.CANNOT_GET_FAVORIT_BY_USER_SPACE_COMPONENT", e); } finally { freeConnection(con); }
   * return result; }
   */

  public Collection getFavoritNodePKsByComponent(String userId,
      String componentName) {
    SilverTrace.info("favorit", "FavoritBmEJB.getFavoritNodePKsByComponent",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId
        + ", componentName = " + componentName);
    Connection con = null;
    Collection result = null;

    try {
      con = getConnection();
      result = NodeActorLinkDAO.getNodePKsByActorComponent(con, rootTableName,
          userId, componentName);
    } catch (Exception e) {
      throw new FavoritRuntimeException(
          "favorit.CANNOT_GET_FAVORIT_BY_USER_SPACE_COMPONENT", e);
    } finally {
      freeConnection(con);
    }
    return result;
  }

  // NEWF DLE

  /**
   * Method declaration
   * @throws CreateException
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * @param sc
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }

}
