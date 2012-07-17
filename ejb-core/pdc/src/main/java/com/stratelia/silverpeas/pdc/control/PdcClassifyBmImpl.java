/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.pdc.control;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngineException;
import com.stratelia.silverpeas.classifyEngine.ObjectValuePair;
import com.stratelia.silverpeas.classifyEngine.PertinentAxis;
import com.stratelia.silverpeas.classifyEngine.PertinentValue;
import com.stratelia.silverpeas.classifyEngine.Position;
import com.stratelia.silverpeas.classifyEngine.Value;
import com.stratelia.silverpeas.containerManager.ContainerManager;
import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.JoinStatement;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class PdcClassifyBmImpl implements PdcClassifyBm {

  private ClassifyEngine classifyEngine = null;
  private ContainerManager containerManager = null;
  private ContentManager contentManager = null;
  private String m_dbName = JNDINames.PDC_DATASOURCE;

  public PdcClassifyBmImpl() {

    try {
      classifyEngine = new ClassifyEngine();
      containerManager = new ContainerManager();
      contentManager = new ContentManager();
    } catch (ClassifyEngineException ex) {
    } catch (ContentManagerException ex) {
    } catch (ContainerManagerException ex) {
    }
  }

  @Override
  public int isPositionAlreadyExists(int silverObjectId, ClassifyPosition position) throws
      PdcException {
    try {
      return classifyEngine.isPositionAlreadyExists(silverObjectId, position);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.isPositionAlreadyExists",
          SilverpeasException.ERROR, "Pdc.CANNOT_ADD_POSITION", e);
    }
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId) throws
      PdcException {
    Connection connection = null;
    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);
      connection.setAutoCommit(false);
      // Vérification de la contrainte invariante
      int nPositionId = classifyEngine.classifySilverObject(connection, silverObjectId, position);
      // Call the containerManager to register the association containerInstance
      // - ContentInstance(SilverObjectId)
      containerManager.addContainerContentInstanceLink(connection, nPositionId, sComponentId);
      connection.commit();
      return 0;
    } catch (Exception e) {
      DBUtil.rollback(connection);
      throw new PdcException("PdcClassifyBmImpl.addPosition",
          SilverpeasException.ERROR, "Pdc.CANNOT_ADD_POSITION", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  @Override
  public int updatePosition(ClassifyPosition position) throws PdcException {
    try {
      classifyEngine.updateSilverObjectPosition(null, position);
      return 0;
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.updatePosition",
          SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_POSITION", e);
    }
  }

  @Override
  public int updatePositions(List<Value> classifyValues, int silverObjectId) throws PdcException {
    try {
      classifyEngine.updateSilverObjectPositions(null, classifyValues,
          silverObjectId);
      return 0;
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.updatePositions",
          SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_POSITION", e);
    }
  }

  @Override
  public void deletePosition(int nPositionId, String sComponentId) throws PdcException {
    Connection connection = null;

    try {
      // Open the connection
      connection = DBUtil.makeConnection(m_dbName);
      connection.setAutoCommit(false);

      classifyEngine.unclassifySilverObjectByPositionId(connection, nPositionId);

      // Call the containerManager to unregister the association
      // containerInstance - ContentInstance(SilverObjectId)
      containerManager.removeContainerContentInstanceLink(connection,
          nPositionId, sComponentId);

      // Commit
      connection.commit();
    } catch (Exception e) {
      DBUtil.rollback(connection);
      throw new PdcException("PdcClassifyBmImpl.deletePosition",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_POSITION", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  @Override
  public List<Position> getPositions(int silverObjectId, String sComponentId)
      throws PdcException {
    List<Position> positions = null;

    try {
      // Get all the positions for the given silverObjectId
      positions = classifyEngine.findPositionsBySilverOjectId(silverObjectId);

      // Extract the positiondIds
      ArrayList<Integer> alPositionIds = new ArrayList<Integer>();
      for (int nI = 0; positions != null && nI < positions.size(); nI++) {
        int nPositionId = positions.get(nI).getPositionId();
        alPositionIds.add(nPositionId);
      }

      // Get only the positions for the given componentId
      List<Integer> alFilteredPositionIds = containerManager.filterPositionsByComponentId(
          alPositionIds, sComponentId);

      // Rebuild the positions
      List<Position> alFinalPositions = new ArrayList<Position>();
      for (Position position : positions) {
        int nPositionId = position.getPositionId();
        for (int nJ = 0; alFilteredPositionIds != null && nJ < alFilteredPositionIds.size(); nJ++) {
          if (alFilteredPositionIds.get(nJ) == nPositionId) {
            alFinalPositions.add(position);
          }
        }
      }

      return positions;
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPositions",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_POSITIONS", e);
    }
  }

  @Override
  public JoinStatement getPositionsJoinStatement(String sComponentId) throws PdcException {
    ArrayList<String> alComponentId = new ArrayList<String>();
    alComponentId.add(sComponentId);
    return getPositionsJoinStatement(alComponentId);
  }

  @Override
  public JoinStatement getPositionsJoinStatement(List<String> alComponentId) throws PdcException {
    try {
      // Get the join statement for all positions for the given componentId
      return containerManager.getFilterPositionsByComponentIdStatement(null, alComponentId);
    } catch (ContainerManagerException e) {
      throw new PdcException("PdcClassifyBmImpl.getPositions",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_POSITIONS", e);
    }
  }

  @Override
  public void registerAxis(Connection con, int axisId) throws PdcException {
    try {
      classifyEngine.registerAxis(con, axisId);
    } catch (ClassifyEngineException e) {
      throw new PdcException("PdcClassifyBmImpl.getPositions",
          SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_AXE", e);
    }
  }

  @Override
  public void unregisterAxis(Connection con, int axisId) throws PdcException {
    try {
      List<Integer> alDeletedPositionIds = classifyEngine.unregisterAxis(con, axisId);
      containerManager.removeAllPositionIdsLink(con, alDeletedPositionIds);

    } catch (ClassifyEngineException e) {
      throw new PdcException("PdcClassifyBmImpl.getPositions", SilverpeasException.ERROR,
          "Pdc.CANNOT_DELETE_AXE", "axisId: " + axisId, e);
    } catch (ContainerManagerException e) {
      throw new PdcException("PdcClassifyBmImpl.getPositions", SilverpeasException.ERROR,
          "Pdc.CANNOT_DELETE_AXE", "axisId: " + axisId, e);
    }
  }

  /**
   * Create two lists of Value. Then, we replace the first Value list by the second
   * @param con - a connection to the database
   * @param axisId - the id of the axis
   * @param oldPath - a list of path
   * @param newPath - a list of path
   */
  @Override
  public void createValuesAndReplace(Connection con, String axisId,
      List<String> oldPath, List<String> newPath) throws PdcException {
    List<Value> oldValues = new ArrayList<Value>();
    List<Value> newValues = new ArrayList<Value>();
    String path = "";
    Value oldValue = null;
    Value newValue = null;
    // set the axisId of Value Objects
    int id = Integer.parseInt(axisId);
    // oldValue.setAxisId(id);
    // newValue.setAxisId(id);
    // build old values and new values object
    for (int i = 0; i < oldPath.size(); i++) {
      oldValue = new Value();
      newValue = new Value();
      // get oldpath
      path = oldPath.get(i);
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
      Position<Value> position = new Position<Value>(newValues);
      if (classifyEngine.isPositionAlreadyExists(id, position) == -1) {
        classifyEngine.replaceValuesOnAxis(con, oldValues, newValues);
      }
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.createValuesAndReplace",
          SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_POSITIONS", e);
    }
  }

  @Override
  public List<Integer> getObjectsByInstance(String instanceId) throws PdcException {
    // récupère la liste des silverobjectId depuis le container manager
    // qui provienne de l'instanceId
    List<Integer> objectIdList = new ArrayList<Integer>();
    try {
      List<Integer> alPositionIds = containerManager.filterPositionsByComponentId(null,
          instanceId);
      objectIdList = classifyEngine.getSilverContentIdsByPositionIds(alPositionIds);
    } catch (ClassifyEngineException e) {
      SilverTrace.info("ClassifyEngine", "PdcClassifyBmImpl.hasAlreadyPositions",
          "pdcClassify.MSG_CANNOT_GET_SILVEROBJECTID_LIST", "", e);
    } catch (ContainerManagerException e) {
      SilverTrace.info("ClassifyEngine", "PdcClassifyBmImpl.hasAlreadyPositions",
          "pdcClassify.MSG_CANNOT_GET_SILVEROBJECTID_LIST", "", e);
    }
    return objectIdList;
  }

  /**
   * search a defined position for one usedAxis
   * @param objectIdList
   * @param usedAxis - the UsedAxis object
   * @return true if for one UsedAxis, a position exists, false otherwise
   * @throws PdcException
   */
  @Override
  public boolean hasAlreadyPositions(List<Integer> objectIdList, UsedAxis usedAxis)
      throws PdcException {
    String newBaseValue = "/"
        + (new Integer(usedAxis.getBaseValue())).toString() + "/";
    String instanceId = usedAxis.getInstanceId();

    boolean hasOnePosition = false;
    // de toutes ces SilverObjectId, je récupère toutes les positions correspondantes
    List<Position> positions = new ArrayList<Position>();
    for (Integer objectId : objectIdList) {
      if (objectId != -1) {
        positions = getPositions(objectId.intValue(), instanceId);
        // maintenant, je récupère toutes les valeurs de toutes les positions
        // pour ne prendre que les path de chaques Values
        // si la valeur de base ne fait pas partie du chemin alors on ne peut
        // pas
        // modifier cette valeur
        // et il faut que la nouvelle valeur de base ne soit pas dans le chemin
        String onePath = "";
        for (Position position : positions) {
          com.stratelia.silverpeas.classifyEngine.Value value = position.getValueByAxis(usedAxis.
              getAxisId());
          onePath = value.getValue();
          if (onePath != null && onePath.contains(newBaseValue)) {
            // une position existe déjà
            // on ne peut donc pas changer cette valeur de base
            hasOnePosition = true;
            break;
          }
          // une position existe deja, inutile de continuer à chercher d'autres
          // positions
          // je sors donc de la boucle
          if (hasOnePosition) {
            break;
          }
        }
        // une position existe deja, inutile de chercher des positions dans
        // d'autres silverobjectid
        // je sors donc de la boucle principale
        if (hasOnePosition) {
          break;
        }
      }
    }

    return hasOnePosition;
  }

  @Override
  public List<PertinentAxis> getPertinentAxis(SearchContext searchContext, List<Integer> axisIds)
      throws PdcException {
    try {
      return classifyEngine.getPertinentAxis(searchContext.getCriterias(), axisIds);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_AXIS", e);
    }
  }

  @Override
  public List<PertinentValue> getPertinentValues(SearchContext searchContext, int axisId)
      throws PdcException {
    try {
      return classifyEngine.getPertinentValues(searchContext.getCriterias(), axisId);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentValues",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_VALUES", e);
    }
  }

  @Override
  public List<PertinentAxis> getPertinentAxis(SearchContext searchContext, List<Integer> axisIds,
      JoinStatement joinStatementAllPositions) throws PdcException {
    try {
      return classifyEngine.getPertinentAxisByJoin(
          searchContext.getCriterias(), axisIds, joinStatementAllPositions);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_AXIS", e);
    }
  }

  @Override
  public PertinentAxis getPertinentAxis(SearchContext searchContext,
      String axisId, String sRootValue, JoinStatement joinStatementAllPositions)
      throws PdcException {
    try {
      return classifyEngine.getSinglePertinentAxisByJoin(null, searchContext.getCriterias(),
          Integer.parseInt(axisId), sRootValue, joinStatementAllPositions);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_AXIS", e);
    }
  }

  @Override
  public List<PertinentValue> getPertinentValues(SearchContext searchContext, int axisId,
      JoinStatement joinStatementAllPositions) throws PdcException {
    try {
      return classifyEngine.getPertinentValuesByJoin(searchContext.getCriterias(), axisId,
          joinStatementAllPositions);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_VALUES", e);
    }
  }

  @Override
  public List<ObjectValuePair> getObjectValuePairs(SearchContext searchContext, int axisId,
      JoinStatement joinStatementAllPositions) throws PdcException {
    try {
      return classifyEngine.getObjectValuePairsByJoin(searchContext.getCriterias(), axisId,
          joinStatementAllPositions);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.getPertinentAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_PERTINENT_VALUES", e);
    }
  }

  /**
   * Remove all the positions of the given content.
   * @param connection
   * @param nSilverContentId
   * @return
   * @throws PdcException
   */
  @Override
  public List<Integer> removePosition(Connection connection, int nSilverContentId)
      throws PdcException {
    try {
      // Get all the positions of the removed object
      List<Position> alPositions = classifyEngine.findPositionsBySilverOjectId(nSilverContentId);

      // Create the liste with only the positionId
      List<Integer> alPositionIds = new ArrayList<Integer>(alPositions.size());
      for (int nI = 0; alPositions != null && nI < alPositions.size(); nI++) {
        alPositionIds.add(alPositions.get(nI).getPositionId());
      }

      // Unclassify the SilverContentId
      classifyEngine.unclassifySilverObject(connection, nSilverContentId);

      return alPositionIds;
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.removePosition",
          SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_SILVERCONTENTID", e);
    }
  }

  /**
   * Find all the SilverContentId with the given position.
   * @param containerPosition
   * @param alComponentId
   * @param authorId
   * @param afterDate
   * @param beforeDate
   * @return
   * @throws PdcException
   */
  @Override
  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate) throws PdcException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
        authorId, afterDate, beforeDate, true, true);
  }

  /**
   * Find all the SilverContentId with the given position.
   * @param containerPosition
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
  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) throws PdcException {
    try {
      // Change the position in criteria
      SearchContext searchContext = (SearchContext) containerPosition;
      List alCriterias = searchContext.getCriterias();
      // Call the classifyEngine to get the objects
      return classifyEngine.findSilverOjectByCriterias(alCriterias,
          containerManager.getFilterPositionsByComponentIdStatement(null,
          alComponentId), contentManager.getPositionsByGenericSearch(
          authorId, afterDate, beforeDate), afterDate, beforeDate,
          recursiveSearch, visibilitySensitive);
    } catch (Exception e) {
      throw new PdcException("PdcClassifyBmImpl.findSilverContentIdByPosition",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_SILVERCONTENTIDS", e);
    }
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
      List<String> alComponentId) throws PdcException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
        null, null, null);
  }
}