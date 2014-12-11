/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.silverpeas.notification.builder;

import com.silverpeas.notification.SilverpeasNotification;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.cache.service.CacheServiceFactory;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserSubscriptionNotificationSendingHandlerTest {

  private HttpServletRequest request;
  private SilverpeasNotification notification;

  @Before
  public void setup() {
    CacheServiceFactory.getThreadCacheService().clear();
    CacheServiceFactory.getRequestCacheService().clear();
    request = mock(HttpServletRequest.class);
    notification = new SilverpeasNotification(null, null);
  }

  @Test
  public void enabledByDefault() {
    UserSubscriptionNotificationSendingHandler.verifyRequestParameters(request);
    UserSubscriptionNotificationSendingHandler.verifySilverpeasNotification(notification);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("toto");
    UserSubscriptionNotificationSendingHandler.verifyRequestParameters(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("false");
    UserSubscriptionNotificationSendingHandler.verifyRequestParameters(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void notEnabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("true");
    UserSubscriptionNotificationSendingHandler.verifyRequestParameters(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceFactory.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceFactory.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterFromNotificationIsNotEqualToTrueOrFalse() {
    notification.addParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "toto");
    UserSubscriptionNotificationSendingHandler.verifySilverpeasNotification(notification);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterFromNotificationIsNotEqualToFalse() {
    notification.addParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "false");
    UserSubscriptionNotificationSendingHandler.verifySilverpeasNotification(notification);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void notEnabledIfSkipHttpFromNotificationParameterIsNotEqualToFalse() {
    notification.addParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "true");
    UserSubscriptionNotificationSendingHandler.verifySilverpeasNotification(notification);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceFactory.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceFactory.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }
}