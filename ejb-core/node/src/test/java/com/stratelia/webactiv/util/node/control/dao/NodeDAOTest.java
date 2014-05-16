/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.util.node.control.dao;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import org.dbunit.database.IDatabaseConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class NodeDAOTest extends AbstractJndiCase {

  private static final String INSTANCE_ID = "kmelia60";

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/stratelia/webactiv/util/node/control/dao/nodes-test-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }


  @AfterClass
  public static void generalTearDown() throws IOException, NamingException, Exception {
    baseTest.shudown();
  }

  @Test
  public void testGetTree() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    List<NodeDetail> tree = NodeDAO.getTree(connection, nodePK);
    assertNotNull(tree);
    assertEquals(5, tree.size());
    nodePK = new NodePK("3", INSTANCE_ID);
    tree = NodeDAO.getTree(connection, nodePK);
    assertNotNull(tree);
    assertEquals(5, tree.size());
    NodeDetail detail = tree.get(0);
    assertNotNull(detail);
    assertEquals(NodePK.ROOT_NODE_ID, String.valueOf(detail.getId()));
    assertEquals("Accueil", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("La Racine", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(1, detail.getLevel());
    assertEquals("-1", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(0, detail.getOrder());
    assertEquals("/", detail.getPath());
    assertNull(detail.getType());
    detail = tree.get(2);
    assertNotNull(detail);
    assertEquals(NodePK.BIN_NODE_ID, String.valueOf(detail.getId()));
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = tree.get(1);
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = tree.get(3);
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = tree.get(4);
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testIsSameNameSameLevelOnCreation() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dbConnection.getConnection();
    NodePK pk = new NodePK(null, INSTANCE_ID);
    NodeDetail detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Corbeille");
    detail.setNodePK(pk);
    assertTrue(NodeDAO.isSameNameSameLevelOnCreation(connection, detail));
    detail = new NodeDetail();
    detail.setLevel(3);
    detail.setName("Corbeille");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Poubelle");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Corbeille");
    pk = new NodePK(null, "kmelia65");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(connection, detail));
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  @Test
  public void testIsSameNameSameLevelOnUpdate() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK pk = new NodePK("1", INSTANCE_ID);
    NodeDetail detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Corbeille");
    detail.setNodePK(pk);
    assertTrue(NodeDAO.isSameNameSameLevelOnCreation(connection, detail));
    detail = new NodeDetail();
    detail.setLevel(3);
    detail.setName("Corbeille");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Poubelle");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Corbeille");
    pk = new NodePK("2", "kmelia65");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    detail = new NodeDetail();
    detail.setLevel(2);
    detail.setName("Corbeille");
    pk = new NodePK("5", "kmelia65");
    detail.setNodePK(pk);
    assertFalse(NodeDAO.isSameNameSameLevelOnCreation(
        connection, detail));
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetChildrenPKs() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    Collection<NodePK> children = NodeDAO.getChildrenPKs(connection, nodePK);
    assertNotNull(children);
    assertEquals(3, children.size());
    nodePK = new NodePK("3", INSTANCE_ID);
    children = NodeDAO.getChildrenPKs(connection, nodePK);
    assertNotNull(children);
    assertEquals(1, children.size());
    NodePK childPk = (NodePK) children.iterator().next();
    assertNotNull(childPk);
    assertEquals("4", childPk.getId());
    assertEquals(INSTANCE_ID, childPk.getInstanceId());
    assertEquals(INSTANCE_ID, childPk.getComponentName());
    assertNull(childPk.getSpaceId());
    assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetDescendantPKs() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("3", INSTANCE_ID);
    Collection children = NodeDAO.getDescendantPKs(connection,
        nodePK);
    assertNotNull(children);
    assertEquals(1, children.size());
    nodePK = new NodePK("0", INSTANCE_ID);
    children = NodeDAO.getDescendantPKs(connection, nodePK);
    assertNotNull(children);
    assertEquals(4, children.size());
    Iterator iter = children.iterator();
    NodePK childPk = (NodePK) iter.next();
    assertNotNull(childPk);
    assertEquals("1", childPk.getId());
    assertEquals(INSTANCE_ID, childPk.getInstanceId());
    assertEquals(INSTANCE_ID, childPk.getComponentName());
    assertNull(childPk.getSpaceId());
    assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    childPk = (NodePK) iter.next();
    assertNotNull(childPk);
    assertEquals("2", childPk.getId());
    assertEquals(INSTANCE_ID, childPk.getInstanceId());
    assertEquals(INSTANCE_ID, childPk.getComponentName());
    assertNull(childPk.getSpaceId());
    assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    childPk = (NodePK) iter.next();
    assertNotNull(childPk);
    assertEquals("3", childPk.getId());
    assertEquals(INSTANCE_ID, childPk.getInstanceId());
    assertEquals(INSTANCE_ID, childPk.getComponentName());
    assertNull(childPk.getSpaceId());
    assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    childPk = (NodePK) iter.next();
    assertEquals("4", childPk.getId());
    assertEquals(INSTANCE_ID, childPk.getInstanceId());
    assertEquals(INSTANCE_ID, childPk.getComponentName());
    assertNull(childPk.getSpaceId());
    assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetDescendantDetailsConnectionNodePK() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("3", INSTANCE_ID);
    Collection children = NodeDAO.getDescendantDetails(
        connection, nodePK);
    assertNotNull(children);
    assertEquals(1, children.size());
    nodePK = new NodePK("0", INSTANCE_ID);
    children = NodeDAO.getDescendantDetails(connection, nodePK);
    assertNotNull(children);
    assertEquals(4, children.size());
    Iterator iter = children.iterator();
    NodeDetail detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = (NodeDetail) iter.next();
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetDescendantDetailsConnectionNodeDetail() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("3", INSTANCE_ID);
    NodeDetail parent = new NodeDetail();
    parent.setNodePK(nodePK);
    parent.setPath("/0/");
    Collection children = NodeDAO.getDescendantDetails(connection, parent.getNodePK());
    assertNotNull(children);
    assertEquals(1, children.size());
    nodePK = new NodePK("0", INSTANCE_ID);
    parent = new NodeDetail();
    parent.setNodePK(nodePK);
    parent.setPath("/");
    children = NodeDAO.getDescendantDetails(connection,
        parent.getNodePK());
    assertNotNull(children);
    assertEquals(4, children.size());
    Iterator iter = children.iterator();
    NodeDetail detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = (NodeDetail) iter.next();
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetHeadersByLevel() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("3", INSTANCE_ID);
    Collection children = NodeDAO.getHeadersByLevel(connection,
        nodePK, 1);
    assertNotNull(children);
    assertEquals(1, children.size());
    children = NodeDAO.getHeadersByLevel(connection, nodePK, 2);
    assertNotNull(children);
    assertEquals(3, children.size());
    Iterator iter = children.iterator();
    NodeDetail detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    children = NodeDAO.getHeadersByLevel(connection, nodePK, 3);
    assertNotNull(children);
    assertEquals(1, children.size());
    detail = (NodeDetail) children.iterator().next();
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetAllHeaders() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    List tree = NodeDAO.getTree(connection, nodePK);
    assertNotNull(tree);
    assertEquals(5, tree.size());
    nodePK = new NodePK("3", INSTANCE_ID);
    tree = NodeDAO.getTree(connection, nodePK);
    assertNotNull(tree);
    assertEquals(5, tree.size());
    NodeDetail detail = (NodeDetail) tree.get(0);
    assertNotNull(detail);
    assertEquals(0, detail.getId());
    assertEquals("Accueil", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("La Racine", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(1, detail.getLevel());
    assertEquals("-1", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(0, detail.getOrder());
    assertEquals("/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) tree.get(2);
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) tree.get(1);
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) tree.get(3);
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = (NodeDetail) tree.get(4);
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetAnotherPath() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("3", INSTANCE_ID);
    Collection tree = NodeDAO.getAnotherPath(connection, nodePK);
    assertNotNull(tree);
    assertEquals(2, tree.size());
    Iterator iter = tree.iterator();
    NodeDetail detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(0, detail.getId());
    assertEquals("Accueil", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("La Racine", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(1, detail.getLevel());
    assertEquals("-1", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(0, detail.getOrder());
    assertEquals("/", detail.getPath());
    assertNull(detail.getType());
    nodePK = new NodePK("4", INSTANCE_ID);
    tree = NodeDAO.getAnotherPath(connection, nodePK);
    assertNotNull(tree);
    assertEquals(3, tree.size());
    iter = tree.iterator();
    detail = (NodeDetail) iter.next();
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
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(0, detail.getId());
    assertEquals("Accueil", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("La Racine", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(1, detail.getLevel());
    assertEquals("-1", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(0, detail.getOrder());
    assertEquals("/", detail.getPath());
    assertNull(detail.getType());
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetAnotherHeader() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("2", INSTANCE_ID);
    NodePK result = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(result);
    NodeDetail detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    nodePK = new NodePK("1", INSTANCE_ID);
    result = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(result);
    detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    nodePK = new NodePK("3", INSTANCE_ID);
    result = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(result);
    detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    nodePK = new NodePK("4", INSTANCE_ID);
    result = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(result);
    detail = result.nodeDetail;
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testGetChildrenDetailsConnectionNodePK() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    Collection<NodeDetail> children = NodeDAO.getChildrenDetails(
        connection, nodePK);
    assertNotNull(children);
    assertEquals(3, children.size());
    Iterator<NodeDetail> iter = children.iterator();
    NodeDetail detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = (NodeDetail) iter.next();
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    detail = iter.next();
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    nodePK = new NodePK("3", INSTANCE_ID);
    children = NodeDAO.getChildrenDetails(connection, nodePK);
    assertNotNull(children);
    assertEquals(1, children.size());
    detail = children.iterator().next();
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }


  @Test
  public void testGetChildrenNumber() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    assertEquals(3, NodeDAO.getChildrenNumber(connection, nodePK));
    nodePK = new NodePK("3", INSTANCE_ID);
    assertEquals(1, NodeDAO.getChildrenNumber(connection, nodePK));
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testSelectByPrimaryKey() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("2", INSTANCE_ID);
    NodePK detail = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("2", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(2, detail.nodeDetail.getId());
    assertEquals("Déclassées", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/23", detail.nodeDetail.getCreationDate());
    assertEquals("0", detail.nodeDetail.getCreatorId());
    assertEquals("Invisible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(1, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertNull(detail.nodeDetail.getType());
    nodePK = new NodePK("1", INSTANCE_ID);
    detail = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("1", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(1, detail.nodeDetail.getId());
    assertEquals("Corbeille", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/23", detail.nodeDetail.getCreationDate());
    assertEquals("0", detail.nodeDetail.getCreatorId());
    assertEquals("Invisible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(2, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertNull(detail.nodeDetail.getType());
    nodePK = new NodePK("3", INSTANCE_ID);
    detail = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("3", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(3, detail.nodeDetail.getId());
    assertEquals("Theme de Test", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/30", detail.nodeDetail.getCreationDate());
    assertEquals("7", detail.nodeDetail.getCreatorId());
    assertEquals("Visible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(3, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertEquals("default", detail.nodeDetail.getType());
    nodePK = new NodePK("4", INSTANCE_ID);
    detail = NodeDAO.selectByPrimaryKey(connection, nodePK);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("4", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(4, detail.nodeDetail.getId());
    assertEquals("Sous Theme de Test", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications peuvent se retrouver ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/05/10", detail.nodeDetail.getCreationDate());
    assertEquals("7", detail.nodeDetail.getCreatorId());
    assertEquals("Visible", detail.nodeDetail.getStatus());
    assertEquals(3, detail.nodeDetail.getLevel());
    assertEquals("3", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(1, detail.nodeDetail.getOrder());
    assertEquals("/0/3/", detail.nodeDetail.getPath());
    assertEquals("default", detail.nodeDetail.getType());
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

/*  @Test
  public void testSelectByNameAndFatherId() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("2", INSTANCE_ID);
    NodePK detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Déclassées", 0);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("2", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(2, detail.nodeDetail.getId());
    assertEquals("Déclassées", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/23", detail.nodeDetail.getCreationDate());
    assertEquals("0", detail.nodeDetail.getCreatorId());
    assertEquals("Invisible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(1, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertNull(detail.nodeDetail.getType());
    try {
      detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Toto", 0);
      fail();
    } catch (NodeRuntimeException nrex) {
      assertEquals("root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", nrex.getMessage());
    }
    assertNotNull(detail);
    nodePK = new NodePK("1", INSTANCE_ID);
    detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Corbeille", 0);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("1", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(1, detail.nodeDetail.getId());
    assertEquals("Corbeille", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/23", detail.nodeDetail.getCreationDate());
    assertEquals("0", detail.nodeDetail.getCreatorId());
    assertEquals("Invisible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(2, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertNull(detail.nodeDetail.getType());
    nodePK = new NodePK("3", INSTANCE_ID);
    detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Theme de Test", 0);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("3", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(3, detail.nodeDetail.getId());
    assertEquals("Theme de Test", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/04/30", detail.nodeDetail.getCreationDate());
    assertEquals("7", detail.nodeDetail.getCreatorId());
    assertEquals("Visible", detail.nodeDetail.getStatus());
    assertEquals(2, detail.nodeDetail.getLevel());
    assertEquals("0", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(3, detail.nodeDetail.getOrder());
    assertEquals("/0/", detail.nodeDetail.getPath());
    assertEquals("default", detail.nodeDetail.getType());
    nodePK = new NodePK("4", INSTANCE_ID);
    try {
      detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Theme de Test", 3);
      fail();
    } catch (NodeRuntimeException nrex) {
      assertEquals("root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", nrex.getMessage());
    }
    detail = NodeDAO.selectByNameAndFatherId(connection, nodePK, "Sous Theme de Test", 3);
    assertNotNull(detail);
    assertNotNull(detail.nodeDetail);
    assertEquals("4", detail.getId());
    assertEquals(INSTANCE_ID, detail.getInstanceId());
    assertEquals(INSTANCE_ID, detail.getComponentName());
    assertEquals(4, detail.nodeDetail.getId());
    assertEquals("Sous Theme de Test", detail.nodeDetail.getName());
    assertEquals(-1, detail.nodeDetail.getNbObjects());
    assertEquals("Vos publications peuvent se retrouver ici",
        detail.nodeDetail.getDescription());
    assertEquals("2008/05/10", detail.nodeDetail.getCreationDate());
    assertEquals("7", detail.nodeDetail.getCreatorId());
    assertEquals("Visible", detail.nodeDetail.getStatus());
    assertEquals(3, detail.nodeDetail.getLevel());
    assertEquals("3", detail.nodeDetail.getFatherPK().getId());
    assertEquals("", detail.nodeDetail.getModelId());
    assertEquals(false, detail.nodeDetail.haveInheritedRights());
    assertEquals(false, detail.nodeDetail.haveLocalRights());
    assertEquals(false, detail.nodeDetail.haveRights());
    assertEquals(-1, detail.nodeDetail.getRightsDependsOn());
    assertNull(detail.nodeDetail.getChildrenDetails());
    assertEquals(1, detail.nodeDetail.getOrder());
    assertEquals("/0/3/", detail.nodeDetail.getPath());
    assertEquals("default", detail.nodeDetail.getType());
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }

  @Test
  public void testSelectByFatherPrimaryKey() throws Exception {
    IDatabaseConnection dataSetConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dataSetConnection.getConnection();
    NodePK nodePK = new NodePK("0", INSTANCE_ID);
    Collection children = NodeDAO.getChildrenPKs(connection, nodePK);
    assertNotNull(children);
    assertEquals(3, children.size());
    Iterator iter = children.iterator();
    NodePK result = (NodePK) iter.next();
    assertNotNull(result);
    NodeDetail detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(2, detail.getId());
    assertEquals("Déclassées", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(1, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    result = (NodePK) iter.next();
    assertNotNull(result);
    detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(1, detail.getId());
    assertEquals("Corbeille", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals(
        "Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
    assertEquals("2008/04/23", detail.getCreationDate());
    assertEquals("0", detail.getCreatorId());
    assertEquals("Invisible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(2, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertNull(detail.getType());
    result = (NodePK) iter.next();
    assertNotNull(result);
    detail = result.nodeDetail;
    assertNotNull(detail);
    assertEquals(3, detail.getId());
    assertEquals("Theme de Test", detail.getName());
    assertEquals(-1, detail.getNbObjects());
    assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
    assertEquals("2008/04/30", detail.getCreationDate());
    assertEquals("7", detail.getCreatorId());
    assertEquals("Visible", detail.getStatus());
    assertEquals(2, detail.getLevel());
    assertEquals("0", detail.getFatherPK().getId());
    assertEquals("", detail.getModelId());
    assertEquals(false, detail.haveInheritedRights());
    assertEquals(false, detail.haveLocalRights());
    assertEquals(false, detail.haveRights());
    assertEquals(-1, detail.getRightsDependsOn());
    assertNull(detail.getChildrenDetails());
    assertEquals(3, detail.getOrder());
    assertEquals("/0/", detail.getPath());
    assertEquals("default", detail.getType());
    nodePK = new NodePK("3", INSTANCE_ID);
    children = NodeDAO.getChildrenPKs(connection, nodePK);
    assertNotNull(children);
    assertEquals(1, children.size());
    result = (NodePK) children.iterator().next();
    assertNotNull(result);
    detail = result.nodeDetail;
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
    baseTest.getDatabaseTester().closeConnection(dataSetConnection);
  }*/
}
