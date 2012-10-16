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

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceAndChildren;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ehugonnet
 */
public interface AdminBusiness {

  public ArrayList<String> getAllRootSpaceIds() throws RemoteException;

  public SpaceInst getSpaceInstById(String spaceId) throws RemoteException;

  public SpaceInstLight getSpaceInstLight(String spaceId) throws RemoteException;

  public ComponentInst getComponentInst(String componentId) throws RemoteException;

  public ComponentInstLight getComponentInstLight(String componentId) throws RemoteException;

  public ArrayList<String> getAvailCompoIds(String spaceId, String userId) throws RemoteException;

  public boolean isComponentAvailable(String spaceId, String componentId, String userId) throws
      RemoteException;

  public List<String> getAvailableSpaceIds(String userId) throws RemoteException;

  public List<String> getAvailableSubSpaceIds(String spaceId, String userId) throws RemoteException;

  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId) throws
      RemoteException;

  public String authenticate(String sKey, String sSessionId) throws RemoteException;

  public String getUserIdByLoginAndDomain(String login, String domainId) throws RemoteException;

  public void addSecurityData(String securityId, String userId, String domainId) throws
      RemoteException;

  public void addSecurityData(String securityId, String userId, String domainId, boolean persistent)
      throws RemoteException;

  public String addComponentInst(ComponentInst componentInst, String userId) throws RemoteException;

  public void updateComponentOrderNum(String sComponentId, int orderNum) throws RemoteException;
}
