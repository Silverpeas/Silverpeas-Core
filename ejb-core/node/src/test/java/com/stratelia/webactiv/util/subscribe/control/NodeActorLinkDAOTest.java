/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.stratelia.webactiv.util.subscribe.control;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import java.io.IOException;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.sql.Connection;
import java.util.Collection;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class NodeActorLinkDAOTest extends AbstractJndiCase {

  private static final String INSTANCE_ID = "kmelia60";

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase(
            "com/stratelia/webactiv/util/subscribe/control/node-actors-test-dataset.xml",
            "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  public NodeActorLinkDAOTest() {
  }

  /**
   * Test of add method, of class NodeActorLinkDAO.
   */
  @Test
  public void testAdd() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "100";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      nodePk.setSpace("100");
      NodeActorLinkDAO.add(connection, userId, nodePk);
      Collection<NodePK> result = NodeActorLinkDAO.getNodePKsByActorComponent(connection, userId,
              INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, contains(nodePk));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of remove method, of class NodeActorLinkDAO.
   */
  @Test
  public void testRemove() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "2";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      Collection<NodePK> result = NodeActorLinkDAO.getNodePKsByActorComponent(connection, userId,
              INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, contains(nodePk));
      NodeActorLinkDAO.remove(connection, userId, nodePk);
      result = NodeActorLinkDAO.getNodePKsByActorComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(0));
      assertThat(result, not(contains(nodePk)));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByUser method, of class NodeActorLinkDAO.
   */
  @Test
  public void testRemoveByUser() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodePK> result = NodeActorLinkDAO.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(3));
      NodeActorLinkDAO.removeByUser(connection, userId);
      result = NodeActorLinkDAO.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(0));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByNodePath method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testRemoveByNodePath() throws Exception {
    System.out.println("removeByNodePath");
    Connection con = null;
    String tableName = "";
    NodePK node = null;
    String path = "";
    NodeActorLinkDAO.removeByNodePath(con, node, path);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getNodePKsByActor method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testGetNodePKsByActor() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodePK> result = NodeActorLinkDAO.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(3));
      assertThat(result, contains(new NodePK("0", "100", INSTANCE_ID)));
      assertThat(result, contains(new NodePK("10", "100", INSTANCE_ID)));
      assertThat(result, contains(new NodePK("20", "100", INSTANCE_ID)));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getNodePKsByActorComponent method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testGetNodePKsByActorComponent() throws Exception {
    System.out.println("getNodePKsByActorComponent");
    Connection con = null;
    String tableName = "";
    String userId = "";
    String componentName = "";
    Collection expResult = null;
    Collection result = NodeActorLinkDAO.getNodePKsByActorComponent(con, userId,
            componentName);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getActorPKsByNodePK method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testGetActorPKsByNodePK() throws Exception {
    System.out.println("getActorPKsByNodePK");
    Connection con = null;
    NodePK node = null;
    Collection expResult = null;
    Collection result = NodeActorLinkDAO.getActorPKsByNodePK(con, node);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getActorPKsByNodePKs method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testGetActorPKsByNodePKs() throws Exception {
    System.out.println("getActorPKsByNodePKs");
    Connection con = null;
    String tableName = "";
    Collection nodePKs = null;
    Collection expResult = null;
    Collection result = NodeActorLinkDAO.getActorPKsByNodePKs(con, nodePKs);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
