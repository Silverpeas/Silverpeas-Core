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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.initialization.Initialization;

import javax.inject.Inject;

/**
 * Initializes the Administration layer of Silverpeas. It sets up the administration service.
 */
public class AdminInitialize implements Initialization {

  @Inject
  private AdminController adminController;

  public AdminInitialize() {
  }

  @Override
  public void init() {
    // Initialize SilverTrace
    try {
      adminController.startServer();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminInitialize.Initialize()", "admin.MSG_ERR_GET_DOMAIN", e);
    }
  }

}
