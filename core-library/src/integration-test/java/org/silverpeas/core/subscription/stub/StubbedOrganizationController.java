/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.subscription.stub;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.DefaultOrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import static org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest.GROUPID_WITH_ONE_USER;
import static org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest.USERID_OF_GROUP_WITH_ONE_USER;
import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedOrganizationController extends DefaultOrganizationController {
  private static final long serialVersionUID = -8307476470533272352L;

  @Override
  public ComponentInstLight getComponentInstLight(final String componentId) {
    ComponentInstLight componentInstLight = new ComponentInstLight();
    componentInstLight.setLocalId(Integer.parseInt(componentId.replaceAll("[^0-9]", "")));
    componentInstLight.setName(componentId.replaceAll("[0-9]", ""));
    return componentInstLight;
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(final String groupId) {
    UserDetail user = new UserDetail();
    if (GROUPID_WITH_ONE_USER.equals(groupId)) {
      user.setId(USERID_OF_GROUP_WITH_ONE_USER);
      return new UserDetail[]{user};
    } else {
      return new UserDetail[]{};
    }
  }

  @Override
  public String[] getAllGroupIdsOfUser(final String userId) {
    if (USERID_OF_GROUP_WITH_ONE_USER.equals(userId)) {
      return new String[]{GROUPID_WITH_ONE_USER};
    } else {
      return new String[]{};
    }
  }
}
