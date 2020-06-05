/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.model.PdcRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * The default implementation of the PdcClassificationService by using both the JPA-based repository
 * and the older business services on the PdC to find, save, update or delete some classifications
 * or some positions on the PdC.
 */
@Transactional
@Singleton
public class DefaultPdcClassificationService implements PdcClassificationService,
    ComponentInstanceDeletion {

  @Inject
  private PdcClassificationRepository classificationRepository;
  @Inject
  private PdcAxisValueRepository valueRepository;
  @Inject
  private NodeService nodeService;

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
   *
   * @param nodeId the unique identifier of the node.
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content published
   * in the specified node or an empty classification.
   */
  @Override
  public PdcClassification findAPreDefinedClassification(String nodeId, String instanceId) {
    try {
      PdcClassification classification = null;
      if (isDefined(nodeId)) {
        NodePK nodeToSeek = new NodePK(nodeId, instanceId);
        while (classification == null && !nodeToSeek.isUndefined()) {
          classification = classificationRepository.findPredefinedClassificationByNodeId(nodeToSeek.
              getId(), nodeToSeek.getInstanceId());
          NodeDetail node = getNodeService().getDetail(nodeToSeek);
          nodeToSeek = node.getFatherPK();
        }
        if (classification == null) {
          classification = getPreDefinedClassification(instanceId);
        }
      } else {
        classification = getPreDefinedClassification(instanceId);
      }
      return classification;
    } catch (Exception ex) {
      throw new EntityNotFoundException(ex.getMessage());
    }
  }

  /**
   * Gets the predefined classification on the PdC that was set for any new contents in the
   * specified node of the specified component instance. If the specified node isn't defined, then
   * the predefined classification associated with the whole component instance is get.
   *
   * In the case no predefined classification is set for the specified node or for the component
   * instance, then a none classification is then returned.
   *
   * @param nodeId the unique node identifier.
   * @param instanceId the unique component instance identifier.
   * @return a predefined classification on the PdC associated with the specified node or with the
   * specified component instance or an empty classification.
   */
  @Override
  public PdcClassification getPreDefinedClassification(String nodeId, String instanceId) {
    if (!isDefined(nodeId)) {
      return getPreDefinedClassification(instanceId);
    }
    NodePK nodeToSeek = new NodePK(nodeId, instanceId);
    PdcClassification classification = classificationRepository.
        findPredefinedClassificationByNodeId(nodeToSeek.getId(), nodeToSeek.getInstanceId());
    if (classification == null) {
      classification = NONE_CLASSIFICATION;
    }
    return classification;
  }

  /**
   * Gets the predefined classification on the PdC that was set for any new contents managed in the
   * specified component instance. This method is for the component instances that don't support the
   * categorization.
   *
   * In the case no predefined classification is set for the whole component instance, a none
   * classification is then returned.
   *
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content published
   * within the component instance or an empty classification.
   */
  @Override
  public PdcClassification getPreDefinedClassification(String instanceId) {
    PdcClassification classification = classificationRepository.
        findPredefinedClassificationByComponentInstanceId(instanceId);
    if (classification == null) {
      classification = NONE_CLASSIFICATION;
    }
    return classification;
  }

  /**
   * Saves the specified predefined classification on the PdC. If a predefined classification
   * already exists for the node (if any) and the component instance to which this classification is
   * related, then it is replaced by the specified one. If the specified classification is empty
   * (all positions were deleted), then it is deleted and the NONE_CLASSIFICATION is sent back.
   *
   * The node (if any) and the component instance for which this classification has to be saved are
   * indicated by the specified classification itself. If no node is refered by it, then the
   * predefined classification will serv for the whole component instance.
   *
   * @param classification either the saved predefined classification or NONE_CLASSIFICATION.
   * @return
   */
  @Override
  public PdcClassification savePreDefinedClassification(final PdcClassification classification) {
    if (!classification.isPredefined()) {
      throw new IllegalArgumentException("The classification isn't a predefined one");
    }
    PdcClassification savedClassification = NONE_CLASSIFICATION;
    if (classification.getId() != null && classificationRepository.contains(classification)
        && classification.isEmpty()) {
      classificationRepository.delete(classification);
    } else {
      for (PdcPosition aPosition : classification.getPositions()) {
        for (PdcAxisValue aValue : aPosition.getValues()) {
          if (!valueRepository.contains(aValue)) {
            valueRepository.save(aValue);
          }
        }
      }
      savedClassification = classificationRepository.saveAndFlush(classification);
    }
    return savedClassification;
  }

  /**
   * Deletes the predefined classification set for the specified node in the specified component
   * instance. If the specified node is null, then the predefined classification set for the whole
   * component instance is deleted.
   *
   * @param nodeId the unique identifier of the node for which the predefined classification has to
   * be deleted.
   * @param instanceId the unique identifier of the component instance to which the node belongs.
   */
  @Override
  public void deletePreDefinedClassification(String nodeId, String instanceId) {
    PdcClassification classification;
    // the node with 0 as identifier matches the component instance itself
    if (!isDefined(nodeId) || "0".equals(nodeId)) {
      classification = getPreDefinedClassification(instanceId);
    } else {
      classification = getPreDefinedClassification(nodeId, instanceId);
    }
    if (classification != NONE_CLASSIFICATION) {
      classificationRepository.delete(classification);
    }
  }

  /**
   * Classifies the specified contribution on the PdC with the specified classification. If the
   * contribution is already classified, then the given classification replaces the existing one.
   * The contribution must exist in Silverpeas before being classified. If an error occurs while
   * classifying the contribution, a runtime exception PdcRuntimeException is thrown.
   * @param contribution the Silverpeas contribution to classify.
   * @param withClassification the classification with which the contribution is positioned on
   * the PdC.
   * @param alertSubscribers indicates if subscribers must be notified or not
   */
  @Override
  public void classifyContent(final Contribution contribution,
      final PdcClassification withClassification, boolean alertSubscribers) {
    List<ClassifyPosition> classifyPositions = withClassification.getClassifyPositions();
    try {
      final ContributionIdentifier contributionId = contribution.getContributionId();
      int silverObjectId = getOrCreateSilverpeasContentId(contribution);
      final String componentInstanceId = contributionId.getComponentInstanceId();
      List<ClassifyPosition> existingPositions =
          getPdcManager().getPositions(silverObjectId, componentInstanceId);
      for (ClassifyPosition aClassifyPosition : classifyPositions) {
        int positionId = getPdcManager()
            .addPosition(silverObjectId, aClassifyPosition, componentInstanceId, alertSubscribers);
        aClassifyPosition.setPositionId(positionId);
      }
      if (!existingPositions.isEmpty()) {
        for (ClassifyPosition anExistingPosition : existingPositions) {
          if (!isFound(anExistingPosition, classifyPositions)) {
            getPdcManager().deletePosition(anExistingPosition.getPositionId(), componentInstanceId);
          }
        }
      }
    } catch (PdcException ex) {
      throw new PdcRuntimeException(ex);
    }
  }

  /**
   * <p>
   * Gets the silverpeas content identifier (the identifier of the content handled by the PDC
   * indeed) from a contribution instance. If it does not exists it is created.
   * </p>
   * <p>
   * Old mechanism has not been refactored, so when the contribution is a {@link SilverpeasContent}
   * one, then the silverpeas content id is retrieved from
   * {@link SilverpeasContent#getSilverpeasContentId()}
   * implemented by the contribution.
   * </p>
   * <p>
   * Otherwise, the silverpeas content id is get or created by using the right implementation of
   * {@link ContentInterface}.<br>
   * If no implementation exists, and so that no silverpeas content id can be get or created, a
   * {@link NotSupportedException} is thrown because the process is in a case where no classification
   * should be used.
   * </p>
   * @param contribution a contribution.
   * @return the content identifier as integer.
   */
  private int getOrCreateSilverpeasContentId(final Contribution contribution) {
    if (contribution instanceof SilverpeasContent) {
      return Integer.valueOf(((SilverpeasContent) contribution).getSilverpeasContentId());
    }
    final ContributionIdentifier contributionId = contribution.getContributionId();

    Optional<ContentInterface> contentInterface =
        ContentInterface.getByInstanceId(contributionId.getComponentInstanceId());
    if (contentInterface.isPresent()) {
      return contentInterface.get().getOrCreateSilverContentId(contribution);
    }

    throw new NotSupportedException(MessageFormat
        .format("contribution {0} must implements WithPdcClassification to be taken in charge",
            contributionId.asString()));
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
   *
   * @param deletedValues the values that are removed from a PdC's axis.
   */
  @Override
  public void axisValuesDeleted(final List<PdcAxisValue> deletedValues) {
    List<PdcClassification> concernedClassifications = classificationRepository.
        findClassificationsByPdcAxisValues(deletedValues);
    for (PdcClassification aClassification : concernedClassifications) {
      aClassification.updateForPdcAxisValuesDeletion(deletedValues);
      savePreDefinedClassification(aClassification);
    }
    classificationRepository.flush(); // apply now all the modifications

    // for instance, the PdcAxisValue objects are taken in charge by this service as they are only
    // used in the predefined classification. Nevertheless, it is planned they will be used when
    // refactoring the PdC old code on the classification of contents and in the PdC definition.
    valueRepository.delete(deletedValues);
  }

  /**
   * An axis comes to be removed from the PdC. Triggers the update of all concerned classifications
   * taken in charge by this service (for instance, only the predefined classifications).
   *
   * The classifications are updated as following:
   * <ul>
   * <li>For each position the values related to the axis are removed.</li>
   * <li>If a position is empty, it is removed.<li>
   * <li>If a classification is empty, it is removed.<li>
   * </ul>
   *
   * @param axisId the unique identifier of the axis.
   */
  @Override
  public void axisDeleted(String axisId) {
    List<PdcAxisValue> valuesToDelete = valueRepository.findByAxisId(Long.valueOf(axisId));
    if (!valuesToDelete.isEmpty()) {
      axisValuesDeleted(valuesToDelete);
    }
  }

  protected NodeService getNodeService() {
    return nodeService;
  }

  protected PdcManager getPdcManager() {
    return PdcManager.get();
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
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    classificationRepository.deleteAllClassificationsByComponentInstanceId(componentInstanceId);
  }
}
