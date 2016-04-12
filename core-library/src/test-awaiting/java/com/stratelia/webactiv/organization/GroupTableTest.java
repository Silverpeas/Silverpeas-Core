/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.organization;

import java.util.Arrays;
import java.util.List;

import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class GroupTableTest {

  private JDBCMockObjectFactory factory;
  private JDBCTestModule module;

  public GroupTableTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    factory = new JDBCMockObjectFactory();
    module = new JDBCTestModule(factory);
    module.setExactMatch(true);
    module.setCaseSensitive(true);
  }

  @After
  public void tearDown() {
    factory.restoreDrivers();
  }

  /**
   * Test of fetchGroup method, of class GroupTable.
   */
//  @Test
//  public void testFetchGroup() throws Exception {
//    ResultSet rs = null;
//    GroupTable instance = null;
//    GroupRow expResult = null;
//    GroupRow result = instance.fetchGroup(rs);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
  /**
   * Test of getGroup method, of class GroupTable.
   */
  @Test
  public void testGetGroupById() throws Exception {
    int id = 5;
    MockConnection connexion = factory.getMockConnection();
    PreparedStatementResultSetHandler handler = connexion.getPreparedStatementResultSetHandler();
    MockResultSet resultSet = handler.createResultSet();
    resultSet.addColumn("id");
    resultSet.addColumn("specificId");
    resultSet.addColumn("domainId");
    resultSet.addColumn("superGroupId");
    resultSet.addColumn("name");
    resultSet.addColumn("description");
    resultSet.addColumn("synchroRule");
    resultSet.addRow(Arrays.asList(String.valueOf(id), "3", "0", "8", "Group for test",
        "Fake group", "DS_AccessLevel = *"));
    handler.prepareResultSet("SELECT id, specificId, domainId, superGroupId, name, "
        + "description, synchroRule FROM ST_Group WHERE id = ?", resultSet);
    OrganizationSchema schema = new OrganizationSchema(connexion);
    GroupTable instance = new GroupTable(schema);

    GroupRow result = instance.getGroup(id);

    module.verifyAllStatementsClosed();
    module.verifyAllResultSetsClosed();
    List<?> statements = module.getPreparedStatements("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE id = ?");
    assertThat(statements, is(notNullValue()));
    assertThat(statements.size(), is(1));
    assertThat(result, is(notNullValue()));
    assertThat(result.id, is(id));
    assertThat(result.description, is("Fake group"));
    assertThat(result.name, is("Group for test"));
    assertThat(result.rule, is("DS_AccessLevel = *"));
    assertThat(result.specificId, is("3"));
    assertThat(result.domainId, is(0));
    assertThat(result.superGroupId, is(8));
  }

  /**
   * Test of getGroupBySpecificId method, of class GroupTable.
   */
  @Test
  public void testGetGroupBySpecificId() throws Exception {
    int domainId = 0;
    String specificId = "3";
    MockConnection connexion = factory.getMockConnection();
    PreparedStatementResultSetHandler handler = connexion.getPreparedStatementResultSetHandler();
    MockResultSet resultSet = handler.createResultSet();
    resultSet.addColumn("id");
    resultSet.addColumn("specificId");
    resultSet.addColumn("domainId");
    resultSet.addColumn("superGroupId");
    resultSet.addColumn("name");
    resultSet.addColumn("description");
    resultSet.addColumn("synchroRule");
    resultSet.addRow(Arrays.asList("5", specificId, String.valueOf(domainId), "8", "Group for test",
        "Fake group", "DS_AccessLevel = *"));
    handler.prepareResultSet("SELECT id, specificId, domainId, superGroupId, name, description, "
        + "synchroRule FROM ST_Group WHERE domainId = ? AND specificId = ?", resultSet);
    OrganizationSchema schema = new OrganizationSchema(connexion);
    GroupTable instance = new GroupTable(schema);

    GroupRow result = instance.getGroupBySpecificId(domainId, specificId);

    module.verifyAllStatementsClosed();
    module.verifyAllResultSetsClosed();
    List<?> statements = module.getPreparedStatements("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE domainId = ? AND "
        + "specificId = ?");
    assertThat((Integer) module.getPreparedStatementParameter("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE domainId = ? AND "
        + "specificId = ?", 1), is(domainId));
    assertThat((String) module.getPreparedStatementParameter("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE domainId = ? AND "
        + "specificId = ?", 2), is(specificId));
    assertThat(statements, is(notNullValue()));
    assertThat(statements.size(), is(1));
    assertThat(result, is(notNullValue()));
    assertThat(result.id, is(5));
    assertThat(result.description, is("Fake group"));
    assertThat(result.name, is("Group for test"));
    assertThat(result.rule, is("DS_AccessLevel = *"));
    assertThat(result.specificId, is(specificId));
    assertThat(result.domainId, is(domainId));
    assertThat(result.superGroupId, is(8));
  }

  /**
   * Test of getRootGroup method, of class GroupTable.
   */
  @Test
  public void testGetRootGroup() throws Exception {
    String name = "Root";
    MockConnection connexion = factory.getMockConnection();
    PreparedStatementResultSetHandler handler = connexion.getPreparedStatementResultSetHandler();
    MockResultSet resultSet = handler.createResultSet();
    resultSet.addColumn("id");
    resultSet.addColumn("specificId");
    resultSet.addColumn("domainId");
    resultSet.addColumn("superGroupId");
    resultSet.addColumn("name");
    resultSet.addColumn("description");
    resultSet.addColumn("synchroRule");
    resultSet.addRow(Arrays.asList("5", "3", "0", null, name, "Fake group",
        "DS_AccessLevel = *"));
    handler.prepareResultSet("SELECT id, specificId, domainId, superGroupId, name, description, "
        + "synchroRule FROM ST_Group WHERE superGroupId IS NULL AND name = ?", resultSet);
    OrganizationSchema schema = new OrganizationSchema(connexion);
    GroupTable instance = new GroupTable(schema);

    GroupRow result = instance.getRootGroup(name);

    module.verifyAllStatementsClosed();
    module.verifyAllResultSetsClosed();
    List<?> statements = module.getPreparedStatements("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE superGroupId IS NULL "
        + "AND name = ?");
    assertThat((String) module.getPreparedStatementParameter("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE superGroupId IS NULL "
        + "AND name = ?", 1), is(name));
    assertThat(statements, is(notNullValue()));
    assertThat(statements.size(), is(1));
    assertThat(result, is(notNullValue()));
    assertThat(result.id, is(5));
    assertThat(result.description, is("Fake group"));
    assertThat(result.name, is(name));
    assertThat(result.rule, is("DS_AccessLevel = *"));
    assertThat(result.specificId, is("3"));
    assertThat(result.domainId, is(0));
    assertThat(result.superGroupId, is(-1));
  }

  /**
   * Test of getGroup method, of class GroupTable.
   */
  @Test
  public void testGetGroupByNameAndParentId() throws Exception {
    int superGroupId = 8;
    String name = "Group for test";
    MockConnection connexion = factory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = connexion
        .getPreparedStatementResultSetHandler();
    MockResultSet resultSet = statementHandler.createResultSet();
    resultSet.addColumn("id");
    resultSet.addColumn("specificId");
    resultSet.addColumn("domainId");
    resultSet.addColumn("superGroupId");
    resultSet.addColumn("name");
    resultSet.addColumn("description");
    resultSet.addColumn("synchroRule");
    resultSet.addRow(Arrays.asList("5", "3", "0", "8", "Group for test", "Fake group",
        "DS_AccessLevel = *"));
    statementHandler.prepareResultSet("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE "
        + "superGroupId = ? AND name = ?", resultSet);
    OrganizationSchema schema = new OrganizationSchema(connexion);
    GroupTable instance = new GroupTable(schema);

    GroupRow result = instance.getGroup(superGroupId, name);

    module.verifyAllStatementsClosed();
    module.verifyAllResultSetsClosed();
    List<?> statements = module.getPreparedStatements("SELECT id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule FROM ST_Group WHERE "
        + "superGroupId = ? AND name = ?");
    assertThat(statements, is(notNullValue()));
    assertThat(statements.size(), is(1));
    assertThat(result, is(notNullValue()));
    assertThat(result.id, is(5));
    assertThat(result.description, is("Fake group"));
    assertThat(result.name, is("Group for test"));
    assertThat(result.rule, is("DS_AccessLevel = *"));
    assertThat(result.specificId, is("3"));
    assertThat(result.domainId, is(0));
    assertThat(result.superGroupId, is(8));
  }

  /**
   * Test of getAllGroups method, of class GroupTable.
   */
  @Test
  public void testGetAllGroups() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = connexion
        .getPreparedStatementResultSetHandler();
    MockResultSet resultSet = statementHandler.createResultSet();
    resultSet.addColumn("id");
    resultSet.addColumn("specificId");
    resultSet.addColumn("domainId");
    resultSet.addColumn("superGroupId");
    resultSet.addColumn("name");
    resultSet.addColumn("description");
    resultSet.addColumn("synchroRule");
    resultSet.addRow(Arrays.asList("5", "3", "0", "8", "Group for test", "Fake group",
        "DS_AccessLevel = *"));
    resultSet.addRow(Arrays.asList("1", "4", "0", null, "Root", "Fake group", "DS_AccessLevel = A"));
    statementHandler.prepareResultSet("select id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule from ST_Group", resultSet);
    OrganizationSchema schema = new OrganizationSchema(connexion);
    GroupTable instance = new GroupTable(schema);

    GroupRow[] result = instance.getAllGroups();

    module.verifyAllStatementsClosed();
    module.verifyAllResultSetsClosed();
    List<?> statements = module.getPreparedStatements("select id, specificId, domainId, "
        + "superGroupId, name, description, synchroRule from ST_Group");
    assertThat(statements, is(notNullValue()));
    assertThat(statements.size(), is(1));
    assertThat(result, is(notNullValue()));
    assertThat(result[0].id, is(5));
    assertThat(result[0].description, is("Fake group"));
    assertThat(result[0].name, is("Group for test"));
    assertThat(result[0].rule, is("DS_AccessLevel = *"));
    assertThat(result[0].specificId, is("3"));
    assertThat(result[0].domainId, is(0));
    assertThat(result[0].superGroupId, is(8));

    assertThat(result[1].id, is(1));
    assertThat(result[1].description, is("Fake group"));
    assertThat(result[1].name, is("Root"));
    assertThat(result[1].rule, is("DS_AccessLevel = A"));
    assertThat(result[1].specificId, is("4"));
    assertThat(result[1].domainId, is(0));
    assertThat(result[1].superGroupId, is(-1));
  }

//  /**
//   * Test of getSynchronizedGroups method, of class GroupTable.
//   */
//  @Test
//  public void testGetSynchronizedGroups() throws Exception {
//    System.out.println("getSynchronizedGroups");
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getSynchronizedGroups();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllGroupIds method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllGroupIds() throws Exception {
//    System.out.println("getAllGroupIds");
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getAllGroupIds();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllRootGroups method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllRootGroups() throws Exception {
//    System.out.println("getAllRootGroups");
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getAllRootGroups();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllRootGroupIds method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllRootGroupIds() throws Exception {
//    System.out.println("getAllRootGroupIds");
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getAllRootGroupIds();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectSubGroups method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectSubGroups() throws Exception {
//    System.out.println("getDirectSubGroups");
//    int superGroupId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getDirectSubGroups(superGroupId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectSubGroupIds method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectSubGroupIds() throws Exception {
//    System.out.println("getDirectSubGroupIds");
//    int superGroupId = 0;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getDirectSubGroupIds(superGroupId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllRootGroupsOfDomain method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllRootGroupsOfDomain() throws Exception {
//    System.out.println("getAllRootGroupsOfDomain");
//    int domainId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getAllRootGroupsOfDomain(domainId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllRootGroupIdsOfDomain method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllRootGroupIdsOfDomain() throws Exception {
//    System.out.println("getAllRootGroupIdsOfDomain");
//    int domainId = 0;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getAllRootGroupIdsOfDomain(domainId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllGroupsOfDomain method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllGroupsOfDomain() throws Exception {
//    System.out.println("getAllGroupsOfDomain");
//    int domainId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getAllGroupsOfDomain(domainId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getSuperGroup method, of class GroupTable.
//   */
//  @Test
//  public void testGetSuperGroup() throws Exception {
//    System.out.println("getSuperGroup");
//    int subGroupId = 0;
//    GroupTable instance = null;
//    GroupRow expResult = null;
//    GroupRow result = instance.getSuperGroup(subGroupId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupsOfUser method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupsOfUser() throws Exception {
//    System.out.println("getDirectGroupsOfUser");
//    int userId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getDirectGroupsOfUser(userId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupsInUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupsInUserRole() throws Exception {
//    System.out.println("getDirectGroupsInUserRole");
//    int userRoleId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getDirectGroupsInUserRole(userRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupIdsInUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupIdsInUserRole() throws Exception {
//    System.out.println("getDirectGroupIdsInUserRole");
//    int userRoleId = 0;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getDirectGroupIdsInUserRole(userRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupsInSpaceUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupsInSpaceUserRole() throws Exception {
//    System.out.println("getDirectGroupsInSpaceUserRole");
//    int spaceUserRoleId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getDirectGroupsInSpaceUserRole(spaceUserRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupIdsInSpaceUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupIdsInSpaceUserRole() throws Exception {
//    System.out.println("getDirectGroupIdsInSpaceUserRole");
//    int spaceUserRoleId = 0;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getDirectGroupIdsInSpaceUserRole(spaceUserRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupsInGroupUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupsInGroupUserRole() throws Exception {
//    System.out.println("getDirectGroupsInGroupUserRole");
//    int groupUserRoleId = 0;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getDirectGroupsInGroupUserRole(groupUserRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getGroupOfGroupUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetGroupOfGroupUserRole() throws Exception {
//    System.out.println("getGroupOfGroupUserRole");
//    int groupUserRoleId = 0;
//    GroupTable instance = null;
//    GroupRow expResult = null;
//    GroupRow result = instance.getGroupOfGroupUserRole(groupUserRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getDirectGroupIdsInGroupUserRole method, of class GroupTable.
//   */
//  @Test
//  public void testGetDirectGroupIdsInGroupUserRole() throws Exception {
//    System.out.println("getDirectGroupIdsInGroupUserRole");
//    int groupUserRoleId = 0;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.getDirectGroupIdsInGroupUserRole(groupUserRoleId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getAllMatchingGroups method, of class GroupTable.
//   */
//  @Test
//  public void testGetAllMatchingGroups() throws Exception {
//    System.out.println("getAllMatchingGroups");
//    GroupRow sampleGroup = null;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.getAllMatchingGroups(sampleGroup);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of searchGroupsIds method, of class GroupTable.
//   */
//  @Test
//  public void testSearchGroupsIds() throws Exception {
//    System.out.println("searchGroupsIds");
//    boolean isRootGroup = false;
//    int componentId = 0;
//    int[] aRoleId = null;
//    GroupRow groupModel = null;
//    GroupTable instance = null;
//    String[] expResult = null;
//    String[] result = instance.searchGroupsIds(isRootGroup, componentId, aRoleId, groupModel);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of searchGroups method, of class GroupTable.
//   */
//  @Test
//  public void testSearchGroups() throws Exception {
//    System.out.println("searchGroups");
//    GroupRow groupModel = null;
//    boolean isAnd = false;
//    GroupTable instance = null;
//    GroupRow[] expResult = null;
//    GroupRow[] result = instance.searchGroups(groupModel, isAnd);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of createGroup method, of class GroupTable.
//   */
//  @Test
//  public void testCreateGroup() throws Exception {
//    System.out.println("createGroup");
//    GroupRow group = null;
//    GroupTable instance = null;
//    instance.createGroup(group);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of prepareInsert method, of class GroupTable.
//   */
//  @Test
//  public void testPrepareInsert() throws Exception {
//    System.out.println("prepareInsert");
//    String insertQuery = "";
//    PreparedStatement insert = null;
//    GroupRow row = null;
//    GroupTable instance = null;
//    instance.prepareInsert(insertQuery, insert, row);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of updateGroup method, of class GroupTable.
//   */
//  @Test
//  public void testUpdateGroup() throws Exception {
//    System.out.println("updateGroup");
//    GroupRow group = null;
//    GroupTable instance = null;
//    instance.updateGroup(group);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of prepareUpdate method, of class GroupTable.
//   */
//  @Test
//  public void testPrepareUpdate() throws Exception {
//    System.out.println("prepareUpdate");
//    String updateQuery = "";
//    PreparedStatement update = null;
//    GroupRow row = null;
//    GroupTable instance = null;
//    instance.prepareUpdate(updateQuery, update, row);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of removeGroup method, of class GroupTable.
//   */
//  @Test
//  public void testRemoveGroup() throws Exception {
//    System.out.println("removeGroup");
//    int id = 0;
//    GroupTable instance = null;
//    instance.removeGroup(id);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of addUserInGroup method, of class GroupTable.
//   */
//  @Test
//  public void testAddUserInGroup() throws Exception {
//    System.out.println("addUserInGroup");
//    int userId = 0;
//    int groupId = 0;
//    GroupTable instance = null;
//    instance.addUserInGroup(userId, groupId);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of addUsersInGroup method, of class GroupTable.
//   */
//  @Test
//  public void testAddUsersInGroup() throws Exception {
//    System.out.println("addUsersInGroup");
//    String[] userIds = null;
//    int groupId = 0;
//    boolean checkRelation = false;
//    GroupTable instance = null;
//    instance.addUsersInGroup(userIds, groupId, checkRelation);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of removeUserFromGroup method, of class GroupTable.
//   */
//  @Test
//  public void testRemoveUserFromGroup() throws Exception {
//    System.out.println("removeUserFromGroup");
//    int userId = 0;
//    int groupId = 0;
//    GroupTable instance = null;
//    instance.removeUserFromGroup(userId, groupId);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of removeUsersFromGroup method, of class GroupTable.
//   */
//  @Test
//  public void testRemoveUsersFromGroup() throws Exception {
//    System.out.println("removeUsersFromGroup");
//    String[] userIds = null;
//    int groupId = 0;
//    boolean checkRelation = false;
//    GroupTable instance = null;
//    instance.removeUsersFromGroup(userIds, groupId, checkRelation);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of fetchRow method, of class GroupTable.
//   */
//  @Test
//  public void testFetchRow() throws Exception {
//    System.out.println("fetchRow");
//    ResultSet rs = null;
//    GroupTable instance = null;
//    GroupRow expResult = null;
//    GroupRow result = instance.fetchRow(rs);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
}
