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

package org.silverpeas.core.contribution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class ContributionModificationContextHandlerTest {

  private static final String CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM =
      "CONTRIBUTION_MODIFICATION_CONTEXT";

  @RegisterExtension
  static SettingBundleStub contributionSettings = new SettingBundleStub(
      "org.silverpeas.contribution.settings.contribution");

  private HttpServletRequest request;

  @BeforeEach
  public void setup() {
    request = mock(HttpServletRequest.class);
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    contributionSettings.put("contribution.modification.behavior.minor", "true");
  }

  @Test
  @DisplayName("If no context exists into HTTP parameters, minor modification method indicates " +
      "that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNull() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(null);
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, but with unknown value for isMinor, minor" +
      " modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn("{}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, but with unknown value as base for " +
      "isMinor 64, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with false value for isMinor, minor " +
      "modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsEqualToFalse() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":false}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with false value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsEqualToFalseAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":false}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with true value for isMinor, minor " +
      "modification method indicates that the modification is minor")
  void minorModificationWhenHttpParameterIsEqualToTrue() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with true value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is minor")
  void minorModificationWhenHttpParameterIsEqualToTrueAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":true}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with true value for isMinor but " +
      "deactivated behavior into settings, minor modification method indicates that the " +
      "modification is not minor")
  void noMinorModificationWhenHttpParameterIsEqualToTrueButBehaviorDeactivated() {
    contributionSettings.put("contribution.modification.behavior.minor", "false");
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If no context exists into HTTP headers, minor modification method indicates " +
      "that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNull() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(null);
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, but with unknown value for isMinor, minor" +
      " modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNotEqualToTrueOrFalse() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn("{}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, but with unknown value as base for " +
      "isMinor 64, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with false value for isMinor, minor " +
      "modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsEqualToFalse() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":false}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with false value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsEqualToFalseAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":false}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with true value for isMinor, minor " +
      "modification method indicates that the modification is minor")
  void minorModificationWhenHttpHeaderIsEqualToTrue() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with true value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is minor")
  void minorModificationWhenHttpHeaderIsEqualToTrueAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":true}".getBytes()));
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(true));
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with true value for isMinor but " +
      "deactivated behavior into settings, minor modification method indicates that the " +
      "modification is not minor")
  void noMinorModificationWhenHttpHeaderIsEqualToTrueButBehaviorDeactivated() {
    contributionSettings.put("contribution.modification.behavior.minor", "false");
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionModificationContextHandler.verifyRequest(request);
    assertThat(ContributionModificationContextHandler.isMinorModification(), is(false));
  }
}