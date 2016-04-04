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

package org.silverpeas.core.web.tools.agenda.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Singleton;

@Singleton
public class CalendarImportSettingsDaoJdbc implements CalendarImportSettingsDao {

  /**
   * Get synchronisation user settings
   * @param userId Id of user whose settings belong to
   * @return CalendarImportSettings object containing user settings, null if no settings found
   * @see org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings
   */
  public CalendarImportSettings getUserSettings(String userId) {
    Connection connection = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    CalendarImportSettings settings = null;

    String query = "select * from sb_agenda_import_settings where userId = ?";

    try {
      connection = getConnection();
      st = connection.prepareStatement(query);
      st.setInt(1, Integer.parseInt(userId));
      rs = st.executeQuery();
      if (rs.next()) {
        settings = new CalendarImportSettings();
        settings.setUserId(rs.getInt("userId"));
        settings.setHostName(rs.getString("hostname"));
        settings.setSynchroType(rs.getInt("synchroType"));
        settings.setSynchroDelay(rs.getInt("synchroDelay"));
        settings.setUrlIcalendar(rs.getString("url"));
        settings.setLoginIcalendar(rs.getString("remoteLogin"));
        settings.setPwdIcalendar(rs.getString("remotePwd"));
        settings.setCharset(rs.getString("charset"));
      }
    } catch (Exception e) {
      SilverTrace.error("agenda", "CalendarImportSettingsDaoJdbc",
          "agenda.EX_CANT_GET_USER_SETTINGS", e);
    } finally {
      DBUtil.close(rs, st);
      close(connection);
    }

    return settings;
  }

  /**
   * Save synchronisation user settings
   * @param settings CalendarImportSettings object containing user settings
   * @see org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings
   */
  public void saveUserSettings(CalendarImportSettings settings)
      throws AgendaException {
    Connection connection = null;
    PreparedStatement st = null;
    ResultSet rs = null;

    String insertStatement =
        "insert into sb_agenda_import_settings (userId, hostName, synchroType, synchroDelay, url, remoteLogin, remotePwd, charset) values (?, ?, ?, ?, ?, ?, ?, ?)";

    try {
      connection = getConnection();
      st = connection.prepareStatement(insertStatement);
      st.setInt(1, settings.getUserId());
      st.setString(2, settings.getHostName());
      st.setInt(3, settings.getSynchroType());
      st.setInt(4, settings.getSynchroDelay());
      st.setString(5, settings.getUrlIcalendar());
      st.setString(6, settings.getLoginIcalendar());
      st.setString(7, settings.getPwdIcalendar());
      st.setString(8, settings.getCharset());
      st.executeUpdate();
    } catch (Exception e) {
      throw new AgendaException(
          "CalendarImportSettingsDaoJdbc.saveUserSettings",
          SilverpeasException.ERROR, "agenda.EX_CANT_SAVE_USER_SETTINGS",
          "user id = " + settings.getUserId(), e);
    } finally {
      DBUtil.close(rs, st);
      close(connection);
    }
  }

  /**
   * Update synchronisation user settings
   * @param settings CalendarImportSettings object containing user settings
   * @see org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings
   */
  public void updateUserSettings(CalendarImportSettings settings)
      throws AgendaException {
    Connection connection = null;
    PreparedStatement st = null;
    ResultSet rs = null;

    String updateStatement =
        "update sb_agenda_import_settings set hostName = ?, synchroType = ?, synchroDelay = ?, url= ?, remoteLogin= ?, remotePwd= ?, charset= ? where userId = ?";

    try {
      connection = getConnection();
      st = connection.prepareStatement(updateStatement);
      st.setString(1, settings.getHostName());
      st.setInt(2, settings.getSynchroType());
      st.setInt(3, settings.getSynchroDelay());
      st.setString(4, settings.getUrlIcalendar());
      st.setString(5, settings.getLoginIcalendar());
      st.setString(6, settings.getPwdIcalendar());
      st.setString(7, settings.getCharset());
      st.setInt(8, settings.getUserId());
      st.executeUpdate();
    } catch (Exception e) {
      throw new AgendaException(
          "CalendarImportSettingsDaoJdbc.updateUserSettings",
          SilverpeasException.ERROR, "agenda.EX_CANT_UPDATE_USER_SETTINGS",
          "user id = " + settings.getUserId(), e);
    } finally {
      DBUtil.close(rs, st);
      close(connection);
    }
  }

  // Fermeture des ressources
  protected void close(Connection connection) {
    if (connection != null)
      try {
        connection.close();
      } catch (SQLException e) {
      }
  }

  // Recuperation de la connection
  private Connection getConnection() throws Exception {
    return DBUtil.openConnection();
  }

}
