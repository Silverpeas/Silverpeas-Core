/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.web.authentication.credentials;

import javax.servlet.http.HttpServletRequest;

/**
 * An handler of a function to perform against the credentials of a user.
 *
 * @author mmoquillon
 */
public interface HttpFunctionHandler {

  /**
   * The name of the function this handler will take in charge.
   * @return the name of a function against the user credentials.
   */
  String getFunction();

  /**
   * Performs the action.
   *
   * @param request the incoming HTTP request.
   * @return the URL at which the control flow has to be passed next the action.
   */
  String doAction(HttpServletRequest request);

  /**
   * Registers itself by using the specified registering service.
   *
   * @param registering the registering service among which this handler has to be register itself.
   */
  void registerWith(HttpFunctionHandlerRegistering registering);
}
  