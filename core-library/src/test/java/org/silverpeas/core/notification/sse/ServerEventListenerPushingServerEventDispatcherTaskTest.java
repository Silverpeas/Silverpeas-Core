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

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * PLEASE NOTICE THAT IT EXISTS TWO LISTENERS...
 * @author Yohann Chastagnier
 */
@EnableAutoWeld
@AddBeanClasses(DefaultServerEventNotifier.class)
@AddPackages(AbstractServerEventDispatcherTaskTest.class)
public class ServerEventListenerPushingServerEventDispatcherTaskTest
    extends AbstractServerEventDispatcherTaskTest {

  @Inject
  TestServerEventBucket bucket;

  @Inject
  TestServerEventNotifier testServerEventNotifier;

  @Inject
  DefaultServerEventNotifier defaultServerEventNotifier;

  @BeforeEach
  @AfterEach
  public void bucketSetup() throws Exception {
    bucket.empty();
  }

  @Test
  public void notifyOneServerEventAFromAWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext("SESSION_ID");
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    TestServerEventA serverEventA = new TestServerEventA();
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifier.notify(serverEventA);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(serverEventA));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_A\ndata: \n\n"));
      assertThat(bucket.getServerEvents(), contains(serverEventA, serverEventA));
    });
  }

  @Test
  public void notifyTwoATwoBTwoCTwoDFromAWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext("SESSION_ID");
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    TestServerEventA serverEventA = new TestServerEventA();
    TestServerEventB serverEventB = new TestServerEventB();
    TestServerEventCNotHandled serverEventC = new TestServerEventCNotHandled();
    TestServerEventDEventSourceURI serverEventD = new TestServerEventDEventSourceURI();
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifier.notify(serverEventA);
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifier.notify(serverEventA);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
    defaultServerEventNotifier.notify(serverEventB);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
    defaultServerEventNotifier.notify(serverEventB);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'C' (not handled)");
    defaultServerEventNotifier.notify(serverEventC);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'C' (not handled)");
    defaultServerEventNotifier.notify(serverEventC);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'D' (event source URI)");
    defaultServerEventNotifier.notify(serverEventD);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'D' (event source URI)");
    defaultServerEventNotifier.notify(serverEventD);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(serverEventA, serverEventA, serverEventC));
      String eventStream = getSentServerEventStream(mockedAsyncContext);
      assertThat(eventStream,
          is("retry: 5000\nid: 0\nevent: EVENT_A\ndata: \n\nretry: 5000\nid: 0\nevent: " +
              "EVENT_A\ndata: \n\nretry: 5000\nid: 2\nevent: EVENT_D\ndata: \n\nretry: 5000\nid: " +
              "2\nevent: EVENT_D\ndata: \n\n"));
      assertThat(bucket.getServerEvents(),
          contains(serverEventA, serverEventA, serverEventA, serverEventA, serverEventB, serverEventB,
              serverEventC, serverEventC, serverEventC, serverEventC, serverEventD, serverEventD,
              serverEventD, serverEventD));
    });
  }

  @Test
  public void notifyOneServerEventBAndOneAFromAWhenSeveralAsyncContext() throws Exception {
    SilverpeasAsyncContext[] mockedAsyncContexts =
        {newMockedAsyncContext("SESSION_ID"), newMockedAsyncContext("SESSION_ID"),
            newMockedAsyncContext("SESSION_ID")};
    for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
      ServerEventDispatcherTask.registerAsyncContext(asyncContext);
    }
    TestServerEventA serverEventA = new TestServerEventA().withData("Some data!");
    TestServerEventB serverEventB = new TestServerEventB();
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
    defaultServerEventNotifier.notify(serverEventB);
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifier.notify(serverEventA);
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(serverEventA));
      for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
        String eventStream = getSentServerEventStream(asyncContext);
        assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_A\ndata: Some data!\n\n"));
      }
      assertThat(bucket.getServerEvents(), contains(serverEventB, serverEventA, serverEventA));
    });
  }

  @Test
  public void notifySeveralServerEventsBAndSeveralAFromAWhenSeveralAsyncContext() throws Exception {
    SilverpeasAsyncContext[] mockedAsyncContexts =
        {newMockedAsyncContext("SESSION_ID"), newMockedAsyncContext("SESSION_ID"),
            newMockedAsyncContext("SESSION_ID")};
    for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
      ServerEventDispatcherTask.registerAsyncContext(asyncContext);
    }
    TestServerEventA serverEventA = new TestServerEventA().withData("Some data!");
    TestServerEventB serverEventB = new TestServerEventB();

    List<ServerEvent> expectedBucketContent = new ArrayList<>();
    List<ServerEvent> expectedLastServerEvents = new ArrayList<>();
    StringBuilder expectedEventStreamForOneAsyncContext = new StringBuilder();
    final String HTTP_REQUEST_RESPONSE_SEND_TEMPLATE =
        "retry: 5000\nid: 0\nevent: EVENT_A\ndata: Some data!\n\n";
    final int nbSend = 10;
    for (int i = 0; i < nbSend; i++) {
      SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
      defaultServerEventNotifier.notify(serverEventB);
      SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
      testServerEventNotifier.notify(serverEventA);
      expectedBucketContent.add(serverEventB);
      expectedBucketContent.add(serverEventA);
      expectedBucketContent.add(serverEventA);
      expectedLastServerEvents.add(serverEventA);
      expectedEventStreamForOneAsyncContext.append(HTTP_REQUEST_RESPONSE_SEND_TEMPLATE);
    }
    assertThat(expectedBucketContent, hasSize(nbSend * 3));
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(
          expectedLastServerEvents.toArray(new ServerEvent[expectedLastServerEvents.size()])));
      for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
        String eventStream = getSentServerEventStream(asyncContext, nbSend);
        assertThat(eventStream, is(expectedEventStreamForOneAsyncContext.toString()));
      }
      assertThat(bucket.getServerEvents(),
          contains(expectedBucketContent.toArray(new ServerEvent[expectedBucketContent.size()])));
    });
  }

  @Test
  public void notifySeveralServerEventsOnCommonNotifierWhenSeveralAsyncContext() throws Exception {
    SilverpeasAsyncContext[] mockedAsyncContexts =
        {newMockedAsyncContext("SESSION_ID"), newMockedAsyncContext("SESSION_ID"),
            newMockedAsyncContext("SESSION_ID")};
    for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
      ServerEventDispatcherTask.registerAsyncContext(asyncContext);
    }

    List<ServerEvent> expectedBucketContent = new ArrayList<>();
    List<ServerEvent> expectedLastServerEvents = new ArrayList<>();
    StringBuilder expectedEventStreamForOneAsyncContext = new StringBuilder();
    final String HTTP_REQUEST_RESPONSE_SEND_TEMPLATE =
        "retry: 5000\nid: {0}\nevent: EVENT_A\ndata: Data \ndata:  A {0}\n\n";
    final int nbSend = 10;
    for (int i = 0; i < nbSend; i++) {
      TestServerEventA serverEventA = new TestServerEventA().withData("Data \n A " + i);
      TestServerEventB serverEventB = new TestServerEventB().withData("Data B " + i);
      SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
      defaultServerEventNotifier.notify(serverEventB);
      SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'A'");
      defaultServerEventNotifier.notify(serverEventA);
      expectedBucketContent.add(serverEventB);
      expectedBucketContent.add(serverEventA);
      expectedBucketContent.add(serverEventA);
      expectedLastServerEvents.add(serverEventA);
      expectedEventStreamForOneAsyncContext.append(format(HTTP_REQUEST_RESPONSE_SEND_TEMPLATE, i));
    }
    assertThat(expectedBucketContent, hasSize(nbSend * 3));
    afterSomeTimesCheck(() -> {
      assertThat(getStoredServerEvents(), contains(
          expectedLastServerEvents.toArray(new ServerEvent[expectedLastServerEvents.size()])));
      for (SilverpeasAsyncContext asyncContext : mockedAsyncContexts) {
        String eventStream = getSentServerEventStream(asyncContext, nbSend);
        assertThat(eventStream, is(expectedEventStreamForOneAsyncContext.toString()));
      }
      assertThat(bucket.getServerEvents(),
          contains(expectedBucketContent.toArray(new ServerEvent[expectedBucketContent.size()])));
    });
  }
}