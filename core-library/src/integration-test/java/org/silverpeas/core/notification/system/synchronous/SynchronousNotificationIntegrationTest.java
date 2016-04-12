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

package org.silverpeas.core.notification.system.synchronous;

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

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the synchronous mode Silverpeas API Notification.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SynchronousNotificationIntegrationTest {

  @Inject
  private SynchronousTestResourceEventNotifier notifier;

  @Inject
  private TestResourceEventBucket bucket;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SynchronousNotificationIntegrationTest.class)
        .addSynchAndAsynchResourceEventFeatures()
        .addClasses(TestResource.class, TestResourceEvent.class, TestResourceEventBucket.class)
        .build();
  }

  @Before
  public void checkInjection() {
    assertThat(notifier, notNullValue());
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
  public void synchronousNotificationShouldWork() {
    TestResourceEvent event =
        new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());

    notifier.notify(event);

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().contains(event), is(true));
  }

  @Test
  public void synchronousNotificationOnCreationShouldWork() {
    notifier.notifyEventOn(ResourceEvent.Type.CREATION, aTestResource());

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().get(0).getType(), is(ResourceEvent.Type.CREATION));
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), nullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void synchronousNotificationOnUpdateShouldWork() {
    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, aTestResource(), aTestResource());

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().get(0).getType(), is(ResourceEvent.Type.UPDATE));
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void synchronousNotificationOnRemovingShouldWork() {
    notifier.notifyEventOn(ResourceEvent.Type.REMOVING, aTestResource());

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().get(0).getType(), is(ResourceEvent.Type.REMOVING));
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), notNullValue());
  }

  @Test
  public void synchronousNotificationOnDeletionShouldWork() {
    notifier.notifyEventOn(ResourceEvent.Type.DELETION, aTestResource());

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().get(0).getType(), is(ResourceEvent.Type.DELETION));
    assertThat(bucket.getContent().get(0).getTransition().getBefore(), notNullValue());
    assertThat(bucket.getContent().get(0).getTransition().getAfter(), nullValue());
  }

  @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
  public void synchronousNotificationOnUpdateWithAMissingArgumentShouldFail() {
    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, aTestResource());
  }

  private TestResource aTestResource() {
    Date now = Date.from(Instant.now());
    return new TestResource("42", "Toto Chez-les-Papoos", now);
  }
}
