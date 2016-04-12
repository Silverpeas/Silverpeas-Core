/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.util.ServiceProvider;

/**
 * A provider of the different access controllers available in Silverpeas.
 *
 * The different access controllers are all managed in the IoC container and then can be get by
 * dependency injection. Nevertheless, not all objects in Silverpeas are managed by the IoC
 * container and they can need the services of an access controller. The AccessControllerProvider
 * aims to make available the different access controllers to such objects.
 */
public class AccessControllerProvider {

  /**
   * Gets the access controller identified by the specified qualifier.
   * @param <T> the type of the resource that is used in the access control mechanism.
   * @return the asked access controller.
   */
  public static <T> AccessController<T> getAccessController(Class<?> type) {
    return (AccessController<T>) ServiceProvider.getService(type);
  }
}
