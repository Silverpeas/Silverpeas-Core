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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * The changes of a user to write into the LDAP directory: the data of the user that differ
 * between the user as it is in the directory and the user as it is updated in Silverpeas. Only the
 * properties that are updatable, either by the user or by an administrator, are taken into
 * account. An undefined value and an empty one are considered as equal.
 * @author mmoquillon
 */
class LDAPUserChanges {

  private final boolean firstNameChanged;
  private final boolean lastNameChanged;
  private final boolean emailAddressChanged;
  private final List<DomainProperty> changedProperties = new ArrayList<>();

  /**
   * Computes the changes between the specified user as it is in the LDAP directory and as it is
   * updated.
   * @param userInDirectory the user as it is currently in the LDAP directory.
   * @param updatedUser the user with the updated data.
   */
  LDAPUserChanges(final UserFull userInDirectory, final UserFull updatedUser) {
    firstNameChanged = isChanged(userInDirectory, updatedUser, UserFull::getFirstName);
    lastNameChanged = isChanged(userInDirectory, updatedUser, UserFull::getLastName);
    emailAddressChanged = isChanged(userInDirectory, updatedUser, UserFull::getEmailAddress);
    for (String propertyName : updatedUser.getPropertiesNames()) {
      DomainProperty property = updatedUser.getProperty(propertyName);
      if (property != null &&
          (property.isUpdateAllowedToAdmin() || property.isUpdateAllowedToUser()) &&
          isChanged(userInDirectory, updatedUser, u -> u.getValue(propertyName))) {
        changedProperties.add(property);
      }
    }
  }

  boolean isFirstNameChanged() {
    return firstNameChanged;
  }

  boolean isLastNameChanged() {
    return lastNameChanged;
  }

  boolean isEmailAddressChanged() {
    return emailAddressChanged;
  }

  /**
   * Gets the updatable properties whose the value has changed.
   * @return a list of domain properties. Empty if no updatable properties have changed.
   */
  List<DomainProperty> getChangedProperties() {
    return List.copyOf(changedProperties);
  }

  /**
   * Is there no changes to write into the LDAP directory?
   * @return true if none of the data of the user have changed, false otherwise.
   */
  boolean isEmpty() {
    return !firstNameChanged && !lastNameChanged && !emailAddressChanged &&
        changedProperties.isEmpty();
  }

  private static boolean isChanged(final UserFull userInDirectory, final UserFull updatedUser,
      final Function<UserFull, String> data) {
    return !Objects.equals(defaultStringIfNotDefined(data.apply(userInDirectory)),
        defaultStringIfNotDefined(data.apply(updatedUser)));
  }
}
