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
package org.silverpeas.core.notification.user.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.SettingBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.notification.user.client.CurrentUserNotificationContext.getCurrentUserNotificationContext;

@EnableSilverTestEnv
public class CurrentUserNotificationContextTest {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  private UserDetail currentUser;
  private SettingBundle mockedSettings;

  @BeforeEach
  public void setup(@TestManagedMock OrganizationController ctrl) {
    currentUser = spy(new UserDetail());
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService()).newSessionCache(currentUser);
    mockedSettings =
        mocker.mockField(NotificationManagerSettings.class, SettingBundle.class, "settings");
    // By default, a user is not an anonymous one
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(false));

    when(ctrl.getUserDetail(anyString())).thenReturn(new UserDetail());
    when(UserProvider.get().getUser(anyString())).thenReturn(new UserDetail());
  }

  @AfterEach
  public void tearDown() {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
  }

  /*
  TESTS around
  {@link CurrentUserNotificationContext#checkManualUserNotification(NotificationMetaData)}.
   */

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndNoCurrentUserAndLimitationNotEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndNoCurrentUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      CacheServiceProvider.getRequestCacheService().clearAllCaches();
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndNoCurrentUserAndLimitationNotEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndNoCurrentUserAndLimitationEnabled()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationNotEnabled() throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationEnabled() throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationEnabledAndNbReceiversOverLimit() {
    assertThrows(NotificationException.class, () -> {
      CacheServiceProvider.getRequestCacheService().clearAllCaches();
      enableLimitationAt(3);
      getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
    });
  }

  @Test
  public void checkManualUserNotificationWithNoCurrentUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Admin

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAdminUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
      assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentAdminUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAdminUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessAdmin(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // Domain administrator

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentDomainAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentDomainAdminUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
      assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentDomainAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentDomainAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentDomainAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessDomainManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // Space administrator

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
      assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentSpaceAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentSpaceAdminUserAndLimitationEnabledAndNbReceiversOverLimit()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.SPACE_ADMINISTRATOR);
    assertThat(UserDetail.getCurrentRequester().isAccessSpaceManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // PDC manager

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentPdcAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentPdcAdminUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
      assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentPdcAdminUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentPdcAdminUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentPdcAdminUserAndLimitationEnabledAndNbReceiversOverLimit()

      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    assertThat(UserDetail.getCurrentRequester().isAccessPdcManager(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
  }

  // User

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.USER);
      assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationEnabledAndNbReceiversOverLimit() {
    assertThrows(NotificationException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.USER);
      assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
      enableLimitationAt(3);
      getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
    });
  }

  @Test
  public void checkManualUserNotificationWithCurrentUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.USER);
    assertThat(UserDetail.getCurrentRequester().isAccessUser(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Guest

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentGuestUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentGuestUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.GUEST);
      assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentGuestUserAndLimitationNotEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentGuestUserAndLimitationEnabled()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationNotEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabled() throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabledAndNbReceiversOverLimit() {
    assertThrows(NotificationException.class, () -> {
      currentUser.setAccessLevel(UserAccessLevel.GUEST);
      assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
      enableLimitationAt(3);
      getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
    });
  }

  @Test
  public void checkManualUserNotificationWithCurrentGuestUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
      throws Exception {
    currentUser.setAccessLevel(UserAccessLevel.GUEST);
    assertThat(UserDetail.getCurrentRequester().isAccessGuest(), is(true));
    enableLimitationAt(3);
    getCurrentUserNotificationContext().checkManualUserNotification(getDefaultUserOne(4));
  }

  // Anonymous

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAnonymousUserAndLimitationNotEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(null);
  }

  @Test
  public void checkManualUserNotificationWithNullNotificationMetaDataAndCurrentAnonymousUserAndLimitationEnabled() {
    assertThrows(NullPointerException.class, () -> {
      doReturn(true).when(currentUser).isAnonymous();
      assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
      enableLimitationAt(1);
      getCurrentUserNotificationContext().checkManualUserNotification(null);
    });
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAnonymousUserAndLimitationNotEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithEmptyNotificationMetaDataAndCurrentAnonymousUserAndLimitationEnabled()
      throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(1);
    getCurrentUserNotificationContext().checkManualUserNotification(new NotificationMetaData());
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationNotEnabled() throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(1));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabled() throws Exception {
    doReturn(true).when(currentUser).isAnonymous();
    assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
    enableLimitationAt(2);
    getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(2));
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabledAndNbReceiversOverLimit() {
    assertThrows(NotificationException.class, () -> {
      doReturn(true).when(currentUser).isAnonymous();
      assertThat(UserDetail.getCurrentRequester().isAnonymous(), is(true));
      enableLimitationAt(3);
      getCurrentUserNotificationContext().checkManualUserNotification(getManualUserOne(4));
    });
  }

  @Test
  public void checkManualUserNotificationWithCurrentAnonymousUserAndLimitationEnabledAndNbReceiversOverLimitAndNotAManualOne()
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