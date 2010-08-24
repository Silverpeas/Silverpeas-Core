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
package com.silverpeas.socialNetwork.relationShip;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RelationShipDao {

  private static final String INSERT_RELATIONSHIP =
      "INSERT INTO sb_sn_RelationShip (id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterid) VALUES (?, ?, ?, ?, ?,?)";
  private static final String DELETE_RELATIONSHIP =
      "DELETE FROM sb_sn_RelationShip WHERE user1Id = ? and user2Id= ? ";
  private static final String SELECT_RELATIONSHIP =
      "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId FROM sb_sn_RelationShip  WHERE user1Id = ? and user2Id= ?";
  private static final String SELECT_ALL_MY_RELATIONSHIP =
      "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId FROM sb_sn_RelationShip  WHERE user1Id = ?";

  /**
   * rturn int (the id of this new relationShip)
   * @param connection
   * @param relationShip
   * @return int
   * @throws UtilException
   * @throws SQLException
   */
  public int createRelationShip(Connection connection, RelationShip relationShip) throws
      UtilException, SQLException {
    int id = DBUtil.getNextId("sb_sn_RelationShip", "id");
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(INSERT_RELATIONSHIP);
      pstmt.setInt(1, id);
      pstmt.setInt(2, relationShip.getUser1Id());
      pstmt.setInt(3, relationShip.getUser2Id());
      pstmt.setInt(4, relationShip.getTypeRelationShipId());
      pstmt.setTimestamp(5, new Timestamp(relationShip.getAcceptanceDate().getTime()));
      pstmt.setInt(6, relationShip.getInviterId());
      pstmt.executeUpdate();
    } finally {
      DBUtil.close(pstmt);
    }
    return id;

  }

  /**
   * delete this relationShip rturn boolean (if this relationShips is deleted return true)
   * @param connection
   * @param user1Id
   * @param user2Id
   * @return boolean
   * @throws SQLException
   */
  public boolean deleteRelationShip(Connection connection, int user1Id, int user2Id) throws
      SQLException {
    PreparedStatement pstmt = null;
    boolean endAction = false;
    try {
      pstmt = connection.prepareStatement(DELETE_RELATIONSHIP);
      pstmt.setInt(1, user1Id);
      pstmt.setInt(2, user2Id);
      pstmt.executeUpdate();
      endAction = true;
    } finally {
      DBUtil.close(pstmt);
    }
    return endAction;
  }

  /**
   * delete this relationShip rturn boolean (if this relationShips is deleted return true)
   * @param connection
   * @param relationShip
   * @return boolean
   * @throws SQLException
   */
  public boolean deleteRelationShip(Connection connection, RelationShip relationShip) throws
      SQLException {

    return deleteRelationShip(connection, relationShip.getUser1Id(), relationShip.getUser2Id());
  }

  /**
   * return the relationShip witch between user1 and user2
   * @param connection
   * @param user1Id
   * @param user2Id
   * @return RelationShip
   * @throws SQLException
   */
  public RelationShip getRelationShip(Connection connection, int user1Id, int user2Id) throws
      SQLException {
    RelationShip relationShip = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(SELECT_RELATIONSHIP);

      pstmt.setInt(1, user1Id);
      pstmt.setInt(2, user2Id);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));
      }

    } finally {
      DBUtil.close(pstmt);
    }
    return relationShip;
  }

  /**
   * rturn if this relationShip exist or not
   * @param connection
   * @param user1Id
   * @param user2Id
   * @return boolean
   * @throws SQLException
   */

  public boolean isInRelationShip(Connection connection, int user1Id, int user2Id) throws
      SQLException {
    return getRelationShip(connection, user1Id, user2Id) != null;
  }

  /**
   * rturn the list of all my RelationShips
   * @param connection
   * @param myId
   * @return List<RelationShip>
   * @throws SQLException
   */
  public List<RelationShip> getAllMyRelationShips(Connection connection, int myId) throws
      SQLException {

    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<RelationShip> listMyRelation = new ArrayList<RelationShip>();
    try {
      pstmt = connection.prepareStatement(SELECT_ALL_MY_RELATIONSHIP);
      pstmt.setInt(1, myId);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        RelationShip relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));

        listMyRelation.add(relationShip);
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * get list of my socialInformation (relationShip) according to number of Item and the first Index
   * @param con
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  public List<SocialInformationRelationShip> getAllMyRelationShips(Connection con,
      String userId, int numberOfElement, int firstIndex) throws SQLException {
    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.contains("POSTGRESQL")) {
      return getAllMyRelationShips_PostgreSQL(con, userId, numberOfElement, firstIndex);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getAllMyRelationShips_Oracle(con, userId, numberOfElement, firstIndex);
    }
    return getAllMyRelationShips_MMS(con, userId, numberOfElement, firstIndex);
  }

  /**
   * when the data base is PostgreSQL get list of my socialInformation (relationShip) according to
   * number of Item and the first Index
   * @param connection
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SQLException
   */
  List<SocialInformationRelationShip> getAllMyRelationShips_PostgreSQL(Connection connection,
      String userId, int numberOfElement, int firstIndex) throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query = "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId "
        + "FROM sb_sn_RelationShip  WHERE user1Id = ? order by acceptanceDate desc ";
    if (numberOfElement > 0) {
      query += " limit ? offset ? ";
    }
    try {
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, Integer.parseInt(userId));
      if (numberOfElement > 0) {
        pstmt.setInt(2, numberOfElement);
        pstmt.setInt(3, firstIndex);
      }

      rs = pstmt.executeQuery();
      while (rs.next()) {
        RelationShip relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));
        listMyRelation.add(new SocialInformationRelationShip(relationShip));

      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * when the data base is Oracle get list of my socialInformation (relationShip) according to
   * number of Item and the first Index
   * @param connection
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */

  List<SocialInformationRelationShip> getAllMyRelationShips_Oracle(Connection connection,
      String userId, int numberOfElement, int firstIndex) throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query =
        "select * from (ROWNUM num , table_oracle.* from (SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,,inviterId "
            + "FROM sb_sn_RelationShip  WHERE user1Id = ? order by acceptanceDate desc ) table_oracle) ";
    if (numberOfElement > 0) {
      query += " where num between ? and ? ";
    }
    try {
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, Integer.parseInt(userId));
      if (numberOfElement > 0) {
        pstmt.setInt(2, numberOfElement);
        pstmt.setInt(3, firstIndex);
      }

      rs = pstmt.executeQuery();
      while (rs.next()) {
        RelationShip relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));
        listMyRelation.add(new SocialInformationRelationShip(relationShip));
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * when the data base is Oracle get list of my socialInformation (relationShip) according to
   * number of Item and the first Index
   * @param connection
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  List<SocialInformationRelationShip> getAllMyRelationShips_MMS(Connection connection,
      String userId, int numberOfElement, int firstIndex) throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query = "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId "
        + "FROM sb_sn_RelationShip  WHERE user1Id = ? order by acceptanceDate desc ";

    try {
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, Integer.parseInt(userId));
      rs = pstmt.executeQuery();
      int index = 0;
      while (rs.next()) {
        if ((index >= firstIndex && index < numberOfElement + firstIndex) || numberOfElement <= 0) {
          RelationShip relationShip = new RelationShip();
          relationShip.setId(rs.getInt(1));
          relationShip.setUser1Id(rs.getInt(2));
          relationShip.setUser2Id(rs.getInt(3));
          relationShip.setTypeRelationShipId(rs.getInt(4));
          relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
          relationShip.setInviterId(rs.getInt(6));
          listMyRelation.add(new SocialInformationRelationShip(relationShip));
        }
        index++;
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * Get list socialInformationRelationShip (relationShips) of my Contacts according to number of
   * Item and the first Index
   * @param con
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  List<SocialInformationRelationShip> getAllRelationShipsOfMyContact(Connection con,
      String myId, List<String> myContactsIds, int numberOfElement, int firstIndex) throws
      SQLException {
    String databaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (databaseProductName.contains("POSTGRESQL")) {
      return getAllRelationShipsOfMyContact_PostgreSQL(con, myId, myContactsIds, numberOfElement,
          firstIndex);
    } else if (databaseProductName.toUpperCase().contains("ORACLE")) {
      return getAllRelationShipsOfMyContact_Oracle(con, myId, myContactsIds, numberOfElement,
          firstIndex);
    }
    return getAllRelationShipsOfMyContact_MMS(con, myId, myContactsIds, numberOfElement, firstIndex);
  }

  private static String listToSqlString(List<String> list) {
    String result = "";
    if (list == null || list.size() == 0) {
      return "";
    }
    int size = list.size();
    int i = 0;
    for (String var : list) {
      i++;
      result += var;
      if (i != size) {
        result += ",";
      }
    }
    return result;
  }

  /**
   * When data base is PostgreSQL get list socialInformationRelationShip (relationShips) of my
   * Contacts according to number of Item and the first Index
   * @param con
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  private List<SocialInformationRelationShip> getAllRelationShipsOfMyContact_PostgreSQL(
      Connection con, String myId, List<String> myContactsIds, int numberOfElement, int firstIndex)
      throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query =
        "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId "
            + "FROM sb_sn_RelationShip  WHERE user1Id in(" + listToSqlString(myContactsIds) +
            ")and inviterid=user1Id order by acceptanceDate desc ";
    if (numberOfElement > 0) {
      query += "limit ? offset ? ";
    }
    try {
      pstmt = con.prepareStatement(query);
      if (numberOfElement > 0) {
        pstmt.setInt(1, numberOfElement);
        pstmt.setInt(2, firstIndex);
      }

      rs = pstmt.executeQuery();
      while (rs.next()) {
        RelationShip relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));
        listMyRelation.add(new SocialInformationRelationShip(relationShip));
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * When data base is Oracle get list socialInformationRelationShip (relationShips) of my Contacts
   * according to number of Item and the first Index
   * @return: List <SocialInformationRelationShip>
   * @param: Connection connection,String myId,List<String> myContactsIds, int numberOfElement, int
   * firstIndex
   */
  private List<SocialInformationRelationShip> getAllRelationShipsOfMyContact_Oracle(Connection con,
      String myId, List<String> myContactsIds, int numberOfElement, int firstIndex) throws
      SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query =
        "select * from (ROWNUM num , table_oracle.* from (SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId "
            +
            "FROM sb_sn_RelationShip  WHERE user1Id in(" +
            listToSqlString(myContactsIds) +
            ")and inviterid=user1Id order by acceptanceDate desc ) table_oracle) ";
    if (numberOfElement > 0) {
      query += " where num between ? and ? ";
    }
    try {
      pstmt = con.prepareStatement(query);
      if (numberOfElement > 0) {
        pstmt.setInt(1, numberOfElement);
        pstmt.setInt(2, firstIndex);
      }

      rs = pstmt.executeQuery();
      while (rs.next()) {
        RelationShip relationShip = new RelationShip();
        relationShip.setId(rs.getInt(1));
        relationShip.setUser1Id(rs.getInt(2));
        relationShip.setUser2Id(rs.getInt(3));
        relationShip.setTypeRelationShipId(rs.getInt(4));
        relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
        relationShip.setInviterId(rs.getInt(6));
        listMyRelation.add(new SocialInformationRelationShip(relationShip));
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * When data base is PostgreSQL get list socialInformationRelationShip (relationShips) of my
   * Contacts according to number of Item and the first Index
   * @param con
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  private List<SocialInformationRelationShip> getAllRelationShipsOfMyContact_MMS(Connection con,
      String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<SocialInformationRelationShip> listMyRelation =
        new ArrayList<SocialInformationRelationShip>();
    String query =
        "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate,inviterId "
            + "FROM sb_sn_RelationShip  WHERE user1Id in(" + listToSqlString(myContactsIds) +
            ")and inviterid=user1Id order by acceptanceDate desc ";

    try {
      pstmt = con.prepareStatement(query);
      rs = pstmt.executeQuery();
      int index = 0;
      while (rs.next()) {
        if ((index >= firstIndex && index < numberOfElement + firstIndex) || numberOfElement <= 0) {
          RelationShip relationShip = new RelationShip();
          relationShip.setId(rs.getInt(1));
          relationShip.setUser1Id(rs.getInt(2));
          relationShip.setUser2Id(rs.getInt(3));
          relationShip.setTypeRelationShipId(rs.getInt(4));
          relationShip.setAcceptanceDate(new Date(rs.getTimestamp(5).getTime()));
          relationShip.setInviterId(rs.getInt(6));
          listMyRelation.add(new SocialInformationRelationShip(relationShip));
        }
        index++;
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }

  /**
   * get all my RelationShips ids
   * @param connection
   * @param myId
   * @return List<String>
   * @throws SQLException
   */
  List<String> getMyContactsIds(Connection connection, int myId) throws SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<String> myContactsIds = new ArrayList<String>();
    try {
      String query = "SELECT  user2Id FROM sb_sn_RelationShip  WHERE user1Id = ?";
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, myId);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        myContactsIds.add(rs.getInt(1) + "");
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return myContactsIds;

  }

  /**
   * get all common contacts Ids of usre1 and user2
   * @param connection
   * @param user1Id
   * @param user2Id
   * @return List<String>
   * @throws SQLException
   */
  List<String> getAllCommonContactsIds(Connection connection, int user1Id, int user2Id) throws
      SQLException {
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    List<String> myContactsIds = new ArrayList<String>();
    try {
      String query = "SELECT  user2Id FROM sb_sn_RelationShip  WHERE user1Id = ? and "
          + " user2id in(SELECT  user2Id FROM sb_sn_RelationShip  WHERE user1Id = ? )";
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, user1Id);
      pstmt.setInt(2, user2Id);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        myContactsIds.add(rs.getInt(1) + "");
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return myContactsIds;

  }
}
