/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcRuntimeException;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * The service aiming at classifying the contents in Silverpeas on the classification plan (named
 * PdC). The classification of a content in Silverpeas consists to position it on the PdC; it is the
 * process to attribute some semantic metadata to the content. The metadata then can be used to find
 * any contents that satisfies a search of information by keywords. As some contents can not to be
 * positioned on the PdC (it is not mandatory), they can be not detected in the search. The
 * classification of a content on the PdC can be done in one of the two ways:
 * <ul>
 * <li>the contributor can position explicitly the content on the PdC,</li>
 * <li>a predefined classification is used either to classify automatically the content or as a
 * template to classify the content.</li>
 * </ul>
 * A predefined classification on the PdC can be created for a given node in a Silverpeas component
 * instance or for the whole component instance. The predefined classification is a way to
 * facilitate or to impose the classification on the PdC of the contents when they are published. A
 * node in Silverpeas is a way to hierarchically categorize a content. A node can represent for
 * example a topic. A node is part of a hierarchic tree and it can then contain both some contents
 * and some nodes. So, a predefined classification on the PdC associated with a node is set for all
 * the contents in its children nodes. Therefore, if a predefined classification is not found for a
 * given node, then it is seeked back upto the root node (that is the component instance ifself).
 */
public interface PdcClassificationService {

  static PdcClassificationService get() {
    return ServiceProvider.getService(PdcClassificationService.class);
  }

  /**
   * Finds a predefined classification on the PdC that was set for any new contents in the specified
   * node of the specified component instance. If the specified node isn't defined, then the
   * predefined classification associated with the whole component instance is seeked. If no
   * predefined classification is found for the specified node, then it is seeked back upto the root
   * node (that is the component instance ifself). In the case no predefined classification is set
   * for the whole component instance, an empty classification is then returned. To get the
   * predefined classification that is set exactly for the specified node (if any), then use the
   * <code>getPreDefinedClassification(java.lang.String, java.lang.String</code> method.
   * @param nodeId the unique identifier of the node.
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content published
   * in the specified node or an empty classification.
   */
  public PdcClassification findAPreDefinedClassification(String nodeId, String instanceId);

  /**
   * Gets the predefined classification on the PdC that was set for any new contents in the
   * specified node of the specified component instance. If the specified node isn't defined, then
   * the predefined classification associated with the whole component instance is get. In the case
   * no predefined classification is set for the specified node or for the component instance, then
   * a none classification is then returned.
   * @param nodeId the unique node identifier.
   * @param instanceId the unique component instance identifier.
   * @return a predefined classification on the PdC associated with the specified node or with the
   * specified component instance or an empty classification.
   */
  public PdcClassification getPreDefinedClassification(String nodeId, String instanceId);

  /**
   * Gets the predefined classification on the PdC that was set for any new contents managed in the
   * specified component instance. This method is for the component instances that don't support the
   * categorization. In the case no predefined classification is set for the whole component
   * instance, a none classification is then returned.
   * @param instanceId the unique identifier of the Silverpeas component instance.
   * @return a predefined classification on the PdC ready to be used to classify a content published
   * within the component instance or an empty classification.
   */
  public PdcClassification getPreDefinedClassification(String instanceId);

  /**
   * Saves the specified predefined classification on the PdC. If a predefined classification
   * already exists for the node (if any) and the component instance to which this classification is
   * related, then it is replaced by the specified one. If the specified classification is empty
   * (all positions were deleted), then it is deleted and the NONE_CLASSIFICATION is sent back. The
   * node (if any) and the component instance for which this classification has to be saved are
   * indicated by the specified classification itself. If no node is refered by it, then the
   * predefined classification will serv for the whole component instance.
   * @param classification either the saved predefined classification or NONE_CLASSIFICATION.
   */
  public PdcClassification savePreDefinedClassification(final PdcClassification classification);

  /**
   * Deletes the predefined classification set for the specified node in the specified component
   * instance. If the specified node is null, then the predefined classification set for the whole
   * component instance is deleted.
   * @param nodeId the unique identifier of the node for which the predefined classification has to
   * be deleted.
   * @param instanceId the unique identifier of the component instance to which the node belongs.
   */
  public void deletePreDefinedClassification(String nodeId, String instanceId);

  /**
   * Classifies the specified content on the PdC with the specified classification. If the content
   * is already classified, then the given classification replaces the existing one. The content
   * must exist in Silverpeas before being classified. If an error occurs while classifying the
   * content, a runtime exception PdcRuntimeException is thrown.
   * Subscribers are notified if at least one of their subscription matches given classification.
   * @param content the Silverpeas content to classify.
   * @param withClassification the classification with which the content is positioned on the PdC.
   */
  public void classifyContent(final SilverpeasContent content,
          final PdcClassification withClassification) throws PdcRuntimeException;

  /**
   * Classifies the specified content on the PdC with the specified classification. If the content
   * is already classified, then the given classification replaces the existing one. The content
   * must exist in Silverpeas before being classified. If an error occurs while classifying the
   * content, a runtime exception PdcRuntimeException is thrown.
   * @param content the Silverpeas content to classify.
   * @param withClassification the classification with which the content is positioned on the PdC.
   * @param alertSubscribers indicates if subscribers must be notified or not
   */
  public void classifyContent(final SilverpeasContent content,
          final PdcClassification withClassification, boolean alertSubscribers) throws PdcRuntimeException;

  /**
   * Some values come to be removed from the PdC. Triggers the update of all concerned
   * classifications taken in charge by this service (for instance, only the predefined
   * classifications). For each value, according to its level in the hierarchical tree representing
   * the PdC's axis, the correct update behaviour is selected for a given classification:
   * <ul>
   * <li>The value is a root one of the axis: the value is removed from any positions of the
   * classification. If the position is empty (it has no values) it is then removed. If the
   * classification is then empty, it is removed.</li>
   * <li>The value is a leaf in a branch: the value is replaced by its mother value in any positions
   * of the classification.</li>
   * </ul>
   * @param deletedValues the values that are removed from a PdC's axis.
   */
  public void axisValuesDeleted(final List<PdcAxisValue> deletedValues);

  /**
   * An axis comes to be removed from the PdC. Triggers the update of all concerned classifications
   * taken in charge by this service (for instance, only the predefined classifications). The
   * classifications are updated as following:
   * <ul>
   * <li>For each position the values related to the axis are removed.</li>
   * <li>If a position is empty, it is removed.
   * <li>
   * <li>If a classification is empty, it is removed.
   * <li>
   * </ul>
   * @param axisId the unique identifier of the axis.
   */
  public void axisDeleted(String axisId);
}