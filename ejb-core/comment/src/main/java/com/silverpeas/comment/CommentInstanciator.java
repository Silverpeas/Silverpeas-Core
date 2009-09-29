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
 * 
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