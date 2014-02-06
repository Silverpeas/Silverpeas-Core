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
package org.silverpeas.cache.service.Handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.cache.service.SimpleCacheService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * User: Yohann Chastagnier
 * Date: 30/12/13
 */
public class CacheValueHandlerTest {

  private final static String PREFIX_CACHE_TEST_KEY =
      "org.silverpeas.cache.service.Handler.CacheValueHandlerTest$CacheValueHandlerTestExtends_";

  @Before
  @After
  public void setupAndTearDown() {
    CacheServiceFactory.getThreadCacheService().clear();
  }

  @Test
  public void testCacheIsEmpty() {
    assertThat(CacheServiceFactory.getThreadCacheService()
        .get(PREFIX_CACHE_TEST_KEY + "cacheKey", Boolean.class), nullValue());
    CacheValueHandlerTestExtends test = new CacheValueHandlerTestExtends();
    assertThat(test.defaultValue(), is(true));
    assertThat(test.getCacheKeySuffix(), is("cacheKey"));
    assertThat(test.get(), is(true));
    assertThat(CacheServiceFactory.getThreadCacheService()
        .get(PREFIX_CACHE_TEST_KEY + "cacheKey", Boolean.class), is(true));
  }

  @Test
  public void testCacheIsNotEmpty() {
    CacheServiceFactory.getThreadCacheService()
        .put(PREFIX_CACHE_TEST_KEY + "cacheKey", Boolean.FALSE);
    assertThat(CacheServiceFactory.getThreadCacheService()
        .get(PREFIX_CACHE_TEST_KEY + "cacheKey", Boolean.class), is(false));
    CacheValueHandlerTestExtends test = new CacheValueHandlerTestExtends();
    assertThat(test.defaultValue(), is(true));
    assertThat(test.getCacheKeySuffix(), is("cacheKey"));
    assertThat(test.get(), is(false));
  }

  /**
   * Test class.
   */
  private class CacheValueHandlerTestExtends extends CacheValueHandler<Boolean> {

    @Override
    protected SimpleCacheService getCacheService() {
      return CacheServiceFactory.getThreadCacheService();
    }

    @Override
    protected String getCacheKeySuffix() {
      return "cacheKey";
    }

    @Override
    protected Boolean defaultValue() {
      return Boolean.TRUE;
    }
  }
}
