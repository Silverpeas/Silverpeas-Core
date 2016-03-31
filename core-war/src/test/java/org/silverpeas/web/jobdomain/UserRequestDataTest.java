/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRequestDataTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private SettingBundle mockedSettings;
  private HttpServletRequest httpServletRequestMock;
  private HttpRequest httpRequest;

  @Before
  public void setup() {
    reflectionRule.setField(DisplayI18NHelper.class, asList("fr", "en", "de"), "languages");
    reflectionRule.setField(DisplayI18NHelper.class, "en", "defaultLanguage");
    mockedSettings = reflectionRule.mockField(NotificationManagerSettings.class,
        SettingBundle.class, "settings");
    httpServletRequestMock = mock(HttpServletRequest.class);
    when(httpServletRequestMock.getMethod()).thenReturn("GET");

    setHttpParameter("Iduser", "user.id");
    setHttpParameter("userLogin", "user.login");
    setHttpParameter("userLastName", "user.lastName");
    setHttpParameter("userFirstName", "user.firstName");
    setHttpParameter("userEMail", "user.email");
    setHttpParameter("userAccessLevel", "GUEST");
    setHttpParameter("userPasswordValid", "true");
    setHttpParameter("userPassword", "user.password");
    setHttpParameter("sendEmail", "true");
    setHttpParameter("GroupId", "user.groupId");
    setHttpParameter("UserLanguage", "user.language");
    setHttpParameter("userManualNotifReceiverLimitEnabled", "true");
    setHttpParameter("userManualNotifReceiverLimitValue", "10");

    httpRequest = HttpRequest.decorate(httpServletRequestMock);
  }

  @Test
  public void verifyParameterWiring() {
    setHttpParameter("sendEmail", "false");
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");
    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    assertThat(userRequestData.getId(), is("user.id"));
    assertThat(userRequestData.getLogin(), is("user.login"));
    assertThat(userRequestData.getLastName(), is("user.lastName"));
    assertThat(userRequestData.getFirstName(), is("user.firstName"));
    assertThat(userRequestData.getEmail(), is("user.email"));
    assertThat(userRequestData.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(userRequestData.isPasswordValid(), is(true));
    assertThat(userRequestData.getPassword(), is("user.password"));
    assertThat(userRequestData.isSendEmail(), is(false));
    assertThat(userRequestData.getGroupId(), is("user.groupId"));
    assertThat(userRequestData.getLanguage(), is("en"));
    assertThat(userRequestData.getUserManualNotifReceiverLimitEnabled(), is(false));
    assertThat(userRequestData.getUserManualNotifReceiverLimitValue(), is(0));

    setHttpParameter("userPasswordValid", "false");
    setHttpParameter("sendEmail", "true");
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");
    userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);

    assertThat(userRequestData.isPasswordValid(), is(false));
    assertThat(userRequestData.isSendEmail(), is(true));
    assertThat(userRequestData.getUserManualNotifReceiverLimitEnabled(), is(false));
    assertThat(userRequestData.getUserManualNotifReceiverLimitValue(), is(0));

    setHttpParameter("userPasswordValid", "false");
    setHttpParameter("sendEmail", "false");
    setHttpParameter("userManualNotifReceiverLimitEnabled", "true");
    userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);

    assertThat(userRequestData.isPasswordValid(), is(false));
    assertThat(userRequestData.isSendEmail(), is(false));
    assertThat(userRequestData.getUserManualNotifReceiverLimitEnabled(), is(true));
    assertThat(userRequestData.getUserManualNotifReceiverLimitValue(), is(10));
  }

  // User

  @Test
  public void applyDataOnNewUserWithUserManualNotificationLimitNotEnabled() {
    UserDetail newUser = aUser();
    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), nullValue());
    assertThat(newUser.getLastName(), isEmptyString());
    assertThat(newUser.getFirstName(), isEmptyString());
    assertThat(newUser.geteMail(), isEmptyString());
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newUser);

    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), is("user.login"));
    assertThat(newUser.getLastName(), is("user.lastName"));
    assertThat(newUser.getFirstName(), is("user.firstName"));
    assertThat(newUser.geteMail(), is("user.email"));
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newUser.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnExistingUserWithUserManualNotificationLimitNotEnabled() {
    UserFull existingUser = aUser();
    existingUser.setPasswordAvailable(true);
    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), isEmptyString());
    assertThat(existingUser.getFirstName(), isEmptyString());
    assertThat(existingUser.geteMail(), isEmptyString());
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(existingUser.isPasswordValid(), is(false));
    assertThat(existingUser.getPassword(), isEmptyString());
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingUser);

    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), is("user.lastName"));
    assertThat(existingUser.getFirstName(), is("user.firstName"));
    assertThat(existingUser.geteMail(), is("user.email"));
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(existingUser.isPasswordValid(), is(true));
    assertThat(existingUser.getPassword(), is("user.password"));
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingUser.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnNewUserWithUserManualNotificationLimitEnabled() {
    enableServerLimitationAt(5);
    UserDetail newUser = aUser();
    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), nullValue());
    assertThat(newUser.getLastName(), isEmptyString());
    assertThat(newUser.getFirstName(), isEmptyString());
    assertThat(newUser.geteMail(), isEmptyString());
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(newUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newUser);

    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), is("user.login"));
    assertThat(newUser.getLastName(), is("user.lastName"));
    assertThat(newUser.getFirstName(), is("user.firstName"));
    assertThat(newUser.geteMail(), is("user.email"));
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(10));
    assertThat(newUser.getNotifManualReceiverLimit(), is(10));
  }

  @Test
  public void applyDataOnExistingUserWithUserManualNotificationLimitEnabled() {
    enableServerLimitationAt(5);

    UserFull existingUser = aUser();
    existingUser.setPasswordAvailable(true);
    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), isEmptyString());
    assertThat(existingUser.getFirstName(), isEmptyString());
    assertThat(existingUser.geteMail(), isEmptyString());
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(existingUser.isPasswordValid(), is(false));
    assertThat(existingUser.getPassword(), isEmptyString());
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(existingUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingUser);

    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), is("user.lastName"));
    assertThat(existingUser.getFirstName(), is("user.firstName"));
    assertThat(existingUser.geteMail(), is("user.email"));
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(existingUser.isPasswordValid(), is(true));
    assertThat(existingUser.getPassword(), is("user.password"));
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(10));
    assertThat(existingUser.getNotifManualReceiverLimit(), is(10));
  }

  @Test
  public void applyDataOnNewUserWithUserManualNotificationLimitEnabledNoLimit() {
    enableServerLimitationAt(5);
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");

    UserDetail newUser = aUser();
    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), nullValue());
    assertThat(newUser.getLastName(), isEmptyString());
    assertThat(newUser.getFirstName(), isEmptyString());
    assertThat(newUser.geteMail(), isEmptyString());
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(newUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newUser);

    assertThat(newUser.getId(), nullValue());
    assertThat(newUser.getLogin(), is("user.login"));
    assertThat(newUser.getLastName(), is("user.lastName"));
    assertThat(newUser.getFirstName(), is("user.firstName"));
    assertThat(newUser.geteMail(), is("user.email"));
    assertThat(newUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(newUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newUser.getNotifManualReceiverLimit(), is(0));
  }

  @Test
  public void applyDataOnExistingUserWithUserManualNotificationLimitEnabledNoLimit() {
    enableServerLimitationAt(5);
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");

    UserFull existingUser = aUser();
    existingUser.setPasswordAvailable(true);
    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), isEmptyString());
    assertThat(existingUser.getFirstName(), isEmptyString());
    assertThat(existingUser.geteMail(), isEmptyString());
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(existingUser.isPasswordValid(), is(false));
    assertThat(existingUser.getPassword(), isEmptyString());
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(existingUser.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingUser);

    assertThat(existingUser.getId(), nullValue());
    assertThat(existingUser.getLogin(), nullValue());
    assertThat(existingUser.getLastName(), is("user.lastName"));
    assertThat(existingUser.getFirstName(), is("user.firstName"));
    assertThat(existingUser.geteMail(), is("user.email"));
    assertThat(existingUser.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(existingUser.isPasswordValid(), is(true));
    assertThat(existingUser.getPassword(), is("user.password"));
    assertThat(existingUser.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingUser.getNotifManualReceiverLimit(), is(0));
  }

  // Admin

  @Test
  public void applyDataOnNewAdminWithUserManualNotificationLimitNotEnabled() {
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserDetail newAdmin = aUser();
    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), nullValue());
    assertThat(newAdmin.getLastName(), isEmptyString());
    assertThat(newAdmin.getFirstName(), isEmptyString());
    assertThat(newAdmin.geteMail(), isEmptyString());
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newAdmin);

    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), is("user.login"));
    assertThat(newAdmin.getLastName(), is("user.lastName"));
    assertThat(newAdmin.getFirstName(), is("user.firstName"));
    assertThat(newAdmin.geteMail(), is("user.email"));
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnExistingAdminWithUserManualNotificationLimitNotEnabled() {
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserFull existingAdmin = anAdmin();
    existingAdmin.setPasswordAvailable(true);
    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), isEmptyString());
    assertThat(existingAdmin.getFirstName(), isEmptyString());
    assertThat(existingAdmin.geteMail(), isEmptyString());
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(false));
    assertThat(existingAdmin.getPassword(), isEmptyString());
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingAdmin);

    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), is("user.lastName"));
    assertThat(existingAdmin.getFirstName(), is("user.firstName"));
    assertThat(existingAdmin.geteMail(), is("user.email"));
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(true));
    assertThat(existingAdmin.getPassword(), is("user.password"));
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnNewAdminWithUserManualNotificationLimitEnabled() {
    enableServerLimitationAt(5);
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserDetail newAdmin = aUser();
    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), nullValue());
    assertThat(newAdmin.getLastName(), isEmptyString());
    assertThat(newAdmin.getFirstName(), isEmptyString());
    assertThat(newAdmin.geteMail(), isEmptyString());
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newAdmin);

    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), is("user.login"));
    assertThat(newAdmin.getLastName(), is("user.lastName"));
    assertThat(newAdmin.getFirstName(), is("user.firstName"));
    assertThat(newAdmin.geteMail(), is("user.email"));
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnExistingAdminWithUserManualNotificationLimitEnabled() {
    enableServerLimitationAt(5);
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserFull existingAdmin = anAdmin();
    existingAdmin.setPasswordAvailable(true);
    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), isEmptyString());
    assertThat(existingAdmin.getFirstName(), isEmptyString());
    assertThat(existingAdmin.geteMail(), isEmptyString());
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(false));
    assertThat(existingAdmin.getPassword(), isEmptyString());
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingAdmin);

    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), is("user.lastName"));
    assertThat(existingAdmin.getFirstName(), is("user.firstName"));
    assertThat(existingAdmin.geteMail(), is("user.email"));
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(true));
    assertThat(existingAdmin.getPassword(), is("user.password"));
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnNewAdminWithUserManualNotificationLimitEnabledNoLimit() {
    enableServerLimitationAt(5);
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserDetail newAdmin = aUser();
    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), nullValue());
    assertThat(newAdmin.getLastName(), isEmptyString());
    assertThat(newAdmin.getFirstName(), isEmptyString());
    assertThat(newAdmin.geteMail(), isEmptyString());
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(5));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnNewUser(newAdmin);

    assertThat(newAdmin.getId(), nullValue());
    assertThat(newAdmin.getLogin(), is("user.login"));
    assertThat(newAdmin.getLastName(), is("user.lastName"));
    assertThat(newAdmin.getFirstName(), is("user.firstName"));
    assertThat(newAdmin.geteMail(), is("user.email"));
    assertThat(newAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(newAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(newAdmin.getNotifManualReceiverLimit(), nullValue());
  }

  @Test
  public void applyDataOnExistingAdminWithUserManualNotificationLimitEnabledNoLimit() {
    enableServerLimitationAt(5);
    setHttpParameter("userManualNotifReceiverLimitEnabled", "false");
    setHttpParameter("userAccessLevel", "ADMINISTRATOR");

    UserFull existingAdmin = anAdmin();
    existingAdmin.setPasswordAvailable(true);
    existingAdmin.setNotifManualReceiverLimit(25);
    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), isEmptyString());
    assertThat(existingAdmin.getFirstName(), isEmptyString());
    assertThat(existingAdmin.geteMail(), isEmptyString());
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(false));
    assertThat(existingAdmin.getPassword(), isEmptyString());
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), is(25));

    UserRequestData userRequestData =
        RequestParameterDecoder.decode(httpRequest, UserRequestData.class);
    userRequestData.applyDataOnExistingUser(existingAdmin);

    assertThat(existingAdmin.getId(), nullValue());
    assertThat(existingAdmin.getLogin(), nullValue());
    assertThat(existingAdmin.getLastName(), is("user.lastName"));
    assertThat(existingAdmin.getFirstName(), is("user.firstName"));
    assertThat(existingAdmin.geteMail(), is("user.email"));
    assertThat(existingAdmin.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(existingAdmin.isPasswordValid(), is(true));
    assertThat(existingAdmin.getPassword(), is("user.password"));
    assertThat(existingAdmin.getUserManualNotificationUserReceiverLimitValue(), is(0));
    assertThat(existingAdmin.getNotifManualReceiverLimit(), is(25));
  }

  /*
  CURRENT TEST TOOLS
   */

  private void setHttpParameter(String parameterName, String parameterValue) {
    when(httpServletRequestMock.getParameter(parameterName)).thenReturn(parameterValue);
  }

  private void enableServerLimitationAt(int limit) {
    when(mockedSettings.getInteger("notif.manual.receiver.limit", 0)).thenReturn(limit);
  }

  private UserFull aUser() {
    UserFull aUser = new UserFull();
    aUser.setAccessLevel(UserAccessLevel.USER);
    return aUser;
  }

  private UserFull anAdmin() {
    UserFull aAdmin = new UserFull();
    aAdmin.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    return aAdmin;
  }
}