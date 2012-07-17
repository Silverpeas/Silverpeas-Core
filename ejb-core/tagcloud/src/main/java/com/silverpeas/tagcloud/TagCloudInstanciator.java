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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.tagcloud;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.webactiv.util.DBUtil;

public class TagCloudInstanciator implements ComponentsInstanciatorIntf {

  private static final String TAGCLOUD_TABLENAME = "SB_TagCloud_TagCloud";

  public TagCloudInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("tagCloud", "TagCloudInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("tagCloud", "TagCloudInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

    String query = "DELETE FROM " + TAGCLOUD_TABLENAME
        + " WHERE instanceId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, componentId);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new InstanciationException("TagCloudInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}