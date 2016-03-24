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

import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public class EhCacheServiceTest {
  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  @Before
  public void setup() {
    Cache cache = new EhCacheService(10).getCache();
    for (Object key : cache.getKeys()) {
      cache.remove(key);
    }
    assertThat(cache.getKeysWithExpiryCheck().size(), is(0));
    assertThat(cache.getCacheConfiguration().getName(), is("SILVERPEAS_COMMON_EH_CACHE"));
    assertThat(cache.getCacheConfiguration().isEternal(), is(false));
    assertThat(cache.getCacheConfiguration().getMaxEntriesLocalHeap(), is(10L));
    assertThat(cache.getCacheConfiguration().getMaxBytesLocalDisk(), is(0L));
    assertThat(cache.getCacheConfiguration().getPersistenceConfiguration(), nullValue());
    assertThat(cache.getCacheConfiguration().getTimeToIdleSeconds(), is(0L));
    assertThat(cache.getCacheConfiguration().getTimeToLiveSeconds(), is(0L));
    assertThat(cache.getCacheConfiguration().isMaxBytesLocalDiskPercentageSet(), is(false));
    assertThat(cache.getCacheConfiguration().isMaxBytesLocalOffHeapPercentageSet(), is(false));
    assertThat(cache.getCacheConfiguration().getMemoryStoreEvictionPolicy(),
        is(MemoryStoreEvictionPolicy.LRU));
  }

  @Test
  public void testClear() {
    EhCacheService service = new EhCacheService(0);
    service.add(Object1);
    service.add(Object2);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
    service.clear();
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
  }

  @Test
  public void testGet() {
    EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    String uniqueKey1 = service.add(Object1);
    assertThat(service.get("dummy"), nullValue());
    assertThat(service.get(uniqueKey1), is((Object) Object1));
    assertThat(service.get(uniqueKey1, Object.class), is((Object) Object1));
    assertThat(service.get(uniqueKey1, String.class), is(Object1));
    assertThat(service.get(uniqueKey1, Number.class), nullValue());
  }

  @Test
  public void testAdd() {
    EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    String uniqueKey1 = service.add(Object1);
    String uniqueKey2 = service.add(Object2);
    assertThat(uniqueKey1, notNullValue());
    assertThat(uniqueKey2, notNullValue());
    assertThat(uniqueKey2, not(is(uniqueKey1)));
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
  }

  @Test
  public void testAddWithExplicitLiveExpirency() throws InterruptedException {
    final EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.add(Object1, 1);
    service.add(Object2, 10);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
    Thread.sleep(1100L);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    Thread.sleep(5000L);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    Thread.sleep(5000L);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
  }

  @Test
  public void testAddWithSameKeyAndWithExplicitIdleExpirency() throws InterruptedException {
    final EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.add(Object1, 10, 1);
    assertWithExplicitIdleExpirency(service);
  }

  @Test
  public void testPut() {
    EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.put("A", Object1);
    service.put("B", Object2);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
  }

  @Test
  public void testPutWithSameKey() {
    EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.put("A", Object1);
    service.put("A", Object2);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
  }

  @Test
  public void testPutWithSameKeyAndWithExplicitLiveExpirency() throws InterruptedException {
    final EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.put("A", Object1, 10);
    service.put("A", Object2, 1);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    Thread.sleep(1100L);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
  }

  @Test
  public void testPutWithSameKeyAndWithExplicitIdleExpirency() throws InterruptedException {
    final EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    service.put("A", Object1, 10, 1);
    assertWithExplicitIdleExpirency(service);
  }

  public void assertWithExplicitIdleExpirency(EhCacheService service) throws InterruptedException {
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    String key = (String) service.getCache().getKeys().get(0);
    for (int i = 0; i < 4; i++) {
      service.get(key);
      Thread.sleep(550L);
      assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    }
    Thread.sleep(1100L);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
  }

  @Test
  public void testRemove() {
    EhCacheService service = new EhCacheService(0);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
    String uniqueKey1 = service.add(Object1);
    String uniqueKey2 = service.add(Object2);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
    service.remove("lkjlkj");
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
    service.remove(uniqueKey1, Number.class);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(2));
    service.remove(uniqueKey1, Object.class);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(1));
    service.remove(uniqueKey2);
    assertThat(service.getCache().getKeysWithExpiryCheck().size(), is(0));
  }
}
