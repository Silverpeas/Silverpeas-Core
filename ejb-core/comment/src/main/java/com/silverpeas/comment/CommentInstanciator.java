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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
 * Created on 8 nov. 2004
 *
 */
package com.silverpeas.comment;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.DBUtil;

/**
 * @author neysseri
 */
public class CommentInstanciator implements ComponentsInstanciatorIntf {

  private static final String COMMENT_TABLENAME = "SB_COMMENT_COMMENT";

  public CommentInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("comment", "CommentInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("comment", "CommentInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

    String delete_query = "delete from " + COMMENT_TABLENAME
        + " where instanceId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(delete_query);
      prepStmt.setString(1, componentId);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new InstanciationException("CommentInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}