/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.admin.service;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.SilverTestEnv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.service.GroupSynchronizationRule.from;
import static org.silverpeas.core.admin.user.constant.UserAccessLevel.*;
import static org.silverpeas.core.util.CollectionUtil.union;

/**
 * @author Yohann Chastagnier
 */
@ExtendWith(SilverTestEnv.class)
@Execution(ExecutionMode.SAME_THREAD)
public class GroupSynchronizationRuleTest {

  private static final String GROUP_ID = "26";

  private static final String PROPERTY_VALUE_VILLE_ROM = "ville=Romans sur Isère";
  private static final String PROPERTY_VALUE_VILLE_VAL = "ville=Va(le)nce";

  private static final Domain SHARED_DOMAIN = initializeDomain(null);
  private static final Domain DOMAIN_A = initializeDomain("10");
  private static final Domain DOMAIN_B = initializeDomain("20");

  private static final UserDetail DOMAIN_A_USER_ADMIN_1 = initializeUser("1000", DOMAIN_A, ADMINISTRATOR, PROPERTY_VALUE_VILLE_ROM);
  private static final UserDetail DOMAIN_A_USER_ADMIN_2 = initializeUser("1001", DOMAIN_A, ADMINISTRATOR, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_A_USER_ADMIN_3 = initializeUser("1002", DOMAIN_A, ADMINISTRATOR, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_A_USER_SPACE_ADMIN_1 = initializeUser("1010", DOMAIN_A, SPACE_ADMINISTRATOR, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_A_USER_SPACE_ADMIN_2 = initializeUser("1011", DOMAIN_A, SPACE_ADMINISTRATOR, PROPERTY_VALUE_VILLE_ROM);
  private static final UserDetail DOMAIN_A_USER_1 = initializeUser("1100", DOMAIN_A, USER, PROPERTY_VALUE_VILLE_ROM);
  private static final UserDetail DOMAIN_A_USER_2 = initializeUser("1101", DOMAIN_A, USER, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_A_USER_3 = initializeUser("1102", DOMAIN_A, USER, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_A_USER_4 = initializeUser("1103", DOMAIN_A, USER, PROPERTY_VALUE_VILLE_VAL);

  private static final Group DOMAIN_A_GROUP_0 = initializeGroup("group0_A");
  private static final Group DOMAIN_A_GROUP_1 = initializeGroup("group1_A", DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_1);
  private static final Group DOMAIN_A_GROUP_11 = initializeGroup("group11_A", DOMAIN_A_USER_3, DOMAIN_A_USER_4);
  private static final Group DOMAIN_A_GROUP_111 = initializeGroup("group111_A", DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_SPACE_ADMIN_1);

  private static final UserDetail DOMAIN_B_USER_ADMIN_1 = initializeUser("2000", DOMAIN_B, ADMINISTRATOR, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_B_USER_SPACE_ADMIN_1 = initializeUser("2010", DOMAIN_B, SPACE_ADMINISTRATOR,PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_B_USER_1 = initializeUser("2100", DOMAIN_B, USER, PROPERTY_VALUE_VILLE_VAL);
  private static final UserDetail DOMAIN_B_USER_2 = initializeUser("2101", DOMAIN_B, USER, PROPERTY_VALUE_VILLE_ROM);

  private static final Group DOMAIN_B_GROUP_0 = initializeGroup("group0_B", DOMAIN_B_USER_SPACE_ADMIN_1);

  private static final List<UserDetail> DOMAIN_A_USERS = Arrays
      .asList(DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
          DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1,
          DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4);

  private static final List<UserDetail> DOMAIN_B_USERS = Arrays
      .asList(DOMAIN_B_USER_ADMIN_1, DOMAIN_B_USER_SPACE_ADMIN_1, DOMAIN_B_USER_1,
          DOMAIN_B_USER_2);

  private static final List<UserDetail> ALL_USERS =
      union(DOMAIN_A_USERS, DOMAIN_B_USERS);

  private static final List<Group> ALL_GROUPS = Arrays
      .asList(DOMAIN_A_GROUP_0, DOMAIN_A_GROUP_1, DOMAIN_A_GROUP_11, DOMAIN_A_GROUP_111,
          DOMAIN_B_GROUP_0);

  @SuppressWarnings({"unchecked", "Duplicates"})
  @BeforeEach
  public void setup(@TestManagedMock UserManager userManager, @TestManagedMock GroupManager groupManager,
      @TestManagedMock DomainDriverManager domainDriverManager,
      @TestManagedMock DomainDriverManagerProvider domainDriverManagerProvider,
      @TestManagedMock Administration admin,
      @TestManagedMock PublicationTemplateManager templateManager) throws Exception {
    Collections.shuffle(DOMAIN_A_USERS);
    Collections.shuffle(DOMAIN_B_USERS);
    Collections.shuffle(ALL_USERS);

    when(domainDriverManagerProvider.getDomainDriverManager())
        .thenReturn(domainDriverManager);

    when(userManager.getAllUsersIds())
        .then(invocation -> Arrays.asList(extractUserIds(ALL_USERS)));

    when(userManager.getAllUserIdsInDomain(anyString()))
        .then(invocation -> {
          String domainId = (String) invocation.getArguments()[0];
          Integer.parseInt(domainId); // the domain should be an integer
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          users.removeIf(user -> !user.getDomainId().equals(domainId));
          return Arrays.asList(extractUserIds(users));
        });

    when(userManager.getUserIdsByDomainAndByAccessLevel(anyString(), any(UserAccessLevel.class)))
        .then(invocation -> {
          String domainId = (String) invocation.getArguments()[0];
          Integer.parseInt(domainId); // the domain should be an integer
          UserAccessLevel accessLevel = (UserAccessLevel) invocation.getArguments()[1];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          users.removeIf(
              user -> !user.getDomainId().equals(domainId) || user.getAccessLevel() != accessLevel);
          return extractUserIds(users);
        });

    when(userManager.getAllUserIdsInGroups(anyListOf(String.class)))
        .then(invocation -> {
          List<String> groupIds = (List<String>) invocation.getArguments()[0];
          List<String> userIds = new ArrayList<>();
          ALL_GROUPS.stream().filter(group -> groupIds.contains(group.getId()))
              .forEach(group -> Collections.addAll(userIds, group.getUserIds()));
          return userIds.stream().distinct().collect(Collectors.toList());
        });

    when(userManager.getUserIdsByAccessLevel(any(UserAccessLevel.class)))
        .then(invocation -> {
          UserAccessLevel accessLevel = (UserAccessLevel) invocation.getArguments()[0];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          users.removeIf(user -> user.getAccessLevel() != accessLevel);
          return extractUserIds(users);
        });

    when(groupManager.getAllSubGroupIdsRecursively(anyString()))
        .then(invocation -> {
          String groupId = (String) invocation.getArguments()[0];
          if (DOMAIN_A_GROUP_1.getId().equals(groupId)) {
            return Arrays.asList(DOMAIN_A_GROUP_11.getId(), DOMAIN_A_GROUP_111.getId());
          } else if (DOMAIN_A_GROUP_11.getId().equals(groupId)) {
            return Collections.singletonList(DOMAIN_A_GROUP_111.getId());
          }
          return Collections.emptyList();
        });

    final DomainDriver domainDriverA = mock(DomainDriver.class);
    final DomainDriver domainDriverB = mock(DomainDriver.class);

    when(admin.getAllDomains()).thenReturn(new Domain[]{DOMAIN_A, DOMAIN_B});

    when(domainDriverManager.getDomainDriver(anyString())).then(invocation -> {
      String domainId = (String) invocation.getArguments()[0];
      Integer.parseInt(domainId); // the domain should be an integer
      if (DOMAIN_A.getId().equals(domainId)) {
        return domainDriverA;
      } else if (DOMAIN_B.getId().equals(domainId)) {
        return domainDriverB;
      }
      return null;
    });

    when(domainDriverA.getUsersBySpecificProperty(anyString(), anyString()))
        .then(invocation -> {
          String propertyName = (String) invocation.getArguments()[0];
          String propertyValue = (String) invocation.getArguments()[1];
          String specificSuffix = "_" + propertyName + "=" + propertyValue;
          List<UserDetail> users = new ArrayList<>(DOMAIN_A_USERS);
          users.removeIf(user -> !user.getSpecificId().endsWith(specificSuffix));
          return users.toArray(new UserDetail[users.size()]);
        });

    when(domainDriverB.getUsersBySpecificProperty(anyString(), anyString()))
        .then(invocation -> {
          String propertyName = (String) invocation.getArguments()[0];
          String propertyValue = (String) invocation.getArguments()[1];
          String specificSuffix = "_" + propertyName + "=" + propertyValue;
          List<UserDetail> users = new ArrayList<>(DOMAIN_B_USERS);
          users.removeIf(user -> !user.getSpecificId().endsWith(specificSuffix));
          return users.toArray(new UserDetail[users.size()]);
        });

    when(userManager.getUsersBySpecificIdsAndDomainId(anyListOf(String.class), anyString()))
        .then(invocation -> {
          List<String> specificIds = (List<String>) invocation.getArguments()[0];
          String domainId = (String) invocation.getArguments()[1];
          Integer.parseInt(domainId); // the domain should be an integer
          List<UserDetail> users = new ArrayList<>(
              domainId.equals(DOMAIN_A.getId()) ? DOMAIN_A_USERS : DOMAIN_B_USERS);
          return users.stream().filter(user -> specificIds.contains(user.getSpecificId())).collect(
              Collectors.toList());
        });

    when(templateManager.getDirectoryTemplate()).thenReturn(null);
  }

  @Test
  public void getUserIdsFromNullRuleShouldReturnEmptyList() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, null)).getUserIds();
    assertThat(userIds, empty());
  }

  @Test
  public void getUserIdsFromEmptyRuleShouldReturnEmptyList() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "")).getUserIds();
    assertThat(userIds, empty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void notEscapedParenthesesShouldThrowAnError() throws Exception {
    try {
      from(group4Rule(DOMAIN_B, "(DC_ville=Va(le)nce)")).getUserIds();
    } catch (GroupSynchronizationRule.Error e) {
      assertThat(e.getCause(), Matchers.any((Class)IllegalArgumentException.class));
      assertThat(e.getMessage(), Matchers.endsWith("expression.operation.malformed"));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void errorShouldContainsOriginalOne() throws Exception {
    GroupSynchronizationRule.Error notNullError = null;
    try {
      from(group4Rule(DOMAIN_B, "(DC_ville=Va(le)nce)")).getUserIds();
    } catch (GroupSynchronizationRule.Error error) {
      assertThat(error.getMessage(), not(is(error.getCause().getMessage())));
      assertThat(error.getMessage(), Matchers.endsWith(" expression.operation.malformed"));
      assertThat(error.getCause().getMessage(), is("expression.operation.malformed"));
      notNullError = error;
    }
    assertThat(notNullError, notNullValue());
    notNullError = null;
    try {
      from(group4Rule(DOMAIN_B, "(DS_acceLevel=A")).getUserIds();
    } catch (GroupSynchronizationRule.Error error) {
      assertThat(error.getMessage(), not(is(error.getCause().getMessage())));
      assertThat(error.getMessage(), Matchers.endsWith(" expression.groundrule.unknown"));
      assertThat(error.getCause().getMessage(), is("expression.groundrule.unknown"));
      notNullError =  error;
    }
    assertThat(notNullError, notNullValue());
  }

  @Test
  public void notEscapedParenthesesButSimpleValueShouldBeTakenIntoAccount() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_B, "DC_ville=Va(le)nce")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1, DOMAIN_B_USER_SPACE_ADMIN_1, DOMAIN_B_USER_1)));
  }

  @Test
  public void escapedParenthesesShouldBeTakenIntoAccount() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_B, "DC_ville=Va\\(le\\)nce")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1, DOMAIN_B_USER_SPACE_ADMIN_1, DOMAIN_B_USER_1)));
  }

  @Test
  public void getUserIdsFromAllAccessLevelRuleShouldReturnAllUsers() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "DS_AccessLevel = *")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4)));

    userIds = from(group4Rule(DOMAIN_B, "DS_AccessLevel = *")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_AccessLevel = *")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));
  }

  @Test
  public void getUserIdsFromAdminAccessLevelRuleShouldReturnAdminUsers() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "DS_AccessLevel = A")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3)));

    userIds = from(group4Rule(DOMAIN_B, "DS_AccessLevel = A")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_AccessLevel = A")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_B_USER_ADMIN_1)));
  }

  @Test
  public void getUserIdsFromUserAccessLevelRuleShouldReturnSimpleUsers() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "DS_AccessLevel =U")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4)));

    userIds = from(group4Rule(DOMAIN_B, "DS_AccessLevel   = U")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "   DS_AccessLevel =   U   ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "   DS_AccessLevel =   ZKW   ")).getUserIds();
    assertThat(userIds, Matchers.empty());
  }

  @Test
  public void getUserIdsFromDomainRuleShouldReturnAllUsersOfDomain()
      throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "  DS_domain= 10  ")).getUserIds();
    assertThat(userIds, Matchers.empty());

    userIds = from(group4Rule(DOMAIN_B, "DS_domain = 10")).getUserIds();
    assertThat(userIds, Matchers.empty());

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_domain = 10")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_domains = 20")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_domains=10,20")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DS_domains = 10 , 20   ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getUserIdsFromWrongDomainIdFormatRuleShouldThrowAnNumberFormatException()
      throws Exception {
    try {
      from(group4Rule(SHARED_DOMAIN, "DS_domain = A")).getUserIds();
    } catch (GroupSynchronizationRule.Error e) {
      assertThat(e.getCause(), Matchers.any((Class)NumberFormatException.class));
    }
  }

  @Test
  public void getUserIdsFromGroupRuleShouldReturnAllUsersOfGroupAndNotThoseOfSubGroups()
      throws Exception {
    Matcher<Iterable<? extends String>> expectedUserIds = containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_1,
        DOMAIN_B_USER_SPACE_ADMIN_1));

    List<String> userIds =
        from(group4Rule(DOMAIN_A, "  Dr_groups= group1_A ,  group0_B")).getUserIds();
    assertThat(userIds, expectedUserIds);

    userIds = from(group4Rule(DOMAIN_B, "Dr_groups=group1_A,group0_B")).getUserIds();
    assertThat(userIds, expectedUserIds);

    userIds = from(group4Rule(SHARED_DOMAIN, "Dr_groups = group1_A , group0_B")).getUserIds();
    assertThat(userIds, expectedUserIds);
  }

  @Test
  public void getUserIdsFromGroupWithSubGroupsRuleShouldReturnAllUsersOfGroupAndSubGroups()
      throws Exception {
    Matcher<Iterable<? extends String>> expectedUserIds = containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_SPACE_ADMIN_1,
        DOMAIN_A_USER_1, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_SPACE_ADMIN_1));

    List<String> userIds =
        from(group4Rule(DOMAIN_A, "  Dr_groupswithSubGroups= group1_A ,  group0_B"))
            .getUserIds();
    assertThat(userIds, expectedUserIds);

    userIds =
        from(group4Rule(DOMAIN_B, "Dr_groupswithSubGroups=group1_A,group0_B")).getUserIds();
    assertThat(userIds, expectedUserIds);

    userIds = from(group4Rule(SHARED_DOMAIN, "Dr_groupswithSubGroups = group1_A , group0_B"))
        .getUserIds();
    assertThat(userIds, expectedUserIds);
  }

  @Test
  public void getUserIdsFromSpecificPropertyRuleShouldReturnUsersWhichVerifyTheCondition()
      throws Exception {
    try {
      from(group4Rule(DOMAIN_A, "  DC_ ville= Romans sur Isère  ")).getUserIds();
      fail("should throw an error");
    } catch (GroupSynchronizationRule.GroundRuleError ignore) {
    }

    List<String> userIds = from(group4Rule(DOMAIN_A, "  DC_ville  = Romans sur Isère  ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds = from(group4Rule(DOMAIN_B, "DC_ville=Romans sur Isère")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "DC_ville=Romans sur Isère")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1,
        DOMAIN_B_USER_2)));
  }

  @Test
  public void getUserIdsFromCombinationRuleWithoutClause() throws Exception {
    List<String> userIds = from(group4Rule(DOMAIN_A, "  ( DC_ville= Romans sur Isère )  ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds = from(group4Rule(DOMAIN_B, "(DC_ville=Romans sur Isère)")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "  ( DC_ville= Romans sur Isère )  ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1,
        DOMAIN_B_USER_2)));
  }

  @Test
  public void getUserIdsFromCombinationRuleWithAndClause() throws Exception {
    List<String> userIds =
        from(group4Rule(DOMAIN_A, "  ( & ( DC_ville= Romans sur Isère ) )  "))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds =
        from(group4Rule(DOMAIN_A, "  (&(DC_ville=Romans sur Isère) (DS_AccessLevel=A) )"))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1)));

    userIds =
        from(group4Rule(DOMAIN_B, "  (&(DC_ville=Romans sur Isère) (DS_AccessLevel=A) )"))
            .getUserIds();
    assertThat(userIds, Matchers.empty());

    userIds =
        from(group4Rule(DOMAIN_B, "  (&(DS_AccessLevel=U)(DC_ville=Romans sur Isère))"))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_2)));

    userIds =
        from(group4Rule(SHARED_DOMAIN, "  (&(DS_AccessLevel=A)(DC_ville=Romans sur Isère))"))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1)));

    userIds =
        from(group4Rule(SHARED_DOMAIN, "  (&(DS_AccessLevel=U)(DC_ville=Romans sur Isère))"))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_1, DOMAIN_B_USER_2)));
  }

  @Test
  public void getUserIdsFromCombinationRuleWithOrClause() throws Exception {
    List<String> userIds =
        from(group4Rule(DOMAIN_A, "  ( | ( DC_ville= Romans sur Isère ) )  "))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds =
        from(group4Rule(DOMAIN_A, "  (|(DC_ville=Romans sur Isère) (DS_AccessLevel=A) )"))
            .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds =
        from(group4Rule(DOMAIN_B, "  (|(DC_ville=Romans sur Isère) (DS_AccessLevel=A) )")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_2)));

    userIds = from(group4Rule(DOMAIN_B, "  (|(DS_AccessLevel=U)(DC_ville=Romans sur Isère))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds =
        from(group4Rule(SHARED_DOMAIN, "  (|(DS_AccessLevel=A)(DC_ville=Romans sur Isère))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_2)));

    userIds =
        from(group4Rule(SHARED_DOMAIN, "  (|(DS_AccessLevel=U)(DC_ville=Romans sur Isère))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1,
        DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));
  }

  @Test
  public void getUserIdsFromCombinationRuleWithNegateOperator() throws Exception {
    List<String> userIds =
        from(group4Rule(DOMAIN_A, " ( ! (    DC_ville= Va\\(le\\)nce  ) ) ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));

    userIds = from(group4Rule(DOMAIN_B, "(!(DC_ville=Romans sur Isère))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1)));

    userIds = from(group4Rule(SHARED_DOMAIN, "( ! (    DC_ville= Va\\(le\\)nce  ) ) ")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1,
        DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN, "(!(DC_ville=Romans sur Isère))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1,
        DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1)));

    userIds = from(group4Rule(SHARED_DOMAIN, "!(DC_ville=Romans sur Isère)")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1,
        DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_1)));
  }

  @Test
  public void negateOperatorCanNotBeUsedDirectlyIntoSimpleSilverpeasRule() throws Exception {
    List<String> userIds =
        from(group4Rule(DOMAIN_A, "(!(DC_ville=Va\\(le\\)nce))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1, DOMAIN_A_USER_SPACE_ADMIN_2, DOMAIN_A_USER_1)));
    try {
      from(group4Rule(DOMAIN_A, "((!DC_ville= Va\\(le\\)nce))")).getUserIds();
      fail("should throw an error");
    } catch (GroupSynchronizationRule.GroundRuleError ignore) {
    }
  }

  @Test
  public void getUserIdsFromCombinationRuleWithNegateOperatorOnSeveralOperands() throws Exception {
    List<String> userIds =
        from(group4Rule(DOMAIN_A, "(!(DC_ville=Va\\(le\\)nce)(DS_AccessLevel=U))")).getUserIds();
    assertThat(userIds, Matchers.empty());
  }

  @Test
  public void getUserIdsFromCombinationRuleWithSubCondition() throws Exception {
    List<String> userIds = from(
        group4Rule(DOMAIN_A, "(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce))"))
        .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1)));

    userIds = from(group4Rule(DOMAIN_A,
        "(&((|(((DS_AccessLevel=A)))((DS_AccessLevel=S))))(DC_ville=Va\\(le\\)nce))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1)));

    userIds = from(group4Rule(SHARED_DOMAIN,
        "(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1)));
  }

  @Test
  public void getUserIdsFromCombinationRuleWithSubSubCondition() throws Exception {
    List<String> userIds = from(
        group4Rule(DOMAIN_A, "(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce))"))
        .getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1)));

    userIds = from(group4Rule(SHARED_DOMAIN,
        "(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_2, DOMAIN_A_USER_ADMIN_3,
        DOMAIN_A_USER_SPACE_ADMIN_1,
        DOMAIN_B_USER_ADMIN_1,
        DOMAIN_B_USER_SPACE_ADMIN_1)));

    userIds = from(group4Rule(SHARED_DOMAIN,
        "(!(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce)))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1,
        DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));

    userIds = from(group4Rule(SHARED_DOMAIN,
        "!(&(|(DS_AccessLevel=A)(DS_AccessLevel=S))(DC_ville=Va\\(le\\)nce))")).getUserIds();
    assertThat(userIds, containsInAnyOrder(extractUserIds(
        DOMAIN_A_USER_ADMIN_1,
        DOMAIN_A_USER_SPACE_ADMIN_2,
        DOMAIN_A_USER_1, DOMAIN_A_USER_2, DOMAIN_A_USER_3, DOMAIN_A_USER_4,
        DOMAIN_B_USER_1, DOMAIN_B_USER_2)));
  }

  private static String[] extractUserIds(UserDetail ... users) {
    return extractUserIds(Arrays.asList(users));
  }

  private static String[] extractUserIds(List<UserDetail> users) {
    String[] userIds = new String[users.size()];
    for(int i=0; i < users.size();i++) {
      userIds[i] = users.get(i).getId();
    }
    return userIds;
  }

  /**
   * Initializes a {@link Group} instance for the rule to test.
   * @param domain a domain to set.
   * @param rule a rule to set.
   * @return the initialized instance.
   */
  private Group group4Rule(Domain domain, String rule) {
    GroupDetail group = new GroupDetail();
    group.setId(GROUP_ID);
    group.setDomainId(domain.getId());
    group.setRule(rule);
    return group;
  }

  /**
   * Initializes a {@link UserDetail} instance.
   * @param id a user id.
   * @param accessLevel an access level.
   * @return the initialized instance.
   */
  private static UserDetail initializeUser(String id, Domain domain,
      UserAccessLevel accessLevel, String propertyValue) {
    UserDetail userDetail = new UserDetail();
    userDetail.setId(id);
    userDetail.setDomainId(domain.getId());
    userDetail.setAccessLevel(accessLevel);
    userDetail.setSpecificId(id + "_" + propertyValue);
    return userDetail;
  }

  /**
   * Initializes a {@link Group} instance.
   * @param id the group identifier.
   * @param users the users the group contains.
   * @return the initialized group.
   */
  private static Group initializeGroup(String id, final UserDetail ... users) {
    GroupDetail group = new GroupDetail(){
      private static final long serialVersionUID = 387818815569157277L;

      @Override
      public String[] getUserIds() {
        return extractUserIds(users);
      }
    };
    group.setId(id);
    return group;
  }

  /**
   * Initializes a {@link Domain} instance.
   * @param id a domain id.
   * @return the initialized instance.
   */
  private static Domain initializeDomain(String id) {
    Domain domain = new Domain();
    domain.setId(id);
    return domain;
  }
}