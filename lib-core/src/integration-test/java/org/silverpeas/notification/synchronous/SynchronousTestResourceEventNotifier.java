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

import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.notification.ResourceEventNotifier;
import org.silverpeas.notification.util.TestResource;
import org.silverpeas.notification.util.TestResourceEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author mmoquillon
 */
public class SynchronousTestResourceEventNotifier implements
    ResourceEventNotifier<TestResourceEvent> {

  @Inject
  private Event<TestResourceEvent> event;

  @Override
  public void notify(final TestResourceEvent resourceEvent) {
    event.fire(resourceEvent);
  }

  @Override
  public void notifyEventOn(final ResourceEvent.Type type, final Object resource) {
    notify(new TestResourceEvent(type, (TestResource) resource));
  }
}
