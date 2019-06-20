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
package org.silverpeas.core.socialnetwork.status;

import org.silverpeas.core.date.Date;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Singleton
public class StatusService {

  @Inject
  private StatusDao statusDao;

  protected StatusService() {
  }

  private Connection getConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  public String changeStatus(Status status) {
    Connection connection = null;
    int id = -1;
    try {
      connection = getConnection();
      id = statusDao.changeStatus(connection, status);
      if (id >= 0) {
        return status.getDescription();
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return null;
  }

  public Status getLastStatus(int userid) {
    Connection connection = null;
    Status status = new Status();
    try {
      connection = getConnection();
      status = statusDao.getLastStatus(connection, userid);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return status;

  }

  public List<SocialInformation> getAllStatus(int userId, Date begin, Date end) {
    Connection connection = null;
    try {
      connection = getConnection();
      return statusDao.getAllStatus(connection, userId, begin, end);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return Collections.emptyList();
  }

  List<SocialInformation> getSocialInformationListOfMyContacts(List<String> myContactsIds,
      Date begin, Date end) {
    Connection connection = null;
    try {
      connection = getConnection();
      return statusDao.getSocialInformationListOfMyContacts(connection, myContactsIds, begin, end);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    } finally {
      DBUtil.close(connection);
    }
    return Collections.emptyList();
  }
}