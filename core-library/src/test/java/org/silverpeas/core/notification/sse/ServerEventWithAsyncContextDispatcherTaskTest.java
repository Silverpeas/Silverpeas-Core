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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.JSONCodec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * @author Yohann Chastagnier
 */
class ServerEventWithAsyncContextDispatcherTaskTest extends AbstractServerEventDispatcherTaskTest {

  private final String SESSION_ID = "SESSION_ID";

  @Test
  void handleEventWithEmptyDataWhenNoAsyncContext() {
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(serverEventContextMap.size(), is(0));
      assertThat(getStoredServerEvents(), hasSize(1));
      verify(mockedServerEvent, atLeast(1)).getId();
    });
  }

  @Test
  void handleEventWithNullDataAndNullEventNameWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent(null, null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\ndata: \n\n"));
    });
  }

  @Test
  void handleEventWithNullDataWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_NAME\ndata: \n\n"));
    });
  }

  @Test
  void handleEventWithEmptyDataAndEmptyEventNameWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\ndata: \n\n"));
    });
  }

  @Test
  void unregisterBySessionIdShouldWork() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.registerContext(newMockedAsyncContext(SESSION_ID));
    assertThat(serverEventContextMap.size(), is(2));
    ServerEventDispatcherTask.registerContext(newMockedAsyncContext("OTHER_SESSION_ID"));
    assertThat(serverEventContextMap.size(), is(3));
    ServerEventDispatcherTask.unregisterBySessionId(SESSION_ID);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.unregisterBySessionId("OTHER_SESSION_ID");
    assertThat(serverEventContextMap.size(), is(0));
  }

  @Test
  void handleEventWithDataOnOneLineWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "This is a line of data");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream,
          is("retry: 5000\nid: 0\nevent: EVENT_ONE_LINE\ndata: This is a line of data\n\n"));
    });
  }

  @Test
  void handleEventWithDataOnOneLineWhenOneAsyncContextButClosed() {
    final SilverpeasAsyncContext4Test mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    mockedAsyncContext.markSendIsNotPossible();
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "This is a line of data");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      verifyNotSent(mockedAsyncContext);
    });
  }

  @Test
  void handleEventWithDataOnSeveralLinesWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "Line 1\nLine 2\n\n\nLine 3");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_ONE_LINE\n" +
          "data: Line 1\ndata: Line 2\ndata: \ndata: \ndata: Line 3\n\n"));
    });
  }

  @Test
  void handleEventWithJsonDataOnOneLineWhenOneAsyncContext() {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_ONE_LINE", JSONCodec.encodeObject(
        jsonObject -> jsonObject.put("unitTest", true)
            .putJSONArray("array", jsonArray -> jsonArray.add("unit").add("test"))));
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_ONE_LINE\n" +
          "data: {\"unitTest\":true,\"array\":[\"unit\",\"test\"]}\n\n"));
    });
  }
}