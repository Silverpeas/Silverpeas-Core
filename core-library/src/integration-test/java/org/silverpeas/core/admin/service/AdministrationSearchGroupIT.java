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

package org.silverpeas.core.admin.service;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.util.SQLRequester;
import org.silverpeas.core.util.SilverpeasList;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.admin.user.constant.UserState.*;

/**
 * Look at CommonAdministrationIT.ods to get a better view of data.
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class AdministrationSearchGroupIT extends AbstractAdministrationTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AdministrationSearchGroupIT.class)
        .addCommonBasicUtilities().addPublicationTemplateFeatures().addSilverpeasExceptionBases()
        .testFocusedOn((w) -> ((WarBuilder4LibCore) w).addAdministrationFeatures()).build();
  }

  @Test
  public void noCriteria() throws AdminException {
    // without children
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder().build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
    // with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withChildren()
        .build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
  }

  /**
   * Deleted users ar never taken into account.
   */
  @Test
  public void userStateCriteria() throws AdminException {
    // no VALID state
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .userStatesToExclude(VALID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(extractData(groups, Group::getId), contains(ALL_GROUP_IDS_SORTED_BY_NAME));
    assertGroupListData(groups, asList(
        1, 1, 4, 4, 2, 2,
        0, 0,
        1, 0
    ));
    // root groups and no BLOCKED, DEACTIVATED, REMOVED, EXPIRED state
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onlyRootGroup()
        .userStatesToExclude(BLOCKED, DEACTIVATED, REMOVED, EXPIRED)
        .build());
    assertThat(groups, notNullValue());
    assertThat(extractData(groups, Group::getId), contains(
        GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID,
        GROUP_SP_1_ID, GROUP_SP_2_ID,
        GROUP_SQL_1_ID));
    assertGroupListData(groups, asList(
        0, 3, 7,
        2, 1,
        4
    ));
  }

  @Test
  public void rootGroupCriteria() throws AdminException {
    // without children
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onlyRootGroup()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID,
        GROUP_SP_1_ID, GROUP_SP_2_ID,
        GROUP_SQL_1_ID
    );
    // with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onlyRootGroup()
        .withChildren()
        .build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
  }

  @Test
  public void noCriteriaWithPagination() throws AdminException {
    // without children
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withPaginationPage(new PaginationPage(1, 3)).build());
    assertThat(groups.originalListSize(), is((long) ALL_GROUP_IDS_SORTED_BY_NAME.length));
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID);
    // without children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withPaginationPage(new PaginationPage(4, 2)).build());
    assertThat(groups.originalListSize(), is((long) ALL_GROUP_IDS_SORTED_BY_NAME.length));
    assertSortedGroupIds(groups, GROUP_SP_1_ID, GROUP_SP_2_ID);
    // with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withPaginationPage(new PaginationPage(4, 2))
        .withChildren()
        .build());
    assertThat(groups.originalListSize(), is((long) ALL_GROUP_IDS_SORTED_BY_NAME.length));
    assertSortedGroupIds(groups, GROUP_SP_1_ID, GROUP_SP_2_ID);
  }

  @Test
  public void idCriteria() throws Exception {
    // without children - verifying DELETED user not taken into account
    final List<SQLRequester.ResultLine> dbGroupRelations = getDbGroupRelations(GROUP_MIX_312_ID);
    assertThat(dbGroupRelations.size(), is(6));
    assertThat(dbGroupRelations.stream()
        .map(r -> "" + r.get("userid"))
        .filter(USER_SQLU15_2015_ID_DELETED::equals).count(), is(1L));
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_312_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_312_ID);
    final GroupDetail group = groups.iterator().next();
    assertGroupData(group);
    assertThat(group.getTotalNbUsers(), is(dbGroupRelations.size() - 1));

    // without children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_1_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
    // without children even it exists some
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_3_ID);
    // 2 ids without children even it exists some
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID, GROUP_MIX_1_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_MIX_3_ID);
    // with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID)
        .withChildren()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID);
    // with children and pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID)
        .withChildren()
        .withPaginationPage(new PaginationPage(3, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_311_ID);
    assertThat(groups.originalListSize(), is(4L));
    // 2 ids with children even it exists some
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID, GROUP_MIX_1_ID)
        .withChildren()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID);
    // 2 ids with children even it exists some and pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID, GROUP_MIX_1_ID)
        .withChildren()
        .withPaginationPage(new PaginationPage(1, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
    assertThat(groups.originalListSize(), is(5L));
    // 2 ids with children even it exists some and pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withIds(GROUP_MIX_3_ID, GROUP_SP_1_ID)
        .withChildren()
        .withPaginationPage(new PaginationPage(2, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_31_ID);
    assertThat(groups.originalListSize(), is(5L));
  }

  @Test
  public void parentIdCriteria() throws AdminException {
    // no sub group with a sub group without children
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_MIX_311_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(extractData(groups, Group::getId), empty());
    // no sub group with a root group without children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_SP_1_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(extractData(groups, Group::getId), empty());
    // two sub groups without children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_MIX_31_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_311_ID, GROUP_MIX_312_ID);
    // three sub groups without children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_MIX_3_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_31_ID);
    // three sub groups with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_MIX_3_ID)
        .withChildren()
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID);
    // three sub groups with children with pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withParentId(GROUP_MIX_3_ID)
        .withChildren()
        .withPaginationPage(new PaginationPage(3, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_312_ID);
    assertThat(groups.originalListSize(), is(3L));
  }

  @Test
  public void containingUserIdsCriteria() throws AdminException {
    // searching for a DELETED user
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .containingUserIds(USER_SQLU3_2003_ID_DELETED)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for a user which is not DELETED
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .containingUserIds(USER_SPU7_1007_ID_DEACTIVATED)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_2_ID, GROUP_MIX_312_ID);
    // searching for users which are not DELETED
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .containingUserIds(USER_SPU7_1007_ID_DEACTIVATED, USER_SPU8_1008_ID_VALID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_2_ID, GROUP_MIX_312_ID);
  }

  @Test
  public void containingUsersWithAccessLevelCriteria() throws AdminException {
    // searching for a DELETED user
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .containingUsersWithAccessLevel(UserAccessLevel.ADMINISTRATOR)
        .build());
    // Indeed, this filter is not handled.
    // Modifying this test if it becomes the case.
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
  }

  @Test
  public void domainCriteria() throws AdminException {
    // searching for mixed domain
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onMixedDomain()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID
    );
    // searching for mixed root domain
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onlyRootGroup()
        .onMixedDomain()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID
    );
    // searching for SP domain
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onDomainId(DOMAIN_SP_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_SP_1_ID, GROUP_SP_2_ID
    );
    // searching for MIXED domain and SP domain
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onMixedDomainAndDomain(DOMAIN_SP_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(extractData(groups, Group::getId), contains(
        GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID,
        GROUP_SP_1_ID, GROUP_SP_2_ID));
    assertGroupListData(groups, asList(
        1, 3, 7, 6, 5, 1,
        2, 1
    ));
  }

  @Test
  public void nameCriteria() throws AdminException {
    // a group in particular
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group MIX 3")
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_3_ID);
    // % character
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group % 1")
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID);
    // * character
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group * 1")
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID);
    // * character and with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group * 1")
        .withChildren()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID);
    // * character and with children and pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group * 1")
        .withChildren()
        .withPaginationPage(new PaginationPage(3, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_SQL_1_ID);
    // several % character occurrences
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName("Group % 1%")
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID, GROUP_SQL_11_ID);
    // several % character occurrences (root with children)
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .onlyRootGroup()
        .withChildren()
        .withName("Group % 1%")
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID, GROUP_SQL_11_ID);
    // ' character management
    GroupDetail group = admin.getGroup(GROUP_MIX_1_ID);
    final String mix1 = "Group MIX 1";
    final String mixApostrophe1 = "Group MIX l'1";
    assertThat(group.getName(), is(mix1));
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mix1)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mix1)
        .withChildren()
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mixApostrophe1)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    group.setName(mixApostrophe1);
    updateGroup(group);
    group = admin.getGroup(GROUP_MIX_1_ID);
    assertThat(group.getName(), is(mixApostrophe1));
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mix1)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mixApostrophe1)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withName(mixApostrophe1)
        .withChildren()
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID);
  }

  @Test
  public void componentIdCriteria() throws AdminException {
    // searching for almanach instance
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_MIX_3_ID, GROUP_SP_2_ID, GROUP_SQL_11_ID);
    // searching for almanach instance with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .withChildren()
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID,
        GROUP_SP_2_ID, GROUP_SQL_11_ID
    );
    // searching for kmelia instance
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_31_ID, GROUP_MIX_312_ID,
        GROUP_SP_1_ID, GROUP_SP_2_ID, GROUP_SQL_1_ID
    );
    // searching for kmelia instance with children
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withChildren()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedGroupIds(groups,
        GROUP_MIX_1_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID,
        GROUP_SP_1_ID, GROUP_SP_2_ID, GROUP_SQL_1_ID, GROUP_SQL_11_ID
    );
    // searching for kmelia instance which is PUBLIC for the next assertion
    setComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withChildren()
        .build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
    unsetComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    // searching for kmelia instance with children and pagination
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withChildren()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withPaginationPage(new PaginationPage(3, 1))
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_311_ID);
  }

  @Test
  public void componentIdAndResourceIdCriteria() throws AdminException {
    // searching for resource id without specifying a component id performs no resource id filtering
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
    // searching for blog instance and a node id with inherited rights returns no group
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for kmelia instance and a node id with inherited rights returns no group
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for kmelia instance and a sub node id with also inherited rights returns also no group
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_1_ID)
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for kmelia instance and a sub sub node id with specific rights
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .build());
    assertSortedGroupIds(groups, GROUP_SP_2_ID);
  }

  @Test
  public void componentIdAndRoleNameCriteria() throws AdminException {
    // searching for role name without specifying a component id performs no role name filtering
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedGroupIds(groups, ALL_GROUP_IDS_SORTED_BY_NAME);
    // searching for blog instance and wrong role name returns no group
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.supervisor.getName())
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for blog instance and a right role name
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_SP_1_ID);
    // searching for blog instance and a other right role name
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.writer.getName())
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_31_ID, GROUP_SQL_1_ID);
    // searching for blog instance and several right role names
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), SilverpeasRole.writer.getName())
        .build());
    assertSortedGroupIds(groups, GROUP_MIX_1_ID, GROUP_MIX_31_ID, GROUP_SP_1_ID, GROUP_SQL_1_ID);
  }

  @Test
  public void componentIdAndResourceIdAndRoleNamesCriteria() throws AdminException {
    // searching for kmelia instance, a node id with inherited rights and right role name returns
    // no group
    SilverpeasList<GroupDetail> groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and an empty role
    // name returns no group
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertThat(groups, notNullValue());
    assertThat(groups, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and a right role
    // name
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.writer.getName())
        .build());
    assertSortedGroupIds(groups, GROUP_SP_2_ID);
    // searching for kmelia instance and a sub sub node id with specific rights and several role
    // names
    groups = admin.searchGroups(newGroupSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), SilverpeasRole.writer.getName())
        .build());
    assertSortedGroupIds(groups, GROUP_SP_2_ID);
  }

  private void assertSortedGroupIds(final List<? extends Group> groups,
      final String... expectedIds) {
    assertThat(groups, notNullValue());
    final List<String> actual = extractData(groups, Group::getId);
    assertThat("Actual group ids: " + actual, actual, contains(expectedIds));
    assertGroupListData(groups);
  }

  private void assertGroupListData(final List<? extends Group> groups) {
    final List<Integer> expectedTotalNbUsers = new ArrayList<>();
    for (final Group group : groups) {
      final int index = ArrayUtils.indexOf(ALL_GROUP_IDS_SORTED_BY_NAME, group.getId());
      expectedTotalNbUsers.add(ALL_GROUP_TOTAL_NB_USERS_SORTED_BY_NAME[index]);
    }
    assertGroupListData(groups, expectedTotalNbUsers);
  }

  private void assertGroupListData(final List<? extends Group> groups,
      final List<Integer> expectedTotalNbUsers) {
    final List<Integer> expectedNbUsers = new ArrayList<>();
    for (final Group group : groups) {
      final int index = ArrayUtils.indexOf(ALL_GROUP_IDS_SORTED_BY_NAME, group.getId());
      expectedNbUsers.add(ALL_GROUP_NB_USERS_SORTED_BY_NAME[index]);
    }
    final List<Integer> actualNbUsers = extractData(groups, Group::getNbUsers);
    assertThat("Nb Users (not filled) " + actualNbUsers,
        actualNbUsers, contains(expectedNbUsers.toArray()));
    final List<Integer> actualTotalNbUsers = extractData(groups, Group::getTotalNbUsers);
    assertThat("Total nb Users (filled) " + actualTotalNbUsers,
        actualTotalNbUsers, contains(expectedTotalNbUsers.toArray()));
  }

  private void assertGroupData(final Group group) {
    final int index = ArrayUtils.indexOf(ALL_GROUP_IDS_SORTED_BY_NAME, group.getId());
    assertThat("Nb Users (not filled)", group.getNbUsers(),
        is(ALL_GROUP_NB_USERS_SORTED_BY_NAME[index]));
    assertThat("Total nb Users (filled)",group.getTotalNbUsers(),
        is(ALL_GROUP_TOTAL_NB_USERS_SORTED_BY_NAME[index]));
  }
}
