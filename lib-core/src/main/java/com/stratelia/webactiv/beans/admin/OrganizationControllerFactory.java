/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import javax.inject.Inject;

/**
 * A factory of OrganizationController instances. This factory is managed by the IoC container used
 * in Silverpeas and it provides an access the managed OrganizationController instances for the
 * beans not taken in charge by the IoC container.
 */
public class OrganizationControllerFactory {

  private static OrganizationControllerFactory instance = new OrganizationControllerFactory();

  @Inject
  private OrganizationController organizationController;

  public static OrganizationControllerFactory getFactory() {
    return instance;
  }
  
  private OrganizationController getController() {
    return organizationController;
  }

  public static OrganizationController getOrganizationController() {
    return instance.getController();
  }

  private OrganizationControllerFactory() {

  }
}
