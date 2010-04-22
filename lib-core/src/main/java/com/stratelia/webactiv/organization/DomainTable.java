/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A DomainTable object manages the ST_Domain table.
 */
public class DomainTable extends Table {
  public DomainTable(OrganizationSchema schema) {
    super(schema, "ST_Domain");
  }

  static final private String DOMAIN_COLUMNS =
      "id,name,description,propFileName,className,authenticationServer,theTimeStamp,silverpeasServerURL";

  /**
   * Fetch the current domain row from a resultSet.
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
   */
  public DomainRow getDomain(int id) throws AdminPersistenceException {
    return (DomainRow) getUniqueRow(SELECT_DOMAIN_BY_ID, id);
  }

  static final private String SELECT_DOMAIN_BY_ID = "select " + DOMAIN_COLUMNS
      + " from ST_Domain where id = ?";

  /**
   * Returns all the Domains.
   */
  public DomainRow[] getAllDomains() throws AdminPersistenceException {
    return (DomainRow[]) getRows(SELECT_ALL_DOMAINS).toArray(new DomainRow[0]);
  }

  static final private String SELECT_ALL_DOMAINS = "select " + DOMAIN_COLUMNS
      + " from ST_Domain where not id=-1 order by name asc";

  /**
   * Insert a new domain row.
   */
  public void createDomain(DomainRow domain) throws AdminPersistenceException {
    insertRow(INSERT_DOMAIN, domain);
  }

  static final private String INSERT_DOMAIN =
      "insert into"
          + " ST_Domain(id,name,description,propFileName,className,authenticationServer,theTimeStamp,silverpeasServerURL)"
          + " values  (? ,? ,?, ? ,?, ?, ?, ?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    DomainRow d = (DomainRow) row;
    if (d.id == -1) {
      d.id = getNextId();
    }

    insert.setInt(1, d.id);
    insert.setString(2, truncate(d.name, 100));
    insert.setString(3, truncate(d.description, 400));
    insert.setString(4, truncate(d.propFileName, 100));
    insert.setString(5, truncate(d.className, 100));
    insert.setString(6, truncate(d.authenticationServer, 100));
    insert.setString(7, truncate(d.theTimeStamp, 100));
    insert.setString(8, truncate(d.silverpeasServerURL, 400));
  }

  /**
   * Updates a domain row.
   */
  public void updateDomain(DomainRow domain) throws AdminPersistenceException {
    updateRow(UPDATE_DOMAIN, domain);
  }

  static final private String UPDATE_DOMAIN = "update ST_Domain set"
      + " name = ?," + " description = ?," + " propFileName = ?,"
      + " className = ?," + " authenticationServer = ?," + " theTimeStamp = ?,"
      + " silverpeasServerURL = ?" + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    DomainRow d = (DomainRow) row;

    update.setString(1, truncate(d.name, 100));
    update.setString(2, truncate(d.description, 400));
    update.setString(3, truncate(d.propFileName, 100));
    update.setString(4, truncate(d.className, 100));
    update.setString(5, truncate(d.authenticationServer, 100));
    update.setString(6, truncate(d.theTimeStamp, 100));
    update.setString(7, truncate(d.silverpeasServerURL, 400));
    update.setInt(8, d.id);
  }

  /**
   * Delete the domain
   */
  public void removeDomain(int id) throws AdminPersistenceException {
    DomainRow domain = getDomain(id);
    if (domain == null)
      return;

    // remove the empty group.
    updateRelation(DELETE_DOMAIN, id);
  }

  static final private String DELETE_DOMAIN = "delete from ST_Domain where id = ?";

  /**
   * Fetch the current domain row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchDomain(rs);
  }
}
