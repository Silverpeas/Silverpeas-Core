/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.JSONCodec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
public class ServerEventDispatcherTaskTest extends AbstractServerEventDispatcherTaskTest {

  private final String SESSION_ID = "SESSION_ID";

  @Test
  public void handleEventWithEmptyDataWhenNoAsyncContext() throws Exception {
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(asyncContextMap.size(), is(0));
      assertThat(getStoredServerEvents(), hasSize(1));
      verify(mockedServerEvent, atLeast(1)).getId();
    });
  }

  @Test
  public void handleEventWithNullDataAndNullEventNameWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent(null, null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\ndata: \n\n"));
    });
  }

  @Test
  public void handleEventWithNullDataWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("EVENT_NAME", null);
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_NAME\ndata: \n\n"));
    });
  }

  @Test
  public void handleEventWithEmptyDataAndEmptyEventNameWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    ServerEvent mockedServerEvent = newMockedServerEvent("", "");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\ndata: \n\n"));
    });
  }

  @Test
  public void unregisterBySessionIdShouldWork() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    assertThat(asyncContextMap.size(), is(1));
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    assertThat(asyncContextMap.size(), is(1));
    ServerEventDispatcherTask.registerAsyncContext(newMockedAsyncContext(SESSION_ID));
    assertThat(asyncContextMap.size(), is(2));
    ServerEventDispatcherTask.registerAsyncContext(newMockedAsyncContext("OTHER_SESSION_ID"));
    assertThat(asyncContextMap.size(), is(3));
    ServerEventDispatcherTask.unregisterBySessionId(SESSION_ID);
    assertThat(asyncContextMap.size(), is(1));
    ServerEventDispatcherTask.unregisterBySessionId("OTHER_SESSION_ID");
    assertThat(asyncContextMap.size(), is(0));
  }

  @Test
  public void handleEventWithDataOnOneLineWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
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
  public void handleEventWithDataOnOneLineWhenOneAsyncContextButClosed() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    when(mockedAsyncContext.isSendPossible()).thenReturn(false);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    ServerEvent mockedServerEvent =
        newMockedServerEvent("EVENT_ONE_LINE", "This is a line of data");
    ServerEventDispatcherTask.dispatch(mockedServerEvent);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(mockedServerEvent));
      verifyNotSent(mockedAsyncContext);
    });
  }

  @Test
  public void handleEventWithDataOnSeveralLinesWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
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
  public void handleEventWithJsonDataOnOneLineWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext(SESSION_ID);
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
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