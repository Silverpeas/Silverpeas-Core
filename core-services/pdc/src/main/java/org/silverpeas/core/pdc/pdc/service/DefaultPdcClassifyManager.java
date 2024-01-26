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
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.pdc.classification.ClassifyEngineException;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.classification.ObjectValuePair;
import org.silverpeas.core.pdc.classification.PertinentAxis;
import org.silverpeas.core.pdc.classification.PertinentValue;
import org.silverpeas.core.pdc.classification.Position;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.JoinStatement;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DefaultPdcClassifyManager implements PdcClassifyManager, ComponentInstanceDeletion {

  @Inject
  private ClassifyEngine classifyEngine;
  @Inject
  private ContentManagementEngine contentMgtEngine;

  protected DefaultPdcClassifyManager() {
  }

  @Override
  public int isPositionAlreadyExists(int silverObjectId, ClassifyPosition position) throws
      PdcException {
    try {
      return classifyEngine.isPositionAlreadyExists(silverObjectId, position);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId) throws
      PdcException {
    try (final Connection connection = DBUtil.openConnection()) {
      // Vérification de la contrainte invariante
      classifyEngine.classifySilverObject(connection, silverObjectId, position);
      return 0;
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public int updatePosition(ClassifyPosition position) throws PdcException {
    try {
      classifyEngine.updateSilverObjectPosition(position);
      return 0;
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public int updatePositions(List<Value> classifyValues, int silverObjectId) throws PdcException {
    try {
      classifyEngine.updateSilverObjectPositions(classifyValues, silverObjectId);
      return 0;
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public void deletePosition(int nPositionId, String sComponentId) throws PdcException {
    try (final Connection connection = DBUtil.openConnection()) {
      classifyEngine.unclassifySilverObjectByPositionId(connection, nPositionId);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public List<Position<Value>> getPositions(int silverObjectId, String sComponentId) throws PdcException {
    try {
      // Get all the positions for the given silverObjectId
      return classifyEngine.findPositionsBySilverOjectId(silverObjectId);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public void registerAxis(Connection con, int axisId) throws PdcException {
    try {
      classifyEngine.registerAxis(con, axisId);
    } catch (ClassifyEngineException e) {
      throw new PdcException(e);
    }
  }

  @Override
  public void unregisterAxis(Connection con, int axisId) throws PdcException {
    try {
      classifyEngine.unregisterAxis(con, axisId);
    } catch (ClassifyEngineException e) {
      throw new PdcException(e);
    }
  }

  /**
   * Create two lists of Value. Then, we replace the first Value list by the second
   *
   * @param con - a connection to the database
   * @param axisId - the id of the axis
   * @param oldPath - a list of path
   * @param newPath - a list of path
   */
  @Override
  public void createValuesAndReplace(Connection con, String axisId,
      List<String> oldPath, List<String> newPath) throws PdcException {
    Objects.requireNonNull(con);
    final List<Value> oldValues = new ArrayList<>();
    final List<Value> newValues = new ArrayList<>();
    // set the axisId of Value Objects
    int id = Integer.parseInt(axisId);
    // build old values and new values object
    for (int i = 0; i < oldPath.size(); i++) {
      final Value oldValue = new Value();
      final Value newValue = new Value();
      // get oldpath
      String path = oldPath.get(i);
      oldValue.setAxisId(id);
      oldValue.setValue(path);
      // get newPath
      path = newPath.get(i);
      newValue.setAxisId(id);
      newValue.setValue(path);

      // add the new values into the arrayList
      oldValues.add(oldValue);
      newValues.add(newValue);
    }
    try {
      Position<Value> position = new Position<>(newValues);
      if (classifyEngine.isPositionAlreadyExists(id, position) == -1) {
        classifyEngine.replaceValuesOnAxis(con, oldValues, newValues);
      }
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public List<Integer> getObjectsByInstance(String instanceId) throws PdcException {
    final List<Integer> objectIdList;
    try {
      JoinStatement contentJoin = contentMgtEngine.getPositionsByGenericSearch(null, null, null);
      List<Criteria> criterias = new ArrayList<>();
      List<String> instanceIds = new ArrayList<>();
      instanceIds.add(instanceId);
      objectIdList = classifyEngine
          .findSilverOjectByCriterias(criterias, instanceIds, contentJoin, null, null, true, false);
    } catch (ClassifyEngineException e) {
      throw new PdcException(e);
    }
    return objectIdList;
  }

  /**
   * search a defined position for one usedAxis
   *
   * @param objectIdList
   * @param usedAxis - the UsedAxis object
   * @return true if for one UsedAxis, a position exists, false otherwise
   * @throws PdcException
   */
  @Override
  public boolean hasAlreadyPositions(List<Integer> objectIdList, UsedAxis usedAxis)
      throws PdcException {
    final String newBaseValue = "/" + usedAxis.getBaseValue() + "/";
    final String instanceId = usedAxis.getInstanceId();

    boolean hasOnePosition = false;
    for (int i = 0; i < objectIdList.size() && !hasOnePosition; i++) {
      if (objectIdList.get(i) != -1) {
        final List<Position<Value>> positions = getPositions(objectIdList.get(i), instanceId);
        for (int j = 0; j < positions.size() && !hasOnePosition; j++) {
          final Value value = positions.get(j).getValueByAxis(usedAxis.getAxisId());
          final String onePath = value.getValue();
          if (onePath != null && onePath.contains(newBaseValue)) {
            hasOnePosition = true;
          }
        }
      }
    }

    return hasOnePosition;
  }

  @Override
  public List<PertinentAxis> getPertinentAxis(SearchContext searchContext, List<Integer> axisIds,
      List<String> instanceIds) throws PdcException {
    try {
      return classifyEngine
          .getPertinentAxisByJoin(searchContext.getCriterias(), axisIds, instanceIds);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public List<PertinentValue> getPertinentValues(SearchContext searchContext, int axisId,
      List<String> instanceIds) throws PdcException {
    try {
      return classifyEngine.getPertinentValuesByJoin(searchContext.getCriterias(), axisId,
          instanceIds);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public List<ObjectValuePair> getObjectValuePairs(SearchContext searchContext, int axisId,
      List<String> instanceIds) throws PdcException {
    try {
      return classifyEngine.getObjectValuePairsByJoin(searchContext.getCriterias(), axisId,
          instanceIds);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Find all the SilverContentId with the given position.
   *
   * @param searchContext
   * @param alComponentId
   * @param authorId
   * @param afterDate
   * @param beforeDate
   * @param recursiveSearch
   * @param visibilitySensitive
   * @return
   * @throws PdcException
   */
  @Override
  public List<Integer> findSilverContentIdByPosition(SearchContext searchContext,
      List<String> alComponentId, String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) throws PdcException {
    try {
      // Change the position in criteria
      List<SearchCriteria> alCriterias = searchContext.getCriterias();
      return classifyEngine.findSilverOjectByCriterias(alCriterias,
          alComponentId, contentMgtEngine.getPositionsByGenericSearch(
          authorId, afterDate, beforeDate), afterDate, beforeDate,
          recursiveSearch, visibilitySensitive);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      ContentManagementEngine engine = ContentManagementEngineProvider.getContentManagementEngine();
      List<Integer> contentIds = engine.getSilverContentIdByInstanceId(componentInstanceId);
      for (Integer contentId : contentIds) {
        classifyEngine.unclassifySilverObject(connection, contentId);
      }
    } catch (ContentManagerException | SQLException e) {
      throw new SilverpeasRuntimeException(e);
    } catch (ClassifyEngineException e) {
      SilverLogger.getLogger(this)
          .warn("[Deletion of {0}] {1}", componentInstanceId, e.getMessage());
    }
  }
}