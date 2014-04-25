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
package org.silverpeas.cache.service;

import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * @author Yohann Chastagnier
 */
public class CacheServiceFactory {

  private static final CacheServiceFactory instance = new CacheServiceFactory();
  private final SimpleCacheService threadCacheService;
  private final SimpleCacheService requestCacheService;
  private final CacheService cacheService;

  /**
   * Initialization of service instances
   */
  private CacheServiceFactory() {

    // Thread cache
    threadCacheService = new ThreadCacheService();

    // Request cache
    requestCacheService = new ThreadCacheService();

    // Common cache
    int nbMaxElements = GeneralPropertiesManager.
        getInteger("application.cache.common.nbMaxElements", 0);
    if (nbMaxElements < 0) {
      nbMaxElements = 0;
    }
    cacheService = new EhCacheService(nbMaxElements);
  }

  /**
   * Gets an instance of this CacheServiceFactory class.
   * @return a CacheServiceFactory instance.
   */
  private static CacheServiceFactory getInstance() {
    return instance;
  }

  /**
   * Gets a useful volatile cache : after the end of the current thread execution, the associated
   * cache is trashed.
   * BE VERY VERE VERY CAREFULLY : into web application with thread pool management,
   * the thread is never killed and this cache is never cleared. If you want the cache cleared
   * after a end of request, please use {@link #getRequestCacheService()}.
   * @return a cache associated to the current thread
   */
  public static SimpleCacheService getThreadCacheService() {
    return getInstance().threadCacheService;
  }

  /**
   * Gets a useful cache in relation with a request : after the end of the request execution,
   * the associated cache is trashed.
   * @return a cache associated to the current request
   */
  public static SimpleCacheService getRequestCacheService() {
    return getInstance().requestCacheService;
  }

  /**
   * Gets the cache of the application.
   * @return an applicative cache ervice
   */
  public static CacheService getApplicationCacheService() {
    return getInstance().cacheService;
  }
}
