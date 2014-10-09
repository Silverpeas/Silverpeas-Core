/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.attachment.notification;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.notification.ResourceEventNotifier;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSProducer;
import javax.jms.Topic;

/**
 * A service to notify about the events which occurs on attachment. It provides an easy access to
 * the underlying messaging system used in the notification.
 */
@JMSDestinationDefinitions(
    value = {@JMSDestinationDefinition(
        name = "java:/topic/attachments",
        interfaceName = "javax.jms.Topic",
        destinationName = "AttachmentEventNotification")})
public class AttachmentEventNotifier implements ResourceEventNotifier<AttachmentEvent> {

  @Inject
  private JMSContext jms;

  @Resource(lookup = "java:/topic/attachments")
  private Topic topic;

  protected AttachmentEventNotifier() {
  }

  @Override
  public void notify(final AttachmentEvent event) {
    JMSProducer producer = jms.createProducer();
    producer.send(topic, event.toText());
  }

  @Override
  public void notifyEventOn(final ResourceEvent.Type type, final Object resource) {
    notify(new AttachmentEvent(type, (SimpleDocument) resource));
  }
}
