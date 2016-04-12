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

package org.silverpeas.web.external.webconnections.control;

import org.silverpeas.core.web.external.webconnections.model.ConnectionDetail;
import org.silverpeas.core.web.external.webconnections.model.WebConnectionsInterface;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.util.StringUtil;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WebConnectionsSessionController extends AbstractComponentSessionController {
  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public WebConnectionsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.external.webConnections.multilang.webConnectionsBundle",
        "org.silverpeas.external.webConnections.settings.webConnectionsIcons");
  }

  /**
   * get the connection for the componentId
   * @param componentId : String
   * @return connection : ConnectionDetail
   */
  public ConnectionDetail getConnection(String componentId) {
    ConnectionDetail connection = null;
    connection = getWebConnectionsInterface().getWebConnection(componentId, getUserId());
    if (connection != null) {
      addParamToConnection(connection);
    }
    return connection;
  }

  /**
   * add all parameters to the connection
   * @param connection : ConnectionDetail
   */
  private void addParamToConnection(ConnectionDetail connection) {
    // ajouter les données venue de hyperlink
    ComponentInst inst = getOrganisationController().getComponentInst(connection.getComponentId());
    String componentName = inst.getLabel();
    String url = inst.getParameterValue("Url");
    Map<String, String> param = connection.getParam();
    int i = 1;
    String nameParam = inst.getParameterValue("nameParam" + i);
    while (StringUtil.isDefined(nameParam)) {
      String value = inst.getParameterValue("valueParam" + i);
      param.put(nameParam, value);
      i = i + 1;
      nameParam = inst.getParameterValue("nameParam" + i);
    }
    connection.setUrl(url);
    connection.setParam(param);
    connection.setUserId(getUserId());
    connection.setUrl(url);
    connection.setComponentName(componentName);
  }

  /**
   * get the connection corresponding to connectionId
   * @param connectionId : String
   * @return the connection : ConnectionDetail
   */
  public ConnectionDetail getConnectionById(String connectionId) {
    ConnectionDetail connection = getWebConnectionsInterface().getWebConnectionById(connectionId, getUserId());
    if (connection != null) {
      addParamToConnection(connection);
    }
    return connection;
  }

  /**
   * update the connection (corresponding to connectionId) with the login and the password
   * @param connectionId : String
   * @param login : String
   * @param password : String
   */
  public void updateConnection(String connectionId, String login, String password) {
    getWebConnectionsInterface().updateWebConnection(connectionId, login, password, getUserId());
  }

  /**
   * delete the connection corresponding to connectionId
   * @param connectionId : String
   */
  public void deleteConnection(String connectionId) {
    getWebConnectionsInterface().deleteWebConnection(connectionId, getUserId());
  }

  /**
   * create a new connection
   * @param connection : ConnectionDetail
   */
  public void createConnection(ConnectionDetail connection) {
    connection.setUserId(getUserId());
    getWebConnectionsInterface().createWebConnection(connection);
  }

  /**
   * get all the connections for the current user
   * @return a list of ConnectionDetail
   * @throws RemoteException
   */
  public List<ConnectionDetail> getConnectionsByUser() throws RemoteException {
    List<ConnectionDetail> connections =
        getWebConnectionsInterface().listWebConnectionsOfUser(getUserId());
    List<ConnectionDetail> newConnections = new ArrayList<ConnectionDetail>();
    Iterator<ConnectionDetail> it = connections.iterator();
    while (it.hasNext()) {
      ConnectionDetail connection = it.next();
      addParamToConnection(connection);
      newConnections.add(connection);
    }
    return connections;
  }

  /**
   * @return WebConnectionsInterface
   */
  private WebConnectionsInterface getWebConnectionsInterface() {
    return WebConnectionsInterface.get();
  }
}