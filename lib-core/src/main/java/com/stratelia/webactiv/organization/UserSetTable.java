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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import java.sql.Types;
import java.util.ArrayList;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A UserSetRelation object manages the UserSet Table and the UserSet-UserSet and UserSet-User
 * relations.
 */
public class UserSetTable extends Table {
  public UserSetTable(OrganizationSchema organization) {
    super(organization, "ST_UserSet");
    this.organization = organization;
  }

  /**
   * Fetch the current usetSet row from a resultSet.
   */
  protected UserSetRow fetchUserSet(ResultSet rs) throws SQLException {
    UserSetRow us = new UserSetRow();

    us.userSetType = rs.getString(1);
    us.userSetId = rs.getInt(2);

    return us;
  }

  /**
   * Delete all usersets and relations
   */
  public void resetAll() throws AdminPersistenceException {
    updateRelation(RESET_ALL_USERSET_USERSET_REL);
    updateRelation(RESET_ALL_USERSET_USER_REL);
    updateRelation(RESET_ALL_USERSET);
  }

  static final private String RESET_ALL_USERSET = "delete from ST_UserSet";

  static final private String RESET_ALL_USERSET_USERSET_REL = "delete from ST_UserSet_UserSet_Rel";

  static final private String RESET_ALL_USERSET_USER_REL = "delete from ST_UserSet_User_Rel";

  /**
   * Returns all the super set of a given sub set
   */
  public UserSetRow[] getSuperUserSet(String subSetType, int subSetId)
      throws AdminPersistenceException {
    String[] types = new String[] { subSetType };
    int[] ids = new int[] { subSetId };

    return (UserSetRow[]) getRows(SELECT_SUPER_USERSET, types, ids).toArray(
        new UserSetRow[0]);
  }

  static final private String SELECT_SUPER_USERSET = "select superSetType, superSetId"
      + " from ST_UserSet_UserSet_Rel" + " where subSetType=? and subSetId=?";

  /**
   * Returns all the sub set of a given super set
   */
  public UserSetRow[] getSubUserSet(String superSetType, int superSetId)
      throws AdminPersistenceException {
    String[] types = new String[] { superSetType };
    int[] ids = new int[] { superSetId };

    return (UserSetRow[]) getRows(SELECT_SUB_USERSET, types, ids).toArray(
        new UserSetRow[0]);
  }

  static final private String SELECT_SUB_USERSET = "select subSetType, subSetId"
      + " from ST_UserSet_UserSet_Rel"
      + " where superSetType=? and superSetId=?";

  /**
   * Returns all the sub user of the subsets of given super set
   */
  public int getSubUserNumber(String superSetType, int superSetId)
      throws AdminPersistenceException {
    return getCount("ST_UserSet_User_Rel", "userSetId", WHERE_SUB_USER_NUMBER,
        superSetId, superSetType);
  }

  static final private String WHERE_SUB_USER_NUMBER = "userSetId=? and userSetType=?";

  /**
   * Returns all the sub user of the subsets of given super set
   */
  public UserSetRow[] getSubUser(String superSetType, int superSetId)
      throws AdminPersistenceException {
    String[] types = new String[] { superSetType };
    int[] ids = new int[] { superSetId };

    ArrayList directSubUsers = getRows(SELECT_DIRECT_SUB_USER, types, ids);

    return (UserSetRow[]) directSubUsers.toArray(new UserSetRow[0]);
  }

  static final private String SELECT_DIRECT_SUB_USER = "select userSetType, userId"
      + " from ST_UserSet_User_Rel" + " where userSetType=? and userSetId=?";

  /**
   * Returns the links count between a userSet and a user.
   */
  public int getLinksCount(String userSetType, int userSetId, int userId)
      throws AdminPersistenceException {
    String[] types = new String[] { userSetType };
    int[] ids = new int[] { userSetId, userId };

    Integer result = getInteger(SELECT_COUNT_USERSET_USER_REL, types, ids);
    if (result == null)
      return 0;
    else
      return result.intValue();
  }

  static final private String SELECT_COUNT_USERSET_USER_REL = "select linksCount"
      + " from ST_UserSet_User_Rel"
      + " where userSetType=? and userSetId=? and userId=?";

  /**
   * Returns the links count between two userSet.
   */
  public int getLinksCount(String superSetType, int superSetId,
      String subSetType, int subSetId) throws AdminPersistenceException {
    String[] types = new String[] { superSetType, subSetType };
    int[] ids = new int[] { superSetId, subSetId };

    Integer result = getInteger(SELECT_COUNT_USERSET_USERSET_REL, types, ids);
    if (result == null)
      return 0;
    else
      return result.intValue();
  }

  static final private String SELECT_COUNT_USERSET_USERSET_REL = "select linksCount"
      + " from ST_UserSet_UserSet_Rel"
      + " where superSetType=? and subSetType=?"
      + " and   superSetId=?   and subSetId=?";

  /**
   * Returns true if given userType already exists
   */
  public boolean isUserSetExists(String userSetType, int userSetId)
      throws AdminPersistenceException {
    String[] types = new String[] { userSetType };
    int[] ids = new int[] { userSetId };

    return getRows(SELECT_USERSET, types, ids).size() >= 1;
  }

  static final private String SELECT_USERSET =
      "select * from ST_UserSet where userSetType=? and userSetId=?";

  /**
   * Inserts in the database a new userset row.
   */
  public void createUserSet(String type, int id)
      throws AdminPersistenceException {
    if (!isUserSetExists(type, id)) {
      UserSetRow us = new UserSetRow();
      us.userSetType = type;
      us.userSetId = id;
      SynchroReport.debug("UserSetTable.createUserSet()",
          "Ajout de l'objet d'Id " + id + " et de type " + type
          + ", requête : " + INSERT_USERSET, null);
      insertRow(INSERT_USERSET, us);
    }
  }

  static final private String INSERT_USERSET =
      "insert into ST_UserSet(userSetType,userSetId) values (?,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    UserSetRow us = (UserSetRow) row;

    insert.setString(1, us.userSetType);
    insert.setInt(2, us.userSetId);
  }

  /**
   * Delete the userset and all his relations.
   */
  public void removeUserSet(String type, int id)
      throws AdminPersistenceException {
    String[] types = new String[] { type };
    int[] ids = new int[] { id };
    SynchroReport
        .debug(
        "UserSetTable.removeUserSet()",
        "Retrait de l'objet d'ID "
        + id
        + " et de type "
        + type
        + " partout dans les tables ST_UserSet_UserSet_Rel, ST_UserSet_User_Rel et ST_UserSet ",
        null);
    updateRelation(DELETE_SUPER_USERSET_REL, types, ids);
    updateRelation(DELETE_SUB_USERSET_REL, types, ids);
    updateRelation(DELETE_USERSET_USER_REL, types, ids);
    updateRelation(DELETE_USERSET, types, ids);
  }

  static final private String DELETE_USERSET =
      "delete from ST_UserSet where userSetType=? and userSetId = ?";

  static final private String DELETE_USERSET_USER_REL = "delete from ST_UserSet_User_Rel"
      + " where userSetType=? and userSetId = ?";

  static final private String DELETE_SUB_USERSET_REL = "delete from ST_UserSet_UserSet_Rel"
      + " where subSetType=? and subSetId = ?";

  static final private String DELETE_SUPER_USERSET_REL = "delete from ST_UserSet_UserSet_Rel"
      + " where superSetType=? and superSetId = ?";

  /**
   * Adds a user in a userSet.
   */
  public void addUserInUserSet(int userId, String userSetType, int userSetId)
      throws AdminPersistenceException {
    UserSetRow[] superSets = getSuperUserSet(userSetType, userSetId);
    int weight = 0;

    linkUserSetUser(userSetType, userSetId, userId, 1);

    if (!userSetType.equals("H")) {
      for (int i = 0; i < superSets.length; i++) {
        weight = getLinksCount(superSets[i].userSetType,
            superSets[i].userSetId, userSetType, userSetId);
        linkUserSetUser(superSets[i].userSetType, superSets[i].userSetId,
            userId, weight);
      }
    }
  }

  protected void linkUserSetUser(String userSetType, int userSetId, int userId,
      int weight) throws AdminPersistenceException {
    int count = getLinksCount(userSetType, userSetId, userId);
    String[] types = new String[] { userSetType };
    int[] ids = new int[] { userSetId, userId };
    if (count == 0) {
      SynchroReport.debug("UserSetTable.linkUserSetUser()",
          "Ajout d'une relation entre l'utilisateur d'ID " + userId
          + " et l'objet de type " + userSetType + " et d'ID " + userSetId
          + ", requête : " + INSERT_USERSET_USER_REL, null);

      if (StringUtil.isDefined(userSetType)
          && userSetType.equalsIgnoreCase("R"))
        createUserSet(userSetType, userSetId);
      updateRelation(INSERT_USERSET_USER_REL, types, ids, weight);
    } else {
      SynchroReport.debug("UserSetTable.linkUserSetUser()",
          "Maj de la relation entre l'utilisateur d'ID " + userId
          + " et l'objet de type " + userSetType + " et d'ID " + userSetId
          + ", requête : " + INCREMENT_USERSET_USER_REL, null);
      updateRelation(INCREMENT_USERSET_USER_REL, types, ids, weight);
    }
  }

  static final private String INSERT_USERSET_USER_REL = "insert into ST_UserSet_User_Rel"
      + " (linksCount, userSetType, userSetId, userId)"
      + "values"
      + " (?          , ?        , ?     , ?)";

  static final private String INCREMENT_USERSET_USER_REL = "update ST_UserSet_User_Rel"
      + " set linksCount = linksCount + ?"
      + " where userSetType = ?"
      + " and   userSetId = ?" + " and   userId = ?";

  /**
   * Removes user from a userSet.
   */
  public void removeUserFromUserSet(int userId, String userSetType,
      int userSetId) throws AdminPersistenceException {
    int weight = 0;
    int count = getLinksCount(userSetType, userSetId, userId);
    if (count == 0)
      return;

    UserSetRow[] superSets = getSuperUserSet(userSetType, userSetId);

    unlinkUserSetUser(userSetType, userSetId, userId, 1);
    for (int i = 0; i < superSets.length; i++) {
      weight = getLinksCount(superSets[i].userSetType, superSets[i].userSetId,
          userSetType, userSetId);
      unlinkUserSetUser(superSets[i].userSetType, superSets[i].userSetId,
          userId, weight);
    }
  }

  protected void unlinkUserSetUser(String userSetType, int userSetId,
      int userId, int weight) throws AdminPersistenceException {
    int count = getLinksCount(userSetType, userSetId, userId);

    String[] types = new String[] { userSetType };
    int[] ids = new int[] { userSetId, userId };
    if (count == weight) {
      SynchroReport.debug("UserSetTable.unlinkUserSetUser()",
          "Suppression de la relation entre l'utilisateur d'ID " + userId
          + " et l'objet de type " + userSetType + " et d'ID " + userSetId
          + ", requête : " + DELETE_A_USERSET_USER_REL, null);
      updateRelation(DELETE_A_USERSET_USER_REL, types, ids);
    } else {
      SynchroReport.debug("UserSetTable.unlinkUserSetUser()",
          "Maj de la relation entre l'utilisateur d'ID " + userId
          + " et l'objet de type " + userSetType + " et d'ID " + userSetId
          + ", requête : " + DECREMENT_USERSET_USER_REL, null);
      updateRelation(DECREMENT_USERSET_USER_REL, types, ids, weight);
    }
  }

  static final private String DELETE_A_USERSET_USER_REL = "delete from ST_UserSet_User_Rel"
      + " where userSetType = ?" + " and   userSetId = ?" + " and   userId = ?";

  static final private String DECREMENT_USERSET_USER_REL = "update ST_UserSet_User_Rel"
      + " set linksCount = linksCount - ?"
      + " where userSetType = ?"
      + " and   userSetId = ?" + " and   userId = ?";

  /**
   * Adds a userSet in a userSet.
   * <P>
   * To insert a SUBSET in a SUPERSET, we must :
   * <UL>
   * <LI>link SUPERSET to SUBSET
   * <LI>link all the super sets of SUPERSET to SUBSET
   * <LI>link SUPERSET to all the sub sets of SUBSET
   * <LI>link all the super sets of SUPERSET to all the sub sets of SUBSET
   * </UL>
   */
  public void addUserSetInUserSet(String subSetType, int subSetId,
      String superSetType, int superSetId) throws AdminPersistenceException {
    // link SUPERSET to SUBSET
    linkUserSetUserSet(superSetType, superSetId, subSetType, subSetId, 1);

    if (!subSetType.equals(UserSetRow.GROUP_MANAGER)
        && !superSetType.equals(ObjectType.OBJECT)
        && !superSetType.equals(ObjectType.DOCUMENT)) {
      UserSetRow[] supersuperSets = getSuperUserSet(superSetType, superSetId);
      UserSetRow[] subsubSets = getSubUserSet(subSetType, subSetId);
      UserSetRow[] subsubUsers = getSubUser(subSetType, subSetId);

      int weight = 0;

      if (!superSetType.equals("H")) {
        // link all the super sets of SUPERSET to SUBSET
        for (int i = 0; i < supersuperSets.length; i++) {
          weight = getLinksCount(supersuperSets[i].userSetType,
              supersuperSets[i].userSetId, superSetType, superSetId);

          linkUserSetUserSet(supersuperSets[i].userSetType,
              supersuperSets[i].userSetId, subSetType, subSetId, weight);
        }

        // link SUPERSET to all the sub sets of SUBSET
        for (int j = 0; j < subsubSets.length; j++) {
          weight = getLinksCount(subSetType, subSetId,
              subsubSets[j].userSetType, subsubSets[j].userSetId);
          linkUserSetUserSet(superSetType, superSetId,
              subsubSets[j].userSetType, subsubSets[j].userSetId, weight);
        }
      }

      // link SUPERSET to all the sub users of SUBSET
      for (int j = 0; j < subsubUsers.length; j++) {
        weight = getLinksCount(subSetType, subSetId, subsubUsers[j].userSetId);
        linkUserSetUser(superSetType, superSetId, subsubUsers[j].userSetId,
            weight);
      }

      if (!superSetType.equals("H")) {
        // link all the super sets of SUPERSET to all the sub sets of SUBSET
        // and all the super sets of SUPERSET to all the sub users of SUBSET
        int weights = 0;
        for (int i = 0; i < supersuperSets.length; i++) {
          weight = getLinksCount(supersuperSets[i].userSetType,
              supersuperSets[i].userSetId, superSetType, superSetId);

          for (int j = 0; j < subsubSets.length; j++) {
            weights = weight
                * getLinksCount(subSetType, subSetId,
                subsubSets[j].userSetType, subsubSets[j].userSetId);
            linkUserSetUserSet(supersuperSets[i].userSetType,
                supersuperSets[i].userSetId, subsubSets[j].userSetType,
                subsubSets[j].userSetId, weights);
          }

          for (int j = 0; j < subsubUsers.length; j++) {
            weights = weight
                * getLinksCount(subSetType, subSetId, subsubUsers[j].userSetId);
            linkUserSetUser(supersuperSets[i].userSetType,
                supersuperSets[i].userSetId, subsubUsers[j].userSetId, weights);
          }
        }
      }
    }
  }

  protected void linkUserSetUserSet(String superSetType, int superSetId,
      String subSetType, int subSetId, int weight)
      throws AdminPersistenceException {
    SilverTrace.info("admin", "UserSetTable.linkUserSetUserSet",
        "root.MSG_GEN_ENTER_METHOD", "subSetType = " + subSetType
        + ", subSetId = " + subSetId + ", superSetType = " + superSetType
        + ", superSetId = " + superSetId);
    if (!subSetType.equals("H")) {
      int count = getLinksCount(superSetType, superSetId, subSetType, subSetId);

      String[] types = new String[] { superSetType, subSetType };
      int[] ids = new int[] { superSetId, subSetId };
      if (count == 0) {
        SynchroReport.debug("UserSetTable.linkUserSetUserSet()",
            "Ajout d'une relation entre l'objet d'ID " + superSetId
            + " et de type " + superSetType + " et l'objet d'ID "
            + subSetId + " et de type " + subSetType + ", requête : "
            + INSERT_USERSET_USERSET_REL, null);
        if (StringUtil.isDefined(superSetType)
            && superSetType.equalsIgnoreCase("R"))
          createUserSet(superSetType, superSetId);
        updateRelation(INSERT_USERSET_USERSET_REL, types, ids, weight);
      } else {
        SynchroReport.debug("UserSetTable.linkUserSetUserSet()",
            "Maj de la relation entre l'objet d'ID " + superSetId
            + " et de type " + superSetType + " et l'objet d'ID "
            + subSetId + " et de type " + subSetType + ", requête : "
            + INCREMENT_USERSET_USERSET_REL, null);
        updateRelation(INCREMENT_USERSET_USERSET_REL, types, ids, weight);
      }
    }
  }

  static final private String INSERT_USERSET_USERSET_REL = "insert into ST_UserSet_UserSet_Rel"
      + " (linksCount, superSetType, subSetType, superSetId, subSetId)"
      + "values" + " (?           , ?         , ?         , ?       , ?)";

  static final private String INCREMENT_USERSET_USERSET_REL = "update ST_UserSet_UserSet_Rel"
      + " set linksCount = linksCount + ?"
      + " where superSetType = ?"
      + " and   subSetType = ?"
      + " and   superSetId = ?"
      + " and   subSetId = ?";

  /**
   * Removes a userSet from a userSet.
   * <P>
   * To remove a SUBSET from a SUPERSET, we must :
   * <UL>
   * <LI>test if SUPERSET and SUBSET are linked
   * <LI>unlink SUPERSET from SUBSET
   * <LI>unlink all the super sets of SUPERSET from SUBSET
   * <LI>unlink SUPERSET from all the sub sets of SUBSET
   * <LI>unlink all the super sets of SUPERSET from all the sub sets of SUBSET
   * </UL>
   */
  public void removeUserSetFromUserSet(String subSetType, int subSetId,
      String superSetType, int superSetId) throws AdminPersistenceException {
    UserSetRow[] supersuperSets = getSuperUserSet(superSetType, superSetId);
    UserSetRow[] subsubSets = getSubUserSet(subSetType, subSetId);
    UserSetRow[] subsubUsers = getSubUser(subSetType, subSetId);

    int weight = 0;

    // link SUPERSET to SUBSET
    unlinkUserSetUserSet(superSetType, superSetId, subSetType, subSetId, 1);

    // link all the super sets of SUPERSET to SUBSET
    for (int i = 0; i < supersuperSets.length; i++) {
      weight = getLinksCount(supersuperSets[i].userSetType,
          supersuperSets[i].userSetId, superSetType, superSetId);

      unlinkUserSetUserSet(supersuperSets[i].userSetType,
          supersuperSets[i].userSetId, subSetType, subSetId, weight);
    }

    // link SUPERSET to all the sub sets of SUBSET
    for (int j = 0; j < subsubSets.length; j++) {
      weight = getLinksCount(subSetType, subSetId, subsubSets[j].userSetType,
          subsubSets[j].userSetId);
      unlinkUserSetUserSet(superSetType, superSetId, subsubSets[j].userSetType,
          subsubSets[j].userSetId, weight);
    }

    // link SUPERSET to all the sub users of SUBSET
    for (int j = 0; j < subsubUsers.length; j++) {
      weight = getLinksCount(subSetType, subSetId, subsubUsers[j].userSetId);
      unlinkUserSetUser(superSetType, superSetId, subsubUsers[j].userSetId,
          weight);
    }

    // link all the super sets of SUPERSET to all the sub sets of SUBSET
    // and all the super sets of SUPERSET to all the sub users of SUBSET
    int weights = 0;
    for (int i = 0; i < supersuperSets.length; i++) {
      weight = getLinksCount(supersuperSets[i].userSetType,
          supersuperSets[i].userSetId, superSetType, superSetId);

      for (int j = 0; j < subsubSets.length; j++) {
        weights = weight
            * getLinksCount(subSetType, subSetId, subsubSets[j].userSetType,
            subsubSets[j].userSetId);
        unlinkUserSetUserSet(supersuperSets[i].userSetType,
            supersuperSets[i].userSetId, subsubSets[j].userSetType,
            subsubSets[j].userSetId, weights);
      }

      for (int j = 0; j < subsubUsers.length; j++) {
        weights = weight
            * getLinksCount(subSetType, subSetId, subsubUsers[j].userSetId);
        unlinkUserSetUser(supersuperSets[i].userSetType,
            supersuperSets[i].userSetId, subsubUsers[j].userSetId, weights);
      }
    }
  }

  protected void unlinkUserSetUserSet(String superSetType, int superSetId,
      String subSetType, int subSetId, int weight)
      throws AdminPersistenceException {
    int count = getLinksCount(superSetType, superSetId, subSetType, subSetId);

    String[] types = new String[] { superSetType, subSetType };
    int[] ids = new int[] { superSetId, subSetId };
    if (count == weight) {
      SynchroReport.debug("UserSetTable.unlinkUserSetUserSet()",
          "Suppression de la relation entre l'objet d'ID " + superSetId
          + " et de type " + superSetType + " et l'objet d'ID " + subSetId
          + " et de type " + subSetType + ", requête : "
          + DELETE_A_USERSET_USERSET_REL, null);
      updateRelation(DELETE_A_USERSET_USERSET_REL, types, ids);
    } else {
      SynchroReport.debug("UserSetTable.unlinkUserSetUserSet()",
          "Maj de la relation entre l'objet d'ID " + superSetId
          + " et de type " + superSetType + " et l'objet d'ID " + subSetId
          + " et de type " + subSetType + ", requête : "
          + DECREMENT_USERSET_USERSET_REL, null);
      updateRelation(DECREMENT_USERSET_USERSET_REL, types, ids, weight);
    }
  }

  static final private String DELETE_A_USERSET_USERSET_REL = "delete from ST_UserSet_UserSet_Rel"
      + " where superSetType = ?"
      + " and   subSetType = ?"
      + " and   superSetId = ?" + " and   subSetId = ?";

  static final private String DECREMENT_USERSET_USERSET_REL = "update ST_UserSet_UserSet_Rel"
      + " set linksCount = linksCount - ?"
      + " where superSetType = ?"
      + " and   subSetType = ?"
      + " and   superSetId = ?"
      + " and   subSetId = ?";

  /**
   * Fetch the current space row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchUserSet(rs);
  }

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    // A UserSet is never updated.
  }

  /**
   * Returns the rows described by the given query with (type,id) parameters.
   */
  protected ArrayList getRows(String query, String[] types, int[] ids)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "UserSetTable.getRows", "root.MSG_QUERY",
          "Ln 605");
      select = organization.getStatement(query);
      // synchronized (select)
      // {
      int i, j;
      for (i = 0; i < types.length; i++) {
        select.setString(i + 1, types[i]);
      }
      for (j = 0; j < ids.length; j++) {
        select.setInt(i + j + 1, ids[j]);
      }
      rs = select.executeQuery();
      // }
      return getRows(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("UserSetTable.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      organization.releaseAll(rs, select);
    }
  }

  protected int updateRelation(String query, String[] types, int[] ids)
      throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      SilverTrace.debug("admin", "UserSetTable.updateRelation",
          "root.MSG_QUERY", "Ln 640");
      statement = organization.getStatement(query);
      // synchronized (statement)
      // {
      int i, j;
      for (i = 0; i < types.length; i++) {
        statement.setString(i + 1, types[i]);
      }
      for (j = 0; j < ids.length; j++) {
        if (ids[j] == -1)
          statement.setNull(i + j + 1, Types.INTEGER);
        else
          statement.setInt(i + j + 1, ids[j]);
      }
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("UserSetTable.updateRelation()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("UserSetTable.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      organization.releaseStatement(statement);
    }
  }

  protected int updateRelation(String query, String[] types, int[] ids,
      int weigth) throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      SilverTrace.debug("admin", "UserSetTable.updateRelation",
          "root.MSG_QUERY", "Ln 677");
      statement = organization.getStatement(query);
      // synchronized (statement)
      // {
      int i, j;
      statement.setInt(1, weigth);
      for (i = 0; i < types.length; i++) {
        statement.setString(i + 2, types[i]);
      }
      for (j = 0; j < ids.length; j++) {
        if (ids[j] == -1)
          statement.setNull(i + j + 2, Types.INTEGER);
        else
          statement.setInt(i + j + 2, ids[j]);
      }
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("UserSetTable.updateRelation()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("UserSetTable.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      organization.releaseStatement(statement);
    }
  }

  /**
   * Returns the integer of the single row, single column resultset returned by the given query with
   * (type,id) parameters. Returns null if the result set was empty.
   */
  protected Integer getInteger(String query, String[] types, int[] ids)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "UserSetTable.getInteger", "root.MSG_QUERY",
          "Ln 721");
      select = organization.getStatement(query);
      // synchronized (select)
      // {
      int i, j;
      for (i = 0; i < types.length; i++) {
        select.setString(i + 1, types[i]);
      }
      for (j = 0; j < ids.length; j++) {
        select.setInt(i + j + 1, ids[j]);
      }
      rs = select.executeQuery();
      // /}
      return getInteger(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("UserSetTable.getInteger",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      organization.releaseAll(rs, select);
    }
  }

  protected int updateRelation(String query) throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      SilverTrace.debug("admin", "UserSetTable.updateRelation",
          "root.MSG_QUERY", "Ln 756");
      statement = organization.getStatement(query);
      // synchronized (statement)
      // {
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      throw new AdminPersistenceException("UserSetTable.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      organization.releaseStatement(statement);
    }
  }

  private OrganizationSchema organization = null;
}
