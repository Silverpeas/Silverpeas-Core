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

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * PLEASE NOTICE THAT IT EXISTS TWO LISTENERS...
 * @author Yohann Chastagnier
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({TestServerEventBucket.class, TestServerEventListenerCommon.class,
    TestServerEventListenerA.class})
public class ServerEventListenerPushingServerEventDispatcherTaskTest
    extends AbstractServerEventDispatcherTaskTest {

  @Inject
  TestServerEventBucket bucket;

  @Inject
  TestServerEventNotifierA testServerEventNotifierA;

  @Inject
  CommonServerEventNotifier commonServerEventNotifier;

  @Before
  @After
  public void bucketSetup() {
    bucket.empty();
  }

  @Test
  public void notifyOneServerEventAFromAWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext("SESSION_ID");
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    TestServerEventA serverEventA = new TestServerEventA();
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifierA.notify(serverEventA);
    pause();
    assertThat(lastServerEvents, contains(serverEventA));
    String eventStream = getSentServerEventStream(mockedAsyncContext);
    assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_A\n\n"));
    assertThat(bucket.getServerEvents(), contains(serverEventA, serverEventA));
  }

  @Test
  public void notifyOneServerEventAAndOneBFromAWhenOneAsyncContext() throws Exception {
    final SilverpeasAsyncContext mockedAsyncContext = newMockedAsyncContext("SESSION_ID");
    ServerEventDispatcherTask.registerAsyncContext(mockedAsyncContext);
    TestServerEventA serverEventA = new TestServerEventA();
    TestServerEventB serverEventB = new TestServerEventB();
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifierA.notify(serverEventA);
    SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'B'");
    commonServerEventNotifier.notify(serverEventB);
    pause();
    assertThat(lastServerEvents, contains(serverEventA));
    String eventStream = getSentServerEventStream(mockedAsyncContext);
    assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_A\n\n"));
    assertThat(bucket.getServerEvents(), contains(serverEventA, serverEventA, serverEventB));
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
    commonServerEventNotifier.notify(serverEventB);
    SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
    testServerEventNotifierA.notify(serverEventA);
    pause();
    assertThat(lastServerEvents, contains(serverEventA));
    for (AsyncContext asyncContext : mockedAsyncContexts) {
      String eventStream = getSentServerEventStream(asyncContext);
      assertThat(eventStream, is("retry: 5000\nid: 0\nevent: EVENT_A\ndata: Some data!\n\n"));
    }
    assertThat(bucket.getServerEvents(), contains(serverEventB, serverEventA, serverEventA));
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
      commonServerEventNotifier.notify(serverEventB);
      SilverLogger.getLogger(this).info("'A' NOTIFY - EVENT 'A'");
      testServerEventNotifierA.notify(serverEventA);
      expectedBucketContent.add(serverEventB);
      expectedBucketContent.add(serverEventA);
      expectedBucketContent.add(serverEventA);
      expectedLastServerEvents.add(serverEventA);
      expectedEventStreamForOneAsyncContext.append(HTTP_REQUEST_RESPONSE_SEND_TEMPLATE);
    }
    assertThat(expectedBucketContent, hasSize(nbSend * 3));
    pause();
    assertThat(lastServerEvents, contains(
        expectedLastServerEvents.toArray(new ServerEvent[expectedLastServerEvents.size()])));
    for (AsyncContext asyncContext : mockedAsyncContexts) {
      String eventStream = getSentServerEventStream(asyncContext, nbSend);
      assertThat(eventStream, is(expectedEventStreamForOneAsyncContext.toString()));
    }
    assertThat(bucket.getServerEvents(),
        contains(expectedBucketContent.toArray(new ServerEvent[expectedBucketContent.size()])));
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
      commonServerEventNotifier.notify(serverEventB);
      SilverLogger.getLogger(this).info("'COMMON' NOTIFY - EVENT 'A'");
      commonServerEventNotifier.notify(serverEventA);
      expectedBucketContent.add(serverEventB);
      expectedBucketContent.add(serverEventA);
      expectedBucketContent.add(serverEventA);
      expectedLastServerEvents.add(serverEventA);
      expectedEventStreamForOneAsyncContext.append(format(HTTP_REQUEST_RESPONSE_SEND_TEMPLATE, i));
    }
    assertThat(expectedBucketContent, hasSize(nbSend * 3));
    pause();
    assertThat(lastServerEvents, contains(
        expectedLastServerEvents.toArray(new ServerEvent[expectedLastServerEvents.size()])));
    for (AsyncContext asyncContext : mockedAsyncContexts) {
      String eventStream = getSentServerEventStream(asyncContext, nbSend);
      assertThat(eventStream, is(expectedEventStreamForOneAsyncContext.toString()));
    }
    assertThat(bucket.getServerEvents(),
        contains(expectedBucketContent.toArray(new ServerEvent[expectedBucketContent.size()])));
  }
}