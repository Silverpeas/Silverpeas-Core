/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.util.node.ejb;

import org.junit.BeforeClass;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class MockedNodeDAOTest {

  private static final String INSTANCE_ID = "kmelia60";

  private static JDBCMockObjectFactory factory;

  public MockedNodeDAOTest() {
  }

  @BeforeClass
  public static void prepare() {
    factory = new JDBCMockObjectFactory();
  }

  @AfterClass
  public static void cleanup() {
    factory.restoreDrivers();
  }

  @Test
  public void testDeleteRow() throws Exception {
    JDBCMockObjectFactory jdbcFactory = new JDBCMockObjectFactory();
    JDBCTestModule module = new JDBCTestModule(jdbcFactory);
    MockConnection mockedConnection = jdbcFactory.getMockConnection();
    StatementResultSetHandler statementHandler = mockedConnection.getStatementResultSetHandler();
    statementHandler.prepareGlobalUpdateCount(1);
    NodePK nodePk = new NodePK("4", INSTANCE_ID);
    NodeDAO.deleteRow(mockedConnection, nodePk);
    module.verifySQLStatementExecuted("delete from sb_node_node where nodeId=4"
        + " and instanceId='kmelia60'");
    module.verifyNotCommitted();
    module.verifyAllResultSetsClosed();
    module.verifyAllStatementsClosed();
  }

  @Test
  public void testLoadRowConnectionNodePKStringInt() throws Exception {
    JDBCMockObjectFactory jdbcFactory = new JDBCMockObjectFactory();
    JDBCTestModule module = new JDBCTestModule(jdbcFactory);
    MockConnection mockedConnection = jdbcFactory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = mockedConnection.getPreparedStatementResultSetHandler();
    MockResultSet result = statementHandler.createResultSet();
    result.addRow(new Object[]{new Integer(4), "Sous Theme de Test",
          "Vos publications peuvent se retrouver ici", "2008/05/10", "7",
          "/0/3/", new Integer(3), new Integer(3), "", "Visible", "",
          "default", new Integer(1), null, new Integer(-1)});
    statementHandler.prepareGlobalResultSet(result);
    NodePK resultPk = NodeDAO.selectByNameAndFatherId(mockedConnection, new NodePK("4", INSTANCE_ID),
        "Sous Theme de Test", 3);
    assertNotNull(resultPk);
    NodeDetail detail = resultPk.nodeDetail;
    assertNotNull(detail);
    assertEquals(4, detail.getId());
    assertEquals("Sous Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
    assertEquals("2008/05/10", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(3, detail.getLevel());
    assertEquals("3", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/3/", detail.getPath());
    assertEquals("default", detail.getType());
    module.verifyPreparedStatementParameter("select * from sb_node_node where "
        + "lower(nodename)=? and instanceId=? and nodefatherid=?", 1, "sous theme de test");
    module.verifyPreparedStatementParameter("select * from sb_node_node where "
        + "lower(nodename)=? and instanceId=? and nodefatherid=?", 2, "kmelia60");
    module.verifyPreparedStatementParameter("select * from sb_node_node where "
        + "lower(nodename)=? and instanceId=? and nodefatherid=?", 3, Integer.valueOf(3));
    module.verifyNotCommitted();
    module.verifyAllResultSetsClosed();
    module.verifyAllStatementsClosed();
  }

  @Test
  public void testStoreRow() throws Exception {
    JDBCMockObjectFactory jdbcFactory = new JDBCMockObjectFactory();
    JDBCTestModule module = new JDBCTestModule(jdbcFactory);
    MockConnection mockedConnection = jdbcFactory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = mockedConnection.getPreparedStatementResultSetHandler();
    statementHandler.prepareGlobalUpdateCount(1);
    NodeDetail detail = new NodeDetail();
    detail.setNodePK(new NodePK("4", INSTANCE_ID));
    assertEquals(4, detail.getId());
    detail.setName("Sous Theme de Test");
    detail.setDescription("Vos publications peuvent se retrouver ici");
    detail.setCreationDate("2008/05/10");
    detail.setCreatorId("7");
    detail.setStatus("Visible");
    detail.setLevel(3);
    detail.setFatherPK(new NodePK("3", INSTANCE_ID));
    detail.setModelId("");
    detail.setRightsDependsOnMe();
    detail.setOrder(1);
    detail.setPath("/0/3/");
    detail.setType("default");
    NodeDAO.storeRow(mockedConnection, detail);
    module.verifyPreparedStatementClosed("update sb_node_node set nodeName =  "
        + "? , nodeDescription = ? , nodePath = ? ,  nodeLevelNumber = ? , "
        + "nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, "
        + "lang = ?, rightsDependsOn = ?  where nodeId = ? and instanceId = ?");
    module.verifyPreparedStatementParameter("update sb_node_node", 1,
        "Sous Theme de Test");
    module.verifyPreparedStatementParameter("update sb_node_node", 2,
        "Vos publications peuvent se retrouver ici");
    module.verifyPreparedStatementParameter("update sb_node_node", 3, "/0/3/");
    module.verifyPreparedStatementParameter("update sb_node_node", 4,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 5,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 6, "");
    module.verifyPreparedStatementParameter("update sb_node_node", 7,
        "Visible");
    module.verifyPreparedStatementParameter("update sb_node_node", 8,
        new Integer(1));
    module.verifyPreparedStatementParameter("update sb_node_node", 9, null);
    module.verifyPreparedStatementParameter("update sb_node_node", 10,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 11,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 12,
        INSTANCE_ID);
    module.verifyNotCommitted();
    module.verifyAllResultSetsClosed();
    module.verifyAllStatementsClosed();
  }

  @Test
  public void testMoveNode() throws Exception {
    JDBCMockObjectFactory jdbcFactory = new JDBCMockObjectFactory();
    JDBCTestModule module = new JDBCTestModule(jdbcFactory);
    MockConnection mockedConnection = jdbcFactory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = mockedConnection.getPreparedStatementResultSetHandler();
    statementHandler.prepareGlobalUpdateCount(1);
    NodeDetail detail = new NodeDetail();
    detail.setNodePK(new NodePK("4", INSTANCE_ID));
    assertEquals(4, detail.getId());
    detail.setName("Sous Theme de Test");
    detail.setDescription("Vos publications peuvent se retrouver ici");
    detail.setCreationDate("2008/05/10");
    detail.setCreatorId("7");
    detail.setStatus("Visible");
    detail.setLevel(3);
    detail.setFatherPK(new NodePK("3", INSTANCE_ID));
    detail.setModelId("");
    detail.setRightsDependsOnMe();
    detail.setOrder(1);
    detail.setPath("/0/3/");
    detail.setType("default");
    NodeDAO.storeRow(mockedConnection, detail);
    module.verifyPreparedStatementClosed("update sb_node_node set nodeName =  "
        + "? , nodeDescription = ? , nodePath = ? ,  nodeLevelNumber = ? , "
        + "nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, "
        + "lang = ?, rightsDependsOn = ?  where nodeId = ? and instanceId = ?");
    module.verifyPreparedStatementParameter("update sb_node_node", 1,
        "Sous Theme de Test");
    module.verifyPreparedStatementParameter("update sb_node_node", 2,
        "Vos publications peuvent se retrouver ici");
    module.verifyPreparedStatementParameter("update sb_node_node", 3, "/0/3/");
    module.verifyPreparedStatementParameter("update sb_node_node", 4,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 5,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 6, "");
    module.verifyPreparedStatementParameter("update sb_node_node", 7,
        "Visible");
    module.verifyPreparedStatementParameter("update sb_node_node", 8,
        new Integer(1));
    module.verifyPreparedStatementParameter("update sb_node_node", 9, null);
    module.verifyPreparedStatementParameter("update sb_node_node", 10,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 11,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 12,
        INSTANCE_ID);
    module.verifyNotCommitted();
    module.verifyAllResultSetsClosed();
    module.verifyAllStatementsClosed();
  }

  @Test
  public void testUpdateRightsDependency() throws Exception {
    JDBCMockObjectFactory jdbcFactory = new JDBCMockObjectFactory();
    JDBCTestModule module = new JDBCTestModule(jdbcFactory);
    MockConnection mockedConnection = jdbcFactory.getMockConnection();
    PreparedStatementResultSetHandler statementHandler = mockedConnection.getPreparedStatementResultSetHandler();
    statementHandler.prepareGlobalUpdateCount(1);
    NodeDetail detail = new NodeDetail();
    detail.setNodePK(new NodePK("4", INSTANCE_ID));
    assertEquals(4, detail.getId());
    detail.setName("Sous Theme de Test");
    detail.setDescription("Vos publications peuvent se retrouver ici");
    detail.setCreationDate("2008/05/10");
    detail.setCreatorId("7");
    detail.setStatus("Visible");
    detail.setLevel(3);
    detail.setFatherPK(new NodePK("3", INSTANCE_ID));
    detail.setModelId("");
    detail.setRightsDependsOnMe();
    detail.setOrder(1);
    detail.setPath("/0/3/");
    detail.setType("default");
    NodeDAO.storeRow(mockedConnection, detail);
    module.verifyPreparedStatementClosed("update sb_node_node set nodeName =  "
        + "? , nodeDescription = ? , nodePath = ? ,  nodeLevelNumber = ? , "
        + "nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, "
        + "lang = ?, rightsDependsOn = ?  where nodeId = ? and instanceId = ?");
    module.verifyPreparedStatementParameter("update sb_node_node", 1,
        "Sous Theme de Test");
    module.verifyPreparedStatementParameter("update sb_node_node", 2,
        "Vos publications peuvent se retrouver ici");
    module.verifyPreparedStatementParameter("update sb_node_node", 3, "/0/3/");
    module.verifyPreparedStatementParameter("update sb_node_node", 4,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 5,
        new Integer(3));
    module.verifyPreparedStatementParameter("update sb_node_node", 6, "");
    module.verifyPreparedStatementParameter("update sb_node_node", 7,
        "Visible");
    module.verifyPreparedStatementParameter("update sb_node_node", 8,
        new Integer(1));
    module.verifyPreparedStatementParameter("update sb_node_node", 9, null);
    module.verifyPreparedStatementParameter("update sb_node_node", 10,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 11,
        new Integer(4));
    module.verifyPreparedStatementParameter("update sb_node_node", 12,
        INSTANCE_ID);
    module.verifyNotCommitted();
    module.verifyAllResultSetsClosed();
    module.verifyAllStatementsClosed();
  }
}
