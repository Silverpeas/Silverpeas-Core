/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceProvider;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A UserTable object manages the ST_User table.
 */
public class UserTable extends Table<UserRow> {

  public UserTable(OrganizationSchema schema) {
    super(schema, "ST_User");
    this.organization = schema;
  }

  static final private String USER_COLUMNS =
      "id, specificId, domainId, login, firstName, lastName," +
          " loginMail, email, accessLevel, loginQuestion, loginAnswer, creationDate, saveDate," +
          " version, tosAcceptanceDate, lastLoginDate, nbSuccessfulLoginAttempts," +
          " lastLoginCredentialUpdateDate, expirationDate, state, stateSaveDate, " +
          "notifManualReceiverLimit";

  /**
   * Fetch the current user row from a resultSet.
   * @param rs
   * @return
   * @throws SQLException
   */
  protected UserRow fetchUser(ResultSet rs) throws SQLException {
    UserRow u = new UserRow();
    u.id = rs.getInt("id");
    u.specificId = rs.getString("specificId");
    u.domainId = rs.getInt("domainId");
    u.login = rs.getString("login");
    u.firstName = rs.getString("firstName");
    u.lastName = rs.getString("lastName");
    u.loginMail = rs.getString("loginMail");
    u.eMail = rs.getString("email");
    u.accessLevel = rs.getString("accessLevel");
    u.loginQuestion = rs.getString("loginQuestion");
    u.loginAnswer = rs.getString("loginAnswer");
    u.creationDate = rs.getTimestamp("creationDate");
    u.saveDate = rs.getTimestamp("saveDate");
    u.version = rs.getInt("version");
    u.tosAcceptanceDate = rs.getTimestamp("tosAcceptanceDate");
    u.lastLoginDate = rs.getTimestamp("lastLoginDate");
    u.nbSuccessfulLoginAttempts = rs.getInt("nbSuccessfulLoginAttempts");
    u.lastLoginCredentialUpdateDate = rs.getTimestamp("lastLoginCredentialUpdateDate");
    u.expirationDate = rs.getTimestamp("expirationDate");
    u.state = rs.getString("state");
    u.stateSaveDate = rs.getTimestamp("stateSaveDate");
    if (StringUtil.isInteger(rs.getString("notifManualReceiverLimit"))) {
      u.notifManualReceiverLimit= rs.getInt("notifManualReceiverLimit");
    }
    return u;
  }

  public int getUserNumberOfDomain(int domainId) throws AdminPersistenceException {
    return getCount("ST_User", "domainId = ? AND state <> ?", domainId, "DELETED");
  }

  /**
   * Returns the User number
   * @return the User number
   * @throws AdminPersistenceException
   */
  public int getUserNumber() throws AdminPersistenceException {
    return getCount("ST_User", "state <> ?", "DELETED");
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

  static final private String SELECT_USER_BY_ID =
      "SELECT " + USER_COLUMNS + " FROM ST_User WHERE id = ?";

  /**
   * Returns the User with the given specificId and login.
   * @param domainId
   * @param specificId
   * @return the User with the given specificId and login.
   * @throws AdminPersistenceException
   */
  public UserRow getUserBySpecificId(int domainId, String specificId)
      throws AdminPersistenceException {
    List<Object> params = new ArrayList<>();
    params.add(domainId);
    params.add(specificId);
    List<UserRow> users = getRows(SELECT_USER_BY_SPECIFICID_AND_LOGIN, params);
    if (users.isEmpty()) {
      return null;
    }
    if (users.size() == 1) {
      return users.get(0);
    }
    throw new AdminPersistenceException("Usertable.getUserBySpecificId", SilverpeasException.ERROR,
        "admin.EX_ERR_LOGIN_FOUND_TWICE",
        "domain id : '" + domainId + "', user specific Id: '" + specificId + "'");
  }

  static final private String SELECT_USER_BY_SPECIFICID_AND_LOGIN =
      "SELECT " + USER_COLUMNS + " FROM ST_User WHERE domainId = ? AND specificId = ?";

  /**
   * Returns the User with the given specificId and login.
   * @param domainId
   * @param specificIds
   * @return
   * @throws AdminPersistenceException
   */
  public UserRow[] getUsersBySpecificIds(int domainId, List<String> specificIds)
      throws AdminPersistenceException {
    if (specificIds == null || specificIds.isEmpty()) {
      return null;
    }

    StringBuilder clauseIN = new StringBuilder("(");
    for (int s = 0; s < specificIds.size(); s++) {
      if (s != 0) {
        clauseIN.append(", ");
      }
      String specificId = specificIds.get(s);
      clauseIN.append("'").append(specificId).append("'");
    }
    clauseIN.append(")");
    String query = SELECT_USERS_BY_SPECIFICIDS + clauseIN;
    List<UserRow> rows = getRows(query, domainId);
    return rows.toArray(new UserRow[rows.size()]);
  }

  static final private String SELECT_USERS_BY_SPECIFICIDS =
      "select " + USER_COLUMNS + " from ST_User where domainId = ? and specificId IN ";

  /**
   * Returns the User with the given domainId and login.
   * @param domainId
   * @param login
   * @return the User with the given domainId and login.
   * @throws AdminPersistenceException
   */
  public UserRow getUserByLogin(int domainId, String login) throws AdminPersistenceException {
    List<Object> params = new ArrayList<>();
    params.add(domainId);
    params.add(login);
    List<UserRow> users = getRows(SELECT_USER_BY_DOMAINID_AND_LOGIN, params);
    SynchroDomainReport.debug("UserTable.getUserByLogin()",
        "Vérification que le login" + login + " du domaine no " + domainId +
            " n'est pas présent dans la base, requête : " + SELECT_USER_BY_DOMAINID_AND_LOGIN);
    if (users.isEmpty()) {
      return null;
    }
    if (users.size() == 1) {
      return users.get(0);
    }
    throw new AdminPersistenceException("Usertable.getUserByLogin", SilverpeasException.ERROR,
        "admin.EX_ERR_LOGIN_FOUND_TWICE",
        "domain id : '" + domainId + "', user login: '" + login + "'");
  }

  static final private String SELECT_USER_BY_DOMAINID_AND_LOGIN = "select " + USER_COLUMNS +
      " from ST_User where domainId = ? and lower(login) = lower(?) and state <> 'DELETED'";

  /**
   * Returns all the Users.
   * @return all the users.
   * @throws AdminPersistenceException
   */
  public UserRow[] getAllUsers() throws AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_ALL_USERS);
    return rows.toArray(new UserRow[rows.size()]);
  }

  static final private String SELECT_ALL_USERS =
      "select " + USER_COLUMNS + " from ST_User where state <> 'DELETED' order by lastName";

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
      "select id from ST_User where state <> 'DELETED' order by lastName";

  /**
   * Returns all the Admin ids.
   * @param fromUser
   * @return all the Admin ids.
   * @throws AdminPersistenceException
   */
  public String[] getAllAdminIds(UserDetail fromUser) throws AdminPersistenceException {
    if (fromUser.isAccessAdmin() || fromUser.isAccessDomainManager()) {
      List<String> rows = getIds(SELECT_ALL_ADMIN_IDS_TRUE);
      return rows.toArray(new String[rows.size()]);
    }
    List<String> rows =
        getIds(SELECT_ALL_ADMIN_IDS_DOMAIN, Integer.parseInt(fromUser.getDomainId()));
    if (rows.isEmpty()) {
      rows = getIds(SELECT_ALL_ADMIN_IDS_TRUE);
    }
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_ALL_ADMIN_IDS_TRUE =
      "select id from ST_User where accessLevel='A' and state <> 'DELETED' order by lastName";
  static final private String SELECT_ALL_ADMIN_IDS_DOMAIN = "select id from ST_User where "
      + "((accessLevel='A') or (accessLevel='D')) and (domainId = ?) and state <> 'DELETED' order by lastName";

  /**
   * Returns all the User ids.
   * @param accessLevel
   * @return all the User ids.
   * @throws AdminPersistenceException
   */
  public String[] getUserIdsByAccessLevel(UserAccessLevel accessLevel)
      throws AdminPersistenceException {
    List<String> rows =
        getIds(SELECT_USER_IDS_BY_ACCESS_LEVEL, Collections.singletonList(accessLevel.code()));
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_BY_ACCESS_LEVEL =
      "select id from ST_User where accessLevel=? and state <> 'DELETED' order by lastName";

  /**
   * Returns all the User ids for the specified domain and access level.
   * @param domainId
   * @param accessLevel
   * @return all the User ids for the specified domain and access level.
   * @throws AdminPersistenceException
   */
  public String[] getUserIdsOfDomainByAccessLevel(int domainId, UserAccessLevel accessLevel)
      throws AdminPersistenceException {
    List<Object> params = new ArrayList<>(2);
    params.add(domainId);
    params.add(accessLevel.code());
    List<String> rows = getIds(SELECT_USER_IDS_BY_ACCESS_LEVEL_AND_DOMAIN, params);
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_BY_ACCESS_LEVEL_AND_DOMAIN =
      "select id from ST_User where domainId = ? AND accessLevel=?" +
          " and state <> 'DELETED' order by lastName";

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

  static final private String SELECT_USERS_IN_GROUP = "select " + USER_COLUMNS +
      " from ST_User,ST_Group_User_Rel where id = userId and groupId = ? and state <> 'DELETED'" +
      " order by lastName";

  /**
   * Returns all the User ids which compose a group.
   * @param groupId
   * @return all the User ids which compose a group.
   * @throws AdminPersistenceException
   */
  public String[] getDirectUserIdsOfGroup(int groupId) throws AdminPersistenceException {
    SynchroDomainReport.debug("UserTable.getDirectUserIdsOfGroup()",
        "Recherche des utilisateurs inclus directement dans le groupe d'ID " + groupId +
            ", requête : " + SELECT_USER_IDS_IN_GROUP);
    List<String> rows = getIds(SELECT_USER_IDS_IN_GROUP, groupId);
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_IN_GROUP =
      "select id from ST_User,ST_Group_User_Rel" +
          " where id = userId and groupId = ? and state <> 'DELETED' order by lastName";

  /**
   * Returns all the User ids having directly a given role.
   * @param userRoleId
   * @return all the User ids having directly a given role.
   * @throws AdminPersistenceException
   */
  public String[] getDirectUserIdsOfUserRole(int userRoleId) throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_USER_IDS_IN_USERROLE, userRoleId);
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_IN_USERROLE = "select id from ST_User," +
      "ST_UserRole_User_Rel where id = userId and userRoleId = ? and state <> 'DELETED'" +
      " order by lastName";

  /**
   * Returns all the Users having a given domain id.
   * @param domainId
   * @return all the Users having a given domain id.
   * @throws AdminPersistenceException
   */
  public UserRow[] getAllUserOfDomain(int domainId) throws AdminPersistenceException {
    SynchroDomainReport.debug("UserTable.getAllUserOfDomain()",
        "Recherche de l'ensemble des " + "utilisateurs du domaine LDAP dans la base (ID " +
            domainId + "), requête : " + SELECT_ALL_USERS_IN_DOMAIN);
    List<UserRow> rows = getRows(SELECT_ALL_USERS_IN_DOMAIN, domainId);
    return rows.toArray(new UserRow[rows.size()]);
  }

  static final private String SELECT_ALL_USERS_IN_DOMAIN =
      "select " + USER_COLUMNS + " from ST_User where domainId=? and state <> 'DELETED'" +
          " order by lastName";

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
      "select id from ST_User where domainId=? and state <> 'DELETED' order by lastName";

  /**
   * Returns all the User ids having directly a given space userRole.
   * @param spaceUserRoleId
   * @return all the User ids having directly a given space userRole.
   * @throws AdminPersistenceException
   */
  public String[] getDirectUserIdsOfSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_USER_IDS_IN_SPACEUSERROLE, spaceUserRoleId);
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_IN_SPACEUSERROLE = "select id from ST_User, " +
      "ST_SpaceUserRole_User_Rel where id = userId and spaceUserRoleId = ? and state <> 'DELETED'";

  /**
   * Returns all the Users having directly a given group userRole.
   * @param groupUserRoleId
   * @return all the Users having directly a given group userRole.
   * @throws AdminPersistenceException
   */
  public UserRow[] getDirectUsersOfGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    List<UserRow> rows = getRows(SELECT_USERS_IN_GROUPUSERROLE, groupUserRoleId);
    return rows.toArray(new UserRow[rows.size()]);
  }

  static final private String SELECT_USERS_IN_GROUPUSERROLE = "select " + USER_COLUMNS +
      " from ST_User, ST_GroupUserRole_User_Rel where id = userId and groupUserRoleId = ? " +
      "and state <> 'DELETED'";

  /**
   * Returns all the User ids having directly a given group userRole.
   * @param groupUserRoleId
   * @return all the User ids having directly a given group userRole.
   * @throws AdminPersistenceException
   */
  public String[] getDirectUserIdsOfGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    List<String> rows = getIds(SELECT_USER_IDS_IN_GROUPUSERROLE, groupUserRoleId);
    return rows.toArray(new String[rows.size()]);
  }

  static final private String SELECT_USER_IDS_IN_GROUPUSERROLE = "select id from ST_User, " +
      "ST_GroupUserRole_User_Rel where id = userId and groupUserRoleId = ? and state <> 'DELETED'";

  /**
   * Centralization.
   * @param params
   * @param query
   * @param userModel
   * @param concatAndOr
   * @param andOr
   * @return
   */
  private boolean addCommonUserParamToQuery(Collection<Object> params, StringBuilder query,
      UserRow userModel, boolean concatAndOr, String andOr) {

    // Optional filters
    concatAndOr = addIdToQuery(params, query, userModel.id, "id", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.specificId, "specificId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, query, userModel.login, "login", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.firstName, "firstName", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.lastName, "lastName", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, query, userModel.eMail, "email", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.accessLevel, "accessLevel", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.loginQuestion, "loginQuestion", concatAndOr,
            andOr);
    concatAndOr =
        addParamToQuery(params, query, userModel.loginAnswer, "loginAnswer", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.creationDate), "creationDate",
            concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.saveDate), "saveDate", concatAndOr,
            andOr);
    concatAndOr = addParamToQuery(params, query, getSqlTimestamp(userModel.tosAcceptanceDate),
        "tosAcceptanceDate", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.lastLoginDate), "lastLoginDate",
            concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.lastLoginCredentialUpdateDate),
            "lastLoginCredentialUpdateDate", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.expirationDate), "expirationDate",
            concatAndOr, andOr);
    if (!UserState.UNKNOWN.equals(UserState.from(userModel.state))) {
      concatAndOr = addParamToQuery(params, query, userModel.state, "state", concatAndOr, andOr);
    }
    concatAndOr =
        addParamToQuery(params, query, getSqlTimestamp(userModel.stateSaveDate), "stateSaveDate",
            concatAndOr, andOr);
    if (userModel.notifManualReceiverLimit != null) {
      concatAndOr = addParamToQuery(params, query, userModel.notifManualReceiverLimit,
          "notifManualReceiverLimit", concatAndOr, andOr);
    }

    // Mandatory filters
    if (concatAndOr) {
      query.append(") AND (state <> 'DELETED')");
    } else {
      query.append(" WHERE (state <> 'DELETED')");
    }
    query.append(" order by UPPER(lastName)");
    return concatAndOr;
  }

  /**
   * Returns all user ids satisfying the model.
   * @param userIds
   * @param userModel
   * @return
   * @throws AdminPersistenceException
   */
  public String[] searchUsersIds(List<String> userIds, UserRow userModel)
      throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr = ") AND (";
    List<Object> params = new ArrayList<>();

    boolean manualFiltering = userIds != null && !userIds.isEmpty() && userIds.size() > 100;
    StringBuilder query = new StringBuilder(SELECT_SEARCH_USERSID);
    if (userIds != null && !userIds.isEmpty() && userIds.size() <= 100) {
      query.append(" WHERE (id IN (").append(list2String(userIds)).append(") ");
      concatAndOr = true;
    }
    if (userModel.domainId >= 0) {
      // users are not bound to "domaine mixte"
      concatAndOr = addIdToQuery(params, query, userModel.domainId, "domainId", concatAndOr, andOr);
    }
    addCommonUserParamToQuery(params, query, userModel, concatAndOr, andOr);

    List<String> result = getIds(query.toString(), params);
    if (manualFiltering) {
      result.retainAll(userIds);
    }
    return result.toArray(new String[result.size()]);
  }

  static final private String SELECT_SEARCH_USERSID =
      "select DISTINCT id, UPPER(lastName) from ST_User";

  /**
   * Returns all the Users satisfying the model.
   * @param userModel
   * @param isAnd
   * @return all the Users satisfying the model.
   * @throws AdminPersistenceException
   */
  public UserRow[] searchUsers(UserRow userModel, boolean isAnd) throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr = isAnd ? ") AND (" : ") OR (";
    StringBuilder query = new StringBuilder(SELECT_SEARCH_USERS);
    List<Object> params = new ArrayList<>();
    concatAndOr = addIdToQuery(params, query, userModel.domainId, "domainId", concatAndOr, andOr);
    addCommonUserParamToQuery(params, query, userModel, concatAndOr, andOr);
    List<UserRow> rows = getRows(query.toString(), params);
    return rows.toArray(new UserRow[rows.size()]);
  }

  static final private String SELECT_SEARCH_USERS =
      "select " + USER_COLUMNS + ", UPPER(lastName) from ST_User";
  private static final String SELECT_SEARCH_BY_EMAIL = "select " + USER_COLUMNS +
      ", UPPER(lastName) from ST_User where state <> 'DELETED' AND email = ?";

  /**
   * Returns the users whose fields match those of the given sample space fields.
   * @param email
   * @return the users whose fields match those of the given sample space fields.
   * @throws AdminPersistenceException
   */
  public UserRow[] getUsersByEmail(String email) throws AdminPersistenceException {
    List<UserRow> users = getRows(SELECT_SEARCH_BY_EMAIL, Collections.singletonList(email));
    return users.toArray(new UserRow[users.size()]);
  }

  /**
   * Inserts in the database a new user row.
   * @param user
   * @throws AdminPersistenceException
   */
  public void createUser(UserRow user) throws AdminPersistenceException {
    SynchroDomainReport
        .debug("UserTable.createUser()", "Ajout de " + user.login + ", requête : " + INSERT_USER);
    insertRow(INSERT_USER, user);
  }

  static final private String INSERT_USER = "insert into ST_User (" + USER_COLUMNS +
      ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, UserRow row)
      throws SQLException {
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
    Timestamp now = getSqlTimestamp(new Date());
    insert.setTimestamp(12, now);
    insert.setTimestamp(13, now);
    insert.setInt(14, 0);
    insert.setTimestamp(15, getSqlTimestamp(row.tosAcceptanceDate));
    insert.setTimestamp(16, getSqlTimestamp(row.lastLoginDate));
    insert.setInt(17, row.nbSuccessfulLoginAttempts);
    insert.setTimestamp(18, getSqlTimestamp(row.lastLoginCredentialUpdateDate));
    insert.setTimestamp(19, getSqlTimestamp(row.expirationDate));
    insert.setString(20,
        !UserState.UNKNOWN.equals(UserState.from(row.state)) ? row.state : UserState.VALID.name());
    insert.setTimestamp(21, now);
    insert.setObject(22, row.notifManualReceiverLimit);
  }

  /**
   * Update a user row.
   * @param user
   * @throws AdminPersistenceException
   */
  public void updateUser(UserRow user) throws AdminPersistenceException {
    SynchroDomainReport.debug("UserTable.updateUser()",
        "Maj de " + user.login + ", Id=" + user.id + ", requête : " + UPDATE_USER);
    updateRow(UPDATE_USER, user);
  }

  static final private String UPDATE_USER =
      "update ST_User set" + " specificId = ?," + " domainId = ?," + " login = ?," +
          " firstName = ?," + " lastName = ?," + " loginMail = ?," + " email = ?," +
          " accessLevel = ?," + " loginQuestion = ?," + " loginAnswer = ?," + " saveDate = ?," +
          " version = ?," + " tosAcceptanceDate = ?," + " lastLoginDate = ?," +
          " nbSuccessfulLoginAttempts = ?," + " lastLoginCredentialUpdateDate = ?," +
          " expirationDate = ?," + " state = ?," + " stateSaveDate = ?," +
          " notifManualReceiverLimit = ?" + " where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, UserRow row)
      throws SQLException {
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
    update.setTimestamp(11, getSqlTimestamp(new Date()));
    update.setInt(12, (row.version + 1));
    update.setTimestamp(13, getSqlTimestamp(row.tosAcceptanceDate));
    update.setTimestamp(14, getSqlTimestamp(row.lastLoginDate));
    update.setInt(15, row.nbSuccessfulLoginAttempts);
    update.setTimestamp(16, getSqlTimestamp(row.lastLoginCredentialUpdateDate));
    update.setTimestamp(17, getSqlTimestamp(row.expirationDate));
    update.setString(18, row.state);
    update.setTimestamp(19, getSqlTimestamp(row.stateSaveDate));
    update.setObject(20, row.notifManualReceiverLimit);

    update.setInt(21, row.id);
  }

  /**
   * Removes a user row.
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeUser(int id) throws AdminPersistenceException {
    UserRow user = getUser(id);
    if (user == null) {
      return;
    }

    SynchroDomainReport.info("UserTable.removeUser()",
        "Suppression de " + user.login + " des groupes dans la base");
    GroupRow[] groups = organization.group.getDirectGroupsOfUser(id);
    for (GroupRow group : groups) {
      organization.group.removeUserFromGroup(id, group.id);
    }

    SynchroDomainReport
        .info("UserTable.removeUser()", "Suppression de " + user.login + " des rôles dans la base");
    UserRoleRow[] roles = organization.userRole.getDirectUserRolesOfUser(id);
    for (UserRoleRow role : roles) {
      organization.userRole.removeUserFromUserRole(id, role.id);
    }

    SynchroDomainReport.info("UserTable.removeUser()",
        "Suppression de " + user.login + " en tant que manager d'espace dans la base");
    SpaceUserRoleRow[] spaceRoles = organization.spaceUserRole.getDirectSpaceUserRolesOfUser(id);
    for (SpaceUserRoleRow spaceRole : spaceRoles) {
      organization.spaceUserRole.removeUserFromSpaceUserRole(id, spaceRole.id);
    }

    SynchroDomainReport
        .info("UserTable.removeUser()", "Delete " + user.login + " from user favorite space table");
    UserFavoriteSpaceService ufsDAO = UserFavoriteSpaceServiceProvider.getUserFavoriteSpaceService();
    if (!ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(id, -1))) {
      throw new AdminPersistenceException("UserTable.removeUser()", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER");
    }

    SynchroDomainReport.debug("UserTable.removeUser()",
        "Suppression de " + user.login + " (ID=" + id + "), requête : " + DELETE_USER);
    // Replace the login by a dummy one that must be unique
    user.login = "???REM???" + java.lang.Integer.toString(id);
    user.specificId = "???REM???" + java.lang.Integer.toString(id);
    if (!UserState.DELETED.name().equals(user.state)) {
      user.state = UserState.DELETED.name();
      user.stateSaveDate = new Date();
    }
    updateRow(UPDATE_USER, user);
  }

  static final private String DELETE_USER = "delete from ST_User where id = ?";

  private static String list2String(List<String> ids) {
    StringBuilder str = new StringBuilder(ids.size() * 3);
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