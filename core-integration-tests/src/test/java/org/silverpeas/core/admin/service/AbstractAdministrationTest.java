/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.integration.rule.TestStatisticRule;
import org.silverpeas.core.test.integration.SQLRequester;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Look at CommonAdministrationIT.ods to get a better view of data.
 * @author silveryocha
 */
@SuppressWarnings("SameParameterValue")
public abstract class AbstractAdministrationTest {

  private static final String SCRIPTS_PATH =
      "/" + AbstractAdministrationTest.class.getPackage().getName().replaceAll("\\.", "/");

  private static final String COMMON_TABLE_CREATION_SCRIPTS =
      SCRIPTS_PATH + "/create-administration-database.sql";

  private static final String COMMON_DATA_INSERTION_SCRIPTS =
      SCRIPTS_PATH + "/insert-administration-database.sql";

  static final String DOMAIN_SP_ID = "0";
  static final String DOMAIN_SQL_ID = "1";

  static final String USER_ADM_0_ID_VALID = "0";
  static final String USER_SPU1_1001_ID_VALID = "1001";
  static final String USER_SPU2_1002_ID_BLOCKED = "1002";
  static final String USER_SPU3_1003_ID_EXPIRED = "1003";
  static final String USER_SPU4_1004_ID_VALID = "1004";
  static final String USER_SPU5_1005_ID_VALID = "1005";
  static final String USER_SPU6_1006_ID_VALID = "1006";
  static final String USER_SPU7_1007_ID_DEACTIVATED = "1007";
  static final String USER_SPU8_1008_ID_VALID = "1008";
  static final String USER_SPU9_1009_ID_VALID = "1009";
  static final String USER_SPU10_1010_ID_VALID = "1010";
  static final String USER_SPU11_1011_ID_REMOVED = "1011";
  static final String USER_SPU12_1012_ID_VALID = "1012";
  static final String USER_SPU13_1013_ID_VALID = "1013";
  static final String USER_SPU14_1014_ID_VALID = "1014";
  static final String USER_SPU15_1015_ID_DELETED = "1015";
  static final String USER_SPU16_1016_ID_VALID = "1016";
  static final String USER_SQLU1_2001_ID_VALID = "2001";
  static final String USER_SQLU2_2002_ID_VALID = "2002";
  static final String USER_SQLU3_2003_ID_DELETED = "2003";
  static final String USER_SQLU4_2004_ID_VALID = "2004";
  static final String USER_SQLU5_2005_ID_VALID = "2005";
  static final String USER_SQLU6_2006_ID_REMOVED = "2006";
  static final String USER_SQLU7_2007_ID_VALID = "2007";
  static final String USER_SQLU8_2008_ID_DELETED = "2008";
  static final String USER_SQLU9_2009_ID_VALID = "2009";
  static final String USER_SQLU10_2010_ID_VALID = "2010";
  static final String USER_SQLU11_2011_ID_VALID = "2011";
  static final String USER_SQLU12_2012_ID_REMOVED = "2012";
  static final String USER_SQLU13_2013_ID_VALID = "2013";
  static final String USER_SQLU14_2014_ID_VALID = "2014";
  static final String USER_SQLU15_2015_ID_DELETED = "2015";

  static final String[] SP_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME = new String[]{
      USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID, USER_SPU10_1010_ID_VALID,
      USER_SPU11_1011_ID_REMOVED,
      USER_SPU12_1012_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID, USER_SPU16_1016_ID_VALID,
      USER_SPU2_1002_ID_BLOCKED, USER_SPU3_1003_ID_EXPIRED,
      USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
      USER_SPU7_1007_ID_DEACTIVATED,
      USER_SPU8_1008_ID_VALID, USER_SPU9_1009_ID_VALID
  };

  static final String[] SQL_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME = new String[]{
      USER_SQLU1_2001_ID_VALID, USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID,
      USER_SQLU12_2012_ID_REMOVED,
      USER_SQLU13_2013_ID_VALID, USER_SQLU14_2014_ID_VALID,
      USER_SQLU2_2002_ID_VALID,
      USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID,
      USER_SQLU6_2006_ID_REMOVED,
      USER_SQLU7_2007_ID_VALID,
      USER_SQLU9_2009_ID_VALID
  };

  static final String[] ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME = Stream
      .concat(Stream.of(SP_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME),
              Stream.of(SQL_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME))
      .toArray(String[]::new);

  static final String GROUP_MIX_1_ID = "1";
  static final String GROUP_MIX_2_ID = "2";
  static final String GROUP_MIX_3_ID = "3";
  static final String GROUP_MIX_31_ID = "31";
  static final String GROUP_MIX_311_ID = "311";
  static final String GROUP_MIX_312_ID = "312";
  static final String GROUP_SP_1_ID = "1001";
  static final String GROUP_SP_2_ID = "1002";
  static final String GROUP_SQL_1_ID = "2001";
  static final String GROUP_SQL_11_ID = "2011";

  static final String[] ALL_GROUP_IDS_SORTED_BY_NAME = new String[] {
      GROUP_MIX_1_ID, GROUP_MIX_2_ID, GROUP_MIX_3_ID, GROUP_MIX_31_ID, GROUP_MIX_311_ID, GROUP_MIX_312_ID,
      GROUP_SP_1_ID, GROUP_SP_2_ID,
      GROUP_SQL_1_ID, GROUP_SQL_11_ID
  };

  static final Integer[] ALL_GROUP_NB_USERS_SORTED_BY_NAME = new Integer[] {
      0, 0, 0, 0, 0, 0,
      0, 0,
      0, 0
  };

  static final Integer[] ALL_GROUP_TOTAL_NB_USERS_SORTED_BY_NAME = new Integer[] {
      1, 4, 11, 10, 5, 5,
      2, 2,
      5, 3
  };

  static final String INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID = "1";
  static final String INSTANCE_SPACE_A_LEVEL_2_BLOG_ID = "2";
  static final String INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID = "3";

  static final String INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID = "10";
  static final String INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_1_ID = "101";
  static final String INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID = "1011";

  @Inject
  Administration admin;

  @Inject
  private UserManager userManager;

  @Inject
  private GroupManager groupManager;

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  /**
   * Look at CommonAdministrationIT.ods to get a better view of data.
   */
  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule
      .createTablesFrom(COMMON_TABLE_CREATION_SCRIPTS)
      .loadInitialDataSetFrom(COMMON_DATA_INSERTION_SCRIPTS);

  @Rule
  public TestStatisticRule testStatisticRule = new TestStatisticRule();

  @Before
  public void setup() throws Exception {
    final File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.getInstance().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
    WAComponentRegistry.get().init();
    PersonalComponentRegistry.get().init();
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @After
  public void tearDown() {
    admin.reloadCache();
  }

  <T, G> List<T> extractData(final List<G> groups,
      final Function<G, T> idMapper) {
    return groups.stream().map(idMapper).collect(Collectors.toList());
  }

  void executePlainQueries(final String... plainQueries)
      throws SQLException {
    try (final Connection connection = DbSetupRule.getSafeConnection()) {
      for(final String query : plainQueries) {
        try (final PreparedStatement ps = connection.prepareStatement(query)) {
          testStatisticRule.log("registering " + ps.executeUpdate() + " lines");
        }
      }
    }
  }

  /**
   * Returns relation registered into database between a group and users.
   * @param id the id of group.
   * @return list of lines represented by a map between column name and value.
   */
  List<SQLRequester.ResultLine> getDbGroupRelations(String id) throws Exception {
    return dbSetupRule.mapJdbcSqlQueryResultAsListOfMappedValues(JdbcSqlQuery
        .select("* from st_group_user_rel")
        .where("groupid = ?", Integer.parseInt(id)));
  }

  void setComponentInstanceAsPublic(final String instanceLocalId) {
    Transaction.performInOne(
        JdbcSqlQuery.update("st_componentinstance")
            .withSaveParam("ispublic", 1, false)
            .where("id = ?", Integer.parseInt(instanceLocalId))::execute);
    admin.reloadCache();
  }

  void unsetComponentInstanceAsPublic(final String instanceLocalId) {
    Transaction.performInOne(
        JdbcSqlQuery.update("st_componentinstance")
            .withSaveParam("ispublic", 0, false)
            .where("id = ?", Integer.parseInt(instanceLocalId))::execute);
    admin.reloadCache();
  }

  UserSearchCriteriaBuilder newUserSearchCriteriaBuilder() {
    return new UserSearchCriteriaBuilder();
  }

  GroupSearchCriteriaBuilder newGroupSearchCriteriaBuilder() {
    return new GroupSearchCriteriaBuilder();
  }

  void updateUser(final UserDetail user) {
    Transaction.performInOne(() -> userManager.updateUser(user, false));
    admin.reloadCache();
  }

  void updateGroup(final GroupDetail group) {
    Transaction.performInOne(() -> groupManager.updateGroup(group, true));
    admin.reloadCache();
  }

  static class UserSearchCriteriaBuilder {
    private final UserDetailsSearchCriteria searchCriteria;

    private UserSearchCriteriaBuilder() {
      searchCriteria = new UserDetailsSearchCriteria();
    }

    UserDetailsSearchCriteria build() {
      return searchCriteria;
    }

    UserSearchCriteriaBuilder withUserStatesToExclude(final UserState... states) {
      searchCriteria.onUserStatesToExclude(states);
      return this;
    }

    UserSearchCriteriaBuilder withAccessLevels(final UserAccessLevel... accessLevels) {
      searchCriteria.onAccessLevels(accessLevels);
      return this;
    }

    UserSearchCriteriaBuilder withPagination(final PaginationPage page) {
      searchCriteria.onPagination(page);
      return this;
    }

    UserSearchCriteriaBuilder withSpecificIds(final String... ids) {
      searchCriteria.onUserSpecificIds(ids);
      return this;
    }

    UserSearchCriteriaBuilder withDomainIds(final String... ids) {
      searchCriteria.onDomainIds(ids);
      return this;
    }

    UserSearchCriteriaBuilder withUserIds(final String... ids) {
      searchCriteria.onUserIds(ids);
      return this;
    }

    UserSearchCriteriaBuilder withName(final String name) {
      searchCriteria.onName(name);
      return this;
    }

    UserSearchCriteriaBuilder withGroupIds(final String... ids) {
      searchCriteria.onGroupIds(ids);
      return this;
    }

    UserSearchCriteriaBuilder withComponentId(final String instanceIds) {
      searchCriteria.onComponentInstanceId(instanceIds);
      return this;
    }

    UserSearchCriteriaBuilder withNodeId(final String nodeId) {
      searchCriteria.onResourceId("O" + nodeId);
      return this;
    }

    UserSearchCriteriaBuilder withRoleNames(final String... roleNames) {
      searchCriteria.onRoleNames(roleNames);
      return this;
    }

    UserSearchCriteriaBuilder matchingAllRoleNames() {
      searchCriteria.matchingAllRoleNames();
      return this;
    }

    UserSearchCriteriaBuilder withFirstName(final String firstName) {
      searchCriteria.onFirstName(firstName);
      return this;
    }

    UserSearchCriteriaBuilder withLastName(final String lastName) {
      searchCriteria.onLastName(lastName);
      return this;
    }
  }

  static class GroupSearchCriteriaBuilder {
    private final GroupsSearchCriteria searchCriteria;
    private String domainId = null;
    private boolean withMixedDomain = false;

    private GroupSearchCriteriaBuilder() {
      searchCriteria = new GroupsSearchCriteria();
    }

    GroupsSearchCriteria build() {
      if (withMixedDomain) {
        searchCriteria.onMixedDomainOrOnDomainId(domainId);
      } else if (isDefined(domainId)) {
        searchCriteria.onDomainIds(domainId);
      }
      return searchCriteria;
    }

    GroupSearchCriteriaBuilder withIds(final String... ids) {
      searchCriteria.onGroupIds(ids);
      return this;
    }

    GroupSearchCriteriaBuilder withParentId(String groupId) {
      if (isDefined(groupId)) {
        searchCriteria.onSuperGroupId(groupId);
      }
      return this;
    }

    GroupSearchCriteriaBuilder withChildren() {
      searchCriteria.withChildren();
      return this;
    }

    GroupSearchCriteriaBuilder withPaginationPage(final PaginationPage page) {
      searchCriteria.onPagination(page);
      return this;
    }

    GroupSearchCriteriaBuilder onlyRootGroup() {
      searchCriteria.onAsRootGroup();
      return this;
    }

    GroupSearchCriteriaBuilder userStatesToExclude(final UserState... states) {
      searchCriteria.onUserStatesToExclude(states);
      return this;
    }

    GroupSearchCriteriaBuilder containingUserIds(final String... ids) {
      searchCriteria.onUserIds(ids);
      return this;
    }

    GroupSearchCriteriaBuilder onMixedDomain() {
      this.withMixedDomain = true;
      return this;
    }

    GroupSearchCriteriaBuilder onDomainId(final String id) {
      this.domainId = id;
      return this;
    }

    GroupSearchCriteriaBuilder onMixedDomainAndDomain(final String id) {
      this.domainId = id;
      return onMixedDomain();
    }

    GroupSearchCriteriaBuilder withName(final String name) {
      if (isDefined(name)) {
        searchCriteria.onName(name);
      }
      return this;
    }

    GroupSearchCriteriaBuilder withComponentId(final String id) {
      searchCriteria.onComponentInstanceId(id);
      return this;
    }

    GroupSearchCriteriaBuilder withNodeId(final String nodeId) {
      searchCriteria.onResourceId("O" + nodeId);
      return this;
    }

    GroupSearchCriteriaBuilder withRoleNames(final String... roleNames) {
      searchCriteria.onRoleNames(roleNames);
      return this;
    }

    GroupSearchCriteriaBuilder matchingAllRoleNames() {
      searchCriteria.matchingAllRoleNames();
      return this;
    }

    public GroupSearchCriteriaBuilder containingUsersWithAccessLevel(
        final UserAccessLevel... accessLevels) {
      searchCriteria.onAccessLevels(accessLevels);
      return this;
    }
  }
}
