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
package com.stratelia.webactiv.util.subscribe.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.subscribe.model.SubscribeRuntimeException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.sql.Connection;
import java.util.Collection;

/**
 * Class declaration
 * @author
 */
public class SubscribeBmEJB implements SessionBean {

  private final String rootTableName = "subscribe";
  private String dbName = JNDINames.SUBSCRIBE_DATASOURCE;

  /**
   * Constructor declaration
   * @see
   */
  public SubscribeBmEJB() {
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
      throw new SubscribeRuntimeException("SubscribeBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param node
   * @see
   */
  public void addSubscribe(String userId, NodePK node) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.addSubscribe",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.add(con, userId, node);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.addSubscribe()",
          SilverpeasRuntimeException.ERROR, "subscribe.CANNOT_ADD_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param node
   * @see
   */
  public void removeSubscribe(String userId, NodePK node) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.removeSubscribe",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.remove(con, userId, node);
    } catch (Exception e) {
      throw new SubscribeRuntimeException("SubscribeBmEJB.removeSubscribe()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_REMOVE_SUBSCRIBE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @see
   */
  public void removeUserSubscribes(String userId) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.removeUserSubscribes",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.removeByUser(con, userId);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.removeUserSubscribes()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_REMOVE_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param node
   * @param path
   * @see
   */
  public void removeNodeSubscribes(NodePK node, String path) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.removeNodeSubscribes",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      NodeActorLinkDAO.removeByNodePath(con, node, path);
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.removeNodeSubscribes()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_REMOVE_NODE_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @see
   */
  public Collection getUserSubscribePKs(String userId) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getUserSubscribePKs",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection result = NodeActorLinkDAO.getNodePKsByActor(con,
          userId);

      return result;
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.getUserSubscribePKs()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_GET_USER_SUBSCRIBES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param componentName
   * @return
   * @see
   */
  public Collection getUserSubscribePKsByComponent(String userId,
      String componentName) {
    SilverTrace.info("subscribe",
        "SubscribeBmEJB.getUserSubscribePKsByComponent",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection result = NodeActorLinkDAO.getNodePKsByActorComponent(con, userId, componentName);
      return result;
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.getUserSubscribesPKsByspaceAndcomponent()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_GET_USER_SUBSCRIBES_SPACE_COMPONENT", e);
    } finally {
      DBUtil.close(con);
    }
  }

  // NEWF DLE

  /**
   * Method declaration
   * @param node
   * @return
   * @see
   */
  public Collection getNodeSubscriberDetails(NodePK node) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getNodeSubscriberDetails",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection result = NodeActorLinkDAO.getActorPKsByNodePK(con,
          node);

      return result;
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.getNodeSubscriberDetails()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_GET_NODE_SUBSCRIBERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Collection getNodeSubscriberDetails(Collection nodePKs) {
    SilverTrace.info("subscribe", "SubscribeBmEJB.getNodeSubscriberDetails",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      Collection result = NodeActorLinkDAO.getActorPKsByNodePKs(con, nodePKs);

      return result;
    } catch (Exception e) {
      throw new SubscribeRuntimeException(
          "SubscribeBmEJB.getNodeSubscriberDetails()",
          SilverpeasRuntimeException.ERROR,
          "subscribe.CANNOT_GET_NODE_SUBSCRIBERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

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
