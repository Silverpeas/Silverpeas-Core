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
 * The state of the user account in Silverpeas.
 * @author Yohann Chastagnier
 */
public enum UserState {
  /**
   * The state is unknown. It is not defined.
   */
  UNKNOWN,

  /**
   * The user account is a valid and the user can sign in Silverpeas.
   */
  VALID,

  /**
   * The user account is blocked and the user cannot sign in Silverpeas.
   */
  BLOCKED,

  /**
   * The user account is temporally deactivated.
   */
  DEACTIVATED,

  /**
   * The user account is expired.
   */
  EXPIRED,

  /**
   * The user account is removed.
   */
  REMOVED,

  /**
   * The user account is deleted.
   */
  DELETED;

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static UserState from(String name) {
    if (name != null) {
      for (UserState userAccountStatus : UserState.values()) {
        if (name.equals(userAccountStatus.name())) {
          return userAccountStatus;
        }
      }
    }
    return UNKNOWN;
  }
}
