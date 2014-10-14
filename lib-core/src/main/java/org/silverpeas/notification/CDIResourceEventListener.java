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

package org.silverpeas.notification;

import javax.enterprise.event.Observes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmoquillon
 */
public abstract class CDIResourceEventListener<T extends ResourceEvent>
    implements ResourceEventListener<T> {

  protected final Logger logger = Logger.getLogger(getClass().getSimpleName());

  /**
   * /**
   * Listens for events related to a resource managed in Silverpeas.
   * <p>
   *   The event is decoded from the specified message and according to the type of the event,
   *   the adequate method is then invoked (
   *   {@code org.silverpeas.notification.CDIResourceEventListener#onCreation(ResourceEvent},
   *   {@code org.silverpeas.notification.CDIResourceEventListener#onUpdate(ResourceEvent},
   *   {@code org.silverpeas.notification.CDIResourceEventListener#onDeletion(ResourceEvent}).
   * </p>
   * @see org.silverpeas.notification.ResourceEventListener#dispatchEvent(ResourceEvent)
   * @param event an event.
   * @throws Exception if the processing of the event fails.
   */
  public void onEvent(@Observes T event) throws Exception {
    dispatchEvent(event);
  }
}
