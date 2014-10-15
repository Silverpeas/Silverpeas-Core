/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.admin.ejb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceAndChildren;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

@Remote
public interface AdminBusiness {

  public ArrayList<String> getAllRootSpaceIds();

  public SpaceInst getSpaceInstById(String spaceId);

  public SpaceInstLight getSpaceInstLight(String spaceId);

  public ComponentInst getComponentInst(String componentId);

  public ComponentInstLight getComponentInstLight(String componentId);

  public ArrayList<String> getAvailCompoIds(String spaceId, String userId);

  public boolean isComponentAvailable(String spaceId, String componentId, String userId);

  public List<String> getAvailableSpaceIds(String userId);

  public List<String> getAvailableSubSpaceIds(String spaceId, String userId);

  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId);

  public String authenticate(String sKey, String sSessionId);

  public String getUserIdByLoginAndDomain(String login, String domainId);

  public String addComponentInst(ComponentInst componentInst, String userId);

  public void updateComponentOrderNum(String sComponentId, int orderNum);
}
