/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.stub;


import org.silverpeas.core.admin.service.DefaultOrganizationController;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import javax.inject.Singleton;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author Yohann Chastagnier
 */
@Service
@Singleton
@Alternative
@Priority(APPLICATION + 10)
@Named("organizationController")
public class StubOrganizationController extends DefaultOrganizationController {
  private static final long serialVersionUID = 1L;

  @Override
  public UserDetail getUserDetail(final String sUserId) {
    UserDetail user = new UserDetail();
    user.setId(sUserId);
    if ("11".equals(sUserId) || "13".equals(sUserId)) {
      user.setState(UserState.VALID);
    } else if ("12".equals(sUserId)) {
      user.setState(UserState.DELETED);
    } else {
      return null;
    }
    return user;
  }

  @Override
  public String[] getUserProfiles(final String userId, final String componentId) {
    return new String[0];
  }
}