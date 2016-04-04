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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * @author Ludovic Bertin
 * @version 1.0
 *  date 10/08/2001
 */

package org.silverpeas.core.admin.service;

import org.silverpeas.core.util.ServiceProvider;

/**
 * AdministrationServiceProvider provides the reference:
 * <ul>
 * <li>to an {@link Administration} instance that gathers all the operations that create the
 * organizational resources for a Silverpeas server instance</li>
 * <li>to an {@link RightRecover} instance that gathers all the operation to restore rights on space and
 * components</li>
 * </ul>
 */
public class AdministrationServiceProvider {

  /**
   * Gets the administration service
   * @return the instance administration service.
   */
  public static Administration getAdminService() {
    return Administration.get();
  }

  /**
   * Gets the recovering service of rights.
   * @return the instance of recovering service of rights.
   */
  public static RightRecover getRightRecoveringService() {
    return ServiceProvider.getService(RightRecover.class);
  }
}