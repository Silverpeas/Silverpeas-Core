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
package org.silverpeas.core.contribution.attachment.notification;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.notification.system.JMSResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
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
public class AttachmentEventNotifier
    extends JMSResourceEventNotifier<SimpleDocument, AttachmentEvent> {

  public static AttachmentEventNotifier getNotifier() {
    return ServiceProvider.getService(AttachmentEventNotifier.class);
  }

  @Resource(lookup = "java:/topic/attachments")
  private Topic topic;

  private AttachmentEventNotifier() {
  }

  @Override
  protected Destination getDestination() {
    return topic;
  }

  @Override
  protected AttachmentEvent createResourceEventFrom(final ResourceEvent.Type type,
      final SimpleDocument... resource) {
    return new AttachmentEvent(type, resource);
  }
}
