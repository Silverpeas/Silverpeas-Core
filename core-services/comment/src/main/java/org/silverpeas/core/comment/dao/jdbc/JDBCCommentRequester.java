/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.dao.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.DateUtil.date2SQLDate;
import static org.silverpeas.core.util.DateUtil.parseDate;

/**
 * A specific JDBC requester dedicated on the comments persisted in the underlying data source.
 */
@Technical
@Bean
public class JDBCCommentRequester {

  private static final int INITIAL_CAPACITY = 1000;
  private static final String COMMENT_ID = "commentId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String RESOURCE_ID = "resourceId";
  private static final String COMMENT_MODIFICATION_DATE = "commentModificationDate";
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String COMMENT_OWNER_ID = "commentOwnerId";
  private static final String COMMENT_TEXT = "commentComment";
  private static final String COMMENT_CREATION_DATE = "commentCreationDate";
  private static final String COMMENT_COUNT = "nb_comment";

  protected JDBCCommentRequester() {
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
    String insertQuery = "INSERT INTO sb_comment_comment (commentId , commentOwnerId, "
        + "commentCreationDate, commentModificationDate, commentComment, resourceType, resourceId, instanceId) "
        + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
    PreparedStatement prepStmt = null;
    int newId;
    newId = DBUtil.getNextId(cmt.getCommentPK().getTableName(), COMMENT_ID);
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, cmt.getOwnerId());
      prepStmt.setString(3, date2SQLDate(cmt.getCreationDate()));
      String modifDate = null;
      if (cmt.getLastUpdateDate() != null) {
        modifDate = date2SQLDate(cmt.getLastUpdateDate());
      }
      prepStmt.setString(4, modifDate);
      prepStmt.setString(5, cmt.getMessage());
      prepStmt.setString(6, cmt.getResourceType());
      prepStmt.setString(7, cmt.getForeignKey().getId());
      prepStmt.setString(8, cmt.getCommentPK().getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
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
    String deleteQuery = "DELETE FROM sb_comment_comment WHERE commentId = ?";
    PreparedStatement prepStmt;
    prepStmt = con.prepareStatement(deleteQuery);
    try {
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Updates the comment representation in the data source by the specified one.
   * @param con the connection to the data source.
   * @param cmt the updated comment.
   * @throws SQLException if an error occurs while updating the comment in the data source.
   */
  public void updateComment(Connection con, Comment cmt) throws SQLException {
    String updateQuery
        = "UPDATE sb_comment_comment SET commentOwnerId=?, commentModificationDate=?, "
        + "commentComment=?, resourceType=?, resourceId=?, instanceId=? WHERE commentId= ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setInt(1, cmt.getOwnerId());
      prepStmt.setString(2, date2SQLDate(cmt.getLastUpdateDate()));
      prepStmt.setString(3, cmt.getMessage());
      prepStmt.setString(4, cmt.getResourceType());
      prepStmt.setString(5, cmt.getForeignKey().getId());
      prepStmt.setString(6, cmt.getCommentPK().getComponentName());
      prepStmt.setInt(7, Integer.parseInt(cmt.getCommentPK().getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
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
  public void moveComments(Connection con, String fromResourceType, ResourceReference fromPK,
      String toResourceType, ResourceReference toPK)
      throws SQLException {
    String updateQuery
        = "UPDATE sb_comment_comment SET resourceType=?, resourceId=?, instanceId=? "
        + "WHERE resourceType=? AND resourceId=? AND instanceId=?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, toResourceType);
      prepStmt.setString(2, toPK.getId());
      prepStmt.setString(3, toPK.getInstanceId());
      prepStmt.setString(4, fromResourceType);
      prepStmt.setString(5, fromPK.getId());
      prepStmt.setString(6, fromPK.getInstanceId());
      prepStmt.executeUpdate();
      prepStmt.close();
    } finally {
      DBUtil.close(prepStmt);
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
    String selectQuery = "SELECT commentOwnerId, commentCreationDate, commentModificationDate, "
        + "commentComment, resourceType, resourceId, instanceId FROM sb_comment_comment WHERE commentId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        pk.setComponentName(rs.getString(INSTANCE_ID));
        WAPrimaryKey fatherId = new CommentPK(rs.getString(RESOURCE_ID));
        try {
          Date modifDate = null;
          String sqlModifDate = rs.getString(COMMENT_MODIFICATION_DATE);
          if (StringUtil.isDefined(sqlModifDate)) {
            modifDate = parseDate(rs.getString(COMMENT_MODIFICATION_DATE));
          }
          return new Comment(pk, rs.getString(RESOURCE_TYPE), fatherId,
              rs.getInt(COMMENT_OWNER_ID), "",
              rs.getString(COMMENT_TEXT), parseDate(rs.getString(COMMENT_CREATION_DATE)),
              modifDate);
        } catch (ParseException ex) {
          throw new SQLException(ex.getMessage(), ex);
        }
      }
      return null;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public List<CommentedPublicationInfo> getMostCommentedAllPublications(Connection con,
      String resType)
      throws SQLException {
    String resourceTypeQuery = (StringUtil.isDefined(resType) ? "where resourceType = '"
        + resType + "'" : "");
    String selectQuery
        = "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM "
        + "sb_comment_comment " + resourceTypeQuery
        + " GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc;";
    Statement prepStmt = null;
    ResultSet rs = null;
    List<CommentedPublicationInfo> listPublisCommentsCount
        = new ArrayList<>();
    try {
      prepStmt = con.createStatement();
      rs = prepStmt.executeQuery(selectQuery);
      while (rs.next()) {
        int countComment = rs.getInt(COMMENT_COUNT);
        String resourceType = rs.getString(RESOURCE_TYPE);
        String resourceId = rs.getString(RESOURCE_ID);
        String instanceId = rs.getString(INSTANCE_ID);
        listPublisCommentsCount.add(new CommentedPublicationInfo(resourceType, resourceId,
            instanceId, countComment));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return listPublisCommentsCount;

  }

  public List<CommentedPublicationInfo> getMostCommentedPublications(Connection con,
      final List<? extends WAPrimaryKey> pks) throws SQLException {
    String query
        = "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM "
        + "sb_comment_comment";
    List<String> resourceIds = new ArrayList<>(pks.size());
    List<String> instanceIds = new ArrayList<>(pks.size());
    for (WAPrimaryKey aPk : pks) {
      if (isIdDefined(aPk.getId())) {
        resourceIds.add(aPk.getId());
      }
      if (isIdDefined(aPk.getInstanceId())) {
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
    List<CommentedPublicationInfo> listPublisCommentsCount = new ArrayList<>();
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
        int countComment = rs.getInt(COMMENT_COUNT);
        String resourceType = rs.getString(RESOURCE_TYPE);
        String resourceId = rs.getString(RESOURCE_ID);
        String instanceId = rs.getString(INSTANCE_ID);
        listPublisCommentsCount.add(new CommentedPublicationInfo(resourceType, resourceId,
            instanceId, countComment));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return listPublisCommentsCount;
  }

  public int getCommentsCount(Connection con, String resourceType, WAPrimaryKey foreignPk)
      throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder(
        "SELECT COUNT(commentId) AS nb_comment FROM sb_comment_comment");
    performQueryAndParams(selectQuery, params, resourceType, foreignPk);
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    int commentsCount = 0;
    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        commentsCount = rs.getInt(COMMENT_COUNT);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return commentsCount;
  }

  public List<Comment> getAllComments(Connection con, String resourceType, WAPrimaryKey foreignPk)
      throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder();
    selectQuery
        .append("SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, ");
    selectQuery
        .append("commentComment, resourceType, resourceId, instanceId FROM sb_comment_comment");
    performQueryAndParams(selectQuery, params, resourceType, foreignPk);
    selectQuery.append("ORDER BY commentCreationDate DESC, commentId DESC");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Comment> comments = new ArrayList<>(INITIAL_CAPACITY);
    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Comment cmt = toComment(rs);
        comments.add(cmt);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return comments;
  }

  public int deleteAllComments(Connection con, String resourceType, ResourceReference
      resourceReference)
      throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder deleteQuery = new StringBuilder("DELETE FROM sb_comment_comment");
    performQueryAndParams(deleteQuery, params, resourceType, resourceReference);

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(deleteQuery.toString());
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private void performQueryAndParams(StringBuilder query, List<Object> params, String resourceType,
      WAPrimaryKey foreignPK) {
    List<String> listResourceType = new ArrayList<>();
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
      appendInList(query, params, clause, RESOURCE_TYPE, listResourceType);
      clause = "AND ";
    }
    if (foreignPK != null) {
      if (isIdDefined(foreignPK.getId())) {
        query.append(clause).append("resourceId = ? ");
        clause = "AND ";
        params.add(foreignPK.getId());
      }
      if (isIdDefined(foreignPK.getInstanceId())) {
        query.append(clause).append("instanceId = ? ");
        clause = "AND ";
        params.add(foreignPK.getInstanceId());
      }
    }
    if (CollectionUtil.isNotEmpty(listUserId)) {
      final Set<Integer> listUserIdsAsInt = listUserId.stream().map(Integer::parseInt).collect(Collectors.toSet());
      appendInList(query, params, clause, COMMENT_OWNER_ID, listUserIdsAsInt);
      clause = "AND ";
    }
    if (listInstanceId != null) {
      if (listInstanceId.isEmpty()) {
        // This empty list indicates that the requester has no component access.
        query.append(clause).append("instanceId IN ('noComponentInstanceId') ");
      } else {
        appendInList(query, params, clause, INSTANCE_ID, listInstanceId);
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

  private <T> void appendInList(final StringBuilder query, final List<Object> params,
      final String clause, final String listName, final Collection<T> values) {
    query.append(clause).append(listName).append(" IN (");
    String sep = "";
    for (T value : values) {
      query.append(sep).append("?");
      sep = ", ";
      params.add(value);
    }
    query.append(") ");
  }

  public List<Comment> getLastComments(Connection con, String instanceId, int count) throws
      SQLException {
    String query = "SELECT commentId, commentOwnerId, commentCreationDate, "
        + "commentModificationDate, commentComment, resourceType, resourceId, instanceId "
        + "FROM sb_comment_comment where instanceId = ? ORDER BY commentCreationDate DESC, "
        + "commentId DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Comment> comments = new ArrayList<>(count);
    try {
      stmt = con.prepareStatement(query);
      stmt.setString(1, instanceId);
      if (count > 0) {
        stmt.setMaxRows(count);
      }
      rs = stmt.executeQuery();
      while (rs.next()) {
        CommentPK pk = new CommentPK(String.valueOf(rs.getInt(COMMENT_ID)));
        pk.setComponentName(rs.getString(INSTANCE_ID));
        WAPrimaryKey resourceId = new CommentPK(rs.getString(RESOURCE_ID));
        try {
          Comment cmt = new Comment(pk, rs.getString(RESOURCE_TYPE), resourceId, rs.getInt(
              COMMENT_OWNER_ID), "", rs.getString(COMMENT_TEXT),
              parseDate(rs.getString(COMMENT_CREATION_DATE)),
              parseDate(rs.getString(COMMENT_MODIFICATION_DATE)));
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
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder();

    selectQuery
        .append("SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, ");
    selectQuery.append("commentComment, resourceType, resourceId, instanceId ");
    selectQuery.append("FROM sb_comment_comment ");
    performQueryAndParams(selectQuery, params, resourceTypes, null, userAuthorIds,
        instanceIds, period);
    selectQuery.append("ORDER BY commentModificationDate DESC, commentId DESC");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<SocialInformationComment> listSocialInformationComment = new ArrayList<>(INITIAL_CAPACITY);
    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      int indexParam = 1;
      for (Object param : params) {
        if (param instanceof String) {
          prepStmt.setString(indexParam++, (String) param);
        } else if (param instanceof Integer) {
          prepStmt.setInt(indexParam++, (Integer) param);
        }
      }
      rs = prepStmt.executeQuery();
      Comment comment;
      while (rs.next()) {
        comment = toComment(rs);
        listSocialInformationComment.add(new SocialInformationComment(comment));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return listSocialInformationComment;

  }

  private Comment toComment(final ResultSet rs) throws SQLException {
    final CommentPK pk;
    final Comment comment;
    pk = new CommentPK(String.valueOf(rs.getInt(COMMENT_ID)));
    pk.setComponentName(rs.getString(INSTANCE_ID));
    WAPrimaryKey fatherId = new CommentPK(rs.getString(RESOURCE_ID));
    try {
      comment =
          new Comment(pk, rs.getString(RESOURCE_TYPE), fatherId, rs.getInt(COMMENT_OWNER_ID), "",
              rs.getString(COMMENT_TEXT), parseDate(rs.getString(COMMENT_CREATION_DATE)),
              parseDate(rs.getString(COMMENT_MODIFICATION_DATE)));
    } catch (ParseException ex) {
      throw new SQLException(ex.getMessage(), ex);
    }
    return comment;
  }

  private static boolean isIdDefined(final String id) {
    return StringUtil.isDefined(id) && !ResourceReference.UNKNOWN_ID.equals(id);
  }
}
