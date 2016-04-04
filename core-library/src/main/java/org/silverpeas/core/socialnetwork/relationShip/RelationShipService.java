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

package org.silverpeas.core.socialnetwork.relationShip;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.UtilException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RelationShipService {

  public static RelationShipService get() {
    return ServiceProvider.getService(RelationShipService.class);
  }

  @Inject
  private RelationShipDao relationShipDao;

  protected RelationShipService() {
  }

  private Connection getConnection(boolean useAutoCommit) throws UtilException, SQLException {
    Connection connection = DBUtil.openConnection();
    connection.setAutoCommit(useAutoCommit);
    return connection;
  }

  /**
   * remove RelationShip (if this relationShips is deleted return true)
   * @param idUser1
   * @param idUser2
   * @return boolean
   */

  public boolean removeRelationShip(int idUser1, int idUser2) {
    Connection connection = null;
    boolean endAction = false;
    try {
      connection = getConnection(false);
      relationShipDao.deleteRelationShip(connection, idUser1, idUser2);
      relationShipDao.deleteRelationShip(connection, idUser2, idUser1);
      connection.commit();
      endAction = true;
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.removeRelationShip", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return endAction;
  }

  /**
   * these two users in relationship
   * @param user1Id
   * @param user2Id
   * @return boolean
   * @throws SQLException
   */
  public boolean isInRelationShip(int user1Id, int user2Id) throws SQLException {
    Connection connection = null;
    boolean isInRelationShip = false;
    try {
      connection = getConnection(true);
      isInRelationShip = relationShipDao.isInRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.isInRelationShip", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return isInRelationShip;
  }

  /**
   * get all my RelationShips
   * @param myId
   * @return List<RelationShip>
   * @throws SQLException
   */
  public List<RelationShip> getAllMyRelationShips(int myId) throws SQLException {
    Connection connection = null;
    List<RelationShip> listMyRelation = new ArrayList<RelationShip>();
    try {
      connection = getConnection(true);
      listMyRelation = relationShipDao.getAllMyRelationShips(connection, myId);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getAllMyRelationShips", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return listMyRelation;
  }

  /**
   * get all my RelationShips ids
   * @param myId
   * @return List<String>
   * @throws SQLException
   */
  public List<String> getMyContactsIds(int myId) throws SQLException {
    Connection connection = null;
    List<String> myContactsIds = new ArrayList<String>();
    try {
      connection = getConnection(true);
      myContactsIds = relationShipDao.getMyContactsIds(connection, myId);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getMyContactsIds", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return myContactsIds;
  }

  /**
   * get all common contacts Ids between usre1 and user2
   * @param user1Id
   * @param user2Id
   * @return List<String>
   * @throws SQLException
   */
  public List<String> getAllCommonContactsIds(int user1Id, int user2Id) throws SQLException {
    Connection connection = null;
    List<String> myContactsIds = new ArrayList<String>();
    try {
      connection = getConnection(true);
      myContactsIds = relationShipDao.getAllCommonContactsIds(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getAllCommonContactsIds", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return myContactsIds;
  }

  /**
   * Get list of my socialInformationRelationShip (relationShips) according to number of Item and
   * the first Index
   * @param userId
   * @param begin date
   * @param end date
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  public List<SocialInformation> getAllMyRelationShips(String userId,
      Date begin, Date end) throws SQLException {
    Connection connection = null;
    try {
      connection = getConnection(true);
      return relationShipDao.getAllMyRelationShips(connection, userId, begin, end);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getAllMyRelationShips", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return new ArrayList<SocialInformation>();
  }

  /**
   * Get list socialInformationRelationShip (relationShips) of my Contacts according to number of
   * Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return List<SocialInformationRelationShip>
   * @throws SQLException
   */
  public List<SocialInformation> getAllRelationShipsOfMyContact(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SQLException {
    Connection connection = null;
    try {
      connection = getConnection(true);
      return relationShipDao.getAllRelationShipsOfMyContact(connection, myId, myContactsIds, begin,
          end);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getAllRelationShipsOfMyContact", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return new ArrayList<SocialInformation>();
  }

  /**
   * get RelationShip witch is between user1 and user2
   * @param user1Id
   * @param user2Id
   * @return RelationShip
   * @throws SQLException
   */
  public RelationShip getRelationShip(int user1Id, int user2Id) throws SQLException {
    Connection connection = null;
    RelationShip relation = null;
    try {
      connection = getConnection(true);
      relation = relationShipDao.getRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverTrace.error("org.silverpeas.core.socialnetwork.relationShip",
          "RelationShipService.getRelationShip", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return relation;
  }
}
