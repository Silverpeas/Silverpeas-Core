/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.external.filesharing;

import java.sql.Connection;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.admin.components.InstanciationException;

public class FileSharingInstanciator implements ComponentsInstanciatorIntf {

  public FileSharingInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("fileSharing", "FileSharingInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
        + componentId + ", userId =" + userId);

    // insert your code here !

    SilverTrace.info("fileSharing", "FileSharingInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("fileSharing", "FileSharingInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
        + componentId + ", userId =" + userId);

    // insert your code here !

    SilverTrace.info("fileSharing", "FileSharingInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }
}