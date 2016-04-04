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
 * A ComponentInstanceI18NTable object manages the ST_ComponentInstance table.
 */
public class ComponentInstanceI18NTable extends Table<ComponentInstanceI18NRow> {

  public ComponentInstanceI18NTable(OrganizationSchema organization) {
    super(organization, "ST_ComponentInstanceI18N");
  }

  static final private String COLUMNS = "id,componentId,lang,name,description";

  /**
   * Fetch the current component row from a resultSet.
   * @param rs
   * @return the current component row from a resultSet.
   * @throws SQLException
   */
  protected ComponentInstanceI18NRow fetchTranslation(ResultSet rs) throws SQLException {
    ComponentInstanceI18NRow s = new ComponentInstanceI18NRow();

    s.id = rs.getInt(1);
    s.componentId = rs.getInt(2);
    s.lang = rs.getString(3);
    s.name = rs.getString(4);
    s.description = rs.getString(5);

    return s;
  }

  /**
   * Returns the Component whith the given id.
   * @param componentId
   * @return the Component whith the given id.
   * @throws AdminPersistenceException
   */
  public List<ComponentInstanceI18NRow> getTranslations(int componentId) throws
      AdminPersistenceException {
    return getRows(SELECT_TRANSLATIONS, componentId);
  }

  static final private String SELECT_TRANSLATIONS = "select " + COLUMNS
      + " from ST_ComponentInstanceI18N where componentId = ?";

  /**
   * Inserts in the database a new component row.
   * @param translation
   * @throws AdminPersistenceException
   */
  public void createTranslation(ComponentInstanceI18NRow translation) throws
      AdminPersistenceException {
    insertRow(INSERT_TRANSLATION, translation);
  }

  static final private String INSERT_TRANSLATION = "insert into"
      + " ST_ComponentInstanceI18N(" + COLUMNS + ")"
      + " values  (?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      ComponentInstanceI18NRow row) throws SQLException {
    row.id = getNextId();
    insert.setInt(1, row.id);
    insert.setInt(2, row.componentId);
    insert.setString(3, row.lang);
    insert.setString(4, truncate(row.name, 100));
    insert.setString(5, truncate(row.description, 400));
  }

  /**
   * Updates a component row.
   * @param component
   * @throws AdminPersistenceException
   */
  public void updateTranslation(ComponentInstanceI18NRow component)
      throws AdminPersistenceException {
    updateRow(UPDATE_TRANSLATION, component);
  }

  static final private String UPDATE_TRANSLATION = "update ST_ComponentInstanceI18N set"
      + " name = ?," + " description = ? " + " WHERE id = ? ";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      ComponentInstanceI18NRow row) throws SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 400));
    update.setInt(3, row.id);
  }

  /**
   * Delete a translation.
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeTranslation(int id) throws AdminPersistenceException {
    updateRelation(DELETE_TRANSLATION, id);
  }

  static final private String DELETE_TRANSLATION =
      "delete from ST_ComponentInstanceI18N where id = ?";

  /**
   * Delete all component's translations.
   * @param componentId
   * @throws AdminPersistenceException
   */
  public void removeTranslations(int componentId) throws AdminPersistenceException {
    updateRelation(DELETE_TRANSLATIONS, componentId);
  }

  static final private String DELETE_TRANSLATIONS =
      "delete from ST_ComponentInstanceI18N where componentId = ?";

  /**
   * Fetch the current component row from a resultSet.
   */
  @Override
  protected ComponentInstanceI18NRow fetchRow(ResultSet rs) throws SQLException {
    return fetchTranslation(rs);
  }
}
