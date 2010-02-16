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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * NavigationStock.java
 */

package com.silverpeas.jobDomainPeas;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

/*
 * CVS Informations
 * 
 * $Id: NavigationStock.java,v 1.2 2004/09/28 12:45:27 neysseri Exp $
 * 
 * $Log: NavigationStock.java,v $
 * Revision 1.2  2004/09/28 12:45:27  neysseri
 * Extension de la longueur du login (de 20 a 50 caracteres) + nettoyage sources
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.2  2002/04/05 05:22:08  tleroi
 * no message
 *
 * Revision 1.1  2002/04/03 07:40:33  tleroi
 * no message
 *
 *
 */

/**
 * This class manage the informations needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A VALID GROUP (with Id, etc...)
 * @t.leroi
 */
public class NavigationStock extends Object {
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
    UserDetail[] valret = null;
    int i;
    
    return m_SubUsers;

    // Simple case : less than a page to display or display all
    /*if ((JobDomainSettings.m_UsersByPage == -1)
        || (m_SubUsers.length <= JobDomainSettings.m_UsersByPage)) {
      return m_SubUsers;
    }
    if (m_SubUsers.length <= (m_FirstDisplayedUser + JobDomainSettings.m_UsersByPage)) {
      valret = new UserDetail[m_SubUsers.length - m_FirstDisplayedUser];
    } else {
      valret = new UserDetail[JobDomainSettings.m_UsersByPage];
    }
    for (i = 0; i < valret.length; i++) {
      valret[i] = m_SubUsers[m_FirstDisplayedUser + i];
    }
    return valret;*/
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
    Group[] valret = null;
    int i;
    
    return m_SubGroups;

    // Simple case : less than a page to display or display all
    /*if ((JobDomainSettings.m_GroupsByPage == -1)
        || (m_SubGroups.length <= JobDomainSettings.m_GroupsByPage)) {
      return m_SubGroups;
    }
    if (m_SubGroups.length <= (m_FirstDisplayedGroup + JobDomainSettings.m_GroupsByPage)) {
      valret = new Group[m_SubGroups.length - m_FirstDisplayedGroup];
    } else {
      valret = new Group[JobDomainSettings.m_GroupsByPage];
    }
    for (i = 0; i < valret.length; i++) {
      valret[i] = m_SubGroups[m_FirstDisplayedGroup + i];
    }
    return valret;*/
  }
}
