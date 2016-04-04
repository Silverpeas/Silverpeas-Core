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

/**
 * A KeyStoreTable object manages the ST_KeyStore table.
 */
public class KeyStoreTable extends Table<KeyStoreRow> {

  public KeyStoreTable(OrganizationSchema organization) {
    super(organization, "ST_KeyStore");
  }

  static final private String KEYSTORE_COLUMNS = "userKey, login, domainId";

  /**
   * Fetch the current keyStore row from a resultSet.
   */
  protected KeyStoreRow fetchKeyStore(ResultSet rs) throws SQLException {
    KeyStoreRow k = new KeyStoreRow();

    k.key = rs.getInt("userKey");
    k.login = rs.getString("login");
    k.domainId = rs.getInt("domainId");

    return k;
  }

  /**
   * Get a keystore record by userKey
   */
  public KeyStoreRow getRecordByKey(int nKey) throws AdminPersistenceException {
    return getUniqueRow(SELECT_RECORD_BY_KEY, nKey);
  }

  static final private String SELECT_RECORD_BY_KEY = "select "
      + KEYSTORE_COLUMNS + " from ST_KeyStore where userKey = ?";

  /**
   * Remove a keystore record with the given key
   */
  public void removeKeyStoreRecord(int nKey) throws AdminPersistenceException {
    updateRelation(DELETE_RECORD, nKey);
  }

  static final private String DELETE_RECORD = "delete from ST_KeyStore where userKey = ?";

  /**
   * Fetch the current accessLevel row from a resultSet.
   */
  protected KeyStoreRow fetchRow(ResultSet rs) throws SQLException {
    return fetchKeyStore(rs);
  }

  /**
   * update a KeyStore
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update, KeyStoreRow row) {
    // not implemented
  }

  /**
   * insert a KeyStore
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert, KeyStoreRow row) {
    // not implemented
  }
}