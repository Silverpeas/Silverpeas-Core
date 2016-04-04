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

package com.silverpeas.comment.mock;

import com.stratelia.webactiv.beans.admin.DefaultOrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;

import javax.inject.Named;
import static org.mockito.Mockito.*;

/**
 * It is a decorator of the OrganizationController by mocking some of the methods provided by the
 * OrganizationController objects.
 */
@Named("organizationController")
public class OrganizationControllerMocking extends DefaultOrganizationController {
  private static final long serialVersionUID = 4787617291562786442L;

  private OrganizationController
      mock = mock(DefaultOrganizationController.class);

  public void saveUser(final UserDetail user) {
    when(mock.getUserDetail(user.getId())).thenReturn(user);
  }

  @Override
  public UserDetail getUserDetail(String sUserId) {
    return mock.getUserDetail(sUserId);
  }

  public OrganizationController getMock() {
    return mock;
  }
}
