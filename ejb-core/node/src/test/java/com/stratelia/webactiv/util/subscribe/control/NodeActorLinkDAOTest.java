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

import java.util.List;
import com.google.common.collect.Lists;
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
  private static NodeActorLinkDAO nodeActorLinkDao = new NodeActorLinkDAO();
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
   * @throws Exception 
   */
  @Test
  public void testAdd() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "100";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      nodePk.setSpace("100");
      nodeActorLinkDao.add(connection, userId, nodePk);
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId,
              INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, hasItem(nodePk));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of remove method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testRemove() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "2";
      NodePK nodePk = new NodePK("0", INSTANCE_ID);
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId,
              INSTANCE_ID);
      assertThat(result, hasSize(1));
      assertThat(result, contains(nodePk));
      nodeActorLinkDao.remove(connection, userId, nodePk);
      result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(0));
      assertThat(result, not(hasItem(nodePk)));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByUser method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testRemoveByUser() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(4));
      nodeActorLinkDao.removeByUser(connection, userId);
      result = nodeActorLinkDao.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(0));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of removeByNodePath method, of class NodeActorLinkDAO.
   */
  @Test
  public void testRemoveByNodePath() throws Exception {    
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String path = "/0/";
      String userId = "1";
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(3));
      result = nodeActorLinkDao.getNodePKsByActorComponent(connection, "11", INSTANCE_ID);
      assertThat(result, hasSize(1));
      nodeActorLinkDao.removeByNodePath(connection, INSTANCE_ID, path);
      result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId, INSTANCE_ID);
      assertThat(result, hasSize(2));
      result = nodeActorLinkDao.getNodePKsByActorComponent(connection, "11", INSTANCE_ID);
      assertThat(result, hasSize(0));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
   
  }

  /**
   * Test of getNodePKsByActor method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testGetNodePKsByActor() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActor(connection, userId);
      assertThat(result, hasSize(4));
      assertThat(result, hasItem(new NodePK("0", null, INSTANCE_ID)));
      assertThat(result, hasItem(new NodePK("10", null, INSTANCE_ID)));
      assertThat(result, hasItem(new NodePK("20", null, INSTANCE_ID)));
      assertThat(result, hasItem(new NodePK("0", null, "kmelia20")));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getNodePKsByActorComponent method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testGetNodePKsByActorComponent() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      String userId = "1";
      Collection<NodePK> result = nodeActorLinkDao.getNodePKsByActorComponent(connection, userId,
              INSTANCE_ID);
      assertThat(result, hasSize(3));
      assertThat(result, hasItem(new NodePK("0", null, INSTANCE_ID)));
      assertThat(result, hasItem(new NodePK("10", null, INSTANCE_ID)));
      assertThat(result, hasItem(new NodePK("20", null, INSTANCE_ID)));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getActorPKsByNodePK method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testGetActorPKsByNodePK() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      NodePK node = new NodePK("0", "100", INSTANCE_ID);
      Collection<String> result = nodeActorLinkDao.getActorPKsByNodePK(connection, node);
      assertThat(result, hasSize(5));
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
      assertThat(result, hasItem("3"));
      assertThat(result, hasItem("4"));
      assertThat(result, hasItem("5"));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }

  /**
   * Test of getActorPKsByNodePKs method, of class NodeActorLinkDAO.
   * @throws Exception 
   */
  @Test
  public void testGetActorPKsByNodePKs() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getConnection();
    try {
      Connection connection = dataSetConnection.getConnection();
      List<NodePK> nodePks = Lists.asList(new NodePK("0", "100", INSTANCE_ID),
              new NodePK[]{new NodePK("10", "100", INSTANCE_ID)});
      Collection<String> result = nodeActorLinkDao.getActorPKsByNodePKs(connection, nodePks);
      assertThat(result, hasSize(5));
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
      assertThat(result, hasItem("3"));
      assertThat(result, hasItem("4"));
      assertThat(result, hasItem("5"));
    } finally {
      baseTest.getDatabaseTester().closeConnection(dataSetConnection);
    }
  }
}
