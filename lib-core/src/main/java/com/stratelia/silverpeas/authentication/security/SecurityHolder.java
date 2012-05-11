/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.authentication.security;

import java.util.HashMap;

public class SecurityHolder {

  private static SecurityCache securityCache = new SecurityCache();
  private static HashMap<String, SecurityData> persistentCache =
      new HashMap<String, SecurityData>();

  public static void addData(String securityId, String userId, String domainId) {
    addData(securityId, userId, domainId, false);
  }

  public static void addData(String securityId, String userId, String domainId,
      boolean persistent) {
    if (persistent) {
      persistentCache.put(securityId, new SecurityData(userId, domainId));
    } else {
      securityCache.addData(securityId, userId, domainId);
    }
  }

  public static SecurityData getData(String securityId) {
    SecurityData securityData = securityCache.getData(securityId);
    if (securityData == null) {
      securityData = getPersistentData(securityId);
    }
    return securityData;
  }

  private static SecurityData getPersistentData(String securityId) {
    return persistentCache.get(securityId);
  }

}
