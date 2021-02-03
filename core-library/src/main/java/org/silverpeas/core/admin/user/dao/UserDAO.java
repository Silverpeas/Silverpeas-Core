/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.silverpeas.core.admin.user.model.UserDetail.BLANK_NAME;

@Repository
public class UserDAO {

  private static final String USER_TABLE = "st_user";
  private static final String GROUP_USER_REL_TABLE = "st_group_user_rel";
  private static final String USER_COLUMNS =
      "DISTINCT(st_user.id),specificId,domainId,login,firstName,lastName,loginMail,email,"
      +    "accessLevel,"
      + "loginQuestion,loginAnswer,creationDate,saveDate,version,tosAcceptanceDate,"
      + "lastLoginDate,nbSuccessfulLoginAttempts,lastLoginCredentialUpdateDate,expirationDate,"
      + "state,stateSaveDate, notifManualReceiverLimit";
  private static final String STATE_CRITERION = "state = ?";
  private static final String ID_CRITERION = "id = ?";
  private static final String DOMAIN_ID_CRITERION = "domainId = ?";
  private static final String ACCESS_LEVEL_CRITERION = "accessLevel = ?";
  private static final String USER_ID_JOINTURE = "id = userId";
  private static final String ACCESS_LEVEL = "accessLevel";
  private static final String LAST_NAME = "lastName";
  private static final String FIRST_NAME = "firstName";
  private static final String DOMAIN_ID = "domainId";
  private static final String SPECIFIC_ID = "specificId";
  private static final String STATE = "state";
  private static final String LOGIN = "login";
  private static final String SAVE_DATE = "saveDate";
  private static final String STATE_SAVE_DATE = "stateSaveDate";

  protected UserDAO() {
  }

  public String saveUser(final Connection connection, final UserDetail user) throws SQLException {
    final int nextId = DBUtil.getNextId(USER_TABLE);
    final Instant now = new Date().toInstant();

    if(UserState.UNKNOWN.equals(user.getState())) {
      user.setState(UserState.VALID);
    }
    JdbcSqlQuery.createInsertFor(USER_TABLE)
        .addInsertParam("id", nextId)
        .addInsertParam(SPECIFIC_ID, user.getSpecificId())
        .addInsertParam(DOMAIN_ID, Integer.parseInt(user.getDomainId()))
        .addInsertParam(LOGIN, user.getLogin())
        .addInsertParam(FIRST_NAME, user.getFirstName())
        .addInsertParam(LAST_NAME, user.getLastName())
        .addInsertParam("loginMail", "")
        .addInsertParam("email", user.geteMail())
        .addInsertParam(ACCESS_LEVEL, user.getAccessLevel().code())
        .addInsertParam("loginQuestion", user.getLoginQuestion())
        .addInsertParam("loginAnswer", user.getLoginAnswer())
        .addInsertParam("creationDate", now)
        .addInsertParam(SAVE_DATE, now)
        .addInsertParam("version", 0)
        .addInsertParam("tosAcceptanceDate", toInstance(user.getTosAcceptanceDate()))
        .addInsertParam("lastLoginDate", toInstance(user.getLastLoginDate()))
        .addInsertParam("nbSuccessfulLoginAttempts", user.getNbSuccessfulLoginAttempts())
        .addInsertParam("lastLoginCredentialUpdateDate",
            toInstance(user.getLastLoginCredentialUpdateDate()))
        .addInsertParam("expirationDate", toInstance(user.getExpirationDate()))
        .addInsertParam(STATE, user.getState())
        .addInsertParam(STATE_SAVE_DATE, now)
        .addInsertParam("notifManualReceiverLimit", user.getNotifManualReceiverLimit())
        .executeWith(connection);

    return String.valueOf(nextId);
  }

  public void restoreUser(final Connection connection, final UserDetail user) throws SQLException {
    Instant now = new Date().toInstant();
    JdbcSqlQuery.createUpdateFor(USER_TABLE)
        .addUpdateParam(STATE, UserState.VALID)
        .addUpdateParam(STATE_SAVE_DATE, now)
        .addUpdateParam(SAVE_DATE, now)
        .where(ID_CRITERION, Integer.parseInt(user.getId()))
        .and(STATE).in(UserState.REMOVED)
        .executeWith(connection);
  }

  public void removeUser(final Connection connection, final UserDetail user) throws SQLException {
    Instant now = new Date().toInstant();
    JdbcSqlQuery.createUpdateFor(USER_TABLE)
        .addUpdateParam(STATE, UserState.REMOVED)
        .addUpdateParam(STATE_SAVE_DATE, now)
        .addUpdateParam(SAVE_DATE, now)
        .where(ID_CRITERION, Integer.parseInt(user.getId()))
        .and(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .executeWith(connection);
  }

  public void deleteUser(final Connection connection, final UserDetail user) throws SQLException {
    Instant now = new Date().toInstant();
    JdbcSqlQuery.createUpdateFor(USER_TABLE)
        .addUpdateParam(LOGIN, "???REM???" + user.getId())
        .addUpdateParam(SPECIFIC_ID, "???REM???" + user.getId())
        .addUpdateParam(STATE, UserState.DELETED)
        .addUpdateParam(STATE_SAVE_DATE, now)
        .addUpdateParam(SAVE_DATE, now)
        .where(ID_CRITERION, Integer.parseInt(user.getId()))
        .executeWith(connection);
  }

  /**
   * Gets the user with the specified unique identifier.
   * @param connection the connection to the data source to use
   * @param id the unique identifier of the user to get.
   * @return the user or null if no such user exist.
   * @throws SQLException if an error occurs while getting the user detail from the data source.
   */
  public UserDetail getUserById(final Connection connection, final String id) throws SQLException {
    return JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(ID_CRITERION, Integer.parseInt(id))
        .executeUniqueWith(connection, UserDAO::fetchUser);
  }

  public boolean isUserByIdExists(final Connection connection, final String id)
      throws SQLException {
    return JdbcSqlQuery.createSelect("COUNT(id)")
        .from(USER_TABLE)
        .where(ID_CRITERION, Integer.parseInt(id))
        .and(STATE).notIn(UserState.DELETED)
        .executeUniqueWith(connection, row -> row.getInt(1)) == 1;
  }

  public UserDetail getUserBySpecificId(final Connection connection, final String domainId,
      final String specificId) throws SQLException {
    return JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("specificId = ?", specificId)
        .executeUniqueWith(connection, UserDAO::fetchUser);
  }

  public List<UserDetail> getUsersBySpecificIds(final Connection connection, final String domainId,
      final List<String> specificIds) throws SQLException {
    return JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and(SPECIFIC_ID).in(specificIds)
        .executeWith(connection, UserDAO::fetchUser);
  }

  /**
   * Gets all the users that were removed in the specified domains.
   * @param connection a connection to the data source.
   * @param domainIds zero, one or more unique identifiers of Silverpeas domains. If no domains
   * are passed, then all the domains are taken by the request.
   * @return a list of user details.
   * @throws SQLException if an error while requesting the users.
   */
  public List<UserDetail> getRemovedUsers(final Connection connection, final String... domainIds)
      throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(domainIds);
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(STATE_CRITERION, UserState.REMOVED);
    final List<Integer> requestedDomainIds =
        Stream.of(domainIds).map(Integer::parseInt).collect(Collectors.toList());
    if (!requestedDomainIds.isEmpty()) {
      query.and(DOMAIN_ID).in(requestedDomainIds);
    }
    return query.executeWith(connection, UserDAO::fetchUser);
  }

  /**
   * Gets all the users that were deleted in the specified domains and that weren't yet blanked.
   * @param connection a connection to the data source.
   * @param domainIds zero, one or more unique identifiers of Silverpeas domains. If no domains
   * are passed, then all the domains are taken by the request.
   * @return a list of user details.
   * @throws SQLException if an error while requesting the users.
   */
  public List<UserDetail> getNonBlankedDeletedUsers(final Connection connection, final String... domainIds)
      throws SQLException {
    Objects.requireNonNull(connection);
    Objects.requireNonNull(domainIds);
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(STATE_CRITERION, UserState.DELETED)
        .and("firstName <> ?", BLANK_NAME);
    final List<Integer> requestedDomainIds =
        Stream.of(domainIds).map(Integer::parseInt).collect(Collectors.toList());
    if (!requestedDomainIds.isEmpty()) {
      query.and(DOMAIN_ID).in(requestedDomainIds);
    }
    return query.executeWith(connection, UserDAO::fetchUser);
  }

  public String getUserIdByLoginAndDomain(final Connection connection, final String login,
      final String domainId) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and("lower(login) = lower(?)", login)
        .and(STATE).notIn(UserState.DELETED)
        .executeUniqueWith(connection, r -> Integer.toString(r.getInt(1)));
  }

  public boolean isUserEmailExisting(final Connection connection, final String email)
      throws SQLException {
    return JdbcSqlQuery.createSelect("COUNT(id)")
        .from(USER_TABLE)
        .where("email = ?", email)
        .executeUniqueWith(connection, row -> row.getInt(1)) > 1;
  }

  /**
   * Get all the domains id in which a user with the specified login exists.
   *
   * @param connection the connection with the data source to use.
   * @param login the login for which we want the domains.
   * @return a list of domain ids.
   * @throws SQLException if an error occurs while getting the user details from the data source.
   */
  public List<String> getDomainsContainingLogin(Connection connection, String login) throws
      SQLException {
    return JdbcSqlQuery.createSelect("DISTINCT(domainId) AS domain")
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.DELETED)
        .and("login = ?", login)
        .orderBy(DOMAIN_ID)
        .executeWith(connection, row -> row.getString("domain"));
  }

  /**
   * Updates the specified user by blanking some of its profile information.
   * @param connection a connection to the data source.
   * @param user the user to blank.
   * @throws SQLException if the update fails.
   */
  public void blankUser(final Connection connection, final UserDetail user) throws SQLException {
    user.setFirstName(BLANK_NAME);
    user.setLastName("");
    user.seteMail("");
    updateUser(connection, user);
  }

  /**
   * Updates into the data source the specified user.
   * @param connection a connection to the data source.
   * @param user the user to update.
   * @throws SQLException if the update fails.
   */
  public void updateUser(final Connection connection, final UserDetail user) throws SQLException {
    Instant now = new Date().toInstant();
    String firstName = user.isBlanked() ? BLANK_NAME : user.getFirstName();
    JdbcSqlQuery.createUpdateFor(USER_TABLE)
        .addUpdateParam(SPECIFIC_ID, user.getSpecificId())
        .addUpdateParam(DOMAIN_ID, Integer.parseInt(user.getDomainId()))
        .addUpdateParam(LOGIN, user.getLogin())
        .addUpdateParam(FIRST_NAME, firstName)
        .addUpdateParam(LAST_NAME, user.getLastName())
        .addUpdateParam("email", user.geteMail())
        .addUpdateParam(ACCESS_LEVEL, user.getAccessLevel().code())
        .addUpdateParam("loginQuestion", user.getLoginQuestion())
        .addUpdateParam("loginAnswer", user.getLoginAnswer())
        .addUpdateParam(SAVE_DATE, now)
        .addUpdateParam("version", user.getVersion() + 1)
        .addUpdateParam("tosAcceptanceDate", toInstance(user.getTosAcceptanceDate()))
        .addUpdateParam("lastLoginDate", toInstance(user.getLastLoginDate()))
        .addUpdateParam("nbSuccessfulLoginAttempts", user.getNbSuccessfulLoginAttempts())
        .addUpdateParam("lastLoginCredentialUpdateDate",
            toInstance(user.getLastLoginCredentialUpdateDate()))
        .addUpdateParam("expirationDate", toInstance(user.getExpirationDate()))
        .addUpdateParam(STATE, user.getState())
        .addUpdateParam(STATE_SAVE_DATE, toInstance(user.getStateSaveDate()))
        .addUpdateParam("notifManualReceiverLimit", user.getNotifManualReceiverLimit())
        .where(ID_CRITERION, Integer.parseInt(user.getId()))
        .executeWith(connection);
  }

  /**
   * Gets the user details that match the specified criteria. The criteria are provided by an
   * UserSearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connection with a data source to use.
   * @param criteria the criteria to apply on the users to get.
   * @return a list of user details matching the criteria or an empty list if no such user details
   * are found.
   */
  public ListSlice<UserDetail> getUsersByCriteria(Connection connection,
      UserDetailsSearchCriteria criteria) throws SQLException {
    SqlUserSelectorByCriteriaBuilder builder = new SqlUserSelectorByCriteriaBuilder(USER_COLUMNS);
    return builder.build(criteria).executeWith(connection, UserDAO::fetchUser);
  }

  /**
   * Gets the number of users that match the specified criteria. The criteria are provided by an
   * UserSearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connexion with a data source to use.
   * @param criteria criteria the criteria to apply on the users to get.
   * @return the number of users that match the specified criteria.
   */
  public int getUserCountByCriteria(Connection connection, UserDetailsSearchCriteria criteria)
      throws SQLException {
    final SqlUserSelectorByCriteriaBuilder builder =
        new SqlUserSelectorByCriteriaBuilder("COUNT(DISTINCT st_user.id)");
    return builder.build(criteria).executeUniqueWith(connection, row -> row.getInt(1));
  }

  public List<UserDetail> getUsersInGroups(Connection con, List<String> groupIds)
      throws SQLException {
    List<Integer> groupIdsAsInt =
        groupIds.stream().map(Integer::parseInt).collect(Collectors.toList());
    return JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE, GROUP_USER_REL_TABLE)
        .where(USER_ID_JOINTURE).and("groupid").in(groupIdsAsInt)
        .and(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .orderBy(LAST_NAME)
        .executeWith(con, UserDAO::fetchUser);
  }

  public List<String> getAllUserIds(Connection connection) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getAllAdminIds(Connection connection, final UserDetail fromUser)
      throws SQLException {
    JdbcSqlQuery query = JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(STATE)
        .notIn(UserState.REMOVED, UserState.DELETED);
    query.and(ACCESS_LEVEL_CRITERION, UserAccessLevel.ADMINISTRATOR.code());
    if (!fromUser.isAccessAdmin() && !fromUser.isAccessDomainManager()) {
      query.or("(accessLevel = ? and domainId = ?)", UserAccessLevel.DOMAIN_ADMINISTRATOR.code(),
          Integer.parseInt(fromUser.getDomainId()));
    }
    return query.orderBy(LAST_NAME).executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getDirectUserIdsInGroup(Connection connection, final String groupId,
      final boolean includeRemoved)
      throws SQLException {
    return getDirectUserIdsByGroup(connection, singletonList(groupId), includeRemoved)
        .getOrDefault(groupId, emptyList());
  }

  public Map<String, List<String>> getDirectUserIdsByGroup(Connection connection, final List<String> groupIds,
      final boolean includeRemoved)
      throws SQLException {
    final Object[] userStatesToExclude = includeRemoved
        ? new UserState[]{UserState.DELETED}
        : new UserState[]{UserState.REMOVED, UserState.DELETED};
    final Map<String, List<String>> result = new HashMap<>(groupIds.size());
    if (!groupIds.isEmpty()) {
      JdbcSqlQuery.createSelect("groupid, userid")
          .from(USER_TABLE, GROUP_USER_REL_TABLE)
          .where(USER_ID_JOINTURE)
          .and("groupId").in(groupIds.stream().map(Integer::parseInt).collect(Collectors.toList()))
          .and(STATE).notIn(userStatesToExclude)
          .orderBy(LAST_NAME)
          .executeWith(connection, row -> MapUtil.putAddList(result, Integer.toString(row.getInt(1)), Integer.toString(row.getInt(2))));
    }
    return result;
  }

  public List<String> getUserIdsInGroups(Connection con, List<String> groupIds)
      throws SQLException {
    List<Integer> groupIdsAsInt =
        groupIds.stream().map(Integer::parseInt).collect(Collectors.toList());
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE, GROUP_USER_REL_TABLE)
        .where(USER_ID_JOINTURE).and("groupid").in(groupIdsAsInt)
        .and(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .orderBy(LAST_NAME)
        .executeWith(con, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getUserIdsInDomain(Connection connection, final String domainId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .and(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getUserIdsByAccessLevel(final Connection connection,
      final UserAccessLevel accessLevel) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .and(ACCESS_LEVEL_CRITERION, accessLevel.code())
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getUserIdsByAccessLevelInDomain(Connection connection,
      final UserAccessLevel accessLevel, final String domainId) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .and(DOMAIN_ID_CRITERION, Integer.parseInt(domainId))
        .and(ACCESS_LEVEL_CRITERION, accessLevel.code())
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getUserIdsByUserRole(final Connection connection, final String userRoleId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE, "ST_UserRole_User_Rel")
        .where(USER_ID_JOINTURE)
        .and("userRoleId = ?", Integer.parseInt(userRoleId))
        .and(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  /**
   * Returns all the User ids having directly a given space userRole.
   * @param spaceUserRoleId the unique identifier of the space user role.
   * @return all the User ids having directly a given space userRole.
   * @throws SQLException if an error occurs.
   */
  public List<String> getUserIdsBySpaceUserRole(final Connection connection,
      final String spaceUserRoleId) throws SQLException {
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE, "ST_SpaceUserRole_User_Rel")
        .where(USER_ID_JOINTURE)
        .and("spaceUserRoleId = ?", Integer.parseInt(spaceUserRoleId))
        .and(STATE).notIn(UserState.REMOVED, UserState.DELETED)
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<String> getDirectUserIdsByGroupUserRole(final Connection connection,
      final String groupUserRoleId, final boolean includeRemoved) throws SQLException {
    final Object[] userStatesToExclude = includeRemoved
        ? new UserState[]{UserState.DELETED}
        : new UserState[]{UserState.REMOVED, UserState.DELETED};
    return JdbcSqlQuery.createSelect("id")
        .from(USER_TABLE, "ST_GroupUserRole_User_Rel")
        .where(USER_ID_JOINTURE)
        .and("groupUserRoleId = ?", Integer.parseInt(groupUserRoleId))
        .and(STATE).notIn(userStatesToExclude)
        .orderBy(LAST_NAME)
        .executeWith(connection, row -> Integer.toString(row.getInt(1)));
  }

  public List<UserDetail> getAllUsers(Connection con) throws SQLException {
    return getAllUsers(con, null, null);
  }

  public List<UserDetail> getAllUsersFromNewestToOldest(Connection con) throws SQLException {
    return getAllUsers(con, null, "id DESC");
  }

  private List<UserDetail> getAllUsers(Connection con, List<String> domainIds, String orderBy)
      throws SQLException {
    final String order = StringUtil.isDefined(orderBy) ? orderBy : LAST_NAME;
    JdbcSqlQuery query = JdbcSqlQuery.createSelect(USER_COLUMNS)
        .from(USER_TABLE)
        .where(STATE).notIn(UserState.REMOVED, UserState.DELETED);
    if (domainIds != null && !domainIds.isEmpty()) {
      final List<Integer> domainIdsAsInt =
          domainIds.stream().map(Integer::parseInt).collect(Collectors.toList());
      query.and(DOMAIN_ID).in(domainIdsAsInt);
    }
    return query.orderBy(order).executeWith(con, UserDAO::fetchUser);
  }

  public List<UserDetail> getUsersOfDomains(Connection con, List<String> domainIds) throws
      SQLException {
    return getAllUsers(con, domainIds, null);
  }

  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(Connection con, List<String> domainIds)
      throws SQLException {
    return getAllUsers(con, domainIds, "id DESC");
  }

  /**
   * Fetch the current user row from a resultSet.
   */
  private static UserDetail fetchUser(ResultSet rs) throws SQLException {
    UserDetail u = new UserDetail();
    u.setId(Integer.toString(rs.getInt(1)));
    u.setSpecificId(rs.getString(2));
    u.setDomainId(Integer.toString(rs.getInt(3)));
    u.setLogin(rs.getString(4));
    u.setFirstName(rs.getString(5));
    u.setLastName(rs.getString(6));
    u.seteMail(rs.getString(8));
    u.setAccessLevel(UserAccessLevel.fromCode(rs.getString(9)));
    u.setLoginQuestion(rs.getString(10));
    u.setLoginAnswer(rs.getString(11));
    u.setCreationDate(rs.getTimestamp(12));
    u.setSaveDate(rs.getTimestamp(13));
    u.setVersion(rs.getInt(14));
    u.setTosAcceptanceDate(rs.getTimestamp(15));
    u.setLastLoginDate(rs.getTimestamp(16));
    u.setNbSuccessfulLoginAttempts(rs.getInt(17));
    u.setLastLoginCredentialUpdateDate(rs.getTimestamp(18));
    u.setExpirationDate(rs.getTimestamp(19));
    u.setState(UserState.from(rs.getString(20)));
    u.setStateSaveDate(rs.getTimestamp(21));
    if (StringUtil.isInteger(rs.getString(22))) {
      u.setNotifManualReceiverLimit(rs.getInt(22));
    }
    return u;
  }

  private Instant toInstance(final Date aDate) {
    return aDate == null ? null : aDate.toInstant();
  }
}