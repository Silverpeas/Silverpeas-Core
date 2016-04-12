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
package org.silverpeas.core.web.authentication.credentials;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;

/**
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public abstract class ChangeCredentialFunctionHandler extends FunctionHandler {

  /**
   * Handle bad credential error.
   * @param request
   * @param originalUrl
   * @param userCanTryAgainToLoginVerifier
   * @param messageBundleKey
   * @return destination url
   */
  protected String performUrlOnBadCredentialError(HttpServletRequest request, String originalUrl,
      UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier, String messageBundleKey) {
    try {
      StringBuilder message = new StringBuilder(getM_Multilang().getString(messageBundleKey));
      String url = userCanTryAgainToLoginVerifier.verify().performRequestUrl(request, originalUrl);
      if (userCanTryAgainToLoginVerifier.isActivated()) {
        message.append("<br/>");
        message.append(userCanTryAgainToLoginVerifier.getMessage());
      }
      request.setAttribute("message", message.toString());
      return url;
    } catch (AuthenticationNoMoreUserConnectionAttemptException e) {
      return userCanTryAgainToLoginVerifier.getErrorDestination();
    }
  }
}
