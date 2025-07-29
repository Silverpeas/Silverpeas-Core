/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.initialization;

import org.silverpeas.kernel.SilverpeasException;

/**
 * This is a markup interface. It qualifies the services that are dedicated to initialize some
 * resources or some others services. The initialization services has to implement the {@link
 * Initialization#init()} method. The {@link  Initialization#release()} method is optional and
 * does nothing by default.
 * @author mmoquillon
 */
public interface Initialization {

  /**
   * Initializes some resources required by the services or performs some initialization processes
   * at Silverpeas startup.
   * @throws SilverpeasException if an error occurs during the initialization. The error won't
   * stop the bootstrap of Silverpeas and will be logged for further investigation.
   */
  void init() throws SilverpeasException;

  /**
   * Gets the priority level of the execution of {@link #init()} method.
   * <p>
   * The less is the value of the priority the more the priority is high.
   * </p>
   * @return an integer priority.
   */
  default int getPriority() {
    return 100;
  }

  /**
   * Releases the previously initialized resources at Silverpeas shutdown. The implementation of
   * this method is optional. By default it does nothing.
   * @throws SilverpeasException if an error occurs during the shutdown process. The error won't
   * stop the shutdown of Silverpeas and it will be logged for further investigation.
   */
  default void release() throws SilverpeasException {
  }
}
