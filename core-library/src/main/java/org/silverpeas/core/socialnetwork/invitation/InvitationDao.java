/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.sql.SelectResultRowProcess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Repository
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
  private static final String DELETE_ALL_INVITATIONS =
      "DELETE FROM sb_sn_invitation WHERE senderID = ? OR receiverId = ?";

  private static final SelectResultRowProcess<Invitation> INVITATION_MAPPER = row -> {
    Invitation invitation = new Invitation();
    invitation.setId(row.getInt("id"));
    invitation.setSenderId(row.getInt("senderID"));
    invitation.setReceiverId(row.getInt("receiverId"));
    invitation.setMessage(row.getString("message"));
    invitation.setInvitationDate(new Date(row.getTimestamp("invitationDate").getTime()));
    return invitation;
  };

  int createInvitation(Connection connection, Invitation invitation) throws SQLException {
    int id = DBUtil.getNextId("sb_sn_invitation", "id");
    JdbcSqlQuery
      .create(INSERT_INVITATION,
        id,
        invitation.getSenderId(),
        invitation.getReceiverId(),
        invitation.getMessage(),
        Timestamp.from(Instant.now()))
      .executeWith(connection);
    return id;
  }

  public boolean deleteInvitation(Connection connection, int invitationId) throws SQLException {
    return JdbcSqlQuery.create(DELETE_INVITATION, invitationId).executeWith(connection) > 0;
  }

  /**
   * Delete invitations from same receiver and sender
   * <ul>
   * <li>Delete nothing if invitation doesn't exist</li>
   * <li>Delete one invitation if only one invitation has been sent from sender to receiver</li>
   * <li>Delete two invitations if sender and receiver has sent an invitation</li>
   * </ul>
   * @param connection connection to the database
   * @param invitationId the invitation identifier
   * @return true true when invitations from same sender identifier and receiver identifier are
   * deleted, false else if
   * @throws SQLException on SQL error
   */
  public boolean deleteSameInvitations(Connection connection, int invitationId) throws SQLException {
    return JdbcSqlQuery.create(DELETE_SAME_INVITATIONS, invitationId).executeWith(connection) > 0;
  }

  public boolean deleteAllInvitations(Connection connection, int userId) throws SQLException {
    return JdbcSqlQuery.create(DELETE_ALL_INVITATIONS, userId, userId).executeWith(connection) > 0;
  }

  public Invitation getInvitation(Connection connection, int senderId, int receiverId) throws
      SQLException {
    return JdbcSqlQuery
        .create(SELECT_INVITATION, senderId, receiverId)
        .executeUniqueWith(connection, INVITATION_MAPPER);
  }

  public Invitation getInvitation(Connection connection, int id) throws SQLException {
    return JdbcSqlQuery
        .create(SELECT_INVITATION_BY_ID, id)
        .executeUniqueWith(connection, INVITATION_MAPPER);
  }

  public boolean isExists(Connection connection, int senderId, int receiverId) throws SQLException {
    return getInvitation(connection, senderId, receiverId) != null;
  }

  public List<Invitation> getAllMyInvitationsSent(Connection connection, int myId) throws
      SQLException {
    return JdbcSqlQuery
        .create(SELECT_ALL_INVITATIONS_SENT, myId)
        .executeWith(connection, INVITATION_MAPPER);
  }

  public List<Invitation> getAllMyInvitationsReceive(Connection connection, int myId) throws
      SQLException {
    return JdbcSqlQuery
        .create(SELECT_ALL_INVITATIONS_RECEIVE, myId)
        .executeWith(connection, INVITATION_MAPPER);
  }
}
