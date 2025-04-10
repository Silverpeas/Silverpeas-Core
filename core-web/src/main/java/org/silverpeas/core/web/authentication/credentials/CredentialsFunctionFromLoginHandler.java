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

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.AuthDomain;
import org.silverpeas.core.security.authentication.Authentication;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Handler of operations against the user credentials invoked directly from the login page.
 *
 * @author mmoquillon
 */
public abstract class CredentialsFunctionFromLoginHandler extends CredentialsFunctionHandler {

  @Inject
  private Authentication authenticator;

  /**
   * Registers itself by using the specified registering service and by indicating the
   * pre-processing tasks have to be skipped for this handler.
   *
   * @param registering the registering service among which this handler has to be register itself.
   */
  @Override
  public void registerWith(HttpFunctionHandlerRegistering registering) {
    registering.register(this, true);
  }

  /**
   * Fetch from the specified HTTP request the user login data allowing to identifying him.
   * @param request the incoming HTTP request from a login page for Silverpeas.
   * @return either a {@link ValidLoginData} instance if the user can be identified or an
   * {@link InvalidLoginData} instance otherwise.
   */
  protected LoginData fetchLoginData(HttpServletRequest request) {
    String loginId = request.getParameter("Login");
    String domainId = request.getParameter("DomainId");

    AuthDomain domain = null;
    try {
      domain = authenticator.getAllAuthDomains().stream()
          .filter(d -> d.getId().equals(domainId))
          .findFirst()
          .orElseThrow();
      User user = Optional.ofNullable(
              getAdminService().getUserIdByLoginAndDomain(loginId, domainId))
          .map(User::getById)
          .orElseThrow();
      return new ValidLoginData(user, domain);
    } catch (NoSuchElementException | AdminException e) {
      return new InvalidLoginData(loginId, domain);
    }
  }

  protected Authentication getAuthenticator() {
    return authenticator;
  }

  /**
   * Interface defining the data in a user login attempt.
   */
  protected interface LoginData {

    /**
     * The name of the user domain set in the login form.
     * @return the name of an existing user domain or an empty string if the domain passed in the
     * login form doesn't exist or wasn't set.
     */
    String getDomainName();

    /**
     * The login identifier set in the login form.
     * @return the login identifier.
     */
    String getLoginId();

    /**
     * Is the login data are invalid? It's invalid if the data in the login form cannot able to
     * identify the user.
     * @return true if this login data are invalid. False otherwise.
     */
    boolean isInvalid();

  }

  /**
   * Valid login data. The user and the user domain are well identified and have been retrieved.
   */
  protected static class ValidLoginData implements LoginData {
    private final User user;
    private final AuthDomain domain;

    public ValidLoginData(User user, AuthDomain domain) {
      this.user = user;
      this.domain = domain;
    }

    /**
     * Gets the user which has been identified within the login form.
     * @return a user in Silverpeas
     */
    public User getUser() {
      return user;
    }

    /**
     * Gets the user domain which has been identified within the login form.
     * @return the authentication domain (a user domain with an authentication controller).
     */
    public AuthDomain getDomain() {
      return domain;
    }

    @Override
    public String getDomainName() {
      return domain.getName();
    }

    @Override
    public String getLoginId() {
      return user.getLogin();
    }

    @Override
    public boolean isInvalid() {
      return false;
    }

  }

  /**
   * Invalid login data. The data set in the login form don't abel to identify the user or the
   * domain in Silverpeas.
   */
  protected static class InvalidLoginData implements LoginData {

    private final String loginId;
    private final String domainName;

    public InvalidLoginData(String loginId, AuthDomain domain) {
      this.loginId = loginId;
      this.domainName = domain == null ? "" : domain.getName();
    }

    @Override
    public String getDomainName() {
      return domainName;
    }

    @Override
    public String getLoginId() {
      return loginId;
    }

    @Override
    public boolean isInvalid() {
      return true;
    }
  }
}
  