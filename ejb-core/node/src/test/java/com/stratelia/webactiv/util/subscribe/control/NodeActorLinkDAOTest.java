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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class NodeActorLinkDAOTest extends AbstractJndiCase {

  private static final String INSTANCE_ID = "kmelia60";
  
  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/stratelia/webactiv/util/subscribe/control/node-actors-test-dataset.xml",
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
  //@Test
  public void testAdd() throws Exception {
    System.out.println("add");
    Connection con = null;
    String userId = "";
    NodePK node = null;
    NodeActorLinkDAO.add(con, userId, node);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of remove method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testRemove() throws Exception {
    System.out.println("remove");
    Connection con = null;
    String userId = "";
    NodePK node = null;
    NodeActorLinkDAO.remove(con, userId, node);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of removeByUser method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testRemoveByUser() throws Exception {
    System.out.println("removeByUser");
    Connection con = null;
    String tableName = "";
    String userId = "";
    NodeActorLinkDAO.removeByUser(con, tableName, userId);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
    NodeActorLinkDAO.removeByNodePath(con, tableName, node, path);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getNodePKsByActor method, of class NodeActorLinkDAO.
   */
  //@Test
  public void testGetNodePKsByActor() throws Exception {
    System.out.println("getNodePKsByActor");
    Connection con = null;
    String userId = "";
    Collection expResult = null;
    Collection result = NodeActorLinkDAO.getNodePKsByActor(con, userId);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
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
    Collection result = NodeActorLinkDAO.getNodePKsByActorComponent(con, tableName, userId,
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
    Collection result = NodeActorLinkDAO.getActorPKsByNodePKs(con, tableName, nodePKs);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
