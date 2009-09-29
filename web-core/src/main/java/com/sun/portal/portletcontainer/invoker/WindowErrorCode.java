/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.invoker;

import com.sun.portal.container.ErrorCode;

/**
 * WindowErrorCode that must have localized message strings corresponding to
 * each.
 **/
public class WindowErrorCode extends ErrorCode {

  // 1
  public static final WindowErrorCode GENERIC_ERROR = new WindowErrorCode(
      "GENERIC_ERROR");

  // 2
  public static final WindowErrorCode CONTENT_EXCEPTION = new WindowErrorCode(
      "CONTENT_EXCEPTION");

  // 3
  public static final WindowErrorCode INVALID_WINDOW_STATE_CHANGE_REQUEST = new WindowErrorCode(
      "INVALID_WINDOW_STATE_CHANGE_REQUEST");

  // 4
  public static final WindowErrorCode INVALID_MODE_CHANGE_REQUEST = new WindowErrorCode(
      "INVALID_MODE_CHANGE_REQUEST");

  // 5
  public static final WindowErrorCode CONTAINER_EXCEPTION = new WindowErrorCode(
      "CONTAINER_EXCEPTION");

  public WindowErrorCode(String errorCodeKey) {
    super(errorCodeKey);
  }

}
