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

package org.silverpeas.core.admin.user.notification.role;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.notification.role.test.Validator;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Base class of the integration tests on the
 * {@link org.silverpeas.core.admin.user.notification.role.UserRoleEvent} event listeners.
 *
 * @author mmoquillon
 */
public abstract class BaseUserRoleEventListenerTest {

  public static final String VALIDATOR_ROLE = "validator";
  public static final String READER_ROLE = "reader";
  public static final List<String> NO_IDS = List.of();
  public static final String INSTANCE_ID = "myComponent1";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create_database.sql")
      .loadInitialDataSetFrom("insert_dataset.sql");

  @Inject
  private ProfileInstManager profileInstManager;

  @Inject
  private Administration admin;

  public static Archive<?> createTestArchiveFor(Class<? extends BaseUserRoleEventListenerTest> test) {
    return WarBuilder4LibCore.onWarForTestClass(test)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addPublicationTemplateFeatures()
        .addAsResource("org/silverpeas/core/admin/user/notification/role")
        .testFocusedOn(warBuilder ->
            warBuilder.addPackages(true, test.getPackageName()))
        .build();
  }

  @Before
  public void setUpCurrentRequester() {
    SessionCacheAccessor sessionCacheAccessor =
        CacheAccessorProvider.getSessionCacheAccessor();
    sessionCacheAccessor.newSessionCache(User.getById("1"));
  }

  @Before
  public void reloadAdminCache() {
    admin.reloadCache();
  }

  public ProfileInst getProfileInst(String roleName) {
    String id;
    switch (roleName) {
      case "admin":
        id = "1";
        break;
      case VALIDATOR_ROLE:
        id = "2";
        break;
      default:
        id = "3";
        break;
    }
    return getProfileInstById(id);
  }

  public ProfileInst getProfileInstById(String id) {
    return Transaction.performInOne(() -> {
      try {
        return profileInstManager.getProfileInst(id, false);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  public void updateProfileInst(ProfileInst profileInst) {
    Transaction.performInOne(() -> {
      try {
        profileInstManager.updateProfileInst(profileInst);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
      return null;
    });
  }

  private String createProfileInst(ProfileInst profileInst) {
    return Transaction.performInOne(() -> {
      try {
        return profileInstManager.createProfileInst(profileInst,
            profileInst.getComponentFatherId());
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  public void setUpProfileInstWith(String profileName, List<String> users, List<String> groups) {
    ProfileInst initialProfile = getProfileInst(profileName);
    initialProfile.getAllUsers().addAll(users);
    initialProfile.getAllGroups().addAll(groups);
    updateProfileInst(initialProfile);
  }

  public String setUpInheritedProfileInstWith(String profileName, List<String> users,
      List<String> groups) {
    ProfileInst initialProfile = new ProfileInst();
    initialProfile.setComponentFatherId(1);
    initialProfile.setInherited(true);
    initialProfile.setName(profileName);
    initialProfile.setLabel(profileName);
    initialProfile.getAllUsers().addAll(users);
    initialProfile.getAllGroups().addAll(groups);
    return createProfileInst(initialProfile);
  }

  public static void assertValidatorsEquality(List<Validator> actualValidators,
      List<Validator> expectedValidators) {
      assertEquals(expectedValidators.size(), actualValidators.size());
      assertTrue(actualValidators.containsAll(expectedValidators));
  }

  public static void assertValidatorIsRemoved(String validatorId,
      List<Validator> actualValidators) {
    assertTrue(actualValidators.stream()
        .noneMatch(v -> v.getUserId().equals(validatorId)));
  }
}
  