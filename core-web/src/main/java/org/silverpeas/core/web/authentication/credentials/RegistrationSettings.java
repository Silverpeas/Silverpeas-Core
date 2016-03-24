/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

/**
 * A wrapper of the settings on the registration of a new user.
 *
 * @author mmoquillon
 */
public class RegistrationSettings {

  private static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  private static String SELF_AUTHENTICATION_ACTIVATION = "newRegistrationEnabled";
  private static String SELF_AUTHENTICATION_DOMAINID = "justRegisteredDomainId";
  private static long PURGE_PERIOD = 10;
  private static final RegistrationSettings instance = new RegistrationSettings();

  public static RegistrationSettings getSettings() {
    return instance;
  }

  /**
   * Is the self registration capability is enabled? With this functionality, a user can register
   * himself either by filling directly a registration form or from its social account (twitter,
   * ...)
   *
   * @return true if a user can create an account in Silverpeas. False otherwise.
   */
  public boolean isUserSelfRegistrationEnabled() {
    return settings.getBoolean(SELF_AUTHENTICATION_ACTIVATION, false);
  }

  /**
   * In case of self registration, define domain id where the account will be created
   *
   * @return specified domain id. "0" otherwise.
   */
  public String userSelfRegistrationDomainId() {
    return settings.getString(SELF_AUTHENTICATION_DOMAINID, "0");
  }
}
