package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A SpaceTable object manages the ST_SPACE table.
 */
public class SpaceTable extends Table {
  public SpaceTable(OrganizationSchema organization) {
    super(organization, "ST_Space");
    this.organization = organization;
  }

  static final private String SPACE_COLUMNS = "id,domainFatherId,name,description,createdBy,firstPageType,firstPageExtraParam,orderNum,createTime,updateTime,removeTime,spaceStatus,updatedBy,removedBy,lang,isInheritanceBlocked,look";

  /**
   * Fetch the current space row from a resultSet.
   */
  protected SpaceRow fetchSpace(ResultSet rs) throws SQLException {
    SpaceRow s = new SpaceRow();

    s.id = rs.getInt(1);
    s.domainFatherId = rs.getInt(2);
    s.name = rs.getString(3);
    s.description = rs.getString(4);
    s.createdBy = rs.getInt(5);
    if (rs.wasNull())
      s.createdBy = -1;

    s.firstPageType = rs.getInt(6);
    s.firstPageExtraParam = rs.getString(7);

    s.orderNum = rs.getInt(8);
    if (rs.wasNull())
      s.orderNum = -1;

    s.createTime = rs.getString(9);
    s.updateTime = rs.getString(10);
    s.removeTime = rs.getString(11);
    s.status = rs.getString(12);

    s.updatedBy = rs.getInt(13);
    if (rs.wasNull())
      s.updatedBy = -1;

    s.removedBy = rs.getInt(14);
    if (rs.wasNull())
      s.removedBy = -1;

    s.lang = rs.getString(15);

    s.inheritanceBlocked = rs.getInt(16);

    s.look = rs.getString(17);

    return s;
  }

  /**
   * Returns the Space whith the given id.
   */
  public SpaceRow getSpace(int id) throws AdminPersistenceException {
    return (SpaceRow) getUniqueRow(SELECT_SPACE_BY_ID, id);
  }

  static final private String SELECT_SPACE_BY_ID = "select " + SPACE_COLUMNS
      + " from ST_Space where id = ?";

  /**
   * Tests if a space with given space id exists
   * 
   * @return true if the given space instance name is an existing space
   */
  public boolean isSpaceInstExist(int id) throws AdminPersistenceException {
    SpaceRow space = this.getSpace(id);
    if (space == null)
      return false;
    else
      return true;
  }

  /**
   * Returns all the Spaces.
   */
  public SpaceRow[] getAllSpaces() throws AdminPersistenceException {
    return (SpaceRow[]) getRows(SELECT_ALL_SPACES).toArray(new SpaceRow[0]);
  }

  static final private String SELECT_ALL_SPACES = "select " + SPACE_COLUMNS
      + " from ST_Space" + " order by orderNum";

  /**
   * Returns all the Space ids.
   */
  public String[] getAllSpaceIds() throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_SPACE_IDS).toArray(new String[0]);
  }

  static final private String SELECT_ALL_SPACE_IDS = "select id from ST_Space"
      + " order by orderNum";

  /**
   * Returns all the root Space ids.
   */
  public String[] getAllRootSpaceIds() throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_ROOT_SPACE_IDS).toArray(new String[0]);
  }

  static final private String SELECT_ALL_ROOT_SPACE_IDS = "select id from ST_Space"
      + " where domainFatherId is null"
      + " AND spaceStatus is null"
      + " order by orderNum";

  /**
   * Returns all the Root Spaces.
   */
  public SpaceRow[] getAllRootSpaces() throws AdminPersistenceException {
    return (SpaceRow[]) getRows(SELECT_ALL_ROOT_SPACES)
        .toArray(new SpaceRow[0]);
  }

  static final private String SELECT_ALL_ROOT_SPACES = "select "
      + SPACE_COLUMNS + " from ST_Space" + " where domainFatherId is null"
      + " AND spaceStatus is null" + " order by orderNum";

  /**
   * Returns all spaces which has been removed but not definitely deleted
   */
  public SpaceRow[] getRemovedSpaces() throws AdminPersistenceException {
    return (SpaceRow[]) getRows(SELECT_REMOVED_SPACES).toArray(new SpaceRow[0]);
  }

  static final private String SELECT_REMOVED_SPACES = "select " + SPACE_COLUMNS
      + " from ST_Space" + " where spaceStatus = '" + SpaceInst.STATUS_REMOVED
      + "'" + " order by removeTime desc";

  /**
   * Returns the Space of a given component instance.
   */
  public SpaceRow getSpaceOfInstance(int instanceId)
      throws AdminPersistenceException {
    return (SpaceRow) getUniqueRow(SELECT_INSTANCE_SPACE, instanceId);
  }

  static final private String SELECT_INSTANCE_SPACE = "select "
      + aliasColumns("s", SPACE_COLUMNS)
      + " from ST_Space s, ST_ComponentInstance i" + " where s.id = i.spaceId"
      + " and   i.id = ?";

  /**
   * Returns all the Root Space allowed to a given user.
   */
  public SpaceRow[] getAllRootSpacesOfUser(int userId)
      throws AdminPersistenceException {
    return (SpaceRow[]) getRows(SELECT_ALL_USER_ROOT_SPACES, userId).toArray(
        new SpaceRow[0]);
  }

  static final private String SELECT_ALL_USER_ROOT_SPACES = "select "
      + aliasColumns("ST_Space", SPACE_COLUMNS)
      + " from ST_Space,ST_UserSet_User_Rel"
      + " where ST_Space.id=userSetId and userSetType='S' and userId=?"
      + " and ST_Space.domainFatherId is null"
      + " and ST_Space.spaceStatus is null" + " union" + " SELECT "
      + aliasColumns("S", SPACE_COLUMNS)
      + " FROM   ST_UserSet_UserSet_Rel, ST_ComponentInstance C, ST_Space S"
      + " WHERE subSetType = 'I' AND subSetId = C.id AND S.id = superSetId"
      + " AND S.domainFatherId is null" + " AND S.spaceStatus is null"
      + " AND C.isPublic = 1" + " order by orderNum";

  /**
   * Returns all the Space allowed to a given user.
   */
  public SpaceRow[] getAllSpacesOfUser(int userId)
      throws AdminPersistenceException {
    return (SpaceRow[]) getRows(SELECT_ALL_USER_SPACES, userId).toArray(
        new SpaceRow[0]);
  }

  /*
   * static final private String SELECT_ALL_USER_SPACES =
   * "select "+SPACE_COLUMNS + " from ST_Space,ST_UserSet_User_Rel" +
   * " where id=userSetId and userSetType='S' and userId=?" +
   * " order by ST_Space.orderNum";
   */

  static final private String SELECT_ALL_USER_SPACES = "select "
      + aliasColumns("ST_Space", SPACE_COLUMNS)
      + " from ST_UserSet_User_Rel, ST_Space"
      + " where userSetType='S' and ST_Space.id = userSetId"
      + " and ST_Space.spaceStatus is null" + " and userId = ?" + " union"
      + " SELECT " + aliasColumns("S", SPACE_COLUMNS)
      + " FROM   ST_UserSet_UserSet_Rel, ST_ComponentInstance C, ST_Space S"
      + " WHERE subSetType = 'I' AND subSetId = C.id AND S.id = superSetId"
      + " AND C.isPublic = 1" + " ORDER BY orderNum";

  /**
   * Returns all spaces allowed to a given user for a given space.
   */
  public SpaceRow[] getSubSpacesOfUser(int userId, int spaceId)
      throws AdminPersistenceException {
    int[] ids = new int[4];
    ids[0] = userId;
    ids[1] = spaceId;
    ids[2] = spaceId;
    ids[3] = spaceId;
    return (SpaceRow[]) getRows(SELECT_USER_SUBSPACES, ids).toArray(
        new SpaceRow[0]);
  }

  static final private String SELECT_USER_SUBSPACES = "select "
      + aliasColumns("ST_Space", SPACE_COLUMNS)
      + " from ST_Space, ST_UserSet_User_Rel, ST_UserSet_UserSet_Rel"
      + " where ST_Space.id=userSetId and userSetType='S' and userId=?"
      + " and superSetId=? and superSetType='S' and subSetType='S' and subSetId=id"
      + " and ST_Space.domainFatherId = ?"
      + " and ST_Space.spaceStatus is null" + " union" + " SELECT "
      + aliasColumns("S", SPACE_COLUMNS)
      + " FROM ST_UserSet_UserSet_Rel, ST_ComponentInstance C, ST_Space S"
      + " WHERE subSetType = 'I' AND subSetId = C.id AND S.id = superSetId"
      + " AND S.domainFatherId = ?" + " AND S.spaceStatus is null"
      + " AND C.isPublic = 1" + " ORDER BY orderNum";

  /**
   * Returns all the space ids having a given superSpace.
   */
  public String[] getDirectSubSpaceIds(int superSpaceId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_SUBSPACE_IDS, superSpaceId).toArray(
        new String[0]);
  }

  static final private String SELECT_SUBSPACE_IDS = "select id from ST_Space"
      + " where domainFatherId = ?" + " and spaceStatus is null"
      + " order by orderNum";

  /**
   * Returns all the father space ids of given space
   */
  public String[] getAllFatherSpaceIds(int spaceId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_FATHER_SPACE_IDS, spaceId).toArray(
        new String[0]);
  }

  static final private String SELECT_ALL_FATHER_SPACE_IDS = "select superSetId from ST_UserSet_UserSet_Rel"
      + " where superSetType='S' and subSetType='S'" + " and subSetId = ?";

  /**
   * Inserts in the database a new space row.
   */
  public void createSpace(SpaceRow space) throws AdminPersistenceException {
    SpaceRow superSpace = null;

    if (space.domainFatherId != -1) {
      superSpace = getSpace(space.domainFatherId);
      if (superSpace == null) {
        throw new AdminPersistenceException("SpaceTable.createSpace",
            SilverpeasException.ERROR, "admin.EX_ERR_SPACE_NOT_FOUND",
            "father space id : '" + space.domainFatherId + "'");
      }
    }

    insertRow(INSERT_SPACE, space);
    organization.userSet.createUserSet("S", space.id);

    if (superSpace != null) {
      organization.userSet.addUserSetInUserSet("S", space.id, "S",
          superSpace.id);
    }

    CallBackManager.invoke(CallBackManager.ACTION_AFTER_CREATE_SPACE, space.id,
        null, null);
  }

  static final private String INSERT_SPACE = "insert into" + " ST_Space("
      + SPACE_COLUMNS + ")"
      + " values  (? ,? ,? ,? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    SpaceRow s = (SpaceRow) row;
    if (s.id == -1) {
      s.id = getNextId();
    }

    // space id
    insert.setInt(1, s.id);

    // space domain father id
    if (s.domainFatherId == -1)
      insert.setNull(2, Types.INTEGER);
    else
      insert.setInt(2, s.domainFatherId);

    // space name
    insert.setString(3, truncate(s.name, 100));

    // space description
    insert.setString(4, truncate(s.description, 500));

    // space creator
    if (s.createdBy == -1)
      insert.setNull(5, Types.INTEGER);
    else
      insert.setInt(5, s.createdBy);

    // First page parameters
    insert.setInt(6, s.firstPageType);
    insert.setString(7, s.firstPageExtraParam);

    insert.setInt(8, s.orderNum);
    insert.setString(9, String.valueOf(new Date().getTime()));
    insert.setString(10, null);
    insert.setString(11, null);
    insert.setString(12, s.status);

    insert.setNull(13, Types.INTEGER);
    insert.setNull(14, Types.INTEGER);

    insert.setString(15, s.lang);

    insert.setInt(16, s.inheritanceBlocked);

    insert.setString(17, s.look);
  }

  public void updateSpaceOrder(int spaceId, int orderNum)
      throws AdminPersistenceException {
    int[] values = new int[] { orderNum, spaceId };
    updateRelation(UPDATE_SPACE_ORDER, values);
  }

  public void updateSpaceInheritance(int spaceId, boolean inheritanceBlocked)
      throws AdminPersistenceException {
    int iInheritance = 0;
    if (inheritanceBlocked)
      iInheritance = 1;
    int[] values = new int[] { iInheritance, spaceId };
    updateRelation(UPDATE_SPACE_INHERITANCE, values);
  }

  static final private String UPDATE_SPACE_INHERITANCE = "update ST_Space set"
      + " isInheritanceBlocked = ?" + " where id = ?";

  static final private String UPDATE_SPACE_ORDER = "update ST_Space set"
      + " orderNum = ?" + " where id = ?";

  /**
   * Updates a space row.
   */
  public void updateSpace(SpaceRow space) throws AdminPersistenceException {
    updateRow(UPDATE_SPACE, space);
  }

  static final private String UPDATE_SPACE = "update ST_Space set"
      + " domainFatherId = ?," + " name = ?," + " description = ?,"
      + " createdBy = ?," + " firstPageType = ?," + " firstPageExtraParam = ?,"
      + " orderNum = ?,"
      // + " createTime = ?,"
      + " updateTime = ?," + " updatedBy = ?,"
      // + " removeTime = ?,"
      + " spaceStatus = ?," + " lang = ?," + " isInheritanceBlocked = ?,"
      + " look = ? " + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    SpaceRow s = (SpaceRow) row;

    if (s.domainFatherId == 0)
      update.setNull(1, Types.INTEGER);
    else
      update.setInt(1, s.domainFatherId);
    update.setString(2, truncate(s.name, 100));
    update.setString(3, truncate(s.description, 500));
    if (s.createdBy == -1)
      update.setNull(4, Types.INTEGER);
    else
      update.setInt(4, s.createdBy);
    update.setInt(5, s.firstPageType);
    update.setString(6, s.firstPageExtraParam);
    update.setInt(7, s.orderNum);
    update.setString(8, String.valueOf(new Date().getTime()));
    if (s.updatedBy == -1)
      update.setNull(9, Types.INTEGER);
    else
      update.setInt(9, s.updatedBy);
    update.setString(10, s.status);
    update.setString(11, s.lang);
    update.setInt(12, s.inheritanceBlocked);
    update.setString(13, s.look);

    update.setInt(14, s.id);
    // First page parameters
  }

  /**
   * Delete the space and all his component instances.
   */
  public void removeSpace(int id) throws AdminPersistenceException {
    CallBackManager.invoke(CallBackManager.ACTION_BEFORE_REMOVE_SPACE, id,
        null, null);

    SpaceRow space = getSpace(id);
    if (space == null)
      return;

    ComponentInstanceRow[] instances = organization.instance
        .getAllComponentInstancesInSpace(id);
    for (int i = 0; i < instances.length; i++) {
      organization.instance.removeComponentInstance(instances[i].id);
    }

    organization.userSet.removeUserSet("S", id);
    updateRelation(DELETE_SPACE, id);
  }

  static final private String DELETE_SPACE = "delete from ST_Space where id = ?";

  /**
   * Delete the space and all his component instances.
   */
  public void sendSpaceToBasket(int id, String newName, String userId)
      throws AdminPersistenceException {
    // CallBackManager.invoke(CallBackManager.ACTION_BEFORE_REMOVE_SPACE, id,
    // null, null);

    PreparedStatement statement = null;

    try {
      statement = organization.getStatement(SEND_SPACE_IN_BASKET);
      statement.setString(1, newName);
      statement.setInt(2, Integer.parseInt(userId));
      statement.setString(3, Long.toString(new Date().getTime()));
      statement.setString(4, SpaceInst.STATUS_REMOVED);
      statement.setInt(5, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminPersistenceException("SpaceTable.sendSpaceToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      organization.releaseStatement(statement);
    }
  }

  static final private String SEND_SPACE_IN_BASKET = "update ST_Space set name = ?, removedBy = ?, removeTime = ?, spaceStatus = ? where id = ?";

  /**
   * Remove the space from the basket Space will be available again
   */
  public void removeSpaceFromBasket(int id) throws AdminPersistenceException {
    PreparedStatement statement = null;

    try {
      statement = organization.getStatement(REMOVE_SPACE_FROM_BASKET);
      statement.setNull(1, Types.INTEGER);
      statement.setNull(2, Types.VARCHAR);
      statement.setNull(3, Types.VARCHAR);
      statement.setInt(4, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminPersistenceException("SpaceTable.removeSpaceFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      organization.releaseStatement(statement);
    }
  }

  static final private String REMOVE_SPACE_FROM_BASKET = "update ST_Space set removedBy = ?, removeTime = ?, spaceStatus = ? where id = ?";

  /**
   * Returns the Space of a given space user role.
   */
  public SpaceRow getSpaceOfSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (SpaceRow) getUniqueRow(SELECT_SPACEUSERROLE_SPACE, spaceUserRoleId);
  }

  static final private String SELECT_SPACEUSERROLE_SPACE = "select "
      + aliasColumns("i", SPACE_COLUMNS)
      + " from ST_Space i, ST_SpaceUserRole us" + " where i.id = us.spaceId"
      + " and   us.id = ?";

  /**
   * Fetch the current space row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchSpace(rs);
  }

  private OrganizationSchema organization = null;
}
