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

package org.silverpeas.core.admin.component.notification;


import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.notification.system.CDIResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ServiceProvider;

/**
 * A notifier of an event about the life-cycle of a component instance.
 * The event is triggered synchronously, meaning it will block until the treatment of the listeners
 * of such events is done.
 * <p>
 * This class is defined for the unmanaged beans requiring access the services exposed by the
 * underlying IoD bean container.
 * </p>
 * @author mmoquillon
 */
public class ComponentInstanceEventNotifier
    extends CDIResourceEventNotifier<ComponentInst, ComponentInstanceEvent> {

  /**
   * Gets an instance of this event notifier.
   * @return a notifier of events about the life-cycle of component instances.
   */
  public static ComponentInstanceEventNotifier getNotifier() {
    return ServiceProvider.getService(ComponentInstanceEventNotifier.class);
  }

  private ComponentInstanceEventNotifier() {
  }

  @Override
  protected ComponentInstanceEvent createResourceEventFrom(final ResourceEvent.Type type,
      final ComponentInst... resource) {
    return new ComponentInstanceEvent(type, resource);
  }

}
