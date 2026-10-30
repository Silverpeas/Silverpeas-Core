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
package org.silverpeas.core.web.rs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import java.util.Optional;
import java.util.Objects;

/**
 * A pending REST second-factor challenge. Session attribute names and retry rules are internal
 * to this class; the challenge never represents an authenticated Silverpeas session.
 */
public final class RestTwoFactorChallenge {
  private static final String PREFIX = "org.silverpeas.core.web.rs.HTTPAuthentication.";
  private static final String REQUIRED = PREFIX + "twoFactorRequired";
  private static final String LOGIN = PREFIX + "twoFactorLogin";
  private static final String DOMAIN = PREFIX + "twoFactorDomain";
  private static final String EXPIRES_AT = PREFIX + "twoFactorExpiresAt";
  private static final String ATTEMPTS = PREFIX + "twoFactorAttempts";
  private static final SettingBundle SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  private final HttpSession session;

  private RestTwoFactorChallenge(final HttpSession session) {
    this.session = Objects.requireNonNull(session, "A challenge requires an HTTP session");
  }

  static void start(final HttpServletRequest request, final String login, final String domainId) {
    HttpSession session = request.getSession(true);
    RestTwoFactorChallenge challenge = new RestTwoFactorChallenge(session);
    if (!challenge.isValid() || !login.equals(challenge.getLogin()) ||
        !domainId.equals(challenge.getDomainId())) {
      session.setAttribute(LOGIN, login);
      session.setAttribute(DOMAIN, domainId);
      session.setAttribute(EXPIRES_AT, System.currentTimeMillis() +
          Math.max(1, SETTINGS.getInteger("twoFactorTotpChallengeLifetime", 120)) * 1000L);
      session.setAttribute(ATTEMPTS, 0);
    }
    request.setAttribute(REQUIRED, Boolean.TRUE);
  }

  /**
   * Gets a valid pending challenge without creating an HTTP session.
   * Expired challenges and challenges with exhausted attempts are cleared.
   * @param request the current request.
   * @return the challenge, if the first factor has been validated and the challenge is valid.
   */
  public static Optional<RestTwoFactorChallenge> from(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return Optional.empty();
    }
    RestTwoFactorChallenge challenge = new RestTwoFactorChallenge(session);
    if (!challenge.isValid()) {
      challenge.clear();
      return Optional.empty();
    }
    return Optional.of(challenge);
  }

  /**
   * Indicates whether password validation requested a second factor for the current request.
   * @param request the request whose first factor has just been validated.
   * @return true if a second factor is required.
   */
  public static boolean isRequired(final HttpServletRequest request) {
    return Boolean.TRUE.equals(request.getAttribute(REQUIRED));
  }

  /**
   * Gets the login validated during the first authentication factor.
   * @return the pending user's login.
   */
  public String getLogin() {
    return (String) session.getAttribute(LOGIN);
  }

  /**
   * Gets the domain validated during the first authentication factor.
   * @return the pending user's domain identifier.
   */
  public String getDomainId() {
    return (String) session.getAttribute(DOMAIN);
  }

  /**
   * Records a failed second factor and clears the challenge when its limit is reached.
   */
  public void recordFailure() {
    int attempts = getAttempts() + 1;
    session.setAttribute(ATTEMPTS, attempts);
    if (attempts >= maxAttempts()) {
      clear();
    }
  }

  /**
   * Clears the pending challenge after authentication, expiry or lockout.
   */
  public void clear() {
    session.removeAttribute(LOGIN);
    session.removeAttribute(DOMAIN);
    session.removeAttribute(EXPIRES_AT);
    session.removeAttribute(ATTEMPTS);
    session.removeAttribute(REQUIRED);
  }

  private boolean isValid() {
    Object expiresAt = session.getAttribute(EXPIRES_AT);
    return StringUtil.isDefined(getLogin()) && StringUtil.isDefined(getDomainId()) &&
        expiresAt instanceof Long expiration && expiration >= System.currentTimeMillis() &&
        getAttempts() < maxAttempts();
  }

  private int getAttempts() {
    Object attempts = session.getAttribute(ATTEMPTS);
    return attempts instanceof Integer count ? count : 0;
  }

  private int maxAttempts() {
    return Math.max(1, SETTINGS.getInteger("twoFactorTotpMaxAttempts", 5));
  }
}
