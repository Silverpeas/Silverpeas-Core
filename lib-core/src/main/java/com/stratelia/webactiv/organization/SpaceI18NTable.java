package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A SpaceTable object manages the ST_SPACE table.
 */
public class SpaceI18NTable extends Table {
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
  public List getTranslations(int spaceId) throws AdminPersistenceException {
    return getRows(SELECT_TRANSLATIONS, spaceId);
  }

  static final private String SELECT_TRANSLATIONS = "select " + COLUMNS
      + " from ST_SpaceI18N where spaceId = ?";

  /**
   * Inserts in the database a new space row.
   */
  public void createTranslation(SpaceI18NRow translation)
      throws AdminPersistenceException {
    insertRow(INSERT_TRANSLATION, translation);
  }

  static final private String INSERT_TRANSLATION = "insert into"
      + " ST_SpaceI18N(" + COLUMNS + ")" + " values  (?, ?, ?, ?, ?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    SpaceI18NRow s = (SpaceI18NRow) row;

    s.id = getNextId();

    insert.setInt(1, s.id);
    insert.setInt(2, s.spaceId);
    insert.setString(3, s.lang);
    insert.setString(4, truncate(s.name, 100));
    insert.setString(5, truncate(s.description, 500));
  }

  /**
   * Updates a space row.
   */
  public void updateTranslation(SpaceI18NRow space)
      throws AdminPersistenceException {
    updateRow(UPDATE_TRANSLATION, space);
  }

  static final private String UPDATE_TRANSLATION = "update ST_SpaceI18N set"
      + " name = ?," + " description = ? " + " WHERE id = ? ";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    SpaceI18NRow s = (SpaceI18NRow) row;

    update.setString(1, truncate(s.name, 100));
    update.setString(2, truncate(s.description, 500));
    update.setInt(3, s.id);
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
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchTranslation(rs);
  }
}
