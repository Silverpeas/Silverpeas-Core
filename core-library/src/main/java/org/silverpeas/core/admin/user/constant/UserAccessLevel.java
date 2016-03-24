/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User: Yohann Chastagnier Date: 18/01/13
 */
public enum UserAccessLevel {

  UNKNOWN(""),
  ADMINISTRATOR("A"),
  DOMAIN_ADMINISTRATOR("D"),
  SPACE_ADMINISTRATOR("S"),
  USER("U"),
  GUEST("G"),
  PDC_MANAGER("K");

  private final String code;

  private UserAccessLevel(String code) {
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
