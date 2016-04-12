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

package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.service.AdminException;

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

    String[] ttv = { driverSettings.getTimeStampVar(), fallbackSortBy };

    LDAPEntry[] theEntries = LDAPUtility.search1000Plus(lds, baseDN,
        driverSettings.getScope(), "(&(" + driverSettings.getTimeStampVar()
        + ">=" + timeStamp + ")" + filter + ")", fallbackSortBy, ttv);


    String ttCurrent;
    for (LDAPEntry theEntry : theEntries) {
      ttCurrent = LDAPUtility.getFirstAttributeValue(theEntry, driverSettings.getTimeStampVar());
      if (ttCurrent.compareTo(timeStamp) > 0) {
        timeStamp = ttCurrent;
      }
    }
  }
}
