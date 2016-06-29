/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.sse;

import org.silverpeas.core.util.logging.SilverLogger;

import javax.enterprise.event.Observes;

/**
 * A synchronous {@link ServerEvent} listener using the notification bus of CDI. This bus is
 * based on the Observer pattern but by using the annotations in place of code lines to setup
 * listeners and so on.
 * <p>
 * Synchronous events are carried within a specific CDI event and are collected by this
 * abstract class. All concrete listeners have just to extend this abstract class and to do
 * some parametrization.
 * @author Yohann Chastagnier
 */
public abstract class CDIServerEventListener<T extends AbstractServerEvent>
    implements ServerEventListener<T> {

  protected final SilverLogger logger = SilverLogger.getLogger(this);

  public void onEvent(@Observes T event) throws Exception {
    on(event);
  }
}
