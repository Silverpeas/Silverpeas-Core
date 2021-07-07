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
package org.silverpeas.core.notification.sse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.sse.ServerEventDispatcherTask.ServerEventStore;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.Level;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@LoggerLevel(Level.DEBUG)
@TestManagedBeans({ServerEventDispatcherTask.class, RequestTaskManager.class,
    ServerEventWaitForManager.class, SilverpeasAsyncContextManager.class})
abstract class AbstractServerEventDispatcherTaskTest {

  final static String EVENT_SOURCE_REQUEST_URI = "/handled";

  Set<AsyncContext> asyncContextMap;
  Map<String, ?> contextsByEventType;
  private ServerEventStore serverEventStore;
  private AsyncContext mockedIgnoredAsyncContext;

  @RequesterProvider
  public User getDefaultRequester() {
    UserDetail user = new UserDetail();
    user.setId("32");
    return user;
  }

  @BeforeEach
  @AfterEach
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void setup() throws Exception {
    final SilverpeasAsyncContextManager manager = SilverpeasAsyncContextManager.get();
    asyncContextMap = (Set<AsyncContext>) FieldUtils.readDeclaredField(manager, "contexts", true);
    final ServerEventWaitForManager waitManager = ServerEventWaitForManager.get();
    contextsByEventType = (Map) FieldUtils.readDeclaredField(waitManager, "contextsByEventType", true);
    contextsByEventType.clear();
    serverEventStore = (ServerEventStore) FieldUtils
        .readDeclaredStaticField(ServerEventDispatcherTask.class, "serverEventStore", true);
    asyncContextMap.clear();
    serverEventStore.clear();
    FieldUtils.writeDeclaredStaticField(AbstractServerEvent.class, "idCounter", 0L, true);
    mockedIgnoredAsyncContext = mock(AsyncContext.class);

    new SseLogger().init();
  }

  ServerEvent newMockedServerEvent(String name, String data) {
    ServerEvent mock = spy(AbstractServerEvent.class);
    when(mock.getName()).thenReturn(() -> name);
    when(mock.getData(anyString(), any(User.class))).thenReturn(data);
    return mock;
  }

  List<ServerEvent> getStoredServerEvents() {
    return serverEventStore.getFromId(-1);
  }

  SilverpeasAsyncContext4Test newMockedAsyncContext(final String sessionId) {
    final SilverpeasAsyncContext4Test context4test = new SilverpeasAsyncContext4Test(
        mockedIgnoredAsyncContext, sessionId, new UserDetail());
    context4test.request4Test.setRequestURI(EVENT_SOURCE_REQUEST_URI);
    return context4test;
  }

  String getSentServerEventStream(final SilverpeasAsyncContext mockedAsyncContext)
      throws IOException {
    return getSentServerEventStream(mockedAsyncContext, 1);
  }

  String getSentServerEventStream(final SilverpeasAsyncContext mockedAsyncContext,
      final int nbPerform) throws IOException {
    final SilverpeasAsyncContext4Test testContext = (SilverpeasAsyncContext4Test) mockedAsyncContext;
    assertThat(testContext.getNbIsPossibleCalls(), greaterThanOrEqualTo(nbPerform));
    assertThat(testContext.getNbGetRequestCalls(), greaterThanOrEqualTo(nbPerform));
    assertThat(testContext.getNbGetResponseCalls(), greaterThanOrEqualTo(nbPerform));
    assertThat(testContext.response4Test.getNbGetWriterCalls(), greaterThanOrEqualTo(nbPerform));
    assertThat(testContext.response4Test.getWriter().getNbFlushCalls(), greaterThanOrEqualTo(nbPerform));
    StringBuilder result = new StringBuilder();
    testContext.response4Test.printer.getAppendedValues().forEach(result::append);
    return result.toString();
  }

  void verifyNotSent(final SilverpeasAsyncContext mockedAsyncContext) {
    final SilverpeasAsyncContext4Test testContext = (SilverpeasAsyncContext4Test) mockedAsyncContext;
    assertThat(testContext.getNbIsPossibleCalls(), greaterThanOrEqualTo(1));
    assertThat(testContext.getNbGetRequestCalls(), is(0));
    assertThat(testContext.getNbGetResponseCalls(), is(0));
    assertThat(testContext.response4Test.getNbGetWriterCalls(), is(0));
  }

  void afterSomeTimesCheck(final ThrowingRunnable assertions) {
    with().pollInterval(400, TimeUnit.MILLISECONDS)
        .await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(assertions);
  }

  static class SilverpeasAsyncContext4Test extends SilverpeasAsyncContext {

    private final static HttpServletRequest reqMock = mock(HttpServletRequest.class);
    private final static HttpServletResponse resMock = mock(HttpServletResponse.class);

    private final HttpRequest4Test request4Test = new HttpRequest4Test(reqMock);
    private final HttpResponse4Test response4Test = new HttpResponse4Test(resMock);
    private final AtomicInteger nbIsPossibleCalls = new AtomicInteger(0);
    private final AtomicInteger nbGetRequestCalls = new AtomicInteger(0);
    private final AtomicInteger nbGetResponseCalls = new AtomicInteger(0);
    private boolean isSendPossible = true;

    SilverpeasAsyncContext4Test(final AsyncContext wrappedInstance, final String sessionId,
        final User user) {
      super(SilverpeasAsyncContextManager.get(), wrappedInstance, sessionId, user);
    }

    void markSendIsNotPossible() {
      isSendPossible = false;
    }

    @Override
    public boolean isSendPossible() {
      nbIsPossibleCalls.addAndGet(1);
      return isSendPossible;
    }

    @Override
    public HttpRequest4Test getRequest() {
      nbGetRequestCalls.addAndGet(1);
      return request4Test;
    }

    @Override
    public HttpResponse4Test getResponse() {
      nbGetResponseCalls.addAndGet(1);
      return response4Test;
    }

    public int getNbIsPossibleCalls() {
      return nbIsPossibleCalls.get();
    }

    public int getNbGetRequestCalls() {
      return nbGetRequestCalls.get();
    }

    public int getNbGetResponseCalls() {
      return nbGetResponseCalls.get();
    }
  }

  static class HttpRequest4Test extends HttpServletRequestWrapper {
    private String requestUri;

    public HttpRequest4Test(final HttpServletRequest request) {
      super(request);
    }

    @Override
    public String getRequestURI() {
      return requestUri;
    }

    public void setRequestURI(final String requestUri) {
      this.requestUri = requestUri;
    }
  }

  static class HttpResponse4Test extends HttpServletResponseWrapper {

    private final PrintWriter4Test printer = new PrintWriter4Test();
    private final AtomicInteger nbGetWriterCalls = new AtomicInteger(0);

    public HttpResponse4Test(final HttpServletResponse response) {
      super(response);
    }

    @Override
    public PrintWriter4Test getWriter() throws IOException {
      nbGetWriterCalls.addAndGet(1);
      return printer;
    }

    public int getNbGetWriterCalls() {
      return nbGetWriterCalls.get();
    }
  }

  static class PrintWriter4Test extends PrintWriter {
    private final List<CharSequence> appends = new LinkedList<>();
    private final AtomicInteger nbFlushCalls = new AtomicInteger(0);
    public PrintWriter4Test() {
      super(new ByteArrayOutputStream());
    }

    @Override
    public PrintWriter4Test append(final CharSequence csq) {
      appends.add(csq);
      return this;
    }

    @Override
    public void flush() {
      nbFlushCalls.addAndGet(1);
      IntStream.range(0, 100000).forEach(i -> {});
    }

    public List<CharSequence> getAppendedValues() {
      return appends;
    }

    public int getNbFlushCalls() {
      return nbFlushCalls.get();
    }
  }
}