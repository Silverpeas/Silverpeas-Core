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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.system.TestResource;
import org.silverpeas.core.notification.system.TestResourceEvent;
import org.silverpeas.core.notification.system.TestResourceEventBucket;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.TestStatisticRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the asynchronous mode of the Silverpeas API Notification.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class MassiveAsynchronousNotificationIntegrationTest {

  private enum ADDITIONAL_PARAMETER {
    NONE, WITH_VALUE, WITHOUT_VALUE
  }

  @Rule
  public TestStatisticRule testStatisticRule = new TestStatisticRule();

  @Inject
  private TestResourceEventBucket bucket;

  @Resource
  private ManagedThreadFactory managedThreadFactory;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore
        .onWarForTestClass(MassiveAsynchronousNotificationIntegrationTest.class)
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
    assertThat(getQueueNotifier(), notNullValue());
    assertThat(getTopicNotifier(), notNullValue());
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
  public void asynchronousWithoutConcurrency() throws InterruptedException {
    int nbSend = 100;
    List<TestResourceEvent> registeredEventsBeforeSend = new ArrayList<>();
    ADDITIONAL_PARAMETER additionalParameter = ADDITIONAL_PARAMETER.NONE;
    for (int i = 0; i < nbSend; i++) {
      TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
      setupAdditionalParameter(event, additionalParameter);

      registeredEventsBeforeSend.add(event);

      getQueueNotifier().notify(event);
      getTopicNotifier().notify(event);
    }

    waitForReception(nbSend);
    assertThatEventIsWellReceived(3, registeredEventsBeforeSend, additionalParameter);
  }

  @Test
  public void asynchronousWithConcurrency() throws InterruptedException {
    int nbSend = 100;
    final ConcurrentLinkedQueue<TestResourceEvent> registeredEventsBeforeSend =
        new ConcurrentLinkedQueue<>();
    final ADDITIONAL_PARAMETER additionalParameter = ADDITIONAL_PARAMETER.NONE;
    final List<Thread> threads = new ArrayList<>(nbSend);
    for (int i = 0; i < nbSend; i++) {
      Thread thread = managedThreadFactory.newThread(() -> {
        TestResourceEvent event =
            new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());
        setupAdditionalParameter(event, additionalParameter);

        registeredEventsBeforeSend.add(event);

        getQueueNotifier().notify(event);
        getTopicNotifier().notify(event);
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join(60000);
    }

    waitForReception(nbSend);
    assertThatEventIsWellReceived(3, registeredEventsBeforeSend, additionalParameter);
  }

  public JMSQueueTestResourceEventNotifier getQueueNotifier() {
    return ServiceProvider.getService(JMSQueueTestResourceEventNotifier.class);
  }

  public JMSTopicTestResourceEventNotifier getTopicNotifier() {
    return ServiceProvider.getService(JMSTopicTestResourceEventNotifier.class);
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

  private void waitForReception(int nbSend) throws InterruptedException {
    Thread.sleep(1000 + (nbSend * 10));
  }

  private void assertThatEventIsWellReceived(int nbListeners,
      Collection<TestResourceEvent> expectedEvents,
      final ADDITIONAL_PARAMETER additionalParameter) {
    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(nbListeners * expectedEvents.size()));
    for (TestResourceEvent expectedEvent : expectedEvents) {
      assertThat(bucket.getContent().contains(expectedEvent), is(true));
    }
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
