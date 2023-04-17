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

package org.silverpeas.core.security.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.SettingBundleStub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class AuthenticationCredentialTest {

  private static final String DOMAIN_ID = "26";
  private static final String OTHER_DOMAIN_ID = "38";
  private static final String FALSE = "false";
  private static final String TRUE = "true";
  private static final String DEFAULT_PARAM = "loginIgnoreCaseOnUserAuthentication.default";
  private static final String DOMAIN_SPECIFIC_PARAM_PREFIX = "loginIgnoreCaseOnUserAuthentication.domain";

  @RegisterExtension
  static SettingBundleStub authenticationSettings =
      new SettingBundleStub("org.silverpeas.authentication.settings.authenticationSettings");

  @BeforeEach
  void setupSettings() {
    authenticationSettings.removeAll();
  }

  @Test
  @DisplayName("When no setting exists, the login case is taken into account on user authentication")
  void loginCaseBehaviorWhenNoSetting() throws AuthenticationException {
    assertThat(getCredentials().loginIgnoreCase(), is(false));
  }

  @Test
  @DisplayName("When default setting exists, it is taken into account")
  void loginCaseBehaviorWhenDefaultOneExists() throws AuthenticationException {
    authenticationSettings.put(DEFAULT_PARAM, FALSE);
    assertThat(getCredentials().loginIgnoreCase(), is(false));
    authenticationSettings.put(DEFAULT_PARAM, TRUE);
    assertThat(getCredentials().loginIgnoreCase(), is(true));
  }

  @Test
  @DisplayName("When specific domain setting exists, it is taken into account")
  void loginCaseBehaviorWhenDomainSpecificOneExists() throws AuthenticationException {
    authenticationSettings.put(DOMAIN_SPECIFIC_PARAM_PREFIX + OTHER_DOMAIN_ID, FALSE);
    assertThat(getCredentials().loginIgnoreCase(), is(false));
    authenticationSettings.put(DOMAIN_SPECIFIC_PARAM_PREFIX + DOMAIN_ID, TRUE);
    assertThat(getCredentials().loginIgnoreCase(), is(true));
  }

  @Test
  @DisplayName("When specific domain and default settings exist, specific domain is taken into account")
  void loginCaseBehaviorWhenDomainSpecificAndDefaultOnesExist() throws AuthenticationException {
    authenticationSettings.put(DEFAULT_PARAM, TRUE);
    assertThat(getCredentials().loginIgnoreCase(), is(true));
    authenticationSettings.put(DOMAIN_SPECIFIC_PARAM_PREFIX + DOMAIN_ID, FALSE);
    assertThat(getCredentials().loginIgnoreCase(), is(false));
  }

  private AuthenticationCredential getCredentials() throws AuthenticationException {
    return AuthenticationCredential.newWithAsLogin("loginTest").withAsDomainId(DOMAIN_ID);
  }
}