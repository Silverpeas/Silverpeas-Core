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

import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import java.util.Optional;

/**
 * @author Yohann Chastagnier
 */
public interface SilverpeasSharedComponentInstance extends SilverpeasComponentInstance,
    LocalizedResource, Securable {

  /**
   * Gets a personal silverpeas component instance from the specified identifier.
   * @param sharedComponentInstanceId a personal component instance identifier as string.
   * @return an optional silverpeas personal component instance of {@link
   * SilverpeasSharedComponentInstance}.
   */
  static Optional<SilverpeasSharedComponentInstance> getById(String sharedComponentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getSharedById(sharedComponentInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  BasicIdentifier getIdentifier();

  @Override
  default boolean canBeAccessedBy(User user) {
    return ComponentAccessControl.get().isUserAuthorized(user.getId(), getIdentifier());
  }

  /**
   * Is the user can modify this component instance?
   * @param user a user in Silverpeas.
   * @return true if the user can both access this component instance and has management privilege
   * on this component instance (by being either an administrator or a space manager)
   */
  @Override
  default boolean canBeModifiedBy(User user) {
    return canBeAccessedBy(user) && (user.isAccessAdmin() || user.isAccessSpaceManager());
  }

  @Override
  default boolean canBeFiledInBy(User user) {
    return canBeModifiedBy(user);
  }
}
