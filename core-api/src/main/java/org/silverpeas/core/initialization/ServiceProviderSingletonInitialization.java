/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.initialization;

/**
 * Extension of {@link Initialization} interface dedicated to Service Providers which are
 * providing a singleton from CDI.<br/>
 * The aim is to initialize a static reference at server startup in order to avoid to use
 * {@link org.silverpeas.core.util.ServiceProvider} mechanism at each supply request.<br/>
 * By this way, there is no memory overload as the references of the services exist into CDI
 * containers, and performances are super amazing!!!
 * @author silveryocha
 */
public interface ServiceProviderSingletonInitialization extends Initialization {

  /**
   * Gets the priority level of the execution of {@link #init()} method.
   * <p>
   * The less is the value of the priority the more the priority is high.
   * </p>
   * @return an integer priority.
   */
  @Override
  default int getPriority() {
    return -1000;
  }
}
