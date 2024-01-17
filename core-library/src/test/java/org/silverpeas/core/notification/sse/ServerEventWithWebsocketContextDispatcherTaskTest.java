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
class ServerEventWithWebSocketContextDispatcherTaskTest extends AbstractServerEventDispatcherTaskTest {

  private final String SESSION_ID = "SESSION_ID";

  @Test
  void handleEventWithEmptyDataWhenNoWebSocketContext() {
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(serverEventContextMap.size(), is(0));
      assertThat(getStoredServerEvents(), hasSize(1));
      verify(mockedServerEvent, atLeast(1)).getId();
    });
  }

  @Test
  void handleEventWithNullDataAndNullEventNameWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent = newMockedServerEvent(null, null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream, is("{\"name\":\"\",\"id\":0,\"data\":\"\"}"));
    });
  }

  @Test
  void handleEventWithNullDataWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream, is("{\"name\":\"EVENT_NAME\",\"id\":0,\"data\":\"\"}"));
    });
  }

  @Test
  void handleEventWithEmptyDataAndEmptyEventNameWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream, is("{\"name\":\"\",\"id\":0,\"data\":\"\"}"));
    });
  }

  @Test
  void unregisterBySessionIdShouldWork() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.registerContext(newMockedWebSocketContext(SESSION_ID));
    assertThat(serverEventContextMap.size(), is(2));
    ServerEventDispatcherTask.registerContext(newMockedWebSocketContext("OTHER_SESSION_ID"));
    assertThat(serverEventContextMap.size(), is(3));
    ServerEventDispatcherTask.unregisterBySessionId(SESSION_ID);
    assertThat(serverEventContextMap.size(), is(1));
    ServerEventDispatcherTask.unregisterBySessionId("OTHER_SESSION_ID");
    assertThat(serverEventContextMap.size(), is(0));
  }

  @Test
  void handleEventWithDataOnOneLineWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "This is a line of data");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream,
          is("{\"name\":\"EVENT_ONE_LINE\",\"id\":0,\"data\":\"This is a line of data\"}"));
    });
  }

  @Test
  void handleEventWithDataOnOneLineWhenOneWebSocketContextButClosed() {
    final SilverpeasWebSocketContext4Test mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    mockedWebSocketContext.markSendIsNotPossible();
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "This is a line of data");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      verifyNotSent(mockedWebSocketContext);
    });
  }

  @Test
  void handleEventWithDataOnSeveralLinesWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "Line 1\nLine 2\n\n\nLine 3");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream,
          is("{\"name\":\"EVENT_ONE_LINE\",\"id\":0,\"data\":\"Line 1\\nLine 2\\n\\n\\nLine 3\"}"));
    });
  }

  @Test
  void handleEventWithJsonDataOnOneLineWhenOneWebSocketContext() {
    final SilverpeasWebSocketContext mockedWebSocketContext = newMockedWebSocketContext(SESSION_ID);
    ServerEventDispatcherTask.registerContext(mockedWebSocketContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_ONE_LINE", JSONCodec.encodeObject(
        jsonObject -> jsonObject.put("unitTest", true)
            .putJSONArray("array", jsonArray -> jsonArray.add("unit").add("test"))));
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerMessage(mockedWebSocketContext);
      assertThat(eventStream,
          is("{\"name\":\"EVENT_ONE_LINE\",\"id\":0," +
              "\"data\":\"{\\\"unitTest\\\":true,\\\"array\\\":[\\\"unit\\\",\\\"test\\\"]}\"}"));
    });
  }
}