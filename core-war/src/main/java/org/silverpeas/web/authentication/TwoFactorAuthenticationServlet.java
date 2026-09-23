/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.authentication;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.totp.TotpService;

import java.io.IOException;

/**
 * Web endpoint used by an authenticated user to enroll or confirm native two-factor
 * authentication.
 */
public class TwoFactorAuthenticationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  private TwoFactorAuthenticationService twoFactorAuthenticationService;

  @Inject
  private TotpService totpService;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    final User user = User.getCurrentRequester();
    if (user == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    final int userId = Integer.parseInt(user.getId());
    final TwoFactorAuthentication authentication =
        twoFactorAuthenticationService.startEnrollment(userId);

    request.setAttribute("twoFactorAuthentication", authentication);
    request.setAttribute("otpAuthUri",
        totpService.buildOtpAuthUri(authentication.getSecret()));
    request.getRequestDispatcher("/twoFactorAuthenticationSetup.jsp").forward(request, response);
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    final User user = User.getCurrentRequester();
    if (user == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    final int userId = Integer.parseInt(user.getId());
    final String code = request.getParameter("code");
    if (twoFactorAuthenticationService.confirmEnrollment(userId, code)) {
      response.sendRedirect(request.getContextPath() + "/TwoFactorAuthentication");
      return;
    }

    final TwoFactorAuthentication authentication =
        twoFactorAuthenticationService.getAuthentication(userId).orElse(null);
    if (authentication == null || authentication.isEnabled()) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
      return;
    }

    request.setAttribute("twoFactorAuthentication", authentication);
    request.setAttribute("otpAuthUri",
        totpService.buildOtpAuthUri(authentication.getSecret()));
    request.setAttribute("twoFactorError", true);
    request.getRequestDispatcher("/twoFactorAuthenticationSetup.jsp").forward(request, response);
  }
}
