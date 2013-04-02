/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.favorit.control;


import java.sql.Connection;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 *
 * @author
 * @version %I%, %G%
 */
@Stateless(name="Favorit", description="Stateless session bean to manage favorites.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class FavoritBmEJB implements FavoritBm {

  private final String rootTableName = "favorit";
  private String dbName = JNDINames.FAVORIT_DATASOURCE;

  /**
   * Constructor declaration
   *
   * @see
   */
  public FavoritBmEJB() {
    SilverTrace.info("favorit", "FavoritBmEJB.FavoritBmEJB", "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);
      return con;
    } catch (Exception e) {
      throw new FavoritRuntimeException("root.MSG_GEN_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param node
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void addFavoritNode(String userId, NodePK node) {
    SilverTrace.info("favorit", "FavoritBmEJB.addFavoritNode", "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", node = " + node);
    Connection con = getConnection();
    try {
      NodeActorLinkDAO.add(con, rootTableName, userId, node);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_ADD_FAVORIT", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param node
   * @see
   */
  @Override
  public void removeFavoritNode(String userId, NodePK node) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritNode", "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", node = " + node);
    Connection con = getConnection();
    try {
      NodeActorLinkDAO.remove(con, rootTableName, userId, node);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_REMOVE_FAVORIT", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeFavoritByUser(String userId) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritByUser", "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId);
    Connection con = getConnection();
    try {
      NodeActorLinkDAO.removeByUser(con, rootTableName, userId);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_REMOVE_FAVORIT_BY_USER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param node
   * @param path
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removeFavoritByNodePath(NodePK node, String path) {
    SilverTrace.info("favorit", "FavoritBmEJB.removeFavoritByNodePath", "root.MSG_GEN_ENTER_METHOD",
        "node = " + node.toString() + ", path = " + path);
    Connection con = getConnection();
    try {
      NodeActorLinkDAO.removeByNodePath(con, rootTableName, node, path);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_REMOVE_FAVORIT_BY_NODE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @see
   */
  @Override
  public Collection getFavoritNodePKs(String userId) {
    SilverTrace.info("favorit", "FavoritBmEJB.getFavoritNodePKs",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    Connection con = getConnection();
    try {
      return NodeActorLinkDAO.getNodePKsByActor(con, rootTableName, userId);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_GET_FAVORIT_BY_USER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection getFavoritNodePKsByComponent(String userId, String componentName) {
    SilverTrace.info("favorit", "FavoritBmEJB.getFavoritNodePKsByComponent",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", componentName = " + componentName);
    Connection con = getConnection();
    try {
      return NodeActorLinkDAO.getNodePKsByActorComponent(con, rootTableName, userId, componentName);
    } catch (Exception e) {
      throw new FavoritRuntimeException("favorit.CANNOT_GET_FAVORIT_BY_USER_SPACE_COMPONENT", e);
    } finally {
      DBUtil.close(con);
    }
  }
}
