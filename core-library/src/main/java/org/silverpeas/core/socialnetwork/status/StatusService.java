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

package org.silverpeas.core.socialnetwork.status;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.silverpeas.core.date.Date;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.UtilException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StatusService {

  @Inject
  private StatusDao statusDao;

  protected StatusService() {
  }

  /**
   * get Connection
   * @return Connection
   * @throws UtilException
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  /**
   * Change my Staus
   * @param status
   * @return String
   */
  public String changeStatusService(Status status) {
    Connection connection = null;
    int id = -1;
    try {
      connection = getConnection();
      id = statusDao.changeStatus(connection, status);
      if (id >= 0) {
        return status.getDescription();
      }
    } catch (Exception ex) {
      SilverTrace
          .error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.changeStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return null;
  }

  /**
   * delete my Status
   * @param id
   * @return boolean
   */
  public boolean deleteStatusService(int id) {
    Connection connection = null;
    boolean delete_status = false;
    try {
      connection = getConnection();
      delete_status = statusDao.deleteStatus(connection, id);
    } catch (Exception ex) {
      SilverTrace
          .error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.deleteStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return delete_status;
  }

  /**
   * get Status for user
   * @param id
   * @return Status
   */
  public Status getStatusService(int id) {
    Connection connection = null;
    Status status = new Status();
    try {
      connection = getConnection();
      status = statusDao.getStatus(connection, id);
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return status;
  }

  /**
   * get last status for user
   * @param userid
   * @return
   */
  public Status getLastStatusService(int userid) {
    Connection connection = null;
    Status status = new Status();
    try {
      connection = getConnection();
      status = statusDao.getLastStatus(connection, userid);
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getLastStatus", "",
          ex);
    } finally {
      DBUtil.close(connection);
    }
    return status;

  }

  /**
   * update status
   * @param status
   * @return boolean
   * @unused
   */
  public boolean updateStatusService(Status status) {
    Connection connection = null;
    boolean update_status = false;
    try {
      connection = getConnection();
      update_status = statusDao.updateStatus(connection, status);
    } catch (Exception ex) {
      SilverTrace
          .error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.UpdateStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return update_status;
  }

  /**
   * get all my SocialInformation according to number of Item and the first Index
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformationStatus>
   */
  public List<SocialInformation> getAllStatusService(int userId, Date begin, Date end) {
    Connection connection = null;
    try {
      connection = getConnection();
      return statusDao.getAllStatus(connection, userId, begin, end);
    } catch (Exception ex) {
      SilverTrace
          .error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getAllStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return Collections.emptyList();
  }

  /**
   * when data base is PostgreSQL get SocialInformation of my conatct according to number of Item
   * and the first Index
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformationStatus>
   */
  List<SocialInformation> getSocialInformationsListOfMyContacts(List<String> myContactsIds,
      Date begin, Date end) {
    Connection connection = null;
    try {
      connection = getConnection();
      return statusDao.getSocialInformationsListOfMyContacts(connection, myContactsIds, begin, end);
    } catch (Exception ex) {
      SilverTrace
          .error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getAllStatus", "", ex);
    } finally {
      DBUtil.close(connection);
    }
    return Collections.emptyList();
  }
}
