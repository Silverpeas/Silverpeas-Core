/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * This interface defines the management of {@link SilverpeasServerEventContext}.
 * <p>
 *   When a WEB client requests for Server Events, this client context MUST be registered by the
 *   {@link SilverpeasServerEventContextManager} instance.
 * </p>
 * <p>
 *   After registration, WEB client will be able to be notified about {@link ServerEvent} pushes.
 * </p>
 * <p>
 *   This manager takes in charge concurrency problematics.
 * </p>
 * @author silveryocha
 */
public interface SilverpeasServerEventContextManager {

  static SilverpeasServerEventContextManager get() {
    return ServiceProvider.getSingleton(SilverpeasServerEventContextManager.class);
  }

  /**
   * Register safely the given {@link SilverpeasServerEventContext} instance.
   * @param context the {@link SilverpeasServerEventContext} instance to register.
   */
  void register(final SilverpeasServerEventContext context);

  /**
   * Unregister safely the given {@link SilverpeasServerEventContext} instance.
   * @param context the {@link SilverpeasServerEventContext} instance to unregister.
   */
  void unregister(final SilverpeasServerEventContext context);

  /**
   * Gets safely a snapshot of the current registered asynchronous contexts.
   * @return a list of {@link SilverpeasServerEventContext} instances.
   */
  List<SilverpeasServerEventContext> getContextSnapshot();
}
