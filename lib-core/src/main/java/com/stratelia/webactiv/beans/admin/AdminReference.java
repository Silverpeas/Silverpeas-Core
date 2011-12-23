/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

package com.stratelia.webactiv.beans.admin;

import javax.inject.Inject;

/**
 * AdminReference represents the reference to an Admin instance. It manages the access to this
 * instance. The Admin objects gathers all the operations that create the organizational resources
 * for a Silverpeas server instance. All objects requiring a reference to an Admin instance should
 * use an instance of this class.
 */
public class AdminReference {
  private final static AdminReference instance = new AdminReference();

  @Inject
  private Admin admin;

  private AdminReference() {
  }

  static AdminReference getInstance() {
    return instance;
  }

  private synchronized Admin getAdmin() {
    if (admin == null) {
      // case where the admin reference is used in tests running out of an IoC container context.
      // maintained for compatibility reason.
      admin = new Admin();
    }
    return admin;
  }

  /**
   * Gets the administration service refered by this AdminReference.
   *
   * @return the admin service instance.
   */
  public static Admin getAdminService() {
    return getInstance().getAdmin();
  }
}