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
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public abstract class ChangeCredentialFunctionHandler extends CredentialsFunctionHandler {

  /**
   * Handle bad credential error.
   * @param request the incoming request asking for credential change.
   * @param originalUrl the targeted original URL by the request.
   * @param userCanTryAgainToLoginVerifier verifier of user login attempts.
   * @param messageBundleKey the key of a l10n message to pass in the case of an error.
   * @return destination url the URL to which the request has to be redirected in case of error.
   */
  protected String performUrlOnBadCredentialError(HttpServletRequest request, String originalUrl,
      UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier, String messageBundleKey) {
    try {
      StringBuilder message = new StringBuilder(getMultilang().getString(messageBundleKey));
      String url = userCanTryAgainToLoginVerifier.verify().performRequestUrl(request, originalUrl);
      if (userCanTryAgainToLoginVerifier.isActivated()) {
        message.append("<br>");
        message.append(userCanTryAgainToLoginVerifier.getMessage());
      }
      request.setAttribute("message", message.toString());
      return url;
    } catch (AuthenticationNoMoreUserConnectionAttemptException e) {
      return userCanTryAgainToLoginVerifier.getErrorDestination();
    }
  }
}
