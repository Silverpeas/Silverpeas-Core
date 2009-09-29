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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.alertUser;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.util.PairObject;

public class AlertUser {
  protected String m_hostSpaceName;
  protected String m_hostComponentId;
  protected PairObject m_hostComponentName;
  protected NotificationMetaData m_notificationMetaData;

  public AlertUser() {
    resetAll();
  }

  public void resetAll() {
    m_hostSpaceName = "";
    m_hostComponentId = "";
    m_hostComponentName = new PairObject("", "");
  }

  static public String getAlertUserURL() {
    return "/RalertUserPeas/jsp/Main";
  }

  public void setHostSpaceName(String hostSpaceName) {
    if (hostSpaceName != null) {
      m_hostSpaceName = hostSpaceName;
    } else {
      m_hostSpaceName = "";
    }
  }

  public String getHostSpaceName() {
    return m_hostSpaceName;
  }

  public void setHostComponentId(String hostComponentId) {
    if (hostComponentId != null) {
      m_hostComponentId = hostComponentId;
    } else {
      m_hostComponentId = "";
    }
  }

  public String getHostComponentId() {
    return m_hostComponentId;
  }

  public void setHostComponentName(PairObject hostComponentName) {
    if (hostComponentName != null) {
      m_hostComponentName = hostComponentName;
    } else {
      m_hostComponentName = new PairObject("", "");
    }
  }

  public PairObject getHostComponentName() {
    return m_hostComponentName;
  }

  public void setNotificationMetaData(NotificationMetaData notificationMetaData) {
    if (notificationMetaData != null) {
      m_notificationMetaData = notificationMetaData;
    } else {
      m_notificationMetaData = new NotificationMetaData();
    }
  }

  public NotificationMetaData getNotificationMetaData() {
    return m_notificationMetaData;
  }
}
