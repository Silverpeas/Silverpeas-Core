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

/**
 * Registering of {@link HttpFunctionHandler} objects so that those objects can then handle the control
 * flow for some functions against the credentials of a user (for example the password reset).
 *
 * @author mmoquillon
 */
public interface HttpFunctionHandlerRegistering {

  /**
   * Register the specified function handler to handle some specific tasks on user credentials. The
   * handler will register itself by specifying the function it will take in charge.
   *
   * @param handler the handler to register.
   * @param bypassPreHandleProcessing is function pre-processing must be bypassed for the function
   * taken in charge by the handler? True if the handler pre-processing has to be bypassed for
   * the given handler. False otherwise.
   */
  void register(HttpFunctionHandler handler, boolean bypassPreHandleProcessing);
}
