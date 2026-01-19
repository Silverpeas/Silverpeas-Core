/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.contribution.publication.model.PublicationI18N;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.MapUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * DAO to manage the persistence of the translations of the l10n publications.
 *
 * @author Nicolas Eysseric
 */
@Repository
public class PublicationI18NDAO {

  private static final String TABLE_NAME = "SB_Publication_PubliI18N";
  private static final String FIELDS = "id, pubId, lang, name, description, keywords";

  /**
   * Deletes all translations of publications linked to the component instance represented by the
   * given identifier.
   *
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on technical SQL error
   */
  public void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.deleteFrom(TABLE_NAME).where("pubId in (" +
        JdbcSqlQuery.select("pubId from " + PublicationDAO.PUBLICATION_TABLE_NAME)
            .where("instanceId = ?").getSqlQuery() + ")", componentInstanceId).execute();
  }

  public List<PublicationI18N> getTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    String selectStatement = "select " + FIELDS + " from " + TABLE_NAME + " where pubId = ? ";
    try (PreparedStatement stmt = con.prepareStatement(selectStatement)) {
      stmt.setInt(1, Integer.parseInt(pubPK.getId()));
      try (ResultSet rs = stmt.executeQuery()) {
        final List<PublicationI18N> list = new ArrayList<>();
        while (rs.next()) {
          list.add(resultSetToEntity(rs));
        }
        return list;
      }
    }
  }

  public Map<String, List<PublicationI18N>> getIndexedTranslations(Connection con,
      List<String> publicationIds) throws SQLException {
    return JdbcSqlQuery.executeBySplittingOn(publicationIds, (idBatch, result) -> JdbcSqlQuery
        .select(FIELDS)
        .from(TABLE_NAME)
        .where("pubId").in(idBatch.stream().map(Integer::parseInt).collect(toList()))
        .executeWith(con, r -> {
          final PublicationI18N pub = resultSetToEntity(r);
          MapUtil.putAddList(result, pub.getObjectId(), pub);
          return null;
        }));
  }

  private static PublicationI18N resultSetToEntity(final ResultSet r) throws SQLException {
    final PublicationI18N pub = new PublicationI18N();
    pub.setId(String.valueOf(r.getInt(1)));
    pub.setObjectId(Integer.toString(r.getInt(2)));
    pub.setLanguage(r.getString(3));
    pub.setName(r.getString(4));
    pub.setDescription(r.getString(5));
    pub.setKeywords(r.getString(6));
    return pub;
  }

  public void addTranslation(Connection con, PublicationI18N translation)
      throws SQLException {
    String insertStatement = "insert into " + TABLE_NAME + " values (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setInt(1, DBUtil.getNextId(TABLE_NAME, "id"));
      prepStmt.setInt(2, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(3, translation.getLanguage());
      prepStmt.setString(4, translation.getName());
      prepStmt.setString(5, translation.getDescription());
      prepStmt.setString(6, translation.getKeywords());
      prepStmt.executeUpdate();
    }
  }

  public void updateTranslation(Connection con,
      PublicationI18N translation) throws SQLException {
    int rowCount;

    String updateQuery = "update " + TABLE_NAME +
        " set name = ? , description = ? , keywords = ? where id = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(updateQuery)) {
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setString(3, translation.getKeywords());
      prepStmt.setInt(4, Integer.parseInt(translation.getId()));

      rowCount = prepStmt.executeUpdate();
    }

    if (rowCount == 0) {
      throw new PublicationRuntimeException("The update of the translation with id = "
          + translation.getId() + " failed!");
    }
  }

  public void removeTranslation(Connection con, String translationId)
      throws SQLException {
    removeTranslation(con, Integer.parseInt(translationId));
  }

  public void removeTranslation(Connection con, int translationId)
      throws SQLException {
    String deleteStatement = "delete from " + TABLE_NAME + " where id = ? ";
    try (PreparedStatement stmt = con.prepareStatement(deleteStatement)) {
      stmt.setInt(1, translationId);
      stmt.executeUpdate();
    }
  }

  public void removeTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    String deleteStatement = "delete from " + TABLE_NAME + " where pubId = ? ";
    try (PreparedStatement stmt = con.prepareStatement(deleteStatement)) {
      stmt.setInt(1, Integer.parseInt(pubPK.getId()));
      stmt.executeUpdate();
    }
  }
}