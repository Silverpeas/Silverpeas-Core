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
package org.silverpeas.core.admin.user.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * The core predefined user roles in Silverpeas. Each role has a name and is attached to a
 * predefined set of privileges. Those privileges are common to all the applications in Silverpeas
 * but they can be extended in the applications themselves. As a reminder, a privilege is the
 * ability to perform a given action and in Silverpeas the privileges are hard-coded.
 */
public enum SilverpeasRole {
  ADMIN("admin"),
  SUPERVISOR("supervisor"),
  MANAGER("Manager"),
  PUBLISHER("publisher"),
  WRITER("writer"),
  PRIVILEGED_USER("privilegedUser"),
  USER("user"),
  READER("reader");

  private final String name;

  // Unfortunately, several codes of role can define the same role nature in Silverpeas ...
  public static final Set<SilverpeasRole> READER_ROLES = EnumSet.of(USER, READER);

  SilverpeasRole(final String name) {
    this.name = name;
  }

  /**
   * Gets the {@link SilverpeasRole} instance that matches the specified role name.
   *
   * @param name the name of a predefined user role in Silverpeas.
   * @return the {@link SilverpeasRole} instance having as name the specified role name or null if
   * no such role exists.
   */
  @JsonCreator
  public static SilverpeasRole from(String name) {
    if (StringUtil.isNotDefined(name)) {
      return null;
    }
    String trimmedName = name.trim();
    return Arrays.stream(values())
        .filter(r -> r.getName().equalsIgnoreCase(trimmedName))
        .findFirst()
        .orElseGet(() -> {
          SilverLogger.getLogger(SilverpeasRole.class).warn("Unknown user role name: {0}", name);
          return null;
        });
  }

  /**
   * Is the specified role name is one of the predefined roles in Silverpeas.
   *
   * @param name the name of a user role.
   * @return true if the role name matches a {@link SilverpeasRole} instance. False otherwise.
   */
  public static boolean exists(String name) {
    return from(name) != null;
  }

  /**
   * Gets for each of the specified roles the matching {@link SilverpeasRole} instance.
   *
   * @param roles a comma-separated array of user role names.
   * @return a set of {@link SilverpeasRole} instance matching each of the roles specified as
   * parameter. If one of the role name doesn't match a {@link SilverpeasRole} instance, then it is
   * skipped.
   */
  public static Set<SilverpeasRole> listFrom(String roles) {
    return from(StringUtil.isDefined(roles) ? StringUtil.split(roles, ",") : null);
  }

  /**
   * Gets for each of the specified roles the matching {@link SilverpeasRole} instance.
   *
   * @param roles an array of user role names.
   * @return a set of {@link SilverpeasRole} instance matching each of the specified role names. If
   * one of the role name doesn't match a {@link SilverpeasRole} instance, then it is skipped.
   */
  public static Set<SilverpeasRole> from(String[] roles) {
    Set<SilverpeasRole> result = EnumSet.noneOf(SilverpeasRole.class);
    if (roles != null) {
      for (String role : roles) {
        SilverpeasRole silverpeasRole = from(role);
        if (silverpeasRole != null) {
          result.add(silverpeasRole);
        }
      }
    }
    return result;
  }

  /**
   * Gets the role name as used in Silverpeas for each of the specified {@link SilverpeasRole}
   * instances.
   *
   * @param roles a set of {@link SilverpeasRole} instances.
   * @return a comma-separated array of role names, each of them matching the specified {@link
   * SilverpeasRole} instances. If the set is null, then null is returned. If the set is empty then
   * an empty {@link String} is returned.
   */
  public static String asString(Set<SilverpeasRole> roles) {
    if (roles == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (SilverpeasRole role : roles) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(role.getName());
    }
    return sb.toString();
  }

  /**
   * Gets the highest role from the given ones.
   *
   * @param roles one or several {@link SilverpeasRole} instances.
   * @return the {@link SilverpeasRole} instance that is the highest privileged among the roles
   * passed as argument.
   */
  public static SilverpeasRole getHighestFrom(SilverpeasRole... roles) {
    return getHighestFrom(Arrays.asList(roles));
  }

  /**
   * Gets the highest role from the given ones.
   *
   * @param roles a collection of {@link SilverpeasRole} instances.
   * @return the {@link SilverpeasRole} instance that is the highest privileged among the roles
   * passed as argument.
   */
  public static SilverpeasRole getHighestFrom(Collection<SilverpeasRole> roles) {
    if (CollectionUtil.isEmpty(roles)) {
      return null;
    }
    @SuppressWarnings("unchecked") EnumSet<SilverpeasRole> givenRoles =
        (roles instanceof EnumSet) ? (EnumSet) roles : EnumSet.copyOf(roles);

    // For now, the highest is the first of the EnumSet
    return givenRoles.iterator().next();
  }

  /**
   * Gets the name of the role as used in Silverpeas.
   *
   * @return the role name.
   */
  @JsonValue
  public String getName() {
    return this.name;
  }

  /**
   * Indicates if a role is greater than an other one.
   *
   * @param role a {@link SilverpeasRole} instance.
   * @return true if this role is greater than the specified one.
   */
  public boolean isGreaterThan(SilverpeasRole role) {
    // For now, ordinal value is used ...
    return ordinal() < role.ordinal();
  }

  /**
   * Indicates if a role is greater than or equals an other one.
   *
   * @param role a {@link SilverpeasRole} instance.
   * @return true if this role is greater or equal than the specified one.
   */
  public boolean isGreaterThanOrEquals(SilverpeasRole role) {
    // For now, ordinal value is used ...
    return ordinal() <= role.ordinal();
  }

  /**
   * Is the specified role names are a predefined {@link SilverpeasRole}.
   *
   * @param roles one or more role names.
   * @return true if at least one of the specified role names is a predefined role as defined in
   * {@link SilverpeasRole}.
   */
  public boolean isInRole(String... roles) {
    try {
      for (String aRole : roles) {
        if (this == from(aRole)) {
          return true;
        }
      }
    } catch (IllegalArgumentException ex) {
      SilverLogger.getLogger(SilverpeasRole.class).warn(ex);
    }
    return false;
  }

  /**
   * Same as {@link SilverpeasRole#getName()}
   *
   * @return the role name of this {@link SilverpeasRole} instance.
   */
  @Override
  public String toString() {
    return getName();
  }
}
