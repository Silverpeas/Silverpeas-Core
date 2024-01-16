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

package org.silverpeas.core.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;

/**
 * @author silveryocha
 */
class RequestCacheSupplierTest {

  private static final String CACHE_KEY_A = "cache_key_a";
  private static final String CACHE_KEY_B = "cache_key_b";
  private static final String CACHE_KEY_NONE = "cache_key_none";
  private static final User USER_26 = createUser("26");
  private static final User USER_38 = createUser("38");

  private List<String> supplierCallCount;

  private static User createUser(String userId) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    return user;
  }

  @BeforeEach
  void setup() {
    supplierCallCount = new ArrayList<>();
  }

  @AfterEach
  void clear() {
    getRequestCacheService().clearAllCaches();
  }

  @Test
  void shouldBeCached() {
    assertThat(getRequestCache().get(CACHE_KEY_A), nullValue());
    for (int i = 0; i < 10; i++) {
      final User user26 = getRequestCache().computeIfAbsent(CACHE_KEY_A, User.class, () -> {
        supplierCallCount.add("CALL");
        return USER_26;
      });
      assertThat(user26, is(USER_26));
      assertThat(getRequestCache().get(CACHE_KEY_A), is(USER_26));
    }
    assertThat(supplierCallCount, hasSize(1));
  }

  @Test
  void shouldBeCachedAndIsolated() {
    callAllSeveralTimes();
    assertThat(supplierCallCount, hasSize(2));
  }

  @Test
  void clearShouldRemoveOnlyAimedData() {
    // Not yet cached
    callAllSeveralTimes();
    assertThat(supplierCallCount, hasSize(2));
    // Cache cleared
    getRequestCache().remove(CACHE_KEY_B);
    // Cache again
    callAllSeveralTimes();
    assertThat(supplierCallCount, hasSize(3));
    // Call the clear about an other entity, no data cleared from cache
    getRequestCache().remove(CACHE_KEY_NONE);
    callAllSeveralTimes();
    // So already cached
    assertThat(supplierCallCount, hasSize(3));
    // Already cached
    callAllSeveralTimes();
    callAllSeveralTimes();
    callAllSeveralTimes();
    callAllSeveralTimes();
    callAllSeveralTimes();
    assertThat(supplierCallCount, hasSize(3));
    // Clear all
    getRequestCache().remove(CACHE_KEY_A);
    getRequestCache().remove(CACHE_KEY_B);
    callAllSeveralTimes();
    callAllSeveralTimes();
    callAllSeveralTimes();
    callAllSeveralTimes();
    assertThat(supplierCallCount, hasSize(5));
  }

  private void callAllSeveralTimes() {
    for (int i = 0; i < 10; i++) {
      getRequestCache().computeIfAbsent(CACHE_KEY_A, User.class, () -> {
        supplierCallCount.add("GET USER_26");
        return USER_26;
      });
      getRequestCache().computeIfAbsent(CACHE_KEY_B, User.class, () -> {
        supplierCallCount.add("GET USER_38");
        return USER_38;
      });
    }
  }

  private SimpleCache getRequestCache() {
    return getRequestCacheService().getCache();
  }
}