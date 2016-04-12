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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static org.silverpeas.core.admin.service.RightAssignationContext.MODE.COPY;
import static org.silverpeas.core.admin.service.RightAssignationContext.MODE.REPLACE;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AssignRightTest  {

  private static final String SCRIPTS_PATH =
      "/" + AssignRightTest.class.getPackage().getName().replaceAll("\\.", "/");

  private final static boolean BUT_NOT_RIGHT_OBJECTS = false;
  private final static boolean WITH_RIGHT_OBJECTS = true;

  private final static String G1_D0 = "1";
  private final static String GROUP_THAT_CONTAINS_DIRECTLY_USER_A = "3";
  private final static String G2_D1 = "10";
  private final static String GROUP_NO_RIGHTS = "26";
  private final static String USER_A = "1";
  private final static String USER_B = "2";
  private final static String USER_NO_RIGHTS = "38";

  private final static String WRITER_PROFILE_ID_OF_KMELIA = "11";
  private final static String WRITER_PROFILE_ID_OF_KMELIA_SUB_NODE = "911";

  private final static String AUTHOR = null;

  @Inject
  private Administration administrationService;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(SCRIPTS_PATH + "/create-assign-rights-database.sql")
          .loadInitialDataSetFrom(SCRIPTS_PATH + "/insert-assign-rights-dataset.sql");

  @Before
  public void setUp() throws Exception {
    try {
      verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
    } catch (Throwable t) {
      Logger.getAnonymousLogger().severe("FAILED ON INITIAL DATA VERIFICATION");
      throw new RuntimeException(t);
    }
  }

  @After
  public void tearDown() throws Exception {
    administrationService.reloadCache();
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AssignRightTest.class).addCommonBasicUtilities()
        .addSilverpeasExceptionBases().testFocusedOn(
            (warBuilder) -> ((WarBuilder4LibCore) warBuilder).addAdministrationFeatures()).build();
  }

  /*
  FUNCTIONAL CASES - VERY HEADACHE CASE:
  A user is in a sub group of a group and the group has right on component that handles node
  structure (kmelia for example).
  This user has direct right on a sub node of the component and a direct right on the component.
  When the direct right on the component is removed for the user, as it is part of the sub group
  of the group that has access to the component, the direct right on the node of the component
  must not be deleted.
   */

  @Test
  public void testDirectKmeliaSubNodeRightMustNotBeDeletedIfUserIsInGroupThatHasKmeliaAccess()
      throws AdminException {
    // Adding writer right on sub node of kmelia to user A
    ProfileInst profileInst =
        administrationService.getProfileInst(WRITER_PROFILE_ID_OF_KMELIA_SUB_NODE);
    profileInst.addUser(USER_A);
    administrationService.updateProfileInst(profileInst);

    // Verifying that the right has been added successfully
    verifyCurrentDirectRights("test-assign-rights-expected-userAWithKmeliaSubNodeRight.txt");

    // Removing the writer right on kmelia from user A
    profileInst = administrationService.getProfileInst(WRITER_PROFILE_ID_OF_KMELIA);
    profileInst.removeUser(USER_A);
    administrationService.updateProfileInst(profileInst);

    // Verifying that the writer right on component has been removed,
    // but not the one on the sub node as the user is in a group that has yet right on kmelia
    // component
    verifyCurrentDirectRights(
        "test-assign-rights-expected-userAWithKmeliaSubNodeRight-directRightRemovedFromKmelia.txt");
  }

  @Test
  public void testDirectKmeliaSubNodeRightMustBeDeletedIfUserHasNoMoreAccessOnKmelia()
      throws AdminException {
    // Adding writer right on sub node of kmelia to user A
    ProfileInst profileInst =
        administrationService.getProfileInst(WRITER_PROFILE_ID_OF_KMELIA_SUB_NODE);
    profileInst.addUser(USER_A);
    administrationService.updateProfileInst(profileInst);

    // Removing user A from the group that has access to kmelia
    administrationService.removeUserFromGroup(USER_A, GROUP_THAT_CONTAINS_DIRECTLY_USER_A);

    // Verifying that the right has been added successfully
    verifyCurrentDirectRights("test-assign-rights-expected-userAWithKmeliaSubNodeRight.txt");

    // Removing the writer right on kmelia from user A
    profileInst = administrationService.getProfileInst(WRITER_PROFILE_ID_OF_KMELIA);
    profileInst.removeUser(USER_A);
    administrationService.updateProfileInst(profileInst);

    // Verifying that the writer right on component has been removed,
    // and also the one on the sub node as the user is no more in a group that has yet right on
    // kmelia component
    verifyCurrentDirectRights("test-assign-rights-expected-userAWithKmeliaSubNodeRight" +
        "-removedFromGoupG1_2AndDirectRightRemovedFromKmelia.txt");
  }

  /*
  FUNCTIONAL CASES - GROUP to USER
   */

  @Test
  public void testAssignFromGroup1ToUserAByCopyModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(COPY, G1_D0, USER_A, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToUser_A-CopyMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup1ToUserAByCopyModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(COPY, G1_D0, USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToUser_A-CopyMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup1ToUserBByReplaceModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(REPLACE, G1_D0, USER_B, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToUser_B-ReplaceMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup1ToUserBByReplaceModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(REPLACE, G1_D0, USER_B, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToUser_B-ReplaceMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromGroupNoRightsToUserAByCopyModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(COPY, GROUP_NO_RIGHTS, USER_A, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
  }

  @Test
  public void testAssignFromGroupNoRightsToUserBByReplaceModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromGroupToUser(REPLACE, GROUP_NO_RIGHTS, USER_B, BUT_NOT_RIGHT_OBJECTS,
            AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupNoRightsToUser_B-ReplaceMode-ButNotObjectRights.txt");
  }

  /*
  FUNCTIONAL CASES - GROUP to GROUP
   */

  @Test
  public void testAssignFromGroup1ToGroup2AByCopyModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(COPY, G1_D0, G2_D1, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToGroupG2-CopyMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup1ToGroup2AByCopyModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(COPY, G1_D0, G2_D1, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToGroupG2-CopyMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup2ToGroupAByReplaceModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(REPLACE, G2_D1, G1_D0, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG2ToGroupG1-ReplaceMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromGroup1ToGroup2AByReplaceModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(REPLACE, G1_D0, G2_D1, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupG1ToGroupG2-ReplaceMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromGroupNoRightsToGroupG1ByCopyModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(COPY, GROUP_NO_RIGHTS, G1_D0, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
  }

  @Test
  public void testAssignFromGroupNoRightsToGroupG1ByReplaceModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(REPLACE, GROUP_NO_RIGHTS, G1_D0, BUT_NOT_RIGHT_OBJECTS,
            AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-GroupNoRightsToGroupG1-ReplaceMode-ButNotObjectRights.txt");
  }

  /*
  FUNCTIONAL CASES - USER to GROUP
   */

  @Test
  public void testAssignFromUserBToGroupG2ByCopyModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(COPY, USER_B, G2_D1, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToGroupG2-CopyMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromUserBToGroupG2ByCopyModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(COPY, USER_B, G2_D1, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToGroupG2-CopyMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromUserBToGroupG1ByReplaceModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(REPLACE, USER_B, G1_D0, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToGroupG1-ReplaceMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromUserBToGroupG1ByReplaceModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(REPLACE, USER_B, G1_D0, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToGroupG1-ReplaceMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromUserNoRightsToGroupG2ByCopyModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(COPY, USER_NO_RIGHTS, G2_D1, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
  }

  @Test
  public void testAssignFromUserNoRightsToGroupG1ByReplaceModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromUserToGroup(REPLACE, USER_NO_RIGHTS, G1_D0, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserNoRightsToGroupG1-ReplaceMode-ButNotObjectRights.txt");
  }

  /*
  FUNCTIONAL CASES - USER to USER
   */

  @Test
  public void testAssignFromUserBToUserAByCopyModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToUser(COPY, USER_B, USER_A, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToUserA-CopyMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromUserBToUserAByCopyModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToUser(COPY, USER_B, USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToUserA-CopyMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromUserAToUserBByReplaceModeButNotObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToUser(REPLACE, USER_A, USER_B, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserAToUserB-ReplaceMode-ButNotObjectRights.txt");
  }

  @Test
  public void testAssignFromUserBToUserAByReplaceModeWithObjectRights() throws AdminException {
    administrationService
        .assignRightsFromUserToUser(REPLACE, USER_B, USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserBToUserA-ReplaceMode-WithObjectRights.txt");
  }

  @Test
  public void testAssignFromUserNoRightsToUserBByCopyModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromUserToUser(COPY, USER_NO_RIGHTS, USER_B, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
  }

  @Test
  public void testAssignFromUserNoRightsToUserBByReplaceModeButNotObjectRights()
      throws AdminException {
    administrationService
        .assignRightsFromUserToUser(REPLACE, USER_NO_RIGHTS, USER_B, BUT_NOT_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights(
        "test-assign-rights-expected-UserNoRightsToUserB-ReplaceMode-ButNotObjectRights.txt");
  }

  /*
  LIMIT CASES
   */

  @Test
  public void testSameSourceAndTarget() throws AdminException {
    administrationService
        .assignRightsFromGroupToGroup(COPY, G1_D0, G1_D0, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
    administrationService
        .assignRightsFromUserToUser(COPY, USER_A, USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    verifyCurrentDirectRights("test-assign-rights-expected-initial.txt");
  }

  @Test
  public void testAssignFromNotDefinedNullSource() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, null, USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedEmptySource() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, "", USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedSpacesSource() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, "   ", USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedNullStringSource() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, "null", USER_A, WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedNullTarget() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, USER_A, null, WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedEmptyTarget() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, USER_A, "", WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedSpacesTarget() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, USER_A, "   ", WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  @Test
  public void testAssignFromNotDefinedNullStringTarget() throws AdminException {
    try {
      administrationService
          .assignRightsFromGroupToUser(COPY, USER_A, "null", WITH_RIGHT_OBJECTS, AUTHOR);
    } catch (AdminException e) {
      if (e.getNested() instanceof IllegalArgumentException) {
        return;
      }
    }
    fail("IllegalArgumentException should be thrown");
  }

  /**
   * Centralization of verification.<br/>
   * <p>
   * The mechanism is the following:<br/>
   * <p/>
   * the given parameters represents the name of the file that contains the expected result that
   * must return the method {@link #getCurrentDirectRights()}.<br/>
   * Normally, each lines starts with a number as the first data is the domain id. So, in order to
   * give the possibility to annotate the file (for comprehension for example), each line of the
   * file that does not start with a number is ignored.<br/>
   * If the file content is equal to the result of the query execution, the test is successfully
   * verified.<br/>
   * If not, the different lines between the file content and the query result are logged to the
   * console.<br/>
   * The lines of current query result and those of the expected ones (from the file) are sorted by
   * alphabetic mode.
   * </p>
   * @param fileNameOfExpectedResult
   */
  @SuppressWarnings("unchecked")
  private void verifyCurrentDirectRights(String fileNameOfExpectedResult) {
    StringReader current = new StringReader(getCurrentDirectRights());
    StringReader expected = new StringReader(getFileContent(fileNameOfExpectedResult));
    try {
      List<String> currentLines = IOUtils.readLines(current);
      List<String> expectedLines = IOUtils.readLines(expected);
      Iterator<String> expectedLinesIt = expectedLines.iterator();
      while (expectedLinesIt.hasNext()) {
        String currentExpectedLine = expectedLinesIt.next();
        byte[] firstChar = new byte[]{currentExpectedLine.getBytes()[0]};
        if (!StringUtil.isInteger(new String(firstChar))) {
          // The line does not start with a number, it must be ignored in verification process.
          expectedLinesIt.remove();
        }
      }
      List<String> leavingCurrentLines = new ArrayList<>(currentLines);
      leavingCurrentLines.removeAll(expectedLines);
      List<String> leavingExpectedLines = new ArrayList<>(expectedLines);
      leavingExpectedLines.removeAll(currentLines);
      boolean areContentEquals = leavingCurrentLines.isEmpty() && leavingExpectedLines.isEmpty();
      String message = "";
      if (!leavingCurrentLines.isEmpty()) {
        Collections.sort(leavingCurrentLines);
        message += "Current lines that are not in expected ones:\n" +
            StringUtil.join(leavingCurrentLines, "\n");
      }
      if (!leavingExpectedLines.isEmpty()) {
        if (StringUtil.isDefined(message)) {
          message += "\n";
        }
        Collections.sort(leavingExpectedLines);
        message += "Expected lines that are not in current ones:\n" +
            StringUtil.join(leavingExpectedLines, "\n");
      }
      if (!areContentEquals) {
        fail("\n" + message);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method execute a SQL Query on the database (with the connection of the test) to extract
   * the list of all direct rights (inherited or not) assigned to users and groups.<br/>
   * The executed query returns 3 columns and the two firsts columns has always the same number of
   * characters (spaces are added after data to obtain the total number of characters that must
   * have
   * the column). For example, the first column always returns 40 characters. <br/>
   * The composition of the columns is the following (separated by tabulation character):
   * <ul>
   * <li>Column 1: the domain identifier of the group or the user</li>
   * <li>
   * Column 2: the following data separated by " - "
   * <ul>
   * <li>(G) if the line aims a group, (U) if the line aims a user</li>
   * <li>the name of the group or of the user</li>
   * </ul>
   * </li>
   * <li>
   * Column 3: the following data separated by " - " expected for the two first data that are
   * separated by a space character
   * <ul>
   * <li>* in order to indicate that the right is not an inherited one, ^ for inherited one</li>
   * <li>
   * the type of resource aimed by the right:
   * <ul>
   * <li>SPACE: the right concerns a space</li>
   * <li>COMPONENT: the right concerns a component</li>
   * <li>COMPONENT-OBJECT: the right concerns an object managed by a component</li>
   * </ul>
   * </li>
   * <li>[name of the aimed role]@[name of the application or space according to the previous data
   * type][#[the name of the aime object if the previous data type is COMPONENT-OBJECT, in these
   * tests an OBJECT is always a node]]</li>
   * </ul>
   * </li>
   * </ul>
   * @return a string that represents the result of the query execution.
   */
  private String getCurrentDirectRights() {
    try {
      final StringBuilder result = new StringBuilder();
      JdbcSqlQuery.create(getFileContent("select-verifying-direct-rights.sql")).execute(row -> {
        if (result.length() > 0) {
          result.append("\n");
        }
        for (int i = 0; i < row.getMetaData().getColumnCount(); i++) {
          if (i > 0) {
            result.append("\t");
          }
          result.append(row.getString(i + 1));
        }
        return null;
      });
      return result.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileContent(String fileName) {
    try (InputStream fileStream = getClass().getResourceAsStream(fileName)) {
      return StringUtil.join(IOUtils.readLines(fileStream), '\n');
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}