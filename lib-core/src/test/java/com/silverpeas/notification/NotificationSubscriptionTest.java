/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification;

import javax.inject.Named;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.notification.NotificationTopic.*;

/**
 * Unit test on the publishing of a silverpeas event.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-notification.xml")
public class NotificationSubscriptionTest {

  private static JMSTestFacade jmsTestFacade;

  @Inject
  private NotificationPublisher eventPublisher;

  @Inject
  @Named("myNotificationSubscriber")
  private NotificationSubscriber eventSubscriber;

  public NotificationSubscriptionTest() {
  }

  @BeforeClass
  public static void bootstrapJMS() throws Exception {
    jmsTestFacade = new JMSTestFacade();
    jmsTestFacade.bootstrap();
  }

  @AfterClass
  public static void shutdownJMS() throws Exception {
    jmsTestFacade.shutdown();
  }

  @Before
  public void setUp() throws Exception {
    assertNotNull(eventSubscriber);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void theSubscriptionToAnExistingTopicShouldSucceed() {
    eventSubscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
    eventSubscriber.unsubscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
  }

  @Test
  @ExpectedException(SubscriptionException.class)
  public void theSubscriptionToAnUnexistingTopicShouldFailed() {
    eventSubscriber.subscribeForNotifications(onTopic("unknown"));
  }

  @Test
  public void unsubscribeToANonSubscribedTopicShouldDoNothing() {
    eventSubscriber.unsubscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));
  }

  @Test
  public void aSubscribedListenerShouldReceivePublishedEvents() {
    eventSubscriber.subscribeForNotifications(onTopic(JMSTestFacade.DEFAULT_TOPIC));

    NotificationSource sender = new NotificationSource().withComponentInstanceId("toto1").withUserId("simpson");
    SilverpeasNotification<String> expectedEvent = new SilverpeasNotification<String>(sender, "coucou");
    eventPublisher.publish(expectedEvent, onTopic(JMSTestFacade.DEFAULT_TOPIC));

    SilverpeasNotification<String> receivedEvent = ((MyNotificationSubscriber)eventSubscriber).getReceivedEvent();
    assertThat(receivedEvent.getObject(), is(expectedEvent.getObject()));
    assertThat(receivedEvent.getSource(), is(expectedEvent.getSource()));
  }
}
