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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.dao.jdbc;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.util.DateUtil.date2SQLDate;
import static org.silverpeas.core.util.DateUtil.parseDate;

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
    String insert_query = "INSERT INTO sb_comment_comment (commentId , commentOwnerId, "
        + "commentCreationDate, commentModificationDate, commentComment, resourceType, resourceId, instanceId) "
        + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
    PreparedStatement prep_stmt = null;
    int newId;
    try {
      newId = DBUtil.getNextId(cmt.getCommentPK().getTableName(), "commentId");
    } catch (SQLException e) {
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
    PreparedStatement prep_stmt;
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
    String update_query
        = "UPDATE sb_comment_comment SET commentOwnerId=?, commentModificationDate=?, "
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
    String update_query
        = "UPDATE sb_comment_comment SET resourceType=?, resourceId=?, instanceId=? "
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
    String select_query = "SELECT commentOwnerId, commentCreationDate, commentModificationDate, "
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

  public List<CommentedPublicationInfo> getMostCommentedAllPublications(Connection con,
      String resType)
      throws SQLException {
    String resourceTypeQuery = (StringUtil.isDefined(resType) ? "where resourceType = '"
        + resType + "'" : "");
    String select_query
        = "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM "
        + "sb_comment_comment " + resourceTypeQuery
        + " GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc;";
    Statement prep_stmt = null;
    ResultSet rs = null;
    List<CommentedPublicationInfo> listPublisCommentsCount
        = new ArrayList<CommentedPublicationInfo>();
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

  public List<CommentedPublicationInfo> getMostCommentedPublications(Connection con,
      final List<? extends WAPrimaryKey> pks) throws SQLException {
    String query
        = "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM "
        + "sb_comment_comment";
    List<String> resourceIds = new ArrayList<String>(pks.size());
    List<String> instanceIds = new ArrayList<String>(pks.size());
    for (WAPrimaryKey aPk : pks) {
      if (StringUtil.isDefined(aPk.getId())) {
        resourceIds.add(aPk.getId());
      }
      if (StringUtil.isDefined(aPk.getInstanceId())) {
        instanceIds.add(aPk.getInstanceId());
      }
    }
    if (!resourceIds.isEmpty()) {
      query += " where resourceId in (?)";
    }
    if (!instanceIds.isEmpty()) {
      query += (query.contains("where") ? " and " : " where ") + "instanceId in (?)";
    }
    query += " GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<CommentedPublicationInfo> listPublisCommentsCount
        = new ArrayList<CommentedPublicationInfo>();
    try {
      stmt = con.prepareStatement(query);
      int i = 1;
      if (!resourceIds.isEmpty()) {
        stmt.setString(i++, StringUtils.join(resourceIds, ","));
      }
      if (!instanceIds.isEmpty()) {
        stmt.setString(i, StringUtils.join(instanceIds, ","));
      }
      rs = stmt.executeQuery();
      while (rs.next()) {
        int countComment = Integer.valueOf(rs.getInt("nb_comment"));
        String resourceType = rs.getString("resourceType");
        String resourceId = rs.getString("resourceId");
        String instanceId = rs.getString("instanceId");
        listPublisCommentsCount.add(new CommentedPublicationInfo(resourceType, resourceId,
            instanceId, countComment));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return listPublisCommentsCount;
  }

  public int getCommentsCount(Connection con, String resourceType, WAPrimaryKey foreign_pk)
      throws SQLException {
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder select_query = new StringBuilder(
        "SELECT COUNT(commentId) AS nb_comment FROM sb_comment_comment");
    performQueryAndParams(select_query, params, resourceType, foreign_pk);
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    int commentsCount = 0;
    try {
      prep_stmt = con.prepareStatement(select_query.toString());
      int indexParam = 1;
      for (Object param : params) {
        prep_stmt.setString(indexParam++, (String) param);
      }
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
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder select_query = new StringBuilder();
    select_query
        .append("SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, ");
    select_query
        .append("commentComment, resourceType, resourceId, instanceId FROM sb_comment_comment");
    performQueryAndParams(select_query, params, resourceType, foreign_pk);
    select_query.append("ORDER BY commentCreationDate DESC, commentId DESC");
    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    List<Comment> comments = new ArrayList<Comment>(INITIAL_CAPACITY);
    try {
      prep_stmt = con.prepareStatement(select_query.toString());
      int indexParam = 1;
      for (Object param : params) {
        prep_stmt.setString(indexParam++, (String) param);
      }
      rs = prep_stmt.executeQuery();
      CommentPK pk;
      Comment cmt = null;
      while (rs.next()) {
        pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = new CommentPK(rs.getString("resourceId"));
        try {
          cmt
              = new Comment(pk, rs.getString("resourceType"), father_id, rs.getInt("commentOwnerId"),
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

  public int deleteAllComments(Connection con, String resourceType, ForeignPK foreignPK)
      throws SQLException {
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder delete_query = new StringBuilder("DELETE FROM sb_comment_comment");
    performQueryAndParams(delete_query, params, resourceType, foreignPK);

    PreparedStatement prep_stmt = null;
    try {
      prep_stmt = con.prepareStatement(delete_query.toString());
      int indexParam = 1;
      for (Object param : params) {
        prep_stmt.setString(indexParam++, (String) param);
      }
      return prep_stmt.executeUpdate();
    } finally {
      DBUtil.close(prep_stmt);
    }
  }

  private void performQueryAndParams(StringBuilder query, List<Object> params, String resourceType,
      WAPrimaryKey foreignPK) {
    List<String> listResourceType = new ArrayList<String>();
    if (StringUtil.isDefined(resourceType)) {
      listResourceType.add(resourceType);
    }
    performQueryAndParams(query, params, listResourceType, foreignPK, null, null, null);
  }

  private void performQueryAndParams(StringBuilder query, List<Object> params,
      List<String> listResourceType, WAPrimaryKey foreignPK, List<String> listUserId,
      List<String> listInstanceId, Period period) {
    String clause = " WHERE ";
    if (CollectionUtil.isNotEmpty(listResourceType)) {
      query.append(clause).append("resourceType IN (");
      clause = "";
      for (String resourceType : listResourceType) {
        query.append(clause).append("?");
        clause = ", ";
        params.add(resourceType);
      }
      query.append(") ");
      clause = "AND ";
    }
    if (foreignPK != null) {
      if (StringUtil.isDefined(foreignPK.getId())) {
        query.append(clause).append("resourceId = ? ");
        clause = "AND ";
        params.add(foreignPK.getId());
      }
      if (StringUtil.isDefined(foreignPK.getInstanceId())) {
        query.append(clause).append("instanceId = ? ");
        clause = "AND ";
        params.add(foreignPK.getInstanceId());
      }
    }
    if (CollectionUtil.isNotEmpty(listUserId)) {
      query.append(clause).append("commentOwnerId IN (");
      clause = "";
      for (String userId : listUserId) {
        Integer ownerId = new Integer(userId);
        query.append(clause).append("?");
        clause = ", ";
        params.add(ownerId);
      }
      query.append(") ");
      clause = "AND ";
    }
    if (listInstanceId != null) {
      if (listInstanceId.isEmpty()) {
        // This empty list indicates that the requester has no component access.
        query.append(clause).append("instanceId IN ('noComponentInstanceId') ");
      } else {
        query.append(clause).append("instanceId IN (");
        clause = "";
        for (String instanceId : listInstanceId) {
          query.append(clause).append("?");
          clause = ", ";
          params.add(instanceId);
        }
        query.append(") ");
      }
      clause = "AND ";
    }
    if (period != null && period.isValid()) {
      query.append(clause).append("((commentModificationDate BETWEEN ? AND ?) ");
      params.add(date2SQLDate(period.getBeginDate()));
      params.add(date2SQLDate(period.getEndDate()));
      query.append("OR (commentCreationDate BETWEEN ? AND ?)) ");
      params.add(date2SQLDate(period.getBeginDate()));
      params.add(date2SQLDate(period.getEndDate()));
    }

    if (params.isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  public List<Comment> getLastComments(Connection con, String instanceId, int count) throws
      SQLException {
    String query = "SELECT commentId, commentOwnerId, commentCreationDate, "
        + "commentModificationDate, commentComment, resourceType, resourceId, instanceId "
        + "FROM sb_comment_comment where instanceId = ? ORDER BY commentCreationDate DESC, "
        + "commentId DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Comment> comments = new ArrayList<Comment>(count);
    try {
      stmt = con.prepareStatement(query);
      stmt.setString(1, instanceId);
      if (count > 0) {
        stmt.setMaxRows(count);
      }
      rs = stmt.executeQuery();
      while (rs.next()) {
        CommentPK pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey resourceId = new CommentPK(rs.getString("resourceId"));
        try {
          Comment cmt = new Comment(pk, rs.getString("resourceType"), resourceId, rs.getInt(
              "commentOwnerId"), "", rs.getString("commentComment"),
              parseDate(rs.getString("commentCreationDate")),
              parseDate(rs.getString("commentModificationDate")));
          comments.add(cmt);
        } catch (ParseException ex) {
          throw new SQLException(ex.getMessage(), ex);
        }
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return comments;
  }

  public List<SocialInformationComment> getSocialInformationComments(Connection con,
      List<String> resourceTypes, List<String> userAuthorIds, List<String> instanceIds,
      Period period) throws SQLException {
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder select_query = new StringBuilder();

    select_query
        .append("SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, ");
    select_query.append("commentComment, resourceType, resourceId, instanceId ");
    select_query.append("FROM sb_comment_comment ");
    performQueryAndParams(select_query, params, resourceTypes, null, userAuthorIds,
        instanceIds, period);
    select_query.append("ORDER BY commentModificationDate DESC, commentId DESC");

    PreparedStatement prep_stmt = null;
    ResultSet rs = null;
    List<SocialInformationComment> listSocialInformationComment =
        new ArrayList<SocialInformationComment>(INITIAL_CAPACITY);
    try {
      prep_stmt = con.prepareStatement(select_query.toString());
      int indexParam = 1;
      for (Object param : params) {
        if (param instanceof String) {
          prep_stmt.setString(indexParam++, (String) param);
        } else if (param instanceof Integer) {
          prep_stmt.setInt(indexParam++, (Integer) param);
        }
      }
      rs = prep_stmt.executeQuery();
      CommentPK pk;
      Comment comment;
      while (rs.next()) {
        pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
        pk.setComponentName(rs.getString("instanceId"));
        WAPrimaryKey father_id = new CommentPK(rs.getString("resourceId"));
        try {
          comment =
              new Comment(pk, rs.getString("resourceType"), father_id, rs.getInt("commentOwnerId"),
                  "", rs.getString("commentComment"),
                  parseDate(rs.getString("commentCreationDate")),
                  parseDate(rs.getString("commentModificationDate")));
        } catch (ParseException ex) {
          throw new SQLException(ex.getMessage(), ex);
        }

        listSocialInformationComment.add(new SocialInformationComment(comment));
      }
    } finally {
      DBUtil.close(rs, prep_stmt);
    }

    return listSocialInformationComment;

  }
}
