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
package com.silverpeas.usernotification.builder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.notification.AbstractResourceEvent;
import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.notification.util.UnitTestResource;
import org.silverpeas.notification.util.UnitTestResourceEvent;
import org.silverpeas.test.rule.CommonAPI4Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserSubscriptionNotificationSendingHandlerTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private HttpServletRequest request;
  private AbstractResourceEvent resourceEvent;

  @Before
  public void setup() {
    CacheServiceProvider.getThreadCacheService().clear();
    CacheServiceProvider.getRequestCacheService().clear();
    request = mock(HttpServletRequest.class);
    resourceEvent = new UnitTestResourceEvent(ResourceEvent.Type.CREATION,
        new UnitTestResource("26", "Toto Chez-les-Papoos", new Date()));
  }

  @Test
  public void enabledByDefault() {
    UserSubscriptionNotificationSendingHandler.verifyRequestParameters(request);
    UserSubscriptionNotificationSendingHandler.verifyResourceEvent(resourceEvent);
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
    CacheServiceProvider.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterFromNotificationIsNotEqualToTrueOrFalse() {
    resourceEvent.putParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "toto");
    UserSubscriptionNotificationSendingHandler.verifyResourceEvent(resourceEvent);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterFromNotificationIsNotEqualToFalse() {
    resourceEvent.putParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "false");
    UserSubscriptionNotificationSendingHandler.verifyResourceEvent(resourceEvent);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void notEnabledIfSkipHttpFromNotificationParameterIsNotEqualToFalse() {
    resourceEvent.putParameter(
        UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
        "true");
    UserSubscriptionNotificationSendingHandler.verifyResourceEvent(resourceEvent);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }
}