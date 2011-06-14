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

package com.silverpeas.admin.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.SessionContext;

import com.stratelia.silverpeas.authentication.security.SecurityHolder;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceAndChildren;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class AdminBmEJB implements javax.ejb.SessionBean {

  private static final long serialVersionUID = 8753816261083500713L;
  AdminController ac = null;

  public AdminBmEJB() {
  }

  private AdminController getAdminController() {
    if (ac == null) {
      ac = new AdminController("unknown");
    }
    return ac;
  }

  public ArrayList<String> getAllRootSpaceIds() throws RemoteException {
    String[] spaceIds = getAdminController().getAllRootSpaceIds();
    ArrayList<String> result = new ArrayList<String>();

    for (String spaceId : spaceIds) {
      result.add(spaceId);
    }

    return result;
  }

  public SpaceInst getSpaceInstById(String spaceId) throws RemoteException {
    return getAdminController().getSpaceInstById(spaceId);
  }

  public SpaceInstLight getSpaceInstLight(String spaceId) throws RemoteException {
    return getAdminController().getSpaceInstLight(spaceId);
  }

  public ComponentInst getComponentInst(String componentId) throws RemoteException {
    return getAdminController().getComponentInst(componentId);
  }

  public ComponentInstLight getComponentInstLight(String componentId) throws RemoteException {
    return getAdminController().getComponentInstLight(componentId);
  }

  public ArrayList<String> getAvailCompoIds(String spaceId, String userId) throws RemoteException {
    String[] compoIds = getAdminController().getAvailCompoIds(spaceId, userId);
    ArrayList<String> result = new ArrayList<String>();

    for (String compoId : compoIds) {
      result.add(compoId);
    }

    return result;
  }

  public boolean isComponentAvailable(String spaceId, String componentId, String userId)
      throws RemoteException {
    return getAdminController().isComponentAvailable(componentId, userId);
  }

  public List<String> getAvailableSpaceIds(String userId) throws RemoteException {
    String[] spaceIds = getAdminController().getAllSpaceIds(userId);
    List<String> result = new ArrayList<String>();

    for (String spaceId : spaceIds) {
      result.add(spaceId);
    }

    return result;
  }

  public List<String> getAvailableSubSpaceIds(String spaceId, String userId) throws RemoteException {
    String[] subSpaceIds = getAdminController().getAllSubSpaceIds(spaceId, userId);
    List<String> result = new ArrayList<String>();

    for (String subSpaceId : subSpaceIds) {
      result.add(subSpaceId);
    }

    return result;
  }

  public Hashtable<String, SpaceAndChildren> getTreeView(String userId, String spaceId) throws RemoteException {
    return getAdminController().getTreeView(userId, spaceId);
  }

  public String authenticate(String sKey, String sSessionId) throws RemoteException {
    return getAdminController().authenticate(sKey, sSessionId, false);
  }

  public String getUserIdByLoginAndDomain(String login, String domainId)
      throws RemoteException {
    return getAdminController().getUserIdByLoginAndDomain(login, domainId);
  }

  public void addSecurityData(String securityId, String userId, String domainId)
      throws RemoteException {
    SecurityHolder.addData(securityId, userId, domainId);
  }

  public void addSecurityData(String securityId, String userId, String domainId,
      boolean persistent)
      throws RemoteException {
    SecurityHolder.addData(securityId, userId, domainId, persistent);
  }
  
  public String addComponentInst(ComponentInst componentInst, String userId)
      throws RemoteException {
    return getAdminController().addComponentInst(componentInst, userId);
  }
  
  public void updateComponentOrderNum(String sComponentId, int orderNum) throws RemoteException {
    getAdminController().updateComponentOrderNum(sComponentId, orderNum);
  }

  /* Ejb Methods */

  public void ejbCreate() {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }

}