/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.security.authentication;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.initialization.Initialization;

import javax.servlet.http.HttpServletRequest;
import java.util.EventListener;

/**
 * This event listener is fired when a user has just been logged in.<br/>
 * Each implementation of this listener has to be registered by calling {@link
 * UserAuthenticationListenerRegistration#register(UserAuthenticationListener)}.<br/>
 * A tip to perform the registration is to implement {@link Initialization} interface. Indeed, each
 * class, that implements it, is called one time at server start.<br/>
 * (See QuickInfoUserAuthenticationListener for example)
 */
public interface UserAuthenticationListener extends EventListener {

  /**
   * This method is called just before redirecting the user to the home page, after a successful
   * authentication.<br/>
   * If it is necessary, the redirection can be overridden...
   * @param request the current user request.
   * @param user the current user.
   * @param finalURL the initial URL of user redirection, just after a successful authentication.
   * @return the overridden url redirection, or null if no override.
   */
  String firstHomepageAccessAfterAuthentication(HttpServletRequest request, UserDetail user,
      String finalURL);
}