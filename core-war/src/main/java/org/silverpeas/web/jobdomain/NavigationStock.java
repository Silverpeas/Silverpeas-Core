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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * NavigationStock.java
 */

package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * This class manage the informations needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A VALID GROUP (with Id, etc...)
 * @t.leroi
 */
public class NavigationStock {
  Group[] m_SubGroups = null;
  UserDetail[] m_SubUsers = null;
  int m_FirstDisplayedUser = 0;
  int m_FirstDisplayedGroup = 0;
  AdminController m_adc = null;

  public NavigationStock(AdminController adc) {
    m_adc = adc;
    m_FirstDisplayedUser = 0;
    m_FirstDisplayedGroup = 0;
  }

  protected void verifIndexes() {
    if (m_SubUsers.length <= m_FirstDisplayedUser) {
      if (m_SubUsers.length > 0) {
        m_FirstDisplayedUser = m_SubUsers.length - 1;
      } else {
        m_FirstDisplayedUser = 0;
      }
    }
    if (m_SubGroups.length <= m_FirstDisplayedGroup) {
      if (m_SubGroups.length > 0) {
        m_FirstDisplayedGroup = m_SubGroups.length - 1;
      } else {
        m_FirstDisplayedGroup = 0;
      }
    }
  }

  // SubUsers functions

  public void nextUserPage() {
    if ((JobDomainSettings.m_UsersByPage != -1)
        && (m_SubUsers.length > (m_FirstDisplayedUser + JobDomainSettings.m_UsersByPage))) {
      m_FirstDisplayedUser += JobDomainSettings.m_UsersByPage;
    }
  }

  public void previousUserPage() {
    if ((JobDomainSettings.m_UsersByPage != -1) && (m_FirstDisplayedUser > 0)) {
      if (m_FirstDisplayedUser >= JobDomainSettings.m_UsersByPage) {
        m_FirstDisplayedUser -= JobDomainSettings.m_UsersByPage;
      } else {
        m_FirstDisplayedUser = 0;
      }
    }
  }

  public boolean isFirstUserPage() {
    if (JobDomainSettings.m_UsersByPage == -1) {
      return true;
    }
    return (m_FirstDisplayedUser == 0);
  }

  public boolean isLastUserPage() {
    if (JobDomainSettings.m_UsersByPage == -1) {
      return true;
    }
    return (m_SubUsers.length <= (m_FirstDisplayedUser + JobDomainSettings.m_UsersByPage));
  }

  public UserDetail[] getAllUserPage() {
    return m_SubUsers;
  }

  public UserDetail[] getUserPage() {
    return m_SubUsers;
  }

  // SubGroups functions

  public void nextGroupPage() {
    if ((JobDomainSettings.m_GroupsByPage != -1)
        && (m_SubGroups.length > (m_FirstDisplayedGroup + JobDomainSettings.m_GroupsByPage))) {
      m_FirstDisplayedGroup += JobDomainSettings.m_GroupsByPage;
    }
  }

  public void previousGroupPage() {
    if ((JobDomainSettings.m_GroupsByPage != -1) && (m_FirstDisplayedGroup > 0)) {
      if (m_FirstDisplayedGroup >= JobDomainSettings.m_GroupsByPage) {
        m_FirstDisplayedGroup -= JobDomainSettings.m_GroupsByPage;
      } else {
        m_FirstDisplayedGroup = 0;
      }
    }
  }

  public boolean isFirstGroupPage() {
    return (m_FirstDisplayedGroup == 0);
  }

  public boolean isLastGroupPage() {
    if (JobDomainSettings.m_GroupsByPage == -1) {
      return true;
    }
    return (m_SubGroups.length <= (m_FirstDisplayedGroup + JobDomainSettings.m_GroupsByPage));
  }

  public Group[] getAllGroupPage() {
    return m_SubGroups;
  }

  public Group[] getGroupPage() {
    return m_SubGroups;
  }
}
