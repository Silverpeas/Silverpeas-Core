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

import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.relationship.RelationShip;
import org.silverpeas.core.socialnetwork.relationship.RelationShipDao;
import org.silverpeas.core.socialnetwork.relationship.RelationShipEventNotifier;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
@Singleton
public class InvitationService {

  @Inject
  private InvitationDao invitationDao;
  @Inject
  private RelationShipDao relationShipDao;
  @Inject
  private RelationShipEventNotifier relationShipEventNotifier;

  public static InvitationService get() {
    return ServiceProvider.getService(InvitationService.class);
  }

  /**
   * Default Constructor
   */
  private InvitationService() {
  }

  /**
   * send invitation
   * @param invitation an Invitation object
   * @return the following integer value
   * <ul>
   * <li>-1 if the invitation already exists</li>
   * <li>-2 if the relationship already exists</li>
   * <li>the id of invitation if the adding has been done successfully</li>
   * </ul>
   */
  public int invite(Invitation invitation) {
    int invitationRslt = 0;
    Connection connection = null;
    try {
      connection = getConnection();
      boolean alreadySent =
          invitationDao.isExists(connection, invitation.getSenderId(), invitation.getReceiverId());
      if (alreadySent) {
        invitationRslt = -1;
      } else {
        if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation
            .getReceiverId())) {
          invitationRslt = -2;
        } else {
          invitationRslt = invitationDao.createInvitation(connection, invitation);
          notifyGuest(invitation);
        }
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return invitationRslt;
  }

  /**
   * ignore this invitation
   * @param id : the invitation identifier to ignore (delete)
   */
  public void ignoreInvitation(int id) {
    Connection connection = null;
    try {
      connection = getConnection();
      invitationDao.deleteInvitation(connection, id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * accept invitation between sender and receiver and create the relationship
   * @param idInvitation
   * @return -1 if this Invitation not exists, -2 if the RelationShip already exists, else the id of
   * RelationShip if the action has been done successfully
   */
  public int acceptInvitation(int idInvitation) {
    int resultAcceptInvitation = 0;
    Connection connection = null;
    try {
      connection = getConnection();
      connection.setAutoCommit(false);
      Invitation invitation = invitationDao.getInvitation(connection, idInvitation);
      RelationShip ship1 = null;
      RelationShip ship2 = null;
      if (invitation == null) {
        resultAcceptInvitation = -1;
      } else if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation.
          getReceiverId())) {
        resultAcceptInvitation = -2;
      } else {
        ship1 = new RelationShip();
        ship1.setUser1Id(invitation.getSenderId());
        ship1.setUser2Id(invitation.getReceiverId());
        ship1.setAcceptanceDate(new java.sql.Timestamp(new Date().getTime()));
        ship1.setInviterId(invitation.getSenderId());

        ship2 = new RelationShip();
        ship2.setUser1Id(invitation.getReceiverId());
        ship2.setUser2Id(invitation.getSenderId());
        ship2.setAcceptanceDate(new java.sql.Timestamp(new Date().getTime()));
        ship2.setInviterId(invitation.getSenderId());

        invitationDao.deleteSameInvitations(connection, idInvitation);
        resultAcceptInvitation = relationShipDao.createRelationShip(connection, ship1);
        relationShipDao.createRelationShip(connection, ship2);
      }
      connection.commit();

      if (ship1 != null) {
        // notify on relationship creation
        for (RelationShip ship : Arrays.asList(ship1, ship2)) {
          relationShipEventNotifier.notifyEventOn(ResourceEvent.Type.CREATION, ship);
        }
      }

      // alert sender of receiver acceptation
      alertAcceptation(invitation);
    } catch (Exception ex) {
      resultAcceptInvitation = 0;
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return resultAcceptInvitation;
  }

  /**
   * return all my invitations sent
   * @param userId the user identifier
   * @return List<Invitation>
   */
  public List<Invitation> getAllMyInvitationsSent(int userId) {
    Connection connection = null;
    List<Invitation> invitations = new ArrayList<>();

    try {
      connection = getConnection();
      invitations = invitationDao.getAllMyInvitationsSent(connection, userId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
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
    List<Invitation> invitations = new ArrayList<>();

    try {
      connection = getConnection();
      invitations = invitationDao.getAllMyInvitationsReceive(connection, myId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return invitations;
  }

  /**
   * return invitation by her id
   * @param id invitation identifier
   * @return Invitation
   */
  public Invitation getInvitation(int id) {
    Connection connection = null;

    try {
      connection = getConnection();
      return invitationDao.getInvitation(connection, id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return null;
  }

  /**
   * return invitation between 2 users
   * @param senderId
   * @param receiverId
   * @return Invitation
   */
  public Invitation getInvitation(int senderId, int receiverId) {
    Connection connection = null;

    try {
      connection = getConnection();
      return invitationDao.getInvitation(connection, senderId, receiverId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return null;
  }

  private Connection getConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  private void alertAcceptation(Invitation invitation) {
    UserNotificationHelper.buildAndSend(new AcceptationUserNotification(invitation));
  }

  private void notifyGuest(Invitation invitation) {
    UserNotificationHelper.buildAndSend(new NewInvitationUserNotification(invitation));
  }
}