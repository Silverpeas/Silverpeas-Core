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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class SecurityCache {

  // Cache name
  private static final String CACHE_NAME = "userIds";
  // Maximum elements in cache
  private static final int MAX_ELEMENTS = 1000000;
  // Use of the disk store
  private static final boolean USE_DISK_STORE = false;
  // Eternal cache elements
  private static final boolean ETERNAL_ELEMENTS = false;
  // Time to live for an element from its creation date (12h)
  private static final int TIME_TO_LIVE = 43200;
  // Time to live for an element from its last accessed or modified date (12h)
  private static final int TIME_TO_IDLE = 43200;

  // Manager allowing to access to the cache
  private CacheManager manager;

  public SecurityCache() {
    manager = new CacheManager();
    manager.addCache(new Cache(CACHE_NAME, MAX_ELEMENTS, USE_DISK_STORE,
        ETERNAL_ELEMENTS, TIME_TO_LIVE, TIME_TO_IDLE));
  }

  public void addData(String sessionId, String userId, String domainId) {
    getCache().put(new Element(sessionId, new SecurityData(userId, domainId)));
  }

  public SecurityData getData(String sessionId) {
    Element userElement = getCache().get(sessionId);
    if (userElement != null) {
      if (userElement.isExpired()) {
        getCache().remove(sessionId);
      } else {
        return (SecurityData) userElement.getObjectValue();
      }
    }
    return null;
  }

  public boolean isUserIdDefined(String sessionId) {
    Element userElement = getCache().get(sessionId);
    return (userElement != null && !userElement.isExpired());
  }

  private Cache getCache() {
    return manager.getCache(CACHE_NAME);
  }

}
