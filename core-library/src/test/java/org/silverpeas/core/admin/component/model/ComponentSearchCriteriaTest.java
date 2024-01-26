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

package org.silverpeas.core.admin.component.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ComponentSearchCriteriaTest {

  @DisplayName("two empty criteria instances are equals")
  @Test
  void twoEmpties() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    assertThat(criteria, is(criteria2));
  }

  @DisplayName("two empty criteria instances (empty set explicitly) are equals")
  @Test
  void twoEmptiesSetExplicitly() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onWorkspace(null);
    criteria.onComponentInstances(null);
    criteria.onUser(null);
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onWorkspace(null);
    criteria2.onComponentInstances(null);
    criteria2.onUser(null);
    assertThat(criteria, is(criteria2));
  }

  @DisplayName("two criteria instances having same component instance list but not is same order " +
      "are equals")
  @Test
  void sameComponentIdListButNotSameSort() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onComponentInstances(List.of("1", "2"));
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onComponentInstances(List.of("2", "1"));
    assertThat(criteria, is(criteria2));
  }

  @DisplayName("two criteria instances having not same component instance list are not equals")
  @Test
  void differentComponentIdList() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onComponentInstances(List.of("1", "2"));
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onComponentInstances(List.of("1", "3"));
    assertThat(criteria, not(is(criteria2)));
  }

  @DisplayName("two criteria instances having same workspace are equals")
  @Test
  void sameWorkspace() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onWorkspace("1");
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onWorkspace("1");
    assertThat(criteria, is(criteria2));
  }

  @DisplayName("two criteria instances having not same workspace are not equals")
  @Test
  void differentWorkspace() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onWorkspace("1");
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onWorkspace("2");
    assertThat(criteria, not(is(criteria2)));
  }

  @DisplayName("two criteria instances having same user are equals")
  @Test
  void sameUser() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onUser(aUser("1"));
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onUser(aUser("1"));
    assertThat(criteria, is(criteria2));
  }

  @DisplayName("two criteria instances having not same user are not equals")
  @Test
  void differentUser() {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria();
    criteria.onUser(aUser("1"));
    final ComponentSearchCriteria criteria2 = new ComponentSearchCriteria();
    criteria2.onUser(aUser("2"));
    assertThat(criteria, not(is(criteria2)));
  }

  private UserDetail aUser(final String id) {
    final UserDetail user = new UserDetail();
    user.setId(id);
    return user;
  }
}