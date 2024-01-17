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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public abstract class ChangeQuestionAnswerFunctionHandler extends ChangeCredentialFunctionHandler {

  /**
   * Handle bad credential error.
   * @param request
   * @param originalUrl
   * @param user
   * @return destination url
   */
  protected String performQuestionAnswerError(HttpServletRequest request, String originalUrl,
      UserDetail user) {
    return performUrlOnBadCredentialError(request, originalUrl,
        AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(user), "invalidAnswer");
  }
}
