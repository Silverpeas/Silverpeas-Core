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

package com.silverpeas.admin.ejb;

import com.stratelia.silverpeas.authentication.security.SecurityHolder;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceAndChildren;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

import javax.ejb.SessionContext;

import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.exception.QuotaRuntimeException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminBmEJB implements javax.ejb.SessionBean, AdminBusiness {

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

  @Override
  public ArrayList<String> getAllRootSpaceIds() {
    String[] spaceIds = getAdminController().getAllRootSpaceIds();
    ArrayList<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(spaceIds));

    return result;
  }

  @Override
  public SpaceInst getSpaceInstById(String spaceId) {
    return getAdminController().getSpaceInstById(spaceId);
  }

  @Override
  public SpaceInstLight getSpaceInstLight(String spaceId) {
    return getAdminController().getSpaceInstLight(spaceId);
  }

  @Override
  public ComponentInst getComponentInst(String componentId) {
    return getAdminController().getComponentInst(componentId);
  }

  @Override
  public ComponentInstLight getComponentInstLight(String componentId) {
    return getAdminController().getComponentInstLight(componentId);
  }

  @Override
  public ArrayList<String> getAvailCompoIds(String spaceId, String userId) {
    String[] compoIds = getAdminController().getAvailCompoIds(spaceId, userId);
    ArrayList<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(compoIds));

    return result;
  }

  @Override
  public boolean isComponentAvailable(String spaceId, String componentId, String userId) {
    return getAdminController().isComponentAvailable(componentId, userId);
  }

  @Override
  public List<String> getAvailableSpaceIds(String userId) {
    String[] spaceIds = getAdminController().getAllSpaceIds(userId);
    List<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(spaceIds));

    return result;
  }

  @Override
  public List<String> getAvailableSubSpaceIds(String spaceId, String userId) {
    String[] subSpaceIds = getAdminController().getAllSubSpaceIds(spaceId, userId);
    List<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(subSpaceIds));

    return result;
  }

  @Override
  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId) {
    return getAdminController().getTreeView(userId, spaceId);
  }

  @Override
  public String authenticate(String sKey, String sSessionId) {
    return getAdminController().authenticate(sKey, sSessionId, false);
  }

  @Override
  public String getUserIdByLoginAndDomain(String login, String domainId) {
    return getAdminController().getUserIdByLoginAndDomain(login, domainId);
  }

  @Override
  public void addSecurityData(String securityId, String userId, String domainId) {
    SecurityHolder.addData(securityId, userId, domainId);
  }

  @Override
  public void addSecurityData(String securityId, String userId, String domainId,
      boolean persistent) {
    SecurityHolder.addData(securityId, userId, domainId, persistent);
  }

  @Override
  public String addComponentInst(ComponentInst componentInst, String userId) {
    try {
      return getAdminController().addComponentInst(componentInst, userId);
    } catch (QuotaException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void updateComponentOrderNum(String sComponentId, int orderNum) {
    getAdminController().updateComponentOrderNum(sComponentId, orderNum);
  }

  /*
   * Ejb Methods
   */
  public void ejbCreate() {
  }

  @Override
  public void ejbRemove() {
  }

  @Override
  public void ejbActivate() {
  }

  @Override
  public void ejbPassivate() {
  }

  @Override
  public void setSessionContext(SessionContext sc) {
  }
}