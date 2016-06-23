/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.notification.sse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
class AbstractServerEventDispatcherTaskTest {

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  Map<AsyncContext, AsyncContext> asyncContextMap;
  List<ServerEvent> lastServerEvents;

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Before
  @After
  @SuppressWarnings("unchecked")
  public void setup() throws Exception {
    asyncContextMap = (Map<AsyncContext, AsyncContext>) FieldUtils
        .readDeclaredStaticField(ServerEventDispatcherTask.class, "asyncContexts", true);
    lastServerEvents = (List<ServerEvent>) FieldUtils
        .readDeclaredStaticField(ServerEventDispatcherTask.class, "lastServerEvents", true);
    asyncContextMap.clear();
    lastServerEvents.clear();
    FieldUtils.writeDeclaredStaticField(AbstractServerEvent.class, "idCounter", 0L, true);
  }

  ServerEvent newMockedServerEvent(String name, String data) throws Exception {
    ServerEvent mock = spy(AbstractServerEvent.class);
    when(mock.getName()).thenReturn(() -> name);
    when(mock.getData(any(User.class))).thenReturn(data);
    return mock;
  }

  SilverpeasAsyncContext newMockedAsyncContext(final String sessionId) throws Exception {
    SilverpeasAsyncContext mock = mock(SilverpeasAsyncContext.class);
    HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
    PrintWriter mockedPrintWriter = mock(PrintWriter.class);
    when(mock.getResponse()).thenReturn(mockedResponse);
    when(mock.getSessionId()).thenReturn(sessionId);
    when(mock.getLastServerEventId()).thenReturn(null);
    when(mockedResponse.getWriter()).thenReturn(mockedPrintWriter);
    when(mockedPrintWriter.append(anyString())).thenReturn(mockedPrintWriter);
    return mock;
  }

  String getSentServerEventStream(final AsyncContext mockedAsyncContext) throws IOException {
    return getSentServerEventStream(mockedAsyncContext, 1);
  }

  String getSentServerEventStream(final AsyncContext mockedAsyncContext, final int nbPerform)
      throws IOException {
    verify(mockedAsyncContext, times(nbPerform)).getResponse();
    verify(mockedAsyncContext.getResponse(), times(nbPerform)).getWriter();
    ArgumentCaptor<String> requestContentCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockedAsyncContext.getResponse().getWriter(), times(nbPerform))
        .append(requestContentCaptor.capture());
    verify(mockedAsyncContext.getResponse(), times(nbPerform)).flushBuffer();
    StringBuilder result = new StringBuilder();
    requestContentCaptor.getAllValues().forEach(result::append);
    return result.toString();
  }

  void pause() throws Exception {
    Thread.sleep(200);
  }
}