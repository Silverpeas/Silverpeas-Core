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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceProvider;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnDeleting;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * A SpaceTable object manages the ST_SPACE table.
 */
@Repository
public class SpaceTable extends Table<SpaceRow> {

  SpaceTable() {
    super("ST_Space");
  }

  private static final String SPACE_COLUMNS = "id,domainFatherId,name,description,createdBy," +
      "firstPageType,firstPageExtraParam,orderNum,createTime,updateTime,removeTime,spaceStatus," +
      "updatedBy,removedBy,lang,isInheritanceBlocked,look,displaySpaceFirst,isPersonal,isCommunity";

  /**
   * Fetch the current space row from a resultSet.
   * @param rs the SQL result set.
   * @return the data of a space in a {@link SpaceRow} instance.
   * @throws SQLException if an error occurs while fetching the space data.
   */
  protected SpaceRow fetchSpace(ResultSet rs) throws SQLException {
    SpaceRow s = new SpaceRow();

    s.id = rs.getInt(1);
    s.domainFatherId = rs.getInt(2);
    s.name = rs.getString(3);
    s.description = rs.getString(4);
    s.createdBy = rs.getInt(5);
    if (rs.wasNull()) {
      s.createdBy = -1;
    }

    s.firstPageType = rs.getInt(6);
    s.firstPageExtraParam = rs.getString(7);

    s.orderNum = rs.getInt(8);
    if (rs.wasNull()) {
      s.orderNum = -1;
    }

    s.createTime = rs.getString(9);
    s.updateTime = rs.getString(10);
    s.removeTime = rs.getString(11);
    s.status = rs.getString(12);

    s.updatedBy = rs.getInt(13);
    if (rs.wasNull()) {
      s.updatedBy = -1;
    }

    s.removedBy = rs.getInt(14);
    if (rs.wasNull()) {
      s.removedBy = -1;
    }

    s.lang = rs.getString(15);

    s.inheritanceBlocked = rs.getInt(16);

    s.look = rs.getString(17);

    s.displaySpaceFirst = rs.getInt(18);
    if (rs.wasNull()) {
      s.displaySpaceFirst = 1;
    }

    s.isPersonalSpace = rs.getInt(19);
    if (rs.wasNull()) {
      s.isPersonalSpace = 0;
    }

    s.isCommunitySpace = rs.getInt(20);
    if (rs.wasNull()) {
      s.isCommunitySpace = 0;
    }

    return s;
  }

  public SpaceRow getSpace(int id) throws SQLException {
    return getUniqueRow(SELECT_SPACE_BY_ID, id);
  }

  public static final String SELECT = "select ";
  private static final String SELECT_SPACE_BY_ID =
      SELECT + SPACE_COLUMNS + " from ST_Space where id = ?";

  public SpaceRow getPersonalSpace(String userId) throws SQLException {
    List<Object> params = new ArrayList<>(2);
    params.add(1);
    params.add(Integer.valueOf(userId));
    List<SpaceRow> rows = getRows(SELECT_PERSONALSPACE, params);
    if (rows != null && !rows.isEmpty()) {
      return rows.get(0);
    }
    return null;
  }

  private static final String SELECT_PERSONALSPACE =
      SELECT + SPACE_COLUMNS + " from ST_Space where isPersonal = ? and createdBy = ? ";

  public SpaceRow[] getAllSpaces() throws SQLException {
    List<SpaceRow> rows = getRows(SELECT_ALL_SPACES);
    return rows.toArray(new SpaceRow[0]);
  }

  private static final String SELECT_ALL_SPACES =
      SELECT + SPACE_COLUMNS + " from ST_Space" + " order by orderNum";

  public String[] getAllSpaceIds() throws SQLException {
    List<String> ids = getIds(SELECT_ALL_SPACE_IDS);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_SPACE_IDS =
      "select id from ST_Space" + " order by orderNum";

  public String[] getAllRootSpaceIds() throws SQLException {
    List<String> ids = getIds(SELECT_ALL_ROOT_SPACE_IDS);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_ALL_ROOT_SPACE_IDS = "SELECT id FROM st_space WHERE " +
      "domainFatherId IS NULL AND spaceStatus IS NULL AND isPersonal IS NULL ORDER BY orderNum";

  /**
   * Returns all spaces which has been removed but not definitely deleted
   * @return an array with the data of all removed but not deleted spaces.
   * @throws SQLException if an error occurs while fetching the space data
   */
  public SpaceRow[] getRemovedSpaces() throws SQLException {
    List<SpaceRow> rows = getRows(SELECT_REMOVED_SPACES);
    return rows.toArray(new SpaceRow[0]);
  }

  private static final String SELECT_REMOVED_SPACES =
      SELECT + SPACE_COLUMNS + " from ST_Space" + " where spaceStatus = '" +
          SpaceInst.STATUS_REMOVED + "'" + " order by removeTime desc";

  /**
   * Returns all the space ids having a given parent space.
   * @param superSpaceId the unique identifier of a parent space.
   * @return all the space ids having the specified parent space.
   * @throws SQLException if an error occurs while requesting the subspaces identifiers.
   */
  public String[] getDirectSubSpaceIds(int superSpaceId) throws SQLException {
    List<String> ids = getIds(SELECT_SUBSPACE_IDS, superSpaceId);
    return ids.toArray(new String[0]);
  }

  private static final String SELECT_SUBSPACE_IDS =
      "select id from ST_Space where domainFatherId = ? " +
          "and spaceStatus is null order by orderNum";

  /**
   * Returns yjr direct sub spaces of the given space.
   * @param superSpaceId the unique identifier of a space.
   * @return all direct sub spaces of given space.
   * @throws SQLException if an error occurs while fetching the subspaces.
   */
  public List<SpaceRow> getDirectSubSpaces(int superSpaceId) throws SQLException {
    return getRows(SELECT_SUBSPACES, superSpaceId);
  }

  private static final String SELECT_SUBSPACES =
      SELECT + SPACE_COLUMNS + " from ST_Space where domainFatherId = ? " +
          "and spaceStatus is null order by orderNum";

  public void createSpace(SpaceRow space) throws SQLException {
    SpaceRow superSpace;

    if (space.domainFatherId != -1) {
      superSpace = getSpace(space.domainFatherId);
      if (superSpace == null) {
        throw new SQLException(
            unknown("parent space", String.valueOf(space.domainFatherId)));
      }
    }
    insertRow(INSERT_SPACE, space);
  }

  private static final String INSERT_SPACE = "insert into" + " ST_Space(" + SPACE_COLUMNS + ")" +
      " values  (? ,? ,? ,? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, SpaceRow row)
      throws SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }
    // space id
    insert.setInt(1, row.id);
    // space domain father id
    if (row.domainFatherId == -1) {
      insert.setNull(2, Types.INTEGER);
    } else {
      insert.setInt(2, row.domainFatherId);
    }
    // space name
    insert.setString(3, truncate(row.name, 100));
    // space description
    insert.setString(4, truncate(row.description, 500));
    // space creator
    if (row.createdBy == -1) {
      insert.setNull(5, Types.INTEGER);
    } else {
      insert.setInt(5, row.createdBy);
    }
    // First page parameters
    insert.setInt(6, row.firstPageType);
    insert.setString(7, row.firstPageExtraParam);

    insert.setInt(8, row.orderNum);
    insert.setString(9, String.valueOf(new Date().getTime()));
    insert.setString(10, null);
    insert.setString(11, null);
    insert.setString(12, row.status);

    insert.setNull(13, Types.INTEGER);
    insert.setNull(14, Types.INTEGER);

    insert.setString(15, row.lang);

    insert.setInt(16, row.inheritanceBlocked);

    insert.setString(17, row.look);

    insert.setInt(18, row.displaySpaceFirst);

    if (row.isPersonalSpace == 1) {
      insert.setInt(19, row.isPersonalSpace);
    } else {
      insert.setNull(19, Types.SMALLINT);
    }

    insert.setInt(20, row.isCommunitySpace);
  }

  public void updateSpaceOrder(int spaceId, int orderNum) throws SQLException {
    int[] values = new int[]{orderNum, spaceId};
    updateRelation(UPDATE_SPACE_ORDER, values);
  }

  private static final String UPDATE_SPACE_ORDER =
      "update ST_Space set" + " orderNum = ? where id = ?";

  public void updateSpace(SpaceRow space) throws SQLException {
    updateRow(UPDATE_SPACE, space);
  }

  private static final String UPDATE_SPACE =
      "update ST_Space set" + " domainFatherId = ?," + " name = ?," + " description = ?," +
          " createdBy = ?," + " firstPageType = ?," + " firstPageExtraParam = ?," +
          " orderNum = ?, updateTime = ?," + " updatedBy = ?," + " spaceStatus = ?, lang = ?," +
          " isInheritanceBlocked = ?," + " look = ?," + " displaySpaceFirst = ?," +
          " isPersonal = ?, isCommunity = ? " + " where id = ?";

  @Override
  protected void  prepareUpdate(String updateQuery, PreparedStatement update, SpaceRow row)
      throws SQLException {

    if (row.domainFatherId == 0) {
      update.setNull(1, Types.INTEGER);
    } else {
      update.setInt(1, row.domainFatherId);
    }
    update.setString(2, truncate(row.name, 100));
    update.setString(3, truncate(row.description, 500));
    if (row.createdBy == -1) {
      update.setNull(4, Types.INTEGER);
    } else {
      update.setInt(4, row.createdBy);
    }
    update.setInt(5, row.firstPageType);
    update.setString(6, row.firstPageExtraParam);
    update.setInt(7, row.orderNum);
    update.setString(8, String.valueOf(new Date().getTime()));
    if (row.updatedBy == -1) {
      update.setNull(9, Types.INTEGER);
    } else {
      update.setInt(9, row.updatedBy);
    }
    update.setString(10, row.status);
    update.setString(11, row.lang);
    update.setInt(12, row.inheritanceBlocked);
    update.setString(13, row.look);

    update.setInt(14, row.displaySpaceFirst);

    if (row.isPersonalSpace == 1) {
      update.setInt(15, row.isPersonalSpace);
    } else {
      update.setNull(15, Types.SMALLINT);
    }

    update.setInt(16, row.isCommunitySpace);

    update.setInt(17, row.id);
  }

  public void moveSpace(int spaceId, int fatherId) throws SQLException {
    int[] params = new int[2];
    params[0] = fatherId;
    params[1] = spaceId;
    updateRelation(MOVE_SPACE, params);
  }

  private static final String MOVE_SPACE = "update ST_SPACE set domainFatherId = ? where id = ?";

  public void removeSpace(int id) throws SQLException {
    SpaceRow space = getSpace(id);
    if (space == null) {
      return;
    }

    ComponentInstanceTable instanceTable = OrganizationSchema.get().instance();
    ComponentInstanceRow[] instances = instanceTable.getAllComponentInstancesInSpace(id);
    for (ComponentInstanceRow instance : instances) {
      instanceTable.removeComponentInstance(instance.id);
    }
    // Remove user favorite space
    UserFavoriteSpaceService ufsDAO = UserFavoriteSpaceServiceProvider.getUserFavoriteSpaceService();
    if (!ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(-1, id))) {
      throw new SQLException(failureOnDeleting("space", String.valueOf(id)));
    }
    updateRelation(DELETE_SPACE, id);
  }

  private static final String DELETE_SPACE = "delete from ST_Space where id = ?";

  /**
   * Removes the specified space and all its component instances. The space and its component
   * instances are placed into the basket.
   * @param id the unique identifier of a space
   * @param newName the new name of the space once in the basket
   * @param userId the unique identifier of the user performing the task.
   * @throws SQLException if an SQL error occurs.
   */
  public void sendSpaceToBasket(int id, String newName, String userId)
      throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(SEND_SPACE_IN_BASKET)) {
      statement.setString(1, newName);
      statement.setInt(2, Integer.parseInt(userId));
      statement.setString(3, Long.toString(new Date().getTime()));
      statement.setString(4, SpaceInst.STATUS_REMOVED);
      statement.setInt(5, id);
      statement.executeUpdate();
    }
  }

  private static final String SEND_SPACE_IN_BASKET =
      "update ST_Space set name = ?, removedBy = ?, removeTime = ?, spaceStatus = ? where id = ?";

  /**
   * Check if a named space already exists in the given space in the basket.
   * @param fatherId the unique identifier of a parent space in the basket.
   * @param name the name of a subspace.
   * @throws SQLException if an SQL error occurs.
   */
  public boolean isSpaceIntoBasket(int fatherId, String name) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(IS_SPACE_INTO_BASKET)) {
      statement.setString(1, name);
      statement.setInt(2, fatherId);
      statement.setString(3, SpaceInst.STATUS_REMOVED);
      try (ResultSet rs = statement.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static final String IS_SPACE_INTO_BASKET =
      "select * from ST_Space where name = ? and domainFatherId = ? and spaceStatus = ? ";

  /**
   * Remove the space from the basket.
   * @param id the unique identifier of the space.
   * @throws SQLException if an SQL error occurs.
   */
  @SuppressWarnings("DuplicatedCode")
  public void removeSpaceFromBasket(int id) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(REMOVE_SPACE_FROM_BASKET)) {
      statement.setNull(1, Types.INTEGER);
      statement.setNull(2, Types.VARCHAR);
      statement.setNull(3, Types.VARCHAR);
      statement.setInt(4, id);
      statement.executeUpdate();
    }
  }

  private static final String REMOVE_SPACE_FROM_BASKET =
      "update ST_Space set removedBy = ?, removeTime = ?, spaceStatus = ? where id = ?";

  /**
   * Fetch the current space row from a resultSet.
   */
  @Override
  protected SpaceRow fetchRow(ResultSet rs) throws SQLException {
    return fetchSpace(rs);
  }
}
