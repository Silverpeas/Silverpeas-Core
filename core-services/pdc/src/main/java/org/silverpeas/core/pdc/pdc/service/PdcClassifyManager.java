/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.pdc.classification.ObjectValuePair;
import org.silverpeas.core.pdc.classification.PertinentAxis;
import org.silverpeas.core.pdc.classification.PertinentValue;
import org.silverpeas.core.pdc.classification.Position;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;

import java.sql.Connection;
import java.util.List;

interface PdcClassifyManager {

  /**
   * Check if the object is already classified on the position
   * @param silverObjectId
   * @param position
   * @return the positionId if the object is already classified, -1 otherwise.
   * @throws PdcException
   */
  int isPositionAlreadyExists(int silverObjectId,
      ClassifyPosition position) throws PdcException;

  /**
   * Add new position for an object
   * @param silverObjectId - id of the object to classify
   * @param position - the position of the object
   * @return 0 position is OK, insertion have been done. 1 if variant constraint not respected
   * @throws PdcException
   */
  int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId) throws PdcException;

  /**
   * Update the position of an object
   * @param position - the position of the object
   * @return 0 position is OK, insertion have been done. 1 if variant constraint not respected
   * @throws PdcException
   */
  int updatePosition(ClassifyPosition position) throws PdcException;

  int updatePositions(List<Value> classifyValues, int silverObjectId)
      throws PdcException;

  /**
   * Delete the position of an object
   * @param positionId - the id of the position
   * @throws PdcException
   */
  void deletePosition(int positionId, String sComponentId)
      throws PdcException;

  /**
   * Returns all positions of an object
   * @param silverObjectId - id of the object
   * @return a Position List
   * @throws PdcException
   */
  List<Position<Value>> getPositions(int silverObjectId, String sComponentId)
      throws PdcException;

  void registerAxis(Connection con, int axisId) throws PdcException;

  void unregisterAxis(Connection con, int axisId) throws PdcException;

  void createValuesAndReplace(Connection con, String axisId,
      List<String> oldPath, List<String> newPath) throws PdcException;

  List<Integer> findSilverContentIdByPosition(
      SearchContext containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) throws PdcException;

  boolean hasAlreadyPositions(List<Integer> objectIdList, UsedAxis usedAxis)
      throws PdcException;

  /** Returns a list of object for one instance */
  List<Integer> getObjectsByInstance(String instanceId) throws PdcException;

  // recherche à l'intérieur d'une instance
  List<PertinentAxis> getPertinentAxis(SearchContext searchContext, List<Integer> axisIds,
      List<String> instanceIds) throws PdcException;

  // recherche à l'intérieur d'une instance
  List<PertinentValue> getPertinentValues(SearchContext searchContext, int axisId,
      List<String> instanceIds) throws PdcException;

  /*
   * recherche tous les objets classés sur l'axe axisId selon le searchContext et le JoinStatement
   * @return une List de ObjectValuePair
   */
  List<ObjectValuePair> getObjectValuePairs(SearchContext searchContext, int axisId,
      List<String> instanceIds) throws PdcException;

}