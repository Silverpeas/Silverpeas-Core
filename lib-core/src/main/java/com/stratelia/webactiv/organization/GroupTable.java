package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;

import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SynchroGroupReport;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A GroupTable object manages the ST_Group table.
 */
public class GroupTable extends Table {
  public GroupTable(OrganizationSchema schema) {
    super(schema, "ST_Group");
    this.organization = schema;
  }

  static final private String GROUP_COLUMNS = "id,specificId,domainId,superGroupId,name,description,synchroRule";

  /**
   * Fetch the current group row from a resultSet.
   */
  protected GroupRow fetchGroup(ResultSet rs) throws SQLException {
    GroupRow g = new GroupRow();

    g.id = rs.getInt(1);
    g.specificId = rs.getString(2);
    if (g.specificId.equals("-1"))
      g.specificId = null;

    g.domainId = rs.getInt(3);

    g.superGroupId = rs.getInt(4);
    if (rs.wasNull())
      g.superGroupId = -1;

    g.name = rs.getString(5);
    g.description = rs.getString(6);

    g.rule = rs.getString(7);

    return g;
  }

  /**
   * Returns the Group whith the given id.
   */
  public GroupRow getGroup(int id) throws AdminPersistenceException {
    return (GroupRow) getUniqueRow(SELECT_GROUP_BY_ID, id);
  }

  static final private String SELECT_GROUP_BY_ID = "select " + GROUP_COLUMNS
      + " from ST_Group where id = ?";

  /**
   * Returns all the space ids manageable by the given group.
   */
  public String[] getManageableSpaceIds(int groupId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_MANAGEABLE_SPACE_IDS, groupId).toArray(
        new String[0]);
  }

  static final private String SELECT_ALL_MANAGEABLE_SPACE_IDS = "select distinct spaceId from ST_UserSet_UserSet_Rel, ST_SpaceUserRole"
      + " where superSetType='M' and id=superSetId"
      + " and subSetType='G' and subSetId=?";

  /**
   * Returns Group whith the given specificId and domainId.
   */
  public GroupRow getGroupBySpecificId(int domainId, String specificId)
      throws AdminPersistenceException {
    GroupRow[] groups = (GroupRow[]) getRows(SELECT_GROUP_BY_SPECIFICID,
        new int[] { domainId }, new String[] { specificId }).toArray(
        new GroupRow[0]);

    if (groups.length == 0)
      return null;
    else if (groups.length == 1)
      return groups[0];
    else {
      throw new AdminPersistenceException("GroupTable.getGroupBySpecificId",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_SPECIFIC_ID_FOUND_TWICE", "domain Id: '"
              + domainId + "', specific Id: '" + specificId + "'");
    }
  }

  static final private String SELECT_GROUP_BY_SPECIFICID = "select "
      + GROUP_COLUMNS + " from ST_Group where domainId = ? and specificId = ?";

  /**
   * Returns the root Group whith the given name.
   */
  public GroupRow getRootGroup(String name) throws AdminPersistenceException {
    GroupRow[] groups = (GroupRow[]) getRows(SELECT_ROOT_GROUP_BY_NAME,
        new String[] { name }).toArray(new GroupRow[0]);

    if (groups.length == 0)
      return null;
    else if (groups.length == 1)
      return groups[0];
    else {
      throw new AdminPersistenceException("GroupTable.getRootGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NAME_FOUND_TWICE",
          "group name: '" + name + "'");
    }
  }

  static final private String SELECT_ROOT_GROUP_BY_NAME = "select "
      + GROUP_COLUMNS + " from ST_Group"
      + " where superGroupId is null and name = ?";

  /**
   * Returns the Group whith the given name in the given super group.
   */
  public GroupRow getGroup(int superGroupId, String name)
      throws AdminPersistenceException {
    GroupRow[] groups = (GroupRow[]) getRows(SELECT_GROUP_BY_NAME,
        new int[] { superGroupId }, new String[] { name }).toArray(
        new GroupRow[0]);

    if (groups.length == 0)
      return null;
    else if (groups.length == 1)
      return groups[0];
    else {
      throw new AdminPersistenceException("GroupTable.getGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NAME_ID_FOUND_TWICE",
          "group name: '" + name + "', father group id: '" + superGroupId + "'");
    }
  }

  static final private String SELECT_GROUP_BY_NAME = "select " + GROUP_COLUMNS
      + " from ST_Group" + " where superGroupId = ? and name = ?";

  /**
   * Returns all the Groups.
   */
  public GroupRow[] getAllGroups() throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_GROUPS).toArray(new GroupRow[0]);
  }

  static final private String SELECT_ALL_GROUPS = "select " + GROUP_COLUMNS
      + " from ST_Group";

  /**
   * Returns all the Groups.
   */
  public GroupRow[] getSynchronizedGroups() throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_SYNCHRONIZED_GROUPS).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_SYNCHRONIZED_GROUPS = "select "
      + GROUP_COLUMNS
      + " from ST_Group where synchroRule is not null and synchroRule <> ''";

  /**
   * Returns all the Group ids.
   */
  public String[] getAllGroupIds() throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_GROUP_IDS).toArray(new String[0]);
  }

  static final private String SELECT_ALL_GROUP_IDS = "select id from ST_Group";

  /**
   * Returns all the Groups without a superGroup.
   */
  public GroupRow[] getAllRootGroups() throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_ROOT_GROUPS)
        .toArray(new GroupRow[0]);
  }

  static final private String SELECT_ALL_ROOT_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group where superGroupId is null";

  /**
   * Returns all the Groups without a superGroup.
   */
  public String[] getAllRootGroupIds() throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_ROOT_GROUP_IDS).toArray(new String[0]);
  }

  static final private String SELECT_ALL_ROOT_GROUP_IDS = "select id from ST_Group where superGroupId is null";

  /**
   * Returns all the Groups having a given superGroup.
   */
  public GroupRow[] getDirectSubGroups(int superGroupId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_SUBGROUPS, superGroupId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_SUBGROUPS = "select " + GROUP_COLUMNS
      + " from ST_Group where superGroupId = ?";

  /**
   * Returns all the Group ids having a given superGroup.
   */
  public String[] getDirectSubGroupIds(int superGroupId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_SUBGROUP_IDS, superGroupId).toArray(
        new String[0]);
  }

  static final private String SELECT_SUBGROUP_IDS = "select id from ST_Group where superGroupId = ?";

  /**
   * Returns all the recursives subGroupIds of a given superGroup.
   */
  public String[] getAllSubGroupIds(int superGroupId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_SUBGROUPS, superGroupId).toArray(
        new String[0]);
  }

  static final private String SELECT_ALL_SUBGROUPS = "select id"
      + " from ST_Group, ST_UserSet_UserSet_Rel"
      + " where id=subSetId and subSetType='G'"
      + " and   superSetType='G' and superSetId = ?";

  /**
   * Returns all the Root Groups having a given domain id.
   */
  public GroupRow[] getAllRootGroupsOfDomain(int domainId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_ROOT_GROUPS_IN_DOMAIN, domainId)
        .toArray(new GroupRow[0]);
  }

  /**
   * Returns all the Root Group Ids having a given domain id.
   */
  public String[] getAllRootGroupIdsOfDomain(int domainId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_ALL_ROOT_GROUPS_IDS_IN_DOMAIN, domainId)
        .toArray(new String[0]);
  }

  static final private String SELECT_ALL_ROOT_GROUPS_IN_DOMAIN = "select "
      + GROUP_COLUMNS + " from ST_Group"
      + " where (domainId=?) AND (superGroupId is null)";

  static final private String SELECT_ALL_ROOT_GROUPS_IDS_IN_DOMAIN = "select id from ST_Group where (domainId=?) AND (superGroupId is null)";

  /**
   * Returns all the Groups having a given domain id.
   */
  public GroupRow[] getAllGroupsOfDomain(int domainId)
      throws AdminPersistenceException {
    SynchroReport.debug("GroupTable.getAllGroupsOfDomain()",
        "Recherche de l'ensemble des groupes du domaine LDAP dans la base (ID "
            + domainId + "), requête : " + SELECT_ALL_GROUPS_IN_DOMAIN, null);
    return (GroupRow[]) getRows(SELECT_ALL_GROUPS_IN_DOMAIN, domainId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_ALL_GROUPS_IN_DOMAIN = "select "
      + GROUP_COLUMNS + " from ST_Group" + " where domainId=?";

  /**
   * Returns the superGroup of a given subGroup.
   */
  public GroupRow getSuperGroup(int subGroupId)
      throws AdminPersistenceException {
    return (GroupRow) getUniqueRow(SELECT_SUPERGROUP, subGroupId);
  }

  static final private String SELECT_SUPERGROUP = "select "
      + aliasColumns("sg", GROUP_COLUMNS) + " from ST_Group sg, ST_GROUP g"
      + " where sg.id=g.superGroupId and g.id=?";

  /**
   * Returns all the recursives superGroups of a given subGroup.
   */
  public GroupRow[] getAllSuperGroups(int subGroupId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_SUPERGROUPS, subGroupId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_ALL_SUPERGROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_UserSet_UserSet_Rel"
      + " where id=superSetId and superSetType='G'"
      + " and   subSetType='G' and subSetId = ?";

  /**
   * Returns all the groups of a given user (not recursive).
   */
  public GroupRow[] getDirectGroupsOfUser(int userId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_USER_GROUPS, userId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_USER_GROUPS = "select " + GROUP_COLUMNS
      + " from ST_Group,ST_Group_User_Rel"
      + " where id = groupId and userId = ?";

  /**
   * Returns all the groups of a given user (recursive).
   */
  public GroupRow[] getAllGroupsOfUser(int userId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_USER_GROUPS, userId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_ALL_USER_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_UserSet_User_Rel"
      + " where id=userSetId and userSetType='G' and userId=?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public GroupRow[] getDirectGroupsInUserRole(int userRoleId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_USERROLE_GROUPS, userRoleId).toArray(
        new GroupRow[0]);
  }

  static final private String SELECT_USERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_UserRole_Group_Rel"
      + " where id = groupId and userRoleId = ?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public String[] getDirectGroupIdsInUserRole(int userRoleId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_USERROLE_GROUP_IDS, userRoleId).toArray(
        new String[0]);
  }

  static final private String SELECT_USERROLE_GROUP_IDS = "select id from ST_Group,ST_UserRole_Group_Rel"
      + " where id = groupId and userRoleId = ?";

  /**
   * Returns all the groups in a given userRole (recursive).
   */
  public GroupRow[] getAllGroupsInUserRole(int userRoleId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_USERROLE_GROUPS, userRoleId)
        .toArray(new GroupRow[0]);
  }

  static final private String SELECT_ALL_USERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_UserSet_UserSet_Rel"
      + " where id=subSetId and subSetType = 'G'"
      + " and   superSetType='R' and superSetId=?";

  /**
   * Returns all the groups in a given spaceUserRole (not recursive).
   */
  public GroupRow[] getDirectGroupsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_SPACEUSERROLE_GROUPS, spaceUserRoleId)
        .toArray(new GroupRow[0]);
  }

  static final private String SELECT_SPACEUSERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_SpaceUserRole_Group_Rel"
      + " where id = groupId and spaceUserRoleId = ?";

  /**
   * Returns all the group ids in a given spaceUserRole (not recursive).
   */
  public String[] getDirectGroupIdsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_SPACEUSERROLE_GROUP_IDS, spaceUserRoleId)
        .toArray(new String[0]);
  }

  static final private String SELECT_SPACEUSERROLE_GROUP_IDS = "select id from ST_Group,ST_SpaceUserRole_Group_Rel"
      + " where id = groupId and spaceUserRoleId = ?";

  /**
   * Returns all the groups in a given spaceUserRole (recursive).
   */
  public GroupRow[] getAllGroupsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_ALL_SPACEUSERROLE_GROUPS,
        spaceUserRoleId).toArray(new GroupRow[0]);
  }

  static final private String SELECT_ALL_SPACEUSERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_UserSet_UserSet_Rel"
      + " where id=subSetId and subSetType = 'G'"
      + " and   superSetType='M' and superSetId=?";

  /**
   * Returns all the groups in a given groupUserRole (not recursive).
   */
  public GroupRow[] getDirectGroupsInGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    return (GroupRow[]) getRows(SELECT_GROUPUSERROLE_GROUPS, groupUserRoleId)
        .toArray(new GroupRow[0]);
  }

  static final private String SELECT_GROUPUSERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from ST_Group,ST_GroupUserRole_Group_Rel"
      + " where id = groupId and groupUserRoleId = ?";

  /**
   * Returns the Group of a given group user role.
   */
  public GroupRow getGroupOfGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    return (GroupRow) getUniqueRow(SELECT_GROUPUSERROLE_GROUP, groupUserRoleId);
  }

  static final private String SELECT_GROUPUSERROLE_GROUP = "select "
      + aliasColumns("i", GROUP_COLUMNS)
      + " from ST_Group i, ST_GroupUserRole us" + " where i.id = us.groupId"
      + " and   us.id = ?";

  /**
   * Returns all the group ids in a given groupUserRole (not recursive).
   */
  public String[] getDirectGroupIdsInGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_GROUPUSERROLE_GROUP_IDS, groupUserRoleId)
        .toArray(new String[0]);
  }

  static final private String SELECT_GROUPUSERROLE_GROUP_IDS = "select id from ST_Group,ST_GroupUserRole_Group_Rel"
      + " where id = groupId and groupUserRoleId = ?";

  /**
   * Returns the Group whose fields match those of the given sample group
   * fields.
   */
  public GroupRow[] getAllMatchingGroups(GroupRow sampleGroup)
      throws AdminPersistenceException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleGroup.name, sampleGroup.description };

    return (GroupRow[]) getMatchingRows(GROUP_COLUMNS, columns, values)
        .toArray(new GroupRow[0]);
  }

  /**
   * Returns all the Groups satifying the model that are direct childs of a
   * specific group
   */
  public String[] searchGroupsIds(boolean isRootGroup, int componentId,
      int[] aRoleId, GroupRow groupModel) throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr = ") AND (";
    StringBuffer theQuery;
    Vector ids = new Vector();
    Vector params = new Vector();

    if ((aRoleId != null) && (aRoleId.length > 0)) {
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID_IN_ROLE);
      theQuery
          .append(" WHERE ((ST_Group.id = ST_UserRole_Group_Rel.groupId) AND ");
      if (aRoleId.length > 1) {
        theQuery.append("(");
      }
      for (int i = 0; i < aRoleId.length; i++) {
        ids.add(new Integer(aRoleId[i]));
        if (i > 0) {
          theQuery.append(" OR ");
        }
        theQuery.append("(ST_UserRole_Group_Rel.userRoleId = ?)");
      }
      if (aRoleId.length > 1) {
        theQuery.append(")");
      }
      concatAndOr = true;
    } else if (componentId >= 0) {
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID_IN_COMPONENT);
      ids.add(new Integer(componentId));
      theQuery
          .append(" WHERE ((ST_UserRole.id = ST_UserRole_Group_Rel.userRoleId) AND (ST_Group.id = ST_UserRole_Group_Rel.groupId) AND (ST_UserRole.instanceId = ?)");
      concatAndOr = true;
    } else {
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID);
    }

    if (isRootGroup) {
      if (concatAndOr) {
        theQuery.append(andOr);
      } else {
        theQuery.append(" where (");
        concatAndOr = true;
      }
      theQuery.append("ST_Group.superGroupId IS NULL");
    } else {
      concatAndOr = addIdToQuery(ids, theQuery, groupModel.superGroupId,
          "ST_Group.superGroupId", concatAndOr, andOr);
    }
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.id, "ST_Group.id",
        concatAndOr, andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.domainId,
        "ST_Group.domainId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.name,
        "ST_Group.name", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.description,
        "ST_Group.description", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.specificId,
        "ST_Group.specificId", concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" order by UPPER(ST_Group.name)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ((Integer) ids.get(i)).intValue();
    }

    return (String[]) getIds(theQuery.toString(), idsArray,
        (String[]) params.toArray(new String[0])).toArray(new String[0]);
  }

  static final private String SELECT_SEARCH_GROUPSID = "select DISTINCT ST_Group.id, UPPER(ST_Group.name) from ST_Group";

  static final private String SELECT_SEARCH_GROUPSID_IN_COMPONENT = "select DISTINCT ST_Group.id, UPPER(ST_Group.name) "
      + "from ST_Group,ST_UserRole_Group_Rel,ST_UserRole";

  static final private String SELECT_SEARCH_GROUPSID_IN_ROLE = "select DISTINCT ST_Group.id, UPPER(ST_Group.name) "
      + "from ST_Group,ST_UserRole_Group_Rel";

  /**
   * Returns all the Groups satiffying the model
   */
  public GroupRow[] searchGroups(GroupRow groupModel, boolean isAnd)
      throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr;
    StringBuffer theQuery = new StringBuffer(SELECT_SEARCH_GROUPS);
    Vector ids = new Vector();
    Vector params = new Vector();

    if (isAnd) {
      andOr = ") AND (";
    } else {
      andOr = ") OR (";
    }
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.id, "id", concatAndOr,
        andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.domainId, "domainId",
        concatAndOr, andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.superGroupId,
        "superGroupId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.name, "name",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.description,
        "description", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.specificId,
        "specificId", concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" order by UPPER(name)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ((Integer) ids.get(i)).intValue();
    }

    return (GroupRow[]) getRows(theQuery.toString(), idsArray,
        (String[]) params.toArray(new String[0])).toArray(new GroupRow[0]);
  }

  static final private String SELECT_SEARCH_GROUPS = "select " + GROUP_COLUMNS
      + ", UPPER(name)" + " from ST_Group";

  /**
   * Insert a new group row.
   */
  public void createGroup(GroupRow group) throws AdminPersistenceException {
    GroupRow superGroup = null;

    if (group.superGroupId != -1) {
      superGroup = getGroup(group.superGroupId);
      if (superGroup == null) {
        throw new AdminPersistenceException("GroupTable.createGroup",
            SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
            "father group id: '" + group.superGroupId + "'");
      }
    }

    SynchroReport.debug("GroupTable.createGroup()", "Ajout de " + group.name
        + ", requête : " + INSERT_GROUP, null);
    insertRow(INSERT_GROUP, group);
    SynchroReport.debug("GroupTable.createGroup()", "Ajout du groupe "
        + group.name + " dans la table ST_UserSet", null);
    organization.userSet.createUserSet("G", group.id);

    if (superGroup != null) {
      SynchroReport.debug("GroupTable.createGroup()",
          "Ajout (ou incrément) de relations entre le groupe " + group.name
              + " d'ID " + group.id + " et son groupe père d'ID "
              + superGroup.id + " dans la table ST_UserSet_UserSet_Rel", null);
      organization.userSet.addUserSetInUserSet("G", group.id, "G",
          superGroup.id);
    }

    CallBackManager.invoke(CallBackManager.ACTION_AFTER_CREATE_GROUP, group.id,
        null, null);
  }

  static final private String INSERT_GROUP = "insert into" + " ST_Group("
      + GROUP_COLUMNS + ")" + " values  (? ,? ,? ,? ,? ,? ,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    GroupRow g = (GroupRow) row;
    if (g.id == -1) {
      g.id = getNextId();
    }

    insert.setInt(1, g.id);

    if (g.specificId == null)
      g.specificId = String.valueOf(g.id);
    insert.setString(2, truncate(g.specificId, 500));

    insert.setInt(3, g.domainId);

    if (g.superGroupId == -1)
      insert.setNull(4, Types.INTEGER);
    else
      insert.setInt(4, g.superGroupId);

    insert.setString(5, truncate(g.name, 100));
    insert.setString(6, truncate(g.description, 500));

    insert.setString(7, g.rule);
  }

  /**
   * Updates a group row.
   */
  public void updateGroup(GroupRow group) throws AdminPersistenceException {
    GroupRow oldGroup = null;

    oldGroup = getGroup(group.id);

    SynchroReport.debug("GroupTable.updateGroup()", "Maj de " + group.name
        + ", Id=" + group.id + ", requête : " + UPDATE_GROUP, null);
    updateRow(UPDATE_GROUP, group);
    if ((oldGroup != null) && (oldGroup.superGroupId != -1)
        && (group.superGroupId != oldGroup.superGroupId)) {
      SynchroReport
          .debug(
              "GroupTable.updateGroup()",
              "Suppression (ou décrément) des relations liant le groupe "
                  + group.name
                  + " d'ID "
                  + group.id
                  + " et le groupe d'ID "
                  + oldGroup.superGroupId
                  + " dans les tables ST_UserSet_UserSet_Rel et ST_UserSet_User_Rel",
              null);
      organization.userSet.removeUserSetFromUserSet("G", group.id, "G",
          oldGroup.superGroupId);
    }
    if ((oldGroup != null) && (group.superGroupId != -1)
        && (group.superGroupId != oldGroup.superGroupId)) {
      SynchroReport
          .debug(
              "GroupTable.updateGroup()",
              "Ajout (ou incrément) de relations entre le groupe "
                  + group.name
                  + " d'ID "
                  + group.id
                  + " et le groupe d'ID "
                  + group.superGroupId
                  + " dans les tables ST_UserSet_UserSet_Rel et ST_UserSet_User_Rel",
              null);
      organization.userSet.addUserSetInUserSet("G", group.id, "G",
          group.superGroupId);
    }
  }

  static final private String UPDATE_GROUP = "update ST_Group set"
      + " domainId = ?," + " specificId = ?," + " name = ?,"
      + " description = ?," + " superGroupId = ?," + " synchroRule = ?"
      + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    GroupRow g = (GroupRow) row;

    update.setInt(1, g.domainId);

    if (g.specificId == null)
      g.specificId = String.valueOf(g.id);
    update.setString(2, truncate(g.specificId, 500));

    update.setString(3, truncate(g.name, 100));
    update.setString(4, truncate(g.description, 500));
    if (g.superGroupId != -1) {
      update.setInt(5, g.superGroupId);
    } else {
      update.setNull(5, Types.INTEGER);
    }
    update.setString(6, g.rule);

    update.setInt(7, g.id);
  }

  /**
   * Delete the group and all the sub-groups
   */
  public void removeGroup(int id) throws AdminPersistenceException {
    CallBackManager.invoke(CallBackManager.ACTION_BEFORE_REMOVE_GROUP, id,
        null, null);

    GroupRow group = getGroup(id);
    if (group == null)
      return;

    SynchroReport.info("GroupTable.removeGroup()", "Suppression du groupe "
        + group.name + " dans la base...", null);
    // remove the group from each role where it's used.
    UserRoleRow[] roles = organization.userRole.getDirectUserRolesOfGroup(id);
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de "
        + group.name + " des rôles dans la base", null);
    for (int i = 0; i < roles.length; i++) {
      organization.userRole.removeGroupFromUserRole(id, roles[i].id);
    }

    // remove the group from each space role where it's used.
    SpaceUserRoleRow[] spaceRoles = organization.spaceUserRole
        .getDirectSpaceUserRolesOfGroup(id);
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de "
        + group.name + " comme manager d'espace dans la base", null);
    for (int i = 0; i < spaceRoles.length; i++) {
      organization.spaceUserRole.removeGroupFromSpaceUserRole(id,
          spaceRoles[i].id);
    }

    // remove all subgroups.
    GroupRow[] subGroups = getDirectSubGroups(id);
    if (subGroups.length > 0)
      SynchroReport.info("GroupTable.removeGroup()",
          "Suppression des groupes fils de " + group.name + " dans la base",
          null);
    for (int i = 0; i < subGroups.length; i++) {
      removeGroup(subGroups[i].id);
    }

    // remove from the group any user.
    UserRow[] users = organization.user.getDirectUsersOfGroup(id);
    for (int i = 0; i < users.length; i++) {
      removeUserFromGroup(users[i].id, id);
    }
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de "
        + users.length + " utilisateurs inclus directement dans le groupe "
        + group.name + " dans la base", null);

    // remove the empty group.
    organization.userSet.removeUserSet("G", id);
    SynchroReport.debug("GroupTable.removeGroup()", "Suppression de "
        + group.name + " (ID=" + id + "), requête : " + DELETE_GROUP, null);
    updateRelation(DELETE_GROUP, id);
  }

  static final private String DELETE_GROUP = "delete from ST_Group where id = ?";

  /**
   * Tests if a user is in a given group (recursive).
   */
  public boolean isUserInGroup(int userId, int groupId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupId };
    Integer result = getInteger(SELECT_COUNT_USERSET_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_USERSET_USER_REL = "select linksCount from ST_UserSet_User_Rel"
      + " where userId = ? and userSetId = ? and userSetType='G'";

  /**
   * Tests if a user is in given group (not recursive).
   */
  public boolean isUserDirectlyInGroup(int userId, int groupId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupId };
    Integer result = getInteger(SELECT_COUNT_GROUP_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_GROUP_USER_REL = "select count(*) from ST_Group_User_Rel"
      + " where userId = ? and groupId = ?";

  /**
   * Add an user in this group.
   */
  public void addUserInGroup(int userId, int groupId)
      throws AdminPersistenceException {
    if (isUserDirectlyInGroup(userId, groupId))
      return;

    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException("GroupTable.addUserInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
          "user id: '" + userId + "'");
    }

    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.addUserInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id: '" + groupId + "'");
    }

    int[] params = new int[] { groupId, userId };
    SynchroReport.debug("GroupTable.addUserInGroup()",
        "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID "
            + groupId + ", requête : " + INSERT_A_GROUP_USER_REL, null);
    updateRelation(INSERT_A_GROUP_USER_REL, params);
    SynchroReport.debug("GroupTable.addUserInGroup()",
        "Ajout (ou incrément) de relations entre l'utilisateur d'ID " + userId
            + " et le groupe d'ID : " + groupId
            + " dans la table ST_UserSet_User_Rel", null);
    organization.userSet.addUserInUserSet(userId, "G", groupId);
  }

  /**
   * Add an user in this group.
   */
  public void addUsersInGroup(String[] userIds, int groupId,
      boolean checkRelation) throws AdminPersistenceException {
    SilverTrace.info("admin", "GroupTable.addUsersInGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId + ", userIds = "
            + userIds.toString());

    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.addUsersInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id: '" + groupId + "'");
    }

    int userId;
    for (int u = 0; u < userIds.length; u++) {
      userId = Integer.parseInt(userIds[u]);

      boolean userInGroup = false;
      if (checkRelation) {
        userInGroup = isUserDirectlyInGroup(userId, groupId);
      }

      if (!userInGroup) {
        UserRow user = organization.user.getUser(userId);
        if (user == null) {
          throw new AdminPersistenceException("GroupTable.addUsersInGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
              "user id: '" + userId + "'");
        }

        int[] params = new int[] { groupId, userId };
        SynchroGroupReport.debug("GroupTable.addUsersInGroup()",
            "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID "
                + groupId + ", requête : " + INSERT_A_GROUP_USER_REL, null);
        updateRelation(INSERT_A_GROUP_USER_REL, params);
        SynchroGroupReport.debug("GroupTable.addUsersInGroup()",
            "Ajout (ou incrément) de relations entre l'utilisateur d'ID "
                + userId + " et le groupe d'ID : " + groupId
                + " dans la table ST_UserSet_User_Rel", null);
        organization.userSet.addUserInUserSet(userId, "G", groupId);
      }
    }
  }

  static final private String INSERT_A_GROUP_USER_REL = "insert into ST_Group_User_Rel(groupId, userId) values(?,?)";

  /**
   * Removes an user from this group.
   */
  public void removeUserFromGroup(int userId, int groupId)
      throws AdminPersistenceException {
    if (!isUserDirectlyInGroup(userId, groupId)) {
      throw new AdminPersistenceException("GroupTable.removeUserFromGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP",
          "group id: '" + groupId + "', user id: '" + userId + "'");
    }
    SynchroReport.debug("GroupTable.removeUserFromGroup()",
        "Retrait (ou décrément) des relations liant l'utilisateur d'ID "
            + userId + " et le groupe d'ID : " + groupId
            + " dans la table ST_UserSet_User_Rel", null);
    organization.userSet.removeUserFromUserSet(userId, "G", groupId);

    int[] params = new int[] { groupId, userId };
    SynchroReport.debug("GroupTable.removeUserFromGroup()",
        "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID "
            + groupId + ", requête : " + DELETE_GROUP_USER_REL, null);
    updateRelation(DELETE_GROUP_USER_REL, params);
  }

  static final private String DELETE_GROUP_USER_REL = "delete from ST_Group_User_Rel where groupId = ? and userId = ?";

  /**
   * Add an user in this group.
   */
  public void removeUsersFromGroup(String[] userIds, int groupId,
      boolean checkRelation) throws AdminPersistenceException {
    SilverTrace.info("admin", "GroupTable.removeUsersFromGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId + ", userIds = "
            + userIds.toString());
    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.removeUsersFromGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group id: '" + groupId + "'");
    }

    int userId;
    for (int u = 0; u < userIds.length; u++) {
      userId = Integer.parseInt(userIds[u]);

      boolean userInGroup = true;
      if (checkRelation) {
        userInGroup = isUserDirectlyInGroup(userId, groupId);
      }

      if (userInGroup) {
        SynchroGroupReport.debug("GroupTable.removeUsersFromGroup()",
            "Retrait (ou décrément) des relations liant l'utilisateur d'ID "
                + userId + " et le groupe d'ID : " + groupId
                + " dans la table ST_UserSet_User_Rel", null);
        organization.userSet.removeUserFromUserSet(userId, "G", groupId);

        int[] params = new int[] { groupId, userId };
        SynchroGroupReport.debug("GroupTable.removeUsersFromGroup()",
            "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID "
                + groupId + ", requête : " + DELETE_GROUP_USER_REL, null);
        updateRelation(DELETE_GROUP_USER_REL, params);
      } else {
        throw new AdminPersistenceException(
            "GroupTable.removeUsersFromGroup()", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_IN_GROUP", "group id: '" + groupId
                + "', user id: '" + userId + "'");
      }
    }
  }

  /**
   * Fetch the current group row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchGroup(rs);
  }

  private OrganizationSchema organization = null;
}
