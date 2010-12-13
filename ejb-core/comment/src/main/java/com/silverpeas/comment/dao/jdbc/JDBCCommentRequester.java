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
package com.silverpeas.comment.dao.jdbc;

import com.silverpeas.comment.dao.CommentInfo;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.sql.Statement;

/**
 * A specific JDBC requester dedicated on the comments persisted in the underlying data source.
 */
public class JDBCCommentRequester {

  private static final int INITIAL_CAPACITY = 1000;
  private static final String COMMENT_TABLENAME = "sb_comment_comment";

  /**
   * Constructs a new JDBCCommentRequester instance.
   */
  public JDBCCommentRequester() {
  }

  /**
   * Saves the specified comment with the specified connection onto a data source.
   * @param con the connection to a data source.
   * @param cmt the comment to save.
   * @return the unique identifier of comment in the data source (id est the primary key).
   * @throws SQLException if an error occurs while saving the comment.
   */
  public CommentPK saveComment(Connection con, Comment cmt)
      throws SQLException {
    String insert_query = "INSERT INTO sb_comment_comment (commentId , commentOwnerId, "
        + "commentCreationDate, commentModificationDate, commentComment, foreignId, instanceId) "
        + "VALUES ( ?, ?, ?, ?, ?, ?, ? )";
    PreparedStatement prep_stmt = null;
    int newId = 0;
    try {
      newId = DBUtil.getNextId(cmt.getCommentPK().getTableName(), "commentId");
    } catch (Exception e) {
      SilverTrace.warn("comments", getClass().getSimpleName() + ".createComment", "root.EX_PK_GENERATION_FAILED", e);
      return null;
    }
    try {
      prep_stmt = con.prepareStatement(insert_query);
      prep_stmt.setInt(1, newId);
      prep_stmt.setInt(2, cmt.getOwnerId());
      prep_stmt.setString(3, cmt.getCreationDate());
      prep_stmt.setString(4, cmt.getModificationDate());
      prep_stmt.setString(5, cmt.getMessage());
      prep_stmt.setInt(6, Integer.parseInt(cmt.getForeignKey().getId()));
      prep_stmt.setString(7, cmt.getCommentPK().getComponentName());
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
    cmt.getCommentPK().setId(String.valueOf(newId));
    return cmt.getCommentPK();
  }

  /**
   * Deletes the comment identified by the specified primary key from the data source onto which
   * the given connection is opened.
   * @param con the connection to the data source.
   * @param pk the unique identifier of the comment in the data source.
   * @throws SQLException if an error occurs while removing the comment from the data source.
   */
  public void deleteComment(Connection con, CommentPK pk) throws SQLException {
    String delete_query = "DELETE FROM sb_comment_comment WHERE commentId = ?";
    PreparedStatement prep_stmt = null;
    prep_stmt = con.prepareStatement(delete_query);
    try {
      prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }

  /**
   * Updates the comment representation in the data source by the specified one.
   * @param con the connection to the data source.
   * @param cmt the updated comment.
   * @throws SQLException if an error occurs while updating the comment in the data source.
   */
  public void updateComment(Connection con, Comment cmt) throws SQLException {
    String update_query = "UPDATE sb_comment_comment SET commentOwnerId=?, commentModificationDate=?, "
        + "commentComment=?, foreignId=?, instanceId=? WHERE commentId= ?";
    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(update_query);
      prep_stmt.setInt(1, cmt.getOwnerId());
      prep_stmt.setString(2, cmt.getModificationDate());
      prep_stmt.setString(3, cmt.getMessage());
      prep_stmt.setInt(4, Integer.parseInt(cmt.getForeignKey().getId()));
      prep_stmt.setString(5, cmt.getCommentPK().getComponentName());
      prep_stmt.setString(6, cmt.getCommentPK().getId());
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }

  /**
   * Moves comments. (Requires more explanation!)
   * @param con the connection to the data source.
   * @param fromPK the source unique identifier of the comment in the data source.
   * @param toPK the destination unique identifier of another comment in the data source.
   * @throws SQLException if an error occurs during the operation.
   */
  public void moveComments(Connection con, ForeignPK fromPK, ForeignPK toPK)
      throws SQLException {
    String update_query = "UPDATE sb_comment_comment SET foreignId=?, instanceId=? WHERE "
        + "foreignId=? AND instanceId=?";
    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(update_query);
      prep_stmt.setInt(1, Integer.parseInt(toPK.getId()));
      prep_stmt.setString(2, toPK.getInstanceId());
      prep_stmt.setInt(3, Integer.parseInt(fromPK.getId()));
      prep_stmt.setString(4, fromPK.getInstanceId());
      prep_stmt.executeUpdate();
      prep_stmt.close();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }

  /**
   * Gets the comment identified by the specified identifier.
   * @param con the connection to use for getting the comment.
   * @param pk the identifier of the comment in the data source.
   * @return the comment or null if no such comment is found.
   * @throws SQLException if an error occurs during the comment fetching.
   */
  public Comment getComment(Connection con, CommentPK pk) throws SQLException {
    String select_query = "SELECT commentOwnerId, commentCreationDate, commentModificationDate, "
        + "commentComment, foreignId, instanceId FROM sb_comment_comment WHERE commentId = ?";
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    try {
      prep_stmt = con.prepareStatement(select_query);
      prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prep_stmt.executeQuery();
      if (rs.next()) {
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = new CommentPK(String.valueOf(rs.getInt("foreignId")));
        return new Comment(pk, father_id, rs.getInt("commentOwnerId"), "",
            rs.getString("commentComment"), rs.getString("commentCreationDate"),
            rs.getString("commentModificationDate"));
      }
      return null;
    } finally {
      DBUtil.close(rs, prep_stmt);
    }
  }

  public List<CommentInfo> getMostCommentedAllPublications(Connection con)
      throws SQLException {
    String select_query = "SELECT COUNT(commentId) as nb_comment, foreignId, instanceId FROM "
        + "sb_comment_comment GROUP BY foreignId, instanceId ORDER BY nb_comment desc;";
    Statement prep_stmt = null;
    ResultSet rs = null;
    List<CommentInfo> listPublisCommentsCount = new ArrayList<CommentInfo>();
    try {
      prep_stmt = con.createStatement();
      rs = prep_stmt.executeQuery(select_query);
      while (rs.next()) {
        Integer countComment = Integer.valueOf(rs.getInt("nb_comment"));
        Integer foreignId = Integer.valueOf(rs.getInt("foreignId"));
        String instanceId = rs.getString("instanceId");
        listPublisCommentsCount.add(new CommentInfo(countComment.intValue(), instanceId,
            foreignId.toString()));
      }
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return listPublisCommentsCount;

  }

  public int getCommentsCount(Connection con, WAPrimaryKey foreign_pk)
      throws SQLException {
    String select_query = "select count(commentId) as nb_comment from "
        + COMMENT_TABLENAME + " where instanceId = '"
        + foreign_pk.getComponentName() + "' AND foreignid="
        + foreign_pk.getId() + ";";

    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    int commentsCount = 0;

    try {
      prep_stmt = con.prepareStatement(select_query);
      rs = prep_stmt.executeQuery();
      while (rs.next()) {
        commentsCount = rs.getInt("nb_comment");
      }
    } catch (Exception e) {
      SilverTrace.error("comment", getClass().getSimpleName() + ".getCommentsCount()",
          "root.EX_NO_MESSAGE", e);
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return commentsCount;
  }

  public List<Comment> getAllComments(Connection con, WAPrimaryKey foreign_pk)
      throws SQLException {
    String select_query =
        "select commentId, commentOwnerId, commentCreationDate, commentModificationDate, commentComment, foreignId, instanceId from "
        + COMMENT_TABLENAME
        + " where foreignId="
        + foreign_pk.getId()
        + " and instanceId = '"
        + foreign_pk.getComponentName()
        + "' order by commentCreationDate DESC, commentId DESC";
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    prep_stmt = con.prepareStatement(select_query);

    List<Comment> comments = new ArrayList<Comment>(INITIAL_CAPACITY);

    try {
      rs = prep_stmt.executeQuery();
      CommentPK pk;
      Comment cmt = null;
      while (rs.next()) {
        pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = (WAPrimaryKey) new CommentPK(String.valueOf(rs.getInt("foreignId")));
        cmt = new Comment(pk, father_id, rs.getInt("commentOwnerId"), "", rs.getString(
            "commentComment"), rs.getString("commentCreationDate"),
            rs.getString("commentModificationDate"));
        comments.add(cmt);
      }
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return comments;
  }

  public void deleteAllComments(Connection con, ForeignPK pk)
      throws SQLException {
    String delete_query = "delete from " + COMMENT_TABLENAME
        + " where foreignId = ? And instanceId = ? ";
    PreparedStatement prep_stmt = null;
    prep_stmt = con.prepareStatement(delete_query);
    try {
      prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
      prep_stmt.setString(2, pk.getInstanceId());
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }
}