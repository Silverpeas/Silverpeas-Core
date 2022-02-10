/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchAxis;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.util.ServiceProvider;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Manager of the PdC (Plan de Classement in French or Classification Plan in English). The PdC is a
 * multi-dimensional tree of coordinates. The tree defines an ontology in which each axis and
 * sub-axis are about a semantic topic (for example "Geography") that is refined down to the the
 * sub-axis and in which each coordinate along each axis is a semantic label (made of one or more
 * words, for example "Sri Lanka" in the sub-axis "Asia" of the axis "Geography") with or without a
 * mapped thesaurus. The PdC gives a way to the users to explicitly or automatically color their
 * contributions with some meanings and from those meanings to find the contributions related by
 * them. The PdC manager is the low-level object that is responsible to construct such a PdC in
 * Silverpeas, to filter the PdC's axis per application instance, and to position semantically the
 * contributions into the PdC.
 */
public interface PdcManager {

  /**
   * The primary axis that made a PdC.
   */
  String PRIMARY_AXIS = "P";
  /**
   * The secondary axis, often hidden or not used, that made a PdC and that provide more accurate
   * classification information.
   */
  String SECONDARY_AXIS = "S";

  static PdcManager get() {
    return ServiceProvider.getSingleton(PdcManager.class);
  }

  List<GlobalSilverContent> findGlobalSilverContents(SearchContext containerPosition,
      List<String> componentIds, boolean recursiveSearch, boolean visibilitySensitive);

  List<AxisHeader> getAxisByType(String type) throws PdcException;

  /**
   * Method declaration
   * @return
   * @throws PdcException
   */
  List<AxisHeader> getAxis() throws PdcException;

  /**
   * Method declaration
   *
   * @return
   * @throws PdcException
   *
   */
  int getNbAxis() throws PdcException;

  int getNbMaxAxis() throws PdcException;

  /**
   * Method declaration
   *
   * @param axisHeader
   * @return
   * @throws PdcException
   *
   */
  int createAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisHeader
   * @return
   * @throws PdcException
   *
   */
  int updateAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @throws PdcException
   *
   */
  void deleteAxis(Connection con, String axisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @return
   * @throws PdcException
   *
   */
  Axis getAxisDetail(String axisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param axisId
   * @return
   * @throws PdcException
   *
   */
  AxisHeader getAxisHeader(String axisId) throws PdcException;

  Value getValue(String axisId, String valueId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @return
   * @throws PdcException
   *
   */
  Value getAxisValue(String valueId, String treeId) throws PdcException;

  /**
   * Return a list of axis values having the value name in parameter
   *
   * @param valueName
   * @return List
   * @throws PdcException
   *
   */
  List<Value> getAxisValuesByName(String valueName) throws PdcException;

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   * @param axisId
   * @param valueId
   * @return List
   * @throws PdcException
   */
  List<String> getDaughterValues(String axisId, String valueId) throws PdcException;

  /**
   * Return the Value corresponding to the axis done
   *
   * @param axisId
   * @return Value
   * @throws PdcException
   *
   */
  Value getRoot(String axisId) throws PdcException;

  /**
   * @param treeId The id of the selected axis.
   * @return The list of values of the axis.
   */
  List<Value> getAxisValues(int treeId) throws PdcException;

  /**
   * Method declaration
   * @param valueToInsert
   * @param refValue
   * @param axisId
   * @return
   * @throws PdcException
   */
  int insertMotherValue(Value valueToInsert, String refValue, String axisId) throws PdcException;

  /**
   * Déplace une valeur et ses sous-valeurs sous un nouveau père
   *
   * @param axis
   * @param valueToMove
   * @param newFatherId
   * @return 1 si valeur soeur de même nom
   * @throws PdcException
   */
  int moveValueToNewFatherId(Axis axis, Value valueToMove, String newFatherId, int orderNumber)
      throws PdcException;

  /**
   * retourne les droits sur la valeur
   *
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  List<List<String>> getManagers(String axisId, String valueId) throws PdcException;

  boolean isUserManager(String userId) throws PdcException;

  /**
   * retourne les droits hérités sur la valeur
   *
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  List<List<String>> getInheritedManagers(Value value) throws PdcException;

  /**
   * met à jour les droits sur la valeur
   *
   * @return
   * @throws PdcException
   */
  void setManagers(List<String> userIds, List<String> groupIds, String axisId, String valueId)
      throws PdcException;

  /**
   * supprime tous les droits sur la valeur
   *
   * @return
   * @throws PdcException
   */
  void razManagers(String axisId, String valueId) throws PdcException;

  void deleteManager(String userId) throws PdcException;

  void deleteGroupManager(String groupId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueToInsert
   * @param refValue
   * @return status
   * @throws PdcException
   *
   */
  int createDaughterValue(Value valueToInsert, String refValue, String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueToInsert
   * @param refValue
   * @return daughterid
   * @throws PdcException
   *
   */
  String createDaughterValueWithId(Value valueToInsert, String refValue, String treeId)
      throws PdcException;

  /**
   * Method declaration
   *
   * @param value
   * @return
   * @throws PdcException
   *
   */
  int updateValue(Value value, String treeId) throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @throws PdcException
   *
   */
  void deleteValueAndSubtree(Connection con, String valueId, String axisId, String treeId)
      throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @throws PdcException
   *
   */
  String deleteValue(Connection con, String valueId, String axisId, String treeId)
      throws PdcException;

  /**
   * Method declaration
   *
   * @param valueId
   * @return
   * @throws PdcException
   *
   */
  List<Value> getFullPath(String valueId, String treeId) throws PdcException;

  String getTreeId(String axisId) throws PdcException;

  /**
   * ****************************************************************
   */
  /* Methods used by the use case 'settings of using of the taxinomy' */

  /**
   * ****************************************************************
   */
  UsedAxis getUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param instanceId
   * @return
   * @throws PdcException
   *
   */
  List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxis
   * @return
   * @throws PdcException
   *
   */
  int addUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxis
   * @return
   * @throws PdcException
   *
   */
  int updateUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxisId
   * @throws PdcException
   *
   */
  void deleteUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   *
   * @param usedAxisIds
   * @throws PdcException
   *
   */
  void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException;

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
  List<UsedAxis> getUsedAxisToClassify(String instanceId, int silverObjectId) throws PdcException;

  int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId)
      throws PdcException;

  int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId,
      boolean alertSubscribers) throws PdcException;

  int updatePosition(ClassifyPosition position, String instanceId, int silverObjectId)
      throws PdcException;

  int updatePosition(ClassifyPosition position, String instanceId, int silverObjectId,
      boolean alertSubscribers) throws PdcException;

  void deletePosition(int positionId, String sComponentId) throws PdcException;

  void addPositions(List<ClassifyPosition> positions, int objectId, String instanceId)
      throws PdcException;

  void copyPositions(int fromObjectId, String fromInstanceId, int toObjectId, String toInstanceId)
      throws PdcException;

  List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId) throws PdcException;

  boolean isClassifyingMandatory(String componentId) throws PdcException;

  List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext, String axisType,
      String instanceId) throws PdcException;

  List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext, String axisType,
      List<String> instanceIds) throws PdcException;

  List<Value> getFirstLevelAxisValuesByInstanceId(SearchContext searchContext, String axisId,
      String instanceId) throws PdcException;

  List<Value> getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext, String axisId,
      List<String> instanceIds) throws PdcException;

  List<Value> getPertinentDaughterValuesByInstanceId(SearchContext searchContext, String axisId,
      String valueId, String instanceId) throws PdcException;

  List<Value> getPertinentDaughterValuesByInstanceIds(SearchContext searchContext, String axisId,
      String valueId, List<String> instanceIds) throws PdcException;

  List<Integer> findSilverContentIdByPosition(SearchContext containerPosition,
      List<String> alComponentId, String authorId, LocalDate afterDate, LocalDate beforeDate)
      throws PdcException;

  /**
   * Find all the SilverContentId with the given position
   */
  List<Integer> findSilverContentIdByPosition(SearchContext containerPosition,
      List<String> alComponentId) throws PdcException;

  List<Value> getDaughters(String refValue, String treeId);

  void indexAllAxis() throws PdcException;

  SearchContext getSilverContentIdSearchContext(int nSilverContentId, String sComponentId)
      throws PdcException;

  List<GlobalSilverContent> getSilverContentsByIds(List<Integer> silverContentIds, String userId);
}