/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role.test;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.notification.role.UserRoleEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listener of {@link UserRoleEvent} to update the validators in the myComponent instances. This for
 * testing the {@link UserRoleEvent} carries all the expected information to update in the component
 * instances their data related to roles played by users in those instances.
 *
 * @author mmoquillon
 */
@Service
public class UserRoleEventListener extends CDIResourceEventListener<UserRoleEvent> {

  @Inject
  private ResourceValidators validators;

  @Override
  public void onDeletion(UserRoleEvent event) {
    if (event.getRole().equals("validator")) {
      List<User> users = event.getUserIds().stream()
          .map(User::getById)
          .collect(Collectors.toList());
      event.getInstanceIds().forEach(c ->
          validators.getAll(c).stream()
              .map(Validator::getResource)
              .forEach(r -> users.forEach(r::removeFromValidators)));
    }
  }
}