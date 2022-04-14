/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authentication;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

/**
 * The authentication result represents the response of an authentication process while
 * authenticating a given user for Silverpeas.
 * @author mmoquillon
 */
public class AuthenticationResponse {

  private static final String ERROR_PREFIX = "Error_";

  /**
   * Gets the response of a successful authentication by specifying the temporary token that
   * identifies uniquely the user behind the login. With the token, the profile in Silverpeas of the
   * authenticated user can be then get by the user identification process.
   * @param authToken the authentication token. It is used to identify uniquely and temporarily the
   * authenticated user in order to be able to get its profile from a user identity service.
   * @return the {@link AuthenticationResponse} instance for a successful authentication.
   */
  public static AuthenticationResponse succeed(final String authToken) {
    return new AuthenticationResponse(authToken);
  }

  /**
   * Gets the response of a failed or rejected authentication by specifying the error status behind
   * this failure. An authentication failure comes from the attempt by a user to log on Silverpeas
   * with bad credentials or with a user account in an invalid state.
   * @param errorStatus the status of the authentication error that explains the reason of that
   * error.
   * @return the {@link AuthenticationResponse} instance for a failed or rejected authentication.
   */
  public static AuthenticationResponse error(final Status errorStatus) {
    return new AuthenticationResponse(errorStatus);
  }

  private final Status status;
  private final String token;

  AuthenticationResponse(final String token) {
    this.status = Status.SUCCESS;
    this.token = token;
  }

  AuthenticationResponse(final Status error) {
    this.status = error;
    this.token = StringUtil.EMPTY;
  }

  /**
   * Gets the status of the authentication result.
   * @return a {@link Status} instance.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Gets the token that identifies uniquely the authenticated user. If the authentication of the
   * user failed, then nothing is returned.
   * @return either a temporarily token identifying uniquely the authenticated user or an empty
   * string if the authentication has failed.
   */
  public String getToken() {
    return token;
  }

  /**
   * Gets the message in this authentication response. The messages are defined in the bundle
   * <code>org/silverpeas/authentication/multilang/authentication.properties</code> for each
   * authentication status.
   * @param language the ISO 639-1 code of the language in which the message has to be localized.
   * @return the localized message about the authentication result.
   */
  public String getMessage(final String language) {
    return status.getMessage(language);
  }

  /**
   * The status of an authentication process.
   */
  public enum Status {

    /**
     * The authentication has succeeded.
     */
    SUCCESS(""),
    /**
     * The authentication has failed for a bad login identifier or password.
     */
    BAD_LOGIN_PASSWORD(ERROR_PREFIX + "1"),
    /**
     * The authentication has failed for a bad login identifier, password or user domain.
     */
    BAD_LOGIN_PASSWORD_DOMAIN(ERROR_PREFIX + "6"),
    /**
     * The authentication has failed for an unknown reason; the reason of the failure cannot be
     * figured out.
     */
    UNKNOWN_FAILURE(ERROR_PREFIX + "2"),
    /**
     * The authentication has failed because no password has been set for the user account to which
     * the login refers.
     */
    NO_PASSWORD(ERROR_PREFIX + "5"),
    /**
     * The authentication has been rejected for an expired password.
     */
    PASSWORD_EXPIRED(ERROR_PREFIX + "PwdExpired"),
    /**
     * The authentication has been rejected because the password requires to be changed before.
     */
    PASSWORD_TO_CHANGE(ERROR_PREFIX + "PwdMustBeChanged"),
    /**
     * The authentication has been rejected because this is a first login that requires the password
     * of the user to be changed before.
     */
    PASSWORD_TO_CHANGE_ON_FIRST_LOGIN(ERROR_PREFIX + "PwdMustBeChangedOnFirstLogin"),
    /**
     * The authentication has been rejected because this is a first login that requires both the
     * password and the email address of the user to be changed before.
     */
    PASSWORD_EMAIL_TO_CHANGE_ON_FIRST_LOGIN(ERROR_PREFIX + "PwdAndEmailMustBeChangedOnFirstLogin"),
    /**
     * The authentication has failed because the account of the user in Silverpeas has been
     * blocked.
     */
    USER_ACCOUNT_BLOCKED(ERROR_PREFIX + "UserAccountBlocked"),
    /**
     * The authentication has failed because the account of the user in Silverpeas has been
     * deactivated.
     */
    USER_ACCOUNT_DEACTIVATED(ERROR_PREFIX + "UserAccountDeactivated");

    private final String key;

    Status(final String key) {
      this.key = key;
    }

    /**
     * Is the authentication has succeeded?
     * @return true if the authentication succeeded. False otherwise.
     */
    public boolean succeeded() {
      return this == SUCCESS;
    }

    /**
     * Is the authentication is in error?
     * @return true if the authentication has either failed or rejected. False if it has succeeded.
     */
    public boolean isInError() {
      return !succeeded();
    }

    /**
     * Is the authentication has failed?
     * @return true if the user credentials are incorrect or if the user account behind the login is
     * an invalid state (blocked or deactivated for example). False otherwise.
     */
    public boolean failed() {
      return !succeeded() && !rejected();
    }

    /**
     * Is the authentication has been rejected?
     * @return true if the login requires first some actions before (like changing its password for
     * example). False otherwise.
     */
    public boolean rejected() {
      return this == PASSWORD_EXPIRED ||
          this == PASSWORD_EMAIL_TO_CHANGE_ON_FIRST_LOGIN ||
          this == PASSWORD_TO_CHANGE ||
          this == PASSWORD_TO_CHANGE_ON_FIRST_LOGIN;
    }

    /**
     * Gets the localized message associated with this status. The messages are defined in the
     * bundle <code>org/silverpeas/authentication/multilang/authentication.properties</code> for
     * each authentication status.
     * @param language the ISO 639-1 code of the language in which the message has to be localized.
     * @return the localized message associated with the status.
     */
    public String getMessage(final String language) {
      LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.authentication.multilang.authentication", language);
      return messages.getString("authentication.logon." + key);
    }

    /**
     * Gets the error code qualifying this status.
     * @return the error identifier.
     */
    public String getCode() {
      return key;
    }

    /**
     * Gets the error code qualifying this status.
     * @return the error identifier.
     * @see Status#getCode()
     */
    @Override
    public String toString() {
      return getCode();
    }
  }
}
