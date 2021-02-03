/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.util.ServiceProvider;

/**
 * A common implementation that extends {@link CDIServerEventNotifier}.<br>
 * Any treatment of Silverpeas has just to use this implementation in order to notify on a server
 * event.
 * @author Yohann Chastagnier
 */
@Bean
public class DefaultServerEventNotifier extends CDIServerEventNotifier<AbstractServerEvent> {

  /**
   * Gets an instance of server event notifier.
   * @return an instance (not a singleton).
   */
  public static DefaultServerEventNotifier get() {
    return ServiceProvider.getService(DefaultServerEventNotifier.class);
  }
}
