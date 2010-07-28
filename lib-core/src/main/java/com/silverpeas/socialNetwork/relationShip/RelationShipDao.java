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

  private static final String INSERT_RELATIONSHIP = "INSERT INTO sb_sn_RelationShip (id, user1Id, user2Id, typeRelationShipId, acceptanceDate) VALUES (?, ?, ?, ?, ?)";
  private static final String DELETE_RELATIONSHIP = "DELETE FROM sb_sn_RelationShip WHERE user1Id = ? and user2Id= ? ";
  private static final String SELECT_RELATIONSHIP = "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate FROM sb_sn_RelationShip  WHERE user1Id = ? and user2Id= ?";
  private static final String SELECT_ALL_MY_RELATIONSHIP = "SELECT id, user1Id, user2Id, typeRelationShipId, acceptanceDate FROM sb_sn_RelationShip  WHERE user1Id = ?";

  /*
   * rturn int (the id of this new relationShip)
   *
   * @param:Connection connection, RelationShip relationShip
   *
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
      pstmt.executeUpdate();
    } finally {
      DBUtil.close(pstmt);
    }
    return id;

  }
  /*
   * rturn boolean (if this relationShips is deleted return true)
   *
   * @param:Connection connection, String user1Id, String user2Id
   *
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
  /*
   * delete RelationShip (if this relationShips is deleted return true)
   * @return boolean
   * @param:Connection connection, RelationShip relationShip
   *
   */

  public boolean deleteRelationShip(Connection connection, RelationShip relationShip) throws
      SQLException {

    return deleteRelationShip(connection, relationShip.getUser1Id(), relationShip.getUser2Id());
  }
  /*
   * rturn RelationShip
   *
   * @param:Connection connection, int user1Id, int user2Id
   *
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
      }

    } finally {
      DBUtil.close(pstmt);
    }
    return relationShip;
  }
  /*
   * rturn boolean (true if this relationShip exist)
   *
   * @param:Connection connection, int user1Id, int user2Id
   *
   */

  public boolean isInRelationShip(Connection connection, int user1Id, int user2Id) throws
      SQLException {
    return getRelationShip(connection, user1Id, user2Id) != null;
  }
  /*
   * rturn the list of all my RelationShips
   *
   * @param:Connection connection, int myId
   *
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

        listMyRelation.add(relationShip);
      }
    } finally {
      DBUtil.close(pstmt);
    }
    return listMyRelation;
  }
}
