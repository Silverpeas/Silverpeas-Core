/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.socialnetwork.invitation;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.relationship.RelationShip;
import org.silverpeas.core.socialnetwork.relationship.RelationShipDao;
import org.silverpeas.core.socialnetwork.relationship.RelationShipEventNotifier;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
@Service
@Singleton
public class InvitationService {

  /**
   * Predefined status code for an invitation already existing at relationship invitation.
   */
  public static final int INVITATION_ALREADY_EXISTING = -1;

  /**
   * Predefined status code for a relationship already existing between two users at invitation
   * acceptance.
   */
  public static final int RELATIONSHIP_ALREADY_EXISTING = -2;

  /**
   * Predefined status code for a non existing invitation at invitation acceptance.
   */
  public static final int INVITATION_NOT_EXISTING = -1;

  @Inject
  private InvitationDao invitationDao;
  @Inject
  private RelationShipDao relationShipDao;
  @Inject
  private RelationShipEventNotifier relationShipEventNotifier;

  /**
   * Default Constructor
   */
  InvitationService() {
  }

  public static InvitationService get() {
    return ServiceProvider.getService(InvitationService.class);
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
    int newId = Transaction.performInOne(() -> saveInvitation(invitation));
    if (newId != INVITATION_ALREADY_EXISTING && newId != RELATIONSHIP_ALREADY_EXISTING) {
      notifyGuest(invitation);
    }
    return newId;
  }

  private int saveInvitation(Invitation invitation) {
    int invitationResult = 0;
    try (Connection connection = getConnection()) {
      boolean alreadySent =
          invitationDao.isExists(connection, invitation.getSenderId(), invitation.getReceiverId());
      if (alreadySent) {
        invitationResult = INVITATION_ALREADY_EXISTING;
      } else {
        if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation
            .getReceiverId())) {
          invitationResult = RELATIONSHIP_ALREADY_EXISTING;
        } else {
          invitationResult = invitationDao.createInvitation(connection, invitation);
        }
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return invitationResult;
  }

  /**
   * ignore this invitation
   * @param id the invitation identifier to ignore (delete)
   */
  @Transactional
  public void ignoreInvitation(int id) {
    try (Connection connection = getConnection()) {
      invitationDao.deleteInvitation(connection, id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  /**
   * accept invitation between sender and receiver and create the relationship
   * @param idInvitation invitation identifier
   * @return -1 if this Invitation not exists, -2 if the RelationShip already exists, else the id of
   * RelationShip if the action has been done successfully
   */
  public int acceptInvitation(int idInvitation) {

    Pair<Pair<Integer, Invitation>, Pair<RelationShip, RelationShip>> result =
        Transaction.performInOne(() -> saveAcceptInvitation(idInvitation));

    final int resultAcceptInvitation = result.getLeft().getLeft();
    if (resultAcceptInvitation > 0) {

      final Invitation invitation = result.getLeft().getRight();
      final RelationShip ship1 = result.getRight().getLeft();
      final RelationShip ship2 = result.getRight().getRight();

      if (ship1 != null) {
        // notify on relationship creation
        for (RelationShip ship : Arrays.asList(ship1, ship2)) {
          relationShipEventNotifier.notifyEventOn(ResourceEvent.Type.CREATION, ship);
        }
      }

      // alert sender of receiver acceptation
      alertAcceptation(invitation);
    }

    return resultAcceptInvitation;
  }

  private Pair<Pair<Integer, Invitation>, Pair<RelationShip, RelationShip>> saveAcceptInvitation(
      int idInvitation) {
    Invitation invitation = null;
    int resultAcceptInvitation = 0;
    RelationShip ship1 = null;
    RelationShip ship2 = null;
    try (Connection connection = getConnection()) {
      invitation = invitationDao.getInvitation(connection, idInvitation);
      if (invitation == null) {
        resultAcceptInvitation = INVITATION_NOT_EXISTING;
      } else if (relationShipDao.isInRelationShip(connection, invitation.getSenderId(), invitation.
          getReceiverId())) {
        resultAcceptInvitation = RELATIONSHIP_ALREADY_EXISTING;
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
    } catch (Exception ex) {
      resultAcceptInvitation = 0;
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return Pair.of(Pair.of(resultAcceptInvitation, invitation), Pair.of(ship1, ship2));
  }

  /**
   * return all my invitations sent
   * @param userId the user identifier
   * @return a list of invitations
   */
  public List<Invitation> getAllMyInvitationsSent(int userId) {
    List<Invitation> invitations = new ArrayList<>();
    try (Connection connection = getConnection()) {
      invitations = invitationDao.getAllMyInvitationsSent(connection, userId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return invitations;
  }

  public List<Invitation> getAllMyInvitationsReceive(int myId) {
    List<Invitation> invitations = new ArrayList<>();
    try (Connection connection = getConnection()) {
      invitations = invitationDao.getAllMyInvitationsReceive(connection, myId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return invitations;
  }

  public Invitation getInvitation(int id) {
    try (Connection connection = getConnection()) {
      return invitationDao.getInvitation(connection, id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return null;
  }

  public Invitation getInvitation(int senderId, int receiverId) {
    try (Connection connection = getConnection()) {
      return invitationDao.getInvitation(connection, senderId, receiverId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return null;
  }

  /**
   * Deletes all the invitations both sent and received of the specified user.
   * @param userId the unique identifier of the user.
   */
  @Transactional
  public void deleteAllMyInvitations(String userId) {
    try (Connection connection = getConnection()) {
      invitationDao.deleteAllInvitations(connection, Integer.valueOf(userId));
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(e);
    }
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