/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum SilverpeasRole {
  admin, Manager, publisher, writer, user, reader, supervisor, privilegedUser;

  // Unfortunately, several codes of role can define the same role nature in Silverpeas ...
  public static EnumSet<SilverpeasRole> READER_ROLES = EnumSet.of(user, reader);

  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * Indicates if a role is greater than an other one.
   * @param role
   * @return
   */
  public boolean isGreaterThan(SilverpeasRole role) {
    // For now, ordinal value is used ...
    return ordinal() < role.ordinal();
  }

  /**
   * Indicates if a role is greater than or equals an other one.
   * @param role
   * @return
   */
  public boolean isGreaterThanOrEquals(SilverpeasRole role) {
    // For now, ordinal value is used ...
    return ordinal() <= role.ordinal();
  }

  @JsonCreator
  public static SilverpeasRole from(String name) {
    if (name == null) {
      return null;
    }
    String trimmedName = name.trim();
    try {
      return valueOf(trimmedName);
    } catch (Exception e) {
      // Safe mode method (but less efficient)
      for (SilverpeasRole role : values()) {
        if (role.getName().equalsIgnoreCase(trimmedName)) {
          return role;
        }
      }
      return null;
    }
  }

  public static boolean exists(String name) {
    return from(name) != null;
  }

  /**
   * Lists the roles from a string. (Each one separated by a comma)
   * @param roles
   * @return
   */
  public static Set<SilverpeasRole> listFrom(String roles) {
    return from(StringUtil.isDefined(roles) ? StringUtil.split(roles, ",") : null);
  }

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

  public boolean isInRole(String... roles) {
    try {
      for (String aRole : roles) {
        if (this == from(aRole)) {
          return true;
        }
      }
    } catch (IllegalArgumentException ex) {
      return false;
    }
    return false;
  }

  /**
   * Gets on or several roles as a string.
   * They are separated between them by comma.
   * @param roles
   * @return
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
      sb.append(role.name());
    }
    return sb.toString();
  }

  /**
   * Gets the greater role from the given ones.
   * @param roles
   * @return
   */
  public static SilverpeasRole getGreaterFrom(SilverpeasRole... roles) {
    return getGreaterFrom(Arrays.asList(roles));
  }

  /**
   * Gets the greater role from the given ones.
   * @param roles
   * @return
   */
  public static SilverpeasRole getGreaterFrom(Collection<SilverpeasRole> roles) {
    if (CollectionUtil.isEmpty(roles)) {
      return null;
    }
    @SuppressWarnings("unchecked") EnumSet<SilverpeasRole> givenRoles =
        (roles instanceof EnumSet) ? (EnumSet) roles : EnumSet.copyOf(roles);

    // For now, the greater it the fisrt of the EnumSet
    return givenRoles.iterator().next();
  }
}
