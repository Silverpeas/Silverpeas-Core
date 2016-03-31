/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.notification.system.asynchronous;

import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.system.TestResource;
import org.silverpeas.core.notification.system.TestResourceEvent;
import org.silverpeas.core.notification.system.TestResourceEventBucket;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the asynchronous mode of the Silverpeas API Notification.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class AsynchronousNotificationIntegrationTest {

  private enum ADDITIONAL_PARAMETER {
    NONE, WITH_VALUE, WITHOUT_VALUE
  }

  @Inject
  private JMSQueueTestResourceEventNotifier queueNotifier;

  @Inject
  private JMSTopicTestResourceEventNotifier topicNotifier;

  @Inject
  private TestResourceEventBucket bucket;

  @Inject
  private Event<TestResourceEvent> notification;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AsynchronousNotificationIntegrationTest.class)
        .addSubscriptionFeatures()
        .testFocusedOn((war) -> {
          WarBuilder4LibCore warBuilder = ((WarBuilder4LibCore) war);
          warBuilder.addSynchAndAsynchResourceEventFeatures();
          warBuilder.addClasses(TestResource.class, TestResourceEvent.class,
              TestResourceEventBucket.class, JMSQueueTestResourceEventNotifier.class,
              JMSQueueTestResourceEventListener.class, JMSTopicTestResourceEventNotifier.class,
              JMSTopicTestResourceEventListener.class, JMSTopicTestResourceEventListener2.class,
              UserSubscriptionNotificationSendingHandler.class);
        }).build();
  }

  @Before
  public void checkInjection() {
    assertThat(queueNotifier, notNullValue());
    assertThat(bucket, notNullValue());
  }

  @After
  public void emptyBucket() {
    bucket.empty();
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
  }

  @Test
  public void asynchronousWithQueueNotificationShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.NONE);

    queueNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(1, event, additionalParameter);
  }

  @Test
  public void asynchronousWithTopicsNotificationShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.WITHOUT_VALUE);

    topicNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(2, event, additionalParameter);
  }

  @Test
  public void asynchronousNotificationFromSynchronousOneShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.WITH_VALUE);

    notification.fire(event);

    waitForReception();
    assertThatEventIsWellReceived(3, event, additionalParameter);
  }

  @Test
  public void asynchronousNotificationOnCreationShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.NONE);

    queueNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(1, event, additionalParameter);
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), nullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void asynchronousNotificationOnUpdateShouldWork() throws InterruptedException {
    TestResourceEvent event =
        new TestResourceEvent(ResourceEvent.Type.UPDATE, aTestResource(), aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.WITH_VALUE);

    queueNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(1, event, additionalParameter);
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void asynchronousNotificationOnRemovingShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.REMOVING, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.WITHOUT_VALUE);

    queueNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(1, event, additionalParameter);
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void asynchronousNotificationOnDeletionShouldWork() throws InterruptedException {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.DELETION, aTestResource());
    ADDITIONAL_PARAMETER additionalParameter =
        setupAdditionalParameter(event, ADDITIONAL_PARAMETER.WITH_VALUE);

    queueNotifier.notify(event);

    waitForReception();
    assertThatEventIsWellReceived(1, event, additionalParameter);
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), nullValue());
  }

  @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
  public void asynchronousNotificationOnUpdateWithAMissingArgumentShouldFail() {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.UPDATE, aTestResource());

    queueNotifier.notify(event);
  }

  private TestResource aTestResource() {
    Date now = Date.from(Instant.now());
    return new TestResource("42", "Toto Chez-les-Papoos", now);
  }

  private ADDITIONAL_PARAMETER setupAdditionalParameter(TestResourceEvent event,
      ADDITIONAL_PARAMETER additionalParameter) {
    switch (additionalParameter) {
      case WITH_VALUE:
        event.putParameter("aParameterName", "anyValue");
        break;
      case WITHOUT_VALUE:
        event.putParameter("aParameterName", null);
        break;
      default:
    }
    return additionalParameter;
  }

  private void waitForReception() throws InterruptedException {
    Thread.sleep(1000);
  }

  private void assertThatEventIsWellReceived(int count, TestResourceEvent event,
      final ADDITIONAL_PARAMETER additionalParameter) {
    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(count));
    assertThat(bucket.getContent().contains(event), is(true));
    for (TestResourceEvent resourceEvent : bucket.getContent()) {
      switch (additionalParameter) {
        case WITH_VALUE:
          assertThat(resourceEvent.getParameters(), hasSize(1));
          assertThat(resourceEvent.getParameters(), hasItem("aParameterName"));
          assertThat(resourceEvent.getParameterValue("aParameterName"), is("anyValue"));
          break;
        case WITHOUT_VALUE:
          assertThat(resourceEvent.getParameters(), hasSize(1));
          assertThat(resourceEvent.getParameters(), hasItem("aParameterName"));
          assertThat(resourceEvent.getParameterValue("aParameterName"), nullValue());
          break;
        default:
          assertThat(resourceEvent.getParameters(), empty());
      }
    }
  }
}
