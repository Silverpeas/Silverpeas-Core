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
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesServiceProvider;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
public abstract class ChangePasswordFunctionHandler extends ChangeCredentialFunctionHandler {

  /**
   * Handle bad credential error.
   * @param request the incoming request.
   * @param originalUrl the original URL targeted by the request.
   * @param credential the credentials of the requester.
   * @return destination url the new URL at which redirect the user.
   */
  protected String performUrlChangePasswordError(HttpServletRequest request, String originalUrl,
      AuthenticationCredential credential) {
    return performUrlOnBadCredentialError(request, originalUrl,
        AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(credential),
        "badCredentials");
  }

  /**
   * Handle bad credential error.
   * @param request the incoming request.
   * @param originalUrl the original URL targeted by the request.
   * @param user the requester.
   * @return destination url the new URL at which redirect the user.
   */
  protected String performUrlChangePasswordError(HttpServletRequest request, String originalUrl,
      UserDetail user) {
    return performUrlOnBadCredentialError(request, originalUrl,
        AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(user),
        "badCredentials");
  }

  /**
   * Asserts the specified password has been checked against the password rules and the checking
   * has been successful.
   * @param checkId the unique identifier of a password check process.
   * @param password the password to assert.
   * @throws AuthenticationException if the password hasn't been successfully checked by the
   * specidfed ckeck process.
   */
  protected void assertPasswordHasBeenCorrectlyChecked(String checkId,String password)
      throws AuthenticationException {
    var passwordRuleService = PasswordRulesServiceProvider.getPasswordRulesService();
    if (!passwordRuleService.isChecked(checkId, password)) {
      throw new AuthenticationException("Password wasn't checked against the password rules!");
    }
  }
}
