/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.notification.sse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.sse.ServerEventDispatcherTask.ServerEventStore;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.util.logging.Level;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@LoggerLevel(Level.DEBUG)
@TestManagedBeans(ServerEventDispatcherTask.class)
abstract class AbstractServerEventDispatcherTaskTest {

  final static String EVENT_SOURCE_REQUEST_URI = "/handled";

  Set<AsyncContext> asyncContextMap;
  private ServerEventStore serverEventStore;

  @RequesterProvider
  public User getDefaultRequester() {
    UserDetail user = new UserDetail();
    user.setId("32");
    return user;
  }

  @BeforeEach
  @AfterEach
  @SuppressWarnings("unchecked")
  public void setup() throws Exception {
    asyncContextMap = (Set<AsyncContext>) FieldUtils
        .readDeclaredStaticField(ServerEventDispatcherTask.class, "synchronizedContexts", true);
    serverEventStore = (ServerEventStore) FieldUtils
        .readDeclaredStaticField(ServerEventDispatcherTask.class, "serverEventStore", true);
    asyncContextMap.clear();
    serverEventStore.clear();
    FieldUtils.writeDeclaredStaticField(AbstractServerEvent.class, "idCounter", 0L, true);

    new SseLogger().init();
  }

  ServerEvent newMockedServerEvent(String name, String data) throws Exception {
    ServerEvent mock = spy(AbstractServerEvent.class);
    when(mock.getName()).thenReturn(() -> name);
    when(mock.getData(anyString(), any(User.class))).thenReturn(data);
    return mock;
  }

  List<ServerEvent> getStoredServerEvents() throws Exception {
    return serverEventStore.getFromId(-1);
  }

  SilverpeasAsyncContext newMockedAsyncContext(final String sessionId) throws Exception {
    SilverpeasAsyncContext mock = mock(SilverpeasAsyncContext.class);
    HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
    PrintWriter mockedPrintWriter = mock(PrintWriter.class);
    when(mock.getMutex()).thenReturn(mock);
    when(mock.isSendPossible()).thenReturn(true);
    when(mock.getUser()).thenReturn(new UserDetail());
    when(mock.getRequest()).thenReturn(mockedRequest);
    when(mock.getResponse()).thenReturn(mockedResponse);
    when(mock.getSessionId()).thenReturn(sessionId);
    when(mock.getLastServerEventId()).thenReturn(null);
    when(mockedRequest.getRequestURI()).thenReturn(EVENT_SOURCE_REQUEST_URI);
    when(mockedResponse.getWriter()).thenReturn(mockedPrintWriter);
    when(mockedPrintWriter.append(anyString())).thenReturn(mockedPrintWriter);
    return mock;
  }

  String getSentServerEventStream(final SilverpeasAsyncContext mockedAsyncContext)
      throws IOException {
    return getSentServerEventStream(mockedAsyncContext, 1);
  }

  String getSentServerEventStream(final SilverpeasAsyncContext mockedAsyncContext,
      final int nbPerform) throws IOException {
    verify(mockedAsyncContext, atLeast(nbPerform)).isSendPossible();
    verify(mockedAsyncContext, atLeast(nbPerform)).getRequest();
    verify(mockedAsyncContext, atLeast(nbPerform)).getResponse();
    verify(mockedAsyncContext.getResponse(), atLeast(nbPerform)).getWriter();
    ArgumentCaptor<String> requestContentCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockedAsyncContext.getResponse().getWriter(), atLeast(nbPerform))
        .append(requestContentCaptor.capture());
    verify(mockedAsyncContext.getResponse(), atLeast(nbPerform)).flushBuffer();
    StringBuilder result = new StringBuilder();
    requestContentCaptor.getAllValues().forEach(result::append);
    return result.toString();
  }

  void verifyNotSent(final SilverpeasAsyncContext mockedAsyncContext) throws IOException {
    verify(mockedAsyncContext, atLeast(1)).isSendPossible();
    verify(mockedAsyncContext, never()).getRequest();
    verify(mockedAsyncContext, never()).getResponse();
    verify(mockedAsyncContext.getResponse(), never()).getWriter();
  }

  void afterSomeTimesCheck(final ThrowingRunnable assertions) {
    with().pollInterval(400, TimeUnit.MILLISECONDS)
        .await()
        .atMost(Duration.TWO_SECONDS)
        .untilAsserted(assertions);
  }
}