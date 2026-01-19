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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Silverpeas component instance that can be accessed by several users. It is component instance
 * shared by several users.
 *
 * @author Yohann Chastagnier
 */
public interface SilverpeasSharedComponentInstance extends SilverpeasComponentInstance,
    LocalizedResource, Securable {

  /**
   * Gets a personal silverpeas component instance from the specified identifier.
   *
   * @param sharedComponentInstanceId a personal component instance identifier as string.
   * @return an optional silverpeas personal component instance of
   * {@link SilverpeasSharedComponentInstance}.
   */
  static Optional<SilverpeasSharedComponentInstance> getById(String sharedComponentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getSharedById(sharedComponentInstanceId);
  }

  /**
   * Gets the identity of the shared component instance referred by the specified unique global
   * identifier. The identity of a shared component instance is serialized in its unique
   * identifier.
   *
   * @param componentInstanceId the unique global identifier of a shared component instance. It must
   * be non-null and well-formed.
   * @return a non-null Identity.
   * @throws IllegalArgumentException if the argument isn't a correct shared component instance
   * identifier.
   * @throws NullPointerException if the argument is null.
   */
  @NonNull
  static Identity getIdentity(@NonNull final String componentInstanceId) {
    return new Identity(componentInstanceId);
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
   *
   * @param user a user in Silverpeas.
   * @return true if the user can both access this component instance and has management privilege
   * on this component instance (by being either an administrator or a space manager)
   */
  @Override
  default boolean canBeModifiedBy(User user) {
    return SpaceAccessControl.get().hasUserSpaceManagementAuthorization(user.getId(), getSpaceId());
  }

  /**
   * Can the user add contributions into a shared component instance?
   *
   * @param user a user in Silverpeas.
   * @return true if he can, false otherwise.
   * @see Securable#canBeFiledInBy(User)
   */
  @Override
  default boolean canBeFiledInBy(User user) {
    final Set<SilverpeasRole> role = ComponentAccessControl.get()
        .getUserRoles(user.getId(), getId(), AccessControlContext.init());
    return role.stream().anyMatch(r -> r.isGreaterThanOrEquals(SilverpeasRole.WRITER));
  }

  class Identity extends SilverpeasComponentInstance.Identity {

    private static final Pattern COMPONENT_INSTANCE_IDENTIFIER =
        Pattern.compile("^([a-zA-Z-_]+)(\\d+)$");

    protected Identity(String componentInstanceId) {
      super(componentInstanceId);
    }

    protected Identity(String name, int localId) {
      super(name, localId);
    }

    @Override
    protected void decode(@NonNull String componentInstanceId) {
      Matcher matcher = COMPONENT_INSTANCE_IDENTIFIER.matcher(componentInstanceId);
      if (matcher.matches()) {
        setComponentName(matcher.group(1));
        setInstanceLocalId(Integer.parseInt(matcher.group(2)));
      } else {
        throw new IllegalArgumentException(
            "The argument doesn't represent a shared component instance identifier!");
      }
    }

    @Override
    public String toString() {
      return getComponentName() + getInstanceLocalId();
    }

    public static boolean isValid(String componentInstanceId) {
      Matcher matcher = COMPONENT_INSTANCE_IDENTIFIER.matcher(componentInstanceId);
      return matcher.matches();
    }
  }
}
