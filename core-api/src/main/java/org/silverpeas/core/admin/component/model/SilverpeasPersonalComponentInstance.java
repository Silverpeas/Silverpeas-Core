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
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Silverpeas component instance that can be accessed only by one user. This component instance is
 * owned by the user.
 *
 * @author Yohann Chastagnier
 */
public interface SilverpeasPersonalComponentInstance extends SilverpeasComponentInstance {

  /**
   * Gets a personal silverpeas component instance from the specified identifier.
   *
   * @param personalComponentInstanceId a personal component instance identifier as string.
   * @return an optional silverpeas personal component instance of
   * {@link SilverpeasPersonalComponentInstance}.
   */
  static Optional<SilverpeasPersonalComponentInstance> getById(String personalComponentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getPersonalById(personalComponentInstanceId);
  }

  /**
   * Gets the identity of the personal component instance referred by the specified unique global
   * identifier. The identity of a personal component instance is serialized in its unique
   * identifier.
   *
   * @param componentInstanceId the unique global identifier of a personal component instance. It
   * must be non-null and well-formed.
   * @return a non-null Identity.
   * @throws IllegalArgumentException if the argument isn't a correct personal component instance
   * identifier.
   * @throws NullPointerException if the argument is null.
   */
  @NonNull
  static Identity getIdentity(@NonNull final String componentInstanceId) {
    return new Identity(componentInstanceId);
  }

  /**
   * Gets the user associated to the personal component instance.
   *
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
      roles = EnumSet.of(SilverpeasRole.ADMIN);
    } else if (isPublic()) {
      roles = EnumSet.of(SilverpeasRole.USER);
    } else {
      roles = EnumSet.noneOf(SilverpeasRole.class);
    }
    return roles;
  }

  class Identity extends SilverpeasComponentInstance.Identity {

    private static final String INSTANCE_SUFFIX = "_PCI";
    private static final Pattern COMPONENT_INSTANCE_IDENTIFIER =
        Pattern.compile("^([a-zA-Z]+)(\\d+)" + INSTANCE_SUFFIX + "$");

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
            "The argument doesn't represent a personal component instance identifier!");
      }
    }

    /**
     * Gets the unique identifier of the user owning this component instance.
     * @return a user unique identifier.
     */
    public String getUserId() {
      return String.valueOf(getInstanceLocalId());
    }

    @Override
    public String toString() {
      return getComponentName() + getUserId() + INSTANCE_SUFFIX;
    }

    public static boolean isValid(String componentInstanceId) {
      Matcher matcher = COMPONENT_INSTANCE_IDENTIFIER.matcher(componentInstanceId);
      return matcher.matches();
    }
  }
}
