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
package com.silverpeas.socialNetwork.status;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;

public class StatusService {

  private StatusDao statusDao;

  public StatusService() {
    this.statusDao = new StatusDao();
  }

  private Connection getConnection() throws UtilException, SQLException {
    Connection connection = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    connection.setAutoCommit(false);
    return connection;
  }

  public String changeStatusService(Status status) {
    Connection connection = null;
    int id = -1;
    try {
      connection = getConnection();
      id = this.statusDao.changeStatus(connection, status);
      connection.commit();
      if (id >= 0) {
        return status.getDescription();
      }
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.changeStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return null;
  }

  public boolean deleteStatusService(int id) {
    Connection connection = null;
    boolean delete_status = false;
    try {
      connection = getConnection();
      delete_status = this.statusDao.deleteStatus(connection, id);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.deleteStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return delete_status;
  }

  public Status getStatusService(int id) {
    Connection connection = null;
    Status status = new Status();
    try {
      connection = getConnection();
      status = this.statusDao.getStatus(connection, id);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return status;
  }

  public Status getLastStatusService(int userid) {
    Connection connection = null;
    Status status = new Status();
    try {
      connection = getConnection();
      status = this.statusDao.getLastStatus(connection, userid);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.getLastStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return status;

  }

  public boolean UpdateStatusService(Status status) {
    Connection connection = null;
    boolean update_status = false;
    try {
      connection = getConnection();
      update_status = this.statusDao.UpdateStatus(connection, status);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status", "StatusService.UpdateStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return update_status;
  }

  public List<SocialInformationStatus> getAllStatusService(int userId, int nbElement, int firstIndex) {
    Connection connection = null;
    List<SocialInformationStatus> status_list = new ArrayList<SocialInformationStatus>();

    try {
      connection = getConnection();
      status_list = this.statusDao.getAllStatus(connection, userId, nbElement, firstIndex);
      connection.commit();
    } catch (Exception ex) {
      SilverTrace.error("Silverpeas.Bus.SocialNetwork.Status",
          "StatusService.getAllStatus", "",
          ex);
      DBUtil.rollback(connection);
    } finally {
      DBUtil.close(connection);
    }
    return status_list;
  }
}
