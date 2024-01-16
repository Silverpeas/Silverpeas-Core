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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.SilverpeasList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.admin.user.constant.UserAccessLevel.*;
import static org.silverpeas.core.admin.user.constant.UserState.*;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.writer;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.truncate;

/**
 * Look at CommonAdministrationIT.ods to get a better view of data.
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class AdministrationSearchUserIT extends AbstractAdministrationTest {

  private static final int DEFAULT_MASSIVE_NB_USERS = 50000;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AdministrationSearchGroupIT.class)
        .addCommonBasicUtilities().addPublicationTemplateFeatures().addSilverpeasExceptionBases()
        .testFocusedOn((w) -> ((WarBuilder4LibCore) w).addAdministrationFeatures()).build();
  }

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void noCriteria() throws AdminException {
    final SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder().build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
  }

  @Test
  public void accessLevelCriteria() throws AdminException {
    // administrators
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withAccessLevels(ADMINISTRATOR)
        .build());
    assertSortedUserIds(users, USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID);
    // users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withAccessLevels(USER)
        .build());
    assertSortedUserIds(users, Stream.of(ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME)
        .filter(i -> !USER_ADM_0_ID_VALID.equals(i))
        .filter(i -> !USER_SPU1_1001_ID_VALID.equals(i))
        .toArray(String[]::new));
    // users with pagination
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withAccessLevels(USER)
        .withPagination(new PaginationPage(3, 2))
        .build());
    assertSortedUserIds(users, USER_SPU14_1014_ID_VALID, USER_SPU2_1002_ID_BLOCKED);
    // pdc managers
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withAccessLevels(PDC_MANAGER)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // administrators, users and pdc managers
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withAccessLevels(ADMINISTRATOR, USER, PDC_MANAGER)
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
  }

  @Test
  public void excludeUserStatesCriteria() throws AdminException {
    // excluding removed
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserStatesToExclude(REMOVED)
        .build());
    assertSortedUserIds(users, Stream.of(ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME)
        .filter(i -> !USER_SPU11_1011_ID_REMOVED.equals(i))
        .filter(i -> !USER_SQLU12_2012_ID_REMOVED.equals(i))
        .filter(i -> !USER_SQLU6_2006_ID_REMOVED.equals(i))
        .toArray(String[]::new));
    // excluding blocked, expired and deactivated
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserStatesToExclude(BLOCKED, EXPIRED, DEACTIVATED)
        .build());
    assertSortedUserIds(users, Stream.of(ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME)
        .filter(i -> !USER_SPU2_1002_ID_BLOCKED.equals(i))
        .filter(i -> !USER_SPU3_1003_ID_EXPIRED.equals(i))
        .filter(i -> !USER_SPU7_1007_ID_DEACTIVATED.equals(i))
        .toArray(String[]::new));
    // excluding blocked, expired and deactivated with pagination
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserStatesToExclude(BLOCKED, EXPIRED, DEACTIVATED)
        .withPagination(new PaginationPage(3, 2))
        .build());
    assertSortedUserIds(users, USER_SPU12_1012_ID_VALID, USER_SPU13_1013_ID_VALID);
  }

  @Test
  public void domainIdsCriteria() throws AdminException {
    // SP domain
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID)
        .build());
    assertSortedUserIds(users, SP_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    // SQL domain
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SQL_ID)
        .build());
    assertSortedUserIds(users, SQL_DOMAIN_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    // both
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID, DOMAIN_SQL_ID)
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
  }

  @Test
  public void specificIdsCriteria() throws AdminException {
    // if no domain id is defined, no filtering is done on specific ids
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withSpecificIds("11")
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    // domain SP, a user
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID)
        .withSpecificIds("11")
        .build());
    assertSortedUserIds(users, USER_SPU11_1011_ID_REMOVED);
    // a user of domain SP, bu domain SQL aimed
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SQL_ID)
        .withSpecificIds("11")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // an unknown one
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID)
        .withSpecificIds("16768767")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // several ones (with one deleted, 30)
    final String[] specificIds = {"11", "12", "13", "15"};
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID)
        .withSpecificIds(specificIds)
        .build());
    assertSortedUserIds(users,
        USER_SPU11_1011_ID_REMOVED, USER_SPU12_1012_ID_VALID, USER_SPU13_1013_ID_VALID);
    // several ones (with one deleted, 30) by excluding also the valid ones
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID)
        .withSpecificIds(specificIds)
        .withUserStatesToExclude(VALID)
        .build());
    assertSortedUserIds(users, USER_SPU11_1011_ID_REMOVED);
  }

  @Test
  public void specificIdsCriteriaError() throws AdminException {
    exceptionRule.expect(AdminException.class);
    exceptionRule.expectMessage("Fail to get users matching some criteria");
    // getting an error if several domain ids criterion is set with the specific ids ones
    admin.searchUsers(newUserSearchCriteriaBuilder()
        .withDomainIds(DOMAIN_SP_ID, DOMAIN_SQL_ID)
        .withSpecificIds("11")
        .build());
  }

  @Test
  public void idCriteria() throws AdminException {
    // single one
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserIds(USER_SPU7_1007_ID_DEACTIVATED)
        .build());
    assertSortedUserIds(users, USER_SPU7_1007_ID_DEACTIVATED);
    // deleted one
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserIds(USER_SQLU8_2008_ID_DELETED)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // several ones
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserIds(USER_SPU7_1007_ID_DEACTIVATED, USER_SPU2_1002_ID_BLOCKED,
            USER_SQLU5_2005_ID_VALID, USER_SPU15_1015_ID_DELETED,
            USER_SQLU10_2010_ID_VALID)
        .build());
    assertSortedUserIds(users,
        USER_SPU2_1002_ID_BLOCKED, USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU10_2010_ID_VALID, USER_SQLU5_2005_ID_VALID);
    // several ones with pagination
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserIds(USER_SPU7_1007_ID_DEACTIVATED, USER_SPU2_1002_ID_BLOCKED,
            USER_SQLU5_2005_ID_VALID, USER_SPU15_1015_ID_DELETED,
            USER_SQLU10_2010_ID_VALID)
        .withPagination(new PaginationPage(3, 1))
        .build());
    assertSortedUserIds(users, USER_SQLU10_2010_ID_VALID);
  }

  @Test
  public void nameCriteria() throws AdminException {
    // unknown
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("NO_NAME_LIKE_THAT")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one, but deleted
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu15")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one on first name
    final String spu14FirstName = "spu14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName(spu14FirstName)
        .build());
    assertSortedUserIds(users, USER_SPU14_1014_ID_VALID);
    // strict one on last name (case insensitive)
    final String spUser14LastName = "sP uSeR 14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName(spUser14LastName)
        .build());
    assertSortedUserIds(users, USER_SPU14_1014_ID_VALID);
    // not searching on concatenation of first name and last name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName(spu14FirstName + "%" + spUser14LastName)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // not searching on concatenation of last name and first name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName(spUser14LastName + "%" + spu14FirstName)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu%1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // magical character *
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu*1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // several magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("s%u%1")
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU11_2011_ID_VALID);
    // several magical character % *
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("s*u%1")
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU11_2011_ID_VALID);
    // ' character management
    UserDetail user = admin.getUserDetail(USER_SPU1_1001_ID_VALID);
    final String name = "spu1 SP USER 1";
    final String nameWithApostrophe = "spu'1 SP USER l'1";
    assertThat(user.getDisplayedName(), is(name));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("SP USER 1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu'1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("SP USER l'1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    user.setFirstName("spu'1");
    user.setLastName("SP USER l'1");
    updateUser(user);
    assertThat(user.getDisplayedName(), is(nameWithApostrophe));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("SP USER 1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("spu'1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withName("SP USER l'1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
  }

  @Test
  public void firstNameCriteria() throws AdminException {
    // unknown
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("NO_FIRST_NAME_LIKE_THAT")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one, but deleted
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu15")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one on first name
    final String spu14FirstName = "spu14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName(spu14FirstName)
        .build());
    assertSortedUserIds(users, USER_SPU14_1014_ID_VALID);
    // strict one on last name (case insensitive)
    final String spUser14LastName = "sP uSeR 14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName(spUser14LastName)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu%1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // magical character *
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu*1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // several magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("s%u%1")
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU11_2011_ID_VALID);
    // ' character management
    UserDetail user = admin.getUserDetail(USER_SPU1_1001_ID_VALID);
    final String name = "spu1 SP USER 1";
    final String nameWithApostrophe = "spu'1 SP USER 1";
    assertThat(user.getDisplayedName(), is(name));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu'1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    user.setFirstName("spu'1");
    updateUser(user);
    assertThat(user.getDisplayedName(), is(nameWithApostrophe));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withFirstName("spu'1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
  }

  @Test
  public void lastNameCriteria() throws AdminException {
    // unknown
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("NO_LAST_NAME_LIKE_THAT")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one, but deleted
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("SP USER 15")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one on first name
    final String spu14FirstName = "spu14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName(spu14FirstName)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // strict one on last name (case insensitive)
    final String spUser14LastName = "sP uSeR 14";
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName(spUser14LastName)
        .build());
    assertSortedUserIds(users, USER_SPU14_1014_ID_VALID);
    // magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("sp user %1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // magical character *
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("sp user *1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED);
    // several magical character %
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("s%u%1")
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU11_1011_ID_REMOVED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU11_2011_ID_VALID);
    // ' character management
    UserDetail user = admin.getUserDetail(USER_SPU1_1001_ID_VALID);
    final String name = "spu1 SP USER 1";
    final String nameWithApostrophe = "spu1 SP USER l'1";
    assertThat(user.getDisplayedName(), is(name));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("SP USER 1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName(nameWithApostrophe)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    user.setLastName("SP USER l'1");
    updateUser(user);
    assertThat(user.getDisplayedName(), is(nameWithApostrophe));
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("SP USER 1")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withLastName("SP USER l'1")
        .build());
    assertSortedUserIds(users, USER_SPU1_1001_ID_VALID);
  }

  @Test
  public void groupIdsCriteria() throws AdminException {
    // not existing group
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds("-189")
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // any users in a group
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID, USER_SPU10_1010_ID_VALID,
        USER_SPU11_1011_ID_REMOVED,
        USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SPU8_1008_ID_VALID,
        USER_SQLU1_2001_ID_VALID, USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID,
        USER_SQLU12_2012_ID_REMOVED,
        USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID,
        USER_SQLU6_2006_ID_REMOVED,
        USER_SQLU9_2009_ID_VALID
    );
    // an existing group id
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(GROUP_MIX_31_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU10_1010_ID_VALID,
        USER_SPU11_1011_ID_REMOVED,
        USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU10_2010_ID_VALID,
        USER_SQLU12_2012_ID_REMOVED,
        USER_SQLU14_2014_ID_VALID
    );
    // several group ids
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(GROUP_MIX_1_ID, GROUP_SQL_1_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SQLU11_2011_ID_VALID, USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID,
        USER_SQLU6_2006_ID_REMOVED, USER_SQLU9_2009_ID_VALID
    );
    // several group ids with pagination
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(GROUP_MIX_1_ID, GROUP_SQL_1_ID)
        .withPagination(new PaginationPage(3, 1))
        .build());
    assertSortedUserIds(users, USER_SQLU4_2004_ID_VALID);
    // several group ids and valid state with pagination
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(GROUP_MIX_1_ID, GROUP_SQL_1_ID)
        .withUserStatesToExclude(BLOCKED, REMOVED)
        .withPagination(new PaginationPage(4, 1))
        .build());
    assertSortedUserIds(users, USER_SQLU9_2009_ID_VALID);
  }

  @Test
  public void componentIdCriteria() throws AdminException {
    // searching for almanach instance, in that case REMOVED users are not taken into account
    // because when searching on instance id, the filtering is done by applying filters on user
    // and group rights which are using services filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // same search with a right full component instance id
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId("almanach" + INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // same search with a wrong full component instance id
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId("kmelia" + INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for almanach instance, in that case REMOVED users are not taken into account
    // because when searching on instance id, the filtering is done by applying filters on user
    // and group rights which are using services filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_B_LEVEL_1_ALMANACH_ID)
        .withGroupIds(GROUP_SP_1_ID)
        .build());
    assertSortedUserIds(users, USER_SPU4_1004_ID_VALID);
    // searching for kmelia instance which is PUBLIC for the next assertion
    setComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    unsetComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    // searching for kmelia instance which is PUBLIC for the next assertion and a group not on kmelia
    setComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withGroupIds(GROUP_MIX_311_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID, USER_SPU10_1010_ID_VALID,
        USER_SPU11_1011_ID_REMOVED,
        USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED);
    unsetComponentInstanceAsPublic(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID);
    // searching for kmelia instance, in that case REMOVED users are not taken into account
    // because when searching on instance id, the filtering is done by applying filters on user
    // and group rights which are using services filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SPU8_1008_ID_VALID,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // searching for kmelia instance, in that case REMOVED users are not taken into account
    // because when searching on instance id, the filtering is done by applying filters on user
    // and group rights which are using services filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // searching for kmelia instance and a group in kmelia, in that case REMOVED users are not
    // taken into account because when searching on instance id, the filtering is done by applying
    // filters on user and group rights which are using services filtering on DELETED and REMOVED
    // users. (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withGroupIds(GROUP_SQL_1_ID)
        .build());
    assertSortedUserIds(users,
        USER_SQLU11_2011_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // searching for kmelia instance and a group in kmelia resource with specific rights, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users. (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withGroupIds(GROUP_SP_2_ID)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID);
    // searching for kmelia instance and a group not linked to kmelia, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users. (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withGroupIds(GROUP_MIX_2_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU6_1006_ID_VALID, USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU14_2014_ID_VALID
    );
  }

  @Test
  public void personalComponentIdCriteria() throws AdminException {
    final User aUser = admin.getUserDetail(USER_SPU1_1001_ID_VALID);
    final PersonalComponent personalComponent = PersonalComponent.getAll().iterator().next();
    // searching for personal instance
    final String personalComponentInstanceId = PersonalComponentInstance.from(aUser, personalComponent).getId();
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .build());
    assertSortedUserIds(users, aUser.getId());
    // searching for personal instance and a right role name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedUserIds(users, aUser.getId());
    // searching for personal instance and a wrong role name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withRoleNames(writer.getName())
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for personal instance and a group
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withGroupIds(GROUP_MIX_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for personal instance and a right user id
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withUserIds(aUser.getId())
        .build());
    assertSortedUserIds(users, aUser.getId());
    // searching for personal instance and a right user id and a group
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withUserIds(aUser.getId())
        .withGroupIds(GROUP_MIX_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for personal instance and a wrong user id
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withUserIds(USER_SPU6_1006_ID_VALID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for personal instance and a wrong user id and a group
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(personalComponentInstanceId)
        .withUserIds(USER_SPU6_1006_ID_VALID)
        .withGroupIds(GROUP_MIX_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
  }

  @Test
  public void componentIdAndResourceIdCriteria() throws AdminException {
    // searching for resource id without specifying a component id performs no resource id filtering
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    // searching for blog instance and a node id with inherited rights returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for blog instance and a node id with inherited rights and a group id returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .withGroupIds(GROUP_SP_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a node id with inherited rights returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a node id with inherited rights and a group id returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .withGroupIds(GROUP_MIX_3_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub node id with also inherited rights returns also no
    // users, in that case REMOVED users are not taken into account because when searching on
    // instance id, the filtering is done by applying filters on user and group rights which are
    // using services filtering on DELETED and REMOVED users.
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub sub node id with specific rights, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID, USER_SQLU1_2001_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a group id
    // linked to node, in that case REMOVED users are not taken into account because when
    // searching on instance id, the filtering is done by applying filters on user and group
    // rights which are using services filtering on DELETED and REMOVED users. (BE AWARE of that
    // users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withGroupIds(GROUP_SP_2_ID)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a group id
    // linked to node, in that case REMOVED users are not taken into account because when
    // searching on instance id, the filtering is done by applying filters on user and group
    // rights which are using services filtering on DELETED and REMOVED users. (BE AWARE of that
    // users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a group id
    // not linked to node, in that case REMOVED users are not taken into account because when
    // searching on instance id, the filtering is done by applying filters on user and group
    // rights which are using services filtering on DELETED and REMOVED users. (BE AWARE of that
    // users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withGroupIds(GROUP_MIX_31_ID)
        .build());
    assertSortedUserIds(users, USER_SQLU1_2001_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a group id
    // not linked to node, in that case REMOVED users are not taken into account because when
    // searching on instance id, the filtering is done by applying filters on user and group
    // rights which are using services filtering on DELETED and REMOVED users. (BE AWARE of that
    // users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withGroupIds(GROUP_SQL_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
  }

  @Test
  public void componentIdAndRoleNameCriteria() throws AdminException {
    // searching for role name id without specifying a component id performs no resource id filtering
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedUserIds(users, ALL_NOT_DELETED_USER_IDS_SORTED_BY_NAME);
    // searching for blog instance and wrong role name returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.supervisor.getName())
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for blog instance and wrong role name and a group id returns no users
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.supervisor.getName())
        .withGroupIds(GROUP_SP_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for blog instance and a right role name, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID,
        USER_SQLU9_2009_ID_VALID
    );
    // searching for blog instance and a right role name and an admin group id, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .withGroupIds(GROUP_MIX_1_ID)
        .build());
    assertSortedUserIds(users, USER_SPU2_1002_ID_BLOCKED);
    // searching for blog instance and a right role name and an admin group id, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertSortedUserIds(users,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID
    );
    // searching for blog instance and a right role name and not linked group id, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .withGroupIds(GROUP_MIX_2_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a group not linked to kmelia, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users. (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(writer.getName())
        .withGroupIds(GROUP_MIX_2_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU6_1006_ID_VALID, USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU14_2014_ID_VALID
    );
    // searching for blog instance and a other right role name, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(writer.getName())
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU10_2010_ID_VALID,
        USER_SQLU11_2011_ID_VALID, USER_SQLU13_2013_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID,
        USER_SQLU9_2009_ID_VALID
    );
    // searching for blog instance and a other right role name and an admin group id, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(writer.getName())
        .withGroupIds(GROUP_SP_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for blog instance and several right role names, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .build());
    assertSortedUserIds(users,
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID, USER_SQLU10_2010_ID_VALID,
        USER_SQLU11_2011_ID_VALID, USER_SQLU13_2013_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID,
        USER_SQLU9_2009_ID_VALID
    );
    // searching for blog instance and several right role names and group ids, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    //
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .withGroupIds(GROUP_SP_1_ID, GROUP_SQL_1_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID,
        USER_SQLU11_2011_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
    // searching for blog instance and several right role names and group ids, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .withGroupIds(GROUP_SP_1_ID, GROUP_SQL_11_ID)
        .build());
    assertSortedUserIds(users,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID,
        USER_SQLU11_2011_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
  }

  @Test
  public void componentIdAndResourceIdAndRoleNamesCriteria() throws AdminException {
    // searching for kmelia instance, a node id with inherited rights and right role name returns
    // no user
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ROOT_FOLDER_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and an empty role
    // name returns no user
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and an empty role
    // and a writer group id name returns no user
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .withGroupIds(GROUP_SP_2_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and an empty role
    // and a writer group id name returns no user
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    // searching for kmelia instance and a sub sub node id with specific rights and a right role
    // name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(writer.getName())
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID, USER_SQLU1_2001_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a right role
    // and a writer group id name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(writer.getName())
        .withGroupIds(GROUP_SP_2_ID)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and a right role
    // and a writer group id name
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(writer.getName())
        .withGroupIds(UserDetailsSearchCriteria.ANY_GROUPS)
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and several role
    // names
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .build());
    assertSortedUserIds(users, USER_SPU13_1013_ID_VALID, USER_SQLU1_2001_ID_VALID);
    // searching for kmelia instance and a sub sub node id with specific rights and several role
    // names and a group on component
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .withGroupIds(GROUP_SQL_1_ID)
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
  }

  @Test
  public void componentIdAndRoleNameAndUserIdCriteria() throws AdminException {
    // searching for blog instance and several right role names and users ids, in that
    // case REMOVED users are not taken into account because when searching on instance id, the
    // filtering is done by applying filters on user and group rights which are using services
    // filtering on DELETED and REMOVED users.
    // (BE AWARE of that users of groups are also retrieved)
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_2_BLOG_ID)
        .withRoleNames(SilverpeasRole.admin.getName(), writer.getName())
        .withUserIds(USER_SPU2_1002_ID_BLOCKED, USER_SQLU4_2004_ID_VALID, USER_SPU13_1013_ID_VALID)
        .build());
    assertSortedUserIds(users, USER_SPU2_1002_ID_BLOCKED, USER_SQLU4_2004_ID_VALID);
  }

  @Test
  public void hugeSetOfUserIdsCriteria() throws Exception {
    final List<String> userIds = create50000UsersAttachedToGroupMIX1();
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    final SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withUserIds(new HashSet<>(userIds).toArray(new String[0]))
        .build());
    assertSortedUserIds(users, userIds.toArray(new String[0]));
  }

  @Test
  public void groupIdWithLotOfUsersCriteria() throws Exception {
    final List<String> userIds = create50000UsersAttachedToGroupMIX1();
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    final SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withGroupIds(GROUP_MIX_1_ID)
        .build());
    assertSortedUserIds(users, Stream.concat(Stream.of(USER_SPU2_1002_ID_BLOCKED), userIds.stream())
        .toArray(String[]::new));
  }

  @Test
  public void componentIdWithGroupIdWithLotOfUsersCriteria() throws Exception {
    final List<String> userIds = create50000UsersAttachedToGroupMIX1();
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    final SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedUserIds(users, Stream.concat(Stream.of(
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SPU8_1008_ID_VALID,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID), userIds.stream())
        .toArray(String[]::new)
    );
  }

  @Test
  public void componentIdWithLotOfUsersAccessingInstanceCriteria() throws Exception {
    // 10 | 1 - kmelia-Space-A_Level-1 | admin
    final List<String> userIds = create50000UsersWithAimedProfile("10");
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    final SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .build());
    assertSortedUserIds(users, Stream.concat(Stream.of(
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU13_1013_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID, USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SPU8_1008_ID_VALID,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID),
        userIds.stream()).toArray(String[]::new)
    );
  }

  @Test
  public void componentIdWithLotOfUsersAccessingInstanceCriteriaAsAdmin() throws Exception {
    // 10 | 1 - kmelia-Space-A_Level-1 | admin
    final List<String> userIds = create50000UsersWithAimedProfile("10");
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertSortedUserIds(users, Stream.concat(Stream.of(
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU13_1013_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID, USER_SPU5_1005_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SPU8_1008_ID_VALID,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU14_2014_ID_VALID), userIds.stream())
        .toArray(String[]::new)
    );
    // on writer right
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withRoleNames(writer.getName())
        .build());
    assertSortedUserIds(users,
        USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU6_1006_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU5_2005_ID_VALID, USER_SQLU9_2009_ID_VALID
    );
  }

  @Test
  public void componentIdWithLotOfUsersAccessingInstanceCriteriaAsWriterOnSpecificRight() throws Exception {
    // 10 | 1 - kmelia-Space-A_Level-1 | admin (component registering)
    // 911 | 1 - kmelia-Space-A_Level-1 | Folder-1-1 (1011) with specific rights | writer (node registering)
    final List<String> userIds = create50000UsersWithAimedProfiles("10", "911");
    assertThat(userIds.size(), is(DEFAULT_MASSIVE_NB_USERS));
    SilverpeasList<UserDetail> users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.admin.getName())
        .build());
    assertThat(users, notNullValue());
    assertThat(users, empty());
    users = admin.searchUsers(newUserSearchCriteriaBuilder()
        .withComponentId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_ID)
        .withNodeId(INSTANCE_SPACE_A_LEVEL_1_KMELIA_FOLDER_11_ID)
        .withRoleNames(SilverpeasRole.writer.getName())
        .build());
    assertSortedUserIds(users, Stream.concat(Stream.of(
        USER_SPU13_1013_ID_VALID, USER_SQLU1_2001_ID_VALID), userIds.stream())
        .toArray(String[]::new)
    );
  }

  @Test
  public void verifyingTechnicalChangeOnAddAllUsersInProfileAlsoUsedBySearchUserIdsByProfile()
      throws AdminException {
    List<String> userIds = admin.searchUserIdsByProfile(asList("320", "32"));
    assertThat(userIds, containsInAnyOrder(
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID,
        USER_SQLU11_2011_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU9_2009_ID_VALID
    ));
    userIds = admin.searchUserIdsByProfile(asList("320", "32", "310", "31"));
    assertThat(userIds, containsInAnyOrder(
        USER_ADM_0_ID_VALID, USER_SPU1_1001_ID_VALID,
        USER_SPU10_1010_ID_VALID, USER_SPU14_1014_ID_VALID,
        USER_SPU2_1002_ID_BLOCKED,
        USER_SPU4_1004_ID_VALID,
        USER_SPU7_1007_ID_DEACTIVATED,
        USER_SQLU1_2001_ID_VALID,
        USER_SQLU10_2010_ID_VALID, USER_SQLU11_2011_ID_VALID, USER_SQLU14_2014_ID_VALID,
        USER_SQLU4_2004_ID_VALID, USER_SQLU9_2009_ID_VALID
    ));
  }

  private void assertSortedUserIds(final List<? extends User> users, final String... expectedIds) {
    assertThat(users, notNullValue());
    final List<String> actual = extractData(users, User::getId);
    assertThat(truncate("Actual user ids: " + actual, 1000), actual, contains(expectedIds));
  }

  /**
   * @return the created ids.
   */
  private List<String> create50000UsersAttachedToGroupMIX1() throws SQLException {
    final int userIdOffset = 100000;
    final int nbUsers = DEFAULT_MASSIVE_NB_USERS;
    final String groupId = GROUP_MIX_1_ID;
    testStatisticRule.log("creating " + nbUsers + " users attached to group " + groupId + "...");
    final long start = System.currentTimeMillis();
    final List<String> userIds = new ArrayList<>(nbUsers);
    final StringBuilder users = new StringBuilder();
    final StringBuilder groupAttachment = new StringBuilder();
    users.append("INSERT INTO st_user (");
    users.append("id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, state, stateSaveDate) VALUES ");
    groupAttachment.append("INSERT INTO st_group_user_rel (groupid, userid) VALUES ");
    IntStream.rangeClosed(1, nbUsers).forEach(i -> {
      if (i != 1) {
        users.append(",");
        groupAttachment.append(",");
      }
      final String userId = String.valueOf(i + userIdOffset);
      userIds.add(userId);
      users.append(format("({0}, 0, ''{0}'', ''du{0}'', ''User{0}'', ''du{0}@silverpeas.org'', ''l{0}'', '''', ''U'', ''VALID'', ''2020-01-01 00:00:00.0'')", userId));
      groupAttachment.append(format("({0}, {1})", groupId, userId));
    });
    executePlainQueries(users.toString(), groupAttachment.toString());
    testStatisticRule
        .log("...ending the creation of " + nbUsers + " users attached to group " + groupId, start,
            System.currentTimeMillis());
    return userIds;
  }


  /**
   * @return the created ids.
   */
  private List<String> create50000UsersWithAimedProfile(final String componentProfileId)
      throws SQLException {
    return create50000UsersWithAimedProfiles(componentProfileId, null);
  }

  /**
   * @return the created ids.
   */
  private List<String> create50000UsersWithAimedProfiles(final String componentProfileId,
      final String nodeProfileId) throws SQLException {
    final int userIdOffset = 100000;
    final int nbUsers = DEFAULT_MASSIVE_NB_USERS;
    testStatisticRule.log(
        "creating " + nbUsers + " users with component userrole id " + componentProfileId +
            " and node userrole id " + nodeProfileId + "...");
    final long start = System.currentTimeMillis();
    final List<String> userIds = new ArrayList<>(nbUsers);
    final StringBuilder users = new StringBuilder();
    final StringBuilder userRoleRel = new StringBuilder();
    users.append("INSERT INTO st_user (");
    users.append("id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, state, stateSaveDate) VALUES ");
    userRoleRel.append("INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES ");
    IntStream.rangeClosed(1, nbUsers).forEach(i -> {
      if (i != 1) {
        users.append(",");
        userRoleRel.append(",");
      }
      final String userId = String.valueOf(i + userIdOffset);
      userIds.add(userId);
      users.append(format("({0}, 0, ''{0}'', ''du{0}'', ''User{0}'', ''du{0}@silverpeas.org'', ''l{0}'', '''', ''U'', ''VALID'', ''2020-01-01 00:00:00.0'')", userId));
      userRoleRel.append(format("({0}, {1})", componentProfileId, userId));
      if (isDefined(nodeProfileId)) {
        userRoleRel.append(format(",({0}, {1})", nodeProfileId, userId));
      }
    });
    executePlainQueries(users.toString(), userRoleRel.toString());
    testStatisticRule.log(
        "...ending the creation of " + nbUsers + " users with userrole id " + componentProfileId +
            " and node userrole id " + nodeProfileId, start,
        System.currentTimeMillis());
    return userIds;
  }
}
