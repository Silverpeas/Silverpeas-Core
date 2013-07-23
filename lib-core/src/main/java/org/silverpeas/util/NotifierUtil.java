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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.util;

import com.silverpeas.util.StringUtil;

import javax.servlet.ServletRequest;

/**
 * This utility class provides tools to display easily some dynamic notifications using the
 * notifier
 * plugin.
 * User: Yohann Chastagnier
 * Date: 23/07/13
 */
public class NotifierUtil {

  /**
   * Add an error message to the request. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param request
   * @param message
   * @return the complete message
   */
  public static String addError(ServletRequest request, String message) {
    return addMessage(request, "notyErrorMessage", message);
  }

  /**
   * Add a success message to the request. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param request
   * @param message
   * @return the complete message
   */
  public static String addSuccess(ServletRequest request, String message) {
    return addMessage(request, "notySuccessMessage", message);
  }

  /**
   * Add an info message to the request. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param request
   * @param message
   * @return the complete message
   */
  public static String addInfo(ServletRequest request, String message) {
    return addMessage(request, "notyInfoMessage", message);
  }

  /**
   * Centralization
   * @param request
   * @param messageType
   * @param message
   * @return the complete message
   */
  private static String addMessage(ServletRequest request, String messageType, String message) {
    String existingMessage = (String) request.getAttribute(messageType);
    if (StringUtil.isNotDefined(existingMessage)) {
      existingMessage = "";
    }
    if (StringUtil.isDefined(message)) {
      existingMessage += (existingMessage.isEmpty() ? "" : "<br/>") + message;
      request.setAttribute(messageType, existingMessage);
    }
    return existingMessage;
  }
}
