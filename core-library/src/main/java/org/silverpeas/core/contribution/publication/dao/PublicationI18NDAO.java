/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.contribution.publication.dao;

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
 * This is the Publication Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationI18NDAO {

  private static final String TABLENAME = "SB_Publication_PubliI18N";
  private static final String FIELDS = "id, pubId, lang, name, description, keywords";

  private PublicationI18NDAO() {
    throw new IllegalStateException("DAO class");
  }

  /**
   * Deletes all translations of publications linked to the component instance represented by the
   * given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on technical SQL error
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(TABLENAME).where("pubId in (" +
        JdbcSqlQuery.createSelect("pubId from " + PublicationDAO.PUBLICATION_TABLE_NAME)
            .where("instanceId = ?").getSqlQuery() + ")", componentInstanceId).execute();
  }

  public static List<PublicationI18N> getTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    final String selectStatement = "select * from " + TABLENAME + " where pubId = ? ";
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

  public static Map<String, List<PublicationI18N>> getIndexedTranslations(Connection con,
      List<String> publicationIds) throws SQLException {
    return JdbcSqlQuery.executeBySplittingOn(publicationIds, (idBatch, result) -> JdbcSqlQuery
        .createSelect(FIELDS)
        .from(TABLENAME)
        .where("pubId").in(idBatch.stream().map(Integer::parseInt).collect(toList()))
        .executeWith(con, r -> {
          final PublicationI18N pub = resultSetToEntity(r);
          MapUtil.putAddList(result, pub.getObjectId(), pub);
          return null;
        }));
  }

  private static PublicationI18N resultSetToEntity(final ResultSet r) throws SQLException {
    final PublicationI18N pub = new PublicationI18N();
    pub.setId(r.getInt(1));
    pub.setObjectId(Integer.toString(r.getInt(2)));
    pub.setLanguage(r.getString(3));
    pub.setName(r.getString(4));
    pub.setDescription(r.getString(5));
    pub.setKeywords(r.getString(6));
    return pub;
  }

  public static void addTranslation(Connection con, PublicationI18N translation)
      throws SQLException {
    StringBuilder insertStatement = new StringBuilder(128);
    insertStatement.append("insert into ").append(TABLENAME).append(
        " values (?, ?, ?, ?, ?, ?)");

    try (PreparedStatement prepStmt = con.prepareStatement(insertStatement.toString())) {
      prepStmt.setInt(1, DBUtil.getNextId(TABLENAME, "id"));
      prepStmt.setInt(2, Integer.parseInt(translation.getObjectId()));
      prepStmt.setString(3, translation.getLanguage());
      prepStmt.setString(4, translation.getName());
      prepStmt.setString(5, translation.getDescription());
      prepStmt.setString(6, translation.getKeywords());
      prepStmt.executeUpdate();
    }
  }

  public static void updateTranslation(Connection con,
      PublicationI18N translation) throws SQLException {
    int rowCount;

    StringBuilder updateQuery = new StringBuilder(128);
    updateQuery.append("update ").append(TABLENAME);
    updateQuery.append(" set name = ? , description = ? , keywords = ? ");
    updateQuery.append(" where id = ? ");

    try (PreparedStatement prepStmt = con.prepareStatement(updateQuery.toString())) {
      prepStmt.setString(1, translation.getName());
      prepStmt.setString(2, translation.getDescription());
      prepStmt.setString(3, translation.getKeywords());
      prepStmt.setInt(4, translation.getId());

      rowCount = prepStmt.executeUpdate();
    }

    if (rowCount == 0) {
      throw new PublicationRuntimeException("The update of the translation with id = "
          + translation.getId() + " failed!");
    }
  }

  public static void removeTranslation(Connection con, String translationId)
      throws SQLException {
    removeTranslation(con, Integer.parseInt(translationId));
  }

  public static void removeTranslation(Connection con, int translationId)
      throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(TABLENAME).append(
        " where id = ? ");
    try (PreparedStatement stmt = con.prepareStatement(deleteStatement.toString())) {
      stmt.setInt(1, translationId);
      stmt.executeUpdate();
    }
  }

  public static void removeTranslations(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(TABLENAME).append(
        " where pubId = ? ");
    try (PreparedStatement stmt = con.prepareStatement(deleteStatement.toString())) {
      stmt.setInt(1, Integer.parseInt(pubPK.getId()));
      stmt.executeUpdate();
    }
  }
}