/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security;

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
 * @author Yohann Chastagnier
 */
public class SecurableRequestCacheTest {

  private static final String ENTITY_A_UUID = "entity_A_uuid";
  private static final String ENTITY_B_UUID = "entity_B_uuid";
  private static final User USER_26 = createUser("26");
  private static final User USER_38 = createUser("38");

  private List<String> authorizedSupplierCallCount;

  private static User createUser(String userId) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    return user;
  }

  @BeforeEach
  public void setup() {
    authorizedSupplierCallCount = new ArrayList<>();
  }

  @AfterEach
  public void clear() {
    getRequestCacheService().clearAllCaches();
  }

  @Test
  public void canBeAccessedByShouldBeCached() throws Exception {
    final String cacheKey = SecurableRequestCache
        .getCacheKey(USER_26, ENTITY_A_UUID, SecurableRequestCache.CAN_BE_ACCESSED_BY_KEY_SUFFIX);
    assertThat(getRequestCache().get(cacheKey), nullValue());
    for (int i = 0; i < 10; i++) {
      boolean canBeAccessedBy =
          SecurableRequestCache.canBeAccessedBy(USER_26, ENTITY_A_UUID, (u) -> {
            authorizedSupplierCallCount.add("ACCESSION CONTEXT CALL");
            return true;
          });
      assertThat(canBeAccessedBy, is(true));
      assertThat(getRequestCache().get(cacheKey), notNullValue());
    }
    assertThat(authorizedSupplierCallCount, hasSize(1));
  }

  @Test
  public void canBeModifiedByShouldBeCached() throws Exception {
    final String cacheKey = SecurableRequestCache
        .getCacheKey(USER_38, ENTITY_B_UUID, SecurableRequestCache.CAN_BE_MODIFIED_BY_KEY_SUFFIX);
    assertThat(getRequestCache().get(cacheKey), nullValue());
    for (int i = 0; i < 10; i++) {
      boolean canBeModifiedBy =
          SecurableRequestCache.canBeModifiedBy(USER_38, ENTITY_B_UUID, (u) -> {
            authorizedSupplierCallCount.add("MODIFICATION CONTEXT CALL");
            return false;
          });
      assertThat(canBeModifiedBy, is(false));
      assertThat(getRequestCache().get(cacheKey), notNullValue());
    }
    assertThat(authorizedSupplierCallCount, hasSize(1));
  }

  @Test
  public void canBeDeletedByShouldBeCached() throws Exception {
    final String cacheKey = SecurableRequestCache
        .getCacheKey(USER_26, ENTITY_A_UUID, SecurableRequestCache.CAN_BE_DELETED_BY_KEY_SUFFIX);
    assertThat(getRequestCache().get(cacheKey), nullValue());
    for (int i = 0; i < 10; i++) {
      boolean canBeDeletedBy = SecurableRequestCache.canBeDeletedBy(USER_26, ENTITY_A_UUID, (u) -> {
        authorizedSupplierCallCount.add("DELETION CONTEXT CALL");
        return false;
      });
      assertThat(canBeDeletedBy, is(false));
      assertThat(getRequestCache().get(cacheKey), notNullValue());
    }
    assertThat(authorizedSupplierCallCount, hasSize(1));
  }

  @Test
  public void canBeXxxByShouldBeCachedAndIsolated() throws Exception {
    callAllVerificationSeveralTimes(USER_26, ENTITY_A_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(3));
  }

  @Test
  public void clearShouldRemoveOnlyAimedData() throws Exception {
    // Not yet cached
    callAllVerificationSeveralTimes(USER_38, ENTITY_B_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(3));
    // Cache cleared
    SecurableRequestCache.clear(ENTITY_B_UUID);
    // Cache again
    callAllVerificationSeveralTimes(USER_38, ENTITY_B_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(6));
    // Call the clear about an other entity, no data cleared from cache
    SecurableRequestCache.clear(ENTITY_A_UUID);
    callAllVerificationSeveralTimes(USER_38, ENTITY_B_UUID);
    // So already cached
    assertThat(authorizedSupplierCallCount, hasSize(6));
    // Cache with an other user (same entity)
    callAllVerificationSeveralTimes(USER_26, ENTITY_B_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(9));
    // Cache 2 other cases (the 2 others are already cached
    callAllVerificationSeveralTimes(USER_38, ENTITY_A_UUID);
    callAllVerificationSeveralTimes(USER_26, ENTITY_A_UUID);
    callAllVerificationSeveralTimes(USER_38, ENTITY_B_UUID);
    callAllVerificationSeveralTimes(USER_26, ENTITY_B_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(15));
    // Clear cache about entity B
    SecurableRequestCache.clear(ENTITY_B_UUID);
    // Data about entity A have not been cleared otherwise authorizedSupplierCallCount size
    // should be 27 instead of 21
    callAllVerificationSeveralTimes(USER_38, ENTITY_A_UUID);
    callAllVerificationSeveralTimes(USER_26, ENTITY_A_UUID);
    callAllVerificationSeveralTimes(USER_38, ENTITY_B_UUID);
    callAllVerificationSeveralTimes(USER_26, ENTITY_B_UUID);
    assertThat(authorizedSupplierCallCount, hasSize(21));
  }

  private void callAllVerificationSeveralTimes(final User user, final String uuid) {
    final String suffix = user.getId() + "_" + uuid;
    for (int i = 0; i < 10; i++) {
      SecurableRequestCache.canBeAccessedBy(user, uuid, (u) -> {
        authorizedSupplierCallCount.add("ACCESSION CONTEXT CALL_" + suffix);
        return true;
      });
      SecurableRequestCache.canBeModifiedBy(user, uuid, (u) -> {
        authorizedSupplierCallCount.add("MODIFICATION CONTEXT CALL_" + suffix);
        return true;
      });
      SecurableRequestCache.canBeDeletedBy(user, uuid, (u) -> {
        authorizedSupplierCallCount.add("DELETION CONTEXT CALL_" + suffix);
        return true;
      });
    }
  }

  private SimpleCache getRequestCache() {
    return getRequestCacheService().getCache();
  }
}