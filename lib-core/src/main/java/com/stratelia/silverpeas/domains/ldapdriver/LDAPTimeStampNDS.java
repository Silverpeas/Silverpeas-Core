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

public class LDAPTimeStampNDS extends AbstractLDAPTimeStamp {
  public LDAPTimeStampNDS(LDAPSettings ds, String theValue) {
    driverSettings = ds;
    timeStamp = theValue;
  }

  public String toString() {
    return timeStamp;
  }

  public int compareTo(Object other) {
    return timeStamp.compareTo(((LDAPTimeStampNDS) other).timeStamp);
  }

  public void initFromServer(String lds, String baseDN, String filter,
      String fallbackSortBy) throws AdminException {
    SilverTrace.info("admin", "LDAPTimeStampNDS.initFromServer()",
        "root.MSG_GEN_ENTER_METHOD");
    String[] ttv = { driverSettings.getTimeStampVar(), fallbackSortBy };

    LDAPEntry[] theEntries = LDAPUtility.search1000Plus(lds, baseDN,
        driverSettings.getScope(), "(&(" + driverSettings.getTimeStampVar()
        + ">=" + timeStamp + ")" + filter + ")", fallbackSortBy, ttv);
    SilverTrace.info("admin", "LDAPTimeStampNDS.initFromServer()",
        "root.MSG_GEN_PARAM_VALUE", "# entries = " + theEntries.length);

    String ttCurrent = "";
    for (int i = 0; i < theEntries.length; i++) {
      ttCurrent = LDAPUtility.getFirstAttributeValue(theEntries[i],
          driverSettings.getTimeStampVar());
      if (ttCurrent.compareTo(timeStamp) > 0)
        timeStamp = ttCurrent;
    }
  }
}
