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

package org.silverpeas.core.workflow.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.engine.user.ReplacementImpl;
import org.silverpeas.core.workflow.engine.user.UserImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class ReplacementListTest {

  private final static String ROLE_A = "roleA";
  private final static String ROLE_B = "roleB";
  private final static String ROLE_C = "roleC";
  private final static String ROLE_D = "roleD";

  @TestManagedMock
  private OrganizationController mockedOrganizationController;

  @TestManagedMock
  private UserManager mockedUserManager;

  private ReplacementList<ReplacementImpl> replacements;

  @BeforeEach
  void populate() throws IllegalAccessException {
    final List<ReplacementImpl> list = new ArrayList<>();
    list.add(replacement("1", "2", LocalDate.parse("2019-04-11"), LocalDate.parse("2019-04-11")));
    list.add(replacement("3", "4", LocalDate.parse("2019-04-13"), LocalDate.parse("2019-04-13")));
    list.add(replacement("5", "6", LocalDate.parse("2019-04-11"), LocalDate.parse("2019-04-13")));
    list.add(replacement("1", "3", LocalDate.parse("2019-04-12"), LocalDate.parse("2019-04-13")));
    replacements = new ReplacementList<>(list);
  }

  @BeforeEach
  void mockServices() throws WorkflowException {
    when(mockedOrganizationController.getUserProfiles(anyString(), anyString())).then(i -> {
      final String userId = i.getArgument(0);
      if ("1".equals(userId)) {
        return new String[]{ROLE_A, ROLE_B};
      } else if ("2".equals(userId)) {
        return new String[]{ROLE_B, ROLE_C};
      } else if ("3".equals(userId)) {
        return new String[]{ROLE_C, ROLE_D};
      } else if ("4".equals(userId)) {
        return new String[]{ROLE_A, ROLE_C};
      } else if ("5".equals(userId)) {
        return new String[]{ROLE_B, ROLE_D};
      } else if ("6".equals(userId)) {
        return new String[]{ROLE_D};
      }
      return new String[0];
    });
    when(mockedUserManager.getUser(anyString())).then(i -> {
      final UserDetail mock = mock(UserDetail.class);
      when(mock.getId()).thenReturn(i.getArgument(0));
      return new UserImpl(mock);
    });
  }

  @Test
  void filterCurrentAtOutOfRange() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-10"))
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-14"))
        .collect(Collectors.toList());
    assertThat(result, empty());
  }

  @Test
  void filterCurrentAndNextAtOutOfRange() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterCurrentAndNextAt(LocalDate.parse("2019-04-14"))
        .collect(Collectors.toList());
    assertThat(result, empty());
  }

  @Test
  void filterOnIncumbentOutOfRange() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterOnIncumbent((String) null)
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnIncumbent("")
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnIncumbent("A")
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnIncumbent("2")
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnIncumbent("2", "A")
        .collect(Collectors.toList());
    assertThat(result, empty());
  }

  @Test
  void filterCurrentAt() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-11"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12,56"));
    result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-12"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("56,13"));
    result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-13"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34,56,13"));
  }

  @Test
  void filterCurrentAndNextAt() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterCurrentAndNextAt(LocalDate.parse("2019-04-10"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12,34,56,13"));
    result = replacements
        .stream()
        .filterCurrentAndNextAt(LocalDate.parse("2019-04-11"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12,34,56,13"));
    result = replacements
        .stream()
        .filterCurrentAndNextAt(LocalDate.parse("2019-04-12"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34,56,13"));
    result = replacements
        .stream()
        .filterCurrentAndNextAt(LocalDate.parse("2019-04-13"))
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34,56,13"));
  }

  @Test
  void filterOnIncumbent() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterOnIncumbent("1")
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12,13"));
    result = replacements
        .stream()
        .filterOnIncumbent("3")
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34"));
    result = replacements
        .stream()
        .filterOnIncumbent("5")
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("56"));
    result = replacements
        .stream()
        .filterOnIncumbent("5", "3")
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34,56"));
  }

  @Test
  void filterOnAtLeastOneRoleButUnknownRole() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterOnAtLeastOneRole("unknown")
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnAtLeastOneRole("")
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnAtLeastOneRole((String) null)
        .collect(Collectors.toList());
    assertThat(result, empty());
  }

  @Test
  void filterOnAtLeastOneRole() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterOnAtLeastOneRole(ROLE_A)
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterOnAtLeastOneRole(ROLE_B)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12"));
    result = replacements
        .stream()
        .filterOnAtLeastOneRole(ROLE_C)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("34"));
    result = replacements
        .stream()
        .filterOnAtLeastOneRole(ROLE_D)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("56"));
    result = replacements
        .stream()
        .filterOnAtLeastOneRole(ROLE_A, ROLE_B, ROLE_D)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12,56"));
  }

  @Test
  void filterAtAndFilterOnAtLeastOneRole() {
    List<ReplacementImpl> result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-11"))
        .filterOnAtLeastOneRole(ROLE_B)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("12"));
    result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-12"))
        .filterOnAtLeastOneRole(ROLE_B)
        .collect(Collectors.toList());
    assertThat(result, empty());
    result = replacements
        .stream()
        .filterCurrentAt(LocalDate.parse("2019-04-12"))
        .filterOnAtLeastOneRole(ROLE_A, ROLE_B, ROLE_D)
        .collect(Collectors.toList());
    assertThat(toUserIdsAsString(result), is("56"));
  }

  private ReplacementImpl replacement(final String incumbentId, final String substituteId,
      final LocalDate start, final LocalDate end) throws IllegalAccessException {
    final ReplacementImpl replacement = new ReplacementImpl();
    writeField(replacement, "incumbentId", incumbentId, true);
    writeField(replacement, "substituteId", substituteId, true);
    writeField(replacement, "workflowId", "w3", true);
    replacement.setPeriod(Period.between(start, end.plusDays(1)));
    return replacement;
  }

  private String toUserIdsAsString(final List<ReplacementImpl> result) {
    return result.stream()
        .map(r -> r.getIncumbent().getUserId() + r.getSubstitute().getUserId())
        .collect(Collectors.joining(","));
  }
}