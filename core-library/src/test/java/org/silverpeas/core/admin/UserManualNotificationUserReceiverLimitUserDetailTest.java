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
package org.silverpeas.core.admin;

import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.SettingBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class UserManualNotificationUserReceiverLimitUserDetailTest {

  private static final int NOT_LIMITED = 0;
  private static final int LIMITED = 5;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private SettingBundle mockedSettings;
  private UserDetail currentUser;

  @Before
  public void setup() {
    mockedSettings = reflectionRule.mockField(NotificationManagerSettings.class,
        SettingBundle.class, "settings");
    currentUser = spy(new UserDetail());
    // By default, a user is not an anonymous one
    assertThat(currentUser.isAnonymous(), is(false));
  }

  @Test
  public void newUserDetailInstanceAndLimitationNotEnabled() {
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void newUserDetailInstanceAndLimitationEnabled() {
    enableLimitation();
    assertThatUserIsLimitedByDefault();
  }

  /*
  TESTS around
  {@link UserDetail#setUserManualNotificationUserReceiverLimit(Integer)}.
   */

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUnknownUserAndLimitationNotEnabled() {
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUnknownUserAndLimitationNotEnabled() {
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUnknownUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThat(currentUser.getUserManualNotificationUserReceiverLimitValue(), is(LIMITED));
    assertThat(currentUser.getNotifManualReceiverLimit(), is(3));
  }

  // Admin

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForAdminUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForAdminUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
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
  public void
  setUserManualNotificationUserReceiverLimitToNullForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForDomainAdminUserAndLimitationEnabled
      () {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForDomainAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForDomainAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
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
  public void
  setUserManualNotificationUserReceiverLimitToNullForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForSpaceAdminUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForSpaceAdminUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
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
  public void
  setUserManualNotificationUserReceiverLimitToNullForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForPdcManagerUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForPdcManagerUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
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
  public void setUserManualNotificationUserReceiverLimitToNullForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsSpecificallyLimitedTo(NOT_LIMITED);
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsSpecificallyLimitedTo(1);
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToDefaultLimitForUserAndLimitationEnabled
      () {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.USER);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsSpecificallyLimitedTo(2);
  }

  // Guest

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForGuestUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToNullForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForGuestUserAndLimitationNotEnabled
      () {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToZeroForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsSpecificallyLimitedTo(NOT_LIMITED);
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsSpecificallyLimitedTo(1);
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForGuestUserAndLimitationNotEnabled() {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForGuestUserAndLimitationEnabled() {
    enableLimitation();
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsSpecificallyLimitedTo(2);
  }

  // Anonymous

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNullForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(null);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToNegativeValueForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(-1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToZeroForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(NOT_LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToOneForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void setUserManualNotificationUserReceiverLimitToOneForAnonymousUserAndLimitationEnabled
      () {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(1);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAnonymousUserAndLimitationNotEnabled
      () {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsNotLimitedAndNoLimitPersisted();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitToDefaultLimitForAnonymousUserAndLimitationEnabled() {
    enableLimitation();
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setUserManualNotificationUserReceiverLimit(LIMITED);
    assertThatUserIsLimitedByDefault();
  }

  @Test
  public void
  setUserManualNotificationUserReceiverLimitWhilePersistedOneExistsForAnonymousUserAndLimitationNotEnabled() {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(currentUser.isAnonymous(), is(true));
    currentUser.setNotifManualReceiverLimit(3);
    currentUser.setUserManualNotificationUserReceiverLimit(2);
    assertThatUserIsNotLimitedAndPersistedLimitIs(3);
  }

  @Test
  public void
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