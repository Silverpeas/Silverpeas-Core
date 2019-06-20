/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.cache.service;

import org.ehcache.config.ResourcePools;
import org.ehcache.config.ResourceType;
import org.ehcache.config.SizedResourcePool;
import org.ehcache.config.units.EntryUnit;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.Mutable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public class EhCacheTest {
  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  @Test
  public void testCacheInception() {
    EhCache cache = new EhCache(2L);

    ResourcePools resourcePools = cache.getCache().getRuntimeConfiguration().getResourcePools();
    assertThat(resourcePools.getResourceTypeSet().size(), is(1));

    SizedResourcePool pool = resourcePools.getPoolForResource(ResourceType.Core.HEAP);
    assertThat(pool, notNullValue());
    assertThat(pool.isPersistent(), is(false));
    assertThat(pool.getUnit(), is(EntryUnit.ENTRIES));
    assertThat(pool.getSize(), is(2L));
    assertThat(elementCountIn(cache), is(0));
  }

  @Test
  public void testClear() {
    EhCache cache = new EhCache(0);
    String key1 = cache.add(Object1);
    String key2 = cache.add(Object2);
    assertThat(elementCountIn(cache), is(2));
    assertThat(cache.getCache().get(key1).getObjectValue(), is(Object1));
    assertThat(cache.getCache().get(key2).getObjectValue(), is(Object2));

    cache.clear();
    assertThat(elementCountIn(cache), is(0));
    assertThat(cache.getCache().get(key1), nullValue());
    assertThat(cache.getCache().get(key1), nullValue());
  }

  @Test
  public void testGet() {
    EhCache cache = new EhCache(0);
    String uniqueKey1 = cache.add(Object1);
    assertThat(cache.get("dummy"), nullValue());
    assertThat(cache.get(uniqueKey1), is(Object1));
    assertThat(cache.get(uniqueKey1, Object.class), is(Object1));
    assertThat(cache.get(uniqueKey1, String.class), is(Object1));
    assertThat(cache.get(uniqueKey1, Number.class), nullValue());
  }

  @Test
  public void testAdd() {
    EhCache cache = new EhCache(0);
    String uniqueKey1 = cache.add(Object1);
    String uniqueKey2 = cache.add(Object2);
    assertThat(uniqueKey1, notNullValue());
    assertThat(uniqueKey2, notNullValue());
    assertThat(uniqueKey2, not(is(uniqueKey1)));
  }

  @Test
  public void testAddWithExplicitLiveExpiry() throws InterruptedException {
    final EhCache cache = new EhCache(0);
    String key1 = cache.add(Object1, 1);
    String key2 = cache.add(Object2, 5);

    Thread.sleep(1100L);
    assertThat(cache.get(key1), nullValue());
    assertThat(cache.get(key2), is(Object2));

    cache.clear();
    assertThat(elementCountIn(cache), is(0));
    key2 = cache.add(Object2, 5);
    Thread.sleep(5000L);
    assertThat(cache.get(key2), nullValue());
    assertThat(elementCountIn(cache), is(0));
  }

  @Test
  public void testAddWithSameKeyAndWithExplicitIdleExpiry() throws InterruptedException {
    final EhCache cache = new EhCache(0);
    String key = cache.add(Object1, 5, 1);
    assertWithExplicitIdleExpirency(key, cache);
  }

  @Test
  public void testPut() {
    EhCache cache = new EhCache(0);
    cache.put("A", Object1);
    cache.put("B", Object2);
    assertThat(cache.get("A"), is(Object1));
    assertThat(cache.get("B"), is(Object2));
  }

  @Test
  public void testPutWithSameKey() {
    EhCache cache = new EhCache(0);
    cache.put("A", Object1);
    cache.put("A", Object2);
    assertThat(cache.get("A"), is(Object2));
  }

  @Test
  public void testPutWithSameKeyAndWithExplicitLiveExpirency() throws InterruptedException {
    final EhCache cache = new EhCache(0);
    cache.put("A", Object1, 10);
    cache.put("A", Object2, 1);
    Thread.sleep(1100L);
    assertThat(elementCountIn(cache), is(0));
  }

  @Test
  public void testPutWithSameKeyAndWithExplicitIdleExpirency() throws InterruptedException {
    final EhCache cache = new EhCache(0);
    assertThat(elementCountIn(cache), is(0));
    cache.put("A", Object1, 5, 1);
    assertWithExplicitIdleExpirency("A", cache);
  }

  public void assertWithExplicitIdleExpirency(String key, EhCache cache)
      throws InterruptedException {
    for (int i = 0; i < 4; i++) {
      assertThat(elementCountIn(cache), is(1));
      cache.get(key);
      Thread.sleep(550L);
    }
    Thread.sleep(5000L - (4 * 550L));
    assertThat(elementCountIn(cache), is(0));
  }

  @Test
  public void testRemove() {
    EhCache cache = new EhCache(0);
    String uniqueKey1 = cache.add(Object1);
    String uniqueKey2 = cache.add(Object2);
    assertThat(elementCountIn(cache), is(2));
    cache.remove("lkjlkj");
    assertThat(elementCountIn(cache), is(2));
    cache.remove(uniqueKey1, Number.class);
    assertThat(elementCountIn(cache), is(2));
    cache.remove(uniqueKey1, Object.class);
    assertThat(elementCountIn(cache), is(1));
    cache.remove(uniqueKey2);
    assertThat(elementCountIn(cache), is(0));
  }

  /**
   * Be cautious: the count of elements in the cache triggers an access to it => this will impact
   * the lifetime of the accessed elements with TTL/TTI set.
   */
  private int elementCountIn(final EhCache cache) {
    Mutable<Integer> count = Mutable.of(0);
    cache.getCache().forEach(e -> count.set(count.get() + 1));
    return count.get();
  }
}
