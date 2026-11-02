/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.selection;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

/**
 * Unit tests on the locking of users and of user groups in a selection.
 * @author mmoquillon
 */
class SelectionTest {

  @Test
  void nothingIsLockedByDefault() {
    Selection selection = new Selection();

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }

  @Test
  void usersAndGroupsCanBeLocked() {
    Selection selection = new Selection();
    List<String> userIds = new ArrayList<>(List.of("1", "2"));
    selection.setLockedUsers(userIds);
    selection.setLockedGroups(List.of("3"));

    userIds.add("12");
    assertThat(selection.getLockedUsers(), contains("1", "2"));
    assertThat(selection.getLockedGroups(), contains("3"));
  }

  @Test
  void lockingNothingUnlocksAll() {
    Selection selection = new Selection();
    selection.setLockedUsers(List.of("1"));
    selection.setLockedGroups(List.of("3"));

    selection.setLockedUsers(null);
    selection.setLockedGroups(null);

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }

  @Test
  void resettingTheSelectionUnlocksAll() {
    Selection selection = new Selection();
    selection.setLockedUsers(List.of("1"));
    selection.setLockedGroups(List.of("3"));

    selection.resetAll();

    assertThat(selection.getLockedUsers(), empty());
    assertThat(selection.getLockedGroups(), empty());
  }
}
