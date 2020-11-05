/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis.security;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.security.authorization.AbstractAccessController;
import org.silverpeas.core.security.authorization.AccessControlContext;

import java.util.Set;

/**
 * Default implementation of the access controller that grants the access for any users and groups
 * of users.
 * @author mmoquillon
 */
public class GrantedAccessController extends AbstractAccessController<String> {

  @Override
  public boolean isUserAuthorized(final Set<SilverpeasRole> userRoles) {
    return true;
  }

  @Override
  public boolean isUserAuthorized(final String userId, final ResourceIdentifier id) {
    return true;
  }

  @Override
  public boolean isUserAuthorized(final String userId, final String object,
      final AccessControlContext context) {
    return true;
  }

  @Override
  public boolean isGroupAuthorized(final String groupId, final String object) {
    return true;
  }
}
  