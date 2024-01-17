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

package org.silverpeas.core.web.admin.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
public class UIUserCacheTest {

  private static final String USER_ID = "26";
  private User aUser;
  private int nbUsersLoadFromPersistence = 0;

  @BeforeEach
  public void setup() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    aUser = mock(User.class);
    when(aUser.getId()).thenReturn(USER_ID);
    when(aUser.getFirstName()).thenReturn("Yo");
    when(aUser.getLastName()).thenReturn("Cha");
    when(UserProvider.get().getUser(anyString())).then(i -> {
      nbUsersLoadFromPersistence++;
      if (USER_ID.equals(i.getArgument(0))) {
        return aUser;
      }
      return null;
    });
    assertThat(nbUsersLoadFromPersistence, is(0));
  }

  @Test
  public void getUserShouldWork() {
    User user = UIUserCache.getById(USER_ID);
    assertThat(user, is(aUser));
    assertThat(nbUsersLoadFromPersistence, is(1));

    user = UIUserCache.getById(USER_ID);
    assertThat(user, is(aUser));
    assertThat(nbUsersLoadFromPersistence, is(1));

    // No cache use
    user = User.getById(USER_ID);
    assertThat(user, is(aUser));
    assertThat(nbUsersLoadFromPersistence, is(2));
  }

  @Test
  public void getUnknownUserShouldAccessPersistenceOnEachCall() {
    User user = UIUserCache.getById("unknown_id");
    assertThat(user, nullValue());
    assertThat(nbUsersLoadFromPersistence, is(1));

    user = UIUserCache.getById("unknown_id");
    assertThat(user, nullValue());
    assertThat(nbUsersLoadFromPersistence, is(2));
  }

  @Test
  public void getUserWithNullIdShouldNotAccessPersistence() {
    User user = UIUserCache.getById(null);
    assertThat(user, nullValue());
    assertThat(nbUsersLoadFromPersistence, is(0));
  }
}