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

package com.stratelia.webactiv.jaas;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;

import java.security.Principal;
import java.util.regex.Pattern;

public class SilverpeasUserPrincipal implements Principal {

  private String userId;
  private boolean administrator;
  private static final Pattern COMPONENTID_PATTERN = Pattern.compile("^([a-zA-Z]+)([0-9]+)$");

  public SilverpeasUserPrincipal(String userId, boolean administrator) {
    this.userId = userId;
    this.administrator = administrator;
  }

  public boolean canAccess(String componentId) {
    if (COMPONENTID_PATTERN.matcher(componentId).find()) {
      AccessController<String> accessController =
          AccessControllerProvider.getAccessController("componentAccessController");
      return accessController.isUserAuthorized(userId, componentId);
    }
    return false;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isAdministrator() {
    return this.administrator;
  }

  @Override
  public String getName() {
    return userId;
  }
}