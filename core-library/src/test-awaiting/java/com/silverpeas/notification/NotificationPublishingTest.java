/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.usernotification;

import javax.inject.Inject;

import com.silverpeas.jms.JMSTestFacade;
import com.silverpeas.usernotification.jms.JMSPublishingService;

import com.mockrunner.mock.jms.MockMessage;
import com.mockrunner.mock.jms.MockObjectMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.silverpeas.usernotification.NotificationTopic.onTopic;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test on the publishing of a notification.
 */
public class NotificationPublishingTest extends NotificationServiceTest {

  @Inject
  private NotificationPublisher eventPublisher;

  public NotificationPublishingTest() {
  }

  @Before
  public void setUp() throws Exception {
    assertNotNull(eventPublisher);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void eventPublisherFactoryReturnsTheCorrectEventPublisherImpl() {
    NotificationPublisherFactory publisherFactory = NotificationPublisherFactory.getFactory();
    NotificationPublisher publisher = publisherFactory.getNotificationPublisher();
    assertNotNull(publisher);
    assertTrue(publisher instanceof JMSPublishingService);
  }

  @Test
  public void publishAnEventToAnExistingTopicShouldSucceed() throws Exception {
    NotificationSource source = new NotificationSource().withUserId("simpson").
        withComponentInstanceId("toto1");
    MyResource resource = new MyResource("toto");
    SilverpeasNotification expectedNotif = new SilverpeasNotification(source,
        resource);
    //expectedNotif.addParameter("test", "true");
    eventPublisher.publish(expectedNotif, onTopic(JMSTestFacade.DEFAULT_TOPIC));
    MockMessage msg = new MockObjectMessage(expectedNotif);
    getJMSTestFacade().verifyReceivedTopicMessageEquals(JMSTestFacade.DEFAULT_TOPIC, 0, msg);
  }

  @Test(expected = PublishingException.class)
  public void publishAnEventToAnUnexistingTopicShouldSucceed() throws Exception {
    NotificationSource source = new NotificationSource().withUserId("simpson").
        withComponentInstanceId("toto1");
    MyResource resource = new MyResource("coucou");
    SilverpeasNotification expectedNotif = new SilverpeasNotification(source,
        resource);
    eventPublisher.publish(expectedNotif, onTopic("unknown"));
  }
}
