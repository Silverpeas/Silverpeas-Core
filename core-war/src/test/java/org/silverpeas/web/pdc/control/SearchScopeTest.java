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
package org.silverpeas.web.pdc.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks the scope of a search, that is the component instances in which the search is performed.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SearchScopeTest {

  @TestManagedMock
  private OrganizationController organizationController;

  private UserDetail user;

  @BeforeEach
  void setUp() {
    user = new UserDetail();
    user.setId("2002");
  }

  @Test
  void theScopeIsMadeOfTheSearchableComponentInstancesMatchingTheCriteria() {
    when(organizationController.getSearchableComponentsByCriteria(any()))
        .thenReturn(List.of("kmelia1"));

    SearchScope scope = SearchScope.of(user, "WA1", List.of());

    assertThat(scope.getComponentInstanceIds(), contains("kmelia1"));
    verify(organizationController).getSearchableComponentsByCriteria(
        new ComponentSearchCriteria().onUser(user).onWorkspace("WA1"));
  }

  @Test
  void theScopeCanBeRestrictedToSomeComponentInstances() {
    when(organizationController.getSearchableComponentsByCriteria(any()))
        .thenReturn(List.of("kmelia1"));

    SearchScope scope = SearchScope.of(user, null, List.of("kmelia1"));

    assertThat(scope.getComponentInstanceIds(), contains("kmelia1"));
    verify(organizationController).getSearchableComponentsByCriteria(
        new ComponentSearchCriteria().onUser(user).onComponentInstances(List.of("kmelia1")));
  }

  /**
   * A component instance created, or made accessible to the user, after a first search has to be
   * taken into account in the next searches of the same user session.
   */
  @Test
  void theScopeTakesIntoAccountTheComponentInstancesNewlyAccessibleToTheUser() {
    when(organizationController.getSearchableComponentsByCriteria(any()))
        .thenReturn(List.of("kmelia1"))
        .thenReturn(List.of("kmelia1", "classifieds29"));
    SearchScope scope = SearchScope.of(user, null, List.of());

    assertThat(scope.getComponentInstanceIds(), contains("kmelia1"));
    assertThat(scope.getComponentInstanceIds(), contains("kmelia1", "classifieds29"));
  }
}
