/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.initialization;

/**
 * This is a markup interface. It qualifies the services that are dedicated to initialize some
 * resources or some others services. The initialization services has to implement the {@code
 * Initialization#init()} method. The {@code Initialization#release()} method is optional and
 * does nothing by default.
 * @author mmoquillon
 */
public interface Initialization {

  /**
   * Initializes some resources required by the services or performs some initialization processes
   * at Silverpeas startup.
   * @throws java.lang.Exception if an error occurs during the initialization process. In this case
   * the Silverpeas startup fails.
   */
  void init() throws Exception;

  /**
   * Releases the previously initialized resources at Silverpeas shutdown. The implementation of
   * this method is optional. By default it does nothing.
   * @throws java.lang.Exception if an error occurs during the shutdown process. In this case, a log
   * will be outputed and the shutdown goes one.
   */
  default void release() throws Exception {
  }
}
