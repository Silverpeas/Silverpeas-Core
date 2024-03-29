/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;

import java.util.HashMap;
import java.util.Map;

public class LDAPSynchroCache {
  protected Map<String, String> userSpecificIds = null;
  protected LDAPSettings driverSettings = null;

  public void init(LDAPSettings driverSettings) {
    this.driverSettings = driverSettings;
  }

  public void beginSynchronization() {
    if (driverSettings.isSynchroCacheEnabled()) {
      userSpecificIds = new HashMap<>();
    }
    // Else keep it to null
  }

  public void endSynchronization() {
    if (userSpecificIds != null) {
      userSpecificIds.clear();
      userSpecificIds = null;
    }
  }

  public void addUser(LDAPEntry userEntry) {
    if (userSpecificIds != null) {
      userSpecificIds.put(userEntry.getDN(), LDAPUtility
          .getFirstAttributeValue(userEntry, driverSettings.getUsersIdField()));
    }
  }

  public void addUser(String theDN, String theId) {
    if (userSpecificIds != null) {
      userSpecificIds.put(theDN, theId);
    }
  }

  public String getUserId(String theDN) {
    if (userSpecificIds != null) {
      return userSpecificIds.get(theDN);
    } else {
      return null;
    }
  }
}