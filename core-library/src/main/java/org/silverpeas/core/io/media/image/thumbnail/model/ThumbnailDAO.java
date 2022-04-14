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
package org.silverpeas.core.io.media.image.thumbnail.model;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ThumbnailDAO {

  private static final String THUMBNAIL_TABLE = "sb_thumbnail_thumbnail";
  private static final String ALL_FIELDS = "instanceid, objectid, objecttype, " +
      "originalattachmentname, modifiedattachmentname, mimetype, xstart, ystart, xlength, ylength";
  private static final String INSERT_THUMBNAIL = "INSERT INTO " + THUMBNAIL_TABLE + " (" + ALL_FIELDS + ") " + "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
  private static final String UPDATE_THUMBNAIL = "UPDATE " + THUMBNAIL_TABLE + " " +
      "SET xstart = ?, ystart = ?, xlength = ?, ylength = ?, originalattachmentname = ?, " +
      "modifiedattachmentname = ? WHERE objectId = ? AND objectType = ? AND instanceId = ? ";
  private static final String DELETE_THUMBNAIL = "DELETE FROM " + THUMBNAIL_TABLE + " " +
      "WHERE objectId = ? AND objectType = ? AND instanceId = ? ";
  private static final String DELETE_COMPONENT_THUMBNAILS =
      "DELETE FROM " + THUMBNAIL_TABLE + " WHERE instanceId = ?";
  private static final String SELECT_THUMBNAIL_BY_PK = "SELECT " + ALL_FIELDS + " FROM " +
      THUMBNAIL_TABLE + " WHERE objectId = ? AND objectType = ? AND instanceId = ?";
  private static final String MOVE_THUMBNAIL = "UPDATE " + THUMBNAIL_TABLE + " " +
      " SET instanceId = ? WHERE objectId = ? AND objectType = ? AND instanceId = ? ";

  protected ThumbnailDAO() {

  }

  public ThumbnailDetail insertThumbnail(Connection con, ThumbnailDetail thumbnailDetail)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(INSERT_THUMBNAIL)) {
      prepStmt.setString(1, thumbnailDetail.getInstanceId());
      prepStmt.setInt(2, thumbnailDetail.getObjectId());
      prepStmt.setInt(3, thumbnailDetail.getObjectType());
      prepStmt.setString(4, thumbnailDetail.getOriginalFileName());
      if (thumbnailDetail.getCropFileName() != null) {
        prepStmt.setString(5, thumbnailDetail.getCropFileName());
      } else {
        prepStmt.setNull(5, Types.VARCHAR);
      }
      if (thumbnailDetail.getMimeType() != null) {
        prepStmt.setString(6, thumbnailDetail.getMimeType());
      } else {
        prepStmt.setNull(6, Types.VARCHAR);
      }
      if (thumbnailDetail.getXStart() != -1) {
        prepStmt.setInt(7, thumbnailDetail.getXStart());
      } else {
        prepStmt.setNull(7, Types.INTEGER);
      }
      if (thumbnailDetail.getYStart() != -1) {
        prepStmt.setInt(8, thumbnailDetail.getYStart());
      } else {
        prepStmt.setNull(8, Types.INTEGER);
      }
      if (thumbnailDetail.getXLength() != -1) {
        prepStmt.setInt(9, thumbnailDetail.getXLength());
      } else {
        prepStmt.setNull(9, Types.INTEGER);
      }
      if (thumbnailDetail.getYLength() != -1) {
        prepStmt.setInt(10, thumbnailDetail.getYLength());
      } else {
        prepStmt.setNull(10, Types.INTEGER);
      }
      prepStmt.executeUpdate();
    }
    return thumbnailDetail;
  }

  public void updateThumbnail(Connection con, ThumbnailDetail thumbToUpdate) throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(UPDATE_THUMBNAIL)) {
      prepStmt.setInt(1, thumbToUpdate.getXStart());
      prepStmt.setInt(2, thumbToUpdate.getYStart());
      prepStmt.setInt(3, thumbToUpdate.getXLength());
      prepStmt.setInt(4, thumbToUpdate.getYLength());
      prepStmt.setString(5, thumbToUpdate.getOriginalFileName());
      prepStmt.setString(6, thumbToUpdate.getCropFileName());
      prepStmt.setInt(7, thumbToUpdate.getObjectId());
      prepStmt.setInt(8, thumbToUpdate.getObjectType());
      prepStmt.setString(9, thumbToUpdate.getInstanceId());
      prepStmt.executeUpdate();
    }
  }

  public void deleteThumbnail(Connection con, int objectId, int objectType, String instanceId)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(DELETE_THUMBNAIL)) {
      prepStmt.setInt(1, objectId);
      prepStmt.setInt(2, objectType);
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    }
  }

  public void moveThumbnail(Connection con, ThumbnailDetail thumbToUpdate, String toInstanceId)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(MOVE_THUMBNAIL)) {
      prepStmt.setString(1, toInstanceId);
      prepStmt.setInt(2, thumbToUpdate.getObjectId());
      prepStmt.setInt(3, thumbToUpdate.getObjectType());
      prepStmt.setString(4, thumbToUpdate.getInstanceId());
      prepStmt.executeUpdate();
    }
  }


  public void deleteAllThumbnails(Connection con, String instanceId) throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(DELETE_COMPONENT_THUMBNAILS)) {
      prepStmt.setString(1, instanceId);
      prepStmt.executeUpdate();
    }
  }

  public ThumbnailDetail selectByKey(Connection con, String instanceId, int objectId,
      int objectType) throws SQLException {
    final ThumbnailDetail thumbnailDetail;
    try (final PreparedStatement prepStmt = con.prepareStatement(SELECT_THUMBNAIL_BY_PK)) {
      prepStmt.setInt(1, objectId);
      prepStmt.setInt(2, objectType);
      prepStmt.setString(3, instanceId);

      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          thumbnailDetail = resultSet2ThumbDetail(rs);
        } else {
          thumbnailDetail = null;
        }
      }
    }
    return thumbnailDetail;
  }

  public List<ThumbnailDetail> selectByReference(Connection con,
      Set<ThumbnailReference> references) throws SQLException {
    final List<ThumbnailDetail> result = new ArrayList<>(references.size());
    final Set<String> instanceIds = references.stream()
        .map(ThumbnailReference::getComponentInstanceId)
        .collect(Collectors.toSet());
    final Set<Integer> objectIds = references.stream()
        .map(ThumbnailReference::getId)
        .map(Integer::parseInt)
        .collect(Collectors.toSet());
    final Set<Integer> objectTypes = references.stream()
        .map(ThumbnailReference::getObjectType)
        .collect(Collectors.toSet());
    JdbcSqlQuery.executeBySplittingOn(instanceIds, (instanceIdBatch, ignore) ->
        JdbcSqlQuery.executeBySplittingOn(objectIds, (objectIdBatch, ignoreToo) ->
            JdbcSqlQuery.select(ALL_FIELDS)
                .from(THUMBNAIL_TABLE)
                .where("instanceId").in(instanceIdBatch)
                .and("objectId").in(objectIdBatch)
                .and("objectType").in(objectTypes)
                .executeWith(con, rs -> {
                  final ThumbnailDetail thumbnailDetail = resultSet2ThumbDetail(rs);
                  if (references.contains(thumbnailDetail.getReference())) {
                    result.add(thumbnailDetail);
                  }
                  return null;
                })));
    return result;
  }

  private static ThumbnailDetail resultSet2ThumbDetail(ResultSet rs) throws SQLException {
    ThumbnailDetail thumbnailDetail = new ThumbnailDetail(rs.getString("instanceid"), rs.getInt(
        "objectid"), rs.getInt("objecttype"));
    thumbnailDetail.setOriginalFileName(rs.getString("originalattachmentname"));
    thumbnailDetail.setCropFileName(rs.getString("modifiedattachmentname"));
    thumbnailDetail.setMimeType(rs.getString("mimetype"));
    thumbnailDetail.setXStart(rs.getInt("xstart"));
    thumbnailDetail.setYStart(rs.getInt("ystart"));
    thumbnailDetail.setXLength(rs.getInt("xlength"));
    thumbnailDetail.setYLength(rs.getInt("ylength"));
    return thumbnailDetail;
  }
}