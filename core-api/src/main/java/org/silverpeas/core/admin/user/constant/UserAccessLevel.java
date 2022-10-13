/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.user.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The access level of a user account in Silverpeas.
 * @author  Yohann Chastagnier
 */
public enum UserAccessLevel {

  /**
   * Unknown. The access level isn't defined.
   */
  UNKNOWN(""),

  /**
   * The user is an administrator.
   */
  ADMINISTRATOR("A"),

  /**
   * The user is an administrator of domains.
   */
  DOMAIN_ADMINISTRATOR("D"),

  /**
   * The user is an administrator of spaces.
   */
  SPACE_ADMINISTRATOR("S"),

  /**
   * The user is a basic Silverpeas user.
   */
  USER("U"),

  /**
   * The user is guest in the platform. Its rights of access are strongly limited to what is public.
   */
  GUEST("G"),

  /**
   * The user is a manager of the PdC.
   */
  PDC_MANAGER("K");

  private final String code;

  UserAccessLevel(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public String getCode() {
    return code();
  }

  @JsonValue
  public String getName() {
    return name();
  }

  public static UserAccessLevel fromCode(String code) {
    if (code != null) {
      for (UserAccessLevel userLevelAccess : UserAccessLevel.values()) {
        if (code.equals(userLevelAccess.code)) {
          return userLevelAccess;
        }
      }
    }
    return UNKNOWN;
  }

  @JsonCreator
  public static UserAccessLevel from(String name) {
    if (name != null) {
      for (UserAccessLevel userAccessLevel : UserAccessLevel.values()) {
        if (name.equals(userAccessLevel.name())) {
          return userAccessLevel;
        }
      }
    }
    return UNKNOWN;
  }
}
