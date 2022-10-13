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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Provider;

/**
 * Implementation by default of the {@link UserProvider} interface.
 * @author Yohann Chastagnier
 */
@Provider
public class DefaultUserProvider implements UserProvider {

  private static final UserDetail systemUser = new UserDetail();
  static {
    systemUser.setId(UserDetail.SYSTEM_USER_ID);
    systemUser.setFirstName("");
    systemUser.setLastName("SYSTEM");
    systemUser.setLogin("avatar-system");
  }

  @Override
  public User getUser(final String userId) {
    if (UserDetail.SYSTEM_USER_ID.equals(userId)) {
      return getSystemUser();
    }
    return OrganizationController.get().getUserDetail(userId);
  }

  @Override
  public User getSystemUser() {
    return systemUser;
  }
}
