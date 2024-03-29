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
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserMustAcceptTermsOfService;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Class that provides tools to verify if the user must accept terms of service.
 * @author  Yohann Chastagnier
 * Date: 10/09/13
 */
public class UserMustAcceptTermsOfServiceVerifier extends AbstractAuthenticationVerifier {

  public static final String ERROR_USER_TOS_REFUSED = "Error_UserTosRefused";
  public static final String ERROR_USER_TOS_TIMEOUT = "Error_UserTosTimeout";

  private static final TermsOfServiceAcceptanceFrequency globalAcceptanceFrequency;

  // In seconds, 10 minutes (60seconds x 10minutes)
  private static final int LIVE_10_MINUTES = 60 * 10;

  private static final Cache applicationCache =
      CacheAccessorProvider.getApplicationCacheAccessor().getCache();

  static {
    globalAcceptanceFrequency = TermsOfServiceAcceptanceFrequency
        .decode(settings.getString("termsOfServiceAcceptanceFrequency"));
  }

  private String tosToken;

  /**
   * Default constructor.
   * @param user the user behind a login.
   */
  protected UserMustAcceptTermsOfServiceVerifier(final User user) {
    super(user);
  }

  /**
   * Gets the destination.
   * @return the relative URL of the web page displaying the terms of service once the user
   * authenticated.
   */
  public String getDestination(HttpServletRequest request) {
    request.setAttribute("tosToken", tosToken);
    request.setAttribute("language", getUser().getUserPreferences().getLanguage());
    // Check if specific template content has to be used
    boolean specificTemplateContentActivated = settings.getBoolean(
        "termsOfServiceAcceptanceSpecificTemplateContent." + "domain" + getUser().getDomainId(),
        false);
    if (specificTemplateContentActivated) {
      request.setAttribute("templateDomainIdContent", "_domain" + getUser().getDomainId());
    }
    return "/CredentialsServlet/TermsOfServiceRequest";
  }

  /**
   * Verify user connection attempts and block user account if necessary.
   */
  public UserMustAcceptTermsOfServiceVerifier verify()
      throws AuthenticationUserMustAcceptTermsOfService {
    if (isTermsOfServiceAcceptanceDateIsExpired()) {
      // Caching for 10 minutes
      tosToken = applicationCache.add(this, LIVE_10_MINUTES);
      throw new AuthenticationUserMustAcceptTermsOfService(
          "The user must accept the terms of service");
    }
    return this;
  }

  /**
   * Indicates if the user must accept terms of service.
   * (anonymous is ignored)
   * If the system is not activated (see file settings), this method answers always no.
   * If the system is activated, this method answers yes user must accept terms of service.
   * @return true if the user must accept terms of service, false otherwise.
   */
  private synchronized boolean isTermsOfServiceAcceptanceDateIsExpired() {
    if (getUser() == null || getUser().isAnonymous()) {
      return false;
    }

    String specificAcceptanceFrequencyValue = settings
        .getString("termsOfServiceAcceptanceFrequency." + "domain" + getUser().getDomainId(), null);
    // Check if domain specific acceptance frequency is specified
    final TermsOfServiceAcceptanceFrequency acceptanceFrequency;
    if (StringUtil.isDefined(specificAcceptanceFrequencyValue)) {
      // If it's specified, we use it
      acceptanceFrequency =
          TermsOfServiceAcceptanceFrequency.decode(specificAcceptanceFrequencyValue);
    } else {
      // If not, we use the global acceptance frequency
      acceptanceFrequency = globalAcceptanceFrequency;
    }

    UserDetail user = (UserDetail) getUser();
    return acceptanceFrequency.isActivated() && acceptanceFrequency
        .isAcceptanceDateExpired(user.getTosAcceptanceDate());
  }

  /**
   * Clearing the cache associated to the verifier.
   */
  public UserMustAcceptTermsOfServiceVerifier clearCache() {
    clearCache(tosToken);
    return this;
  }

  /**
   * Gets the verifier with the given token.
   * @param tosToken token mapped with a {@link UserMustAcceptTermsOfServiceVerifier} instance.
   * @return a {@link UserMustAcceptTermsOfServiceVerifier} object.
   */
  protected static synchronized UserMustAcceptTermsOfServiceVerifier get(String tosToken) {
    UserMustAcceptTermsOfServiceVerifier verifier = applicationCache
        .get(tosToken, UserMustAcceptTermsOfServiceVerifier.class);
    if (verifier == null) {
      verifier = new UserMustAcceptTermsOfServiceVerifier(null);
    }
    return verifier;
  }

  /**
   * Clear cache of the verifier.
   * @param tosToken token identifying in the cache a {@link UserMustAcceptTermsOfServiceVerifier}
   * instance.
   */
  private static synchronized void clearCache(String tosToken) {
    if (tosToken != null) {
      applicationCache.remove(tosToken);
    }
  }
}
