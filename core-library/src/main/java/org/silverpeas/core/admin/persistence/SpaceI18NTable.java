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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A SpaceTable object manages the ST_SPACE table.
 */
public class SpaceI18NTable extends Table<SpaceI18NRow> {

  public SpaceI18NTable(OrganizationSchema organization) {
    super(organization, "ST_SpaceI18N");
  }

  static final private String COLUMNS = "id,spaceId,lang,name,description";

  /**
   * Fetch the current space row from a resultSet.
   */
  protected SpaceI18NRow fetchTranslation(ResultSet rs) throws SQLException {
    SpaceI18NRow s = new SpaceI18NRow();
    s.id = rs.getInt(1);
    s.spaceId = rs.getInt(2);
    s.lang = rs.getString(3);
    s.name = rs.getString(4);
    s.description = rs.getString(5);
    return s;
  }

  /**
   * Returns the Space whith the given id.
   */
  public List<SpaceI18NRow> getTranslations(int spaceId) throws AdminPersistenceException {
    return getRows(SELECT_TRANSLATIONS, spaceId);
  }

  static final private String SELECT_TRANSLATIONS = "select " + COLUMNS
      + " from ST_SpaceI18N where spaceId = ?";

  /**
   * Inserts in the database a new space row.
   */
  public void createTranslation(SpaceI18NRow translation) throws AdminPersistenceException {
    insertRow(INSERT_TRANSLATION, translation);
  }

  static final private String INSERT_TRANSLATION = "insert into"
      + " ST_SpaceI18N(" + COLUMNS + ")" + " values  (?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, SpaceI18NRow row)
      throws SQLException {
    row.id = getNextId();
    insert.setInt(1, row.id);
    insert.setInt(2, row.spaceId);
    insert.setString(3, row.lang);
    insert.setString(4, truncate(row.name, 100));
    insert.setString(5, truncate(row.description, 500));
  }

  /**
   * Updates a space row.
   */
  public void updateTranslation(SpaceI18NRow space) throws AdminPersistenceException {
    updateRow(UPDATE_TRANSLATION, space);
  }

  static final private String UPDATE_TRANSLATION = "update ST_SpaceI18N set"
      + " name = ?," + " description = ? " + " WHERE id = ? ";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, SpaceI18NRow row)
      throws SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 500));
    update.setInt(3, row.id);
  }

  /**
   * Delete a translation.
   */
  public void removeTranslation(int id) throws AdminPersistenceException {
    updateRelation(DELETE_TRANSLATION, id);
  }

  static final private String DELETE_TRANSLATION = "delete from ST_SpaceI18N where id = ?";

  /**
   * Delete all space's translations.
   */
  public void removeTranslations(int spaceId) throws AdminPersistenceException {
    updateRelation(DELETE_TRANSLATIONS, spaceId);
  }

  static final private String DELETE_TRANSLATIONS = "delete from ST_SpaceI18N where spaceId = ?";

  /**
   * Fetch the current space row from a resultSet.
   */
  @Override
  protected SpaceI18NRow fetchRow(ResultSet rs) throws SQLException {
    return fetchTranslation(rs);
  }
}
