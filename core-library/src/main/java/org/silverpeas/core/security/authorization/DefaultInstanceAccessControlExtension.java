/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.kernel.annotation.Base;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;

/**
 * @author silveryocha
 */
@Base
@Singleton
@Named
public class DefaultInstanceAccessControlExtension
    implements ComponentInstanceAccessControlExtension {

  @Override
  public boolean fillUserRolesFromComponentInstance(
      final ComponentAccessController.DataManager dataManager, final User user,
      final String componentId, final AccessControlContext context,
      final Set<SilverpeasRole> userRoles) {
    final Optional<SilverpeasComponentInstance> optionalInstance = dataManager.getComponentInstance(componentId);
    if (optionalInstance.isEmpty() || (!canAnonymousAccessInstance(context) && user.isAnonymous())) {
      return true;
    }

    final Set<AccessControlOperation> operations = context.getOperations();
    final SilverpeasComponentInstance componentInstance = optionalInstance.get();
    if (componentInstance.isPersonal()) {
      userRoles.addAll(componentInstance.getSilverpeasRolesFor(user));
      if (AccessControlOperation.isPersistActionFrom(operations) ||
          AccessControlOperation.isDownloadActionFrom(operations) ||
          AccessControlOperation.isSharingActionFrom(operations)) {
        userRoles.remove(SilverpeasRole.USER);
      }
      return true;
    }

    if (mustUserBeComponentInstanceAdminIfManagerOfParentSpace(dataManager, user, componentInstance)) {
      userRoles.add(SilverpeasRole.ADMIN);
    }

    if (componentInstance.isPublic() || dataManager.isPublicFilesEnabled(componentId)) {
      userRoles.add(SilverpeasRole.USER);
    }
    return false;
  }

  protected boolean mustUserBeComponentInstanceAdminIfManagerOfParentSpace(
      final ComponentAccessController.DataManager dataManager, final User user,
      final SilverpeasComponentInstance componentInstance) {
    return false;
  }

  protected boolean canAnonymousAccessInstance(final AccessControlContext context) {
    return true;
  }
}
