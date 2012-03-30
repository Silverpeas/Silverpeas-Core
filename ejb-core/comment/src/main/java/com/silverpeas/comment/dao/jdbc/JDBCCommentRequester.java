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
package com.silverpeas.comment.dao.jdbc;

import java.util.Date;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.sql.Statement;
import static com.stratelia.webactiv.util.DateUtil.*;

/**
 * A specific JDBC requester dedicated on the comments persisted in the underlying data source.
 */
public class JDBCCommentRequester {

  private static final int INITIAL_CAPACITY = 1000;

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
    String insert_query =
        "INSERT INTO sb_comment_comment (commentId , commentOwnerId, "
            + "commentCreationDate, commentModificationDate, commentComment, resourceType, resourceId, instanceId) "
            + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
    PreparedStatement prep_stmt = null;
    int newId = 0;
    try {
      newId = DBUtil.getNextId(cmt.getCommentPK().getTableName(), "commentId");
    } catch (Exception e) {
      SilverTrace.warn("comments", getClass().getSimpleName() + ".createComment",
          "root.EX_PK_GENERATION_FAILED", e);
      return null;
    }
    try {
      prep_stmt = con.prepareStatement(insert_query);
      prep_stmt.setInt(1, newId);
      prep_stmt.setInt(2, cmt.getOwnerId());
      prep_stmt.setString(3, date2SQLDate(cmt.getCreationDate()));
      String modifDate = null;
      if (cmt.getModificationDate() != null) {
        modifDate = date2SQLDate(cmt.getModificationDate());
      }
      prep_stmt.setString(4, modifDate);
      prep_stmt.setString(5, cmt.getMessage());
      prep_stmt.setString(6, cmt.getResourceType());
      prep_stmt.setString(7, cmt.getForeignKey().getId());
      prep_stmt.setString(8, cmt.getCommentPK().getComponentName());
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
    cmt.getCommentPK().setId(String.valueOf(newId));
    return cmt.getCommentPK();
  }

  /**
   * Deletes the comment identified by the specified primary key from the data source onto which the
   * given connection is opened.
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
    String update_query =
        "UPDATE sb_comment_comment SET commentOwnerId=?, commentModificationDate=?, "
            + "commentComment=?, resourceType=?, resourceId=?, instanceId=? WHERE commentId= ?";
    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(update_query);
      prep_stmt.setInt(1, cmt.getOwnerId());
      prep_stmt.setString(2, date2SQLDate(cmt.getModificationDate()));
      prep_stmt.setString(3, cmt.getMessage());
      prep_stmt.setString(4, cmt.getResourceType());
      prep_stmt.setString(5, cmt.getForeignKey().getId());
      prep_stmt.setString(6, cmt.getCommentPK().getComponentName());
      prep_stmt.setInt(7, Integer.parseInt(cmt.getCommentPK().getId()));
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }

  /**
   * Moves comments. (Requires more explanation!)
   * @param con the connection to the data source.
   * @param fromResourceType the source type of the commented resource
   * @param fromPK the source unique identifier of the comment in the data source.
   * @param toResourceType the destination type of the commented resource
   * @param toPK the destination unique identifier of another comment in the data source.
   * @throws SQLException if an error occurs during the operation.
   */
  public void moveComments(Connection con, String fromResourceType, ForeignPK fromPK,
      String toResourceType, ForeignPK toPK)
      throws SQLException {
    String update_query =
        "UPDATE sb_comment_comment SET resourceType=?, resourceId=?, instanceId=? "
            + "WHERE resourceType=? AND resourceId=? AND instanceId=?";
    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(update_query);
      prep_stmt.setString(1, toResourceType);
      prep_stmt.setString(2, toPK.getId());
      prep_stmt.setString(3, toPK.getInstanceId());
      prep_stmt.setString(4, fromResourceType);
      prep_stmt.setString(5, fromPK.getId());
      prep_stmt.setString(6, fromPK.getInstanceId());
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
    String select_query =
        "SELECT commentOwnerId, commentCreationDate, commentModificationDate, "
            + "commentComment, resourceType, resourceId, instanceId FROM sb_comment_comment WHERE commentId = ?";
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    try {
      prep_stmt = con.prepareStatement(select_query);
      prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prep_stmt.executeQuery();
      if (rs.next()) {
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = new CommentPK(rs.getString("resourceId"));
        try {
          Date modifDate = null;
          String sqlModifDate = rs.getString("commentModificationDate");
          if (StringUtil.isDefined(sqlModifDate)) {
            modifDate = parseDate(rs.getString("commentModificationDate"));
          }
          return new Comment(pk, rs.getString("resourceType"), father_id,
              rs.getInt("commentOwnerId"), "",
              rs.getString("commentComment"), parseDate(rs.getString("commentCreationDate")),
              modifDate);
        } catch (ParseException ex) {
          throw new SQLException(ex.getMessage(), ex);
        }
      }
      return null;
    } finally {
      DBUtil.close(rs, prep_stmt);
    }
  }

  public List<CommentedPublicationInfo> getMostCommentedAllPublications(Connection con)
      throws SQLException {
    String select_query =
        "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM "
            + "sb_comment_comment GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc;";
    Statement prep_stmt = null;
    ResultSet rs = null;
    List<CommentedPublicationInfo> listPublisCommentsCount =
        new ArrayList<CommentedPublicationInfo>();
    try {
      prep_stmt = con.createStatement();
      rs = prep_stmt.executeQuery(select_query);
      while (rs.next()) {
        Integer countComment = Integer.valueOf(rs.getInt("nb_comment"));
        String resourceType = rs.getString("resourceType");
        String resourceId = rs.getString("resourceId");
        String instanceId = rs.getString("instanceId");
        listPublisCommentsCount.add(new CommentedPublicationInfo(resourceType, resourceId,
            instanceId, countComment.intValue()));
      }
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return listPublisCommentsCount;

  }

  public int getCommentsCount(Connection con, String resourceType, WAPrimaryKey foreign_pk)
      throws SQLException {
    String select_query = "SELECT COUNT(commentId) AS nb_comment FROM sb_comment_comment "
        + "WHERE instanceId = ? AND resourceType = ? AND resourceId = ?";
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    int commentsCount = 0;
    try {
      prep_stmt = con.prepareStatement(select_query);
      prep_stmt.setString(1, foreign_pk.getComponentName());
      prep_stmt.setString(2, resourceType);
      prep_stmt.setString(3, foreign_pk.getId());
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

  public List<Comment> getAllComments(Connection con, String resourceType, WAPrimaryKey foreign_pk)
      throws SQLException {
    String select_query =
        "SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, "
            + "commentComment, resourceType, resourceId, instanceId FROM sb_comment_comment "
            + "WHERE resourceType = ? AND resourceId = ? AND instanceId = ? "
            + "ORDER BY commentCreationDate DESC, commentId DESC";
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    List<Comment> comments = new ArrayList<Comment>(INITIAL_CAPACITY);
    try {
      prep_stmt = con.prepareStatement(select_query);
      prep_stmt.setString(1, resourceType);
      prep_stmt.setString(2, foreign_pk.getId());
      prep_stmt.setString(3, foreign_pk.getComponentName());
      rs = prep_stmt.executeQuery();
      CommentPK pk;
      Comment cmt = null;
      while (rs.next()) {
        pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = new CommentPK(rs.getString("resourceId"));
        try {
          cmt =
              new Comment(pk, rs.getString("resourceType"), father_id, rs.getInt("commentOwnerId"),
                  "", rs.getString(
                      "commentComment"), parseDate(rs.getString("commentCreationDate")),
                  parseDate(rs.getString("commentModificationDate")));
        } catch (ParseException ex) {
          throw new SQLException(ex.getMessage(), ex);
        }
        comments.add(cmt);
      }
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return comments;
  }

  public void deleteAllComments(Connection con, String resourceType, ForeignPK foreignPK)
      throws SQLException {
    String delete_query =
        "DELETE FROM sb_comment_comment WHERE resourceType = ? AND resourceId = ? AND instanceId = ?";
    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(delete_query);
      prep_stmt.setString(1, resourceType);
      prep_stmt.setString(2, foreignPK.getId());
      prep_stmt.setString(3, foreignPK.getInstanceId());
      prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }
}