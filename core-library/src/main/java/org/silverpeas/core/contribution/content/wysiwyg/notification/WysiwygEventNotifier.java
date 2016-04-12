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

package org.silverpeas.core.contribution.content.wysiwyg.notification;

import org.silverpeas.core.contribution.content.wysiwyg.WysiwygContent;
import org.silverpeas.core.notification.system.JMSResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.Topic;

/**
 * A notifier of events about a WYSIWYG content. Currently, the notifications are sent
 * asynchronously.
 * @author mmoquillon
 */
@JMSDestinationDefinitions(
    value = {@JMSDestinationDefinition(
        name = "java:/topic/wysiwyg",
        interfaceName = "javax.jms.Topic",
        destinationName = "WysiwygEventNotification")})
public class WysiwygEventNotifier extends JMSResourceEventNotifier<WysiwygContent, WysiwygEvent> {

  @Resource(lookup = "java:/topic/wysiwyg")
  private Topic topic;

  private WysiwygEventNotifier() {
  }

  @Override
  protected Destination getDestination() {
    return topic;
  }

  @Override
  protected WysiwygEvent createResourceEventFrom(final ResourceEvent.Type type,
      final WysiwygContent... resource) {
    return new WysiwygEvent(type, resource);
  }
}
