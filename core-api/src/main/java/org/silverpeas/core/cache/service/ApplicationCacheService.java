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
package org.silverpeas.core.cache.service;

import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.util.ResourceLocator;

/**
 * A service to handle a cache whose lifetime is over the whole application runtime.
 * @author mmoquillon
 */
public class ApplicationCacheService implements CacheService {

  private Cache cache;

  protected ApplicationCacheService() {

  }

  @Override
  public Cache getCache() {
    if (cache == null) {
      int nbMaxElements = ResourceLocator.getGeneralSettingBundle().
          getInteger("application.cache.common.nbMaxElements", 0);
      if (nbMaxElements < 0) {
        nbMaxElements = 0;
      }
      cache = new EhCache(nbMaxElements);
    }
    return cache;
  }

  @Override
  public void clearAllCaches() {
    cache.clear();
  }
}
