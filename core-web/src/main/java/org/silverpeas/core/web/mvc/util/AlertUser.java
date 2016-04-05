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

package org.silverpeas.core.web.mvc.util;

import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.util.Pair;

public class AlertUser {
  protected String m_hostSpaceName;
  protected String m_hostComponentId;
  protected Pair<String, String> m_hostComponentLabel;
  protected NotificationMetaData m_notificationMetaData;
  protected SelectionUsersGroups extraParams;

  public AlertUser() {
    resetAll();
  }

  public void resetAll() {
    m_hostSpaceName = "";
    m_hostComponentId = "";
    m_hostComponentLabel = new Pair<>("", "");
    extraParams = null;
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

  /**
   * Sets the component instance id that permits to apply a filter on the users or groups that have
   * right access to the component.
   * @param hostComponentId
   */
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

  /**
   * Sets the name of the component for the browsebar rendering. It is handled by a PairObject(component name, link_to_component).
   * Only the first element is represented for now because of the systematic use of a POPUP.
   * @param hostComponentLabel the component label
   */
  public void setHostComponentName(Pair<String, String> hostComponentLabel) {
    if (hostComponentLabel != null) {
      m_hostComponentLabel = hostComponentLabel;
    } else {
      m_hostComponentLabel = new Pair<>("", "");
    }
  }

  public Pair<String, String> getHostComponentName() {
    return m_hostComponentLabel;
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

  public SelectionUsersGroups getSelectionUsersGroups() {
    return extraParams;
  }

  public void setSelectionUsersGroups(SelectionUsersGroups extraParams) {
    this.extraParams = extraParams;
  }
}
