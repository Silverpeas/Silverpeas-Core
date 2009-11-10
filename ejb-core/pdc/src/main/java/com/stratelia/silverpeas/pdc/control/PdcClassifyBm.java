/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.pdc.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.PertinentAxis;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.util.JoinStatement;

public interface PdcClassifyBm {

  /**
   * Check if the object is already classified on the position
   * 
   * @param silverObjectId
   * @param position
   * @return the positionId if the object is already classified, -1 otherwise.
   * @throws PdcException
   */
  public int isPositionAlreadyExists(int silverObjectId,
      ClassifyPosition position) throws PdcException;

  /**
   * Add new position for an object
   * 
   * @param silverObjectId
   *          - id of the object to classify
   * @param position
   *          - the position of the object
   * 
   * @return 0 position is OK, insertion have been done. 1 if variant constraint
   *         not respected
   * 
   * @throws PdcException
   * 
   */
  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId) throws PdcException;

  /**
   * Update the position of an object
   * 
   * @param silverObjectId
   *          - id of the object
   * @param position
   *          - the position of the object
   * 
   * @return 0 position is OK, insertion have been done. 1 if variant constraint
   *         not respected
   * 
   * @throws PdcException
   * 
   */
  public int updatePosition(ClassifyPosition position) throws PdcException;

  public int updatePositions(List classifyValues, int silverObjectId)
      throws PdcException;

  /**
   * Delete the position of an object
   * 
   * @param silverObjectId
   *          - id of the object
   * @param position
   *          - the id of the position
   * 
   * 
   * @throws PdcException
   * 
   */
  public void deletePosition(int positionId, String sComponentId)
      throws PdcException;

  /**
   * Returns all positions of an object
   * 
   * @param silverObjectId
   *          - id of the object
   * 
   * @return a Position List
   * 
   * @throws PdcException
   * 
   */
  public List getPositions(int silverObjectId, String sComponentId)
      throws PdcException;

  public JoinStatement getPositionsJoinStatement(String sComponentId)
      throws PdcException;

  public JoinStatement getPositionsJoinStatement(List alComponentId)
      throws PdcException;

  public void registerAxis(Connection con, int axisId) throws PdcException;

  public void unregisterAxis(Connection con, int axisId) throws PdcException;

  public void createValuesAndReplace(Connection con, String axisId,
      ArrayList oldPath, ArrayList newPath) throws PdcException;

  /** Remove all the positions of the given content */
  public List removePosition(Connection connection, int nSilverContentId)
      throws PdcException;

  /** Find all the SilverContentId with the given position */
  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      String authorId, String afterDate, String beforeDate) throws PdcException;

  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) throws PdcException;

  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId)
      throws PdcException;

  public boolean hasAlreadyPositions(List objectIdList, UsedAxis usedAxis)
      throws PdcException;

  /** Returns a list of object for one instance */
  public List getObjectsByInstance(String instanceId) throws PdcException;

  // Recherche globale
  public List getPertinentAxis(SearchContext searchContext, List axisIds)
      throws PdcException;

  // Recherche globale
  public List getPertinentValues(SearchContext searchContext, int axisId)
      throws PdcException;

  // recherche à l'intérieur d'une instance
  public List getPertinentAxis(SearchContext searchContext, List axisIds,
      JoinStatement joinStatementAllPositions) throws PdcException;

  // recherche à l'intérieur d'une instance
  public PertinentAxis getPertinentAxis(SearchContext searchContext,
      String axisId, String sRootValue, JoinStatement joinStatementAllPositions)
      throws PdcException;

  // recherche à l'intérieur d'une instance
  public List getPertinentValues(SearchContext searchContext, int axisId,
      JoinStatement joinStatementAllPositions) throws PdcException;

  /*
   * recherche tous les objets classés sur l'axe axisId selon le searchContext
   * et le JoinStatement
   * 
   * @return une List de ObjectValuePair
   */
  public List getObjectValuePairs(SearchContext searchContext, int axisId,
      JoinStatement joinStatementAllPositions) throws PdcException;

}