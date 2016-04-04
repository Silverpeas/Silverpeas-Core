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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.usernotification;

import com.silverpeas.jms.JMSTestFacade;
import javax.inject.Named;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.usernotification.NotificationTopic.*;

/**
 * Unit test on the subscription to a topic for receiving notifications.
 */
public class NotificationSubscriptionTest extends NotificationServiceTest {

  @Inject
  private NotificationPublisher publisher;
  @Inject
  @Named("myNotificationSubscriber")
  private NotificationSubscriber subscriber;

  public NotificationSubscriptionTest() {
  }

  @Before
  public void setUp() throws Exception {
    assertNotNull(subscriber);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void theSubscriptionToAnExistingTopicShouldSucceed() {
    subscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
    subscriber.unsubscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
  }

  @Test
  public void theSubscriptionASecondTimeToAnExistingTopicShouldDoNothing() {
    subscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
    subscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
    subscriber.unsubscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
  }

  @Test(expected=SubscriptionException.class)
  public void theSubscriptionToAnUnexistingTopicShouldFailed() {
    subscriber.subscribeForNotifications(onTopic("unknown"));
  }

  @Test
  public void unsubscribeToANonSubscribedTopicShouldDoNothing() {
    subscriber.unsubscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
  }

  @Test
  public void aSubscribedListenerShouldReceivePublishedEvents() {
    subscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));

    MyResource resource = new MyResource("toto");
    NotificationSource sender = new NotificationSource().withComponentInstanceId("toto1").withUserId(
      "simpson");
    SilverpeasNotification expectedNotif = new SilverpeasNotification(sender,
      resource);
    publisher.publish(expectedNotif, onTopic(JMSTestFacade.DEFAULT_TOPIC));

    SilverpeasNotification receivedNotif = ((MyNotificationSubscriber) subscriber).
      getReceivedNotification();
    assertThat(receivedNotif.getObject(), is(expectedNotif.getObject()));
    assertThat(receivedNotif.getSource(), is(expectedNotif.getSource()));
  }
}
