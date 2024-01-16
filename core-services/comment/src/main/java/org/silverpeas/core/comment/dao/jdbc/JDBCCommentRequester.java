/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.dao.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Pair;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.DateUtil.*;

/**
 * A specific JDBC requester dedicated on the comments persisted in the underlying data source.
 */
@Technical
@Bean
public class JDBCCommentRequester {

  private static final int INITIAL_CAPACITY = 1000;
  private static final String COMMENT_TABLE = "sb_comment_comment";
  private static final String COMMENT_ID = "commentId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String RESOURCE_ID = "resourceId";
  private static final String COMMENT_MODIFICATION_DATE = "commentModificationDate";
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String COMMENT_OWNER_ID = "commentOwnerId";
  private static final String COMMENT_TEXT = "commentComment";
  private static final String COMMENT_CREATION_DATE = "commentCreationDate";
  private static final String COMMENT_COUNT = "nb_comment";
  private static final String SQL_COMMENT_QUERY =
      "SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, " +
          "commentComment, resourceType, resourceId, instanceId FROM " + COMMENT_TABLE;
  private static final String SELECT_COMMENT_COUNT =
      "SELECT COUNT(commentId) as nb_comment, resourceType, resourceId, instanceId FROM " +
          COMMENT_TABLE;

  protected JDBCCommentRequester() {
  }

  /**
   * Saves the specified comment with the specified connection onto a data source. Once saved, the
   * comment passed as argument isn't updated with the persistence information and hence the
   * returned comment instance should be used for further handling.
   * @param con the connection to a data source.
   * @param cmt the comment to save.
   * @return the comment that is saved into the data source with its unique identifier.
   * @throws SQLException if an error occurs while saving the comment.
   */
  public Comment saveComment(Connection con, Comment cmt) throws SQLException {
    String insertQuery = "INSERT INTO sb_comment_comment (commentId , commentOwnerId, " +
        "commentCreationDate, commentModificationDate, commentComment, resourceType, resourceId, " +
        "instanceId) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
    int newId = DBUtil.getNextId(COMMENT_TABLE, COMMENT_ID);
    try (PreparedStatement prepStmt = con.prepareStatement(insertQuery)) {
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(cmt.getCreatorId()));
      prepStmt.setString(3, date2SQLDate(cmt.getCreationDate()));
      String updateDate;
      if (cmt.getLastUpdateDate() != null) {
        updateDate = date2SQLDate(cmt.getLastUpdateDate());
      } else {
        updateDate = null;
      }
      prepStmt.setString(4, updateDate);
      prepStmt.setString(5, cmt.getMessage());
      prepStmt.setString(6, cmt.getResourceType());
      prepStmt.setString(7, cmt.getResourceReference().getLocalId());
      prepStmt.setString(8, cmt.getIdentifier().getComponentInstanceId());
      prepStmt.executeUpdate();
    }
    CommentId id =
        new CommentId(cmt.getIdentifier().getComponentInstanceId(), String.valueOf(newId));
    return getComment(con, id);
  }

  /**
   * Deletes the comment identified by the specified primary key from the data source onto which the
   * given connection is opened.
   * @param con the connection to the data source.
   * @param id the unique identifier of the comment in the data source.
   * @throws SQLException if an error occurs while removing the comment from the data source.
   */
  public void deleteComment(Connection con, CommentId id) throws SQLException {
    String deleteQuery = "DELETE FROM sb_comment_comment WHERE commentId = ?";
    try (PreparedStatement prepStmt = con.prepareStatement(deleteQuery)) {
      prepStmt.setInt(1, Integer.parseInt(id.getLocalId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Updates the comment representation in the data source by the specified one.
   * @param con the connection to the data source.
   * @param cmt the updated comment.
   * @throws SQLException if an error occurs while updating the comment in the data source.
   */
  public void updateComment(Connection con, Comment cmt) throws SQLException {
    String updateQuery =
        "UPDATE sb_comment_comment SET commentOwnerId=?, commentModificationDate=?, " +
            "commentComment=?, resourceType=?, resourceId=?, instanceId=? WHERE commentId= ?";
    try (PreparedStatement prepStmt = con.prepareStatement(updateQuery)) {
      prepStmt.setInt(1, Integer.parseInt(cmt.getCreatorId()));
      prepStmt.setString(2, date2SQLDate(cmt.getLastUpdateDate()));
      prepStmt.setString(3, cmt.getMessage());
      prepStmt.setString(4, cmt.getResourceType());
      prepStmt.setString(5, cmt.getResourceReference().getLocalId());
      prepStmt.setString(6, cmt.getIdentifier().getComponentInstanceId());
      prepStmt.setInt(7, Integer.parseInt(cmt.getIdentifier().getLocalId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Moves all the comments from the resource they commented to another resource as they comment
   * this new resource instead of the previous one.
   * @param con the connection to the data source.
   * @param fromResourceType the source type of the commented resource
   * @param fromResource the source unique identifier of the comment in the data source.
   * @param toResourceType the destination type of the commented resource
   * @param toResource the destination unique identifier of another comment in the data source.
   * @throws SQLException if an error occurs during the operation.
   */
  public void moveComments(Connection con, String fromResourceType, ResourceReference fromResource,
      String toResourceType, ResourceReference toResource) throws SQLException {
    String updateQuery =
        "UPDATE sb_comment_comment SET resourceType=?, resourceId=?, instanceId=? " +
            "WHERE resourceType=? AND resourceId=? AND instanceId=?";
    try (PreparedStatement prepStmt = con.prepareStatement(updateQuery)) {
      prepStmt.setString(1, toResourceType);
      prepStmt.setString(2, toResource.getLocalId());
      prepStmt.setString(3, toResource.getComponentInstanceId());
      prepStmt.setString(4, fromResourceType);
      prepStmt.setString(5, fromResource.getLocalId());
      prepStmt.setString(6, fromResource.getComponentInstanceId());
      prepStmt.executeUpdate();
    }
  }

  /**
   * Gets the comment identified by the specified identifier.
   * @param con the connection to use for getting the comment.
   * @param commentId the identifier of the comment in the data source.
   * @return the comment or null if no such comment is found.
   * @throws SQLException if an error occurs during the comment fetching.
   */
  public Comment getComment(Connection con, CommentId commentId) throws SQLException {
    String selectQuery = SQL_COMMENT_QUERY + " WHERE commentId = ?";
    try (PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setInt(1, Integer.parseInt(commentId.getLocalId()));
      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          return toComment(rs);
        }
        return null;
      }
    }
  }

  public List<CommentedPublicationInfo> getMostCommentedAllPublications(Connection con,
      String resType) throws SQLException {
    if (StringUtil.isDefined(resType)) {
      String selectQuery = SELECT_COMMENT_COUNT + " WHERE resourceType = ?" +
          " GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc;";
      try (PreparedStatement stmt = con.prepareStatement(selectQuery)) {
        stmt.setString(1, resType);
        try (ResultSet rs = stmt.executeQuery()) {
          return fetchCommentedPublicationInfos(rs);
        }
      }
    } else {
      String selectQuery = SELECT_COMMENT_COUNT + " GROUP BY resourceType, resourceId, instanceId" +
          " ORDER BY nb_comment desc;";
      try (Statement prepStmt = con.createStatement();
           ResultSet rs = prepStmt.executeQuery(selectQuery)) {
        return fetchCommentedPublicationInfos(rs);
      }
    }
  }

  public List<CommentedPublicationInfo> getMostCommentedPublications(Connection con,
      final List<ResourceReference> pubRefs) throws SQLException {
    String query = SELECT_COMMENT_COUNT;
    List<String> resourceIds = new ArrayList<>(pubRefs.size());
    List<String> instanceIds = new ArrayList<>(pubRefs.size());
    for (ResourceReference ref : pubRefs) {
      if (isIdDefined(ref.getLocalId())) {
        resourceIds.add(ref.getLocalId());
      }
      if (isIdDefined(ref.getComponentInstanceId())) {
        instanceIds.add(ref.getComponentInstanceId());
      }
    }
    if (!resourceIds.isEmpty()) {
      query += " where resourceId in (?)";
    }
    if (!instanceIds.isEmpty()) {
      query += (query.contains("where") ? " and " : " where ") + "instanceId in (?)";
    }
    query += " GROUP BY resourceType, resourceId, instanceId ORDER BY nb_comment desc";
    try (PreparedStatement stmt = con.prepareStatement(query)) {
      int i = 1;
      if (!resourceIds.isEmpty()) {
        stmt.setString(i++, StringUtils.join(resourceIds, ","));
      }
      if (!instanceIds.isEmpty()) {
        stmt.setString(i, StringUtils.join(instanceIds, ","));
      }
      try (ResultSet rs = stmt.executeQuery()) {
        return fetchCommentedPublicationInfos(rs);
      }
    }
  }

  public int getCommentsCount(Connection con, String resourceType, ResourceReference resourceRef)
      throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery =
        new StringBuilder("SELECT COUNT(commentId) AS nb_comment FROM sb_comment_comment");
    performQueryAndParams(selectQuery, params, resourceType, resourceRef);
    int commentsCount = 0;
    try (PreparedStatement prepStmt = con.prepareStatement(selectQuery.toString())) {
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          commentsCount = rs.getInt(COMMENT_COUNT);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }

    return commentsCount;
  }

  public Map<ResourceReference, Integer> getCommentCountIndexedByResource(Connection con,
      String resourceType, String instanceId) throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder(
        "SELECT resourceId, COUNT(commentId) AS nb_comment FROM sb_comment_comment");
    performQueryAndParams(selectQuery, params, resourceType,
        new ResourceReference(null, instanceId));
    selectQuery.append(" GROUP BY resourceId");
    final List<Pair<ResourceReference, Integer>> results = new LinkedList<>();
    try (final PreparedStatement prepStmt = con.prepareStatement(selectQuery.toString())) {
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          results.add(Pair.of(new ResourceReference(rs.getString(RESOURCE_ID), instanceId),
              rs.getInt(COMMENT_COUNT)));
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return results.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
  }

  public List<Comment> getAllComments(Connection con, String resourceType,
      ResourceReference resourceRef) throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder();
    selectQuery.append(SQL_COMMENT_QUERY);
    performQueryAndParams(selectQuery, params, resourceType, resourceRef);
    selectQuery.append("ORDER BY commentCreationDate DESC, commentId DESC");
    List<Comment> comments = new ArrayList<>(INITIAL_CAPACITY);
    try (PreparedStatement prepStmt = con.prepareStatement(selectQuery.toString())) {
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          Comment cmt = toComment(rs);
          comments.add(cmt);
        }
      }
    }

    return comments;
  }

  public int deleteAllComments(Connection con, String resourceType,
      ResourceReference resourceReference) throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder deleteQuery = new StringBuilder("DELETE FROM sb_comment_comment");
    performQueryAndParams(deleteQuery, params, resourceType, resourceReference);

    try (PreparedStatement prepStmt = con.prepareStatement(deleteQuery.toString())) {
      int indexParam = 1;
      for (Object param : params) {
        prepStmt.setString(indexParam++, (String) param);
      }
      return prepStmt.executeUpdate();
    }
  }

  private void performQueryAndParams(StringBuilder query, List<Object> params, String resourceType,
      ResourceReference resourceRef) {
    List<String> listResourceType = new ArrayList<>();
    if (StringUtil.isDefined(resourceType)) {
      listResourceType.add(resourceType);
    }
    performQueryAndParams(query, params, listResourceType, resourceRef, null, null, null);
  }

  private void performQueryAndParams(StringBuilder query, List<Object> params,
      List<String> listResourceType, ResourceReference resourceRef, List<String> listUserId,
      List<String> listInstanceId, Period period) {
    String clause = " WHERE ";
    if (CollectionUtil.isNotEmpty(listResourceType)) {
      appendInList(query, params, clause, RESOURCE_TYPE, listResourceType);
      clause = "AND ";
    }
    if (resourceRef != null) {
      if (isIdDefined(resourceRef.getLocalId())) {
        query.append(clause).append("resourceId = ? ");
        clause = "AND ";
        params.add(resourceRef.getLocalId());
      }
      if (isIdDefined(resourceRef.getComponentInstanceId())) {
        query.append(clause).append("instanceId = ? ");
        clause = "AND ";
        params.add(resourceRef.getComponentInstanceId());
      }
    }
    if (CollectionUtil.isNotEmpty(listUserId)) {
      final Set<Integer> listUserIdsAsInt =
          listUserId.stream().map(Integer::parseInt).collect(Collectors.toSet());
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
    if (period != null) {
      query.append(clause).append("((commentModificationDate BETWEEN ? AND ?) ");
      params.add(temporal2SQLDate(period.getStartDate()));
      params.add(temporal2SQLDate(period.getEndDate()));
      query.append("OR (commentCreationDate BETWEEN ? AND ?)) ");
      params.add(temporal2SQLDate(period.getStartDate()));
      params.add(temporal2SQLDate(period.getEndDate()));
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

  public List<Comment> getLastComments(Connection con, String instanceId, int count)
      throws SQLException {
    String query = SQL_COMMENT_QUERY +
        " WHERE instanceId = ? ORDER BY commentCreationDate DESC, commentId DESC";
    List<Comment> comments = new ArrayList<>(count);
    try (PreparedStatement stmt = con.prepareStatement(query)) {
      stmt.setString(1, instanceId);
      if (count > 0) {
        stmt.setMaxRows(count);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Comment comment = toComment(rs);
          comments.add(comment);
        }
      }
    }

    return comments;
  }

  public List<SocialInformationComment> getSocialInformationComments(Connection con,
      List<String> resourceTypes, List<String> userAuthorIds, List<String> instanceIds,
      Period period) throws SQLException {
    final List<Object> params = new ArrayList<>();
    final StringBuilder selectQuery = new StringBuilder();

    selectQuery.append(SQL_COMMENT_QUERY);
    performQueryAndParams(selectQuery, params, resourceTypes, null, userAuthorIds, instanceIds,
        period);
    selectQuery.append("ORDER BY commentModificationDate DESC, commentId DESC");

    List<SocialInformationComment> listSocialInformationComment = new ArrayList<>(INITIAL_CAPACITY);
    try (PreparedStatement prepStmt = con.prepareStatement(selectQuery.toString())) {
      int indexParam = 1;
      for (Object param : params) {
        if (param instanceof String) {
          prepStmt.setString(indexParam++, (String) param);
        } else if (param instanceof Integer) {
          prepStmt.setInt(indexParam++, (Integer) param);
        }
      }
      try (ResultSet rs = prepStmt.executeQuery()) {
        Comment comment;
        while (rs.next()) {
          comment = toComment(rs);
          listSocialInformationComment.add(new SocialInformationComment(comment));
        }
      }
    }

    return listSocialInformationComment;

  }

  private List<CommentedPublicationInfo> fetchCommentedPublicationInfos(final ResultSet rs)
      throws SQLException {
    List<CommentedPublicationInfo> listPublisCommentsCount = new ArrayList<>();
    while (rs.next()) {
      int countComment = rs.getInt(COMMENT_COUNT);
      String resourceType = rs.getString(RESOURCE_TYPE);
      String resourceId = rs.getString(RESOURCE_ID);
      String instanceId = rs.getString(INSTANCE_ID);
      listPublisCommentsCount.add(
          new CommentedPublicationInfo(resourceType, resourceId, instanceId, countComment));
    }
    return listPublisCommentsCount;
  }

  private Comment toComment(final ResultSet rs) throws SQLException {
    String instanceId = rs.getString(INSTANCE_ID);
    CommentId id = new CommentId(instanceId, String.valueOf(rs.getInt(COMMENT_ID)));
    ResourceReference resourceId = new ResourceReference(rs.getString(RESOURCE_ID), instanceId);
    String resourceType = rs.getString(RESOURCE_TYPE);
    String creatorId = String.valueOf(rs.getInt(COMMENT_OWNER_ID));
    String text = rs.getString(COMMENT_TEXT);
    try {
      Date creationDate = parseDate(rs.getString(COMMENT_CREATION_DATE));
      Date updateDate = parseDate(rs.getString(COMMENT_MODIFICATION_DATE));
      Comment comment = new Comment(id, creatorId, resourceType, resourceId, creationDate);
      comment.setLastUpdateDate(updateDate);
      comment.setMessage(text);
      return comment;
    } catch (ParseException ex) {
      throw new SQLException(ex.getMessage(), ex);
    }
  }

  private static boolean isIdDefined(final String id) {
    return StringUtil.isDefined(id) && !ResourceReference.UNKNOWN_ID.equals(id);
  }
}
