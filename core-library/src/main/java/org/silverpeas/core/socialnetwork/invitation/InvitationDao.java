/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class InvitationDao {

  private static final String INSERT_INVITATION =
      "INSERT INTO sb_sn_invitation (id, senderID, receiverId, message, invitationDate) VALUES (?, ?, ?, ?, ?)";
  private static final String DELETE_INVITATION = "DELETE FROM sb_sn_invitation WHERE id = ?";
  private static final String DELETE_SAME_INVITATIONS =
      "DELETE FROM sb_sn_invitation  WHERE id IN (SELECT invit.id FROM sb_sn_invitation invit, sb_sn_invitation tmp WHERE tmp.id = ? AND ((tmp.senderid= invit.senderid AND tmp.receiverid = invit.receiverid) OR (tmp.senderid = invit.receiverid AND tmp.receiverid = invit.senderid)))";
  private static final String SELECT_INVITATION =
      "SELECT id, senderID, receiverId, message, invitationDate FROM sb_sn_invitation  WHERE senderID = ? and receiverId= ?";
  private static final String SELECT_INVITATION_BY_ID =
      "SELECT id, senderID, receiverId, message, invitationDate FROM sb_sn_invitation  WHERE id = ? ";
  private static final String SELECT_ALL_INVITATIONS_SENT =
      "SELECT id, senderID, receiverId, message, invitationDate FROM sb_sn_invitation  WHERE senderID = ?";
  private static final String SELECT_ALL_INVITATIONS_RECEIVE =
      "SELECT id, senderID, receiverId, message, invitationDate FROM sb_sn_invitation  WHERE receiverId= ?";

  /**
   * Create new invitation
   * @param connection
   * @param invitation
   * @return int the id of invitation
   * @throws SQLException
   */
  public int createInvitation(Connection connection, Invitation invitation) throws SQLException {

    int id = DBUtil.getNextId("sb_sn_invitation", "id");
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(INSERT_INVITATION);

      pstmt.setInt(1, id);
      pstmt.setInt(2, invitation.getSenderId());
      pstmt.setInt(3, invitation.getReceiverId());
      pstmt.setString(4, invitation.getMessage());
      pstmt.setTimestamp(5, new Timestamp(invitation.getInvitationDate().getTime()));
      pstmt.executeUpdate();
    } finally {
      DBUtil.close(pstmt);
    }
    return id;

  }

  /**
   * Delete invitation return true when this invitation was deleting
   * @param connection
   * @param invitationId the invitation identifier
   * @return boolean
   * @throws SQLException
   */
  public boolean deleteInvitation(Connection connection, int invitationId) throws SQLException {
    PreparedStatement pstmt = null;
    boolean endAction = false;
    try {
      pstmt = connection.prepareStatement(DELETE_INVITATION);
      pstmt.setInt(1, invitationId);
      pstmt.executeUpdate();
      endAction = true;
    } finally {
      DBUtil.close(pstmt);
    }
    return endAction;
  }

  /**
   * Delete invitations from same receiver and sender
   * <ul>
   * <li>Delete nothing if invitation doesn't exist</li>
   * <li>Delete one invitation if only one invitation has been sent from sender to receiver</li>
   * <li>Delete two invitations if sender and receiver has sent an invitation</li>
   * </ul>
   * @param connection
   * @param invitationId the invitation identifier
   * @return true true when invitations from same sender identifier and receiver identifier are
   * deleted, false else if
   * @throws SQLException
   */
  public boolean deleteSameInvitations(Connection connection, int invitationId) throws SQLException {
    PreparedStatement pstmt = null;
    boolean endAction = false;
    try {
      pstmt = connection.prepareStatement(DELETE_SAME_INVITATIONS);
      pstmt.setInt(1, invitationId);
      pstmt.executeUpdate();
      endAction = true;
    } finally {
      DBUtil.close(pstmt);
    }
    return endAction;
  }

  /**
   * @param connection a Connection
   * @param senderId the sender identifier
   * @param receiverId the receiver identifier
   * @return invitation between 2 users
   * @throws SQLException
   */
  public Invitation getInvitation(Connection connection, int senderId, int receiverId) throws
      SQLException {
    Invitation invitation = null;
    ResultSet rs;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(SELECT_INVITATION);
      pstmt.setInt(1, senderId);
      pstmt.setInt(2, receiverId);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        invitation = new Invitation();
        invitation.setId(rs.getInt(1));
        invitation.setSenderId(rs.getInt(2));
        invitation.setReceiverId(rs.getInt(3));
        invitation.setMessage(rs.getString(4));
        invitation.setInvitationDate(new Date(rs.getTimestamp(5).getTime()));
      }

    } finally {
      DBUtil.close(pstmt);
    }
    return invitation;
  }

  /**
   * retrieve an invitation
   * @param connection
   * @param id
   * @return Invitation
   * @throws SQLException
   * @return an invitation
   */

  public Invitation getInvitation(Connection connection, int id) throws SQLException {
    Invitation invitation = null;
    ResultSet rs;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(SELECT_INVITATION_BY_ID);
      pstmt.setInt(1, id);

      rs = pstmt.executeQuery();
      if (rs.next()) {
        invitation = new Invitation();
        invitation.setId(rs.getInt(1));
        invitation.setSenderId(rs.getInt(2));
        invitation.setReceiverId(rs.getInt(3));
        invitation.setMessage(rs.getString(4));
        invitation.setInvitationDate(new Date(rs.getTimestamp(5).getTime()));
      }

    } finally {
      DBUtil.close(pstmt);
    }
    return invitation;
  }

  /**
   * return true if this invitation exist between 2 users
   * @param connection
   * @param senderId
   * @param receiverId
   * @return boolean
   * @throws SQLException
   */

  public boolean isExists(Connection connection, int senderId, int receiverId) throws SQLException {
    return (getInvitation(connection, senderId, receiverId) != null);
  }

  /**
   * return All my invitations sented
   * @param connection
   * @param myId
   * @return List<Invitation>
   * @throws SQLException
   */

  public List<Invitation> getAllMyInvitationsSent(Connection connection, int myId) throws
      SQLException {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List<Invitation> invitations = new ArrayList<>();
    try {
      pstmt = connection.prepareStatement(SELECT_ALL_INVITATIONS_SENT);
      pstmt.setInt(1, myId);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        Invitation invitation = new Invitation();
        invitation.setId(rs.getInt(1));
        invitation.setSenderId(rs.getInt(2));
        invitation.setReceiverId(rs.getInt(3));
        invitation.setMessage(rs.getString(4));
        invitation.setInvitationDate(new Date(rs.getTimestamp(5).getTime()));
        invitations.add(invitation);
      }
    } finally {
      DBUtil.close(rs, pstmt);
    }
    return invitations;
  }

  /**
   * return All my invitations received
   * @param connection
   * @param myId
   * @return List<Invitation>
   * @throws SQLException
   */
  public List<Invitation> getAllMyInvitationsReceive(Connection connection, int myId) throws
      SQLException {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List<Invitation> invitations = new ArrayList<>();
    try {
      pstmt = connection.prepareStatement(SELECT_ALL_INVITATIONS_RECEIVE);
      pstmt.setInt(1, myId);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        Invitation invitation = new Invitation();
        invitation.setId(rs.getInt(1));
        invitation.setSenderId(rs.getInt(2));
        invitation.setReceiverId(rs.getInt(3));
        invitation.setMessage(rs.getString(4));
        invitation.setInvitationDate(new Date(rs.getTimestamp(5).getTime()));
        invitations.add(invitation);
      }
    } finally {
      DBUtil.close(rs, pstmt);
    }
    return invitations;
  }
}
