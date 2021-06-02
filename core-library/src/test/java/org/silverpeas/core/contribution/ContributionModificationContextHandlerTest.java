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
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestedBean;
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

  @TestedBean
  ContributionModificationContextHandler handler;

  private HttpServletRequest request;

  @BeforeEach
  public void setup() {
    request = mock(HttpServletRequest.class);
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
  }

  @Test
  @DisplayName("If no context exists into HTTP parameters, minor modification method indicates " +
      "that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNull() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(null);
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, but with unknown value for isMinor, minor" +
      " modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNotEqualToTrueOrFalse() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn("{}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, but with unknown value as base for " +
      "isMinor 64, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with false value for isMinor, minor " +
      "modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsEqualToFalse() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":false}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with false value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpParameterIsEqualToFalseAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":false}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with true value for isMinor, minor " +
      "modification method indicates that the modification is minor")
  void minorModificationWhenHttpParameterIsEqualToTrue() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMinorModificationDetected();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertMinorModificationDetected();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP parameters, and with true value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is minor")
  void minorModificationWhenHttpParameterIsEqualToTrueAsBase64() {
    when(request.getParameter(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":true}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMinorModificationDetected();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertMinorModificationDetected();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If no context exists into HTTP headers, minor modification method indicates " +
      "that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNull() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(null);
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, but with unknown value for isMinor, minor" +
      " modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNotEqualToTrueOrFalse() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn("{}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, but with unknown value as base for " +
      "isMinor 64, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsNotEqualToTrueOrFalseAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with false value for isMinor, minor " +
      "modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsEqualToFalse() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":false}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with false value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is not minor")
  void noMinorModificationWhenHttpHeaderIsEqualToFalseAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":false}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with true value for isMinor, minor " +
      "modification method indicates that the modification is minor")
  void minorModificationWhenHttpHeaderIsEqualToTrue() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        "{\"isMinor\":true}");
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMinorModificationDetected();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertMinorModificationDetected();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertNoMinorMajorModificationDetected();
  }

  @Test
  @DisplayName("If context exists into HTTP headers, and with true value as base 64 for " +
      "isMinor, minor modification method indicates that the modification is minor")
  void minorModificationWhenHttpHeaderIsEqualToTrueAsBase64() {
    when(request.getHeader(CONTRIBUTION_MODIFICATION_CONTEXT_HTTP_PARAM)).thenReturn(
        StringUtil.asBase64("{\"isMinor\":true}".getBytes()));
    ContributionOperationContextPropertyHandler.parseRequest(request);
    assertMinorModificationDetected();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertMinorModificationDetected();
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertNoMinorMajorModificationDetected();
  }

  private void assertNoMinorMajorModificationDetected() {
    assertThat(handler.isMinorModification().isPresent(), is(false));
  }

  private void assertMinorModificationDetected() {
    assertThat(handler.isMinorModification().isPresent(), is(true));
    assertThat(handler.isMinorModification().get(), is(true));
  }

  private void assertMajorModificationDetected() {
    assertThat(handler.isMinorModification().isPresent(), is(true));
    assertThat(handler.isMinorModification().get(), is(false));
  }
}