/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.util.SettingBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv
class UserManualNotificationUserReceiverLimitUserDetailTest {

  private static final int NOT_LIMITED = 0;
  private static final int LIMITED = 5;

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  private SettingBundle mockedSettings;
  private UserDetail currentUser;

  @BeforeEach
  public void setup() {
    mockedSettings = mocker.mockField(NotificationManagerSettings.class,
        SettingBundle.class, "settings");
    currentUser = spy(new UserDetail());
    // By default, a user is not an anonymous one
    assertThat(currentUser.isAnonymous(), is(false));
  }

  @Test
  void newUserDetailInstanceAndLimitationNotEnabled() {
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void newUserDetailInstanceAndLimitationEnabled() {
    enableLimitation();
    assertThatUserIsLimitedByDefault();
  }

  /*
  TESTS around
  {@link UserDetail#setUserManualNotificationUserReceiverLimit(Integer)}.
   */

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUnknownUserAndLimitationNotEnabled() {
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // Admin

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForAdminUserAndLimitationNotEnabled () {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForAdminUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(NOT_LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // Domain administrator

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForDomainAdminUserAndLimitationEnabled
      () {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(NOT_LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // Space administrator

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(NOT_LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // Pdc Manager

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(NOT_LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // User

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsSpecificallyLimitedTo(NOT_LIMITED);
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsSpecificallyLimitedTo(1);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToDefaultLimitForUserAndLimitationEnabled
      () {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsSpecificallyLimitedTo(2);
  }

  // Guest

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForGuestUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToNullForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForGuestUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToZeroForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsSpecificallyLimitedTo(NOT_LIMITED);
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsSpecificallyLimitedTo(1);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsSpecificallyLimitedTo(2);
  }

  // Anonymous

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNullForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToZeroForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToOneForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void setUserManualNotificationUserReceiverLimitToOneForAnonymousUserAndLimitationEnabled
      () {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAnonymousUserAndLimitationNotEnabled
      () {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  /*
  CURRENT TEST TOOLS
   */

  private void enableLimitation() {
    when(mockedSettings.getInteger("notif.manual.receiver.limit", 0)).thenReturn(LIMITED);
  }

  private void assertThatUserIsLimitedByDefault() {
    assertThat(currentUser.isUserManualNotificationUserReceiverLimit(), is(true));
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), nullValue());
  }

  private void assertThatUserIsNotLimitedAndNoLimitPersisted() {
    assertThatUserIsNotLimitedAndPersistedLimitIs(null);
  }

  private void assertThatUserIsNotLimitedAndPersistedLimitIs(Integer persistedLimit) {
    assertThat(currentUser.isUserManualNotificationUserReceiverLimit(), is(false));
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(NOT_LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(persistedLimit));
  }

  private void assertThatUserIsSpecificallyLimitedTo(Integer limit) {
    boolean isUserManualNotificationUserReceiverLimit = limit != null && limit > 0;
    assertThat(currentUser.isUserManualNotificationUserReceiverLimit(),
        is(isUserManualNotificationUserReceiverLimit));
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(limit));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(limit));
  }
}