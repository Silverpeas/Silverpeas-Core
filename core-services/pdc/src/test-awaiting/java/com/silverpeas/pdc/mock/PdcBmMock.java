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

package com.silverpeas.pdc.mock;

import org.silverpeas.core.contribution.contentcontainer.container.ContainerManagerException;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPositionInterface;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import org.silverpeas.search.searchEngine.model.AxisFilter;
import org.silverpeas.util.exception.SilverpeasException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;

/**
 * A mock of the PdcBm service.
 */
@Named("pdcBm")
public class PdcBmMock implements PdcManager {

  private List<TreeNode> treeNodes = new ArrayList<TreeNode>();

  public void addTreeNodes(final List<TreeNode> treeNodes) {
    this.treeNodes.addAll(treeNodes);
  }

  public void addTreeNode(final TreeNode treeNode) {
    this.treeNodes.add(treeNode);
  }

  @Override
  public List<AxisHeader> getAxisByType(String type) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AxisHeader> getAxis() throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getNbAxis() throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getNbMaxAxis() throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int createAxis(AxisHeader axisHeader) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int updateAxis(AxisHeader axisHeader) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAxis(Connection con, String axisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Axis getAxisDetail(String axisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Axis getAxisDetail(String axisId, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public AxisHeader getAxisHeader(String axisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Value getValue(String axisId, String valueId) throws PdcException {
    Value value = null;
    for (TreeNode treeNode : treeNodes) {
      if (treeNode.getTreeId().equals(axisId) && treeNode.getPK().getId().equals(valueId)) {
        value = new Value(valueId, treeNode.getTreeId(), treeNode.getName(), axisId, treeNode.
                getCreationDate(), treeNode.getCreatorId(), treeNode.getPath(), treeNode.
                getLevelNumber(), 0, treeNode.getFatherId());
        break;
      }
    }
    if (value == null) {
      throw new PdcException(getClass().getSimpleName() + "getValue()", SilverpeasException.ERROR,
              "");
    }
    return value;
  }

  @Override
  public Value getAxisValue(String valueId, String treeId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getAxisValuesByName(String valueName) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<String> getDaughterValues(String axisId, String valueId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getFilteredAxisValues(String rootId, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Value getRoot(String axisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getAxisValues(int treeId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int insertMotherValue(Value valueToInsert, String refValue, String axisId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int moveValueToNewFatherId(Axis axis, Value valueToMove, String newFatherId,
          int orderNumber) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<List<String>> getManagers(String axisId, String valueId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isUserManager(String userId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<List<String>> getInheritedManagers(Value value) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setManagers(List<String> userIds,
          List<String> groupIds, String axisId, String valueId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void razManagers(String axisId, String valueId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteManager(String userId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteGroupManager(String groupId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int createDaughterValue(Value valueToInsert, String refValue, String treeId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String createDaughterValueWithId(Value valueToInsert, String refValue, String treeId)
          throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int updateValue(Value value, String treeId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteValueAndSubtree(Connection con, String valueId, String axisId, String treeId)
          throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String deleteValue(Connection con, String valueId, String axisId, String treeId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    List<Value> fullPath = new ArrayList<Value>();
    Value value = getValue(treeId, valueId);
    fullPath.add(value);
    while (value.hasFather()) {
      value = getValue(treeId, value.getFatherId());
      fullPath.add(0, value);
    }
    return fullPath;
  }

  @Override
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int addUsedAxis(UsedAxis usedAxis) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<UsedAxis> getUsedAxisToClassify(String instanceId, int silverObjectId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId,
          boolean alertSubscribers) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int updatePosition(ClassifyPosition position, String instanceId, int silverObjectId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int updatePosition(ClassifyPosition position, String instanceId, int silverObjectId,
          boolean alertSubscribers) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deletePosition(int positionId, String sComponentId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void copyPositions(int fromObjectId, String fromInstanceId, int toObjectId,
          String toInstanceId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isClassifyingMandatory(String componentId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SearchAxis> getPertinentAxis(SearchContext searchContext, String axisType) throws
          PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext, String axisType,
          String instanceId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext, String axisType,
          String instanceId, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext, String axisType,
          List<String> instanceIds) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext, String axisType,
          List<String> instanceIds, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getFirstLevelAxisValuesByInstanceId(SearchContext searchContext, String axisId,
          String instanceId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext, String axisId,
          List<String> instanceIds) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceId(SearchContext searchContext,
          String axisId, String valueId, String instanceId) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceId(SearchContext searchContext,
          String axisId, String valueId, String instanceId, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceIds(SearchContext searchContext,
          String axisId, String valueId,
          List<String> instanceIds) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceIds(SearchContext searchContext,
          String axisId, String valueId,
          List<String> instanceIds, AxisFilter filter) throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
          List<String> alComponentId, String authorId, String afterDate, String beforeDate) throws
          ContainerManagerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
          List<String> alComponentId, String authorId, String afterDate, String beforeDate,
          boolean recursiveSearch, boolean visibilitySensitive) throws ContainerManagerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
          List<String> alComponentId) throws ContainerManagerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
          List<String> alComponentId, boolean recursiveSearch, boolean visibilitySensitive) throws
          ContainerManagerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getDaughters(String refValue, String treeId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Value> getSubAxisValues(String axisId, String valueId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void indexAllAxis() throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getTreeId(String axisId) throws PdcException {
    return axisId;
  }

  @Override
  public void addPositions(List<ClassifyPosition> positions, int objectId, String instanceId)
      throws PdcException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
