/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.test.extension.RequesterProvider;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the use of the {@link SelectionBasket}
 * @author mmoquillon
 */
@EnableSilverTestEnv
class SelectionBasketTest {

  @RequesterProvider
  public User currentRequester() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("42");
    return user;
  }

  @BeforeEach
  void prepareSessionCache() {
    User user = User.getCurrentRequester();
    SessionCacheService cacheService =
        (SessionCacheService) CacheServiceProvider.getSessionCacheService();
    cacheService.newSessionCache(user);
  }

  @Test
  @DisplayName("A resource put in the basket should be in the basket")
  void putAResourceInBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(1);
    basket.put(contribution);

    assertThat(basket.isEmpty(), is(false));
    assertThat(basket.count(), is(1));
    assertThat(basket.getAt(0).isPresent(), is(true));
    assertThat(basket.getAt(0).get().getResource(), is(contribution));
  }

  @Test
  @DisplayName("A resource put a second time in the basket should be only one time in the basket")
  void putASecondTimeAResourceInBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(1);
    basket.put(contribution);
    assertThat(basket.count(), is(1));

    basket.put(contribution);

    assertThat(basket.count(), is(1));
    assertThat(basket.getAt(0).isPresent(), is(true));
    assertThat(basket.getAt(0).get().getResource(), is(contribution));
  }

  @Test
  @DisplayName("A resource put a second time in the basket should move it at the head of the basket")
  void putASecondTimeAResourceInBasketShouldMoveIt() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(1);
    MyContribution lastContribution = getContribution(3);
    basket.put(contribution);
    basket.put(getContribution(2));
    basket.put(lastContribution);
    assertThat(basket.count(), is(3));
    assertThat(basket.getAt(0).isPresent(), is(true));
    assertThat(basket.getAt(0).get().getResource(), is(lastContribution));

    basket.put(contribution);

    assertThat(basket.count(), is(3));
    assertThat(basket.getAt(0).isPresent(), is(true));
    assertThat(basket.getAt(0).get().getResource(), is(contribution));
    assertThat(basket.getAt(1).isPresent(), is(true));
    assertThat(basket.getAt(1).get().getResource(), is(lastContribution));
  }

  @Test
  @DisplayName("A resource that is popped shouldn't be anymore in the basket")
  void popAResourceShouldRemoveItFromTheBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(1);
    basket.put(contribution);
    assertThat(basket.isEmpty(), is(false));

    Optional<SelectionEntry<MyContribution>> maybe = basket.pop();
    assertThat(maybe.isPresent(), is(true));
    assertThat(maybe.get().getResource(), is(contribution));
    assertThat(basket.isEmpty(), is(true));
  }

  @Test
  @DisplayName("The popped resource should be the last one put in the basket")
  void popAResourceFromTheBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(3);
    basket.put(getContribution(1));
    basket.put(getContribution(2));
    basket.put(contribution);
    assertThat(basket.count(), is(3));

    Optional<SelectionEntry<MyContribution>> maybe = basket.pop();
    assertThat(maybe.isPresent(), is(true));
    assertThat(maybe.get().getResource(), is(contribution));
    assertThat(basket.count(), is(2));
  }

  @Test
  @DisplayName("Popped a resource from an empty basket should do nothing")
  void popFromAnEmptyBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    Optional<SelectionEntry<MyContribution>> maybe = basket.pop();
    assertThat(maybe.isEmpty(), is(true));
    assertThat(basket.isEmpty(), is(true));
  }

  @Test
  @DisplayName("Stream over the resources in the basket")
  void getSelectedResources() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    basket.put(getContribution(1));
    basket.put(getContribution(2));
    basket.put(getContribution(3));
    basket.put(getContribution(4));
    assertThat(basket.count(), is(4));

    boolean matches = basket.getSelectedResources()
        .map(SelectionEntry::getResource)
        .map(MyContribution.class::cast)
        .map(MyContribution::getIdentifier)
        .map(ContributionIdentifier::getLocalId)
        .map(Integer::valueOf)
        .allMatch(i -> i >= 1 && i <= 4);
    assertThat(matches, is(true));
  }

  @Test
  @DisplayName("Stream over the resources of an empty basket")
  void getSelectedResourcesFromEmptyBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    assertThat(basket.getSelectedResources().count(), is(0L));
  }

  @Test
  @DisplayName("Get a resource in an empty basket should return nothing whatever the position asked")
  void getASelectedResourceInEmptyBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    assertThat(basket.getAt(0).isEmpty(), is(true));
    assertThat(basket.getAt(8).isEmpty(), is(true));
  }

  @Test
  @DisplayName("Get a resource at an invalid position in the basket should return nothing")
  void getASelectedResourceAtAnInvalidPositionInBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    basket.put(getContribution(1));
    basket.put(getContribution(2));
    assertThat(basket.count(), is(2));

    assertThat(basket.getAt(8).isEmpty(), is(true));
  }

  @Test
  @DisplayName("Get a resource at a valid position in the basket should return it")
  void getASelectedResource() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(2);
    basket.put(getContribution(1));
    basket.put(contribution);
    basket.put(getContribution(3));
    basket.put(getContribution(4));
    assertThat(basket.count(), is(4));

    assertThat(basket.getAt(2).isPresent(), is(true));
    assertThat(basket.getAt(2).get().getResource(), is(contribution));
  }

  @Test
  @DisplayName("Remove a resource in an empty basket should do nothing")
  void removeAResourceInAnEmptyBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    assertThat(basket.removeAt(0).isEmpty(), is(true));
    assertThat(basket.removeAt(8).isEmpty(), is(true));
  }

  @Test
  @DisplayName("Remove a resource at an invalid position in the basket should do nothing")
  void removeASelectedResourceAtAnInvalidPositionInBasket() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    basket.put(getContribution(1));
    basket.put(getContribution(2));
    assertThat(basket.count(), is(2));

    assertThat(basket.removeAt(8).isEmpty(), is(true));
    assertThat(basket.count(), is(2));
  }

  @Test
  @DisplayName("Remove a resource at a valid position in the basket should remove it")
  void removeASelectedResource() {
    SelectionBasket basket = SelectionBasket.get();
    assertThat(basket.isEmpty(), is(true));

    MyContribution contribution = getContribution(2);
    basket.put(getContribution(1));
    basket.put(contribution);
    basket.put(getContribution(3));
    basket.put(getContribution(4));
    assertThat(basket.count(), is(4));

    Optional<SelectionEntry<MyContribution>> removedContribution = basket.removeAt(2);
    assertThat(basket.count(), is(3));
    assertThat(removedContribution.isPresent(), is(true));
    assertThat(removedContribution.get().getResource(), is(contribution));
    assertThat(basket.getSelectedResources()
        .map(SelectionEntry::getResource)
        .anyMatch(r -> r.equals(contribution)), is(false));

  }

  private MyContribution getContribution(int i) {
    return new MyContribution("Test " + i).setId(
        ContributionIdentifier.from("kmelia12", String.valueOf(i),
            MyContribution.class.getTypeName()));
  }
}