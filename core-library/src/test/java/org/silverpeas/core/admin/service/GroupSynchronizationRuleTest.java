/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRow;
import org.silverpeas.core.admin.persistence.UserTable;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @SuppressWarnings({"unchecked", "Duplicates"})
  @Before
  public void setup() throws Exception {
    Collections.shuffle(DOMAIN_A_USERS);
    Collections.shuffle(DOMAIN_B_USERS);
    Collections.shuffle(ALL_USERS);

    UserManager userManager = commonAPI4Test.injectIntoMockedBeanContainer(mock(UserManager.class));
    GroupManager groupManager = commonAPI4Test.injectIntoMockedBeanContainer(mock(GroupManager.class));

    UserTable userTable = mock(UserTable.class);
    OrganizationSchema organizationSchema = new OrganizationSchema(mock(Connection.class));
    organizationSchema.user = userTable;
    final DomainDriverManager domainDriverManager = mock(DomainDriverManager.class);
    when(domainDriverManager.getOrganization()).thenReturn(organizationSchema);
    ThreadLocal<DomainDriverManager> domainDriverManagerRef =
        new ThreadLocal<DomainDriverManager>() {
          @Override
          protected DomainDriverManager initialValue() {
            return domainDriverManager;
          }
        };
    DomainDriverManagerProvider domainDriverManagerProvider =
        commonAPI4Test.injectIntoMockedBeanContainer(mock(DomainDriverManagerProvider.class));
    when(domainDriverManagerProvider.getDomainDriverManagerRef())
        .thenReturn(domainDriverManagerRef);

    when(userManager.getAllUsersIds(any(DomainDriverManager.class)))
        .then((Answer<String[]>) invocation -> extractUserIds(ALL_USERS));

    when(userManager.getUserIdsOfDomain(any(DomainDriverManager.class), anyString()))
        .then((Answer<String[]>) invocation -> {
          String domainId = (String) invocation.getArguments()[1];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (!user.getDomainId().equals(domainId)) {
              userIt.remove();
            }
          }
          return extractUserIds(users);
        });

    when(userManager.getUserIdsOfDomainAndAccessLevel(any(DomainDriverManager.class), anyString(), any(
        UserAccessLevel.class)))
        .then((Answer<String[]>) invocation -> {
          String domainId = (String) invocation.getArguments()[1];
          UserAccessLevel accessLevel = (UserAccessLevel) invocation.getArguments()[2];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (!user.getDomainId().equals(domainId) || user.getAccessLevel() != accessLevel) {
              userIt.remove();
            }
          }
          return extractUserIds(users);
        });

    when(userManager.getAllUserIdsOfGroups(anyListOf(String.class)))
        .then((Answer<List<String>>) invocation -> {
          List<String> groupIds = (List<String>) invocation.getArguments()[0];
          List<String> userIds = new ArrayList<>();
          ALL_GROUPS.stream().filter(group -> groupIds.contains(group.getId()))
              .forEach(group -> Collections.addAll(userIds, group.getUserIds()));
          return userIds.stream().distinct().collect(Collectors.toList());
        });

    when(userTable.getUserIdsByAccessLevel(any(UserAccessLevel.class)))
        .then((Answer<String[]>) invocation -> {
          UserAccessLevel accessLevel = (UserAccessLevel) invocation.getArguments()[0];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (user.getAccessLevel() != accessLevel) {
              userIt.remove();
            }
          }
          return extractUserIds(users);
        });

    when(userTable.getUserIdsOfDomain(anyInt()))
        .then((Answer<String[]>) invocation -> {
          Integer domainId = (Integer) invocation.getArguments()[0];
          List<UserDetail> users = new ArrayList<>(ALL_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (Integer.parseInt(user.getDomainId()) != domainId) {
              userIt.remove();
            }
          }
          return extractUserIds(users);
        });

    when(groupManager.getAllSubGroupIdsRecursively(anyString()))
        .then((Answer<List<String>>) invocation -> {
          String groupId = (String) invocation.getArguments()[0];
          if (DOMAIN_A_GROUP_1.getId().equals(groupId)) {
            return Arrays.asList(DOMAIN_A_GROUP_11.getId(), DOMAIN_A_GROUP_111.getId());
          } else if (DOMAIN_A_GROUP_11.getId().equals(groupId)) {
            return Collections.singletonList(DOMAIN_A_GROUP_111.getId());
          }
          return Collections.emptyList();
        });

    /**
     * Property name / property value part
     */

    Administration admin = commonAPI4Test.injectIntoMockedBeanContainer(mock(Administration.class));
    final DomainDriver domainDriverA = mock(DomainDriver.class);
    final DomainDriver domainDriverB = mock(DomainDriver.class);

    when(admin.getAllDomains()).thenReturn(new Domain[]{DOMAIN_A, DOMAIN_B});

    when(domainDriverManager.getDomainDriver(anyInt())).then((Answer<DomainDriver>) invocation -> {
      Integer domainId = (Integer) invocation.getArguments()[0];
      if (domainId == Integer.parseInt(DOMAIN_A.getId())) {
        return domainDriverA;
      } else if (domainId == Integer.parseInt(DOMAIN_B.getId())) {
        return domainDriverB;
      }
      return null;
    });

    when(domainDriverA.getUsersBySpecificProperty(anyString(), anyString()))
        .then((Answer<UserDetail[]>) invocation -> {
          String propertyName = (String) invocation.getArguments()[0];
          String propertyValue = (String) invocation.getArguments()[1];
          String specificSuffix = "_" + propertyName + "=" + propertyValue;
          List<UserDetail> users = new ArrayList<>(DOMAIN_A_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (!user.getSpecificId().endsWith(specificSuffix)) {
              userIt.remove();
            }
          }
          return users.toArray(new UserDetail[users.size()]);
        });

    when(domainDriverB.getUsersBySpecificProperty(anyString(), anyString()))
        .then((Answer<UserDetail[]>) invocation -> {
          String propertyName = (String) invocation.getArguments()[0];
          String propertyValue = (String) invocation.getArguments()[1];
          String specificSuffix = "_" + propertyName + "=" + propertyValue;
          List<UserDetail> users = new ArrayList<>(DOMAIN_B_USERS);
          Iterator<UserDetail> userIt = users.iterator();
          while (userIt.hasNext()) {
            UserDetail user = userIt.next();
            if (!user.getSpecificId().endsWith(specificSuffix)) {
              userIt.remove();
            }
          }
          return users.toArray(new UserDetail[users.size()]);
        });

    when(userTable.getUsersBySpecificIds(anyInt(), anyListOf(String.class)))
        .then((Answer<UserRow[]>) invocation -> {
          Integer domainId = (Integer) invocation.getArguments()[0];
          List<String> specificIds = (List<String>) invocation.getArguments()[1];
          List<UserDetail> users = new ArrayList<>(
              String.valueOf(domainId).equals(DOMAIN_A.getId()) ? DOMAIN_A_USERS : DOMAIN_B_USERS);
          List<UserRow> userRows = new ArrayList<>();
          users.stream().filter(user -> specificIds.contains(user.getSpecificId()))
              .forEach(user -> {
                UserRow userRow = new UserRow();
                userRow.id = Integer.valueOf(user.getId());
                userRows.add(userRow);
              });
          return userRows.toArray(new UserRow[userRows.size()]);
        });
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
    exception.expect(GroupSynchronizationRule.Error.class);
    exception.expectCause(Matchers.any((Class)IllegalArgumentException.class));
    exception.expectMessage(Matchers.endsWith("expression.operation.malformed"));
    from(group4Rule(DOMAIN_B, "(DC_ville=Va(le)nce)")).getUserIds();
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
    exception.expect(GroupSynchronizationRule.Error.class);
    exception.expectCause(Matchers.any((Class)NumberFormatException.class));
    from(group4Rule(SHARED_DOMAIN, "DS_domain = A")).getUserIds();
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