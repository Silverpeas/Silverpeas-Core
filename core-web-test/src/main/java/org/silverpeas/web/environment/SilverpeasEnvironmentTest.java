/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.web.environment;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.UserReference;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This class permits to load easily a Silverpeas Environment:
 * <ul>
 * <li>Spaces</li>
 * <li>Components</li>
 * <li>Users</li>
 * <li>etc.</li>
 * </ul>
 */
@Singleton
public class SilverpeasEnvironmentTest {

  public static final String DEFAULT_DOMAIN = "0";

  @Inject
  private Administration administration;

  private List<TableHandler> tableHandlers = Arrays.asList(
      new SqlScriptTableHandler("create-table-domain-user-group.sql",
          "create-table-space-component.sql", "create-table-profile.sql",
          "create-table-token.sql"));

  /**
   * Gets the Silverpeas environment.
   * @return the instance of the Silverpeas environment.
   */
  public static SilverpeasEnvironmentTest getSilverpeasEnvironmentTest() {
    return ServiceProvider.getService(SilverpeasEnvironmentTest.class);
  }

  @PostConstruct
  void initialize() {
    for (TableHandler tableHandler : tableHandlers) {
      if (!tableHandler.tablesExist()) {
        tableHandler.createTables();
      }
    }
    administration.reloadCache();
  }

  void clear() {
    for (TableHandler tableHandler : tableHandlers) {
      if (tableHandler.tablesExist()) {
        tableHandler.dropTables();
      }
    }
  }

  /**
   * Adds SQL script files to execute before each test.
   * @param sqlScriptFiles the SQL script files.
   */
  public SilverpeasEnvironmentTest addSqlScriptToExecuteBeforeTest(File... sqlScriptFiles) {
    if (sqlScriptFiles.length > 0) {
      String[] sqlScriptFilePaths = new String[sqlScriptFiles.length];
      for (File sqlScriptFile : sqlScriptFiles) {
        sqlScriptFilePaths[sqlScriptFilePaths.length] = sqlScriptFile.getPath();
      }
      tableHandlers.add(new SqlScriptTableHandler(sqlScriptFilePaths));
    }
    return this;
  }

  /**
   * Creates a default user to use in the test case.
   * @return the detail about the user in use in the current test case.
   */
  public UserDetail createDefaultUser() {
    UserDetail user = new UserDetail();
    user.setLogin("toto");
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setDomainId(DEFAULT_DOMAIN);
    user.setState(UserState.VALID);
    user.setAccessLevel(UserAccessLevel.USER);
    addUser(user);
    return user;
  }

  /**
   * Adds a user.
   */
  public SilverpeasEnvironmentTest addUser(UserDetail userDetail) {
    try {
      administration.addUser(userDetail);
    } catch (AdminException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Gets the token of a user that must be passed into request header with parameter
   * "X-Silverpeas-Size".
   * @param userDetail the user details for which the token is needed.
   * @return the token of the given user.
   */
  public String getTokenOf(UserDetail userDetail) {
    try {
      return PersistentResourceToken.getOrCreateToken(UserReference.fromUser(userDetail))
          .getValue();
    } catch (TokenException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the dummy component created into database at start of the test.
   * @return a {@link ComponentInst} instance that represents the dummy component.
   */
  public ComponentInst getDummyPublicComponent() {
    try {
      return administration.getComponentInst("dummyComponent0");
    } catch (AdminException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Updates the database with the given component instance.
   * @param componentInst the {@link ComponentInst} instance
   * @return the manager of test environment itself.
   */
  public SilverpeasEnvironmentTest updateComponent(ComponentInst componentInst) {
    try {
      administration.updateComponentInst(componentInst);
    } catch (AdminException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
}
