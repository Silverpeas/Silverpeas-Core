/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
class UserSubscriptionNotificationSendingHandlerTest {

  private static final String SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM =
      "SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION";

  @TestedBean
  UserSubscriptionNotificationSendingHandler handler;

  private HttpServletRequest request;

  @BeforeEach
  public void setup() {
    request = mock(HttpServletRequest.class);
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void enabledIfSkipHttpParameterIsNull() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(null);
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void notEnabledIfSkipHttpParameterIsNullButDisabledManually() {
    handler.skipNotificationSend();
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsEqualToFalse() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":false}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpParameterIsEqualToFalseAsBase64() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{\"skip\":false}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void notEnabledIfSkipHttpParameterIsEqualToTrue() {
    when(request.getParameter(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":true}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNull() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(null);
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToTrueOrFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":false}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void enabledIfSkipHttpHeaderIsNotEqualToFalseAsBase64() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn(StringUtil.asBase64("{\"skip\":false}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

  @Test
  void notEnabledIfSkipHttpHeaderIsNotEqualToFalse() {
    when(request.getHeader(SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM))
        .thenReturn("{\"skip\":true}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(false));
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    assertThat(handler.isSubscriptionNotificationEnabledForCurrentRequest(), is(true));
  }

}