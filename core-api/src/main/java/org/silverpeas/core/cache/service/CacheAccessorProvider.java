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
package org.silverpeas.core.cache.service;

import org.silverpeas.core.cache.model.SimpleCache;

/**
 * A provider of different kinds of cache accessors available in Silverpeas.
 * @author Yohann Chastagnier
 */
public class CacheAccessorProvider {

  private static final CacheAccessorProvider instance = new CacheAccessorProvider();
  private final CacheAccessor sessionCacheAccessor = new SessionCacheAccessor();
  private final CacheAccessor threadCacheAccessor = new ThreadCacheAccessor();
  private final CacheAccessor applicationCacheAccessor = new ApplicationCacheAccessor();

  /**
   * Initialization of service instances
   */
  private CacheAccessorProvider() {
    // Common cache is lazily initialized because resource locator needs CacheAccessorProvider
  }

  /**
   * Gets an instance of this cache accessor provider.
   * @return a {@link CacheAccessorProvider} instance.
   */
  private static CacheAccessorProvider getInstance() {
    return instance;
  }

  /**
   * Gets a useful volatile cache: after the end of the current thread execution, the associated
   * cache is trashed.
   * BE VERY VERY VERY CAREFULLY: into web application with thread pool management,
   * the thread is never killed and this cache is never cleared. If you want the cache cleared
   * after the end of the request, please use the {@link SimpleCache#clear()}} method of the
   * thread cache.
   * @return an accessor to a cache associated to the current thread.
   */
  public static CacheAccessor getThreadCacheAccessor() {
    return getInstance().threadCacheAccessor;
  }

  /**
   * Gets an accessor to a useful cache in relation with a session: after the end of the session,
   * the associated cache is trashed. If no session cache exists, then it is created and returned.
   * @return an accessor to a cache associated to the current session.
   */
  public static CacheAccessor getSessionCacheAccessor() {
    return getInstance().sessionCacheAccessor;
  }

  /**
   * Gets an accessor to the cache of the application.
   * @return an accessor to the application's cache.
   */
  public static CacheAccessor getApplicationCacheAccessor() {
    return getInstance().applicationCacheAccessor;
  }
}
