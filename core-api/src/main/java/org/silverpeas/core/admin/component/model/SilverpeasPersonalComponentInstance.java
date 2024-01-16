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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

/**
 * @author Yohann Chastagnier
 */
public interface SilverpeasPersonalComponentInstance extends SilverpeasComponentInstance {

  /**
   * Gets a personal silverpeas component instance from the specified identifier.
   * @param personalComponentInstanceId a personal component instance identifier as string.
   * @return an optional silverpeas personal component instance of {@link
   * SilverpeasPersonalComponentInstance}.
   */
  static Optional<SilverpeasPersonalComponentInstance> getById(String personalComponentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getPersonalById(personalComponentInstanceId);
  }

  /**
   * Gets the user associated to the personal component instance.
   * @return a {@link User} instance.
   */
  User getUser();

  @Override
  default boolean isPersonal() {
    return true;
  }

  @Override
  default Collection<SilverpeasRole> getSilverpeasRolesFor(User user) {
    final Collection<SilverpeasRole> roles;
    if (getUser().getId().equals(user.getId())) {
      roles = EnumSet.of(SilverpeasRole.admin);
    } else if (isPublic()) {
      roles = EnumSet.of(SilverpeasRole.user);
    } else {
      roles = EnumSet.noneOf(SilverpeasRole.class);
    }
    return roles;
  }
}
