/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.socialnetwork.relationship;

import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
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

@Singleton
@Transactional
public class RelationShipService {

  @Inject
  private RelationShipDao relationShipDao;
  @Inject
  private RelationShipEventNotifier relationShipEventNotifier;

  public static RelationShipService get() {
    return ServiceProvider.getService(RelationShipService.class);
  }

  protected RelationShipService() {
  }

  public boolean removeRelationShip(int idUser1, int idUser2) {
    boolean endAction = false;
    try (Connection connection = DBUtil.openConnection()) {
      RelationShip rel1to2 = relationShipDao.getRelationShip(connection, idUser1, idUser2);
      RelationShip rel2to1 = relationShipDao.getRelationShip(connection, idUser2, idUser1);
      relationShipDao.deleteRelationShip(connection, idUser1, idUser2);
      relationShipDao.deleteRelationShip(connection, idUser2, idUser1);
      endAction = true;
      for (RelationShip ship : Arrays.asList(rel1to2, rel2to1)) {
        if (ship != null) {
          relationShipEventNotifier.notifyEventOn(ResourceEvent.Type.DELETION, ship);
        }
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return endAction;
  }

  public boolean isInRelationShip(int user1Id, int user2Id) throws SQLException {
    boolean isInRelationShip = false;
    try (Connection connection = DBUtil.openConnection()) {
      isInRelationShip = relationShipDao.isInRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return isInRelationShip;
  }

  public List<RelationShip> getAllMyRelationShips(int myId) throws SQLException {
    List<RelationShip> listMyRelation = new ArrayList<RelationShip>();
    try (Connection connection = DBUtil.openConnection()) {
      listMyRelation = relationShipDao.getAllMyRelationShips(connection, myId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return listMyRelation;
  }

  public List<String> getMyContactsIds(int myId) throws SQLException {
    List<String> myContactsIds = new ArrayList<String>();
    try (Connection connection = DBUtil.openConnection()) {
      myContactsIds = relationShipDao.getMyContactsIds(connection, myId);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return myContactsIds;
  }

  public List<String> getAllCommonContactsIds(int user1Id, int user2Id) throws SQLException {
    List<String> myContactsIds = new ArrayList<String>();
    try (Connection connection = DBUtil.openConnection()) {
      myContactsIds = relationShipDao.getAllCommonContactsIds(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return myContactsIds;
  }

  public List<SocialInformation> getAllMyRelationShips(String userId,
      Date begin, Date end) throws SQLException {
    try (Connection connection = DBUtil.openConnection()) {
      return relationShipDao.getAllMyRelationShips(connection, userId, begin, end);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return new ArrayList<>();
  }

  public List<SocialInformation> getAllRelationShipsOfContacts(List<String> myContactsIds,
      Date begin, Date end) throws SQLException {
    try (Connection connection = DBUtil.openConnection()) {
      return relationShipDao.getAllRelationShipsOfContacts(connection, myContactsIds, begin, end);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return new ArrayList<>();
  }

  public RelationShip getRelationShip(int user1Id, int user2Id) throws SQLException {
    RelationShip relation = null;
    try (Connection connection = DBUtil.openConnection()) {
      relation = relationShipDao.getRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return relation;
  }
}
