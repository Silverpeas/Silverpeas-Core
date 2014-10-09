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

package org.silverpeas.notification.synchronous;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.notification.synchronous.SynchronousTestResourceEventListener;
import org.silverpeas.notification.synchronous.SynchronousTestResourceEventNotifier;
import org.silverpeas.notification.util.TestResource;
import org.silverpeas.notification.util.TestResourceEvent;
import org.silverpeas.notification.util.TestResourceEventBucket;
import org.silverpeas.util.BeanContainer;
import org.silverpeas.util.CDIContainer;
import org.silverpeas.util.JSONCodec;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.DecodingException;
import org.silverpeas.util.exception.EncodingException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
    return ShrinkWrap.create(JavaArchive.class, "test.jar")
        .addClasses(ServiceProvider.class, BeanContainer.class, CDIContainer.class,
            DecodingException.class, EncodingException.class, JSONCodec.class)
        .addClasses(TestResource.class, TestResourceEvent.class, TestResourceEventBucket.class,
            SynchronousTestResourceEventNotifier.class, SynchronousTestResourceEventListener.class)
        .addPackage("org.silverpeas.notification")
        .addAsManifestResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "services/org.silverpeas.util.BeanContainer")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Before
  public void checkInjection() {
    assertThat(notifier, notNullValue());
    assertThat(bucket, notNullValue());
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
  }

  @Test
  public void synchronousNotificationShouldWork() {
    TestResourceEvent event = new TestResourceEvent(ResourceEvent.Type.CREATION, aTestResource());

    notifier.notify(event);

    assertThat(bucket.isEmpty(), is(false));
    assertThat(bucket.getContent().size(), is(1));
    assertThat(bucket.getContent().contains(event), is(true));
  }

  private TestResource aTestResource() {
    Date now = Date.from(Instant.now());
    return new TestResource("42", "Toto Chez-les-Papoos", now);
  }
}
