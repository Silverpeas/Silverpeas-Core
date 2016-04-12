/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.cache.service;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getSessionCacheService;

/**
 * @author Yohann Chastagnier
 */
public class VolatileCacheServiceProvider {

  /**
   * Hidden constructor
   */
  private VolatileCacheServiceProvider() {
  }

  /**
   * Gets the volatile resource cache linked to the current user session.<br/>
   * For example (and for now), this cache permits to handle attachments linked to a contribution
   * not yet persisted. In case of the user does not persist its contribution, all resources
   * linked to a "volatile" contribution (attachments for the example) are automatically deleted
   * on the user session closing.
   * @return the volatile resource cache linked to the current user session.
   */
  public static VolatileResourceCacheService getSessionVolatileResourceCacheService() {
    VolatileResourceCacheService volatileResourceCacheService = getSessionCacheService()
        .get(VolatileResourceCacheService.class.getName(), VolatileResourceCacheService.class);
    if (volatileResourceCacheService == null) {
      volatileResourceCacheService = new VolatileResourceCacheService();
      getSessionCacheService()
          .put(VolatileResourceCacheService.class.getName(), volatileResourceCacheService);
    }
    return volatileResourceCacheService;
  }
}
