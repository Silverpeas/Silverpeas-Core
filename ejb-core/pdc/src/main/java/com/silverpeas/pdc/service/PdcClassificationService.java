/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.service;

import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcPosition;
import java.util.ArrayList;
import com.silverpeas.pdc.dao.PdcAxisValueDAO;
import java.util.List;
import com.silverpeas.SilverpeasContent;
import com.silverpeas.pdc.dao.PdcClassificationDAO;
import com.silverpeas.pdc.model.PdcClassification;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import static com.silverpeas.pdc.model.PdcClassification.*;
import static com.silverpeas.util.StringUtil.isDefined;

/**
 * The service aiming at classifying the contents in Silverpeas on the classification plan (named
 * PdC).
 * 
 * The classification of a content in Silverpeas consists to position it on the PdC; it is the
 * process to attribute some semantic metadata to the content. The metadata then can be used to find
 * any contents that satisfies a search of information by keywords. As some contents can not to be 
 * positioned on the PdC (it is not mandatory), they can be not detected in the search.
 * 
 * The classification of a content on the PdC can be done in one of the two ways:
 * <ul>
 * <li>the contributor can position explicitly the content on the PdC,</li>
 * <li>a predefined classification is used either to classify automatically the content or as a
 * template to classify the content.</li>
 * </ul>
 * 
 * A predefined classification on the PdC can be created for a given node in a Silverpeas component
 * instance or for the whole component instance. The predefined classification is a way to facilitate
 * or to impose the classification on the PdC of the contents when they are published. A node in
 * Silverpeas is a way to hierarchically categorize a content. A node can represent for example a
 * topic. A node is part of a hierarchic tree and it can then contain both some contents and some
 * nodes. So, a predefined classification on the PdC associated with a node is set for all the
 * contents in its children nodes. Therefore, if a predefined classification is not found for a
 * given node, then it is seeked back upto the root node (that is the component instance ifself).
 */
@Named
@Transactional
public class PdcClassificationService {

  @Inject
  private PdcClassificationDAO classificationDao;
  @Inject
  private PdcAxisValueDAO valueDao;
  private NodeBm nodeBm;
  @Inject
  private PdcBm pdcBm;

  /**
   * Finds a predefined classification on the PdC that was set for any new contents in the specified
   * node of the specified component instance. If the specified node isn't defined, then the
   * predefined classification associated with the whole component instance is seeked.
   * 
   * If no predefined classification is found for the specified node, then it is seeked back upto
   * the root node (that is the component instance ifself). In the case no predefined classification
   * is set for the whole component instance, an empty classification is then returned. To get the
   * predefined classification that is set exactly for the specified node (if any), then use the
   * <code>getPreDefinedClassification(java.lang.String, java.lang.String</code> method.
   * @param nodeId the unique identifier of the node.
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content published
   * in the specified node or an empty classification.
   */
  public PdcClassification findAPreDefinedClassification(String nodeId, String instanceId) {
    try {
      PdcClassification classification = null;
      if (isDefined(nodeId)) {
        NodePK nodeToSeek = new NodePK(nodeId, instanceId);
        while (classification == null && !nodeToSeek.isUndefined()) {
          classification = classificationDao.findPredefinedClassificationByNodeId(nodeToSeek.getId(),
                  nodeToSeek.getInstanceId());
          NodeDetail node = getNodeBm().getDetail(nodeToSeek);
          nodeToSeek = node.getFatherPK();
        }
        if (classification == null) {
          classification = getPreDefinedClassification(instanceId);
        }
      } else {
        classification = getPreDefinedClassification(instanceId);
      }
      return classification;
    } catch (RemoteException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName() + ".getPreDefinedClassification()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
              ex);
    } catch (Exception ex) {
      throw new EntityNotFoundException(ex.getMessage());
    }
  }

  /**
   * Gets the predefined classification on the PdC that was set for any new contents in the specified
   * node of the specified component instance. If the specified node isn't defined, then the
   * predefined classification associated with the whole component instance is seeked.
   * 
   * In the case no predefined classification is set for the specified node or for the component
   * instance, then a none classification is then returned.
   * @param nodeId the unique node identifier.
   * @param instanceId the unique component instance identifier.
   * @return a predefined classification on the PdC associated with the specified node or with
   * the specified component instance or an empty classification.
   */
  public PdcClassification getPreDefinedClassification(String nodeId, String instanceId) {
    if (!isDefined(nodeId)) {
      return getPreDefinedClassification(instanceId);
    }
    NodePK nodeToSeek = new NodePK(nodeId, instanceId);
    PdcClassification classification =
            classificationDao.findPredefinedClassificationByNodeId(nodeToSeek.getId(),
            nodeToSeek.getInstanceId());
    if (classification == null) {
      classification = NONE_CLASSIFICATION;
    }
    return classification;
  }

  /**
   * Gets the predefined classification on the PdC that was set for any new contents managed in the
   * specified component instance. This method is for the component instances that
   * don't support the categorization.
   * 
   * In the case no predefined classification is set for the whole component instance, a none
   * classification is then returned.
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content
   * published within the component instance or an empty classification.
   */
  public PdcClassification getPreDefinedClassification(String instanceId) {
    PdcClassification classification = classificationDao.
            findPredefinedClassificationByComponentInstanceId(
            instanceId);
    if (classification == null) {
      classification = NONE_CLASSIFICATION;
    }
    return classification;
  }

  /**
   * Saves the specified predefined classification on the PdC. If a predefined classification
   * already exists for the node (if any) and the component instance to which this classification is
   * related, then it is replaced by the specified one.
   * If the specified classification is empty (all positions were deleted), then it is deleted and
   * the NONE_CLASSIFICATION is sent back.
   * 
   * The node (if any) and the component instance for which this classification has to be saved
   * are indicated by the specified classification itself. If no node is refered by it, then the
   * predefined classification will serv for the whole component instance.
   * @param classification either the saved predefined classification or NONE_CLASSIFICATION.
   */
  public PdcClassification savePreDefinedClassification(final PdcClassification classification) {
    if (!classification.isPredefined()) {
      throw new IllegalArgumentException("The classification isn't a predefined one");
    }
    PdcClassification savedClassification = NONE_CLASSIFICATION;
    if (classification.getId() != null && classificationDao.exists(classification.getId())
            && classification.isEmpty()) {
      classificationDao.delete(classification);
    } else {
      List<PdcAxisValue> allValues = new ArrayList<PdcAxisValue>();
      for (PdcPosition aPosition : classification.getPositions()) {
        allValues.addAll(aPosition.getValues());
      }
      valueDao.save(allValues);
      savedClassification = classificationDao.saveAndFlush(classification);
    }
    return savedClassification;
  }

  /**
   * Deletes all the predefined classification set for the specified node and its children nodes
   * in the specified component instance. If the specified node is null, then all the predefined
   * classifications set for all nodes in the component instance (plus the one for the component
   * instance itself) are deleted.
   * 
   * This method is mainly dedicated when a given node or a given component instance is removed
   * from Silverpeas.
   * @param nodeId the unique identifier of the node.
   * @param instanceId the unique identifier of the component instance.
   */
  public void deleteAllPreDefinedClassifications(String nodeId, String instanceId) {
    Collection<NodeDetail> nodes;
    if (isDefined(nodeId)) {
      nodes = new ArrayList<NodeDetail>();
      NodeDetail currentNode = new NodeDetail();
      currentNode.setNodePK(new NodePK(nodeId, instanceId));
      nodes.add(currentNode);
    } else {
      try {
        nodes = getNodeBm().getAllNodes(new NodePK(null, instanceId));
        NodeDetail forComponentInstance = new NodeDetail();
        forComponentInstance.setNodePK(new NodePK(null, instanceId));
        nodes.add(forComponentInstance);
      } catch (RemoteException ex) {
        throw new PdcRuntimeException(getClass().getSimpleName(), SilverpeasRuntimeException.ERROR,
                ex.getMessage(), ex);
      }
    }

    for (NodeDetail nodeDetail : nodes) {
      PdcClassification classification = getPreDefinedClassification(
              nodeDetail.getNodePK().getId(), instanceId);
      if (classification != NONE_CLASSIFICATION) {
        classificationDao.delete(classification);
      }
    }
  }

  /**
   * Classifies the specified content on the PdC with the specified classification. If the
   * content is already classified, then the given classification replaces the existing one.
   * The content must exist in Silverpeas before being classified.
   * If an error occurs while classifying the content, a runtime exception PdcRuntimeException is
   * thrown.
   * @param content the Silverpeas content to classify.
   * @param withClassification the classification with which the content is positioned on the PdC.
   */
  public void classifyContent(final SilverpeasContent content,
          final PdcClassification withClassification) throws PdcRuntimeException {
    List<ClassifyPosition> classifyPositions = withClassification.getClassifyPositions();
    try {
      int silverObjectId = Integer.valueOf(content.getSilverpeasContentId());
      List<ClassifyPosition> existingPositions = pdcBm.getPositions(silverObjectId, content.
              getComponentInstanceId());
      for (ClassifyPosition aClassifyPosition : classifyPositions) {
        int positionId = pdcBm.addPosition(silverObjectId, aClassifyPosition, content.
                getComponentInstanceId());
        aClassifyPosition.setPositionId(positionId);
      }
      if (!existingPositions.isEmpty()) {
        for (ClassifyPosition anExistingPosition : existingPositions) {
          if (!isFound(anExistingPosition, classifyPositions)) {
            pdcBm.deletePosition(anExistingPosition.getPositionId(),
                    content.getComponentInstanceId());
          }
        }
      }
    } catch (PdcException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName() + ".classifyContent()", ex.
              getErrorLevel(), ex.getMessage(), ex);
    }
  }

  /**
   * Some values come to be removed from the PdC. Triggers the update of all concerned
   * classifications taken in charge by this service (for instance, only the predefined
   * classifications).
   * 
   * For each value, according to its level in the hierarchical tree representing the PdC's axis,
   * the correct update behaviour is selected for a given classification:
   * <ul>
   * <li>The value is a root one of the axis: the value is removed from any positions of the
   * classification. If the position is empty (it has no values) it is then removed. If the
   * classification is then empty, it is removed.</li>
   * <li>The value is a leaf in a branch: the value is replaced by its mother value in any positions
   * of the classification.</li>
   * </ul>
   * @param deletedValue the value that is removed from a PdC's axis.
   */
  public void axisValuesDeleted(final List<PdcAxisValue> deletedValues) {
    List<PdcClassification> concernedClassifications = classificationDao.
            findClassificationsByPdcAxisValues(deletedValues);
    for (PdcClassification aClassification : concernedClassifications) {
      aClassification.updateForPdcAxisValuesDeletion(deletedValues);
      savePreDefinedClassification(aClassification);
    }
    classificationDao.flush(); // apply now all the modifications

    // for instance, the PdcAxisValue objects are taken in charge by this service as they are only
    // used in the predefined classification. Nevertheless, it is planned they will be used when
    // refactoring the PdC old code on the classification of contents and in the PdC definition.
    valueDao.delete(deletedValues);
  }

  /**
   * An axis comes to be removed from the PdC. Triggers the update of all concerned
   * classifications taken in charge by this service (for instance, only the predefined
   * classifications).
   * 
   * The classifications are updated as following:
   * <ul>
   * <li>For each position the values related to the axis are removed.</li>
   * <li>If a position is empty, it is removed.<li>
   * <li>If a classification is empty, it is removed.<li>
   * </ul>
   * @param axisId the unique identifier of the axis.
   */
  public void axisDeleted(String axisId) {
    List<PdcAxisValue> valuesToDelete = valueDao.findByAxisId(Long.valueOf(axisId));
    if (!valuesToDelete.isEmpty()) {
      axisValuesDeleted(valuesToDelete);
    }
  }

  /**
   * A convenient method to enhance the readability of method calls.
   * @param classification a classification on the PdC.
   * @return the classification.
   */
  public static PdcClassification withClassification(final PdcClassification classification) {
    return classification;
  }

  protected NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome home = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
                JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = home.create();
      } catch (Exception ex) {
        throw new PdcRuntimeException(getClass().getSimpleName() + ".getNodeBm()",
                SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
                ex);
      }
    }
    return nodeBm;
  }

  protected void setNodeBm(final NodeBm nodeBm) {
    this.nodeBm = nodeBm;
  }

  private boolean isFound(ClassifyPosition aPosition,
          List<ClassifyPosition> newPositions) {
    for (ClassifyPosition aNewPosition : newPositions) {
      if (aNewPosition.getPositionId() == aPosition.getPositionId()) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method is called to find all the children of a given node. It is used in order to find all
   * the children of a node that is going to be deleted.
   * Nevertheless, as this wor
   * @param node
   * @return 
   */
  private Collection<NodeDetail> getRecursivelyAllChildren(final NodePK node) {
    try {
      Collection<NodeDetail> children = getNodeBm().getChildrenDetails(node);
      Collection<NodeDetail> allChildren = new ArrayList<NodeDetail>(children);
      for (NodeDetail aChildNode : children) {
        allChildren.addAll(getRecursivelyAllChildren(aChildNode.getNodePK()));
      }
      return allChildren;
    } catch (RemoteException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName(), SilverpeasRuntimeException.ERROR,
              ex.getMessage(), ex);
    }
  }
}
