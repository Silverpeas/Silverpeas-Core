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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.SettingBundle;

import static org.silverpeas.core.notification.user.client.CurrentUserNotificationContext
    .getCurrentUserNotificationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CurrentUserNotificationContextTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private UserDetail currentUser;
  private SettingBundle mockedSettings;

  @Before
  public void setup() {
    currentUser = spy(new UserDetail());
    CacheServiceProvider.getRequestCacheService().clear();
    CacheServiceProvider.getRequestCacheService().put(UserDetail.CURRENT_REQUESTER_KEY, currentUser);
    mockedSettings = reflectionRule
        .mockField(NotificationManagerSettings.class, SettingBundle.class, "settings");
    // By default, a user is not an anonymous one
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(false));

    final OrganizationController mockedOrganizationController =
        commonAPI4Test.injectIntoMockedBeanContainer(mock(OrganizationController.class));
    when(mockedOrganizationController.getUserDetail(anyString())).thenReturn(new UserDetail());
  }

  @After
  public void tearDown() {
    CacheServiceProvider.getRequestCacheService().clear();
  }

  /*
  TESTS around
  {@link CurrentUserNotificationContext#checkManualUserNotification(NotificationMetaData)}.
   */

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndNoCurrentUserAndLimitationNotEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndNoCurrentUserAndLimitationEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndNoCurrentUserAndLimitationNotEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndNoCurrentUserAndLimitationEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationNotEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationEnabled() throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test(expected = NotificationManagerException.class)
  public void
  checkManualUserNotificationWithNoCurrentUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  @Test
  public void
  checkManualUserNotificationWithNoCurrentUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clear();
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Admin

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // Domain administrator

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentDomainAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentDomainAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentDomainAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentDomainAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // Space administrator

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // PDC manager

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentPdcAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentPdcAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentPdcAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentPdcAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // User

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test(expected = NotificationManagerException.class)
  public void
  checkManualUserNotificationWithCurrentUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Guest

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentGuestUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentGuestUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentGuestUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentGuestUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test(expected = NotificationManagerException.class)
  public void
  checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Anonymous

  @Test
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAnonymousUserAndLimitationNotEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test(expected = NullPointerException.class)
  public void
  checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAnonymousUserAndLimitationEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAnonymousUserAndLimitationNotEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void
  checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAnonymousUserAndLimitationEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationNotEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test(expected = NotificationManagerException.class)
  public void
  checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  @Test
  public void
  checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  /*
  CURRENT TEST TOOLS
   */

  private void enableLimitationAt(int limit) {
    when(mockedSettings.getInteger("notif.manual.receiver.limit", 0)).thenReturn(limit);
  }

  private NotificationMetaData getManualUserOne(int nbReceivers) {
    return getDefaultUserOne(nbReceivers).manualUserNotification();
  }

  private NotificationMetaData getDefaultUserOne(int nbReceivers) {
    NotificationMetaData notificationMetaData = new NotificationMetaData();
    for (int i = 0; i < nbReceivers; i++) {
      notificationMetaData.addUserRecipient(new UserRecipient(String.valueOf(i)));
    }
    return notificationMetaData;
  }
}