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
 * A DomainTable object manages the ST_Domain table.
 */
public class DomainTable extends Table<DomainRow> {

  public DomainTable(OrganizationSchema schema) {
    super(schema, "ST_Domain");
  }

  static final private String DOMAIN_COLUMNS =
      "id,name,description,propFileName,className,authenticationServer,theTimeStamp,silverpeasServerURL";

  /**
   * Fetch the current domain row from a resultSet.
   * @param rs
   * @return the current domain row from a resultSet.
   * @throws SQLException
   */
  protected DomainRow fetchDomain(ResultSet rs) throws SQLException {
    DomainRow d = new DomainRow();
    d.id = rs.getInt(1);
    d.name = rs.getString(2);
    d.description = rs.getString(3);
    d.propFileName = rs.getString(4);
    d.className = rs.getString(5);
    d.authenticationServer = rs.getString(6);
    d.theTimeStamp = rs.getString(7);
    d.silverpeasServerURL = rs.getString(8);

    return d;
  }

  /**
   * Returns the domain whith the given id.
   * @param id
   * @return the domain whith the given id.
   * @throws AdminPersistenceException
   */
  public DomainRow getDomain(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_DOMAIN_BY_ID, id);
  }

  static final private String SELECT_DOMAIN_BY_ID = "select " + DOMAIN_COLUMNS
      + " from ST_Domain where id = ?";

  /**
   * Returns all the Domains.
   * @return all the Domains.
   * @throws AdminPersistenceException
   */
  public DomainRow[] getAllDomains() throws AdminPersistenceException {
    List<DomainRow> rows = getRows(SELECT_ALL_DOMAINS);
    return rows.toArray(new DomainRow[rows.size()]);
  }

  static final private String SELECT_ALL_DOMAINS = "select " + DOMAIN_COLUMNS
      + " from ST_Domain where not id=-1 order by name asc";

  /**
   * Insert a new domain row.
   * @param domain
   * @throws AdminPersistenceException
   */
  public void createDomain(DomainRow domain) throws AdminPersistenceException {
    insertRow(INSERT_DOMAIN, domain);
  }

  static final private String INSERT_DOMAIN = "INSERT INTO ST_Domain (id, name, description, " +
      "propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL) VALUES " +
      " (? ,? ,?, ? ,?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, DomainRow row) throws
      SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }

    insert.setInt(1, row.id);
    insert.setString(2, truncate(row.name, 100));
    insert.setString(3, truncate(row.description, 400));
    insert.setString(4, truncate(row.propFileName, 100));
    insert.setString(5, truncate(row.className, 100));
    insert.setString(6, truncate(row.authenticationServer, 100));
    String valueTimeStamp = truncate(row.theTimeStamp, 100);
    if(valueTimeStamp == null || valueTimeStamp.length() == 0) {
      valueTimeStamp = "0";
    }
    insert.setString(7, valueTimeStamp);
    insert.setString(8, truncate(row.silverpeasServerURL, 400));
  }

  /**
   * Updates a domain row.
   * @param domain
   * @throws AdminPersistenceException
   */
  public void updateDomain(DomainRow domain) throws AdminPersistenceException {
    updateRow(UPDATE_DOMAIN, domain);
  }

  static final private String UPDATE_DOMAIN = "update ST_Domain set"
      + " name = ?," + " description = ?," + " propFileName = ?,"
      + " className = ?," + " authenticationServer = ?," + " theTimeStamp = ?,"
      + " silverpeasServerURL = ?" + " where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, DomainRow row) throws
      SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 400));
    update.setString(3, truncate(row.propFileName, 100));
    update.setString(4, truncate(row.className, 100));
    update.setString(5, truncate(row.authenticationServer, 100));
    String valueTimeStamp = truncate(row.theTimeStamp, 100);
    if(valueTimeStamp == null || valueTimeStamp.length() == 0) {
      valueTimeStamp = "0";
    }
    update.setString(6, valueTimeStamp);
    update.setString(7, truncate(row.silverpeasServerURL, 400));
    update.setInt(8, row.id);
  }

  /**
   * Delete the domain
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeDomain(int id) throws AdminPersistenceException {
    DomainRow domain = getDomain(id);
    if (domain == null) {
      return;
    }
    updateRelation(DELETE_DOMAIN, id);
  }

  static final private String DELETE_DOMAIN = "delete from ST_Domain where id = ?";

  /**
   * Fetch the current domain row from a resultSet.
   */
  @Override
  protected DomainRow fetchRow(ResultSet rs) throws SQLException {
    return fetchDomain(rs);
  }
}
