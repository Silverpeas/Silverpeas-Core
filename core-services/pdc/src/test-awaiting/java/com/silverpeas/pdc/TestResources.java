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
package com.silverpeas.pdc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.pdc.mock.PdcBmMock;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;

import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import com.stratelia.silverpeas.treeManager.model.TreeNodePK;
import com.stratelia.webactiv.node.NodeBmProvider;
import com.stratelia.webactiv.node.control.NodeService;
import org.silverpeas.util.exception.SilverpeasException;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.node.model.NodeRuntimeException;

import org.springframework.test.util.ReflectionTestUtils;

import static com.silverpeas.pdc.model.PdcAxisValue.aPdcAxisValueFromTreeNode;
import static com.silverpeas.pdc.model.PdcModelHelper.*;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * It wraps the resources and the parameters to use in the unit tests
 */
@Named
public final class TestResources {

  public static final String COMPONENT_INSTANCE_1_ID = "kmelia1";
  public static final String COMPONENT_INSTANCE_2_ID = "kmelia2";
  public static final String COMPONENT_INSTANCE_3_ID = "kmelia5";
  public static final String COMPONENT_INSTANCE_1_NODE_1_ID = "21";
  public static final String COMPONENT_INSTANCE_1_NODE_2_ID = "22";
  public static final String COMPONENT_INSTANCE_2_NODE_1_ID = "3";
  public static final String COMPONENT_INSTANCE_2_NODE_2_ID = "4";
  public static final String COMPONENT_INSTANCE_3_NODE_1_ID = "10";
  public static final String COMPONENT_INSTANCE_3_NODE_2_ID = "11";
  public static final String CONTENT_ID = "1";
  public static final String UNEXISTING_NODE_ID = "1000";
  @Inject
  private PdcBmMock pdcBmMock;
  @Inject
  private NodeBmProvider nodeBmProvider;
  private NodeService nodeServiceMock = mock(NodeService.class);

  @PostConstruct
  protected void init() {
    ReflectionTestUtils.invokeSetterMethod(nodeBmProvider, "setNodeBm", nodeServiceMock);
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_1_NODE_2_ID, COMPONENT_INSTANCE_1_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_1_NODE_2_ID,
        COMPONENT_INSTANCE_1_ID), "", "", "", "", "",
        1, new NodePK(COMPONENT_INSTANCE_1_NODE_1_ID, COMPONENT_INSTANCE_1_ID),
        new ArrayList<NodeDetail>()));
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_1_NODE_1_ID, COMPONENT_INSTANCE_1_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_1_NODE_1_ID,
        COMPONENT_INSTANCE_1_ID), "", "", "", "", "",
        1, new NodePK(NodePK.UNDEFINED_NODE_ID, COMPONENT_INSTANCE_1_ID),
        new ArrayList<NodeDetail>()));
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_2_NODE_2_ID, COMPONENT_INSTANCE_2_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_2_NODE_2_ID,
        COMPONENT_INSTANCE_2_ID), "", "", "", "", "",
        1, new NodePK(COMPONENT_INSTANCE_2_NODE_1_ID, COMPONENT_INSTANCE_2_ID),
        new ArrayList<NodeDetail>()));
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_2_NODE_1_ID, COMPONENT_INSTANCE_2_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_2_NODE_1_ID,
        COMPONENT_INSTANCE_2_ID), "", "", "", "", "",
        1, new NodePK(NodePK.UNDEFINED_NODE_ID, COMPONENT_INSTANCE_2_ID),
        new ArrayList<NodeDetail>()));
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_3_NODE_2_ID, COMPONENT_INSTANCE_3_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_3_NODE_2_ID,
        COMPONENT_INSTANCE_3_ID), "", "", "", "", "",
        1, new NodePK(COMPONENT_INSTANCE_3_NODE_1_ID, COMPONENT_INSTANCE_3_ID),
        new ArrayList<NodeDetail>()));
    when(nodeServiceMock.getDetail(new NodePK(COMPONENT_INSTANCE_3_NODE_1_ID, COMPONENT_INSTANCE_3_ID))).
        thenReturn(new NodeDetail(new NodePK(COMPONENT_INSTANCE_3_NODE_1_ID,
        COMPONENT_INSTANCE_3_ID), "", "", "", "", "",
        1, new NodePK(NodePK.UNDEFINED_NODE_ID, COMPONENT_INSTANCE_3_ID),
        new ArrayList<NodeDetail>()));

    when(nodeServiceMock.getDetail(new NodePK(UNEXISTING_NODE_ID, COMPONENT_INSTANCE_1_ID))).thenThrow(
        new NodeRuntimeException("",
        SilverpeasException.ERROR, ""));
    when(nodeServiceMock.getDetail(new NodePK(UNEXISTING_NODE_ID, COMPONENT_INSTANCE_2_ID))).thenThrow(
        new NodeRuntimeException("",
        SilverpeasException.ERROR, ""));
    when(nodeServiceMock.getDetail(new NodePK(UNEXISTING_NODE_ID, COMPONENT_INSTANCE_3_ID))).thenThrow(
        new NodeRuntimeException("",
        SilverpeasException.ERROR, ""));

    pdcBmMock.addTreeNodes(allTreeNodes());
  }

  public TreeNode addTreeNode(String nodeId, String treeId, String parentNodeId, String label) {
    TreeNode treeNode = null;
    if (isDefined(parentNodeId) && !parentNodeId.equals("-1")) {
      try {
        TreeNode parent = pdcBmMock.getValue(treeId, parentNodeId);
        treeNode = aTreeNodeWithParent(nodeId, label, parent);
      } catch (PdcException ex) {
        Logger.getLogger(TestResources.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      treeNode = aRootTreeNode(nodeId, label, treeId);
    }
    pdcBmMock.addTreeNode(treeNode);
    return treeNode;
  }

  public String unexistingNodeId() {
    return UNEXISTING_NODE_ID;
  }

  public String componentInstanceWithAPredefinedClassification() {
    return COMPONENT_INSTANCE_2_ID;
  }

  public String componentInstanceWithOnlyAWholePredefinedClassification() {
    return COMPONENT_INSTANCE_1_ID;
  }

  public String componentInstanceWithoutPredefinedClassification() {
    return COMPONENT_INSTANCE_3_ID;
  }

  public List<String> nodesIdOfComponentInstance(String componentInstanceId) {
    List<String> nodeIds = new ArrayList<String>();
    if (COMPONENT_INSTANCE_2_ID.equals(componentInstanceId)) {
      nodeIds.add(COMPONENT_INSTANCE_2_NODE_1_ID);
      nodeIds.add(COMPONENT_INSTANCE_2_NODE_2_ID);
    } else if (COMPONENT_INSTANCE_3_ID.equals(componentInstanceId)) {
      nodeIds.add(COMPONENT_INSTANCE_3_NODE_1_ID);
      nodeIds.add(COMPONENT_INSTANCE_3_NODE_2_ID);
    } else if (COMPONENT_INSTANCE_1_ID.equals(componentInstanceId)) {
      nodeIds.add(COMPONENT_INSTANCE_1_NODE_1_ID);
      nodeIds.add(COMPONENT_INSTANCE_1_NODE_2_ID);
    }
    return nodeIds;
  }

  public PdcClassification predefinedClassificationForComponentInstance(String instanceId) {
    PdcClassification classification = null;
    List<PdcClassification> classifications = allPdcClassifications();
    for (PdcClassification aClassification : classifications) {
      if (aClassification.getComponentInstanceId().equals(instanceId) && aClassification.
          isPredefinedForTheWholeComponentInstance()) {
        classification = aClassification;
        break;
      }
    }
    return classification;
  }

  public PdcClassification predefinedClassificationForNode(String nodeId, String instanceId) {
    List<String> allNodes = nodesIdOfComponentInstance(instanceId);
    List<String> nodePath = new ArrayList<String>(allNodes.size());
    for (int i = 0; i < allNodes.size(); i++) {
      String aNodeId = allNodes.get(i);
      nodePath.add(aNodeId);
      if (aNodeId.equals(nodeId)) {
        break;
      }
    }
    Collections.reverse(nodePath);
    List<PdcClassification> allClassifications = predefinedClassificationForNodesInOrder(nodePath);
    PdcClassification classification = PdcClassification.NONE_CLASSIFICATION;
    if (!allClassifications.isEmpty()) {
      classification = allClassifications.get(0);
    }
    return classification;
  }

  public List<PdcClassification> predefinedClassificationForNodesInOrder(final List<String> nodeIds) {
    List<PdcClassification> classifications = new ArrayList<PdcClassification>();
    List<PdcClassification> allClassifications = allPdcClassifications();
    for (String nodeId : nodeIds) {
      for (PdcClassification aClassification : allClassifications) {
        if (aClassification.isPredefinedForANode()
            && nodeId.equals(aClassification.getNodeId())) {
          classifications.add(aClassification);
        }
      }
    }
    return classifications;
  }

  public PdcAxisValue aRandomlyPdcAxisValue() {
    List<PdcAxisValue> allValues = allPdcAxisValues();
    int idx = new Random().nextInt(allValues.size());
    return allValues.get(idx);
  }

  public List<PdcClassification> classificationsHavingAsValue(final PdcAxisValue value) {
    List<PdcClassification> concernedClassifications = new ArrayList<PdcClassification>();
    List<PdcClassification> allClassifications = allPdcClassifications();
    for (PdcClassification pdcClassification : allClassifications) {
      boolean isAdded = false;
      for (PdcPosition pdcPosition : pdcClassification.getPositions()) {
        for (PdcAxisValue pdcAxisValue : pdcPosition.getValues()) {
          if (pdcAxisValue.equals(value)) {
            concernedClassifications.add(pdcClassification);
            isAdded = true;
            break;
          }
        }
        if (isAdded) {
          break;
        }
      }
    }
    return concernedClassifications;
  }

  private static List<PdcAxisValue> allPdcAxisValues() {
    List<PdcAxisValue> values = new ArrayList<PdcAxisValue>();
    for (TreeNode aTreeNode : allTreeNodes()) {
      values.add(aPdcAxisValueFromTreeNode(aTreeNode));
    }
    return values;
  }

  private static List<TreeNode> allTreeNodes() {
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    nodes.add(aRootTreeNode("0", "France", "1"));
    nodes.add(aTreeNodeWithParent("1", "Rhône-Alpes", nodes.get(0)));
    nodes.add(aTreeNodeWithParent("2", "Isère", nodes.get(1)));
    nodes.add(aTreeNodeWithParent("3", "Grenoble", nodes.get(2)));

    nodes.add(aRootTreeNode("4", "Epoque contemporaine", "2"));
    nodes.add(aTreeNodeWithParent("5", "Révolution industrielle", nodes.get(4)));
    nodes.add(aTreeNodeWithParent("6", "Entre-deux-guerres", nodes.get(4)));
    nodes.add(aRootTreeNode("7", "Christianisme", "3"));
    nodes.add(aTreeNodeWithParent("8", "Catholicisme", nodes.get(7)));
    nodes.add(aTreeNodeWithParent("9", "Orthodoxie", nodes.get(7)));
    nodes.add(aRootTreeNode("10", "Islam", "3"));
    nodes.add(aTreeNodeWithParent("11", "Sunnisme", nodes.get(10)));
    nodes.add(aTreeNodeWithParent("12", "Chiisme", nodes.get(11)));

    return nodes;
  }

  private static List<PdcClassification> allPdcClassifications() {
    List<PdcClassification> classifications = new ArrayList<PdcClassification>();

    List<PdcAxisValue> values = allPdcAxisValues();

    classifications.add(
        aPredefinedPdcClassification(0).inComponentInstance(COMPONENT_INSTANCE_2_ID).
        withPosition(aPdcPosition(0).withValue(values.get(2)).withValue(values.get(5))).
        withPosition(aPdcPosition(1).withValue(values.get(8))));
    classifications.add(aPredefinedPdcClassification(1).modifiable().inComponentInstance(
        COMPONENT_INSTANCE_2_ID).forNode(COMPONENT_INSTANCE_2_NODE_1_ID).withPosition(aPdcPosition(
        2).withValue(values.get(3))));
    classifications.add(
        aPredefinedPdcClassification(2).inComponentInstance(COMPONENT_INSTANCE_2_ID).
        forNode(COMPONENT_INSTANCE_2_NODE_2_ID).withPosition(aPdcPosition(3).withValue(values.
        get(6))));
    classifications.add(aPdcClassification(3).ofContent(CONTENT_ID).inComponentInstance(
        COMPONENT_INSTANCE_2_ID).withPosition(aPdcPosition(4).withValue(values.get(2))));
    classifications.add(
        aPredefinedPdcClassification(4).inComponentInstance(COMPONENT_INSTANCE_1_ID).
        withPosition(aPdcPosition(5).withValue(values.get(0)).withValue(values.get(4))));

    return classifications;
  }

  private static TreeNode aRootTreeNode(String id, String label, String treeId) {
    TreeNode node = new TreeNode();
    node.setPK(new TreeNodePK(id));
    node.setName(label);
    node.setTreeId(treeId);
    node.setFatherId("-1");
    return node;
  }

  private static TreeNode aTreeNodeWithParent(String id, String label, final TreeNode parent) {
    TreeNode node = new TreeNode();
    node.setPK(new TreeNodePK(id));
    node.setName(label);
    node.setTreeId(parent.getTreeId());
    node.setFatherId(parent.getPK().getId());
    return node;
  }
}
