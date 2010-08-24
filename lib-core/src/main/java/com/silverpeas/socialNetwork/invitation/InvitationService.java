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
package com.silverpeas.socialNetwork.invitation;

import com.silverpeas.socialNetwork.relationShip.RelationShip;
import com.silverpeas.socialNetwork.relationShip.RelationShipDao;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
public class InvitationService {

  private InvitationDao invitationDao;
  private RelationShipDao relationShipDao;

  public InvitationService() {
    invitationDao = new InvitationDao();
    relationShipDao = new RelationShipDao();
  }

  /*
   * send invitationreturn -1 if the invitation already exists return -2 if the relationShip already
   * exists return the id of inviation if the adding done
   * @param: int id
   */
  public int invite(Invitation invitation) {
    int resultRnvitation = 0;
    Connection connection = null;
    try {
      connection = getConnection();
      if ((invitationDao.isExists(connection, invitation.getSenderId(), invitation.getReceiverId()) || invitationDao
          .isExists(connection, invitation.getReceiverId(), invitation.getSenderId()))) {
        resultRnvitation = -1;
      } else if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation.
          getReceiverId())) {
        resultRnvitation = -2;
      } else {
        resultRnvitation = invitationDao.createInvitation(connection, invitation);
      }
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation", "InvitationService.invite", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return resultRnvitation;
  }

  /*
   * ignore this invitation
   * @param: int id
   */

  public void ignoreInvitation(int id) {
    Connection connection = null;
    try {
      connection = getConnection();
      invitationDao.deleteInvitation(connection, id);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.ignoreInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
  }

  /*
   * accepte this invitation that the sender an receiver become in Relationreturn -1 if this
   * Invitation not existsreturn -2 if the RelationShip already exists return the id of RelationShip
   * if the adding done
   * @param: int idInvitation
   */

  public int accepteInvitation(int idInvitation) {
    int resultAccepteInvitation = 0;
    Connection connection = null;
    try {
      connection = getConnection();
      Invitation invitation = invitationDao.getInvitation(connection, idInvitation);
      if (invitation == null) {
        resultAccepteInvitation = -1;
      } else if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation.
          getReceiverId())) {
        resultAccepteInvitation = -2;
      } else {

        RelationShip ship1 = new RelationShip();
        ship1.setUser1Id(invitation.getSenderId());
        ship1.setUser2Id(invitation.getReceiverId());
        ship1.setAcceptanceDate(new java.sql.Timestamp(new Date().getTime()));
        ship1.setInviterId(invitation.getSenderId());

        RelationShip ship2 = new RelationShip();
        ship2.setUser1Id(invitation.getReceiverId());
        ship2.setUser2Id(invitation.getSenderId());
        ship2.setAcceptanceDate(new java.sql.Timestamp(new Date().getTime()));
        ship2.setInviterId(invitation.getSenderId());

        invitationDao.deleteInvitation(connection, idInvitation);
        resultAccepteInvitation = relationShipDao.createRelationShip(connection, ship1);
        relationShipDao.createRelationShip(connection, ship2);
      }
      connection.commit();
    } catch (Exception ex) {
      resultAccepteInvitation = 0;
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.accepteInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return resultAccepteInvitation;
  }

  /**
   * return All my invitations sented
   * @param myId
   * @return List<Invitation>
   */
  public List<Invitation> getAllMyInvitationsSent(int myId) {
    Connection connection = null;
    List<Invitation> invitations = new ArrayList<Invitation>();

    try {
      connection = getConnection();
      invitations = invitationDao.getAllMyInvitationsSent(connection, myId);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.ignoreInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return invitations;
  }

  /**
   * return All my invitations received
   * @param myId
   * @return
   */
  public List<Invitation> getAllMyInvitationsReceive(int myId) {
    Connection connection = null;
    List<Invitation> invitations = new ArrayList<Invitation>();

    try {
      connection = getConnection();
      invitations = invitationDao.getAllMyInvitationsReceive(connection, myId);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.ignoreInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return invitations;
  }

  /**
   * rturn invitation by her id
   * @param senderId
   * @param receiverId
   * @return Invitation
   */
  public Invitation getInvitation(int id) {
    Connection connection = null;
    Invitation invitation = new Invitation();

    try {
      connection = getConnection();
      invitation = invitationDao.getInvitation(connection, id);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.ignoreInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return invitation;
  }

  /**
   * rturn invitation between 2 users
   * @param senderId
   * @param receiverId
   * @return Invitation
   */
  public Invitation getInvitation(int senderId, int receiverId) {
    Connection connection = null;
    Invitation invitation = new Invitation();

    try {
      connection = getConnection();
      invitation = invitationDao.getInvitation(connection, senderId, receiverId);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Invitation",
          "InvitationService.ignoreInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return invitation;
  }

  /**
   * initialize the Connection to database
   * @return Connection
   * @throws UtilException
   * @throws SQLException
   */

  private Connection getConnection() throws UtilException, SQLException {
    Connection connection = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    connection.setAutoCommit(false);
    return connection;
  }
}
