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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RelationShipService {

  private final RelationShipDao relationShipDao;

  public RelationShipService() {
    relationShipDao = new RelationShipDao();
  }

  public Connection getConnection() throws UtilException, SQLException {
    Connection connection = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    connection.setAutoCommit(false);
    return connection;
  }
  /*
   * remove RelationShip (if this relationShips is deleted return true)
   * @return boolean
   * @param: RelationShip relationShip
   *
   */

  public boolean removeRelationShip(int idUser1, int idUser2) {
    Connection connection = null;
    boolean endAction = false;
    try {
      connection = getConnection();
      relationShipDao.deleteRelationShip(connection, idUser1, idUser2);
      relationShipDao.deleteRelationShip(connection, idUser2, idUser1);
      connection.commit();
      endAction = true;
    } catch (Exception ex) {
      SilverTrace.error("com.silverpeas.socialNetwork.relationShip",
          "RelationShipService.ignoreremoveRelationShipInvitation", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return endAction;
  }
  /*
   * these two users in relationship
   * @return:boolean
   * @param: int user1Id, int user2Id
   *
   */
  public boolean isInRelationShip(int user1Id, int user2Id) throws SQLException {
    Connection connection = null;
    boolean isInRelationShip = false;
    try {
      connection = getConnection();
     isInRelationShip= relationShipDao.isInRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverTrace.error("com.silverpeas.socialNetwork.relationShip",
          "RelationShipService.isInRelationShip", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return isInRelationShip;
  }
  /*
   * get all my RelationShips
   * @return:List<RelationShip>
   * @param: int myId
   *
   */

  public List<RelationShip> getAllMyRelationShips(int myId) throws SQLException {
    Connection connection = null;
    List<RelationShip> listMyRelation = new ArrayList<RelationShip>();
    try {
      connection = getConnection();
      listMyRelation = relationShipDao.getAllMyRelationShips(connection, myId);
    } catch (Exception ex) {
      SilverTrace.error("com.silverpeas.socialNetwork.relationShip",
          "RelationShipService.isInRelationShip", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return listMyRelation;
  }
  /*
   * get RelationShip
   * @return:RelationShip
   * @param: int user1Id, int user2Id
   *
   */

  public RelationShip getRelationShip(int user1Id, int user2Id) throws SQLException {
    Connection connection = null;
    RelationShip relation = null;
    try {
      connection = getConnection();
      relation = relationShipDao.getRelationShip(connection, user1Id, user2Id);
    } catch (Exception ex) {
      SilverTrace.error("com.silverpeas.socialNetwork.relationShip",
          "RelationShipService.isInRelationShip", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return relation;
  }
}

