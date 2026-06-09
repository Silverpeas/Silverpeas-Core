/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.Objects;

/**
 * Factory of identities of Silverpeas component instances.
 *
 * @author mmoquillon
 */
class ComponentInstanceIdentityFactory {

  /**
   * Creates the Silverpeas component instance identity from the specified unique global instance
   * identifier. The identifier can refer either a shared component instance or a personal component
   * instance. If the identifier doesn't refer any valid component instance identity, then an
   * {@link IllegalArgumentException} is thrown.
   *
   * @param instanceId the unique global instance identifier of a Silverpeas component instance.
   * @return the identity of a Silverpeas component instance.
   * @throws IllegalArgumentException if the argument doesn't refer any valid component instance
   * identifier.
   */
  @NonNull
  public SilverpeasComponentInstance.Identity create(@NonNull String instanceId) {
    if (SilverpeasSharedComponentInstance.Identity.isValid(instanceId)) {
      return SilverpeasSharedComponentInstance.getIdentity(instanceId);
    } else if (SilverpeasPersonalComponentInstance.Identity.isValid(instanceId)) {
      return SilverpeasPersonalComponentInstance.getIdentity(instanceId);
    } else {
      throw new IllegalArgumentException(instanceId +
          " doesn't match the identifier of any Silverpeas component instance!");
    }
  }

  /**
   * Creates the identity of an instance of a personal component whose name is specified by the
   * first argument and that is owned by the given user.
   *
   * @param name the name of a personal component.
   * @param user the user owning the personal component instance.
   * @return the identity of a personal component instance.
   */
  @NonNull
  public SilverpeasPersonalComponentInstance.Identity create(@NonNull String name,
      @NonNull User user) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(user);
    return new SilverpeasPersonalComponentInstance.Identity(name, Integer.parseInt(user.getId()));
  }

  /**
   * Creates the identity of an instance of a shared component whose name is specified by the first
   * argument and with as local identifier the second argument.
   *
   * @param name the name of the shared component instance.
   * @param localId the unique local identifier of a multi-user component instance.
   * @return the identity of a shared component instance.
   */
  public SilverpeasSharedComponentInstance.Identity create(@NonNull String name,
      @NonNull String localId) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(localId);
    return new SilverpeasSharedComponentInstance.Identity(name, Integer.parseInt(localId));
  }

  /**
   * Is the specified component instance identifier is valid and refers then a possible component
   * instance.
   * @param instanceId the serializing form of a unique identifier of a component instance.
   * @return true if the specified instance identifier is valid, false otherwise.
   */
  public boolean isValid(String instanceId) {
    return SilverpeasSharedComponentInstance.Identity.isValid(instanceId) ||
        SilverpeasPersonalComponentInstance.Identity.isValid(instanceId);
  }
}
  