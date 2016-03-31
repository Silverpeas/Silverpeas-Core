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

package org.silverpeas.core.notification.system;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * A synchronous event notifier using the notification bus of CDI. This bus is based on the
 * Observer pattern but by using the annotations in place of code lines to setup listeners and so
 * on. All synchronous notifiers should extend this class; they have just to implement the
 * {@code createResourceEventFrom} method, the notification itself is performed by this abstract
 * class.
 * @param <T> the type of the resource event.
 * @author mmoquillon
 */
public abstract class CDIResourceEventNotifier<R extends Serializable, T extends AbstractResourceEvent>
    implements ResourceEventNotifier<R, T> {

  @Inject
  private Event<T> notification;

  protected abstract T createResourceEventFrom(final ResourceEvent.Type type, final R... resource);

  @Override
  public final void notify(final T event) {
    notification.fire(event);
  }

  @Override
  public final void notifyEventOn(final ResourceEvent.Type type, final R... resource) {
    notify(createResourceEventFrom(type, resource));
  }
}
