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
 * "http://www.silverpeas.com/legal/licensing"
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

import com.stratelia.silverpeas.domains.ldapdriver.LDAPUtility;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A UserTable object manages the ST_User table.
 */
public class UserTable extends Table<UserRow> {

  public UserTable(OrganizationSchema schema) {
    super(schema, "ST_User");
    this.organization = schema;
  }
  static final private String USER_COLUMNS = "id, specificId, domainId, login, firstName, lastName,"
      + "loginMail, email, accessLevel, loginQuestion, loginAnswer";

  /**
   * Fetch the current user row from a resultSet.
   * @param rs
   * @return
   * @throws SQLException 
   */
  protected UserRow fetchUser(ResultSet rs) throws SQLException {
    UserRow u = new UserRow();
    u.id = rs.getInt(1);
    u.specificId = rs.getString(2);
    u.domainId = rs.getInt(3);
    u.login = rs.getString(4);
    u.firstName = rs.getString(5);
    u.lastName = rs.getString(6);
    u.loginMail = rs.getString(7);
    u.eMail = rs.getString(8);
    u.accessLevel = rs.getString(9);
    u.loginQuestion = rs.getString(10);
    u.loginAnswer = rs.getString(11);
    return u;
  }

  public int getUserNumberOfDomain(int domainId) throws AdminPersistenceException {
    return getCount("ST_User", "id", "domainId = ? AND accessLevel <> ?", domainId, "R");
  }

  /**
   * Returns the User number
   * @return the User number
   * @throws AdminPersistenceException 
   */
  public int getUserNumber() throws AdminPersistenceException {
    return getCount("ST_User", "id", "accessLevel <> ?", "R");
  }

  /**
   * Returns the User whith the given id.
   * @param id
   * @return the User whith the given id.
   * @throws AdminPersistenceException 
   */
  public UserRow getUser(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_USER_BY_ID, id);
  }
  static final private String SELECT_USER_BY_ID = "SELECT " + USER_COLUMNS
      + " FROM ST_User WHERE id = ?";

  /**
   * Returns the User with the given specificId and login.
   * @param domainId
   * @param specificId
   * @return the User with the given specificId and login.
   * @throws AdminPersistenceException 
   */
  public UserRow getUserBySpecificId(int domainId, String specificId) throws
      AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USER_BY_SPECIFICID_AND_LOGIN, new int[]{domainId}, new String[]{
          specificId});
    UserRow[] users = rows.toArray(new UserRow[rows.size()]);

    if (users.length == 0) {
      return null;
    }
    if (users.length == 1) {
      return users[0];
    }

    throw new AdminPersistenceException("Usertable.getUserBySpecificId", SilverpeasException.ERROR,
        "admin.EX_ERR_LOGIN_FOUND_TWICE", "domain id : '" + domainId + "', user specific Id: '"
        + specificId + "'");
  }
  static final private String SELECT_USER_BY_SPECIFICID_AND_LOGIN = "select "
      + USER_COLUMNS + " from ST_User where domainId = ? and specificId = ?";

  /**
   * Returns the User with the given specificId and login.
   * @param domainId
   * @param specificIds
   * @return
   * @throws AdminPersistenceException 
   */
  public UserRow[] getUsersBySpecificIds(int domainId, List<String> specificIds) throws
      AdminPersistenceException {
    if (specificIds == null || specificIds.isEmpty()) {
      return null;
    }

    StringBuilder clauseIN = new StringBuilder("(");
    String specificId = null;
    for (int s = 0; s < specificIds.size(); s++) {
      if (s != 0) {
        clauseIN.append(", ");
      }
      specificId = specificIds.get(s);
      clauseIN.append("'").append(LDAPUtility.dblBackSlashesForDNInFilters(specificId)).append("'");
    }
    clauseIN.append(")");
    String query = SELECT_USERS_BY_SPECIFICIDS + clauseIN;
    List<UserRow> rows = getRows(query, domainId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_USERS_BY_SPECIFICIDS = "select " + USER_COLUMNS
      + " from ST_User where domainId = ? and specificId IN ";

  /**
   * Returns the User with the given domainId and login.
   * @param domainId
   * @param login
   * @return the User with the given domainId and login.
   * @throws AdminPersistenceException 
   */
  public UserRow getUserByLogin(int domainId, String login) throws AdminPersistenceException {
    List<UserRow> users = getRows(SELECT_USER_BY_DOMAINID_AND_LOGIN, new int[]{domainId}, new String[]{
          login});
    SynchroReport.debug("UserTable.getUserByLogin()", "Vérification que le login" + login
        + " du domaine no " + domainId + " n'est pas présent dans la base, requête : "
        + SELECT_USER_BY_DOMAINID_AND_LOGIN, null);
    if (users.isEmpty()) {
      return null;
    }
    if (users.size() == 1) {
      return users.get(0);
    }
    throw new AdminPersistenceException("Usertable.getUserByLogin", SilverpeasException.ERROR,
        "admin.EX_ERR_LOGIN_FOUND_TWICE", "domain id : '" + domainId + "', user login: '"
        + login + "'");
  }
  static final private String SELECT_USER_BY_DOMAINID_AND_LOGIN = "select " + USER_COLUMNS
      + " from ST_User where domainId = ? and lower(login) = lower(?)";

  /**
   * Returns all the Users.
   * @return
   * @throws AdminPersistenceException 
   */
  public UserRow[] getAllUsers() throws AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_ALL_USERS);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_ALL_USERS = "select " + USER_COLUMNS
      + " from ST_User where accessLevel <> 'R' order by lastName";

  /**
   * Returns all the User ids.
   * @return all the User ids.
   * @throws AdminPersistenceException 
   */
  public String[] getAllUserIds() throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_ALL_USER_IDS);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_ALL_USER_IDS =
      "select id from ST_User where accessLevel <> 'R' order by lastName";

  /**
   * Returns all the Admin ids.
   * @param fromUser
   * @return
   * @throws AdminPersistenceException 
   */
  public String[] getAllAdminIds(UserDetail fromUser) throws AdminPersistenceException {
    if (fromUser.isAccessAdmin() || fromUser.isAccessDomainManager()) {
      List<String> rows = getIds(SELECT_ALL_ADMIN_IDS_TRUE);
      return rows.toArray(new String[rows.size()]);
    }
    List<String> rows = getIds(SELECT_ALL_ADMIN_IDS_DOMAIN, Integer.parseInt(fromUser.getDomainId()));
    if (rows.isEmpty()) {
      rows = getIds(SELECT_ALL_ADMIN_IDS_TRUE);
    }
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_ALL_ADMIN_IDS_TRUE =
      "select id from ST_User where accessLevel='A' order by lastName";
  static final private String SELECT_ALL_ADMIN_IDS_DOMAIN = "select id from ST_User where "
      + "((accessLevel='A') or (accessLevel='D')) and (domainId = ?) order by lastName";

  /**
   *  Returns all the User ids.
   * @param accessLevel
   * @return
   * @throws AdminPersistenceException 
   */
  public String[] getUserIdsByAccessLevel(String accessLevel) throws AdminPersistenceException {
    String[] params = new String[]{accessLevel};
    List<String> rows = getIds(SELECT_USER_IDS_BY_ACCESS_LEVEL, new int[0], params);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_BY_ACCESS_LEVEL =
      "select id from ST_User where accessLevel=? order by lastName";

  /**
   * Returns all the User ids.
   * @param domainId
   * @param accessLevel
   * @return
   * @throws AdminPersistenceException 
   */
  public String[] getUserIdsOfDomainByAccessLevel(int domainId, String accessLevel) throws
      AdminPersistenceException {
    String[] params = new String[]{accessLevel};
    int[] domainIds = new int[]{domainId};
    List<String> rows = getIds(SELECT_USER_IDS_BY_ACCESS_LEVEL_AND_DOMAIN, domainIds, params);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_BY_ACCESS_LEVEL_AND_DOMAIN =
      "select id from ST_User where domainId = ? AND accessLevel=? order by lastName";

  /**
   * Returns all the Users which compose a group.
   * @param groupId
   * @return all the Users which compose a group.
   * @throws AdminPersistenceException 
   */
  public UserRow[] getDirectUsersOfGroup(int groupId) throws AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USERS_IN_GROUP, groupId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_USERS_IN_GROUP = "select " + USER_COLUMNS
      + " from ST_User,ST_Group_User_Rel where id = userId and groupId = ? and accessLevel <> 'R'"
      + " order by lastName";

  /**
   * Returns all the User ids which compose a group.
   * @param groupId
   * @return all the User ids which compose a group.
   * @throws AdminPersistenceException 
   */
  public String[] getDirectUserIdsOfGroup(int groupId) throws AdminPersistenceException {
    SynchroReport.debug("UserTable.getDirectUserIdsOfGroup()",
        "Recherche des utilisateurs inclus directement dans le groupe d'ID " + groupId
        + ", requête : " + SELECT_USER_IDS_IN_GROUP, null);
    List<String> rows = getIds(SELECT_USER_IDS_IN_GROUP, groupId);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_IN_GROUP = "select id from ST_User,ST_Group_User_Rel"
      + " where id = userId and groupId = ? and accessLevel <> 'R' order by lastName";

  /**
   * Returns all the Users having directly a given role.
   * @param userRoleId
   * @return
   * @throws AdminPersistenceException 
   */
  public UserRow[] getDirectUsersOfUserRole(int userRoleId) throws AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USERS_IN_USERROLE, userRoleId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_USERS_IN_USERROLE = "select " + USER_COLUMNS
      + " from ST_User,ST_UserRole_User_Rel where id = userId and userRoleId = ? and "
      + "accessLevel <> 'R' order by lastName";

  /**
   * Returns all the User ids having directly a given role.
   * @param userRoleId
   * @return
   * @throws AdminPersistenceException 
   */
  public String[] getDirectUserIdsOfUserRole(int userRoleId) throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_USER_IDS_IN_USERROLE, userRoleId);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_IN_USERROLE = "select id from ST_User,"
      + "ST_UserRole_User_Rel where id = userId and userRoleId = ? and accessLevel <> 'R'"
      + " order by lastName";

  /**
   * Returns all the Users having a given domain id.
   * @param domainId
   * @return all the Users having a given domain id.
   * @throws AdminPersistenceException 
   */
  public UserRow[] getAllUserOfDomain(int domainId) throws AdminPersistenceException {
    SynchroReport.debug("UserTable.getAllUserOfDomain()", "Recherche de l'ensemble des "
        + "utilisateurs du domaine LDAP dans la base (ID " + domainId + "), requête : "
        + SELECT_ALL_USERS_IN_DOMAIN, null);
    List<UserRow> rows = getRows(SELECT_ALL_USERS_IN_DOMAIN, domainId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_ALL_USERS_IN_DOMAIN = "select " + USER_COLUMNS
      + " from ST_User where domainId=? and accessLevel <> 'R'" + " order by lastName";

  /**
   * Returns all the User ids having a given domain id.
   * @param domainId
   * @return all the User ids having a given domain id.
   * @throws AdminPersistenceException 
   */
  public String[] getUserIdsOfDomain(int domainId) throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_ALL_USER_IDS_IN_DOMAIN, domainId);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_ALL_USER_IDS_IN_DOMAIN =
      "select id from ST_User where domainId=? and accessLevel <> 'R' order by lastName";

  /**
   * Returns all the Users having directly a given space userRole.
   * @param spaceUserRoleId
   * @return all the Users having directly a given space userRole.
   * @throws AdminPersistenceException 
   */
  public UserRow[] getDirectUsersOfSpaceUserRole(int spaceUserRoleId) throws
      AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USERS_IN_SPACEUSERROLE, spaceUserRoleId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_USERS_IN_SPACEUSERROLE = "select "
      + USER_COLUMNS + " from ST_User,ST_SpaceUserRole_User_Rel"
      + " where id = userId and spaceUserRoleId = ? and accessLevel <> 'R'";

  /**
   * Returns all the User ids having directly a given space userRole.
   * @param spaceUserRoleId
   * @return all the User ids having directly a given space userRole.
   * @throws AdminPersistenceException 
   */
  public String[] getDirectUserIdsOfSpaceUserRole(int spaceUserRoleId) throws
      AdminPersistenceException {
    List<String> rows = getIds(SELECT_USER_IDS_IN_SPACEUSERROLE, spaceUserRoleId);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_IN_SPACEUSERROLE = "select id from ST_User, "
      + "ST_SpaceUserRole_User_Rel where id = userId and spaceUserRoleId = ? and accessLevel <> 'R'";

  /**
   * Returns all the Users having directly a given group userRole.
   * @param groupUserRoleId
   * @return all the Users having directly a given group userRole.
   * @throws AdminPersistenceException 
   */
  public UserRow[] getDirectUsersOfGroupUserRole(int groupUserRoleId) throws
      AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USERS_IN_GROUPUSERROLE, groupUserRoleId);
    return rows.toArray(new UserRow[rows.size()]);
  }
  static final private String SELECT_USERS_IN_GROUPUSERROLE = "select " + USER_COLUMNS
      + " from ST_User, ST_GroupUserRole_User_Rel where id = userId and groupUserRoleId = ? "
      + "and accessLevel <> 'R'";

  /**
   * Returns all the User ids having directly a given group userRole.
   * @param groupUserRoleId
   * @return all the User ids having directly a given group userRole.
   * @throws AdminPersistenceException 
   */
  public String[] getDirectUserIdsOfGroupUserRole(int groupUserRoleId) throws
      AdminPersistenceException {
    List<String> rows =  getIds(SELECT_USER_IDS_IN_GROUPUSERROLE, groupUserRoleId);
    return rows.toArray(new String[rows.size()]);
  }
  static final private String SELECT_USER_IDS_IN_GROUPUSERROLE = "select id from ST_User, "
      + "ST_GroupUserRole_User_Rel where id = userId and groupUserRoleId = ? and accessLevel <> 'R'";

  /**
   * 
   * @param userIds
   * @param userModel
   * @return
   * @throws AdminPersistenceException 
   */
  public String[] searchUsersIds(List<String> userIds, UserRow userModel) throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr = ") AND (";
    List<Integer> ids = new ArrayList<Integer>();
    List<String> params = new ArrayList<String>();

    // WARNING !!! Ids must all be set before Params !!!!
    boolean manualFiltering = userIds != null && !userIds.isEmpty() && userIds.size() > 100;
    StringBuffer theQuery = new StringBuffer(SELECT_SEARCH_USERSID);
    if (userIds != null && !userIds.isEmpty() && userIds.size() <= 100) {
      theQuery.append(" WHERE (ST_User.id IN (").append(list2String(userIds)).append(") ");
      concatAndOr = true;
    }
    concatAndOr = addIdToQuery(ids, theQuery, userModel.id, "ST_User.id", concatAndOr, andOr);
    if (userModel.domainId >= 0) { // users are not bound to "domaine mixte"
      concatAndOr = addIdToQuery(ids, theQuery, userModel.domainId,
          "ST_User.domainId", concatAndOr, andOr);
    }
    concatAndOr = addParamToQuery(params, theQuery, userModel.specificId, "ST_User.specificId",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.login, "ST_User.login", concatAndOr,
        andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.firstName, "ST_User.firstName",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.lastName, "ST_User.lastName",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.eMail, "ST_User.email", concatAndOr,
        andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.accessLevel, "ST_User.accessLevel",
        concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, userModel.loginQuestion, "ST_User.loginQuestion",
        concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, userModel.loginAnswer, "ST_User.loginAnswer",
        concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(") AND (accessLevel <> 'R')");
    } else {
      theQuery.append(" WHERE (accessLevel <> 'R')");
    }
    theQuery.append(" order by UPPER(ST_User.lastName)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ids.get(i).intValue();
    }
    List<String> result =
        getIds(theQuery.toString(), idsArray, params.toArray(new String[params.size()]));
    if (manualFiltering) {
      result.retainAll(userIds);
    }
    return result.toArray(new String[result.size()]);
  }
  static final private String SELECT_SEARCH_USERSID =
      "select DISTINCT ST_User.id, UPPER(ST_User.lastName) from ST_User";

  /**
   * Returns all the Users satiffying the model
   * @param userModel
   * @param isAnd
   * @return
   * @throws AdminPersistenceException
   */
  public UserRow[] searchUsers(UserRow userModel, boolean isAnd) throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr;
    StringBuffer theQuery = new StringBuffer(SELECT_SEARCH_USERS);
    List<Integer> ids = new ArrayList<Integer>();
    List<String> params = new ArrayList<String>();

    if (isAnd) {
      andOr = ") AND (";
    } else {
      andOr = ") OR (";
    }
    concatAndOr = addIdToQuery(ids, theQuery, userModel.id, "id", concatAndOr, andOr);
    concatAndOr = addIdToQuery(ids, theQuery, userModel.domainId, "domainId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.specificId, "specificId", concatAndOr,
        andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.login, "login", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.firstName, "firstName", concatAndOr,
        andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.lastName, "lastName", concatAndOr,
        andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.eMail, "email", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.accessLevel, "accessLevel",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.loginQuestion, "loginQuestion",
        concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, userModel.loginAnswer, "loginAnswer",
        concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(") AND (accessLevel <> 'R')");
    } else {
      theQuery.append(" WHERE (accessLevel <> 'R')");
    }
    theQuery.append(" order by UPPER(lastName)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ids.get(i).intValue();
    }

    return getRows(theQuery.toString(), idsArray, params.toArray(new String[0])).toArray(
        new UserRow[0]);
  }
  static final private String SELECT_SEARCH_USERS = "select " + USER_COLUMNS
      + ", UPPER(lastName) from ST_User";

  /**
   * Returns the users whose fields match those of the given sample space fields.
   * @param sampleUser
   * @return the users whose fields match those of the given sample space fields.
   * @throws AdminPersistenceException 
   */
  public UserRow[] getAllMatchingUsers(UserRow sampleUser) throws AdminPersistenceException {
    String[] columns = new String[]{"login", "firstName", "lastName", "email"};
    String[] values = new String[]{sampleUser.login, sampleUser.firstName, sampleUser.lastName,
      sampleUser.eMail};
    List<UserRow> rows = getMatchingRows(USER_COLUMNS, columns, values);
    return rows.toArray(new UserRow[rows.size()]);
  }

  /**
   * Inserts in the database a new user row.
   * @param user
   * @throws AdminPersistenceException 
   */
  public void createUser(UserRow user) throws AdminPersistenceException {
    SynchroReport.debug("UserTable.createUser()", "Ajout de " + user.login + ", requête : "
        + INSERT_USER, null);
    insertRow(INSERT_USER, user);
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_AFTER_CREATE_USER, user.id, null, null);
  }
  static final private String INSERT_USER = "insert into ST_User ("
      + USER_COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, UserRow row) throws
      SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }

    insert.setInt(1, row.id);
    insert.setString(2, truncate(row.specificId, 500));
    insert.setInt(3, row.domainId);
    insert.setString(4, truncate(row.login, 50));
    insert.setString(5, truncate(row.firstName, 100));
    insert.setString(6, truncate(row.lastName, 100));
    insert.setString(7, truncate(row.loginMail, 100));
    insert.setString(8, truncate(row.eMail, 100));
    insert.setString(9, truncate(row.accessLevel, 1));
    insert.setString(10, truncate(row.loginQuestion, 200));
    insert.setString(11, truncate(row.loginAnswer, 200));
  }

  /**
   * Update a user row.
   * @param user
   * @throws AdminPersistenceException 
   */
  public void updateUser(UserRow user) throws AdminPersistenceException {
    SynchroReport.debug("UserTable.updateUser()", "Maj de " + user.login + ", Id=" + user.id 
        + ", requête : " + UPDATE_USER, null);
    updateRow(UPDATE_USER, user);
  }
  static final private String UPDATE_USER = "update ST_User set"
      + " specificId = ?,"
      + " domainId = ?,"
      + " login = ?,"
      + " firstName = ?,"
      + " lastName = ?,"
      + " loginMail = ?,"
      + " email = ?,"
      + " accessLevel = ?,"
      + " loginQuestion = ?,"
      + " loginAnswer = ?"
      + " where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, UserRow row) throws
      SQLException {
    update.setString(1, truncate(row.specificId, 500));
    update.setInt(2, row.domainId);
    update.setString(3, truncate(row.login, 50));
    update.setString(4, truncate(row.firstName, 100));
    update.setString(5, truncate(row.lastName, 100));
    update.setString(6, truncate(row.loginMail, 100));
    update.setString(7, truncate(row.eMail, 100));
    update.setString(8, truncate(row.accessLevel, 1));
    update.setString(9, truncate(row.loginQuestion, 200));
    update.setString(10, truncate(row.loginAnswer, 200));

    update.setInt(11, row.id);
  }

  /**
   * Removes a user row.
   * @param id
   * @throws AdminPersistenceException 
   */
  public void removeUser(int id) throws AdminPersistenceException {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_BEFORE_REMOVE_USER, id, null, null);

    UserRow user = getUser(id);
    if (user == null) {
      return;
    }

    SynchroReport.info("UserTable.removeUser()", "Suppression de " + user.login
        + " des groupes dans la base", null);
    GroupRow[] groups = organization.group.getDirectGroupsOfUser(id);
    for (int i = 0; i < groups.length; i++) {
      organization.group.removeUserFromGroup(id, groups[i].id);
    }

    SynchroReport.info("UserTable.removeUser()", "Suppression de " + user.login
        + " des rôles dans la base", null);
    UserRoleRow[] roles = organization.userRole.getDirectUserRolesOfUser(id);
    for (int i = 0; i < roles.length; i++) {
      organization.userRole.removeUserFromUserRole(id, roles[i].id);
    }

    SynchroReport.info("UserTable.removeUser()", "Suppression de " + user.login
        + " en tant que manager d'espace dans la base", null);
    SpaceUserRoleRow[] spaceRoles = organization.spaceUserRole.getDirectSpaceUserRolesOfUser(id);
    for (int i = 0; i < spaceRoles.length; i++) {
      organization.spaceUserRole.removeUserFromSpaceUserRole(id,
          spaceRoles[i].id);
    }

    SynchroReport.info("UserTable.removeUser()", "Delete " + user.login
        + " from user favorite space table", null);
    UserFavoriteSpaceDAO ufsDAO = DAOFactory.getUserFavoriteSpaceDAO();
    if (!ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(id, -1))) {
      throw new AdminPersistenceException("UserTable.removeUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER");
    }

    SynchroReport.debug("UserTable.removeUser()", "Suppression de "
        + user.login + " (ID=" + id + "), requête : " + DELETE_USER, null);

    // updateRelation(DELETE_USER, id);
    // Replace the login by a dummy one that must be unique
    user.login = "???REM???" + java.lang.Integer.toString(id);
    user.accessLevel = "R";
    user.specificId = "???REM???" + java.lang.Integer.toString(id);
    updateRow(UPDATE_USER, user);
  }
  static final private String DELETE_USER = "delete from ST_User where id = ?";

  private static String list2String(List<String> ids) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i != 0) {
        str.append(",");
      }
      str.append(ids.get(i));
    }
    return str.toString();
  }

  /**
   * Fetch the current user row from a resultSet.
   */
  @Override
  protected UserRow fetchRow(ResultSet rs) throws SQLException {
    return fetchUser(rs);
  }
  private OrganizationSchema organization = null;
}