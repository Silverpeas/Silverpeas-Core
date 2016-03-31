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

import org.silverpeas.core.notification.system.JMSResourceEventListener;
import org.silverpeas.core.notification.system.TestResourceEvent;
import org.silverpeas.core.notification.system.TestResourceEventBucket;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * @author mmoquillon
 */
@MessageDriven(name = "ResourceEventListener2", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "topic/resource"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Singleton
public class JMSTopicTestResourceEventListener2 extends JMSResourceEventListener<TestResourceEvent> {

  @Inject
  private TestResourceEventBucket bucket;

  @Override
  protected Class<TestResourceEvent> getResourceEventClass() {
    return TestResourceEvent.class;
  }

  @Override
  public void onDeletion(final TestResourceEvent event) throws Exception {
    SilverLogger.getLogger(this).info("Deletion event reception...");
    this.bucket.pour(event);
  }

  @Override
  public void onRemoving(final TestResourceEvent event) throws Exception {
    SilverLogger.getLogger(this).info("Removing event reception...");
    this.bucket.pour(event);
  }

  @Override
  public void onUpdate(final TestResourceEvent event) throws Exception {
    SilverLogger.getLogger(this).info("Update event reception...");
    this.bucket.pour(event);
  }

  @Override
  public void onCreation(final TestResourceEvent event) throws Exception {
    SilverLogger.getLogger(this).info("Creation event reception...");
    this.bucket.pour(event);
  }

}
