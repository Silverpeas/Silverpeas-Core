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
package org.silverpeas.core.notification.user;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.system.UnitTestResource;
import org.silverpeas.core.notification.system.UnitTestResourceEvent;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserSubscriptionNotificationSendingHandlerTest {

  private static final String SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM =
      "SKIP_SUBSCRIPTION_NOTIFICATION_SENDING";

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
  public void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("toto");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("false");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void notEnabledIfSkipHttpParameterIsNotEqualToFalse() {
    when(request.getParameter(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))
        .thenReturn("true");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpHeaderIsNotEqualToTrueOrFalse() {
    when(request.getHeader(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM)).thenReturn("toto");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void enabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM)).thenReturn("false");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

  @Test
  public void notEnabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM)).thenReturn("true");
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getThreadCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(false));
    CacheServiceProvider.getRequestCacheService().clear();
    assertThat(UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest(), is(true));
  }

}