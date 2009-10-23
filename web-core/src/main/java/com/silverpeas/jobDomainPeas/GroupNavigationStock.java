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
 * GroupNavigationStock.java
 */

package com.silverpeas.jobDomainPeas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

/*
 * CVS Informations
 * 
 * $Id: GroupNavigationStock.java,v 1.3 2008/03/12 16:42:46 neysseri Exp $
 * 
 * $Log: GroupNavigationStock.java,v $
 * Revision 1.3  2008/03/12 16:42:46  neysseri
 * no message
 *
 * Revision 1.2.16.3  2007/12/13 15:27:59  neysseri
 * no message
 *
 * Revision 1.2.16.2  2007/12/12 16:39:12  neysseri
 * no message
 *
 * Revision 1.2.16.1  2007/12/11 15:32:08  neysseri
 * no message
 *
 * Revision 1.2  2004/09/28 12:45:27  neysseri
 * Extension de la longueur du login (de 20 a 50 caracteres) + nettoyage sources
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.5  2002/04/05 05:22:08  tleroi
 * no message
 *
 * Revision 1.4  2002/04/03 07:40:33  tleroi
 * no message
 *
 * Revision 1.3  2002/04/02 14:34:01  tleroi
 * no message
 *
 * Revision 1.1  2002/03/27 11:22:22  tleroi
 * no message
 *
 *
 *
 */

/**
 * This class manage the informations needed for groups navigation and browse
 * 
 * PRE-REQUIRED : the Group passed in the constructor MUST BE A VALID GROUP
 * (with Id, etc...)
 * 
 * @t.leroi
 */
public class GroupNavigationStock extends NavigationStock {
  Group m_NavGroup = null;
  String m_GroupId = null;
  List manageableGroupIds = null;

  public GroupNavigationStock(String navGroup, AdminController adc,
      List manageableGroupIds) {
    super(adc);
    m_GroupId = navGroup;
    this.manageableGroupIds = manageableGroupIds;
    refresh();
  }

  public void refresh() {
    String[] subUsersIds = null;
    String[] subGroupsIds = null;
    int i;

    m_NavGroup = m_adc.getGroupById(m_GroupId);
    subUsersIds = m_NavGroup.getUserIds();
    if (subUsersIds == null) {
      m_SubUsers = new UserDetail[0];
    } else {
      m_SubUsers = m_adc.getUserDetails(subUsersIds);
    }
    JobDomainSettings.sortUsers(m_SubUsers);

    subGroupsIds = m_adc.getAllSubGroupIds(m_NavGroup.getId());
    if (subGroupsIds == null) {
      m_SubGroups = new Group[0];
    } else {
      if (manageableGroupIds != null)
        subGroupsIds = filterGroupsToGroupManager(subGroupsIds);

      m_SubGroups = new Group[subGroupsIds.length];
      for (i = 0; i < subGroupsIds.length; i++) {
        m_SubGroups[i] = m_adc.getGroupById(subGroupsIds[i]);
      }
    }
    JobDomainSettings.sortGroups(m_SubGroups);
    verifIndexes();
  }

  private String[] filterGroupsToGroupManager(String[] groupIds) {
    // get all manageable groups by current user
    Iterator itManageableGroupsIds = null;

    List temp = new ArrayList();

    // filter groups
    String groupId = null;
    for (int g = 0; g < groupIds.length; g++) {
      groupId = groupIds[g];

      if (manageableGroupIds.contains(groupId))
        temp.add(groupId);
      else {
        // get all subGroups of group
        List subGroupIds = Arrays.asList(m_adc
            .getAllSubGroupIdsRecursively(groupId));

        // check if at least one manageable group is part of subGroupIds
        itManageableGroupsIds = manageableGroupIds.iterator();

        String manageableGroupId = null;
        boolean find = false;
        while (!find && itManageableGroupsIds.hasNext()) {
          manageableGroupId = (String) itManageableGroupsIds.next();
          if (subGroupIds.contains(manageableGroupId))
            find = true;
        }

        if (find)
          temp.add(groupId);
      }
    }

    return (String[]) temp.toArray(new String[0]);
  }

  public boolean isThisGroup(String grId) {
    if (StringUtil.isDefined(grId)) {
      return (grId.equals(m_NavGroup.getId()));
    } else {
      return (isGroupValid(m_NavGroup) == false);
    }
  }

  public Group getThisGroup() {
    return m_NavGroup;
  }

  static public boolean isGroupValid(Group gr) {
    if (gr != null && StringUtil.isDefined(gr.getId())) {
      return true;
    }
    return false;
  }
}
