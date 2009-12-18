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

/*
 * JobDomainSettings.java
 */

package com.silverpeas.jobDomainPeas;

import java.util.Arrays;
import java.util.Comparator;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/*
 * CVS Informations
 *
 * $Id: JobDomainSettings.java,v 1.5 2008/03/12 16:42:46 neysseri Exp $
 *
 * $Log: JobDomainSettings.java,v $
 * Revision 1.5  2008/03/12 16:42:46  neysseri
 * no message
 *
 * Revision 1.4.4.1  2007/12/17 07:44:24  neysseri
 * no message
 *
 * Revision 1.4  2007/04/17 09:42:12  neysseri
 * Dév FNMJ/Ganesha
 *
 * Revision 1.3.10.1  2007/03/16 15:44:10  cbonin
 * *** empty log message ***
 *
 * Revision 1.3  2004/09/28 12:45:27  neysseri
 * Extension de la longueur du login (de 20 à 50 caractères) + nettoyage sources
 *
 * Revision 1.2  2003/12/05 15:02:41  tleroi
 * Go to jikes 1.15
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.2  2002/04/02 14:34:01  tleroi
 * no message
 *
 * Revision 1.1  2002/03/27 11:22:22  tleroi
 * no message
 *
 *
 *
 */

/**
 * This class manage the informations needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A VALID GROUP (with Id, etc...)
 * @t.leroi
 */
public class JobDomainSettings extends SilverpeasSettings {
  public static int m_UsersByPage = 10;
  public static int m_GroupsByPage = 10;

  public static int m_MinLengthLogin = 5;
  public static int m_MinLengthPwd = 4;
  public static boolean m_BlanksAllowedInPwd = true;
  public static boolean m_UserAddingAllowedForGroupManagers = false;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings", "");

    m_UsersByPage = readInt(rs, "UsersByPage", 10);
    m_GroupsByPage = readInt(rs, "GroupsByPage", 10);
    m_MinLengthLogin = readInt(rs, "MinLengthLogin", 5);
    m_MinLengthPwd = readInt(rs, "MinLengthPwd", 4);
    m_BlanksAllowedInPwd = readBoolean(rs, "BlanksAllowedInPwd", true);
    m_UserAddingAllowedForGroupManagers = readBoolean(rs,
        "UserAddingAllowedForGroupManagers", false);
  }

  static public void sortGroups(Group[] toSort) {
    Arrays.sort(toSort, new Comparator() {
        public int compare(Object o1, Object o2) {
        return ((Group) o1).compareTo((Group) o2);
        }

      public boolean equals(Object o) {
        return false;
        }

    });
  }

  static public void sortUsers(UserDetail[] toSort) {
    Arrays.sort(toSort, new Comparator() {
        public int compare(Object o1, Object o2) {
        return ((UserDetail) o1).compareTo((UserDetail) o2);
        }

      public boolean equals(Object o) {
        return false;
        }

    });
  }
}
