/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.admin.service;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.UnitTest;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv
class UserSpaceAvailabilityCheckerTest {

  private static final String UNKNOWN_SPACE_ID = "38";
  private static final String SPACE_ID = "26";

  @Test
  void spaceIsAvailableToUser() {
    final UserSpaceAvailabilityChecker checker = new UserSpaceAvailabilityCheckerBuilder().build();
    assertThat(checker.getUserId(), is("aUserId"));
    assertThat(checker.isAvailable(SPACE_ID), is(true));
  }

  @Test
  void unknownSpaceIsNotAvailableToUser() {
    final UserSpaceAvailabilityChecker checker = new UserSpaceAvailabilityCheckerBuilder().build();
    assertThat(checker.isAvailable(UNKNOWN_SPACE_ID), is(false));
  }

  @Test
  void removedSpaceIsNotAvailableToUser() {
    final UserSpaceAvailabilityChecker checker =
        new UserSpaceAvailabilityCheckerBuilder().spaceIsRemoved().build();
    assertThat(checker.isAvailable(SPACE_ID), is(false));
  }

  @Test
  void spaceIsNotAvailableToUserWithoutAnyAvailableComponentInstances() {
    final UserSpaceAvailabilityChecker checker =
        new UserSpaceAvailabilityCheckerBuilder().withoutAnyAvailableInstances().build();
    assertThat(checker.isAvailable(SPACE_ID), is(false));
  }

  @Test
  void emptySpaceIsNotAvailableToUser() {
    final UserSpaceAvailabilityChecker checker =
        new UserSpaceAvailabilityCheckerBuilder().noSpaceIsContainingComponent().build();
    assertThat(checker.isAvailable(SPACE_ID), is(false));
  }

  private static class UserSpaceAvailabilityCheckerBuilder {
    private List<String> availableInstanceIds = List.of("anId");
    private Set<Integer> spaceContainsOneComponent = Set.of(Integer.parseInt(SPACE_ID));
    private final RemovedSpaceAndComponentInstanceChecker removedChecker = mock(RemovedSpaceAndComponentInstanceChecker.class);

    public UserSpaceAvailabilityCheckerBuilder withoutAnyAvailableInstances() {
      availableInstanceIds = List.of();
      return this;
    }

    public UserSpaceAvailabilityCheckerBuilder noSpaceIsContainingComponent() {
      spaceContainsOneComponent = Set.of();
      return this;
    }

    public UserSpaceAvailabilityCheckerBuilder spaceIsRemoved() {
      when(removedChecker.isRemovedSpaceById(SPACE_ID)).thenReturn(true);
      return this;
    }

    public UserSpaceAvailabilityChecker build() {
      return new UserSpaceAvailabilityChecker("aUserId", availableInstanceIds, Integer::parseInt,
          (s, i) -> {
            assertThat(s, containsInAnyOrder(availableInstanceIds.toArray(new String[0])));
            return spaceContainsOneComponent.contains(i);
          }, removedChecker);
    }
  }
}