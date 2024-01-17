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
package org.silverpeas.core.node.dao;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.DateUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.silverpeas.core.node.model.NodeDetail.NO_RIGHTS_DEPENDENCY;
import static org.silverpeas.core.test.rule.DbSetupRule.getSafeConnection;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class NodeDAOIT {

  private static final String INSTANCE_ID = "kmelia60";

  private static final String TABLE_CREATION_SCRIPT = "/node-create-database.sql";
  private static final String DATASET_XML_SCRIPT = "nodes-test-dataset.xml";
  
  @Inject
  private NodeDAO nodeDAO;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(NodeDAOIT.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addIndexEngineFeatures()
        .addWysiwygFeatures()
        .addPublicationTemplateFeatures()
        .testFocusedOn(
            war -> war.addPackages(true, "org.silverpeas.core.node")
                .addAsResource("node-create-database.sql")
                .addAsResource("org/silverpeas/node")
                .addAsResource("org/silverpeas/core/node"))
        .build();
  }

  @Test
  public void testGetTree() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("0", INSTANCE_ID);
      List<NodeDetail> tree = nodeDAO.getTree(connection, nodePK);
      assertNotNull(tree);
      assertEquals(5, tree.size());
      nodePK = new NodePK("3", INSTANCE_ID);
      tree = nodeDAO.getTree(connection, nodePK);
      assertNotNull(tree);
      assertEquals(5, tree.size());
      NodeDetail detail = tree.get(0);
      assertNotNull(detail);
      assertEquals(NodePK.ROOT_NODE_ID, detail.getId());
      assertEquals("Accueil", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("La Racine", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(1, detail.getLevel());
      assertEquals("-1", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(0, detail.getOrder());
      assertEquals("/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = tree.get(2);
      assertNotNull(detail);
      assertEquals(NodePK.BIN_NODE_ID, detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = tree.get(1);
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = tree.get(3);
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = tree.get(4);
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testIsSameNameSameLevelOnCreation() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK pk = new NodePK(null, INSTANCE_ID);
      NodeDetail detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Corbeille");
      detail.setNodePK(pk);
      assertTrue(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(3);
      detail.setName("Corbeille");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Poubelle");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Corbeille");
      pk = new NodePK(null, "kmelia65");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
    }
  }

  @Test
  public void testIsSameNameSameLevelOnUpdate() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK pk = new NodePK("1", INSTANCE_ID);
      NodeDetail detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Corbeille");
      detail.setNodePK(pk);
      assertTrue(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(3);
      detail.setName("Corbeille");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Poubelle");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Corbeille");
      pk = new NodePK("2", "kmelia65");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
      detail = new NodeDetail();
      detail.setLevel(2);
      detail.setName("Corbeille");
      pk = new NodePK("5", "kmelia65");
      detail.setNodePK(pk);
      assertFalse(nodeDAO.isSameNameSameLevelOnCreation(connection, detail));
    }
  }

  @Test
  public void testGetChildrenPKs() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("0", INSTANCE_ID);
      Collection<NodePK> children = nodeDAO.getChildrenPKs(connection, nodePK);
      assertNotNull(children);
      assertEquals(3, children.size());
      nodePK = new NodePK("3", INSTANCE_ID);
      children = nodeDAO.getChildrenPKs(connection, nodePK);
      assertNotNull(children);
      assertEquals(1, children.size());
      NodePK childPk = children.iterator().next();
      assertNotNull(childPk);
      assertEquals("4", childPk.getId());
      assertEquals(INSTANCE_ID, childPk.getInstanceId());
      assertEquals(INSTANCE_ID, childPk.getComponentName());
      assertNull(childPk.getSpaceId());
      assertEquals("sb_node_node", childPk.getTableName().toLowerCase());
    }
  }

  @Test
  public void testGetDescendantPKs() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("3", INSTANCE_ID);
      Collection children = nodeDAO.getDescendantPKs(connection, nodePK);
      assertNotNull(children);
      assertEquals(1, children.size());
      nodePK = new NodePK("0", INSTANCE_ID);
      children = nodeDAO.getDescendantPKs(connection, nodePK);
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
    }
  }

  @Test
  public void testGetDescendantDetailsConnectionNodePK() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("3", INSTANCE_ID);
      Collection children = nodeDAO.getDescendantDetails(connection, nodePK);
      assertNotNull(children);
      assertEquals(1, children.size());
      nodePK = new NodePK("0", INSTANCE_ID);
      children = nodeDAO.getDescendantDetails(connection, nodePK);
      assertNotNull(children);
      assertEquals(4, children.size());
      Iterator iter = children.iterator();
      NodeDetail detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testGetDescendantDetailsConnectionNodeDetail() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("3", INSTANCE_ID);
      NodeDetail parent = new NodeDetail();
      parent.setNodePK(nodePK);
      parent.setPath("/0/");
      Collection children = nodeDAO.getDescendantDetails(connection, parent.getNodePK());
      assertNotNull(children);
      assertEquals(1, children.size());
      nodePK = new NodePK("0", INSTANCE_ID);
      parent = new NodeDetail();
      parent.setNodePK(nodePK);
      parent.setPath("/");
      children = nodeDAO.getDescendantDetails(connection, parent.getNodePK());
      assertNotNull(children);
      assertEquals(4, children.size());
      Iterator iter = children.iterator();
      NodeDetail detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testGetHeadersByLevel() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("3", INSTANCE_ID);
      Collection children = nodeDAO.getHeadersByLevel(connection, nodePK, 1);
      assertNotNull(children);
      assertEquals(1, children.size());
      children = nodeDAO.getHeadersByLevel(connection, nodePK, 2);
      assertNotNull(children);
      assertEquals(3, children.size());
      Iterator iter = children.iterator();
      NodeDetail detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      children = nodeDAO.getHeadersByLevel(connection, nodePK, 3);
      assertNotNull(children);
      assertEquals(1, children.size());
      detail = (NodeDetail) children.iterator().next();
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testGetAllHeaders() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("0", INSTANCE_ID);
      List tree = nodeDAO.getTree(connection, nodePK);
      assertNotNull(tree);
      assertEquals(5, tree.size());
      nodePK = new NodePK("3", INSTANCE_ID);
      tree = nodeDAO.getTree(connection, nodePK);
      assertNotNull(tree);
      assertEquals(5, tree.size());
      NodeDetail detail = (NodeDetail) tree.get(0);
      assertNotNull(detail);
      assertEquals("0", detail.getId());
      assertEquals("Accueil", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("La Racine", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(1, detail.getLevel());
      assertEquals("-1", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(0, detail.getOrder());
      assertEquals("/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) tree.get(2);
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) tree.get(1);
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = (NodeDetail) tree.get(3);
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) tree.get(4);
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testGetPath() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("3", INSTANCE_ID);
      Collection tree = nodeDAO.getNodePath(connection, nodePK);
      assertNotNull(tree);
      assertEquals(2, tree.size());
      Iterator iter = tree.iterator();
      NodeDetail detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("0", detail.getId());
      assertEquals("Accueil", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("La Racine", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(1, detail.getLevel());
      assertEquals("-1", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(0, detail.getOrder());
      assertEquals("/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      nodePK = new NodePK("4", INSTANCE_ID);
      tree = nodeDAO.getNodePath(connection, nodePK);
      assertNotNull(tree);
      assertEquals(3, tree.size());
      iter = tree.iterator();
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      detail = (NodeDetail) iter.next();
      assertNotNull(detail);
      assertEquals("0", detail.getId());
      assertEquals("Accueil", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("La Racine", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(1, detail.getLevel());
      assertEquals("-1", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(0, detail.getOrder());
      assertEquals("/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
    }
  }

  @Test
  public void testGetHeader() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("2", INSTANCE_ID);
      NodeDetail detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      nodePK = new NodePK("1", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      nodePK = new NodePK("3", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      nodePK = new NodePK("4", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }

  @Test
  public void testGetChildrenDetailsConnectionNodePK() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("0", INSTANCE_ID);
      Collection<NodeDetail> children = nodeDAO.getChildrenDetails(connection, nodePK);
      assertNotNull(children);
      assertEquals(3, children.size());
      Iterator<NodeDetail> iter = children.iterator();
      NodeDetail detail = iter.next();
      assertNotNull(detail);
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = iter.next();
      assertNotNull(detail);
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      detail = iter.next();
      assertNotNull(detail);
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      nodePK = new NodePK("3", INSTANCE_ID);
      children = nodeDAO.getChildrenDetails(connection, nodePK);
      assertNotNull(children);
      assertEquals(1, children.size());
      detail = children.iterator().next();
      assertNotNull(detail);
      assertEquals("4", detail.getId());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }


  @Test
  public void testGetChildrenNumber() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("0", INSTANCE_ID);
      assertEquals(3, nodeDAO.getChildrenNumber(connection, nodePK));
      nodePK = new NodePK("3", INSTANCE_ID);
      assertEquals(1, nodeDAO.getChildrenNumber(connection, nodePK));
    }
  }

  @Test
  public void testSelectByPrimaryKey() throws Exception {
    try (Connection connection = getSafeConnection()) {
      NodePK nodePK = new NodePK("2", INSTANCE_ID);
      NodeDetail detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("2", detail.getNodePK().getId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getInstanceId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getComponentName());
      assertEquals("2", detail.getId());
      assertEquals("Déclassées", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications inaccessibles se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      nodePK = new NodePK("1", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("1", detail.getNodePK().getId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getInstanceId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getComponentName());
      assertEquals("1", detail.getId());
      assertEquals("Corbeille", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vous trouvez ici les publications que vous avez supprimées", detail.getDescription());
      assertEquals("2008/04/23", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("0", detail.getCreatorId());
      assertEquals("Invisible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(2, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals(NodeDetail.DEFAULT_NODE_TYPE, detail.getNodeType());
      nodePK = new NodePK("3", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("3", detail.getNodePK().getId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getInstanceId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getComponentName());
      assertEquals("3", detail.getId());
      assertEquals("Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications de test se retrouvent ici", detail.getDescription());
      assertEquals("2008/04/30", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(2, detail.getLevel());
      assertEquals("0", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(3, detail.getOrder());
      assertEquals("/0/", detail.getPath());
      assertEquals("default", detail.getNodeType());
      nodePK = new NodePK("4", INSTANCE_ID);
      detail = nodeDAO.selectByPrimaryKey(connection, nodePK);
      assertNotNull(detail);
      assertEquals("4", detail.getNodePK().getId());
      assertEquals("4", detail.getId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getInstanceId());
      assertEquals(INSTANCE_ID, detail.getNodePK().getComponentName());
      assertEquals("Sous Theme de Test", detail.getName());
      assertEquals(-1, detail.getNbObjects());
      assertEquals("Vos publications peuvent se retrouver ici", detail.getDescription());
      assertEquals("2008/05/10", DateUtil.date2SQLDate(detail.getCreationDate()));
      assertEquals("7", detail.getCreatorId());
      assertEquals("Visible", detail.getStatus());
      assertEquals(3, detail.getLevel());
      assertEquals("3", detail.getFatherPK().getId());
      assertNull(detail.getModelId());
      assertFalse(detail.haveInheritedRights());
      assertFalse(detail.haveLocalRights());
      assertFalse(detail.haveRights());
      assertEquals(NO_RIGHTS_DEPENDENCY, detail.getRightsDependsOn());
      assertNull(detail.getChildrenDetails());
      assertEquals(1, detail.getOrder());
      assertEquals("/0/3/", detail.getPath());
      assertEquals("default", detail.getNodeType());
    }
  }
}
