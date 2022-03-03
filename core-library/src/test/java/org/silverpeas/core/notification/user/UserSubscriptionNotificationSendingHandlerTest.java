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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.system.UnitTestResource;
import org.silverpeas.core.notification.system.UnitTestResourceEvent;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
class UserSubscriptionNotificationSendingHandlerTest {

  private static final String SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM =
      "SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION";

  private HttpServletRequest request;
  private AbstractResourceEvent resourceEvent;

  @BeforeEach
  public void setup() {
    request = mock(HttpServletRequest.class);
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    resourceEvent = new UnitTestResourceEvent(ResourceEvent.Type.CREATION,
        new UnitTestResource("26", "Toto Chez-les-Papoos", new Date()));
  }

  @Test
  void enabledIfSkipHttpParameterIsNull() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(null);
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{}".getBytes()));
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":false}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToFalseAsBase64() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{\"skip\":false}".getBytes()));
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void notEnabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":true}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNull() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(null);
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToTrueOrFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{}".getBytes()));
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":false}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToFalseAsBase64() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{\"skip\":false}".getBytes()));
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void notEnabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":true}");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(UserSubscriptionNotificationSendingHandler
        .isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

}