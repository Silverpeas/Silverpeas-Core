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
package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;

public class LDAPTimeStampMSAD extends AbstractLDAPTimeStamp {
  long lTimeStamp;

  public LDAPTimeStampMSAD(LDAPSettings ds, String theValue) {
    driverSettings = ds;
    timeStamp = theValue;
    lTimeStamp = Long.parseLong(theValue);
  }

  public String toString() {
    return timeStamp;
  }

  public int compareTo(Object other) {
    if (lTimeStamp > ((LDAPTimeStampMSAD) other).lTimeStamp)
      return 1;
    else if (lTimeStamp < ((LDAPTimeStampMSAD) other).lTimeStamp)
      return -1;
    else
      return 0;
  }

  public void initFromServer(String lds, String baseDN, String filter,
      String fallbackSortBy) throws AdminException {
    SilverTrace.info("admin", "LDAPTimeStampMSAD.initFromServer()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPEntry[] theEntries = LDAPUtility.search1000Plus(lds, baseDN,
        driverSettings.getScope(), "(&(" + driverSettings.getTimeStampVar()
        + ">=" + timeStamp + ")" + filter + ")", driverSettings
        .getTimeStampVar(), driverSettings.getGroupAttributes());
    if (theEntries.length > 0) {
      // Problem is : the search1000Plus function sorts normaly by descending
      // order. BUT most LDAP server can't performs this type of order (like
      // Active Directory)
      // So, it may be ordered in the oposite way....
      long firstVal = Long.parseLong(LDAPUtility.getFirstAttributeValue(
          theEntries[0], driverSettings.getTimeStampVar()));
      long lastVal = Long.parseLong(LDAPUtility.getFirstAttributeValue(
          theEntries[theEntries.length - 1], driverSettings.getTimeStampVar()));
      if (firstVal >= lastVal) {
        lTimeStamp = firstVal;
      } else {
        lTimeStamp = lastVal;
      }
      timeStamp = Long.toString(lTimeStamp);
    }
  }
}
