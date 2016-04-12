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
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.webapi.pdc.UserThesaurusHolder.forUser;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;

/**
 * A provider of services on the classification plan (named PdC). This class implements the adaptor
 * pattern by wrapping all the features about the PdC and provided by several business services so
 * that an unified and a unique access point is provided to the PdC web resources. The instances of
 * this class are managed by the IoC container and can be then injected as dependency into the PdC
 * web resources.
 */
@Named
public class PdcServiceProvider {

  @Inject
  private PdcManager pdcManager;
  @Inject
  private ThesaurusManager thesaurusManager;
  @Inject
  private ContentManager contentManager;
  @Inject
  private PdcClassificationService classificationService;
  @Inject
  private OrganizationController organisationController;

  /**
   * A convenient method to enhance the readability of method calls when a component identifier is
   * expected as argument.
   *
   * @param componentId the identifier of a Silverpeas component instance.
   * @return the identifier.
   */
  public static String inComponentOfId(String componentId) {
    return componentId;
  }

  /**
   * A convenient method to enhance the readability of method calls when a resource content
   * identifier is expected as argument.
   *
   * @param contentId the identifier of a Silverpeas resource content.
   * @return the identifier.
   */
  public static String forContentOfId(String contentId) {
    return contentId;
  }

  /**
   * Adds a new position of the specified resource content on the PdC configured for the specified
   * Silverpeas component instance. Once added, an identifier is set for the specified position.
   *
   * @param position the classification position to add.
   * @param contentId the identifier of the content for which a new position is created on the PdC.
   * @param componentId the identifier of the component instance that owns the PdC instance.
   * @throws ContentManagerException if no such content or component instance exists with the
   * specified identifier.
   * @throws PdcException if the position adding fails.
   */
  public void addPosition(final ClassifyPosition position, String contentId, String componentId)
      throws ContentManagerException, PdcException {
    int silverObjectId = getSilverObjectId(contentId, componentId);
    int positionId = getPdcManager().addPosition(silverObjectId, position, componentId);
    position.setPositionId(positionId);
  }

  /**
   * Updates the specified position of the specified resource content on the PdC configured for the
   * specified Silverpeas component instance. The position of the content on the PdC whose the
   * identifier is the one of the specified position is replaced by the passed position.
   *
   * @param position the classification position to update.
   * @param contentId the identifier of the content for which the position is to update on the PdC.
   * @param componentId the identifier of the component instance that owns the PdC instance.
   * @throws ContentManagerException if no such content or component instance exists with the
   * specified identifier.
   * @throws PdcException if the position update fails.
   */
  public void updatePosition(final ClassifyPosition position, String contentId, String componentId)
      throws ContentManagerException, PdcException {
    int silverObjectId = getSilverObjectId(contentId, componentId);
    getPdcManager().updatePosition(position, componentId, silverObjectId);
  }

  /**
   * Deletes the specified position of the specified resource content on the PdC configured for the
   * specified component instance.
   *
   * @param positionId the identifier of the position to delete.
   * @param componentId the identifier of the component that owns the PdC instance.
   * @throws PdcException if the position or the component identifier doesn't exist or if the
   * deletion fails.
   */
  public void deletePosition(int positionId, String contentId, String componentId) throws
      PdcException, ContentManagerException {
    List<UsedAxis> axis = getAxisUsedInPdcFor(componentId);
    List<ClassifyPosition> positions = getAllPositions(contentId, componentId);
    if (positions.size() == 1) {
      for (UsedAxis anAxis : axis) {
        if (anAxis.getMandatory() == 1) {
          throw new PdcPositionDeletionException(getClass().getSimpleName(),
              SilverpeasException.ERROR,
              "Pdc.CANNOT_DELETE_VALUE");
        }
      }
    }
    getPdcManager().deletePosition(positionId, componentId);
  }

  /**
   * Gets the positions of the specified resource content on the PdC of the specified component
   * instance.
   *
   * @param contentId the identifier of the content.
   * @param componentId the identifier of the Silverpeas component instance.
   * @return a list of classification positions of the specified content.
   * @throws ContentManagerException if no such content or component instance exists with the
   * specified identifier.
   * @throws PdcException if the position fetching fails.
   */
  public List<ClassifyPosition> getAllPositions(String contentId, String componentId) throws
      ContentManagerException, PdcException {
    int silverObjectId = getSilverObjectId(contentId, componentId);
    return getPdcManager().getPositions(silverObjectId, componentId);
  }

  /**
   * Finds the predefined PdC classification to use for classifying new contents in the specified
   * node of the specified component instance. If the node isn't set, then the predefined PdC
   * classification of the component instance is looked for.
   *
   * @param nodeId the unique identifier of the node. A node is a generic way in Silverpeas to
   * categorize contents in a Silverpeas component.
   * @param componentId the unique identifier of the component.
   * @return a default PdC classification to use to classify contents.
   */
  PdcClassification findPredefinedClassificationForContentsIn(String nodeId, String componentId) {
    return classificationService.findAPreDefinedClassification(nodeId, componentId);
  }

  /**
   * Gets the predefined PdC classification that is associated with the specified node of the
   * specified component instance. If the node isn't set, then the predefined PdC classification
   * associated with the component instance is looked for.
   *
   * @param nodeId the unique identifier of the node. A node is a generic way in Silverpeas to
   * categorize contents in a Silverpeas component.
   * @param componentId the unique identifier of the component.
   * @return a default PdC classification to use to classify contents.
   */
  PdcClassification getPredefinedClassification(String nodeId, String componentId) {
    return classificationService.getPreDefinedClassification(nodeId, componentId);
  }

  /**
   * Saves or updates the specified predefined classification. The node and the component instance
   * related by the specified classification is indicated by its respective properties. If the
   * classification is empty (that is to say all of its positions are deleted), then it is removed
   * from the persistence context. In this case, the predefined classification associated with the
   * closest parent node is taken as the one for the related node (if any). If there is no
   * predefined classification associated with a parent node or with the component instance, then
   * NONE_CLASSIFICATION is returned.
   *
   * @param predefinedClassification the predefined classification to save or to update.
   * @return the classification used for the related node or component instance or NONE_CLASSICATION
   * if there is no predefined classification with the component instance.
   */
  PdcClassification saveOrUpdatePredefinedClassification(
      final PdcClassification predefinedClassification) {
    PdcClassification savedClassification = classificationService.savePreDefinedClassification(
        predefinedClassification);
    if (savedClassification == NONE_CLASSIFICATION) {
      savedClassification = findPredefinedClassificationForContentsIn(predefinedClassification.
          getNodeId(), predefinedClassification.getComponentInstanceId());
    }
    return savedClassification;
  }

  /**
   * Gets the axis used in the PdC configured for the specified component instance in order to
   * classify the specified resource content. If the resource content is already classified, then
   * the positions of the resource content on the invariant axis are kept as the only possible value
   * on theses axis. In the case no axis are specifically used for the component instance, then all
   * the PdC axis are sent back as axis that can be used to classify the specified content.
   *
   * @param contentId the identifier of the content to classify (or to refine the classification).
   * It is used to find its previous classification in order to fix the value of the invariant axis.
   * @param inComponentId the identifier of the component instance.
   * @return a list of used axis.
   * @throws ContentManagerException if no such content or component instance exists with the
   * specified identifier.
   * @throws PdcException if the axis cannot be fetched.
   */
  public List<UsedAxis> getAxisUsedInPdcToClassify(String contentId, String inComponentId)
      throws ContentManagerException, PdcException {
    int silverObjectId = getSilverObjectId(contentId, inComponentId);
    return getPdcManager().getUsedAxisToClassify(inComponentId, silverObjectId);
  }

  /**
   * Gets the axis used in the PdC configured for the specified Silverpeas component instance.
   *
   * @param componentId the unique identifier of the component instance.
   * @return a list of axis used in the PdC configured for the component instance.
   * @throws PdcException if the axis cannot be fetched.
   */
  public List<UsedAxis> getAxisUsedInPdcFor(String componentId) throws PdcException {
    return getPdcManager().getUsedAxisToClassify(componentId, -1);
  }

  /**
   * Gets a holder of the thesaurus for the specified user.
   *
   * @param user the user for which a holder will hold the thesaurus.
   * @return a UserThesaurusHolder instance.
   */
  public UserThesaurusHolder getThesaurusOfUser(final UserDetail user) {
    return UserThesaurusHolder.holdThesaurus(forUser(user));
  }

  /**
   * Gets the axis and, for each of them, the values that are used in the classification of the
   * contents that match the specified criteria.
   *
   * @param criteria the criteria the classified contents have to satisfy. The expected criteria are
   * on the component instance or the workspace in which they were published, on some axis values
   * with which they were classified, and on the inclusion of the secondary axis.
   * @return the axis with the values used in a classification. The secondary axis are inserted at
   * the end of the list.
   * @throws PdcException if an error occurs while getting the PdC's axis that are used in a
   * classification.
   */
  public List<UsedAxis> getAxisUsedInClassificationsByCriteria(final PdcFilterCriteria criteria)
      throws PdcException {
    List<UsedAxis> usedAxis = new ArrayList<>();

    ComponentSearchCriteria searchCriteria = new ComponentSearchCriteria().
        onComponentInstance(criteria.getComponentInstanceId()).
        onWorkspace(criteria.getWorkspaceId()).
        onUser(criteria.getUser());
    SearchContext searchContext = setUpSearchContextFromCriteria(criteria);
    List<String> availableComponents = getOrganisationController().
        getSearchableComponentsByCriteria(searchCriteria);

    List<AxisHeader> allAxis = getPdcManager().getAxisByType(PdcManager.PRIMARY_AXIS);
    List<UsedAxis> filteredAxis = filterAxis(allAxis, searchContext, availableComponents);
    usedAxis.addAll(filteredAxis);

    if (criteria.hasSecondaryAxisToBeIncluded()) {
      allAxis = getPdcManager().getAxisByType(PdcManager.SECONDARY_AXIS);
      filteredAxis = filterAxis(allAxis, searchContext, availableComponents);
      usedAxis.addAll(filteredAxis);
    }
    return usedAxis;
  }

  /**
   * Gets all the axis of the PdC in Silverpeas.
   *
   * @return the axis of the PdC.
   * @throws PdcException if an error occurs while getting the PdC's axis.
   */
  public List<Axis> getAllAxis() throws PdcException {
    List<Axis> pdcAxis = new ArrayList<>();
    List<AxisHeader> headers = getPdcManager().getAxis();
    for (AxisHeader aHeader : headers) {
      String treeId = getPdcManager().getTreeId(aHeader.getPK().getId());
      List<Value> values = getPdcManager().getAxisValues(Integer.valueOf(treeId));
      pdcAxis.add(new Axis(aHeader, values));
    }
    return pdcAxis;
  }

  private PdcManager getPdcManager() {
    return this.pdcManager;
  }

  private ContentManager getContentManager() {
    return this.contentManager;
  }

  private ThesaurusManager getThesaurusManager() {
    return this.thesaurusManager;
  }

  public OrganizationController getOrganisationController() {
    return organisationController;
  }

  private int getSilverObjectId(String ofTheContent, String inTheComponent) throws
      ContentManagerException {
    return getContentManager().getSilverContentId(ofTheContent, inTheComponent);
  }

  private SearchContext setUpSearchContextFromCriteria(final PdcFilterCriteria criteria) {
    SearchContext context = new SearchContext(null);
    if (criteria.hasCriterionOnUser()) {
      context.setUserId(criteria.getUser().getId());
    }
    if (criteria.hasCriterionOnAxisValues()) {
      for (AxisValueCriterion axisValueCriterion : criteria.getAxisValues()) {
        context.addCriteria(axisValueCriterion);
      }
    }
    return context;
  }

  private UsedAxis createUsedAxis(AxisHeader axisHeader, List<Value> values) throws PdcException {
    UsedAxis axis = new UsedAxis(axisHeader.getPK().getId(), "", axisHeader.getRootId(), 0, 0, 1);
    axis._setAxisHeader(axisHeader);
    axis._setAxisName(axisHeader.getName());
    axis._setAxisType(axisHeader.getAxisType());
    axis._setBaseValueName(axisHeader.getName());
    axis._setAxisRootId(Integer.parseInt(
        getPdcManager().getRoot(axisHeader.getPK().getId()).getValuePK().getId()));
    axis._setAxisValues(values);
    return axis;
  }

  private List<UsedAxis> filterAxis(List<AxisHeader> axisHeaders,
      SearchContext searchContext, List<String> availableComponents) throws PdcException {
    List<UsedAxis> filteredAxis = new ArrayList<>();
    for (AxisHeader axisHeader : axisHeaders) {
      List<Value> values = getPdcManager().getPertinentDaughterValuesByInstanceIds(
          searchContext, axisHeader.getPK().getId(), "0", availableComponents);
      if (values != null && !values.isEmpty()) {
        UsedAxis usedAxis = createUsedAxis(axisHeader, values);
        filteredAxis.add(usedAxis);
      }
    }
    return filteredAxis;
  }
}
