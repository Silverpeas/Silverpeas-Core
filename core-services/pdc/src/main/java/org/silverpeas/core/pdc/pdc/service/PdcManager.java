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
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.contribution.contentcontainer.container.ContainerManagerException;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPositionInterface;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchAxis;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.index.search.model.AxisFilter;
import org.silverpeas.core.util.ServiceProvider;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

public interface PdcManager {

  /**
   * The primary axis that made a PdC.
   */
  public static final String PRIMARY_AXIS = "P";
  /**
   * The secondary axis, often hiden or not used, that made a PdC and that provide more accurate
   * classification information.
   */
  public static final String SECONDARY_AXIS = "S";

  public static PdcManager get() {
    return ServiceProvider.getService(PdcManager.class);
  }

  public List<GlobalSilverContent> findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List<String> componentIds,
      boolean recursiveSearch, boolean visibilitySensitive);

  public List<AxisHeader> getAxisByType(String type) throws PdcException;

  /**
   * Method declaration
   *
   * @return
   * @throws PdcException
   * @see
   */
  public List<AxisHeader> getAxis() throws PdcException;

  /**
   * Method declaration
   *
   * @return
   * @throws PdcException
   * @see
   */
  public int getNbAxis() throws PdcException;

  public int getNbMaxAxis() throws PdcException;

  /**
   * Method declaration
   *
   * @param axisHeader
   * @return
   * @throws PdcException
   * @see
   */
  public int createAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisHeader
   * @return
   * @throws PdcException
   * @see
   */
  public int updateAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @throws PdcException
   * @see
   */
  public void deleteAxis(Connection con, String axisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @return
   * @throws PdcException
   * @see
   */
  public Axis getAxisDetail(String axisId) throws PdcException;

  public Axis getAxisDetail(String axisId, AxisFilter filter)
      throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @return
   * @throws PdcException
   * @see
   */
  public AxisHeader getAxisHeader(String axisId) throws PdcException;

  public Value getValue(String axisId, String valueId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @return
   * @throws PdcException
   * @see
   */
  public Value getAxisValue(String valueId, String treeId) throws PdcException;

  /**
   * Return a list of axis values having the value name in parameter
   *
   * @param valueName
   * @return List
   * @throws PdcException
   * @see
   */
  public List<Value> getAxisValuesByName(String valueName) throws PdcException;

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   *
   * @param axisId
   * @param valueId
   * @return List
   * @throws PdcException
   * @see
   */
  public List<String> getDaughterValues(String axisId, String valueId)
      throws PdcException;

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   *
   * @return List
   * @throws PdcException
   * @see
   */
  public List<Value> getFilteredAxisValues(String rootId, AxisFilter filter)
      throws PdcException;

  /**
   * Return the Value corresponding to the axis done
   *
   * @param axisId
   * @return Value
   * @throws PdcException
   * @see
   */
  public Value getRoot(String axisId) throws PdcException;

  /**
   * @param treeId The id of the selected axis.
   * @return The list of values of the axis.
   */
  public List<Value> getAxisValues(int treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueToInsert
   * @param refValue
   * @param axisId
   * @return
   * @throws PdcException
   * @see
   */
  public int insertMotherValue(Value valueToInsert, String refValue,
      String axisId) throws PdcException;

  /**
   * Déplace une valeur et ses sous-valeurs sous un nouveau père
   *
   * @param axis
   * @param valueToMove
   * @param newFatherId
   * @return 1 si valeur soeur de même nom
   * @throws PdcException
   */
  public int moveValueToNewFatherId(Axis axis, Value valueToMove,
      String newFatherId, int orderNumber) throws PdcException;

  /**
   * retourne les droits sur la valeur
   *
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  public List<List<String>> getManagers(String axisId, String valueId) throws PdcException;

  public boolean isUserManager(String userId) throws PdcException;

  /**
   * retourne les droits hérités sur la valeur
   *
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  public List<List<String>> getInheritedManagers(Value value) throws PdcException;

  /**
   * met à jour les droits sur la valeur
   *
   * @return
   * @throws PdcException
   */
  public void setManagers(List<String> userIds, List<String> groupIds, String axisId,
      String valueId) throws PdcException;

  /**
   * supprime tous les droits sur la valeur
   *
   * @return
   * @throws PdcException
   */
  public void razManagers(String axisId, String valueId) throws PdcException;

  public void deleteManager(String userId) throws PdcException;

  public void deleteGroupManager(String groupId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueToInsert
   * @param refValue
   * @return status
   * @throws PdcException
   * @see
   */
  public int createDaughterValue(Value valueToInsert, String refValue,
      String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueToInsert
   * @param refValue
   * @return daughterid
   * @throws PdcException
   * @see
   */
  public String createDaughterValueWithId(Value valueToInsert, String refValue,
      String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param value
   * @return
   * @throws PdcException
   * @see
   */
  public int updateValue(Value value, String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @throws PdcException
   * @see
   */
  public void deleteValueAndSubtree(Connection con, String valueId,
      String axisId, String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @throws PdcException
   * @see
   */
  public String deleteValue(Connection con, String valueId, String axisId,
      String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @return
   * @throws PdcException
   * @see
   */
  public List<Value> getFullPath(String valueId, String treeId) throws PdcException;

  public String getTreeId(String axisId) throws PdcException;

  /**
   * ****************************************************************
   */
  /* Methods used by the use case 'settings of using of the taxinomy' */
  /**
   * ****************************************************************
   */
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param instanceId
   * @return
   * @throws PdcException
   * @see
   */
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxis
   * @return
   * @throws PdcException
   * @see
   */
  public int addUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxis
   * @return
   * @throws PdcException
   * @see
   */
  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxisId
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxisIds
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException;

  /**
   * Gets the axis used by the specified component instance to classify the specified Silverpeas
   * object. If there is no axis configured to be used in the component instance, then all PdC axis
   * are returned as axis that can be used for classifying a content. If the content is already
   * classified, then the values on invariant axis are used as invariant values.
   *
   * @param instanceId the unique identifier of the component instance.
   * @param silverObjectId the Silverpeas object identifier representing the content to classify.
   * @return a list of axis to use in the classification of a content.
   * @throws PdcException if an error occurs while getting the PdC axis for the specified component
   * instance.
   */
  public List<UsedAxis> getUsedAxisToClassify(String instanceId, int silverObjectId)
      throws PdcException;

  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId) throws PdcException;

  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId, boolean alertSubscribers) throws PdcException;

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId) throws PdcException;

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId, boolean alertSubscribers) throws PdcException;

  public void deletePosition(int positionId, String sComponentId)
      throws PdcException;

  public void addPositions(List<ClassifyPosition> positions, int objectId, String instanceId)
      throws PdcException;

  public void copyPositions(int fromObjectId, String fromInstanceId,
      int toObjectId, String toInstanceId) throws PdcException;

  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId)
      throws PdcException;

  public boolean isClassifyingMandatory(String componentId) throws PdcException;

  /**
   * Search methods
   */
  public List<SearchAxis> getPertinentAxis(SearchContext searchContext, String axisType)
      throws PdcException;

  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId) throws PdcException;

  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId, AxisFilter filter)
      throws PdcException;

  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext,
      String axisType, List<String> instanceIds) throws PdcException;

  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext,
      String axisType, List<String> instanceIds, AxisFilter filter) throws PdcException;

  // public List getFirstLevelAxisValues(SearchContext searchContext, String
  // axisId) throws PdcException;
  public List<Value> getFirstLevelAxisValuesByInstanceId(SearchContext searchContext,
      String axisId, String instanceId) throws PdcException;

  public List<Value> getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext,
      String axisId, List<String> instanceIds) throws PdcException;

  // recherche globale
  // public List getPertinentDaughterValues(SearchContext searchContext, String
  // axisId, String valueId) throws PdcException;
  // recherche à l'intérieur d'une instance
  public List<Value> getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId) throws PdcException;

  public List<Value> getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId, AxisFilter filter) throws PdcException;

  public List<Value> getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List<String> instanceIds) throws PdcException;

  public List<Value> getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List<String> instanceIds, AxisFilter filter) throws PdcException;

  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate)
      throws ContainerManagerException;

  /**
   * Find all the SilverContentId with the given position
   */
  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException;

  /**
   * Find all the SilverContentId with the given position
   */
  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId)
      throws ContainerManagerException;

  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException;

  public List<Value> getDaughters(String refValue, String treeId);

  public List<Value> getSubAxisValues(String axisId, String valueId);

  public void indexAllAxis() throws PdcException;
}
