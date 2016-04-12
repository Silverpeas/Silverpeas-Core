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
package com.silverpeas.pdc.web.mock;

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import org.silverpeas.core.util.StringUtil;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.control.GlobalPdcManager;
import com.stratelia.silverpeas.pdc.model.*;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.silverpeas.pdc.web.TestConstants.COMPONENT_INSTANCE_ID;
import static com.silverpeas.pdc.web.TestConstants.CONTENT_ID;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.aClassificationPlan;
import static com.silverpeas.pdc.web.beans.TestPdcClassification.aClassificationFromPositions;

/**
 * A decorator of the PdcBm implementation by mocking some of its services for testing purpose.
 */
@Named("pdcBm")
public class PdcBmMock extends GlobalPdcManager {

  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();
  private List<UsedAxis> axis = new ArrayList<UsedAxis>();

  public PdcBmMock() {
    PdcManager pdcService = mock(PdcManager.class);
    try {
      when(pdcService.getPositions(anyInt(), anyString())).thenReturn(aListOfPositions());
    } catch (PdcException ex) {
      Logger.getLogger(PdcBmMock.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId) throws
      PdcException {
    if (silverObjectId < 0) {
      return new ArrayList<ClassifyPosition>();
    }
    String contentId = getContentIdOf(silverObjectId);
    assertContentExists(contentId, in(sComponentId));
    return new ArrayList<ClassifyPosition>(aListOfPositions());
  }

  @Override
  public void deletePosition(int positionId, String sComponentId) throws PdcException {
    assertComponentExists(sComponentId);
    ClassifyPosition positionToDelete = null;
    for (ClassifyPosition position : aListOfPositions()) {
      if (position.getPositionId() == positionId) {
        positionToDelete = position;
        break;
      }
    }
    if (positionToDelete != null) {
      aListOfPositions().remove(positionToDelete);
    } else {
      throw new PdcException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR, "");
    }
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position, String sComponentId)
      throws PdcException {
    String contentId = getContentIdOf(silverObjectId);
    assertContentExists(contentId, in(sComponentId));
    addPosition(position);
    return position.getPositionId();
  }

  @Override
  public int updatePosition(ClassifyPosition position, String instanceId, int silverObjectId) throws
      PdcException {
    return 0;
  }

  @Override
  public AxisHeader getAxisHeader(String axisId, boolean setTranslations) {
    ClassificationPlan pdc = aClassificationPlan();
    return pdc.getAxisHeader(axisId);
  }

  @Override
  public List<AxisHeader> getAxisByType(String type) throws PdcException {
    if (!StringUtil.isDefined(type)) {
      throw new NullPointerException();
    }
    ClassificationPlan pdc = aClassificationPlan();
    return pdc.getAxisHeaders(type);
  }

  @Override
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    List<UsedAxis> usedAxis = new ArrayList<UsedAxis>();
    ClassificationPlan pdc = aClassificationPlan();
    List<UsedAxis> allAxis = pdc.getUsedAxis();
    for (UsedAxis anAxis : allAxis) {
      if (anAxis.getInstanceId().equals(instanceId)) {
        usedAxis.add(anAxis);
      }
    }
    return usedAxis;
  }

  @Override
  public List<AxisHeader> getAxis() throws PdcException {
    ClassificationPlan pdc = aClassificationPlan();
    return pdc.getAxisHeaders(null);
  }

  @Override
  public List<Value> getAxisValues(int treeId) throws PdcException {
    ClassificationPlan pdc = aClassificationPlan();
    return pdc.getValuesOfAxisById(String.valueOf(treeId));
  }

  @Override
  public Value getValue(String axisId, String valueId) throws PdcException {
    Value theValue = null;
    ClassificationPlan pdc = aClassificationPlan();
    for (Value aValue : pdc.getValuesOfAxisById(axisId)) {
      if (aValue.getPK().getId().equals(valueId)) {
        theValue = aValue;
        break;
      }
    }
    if (theValue == null) {
      throw new PdcException(getClass().getSimpleName() + ".getValue()", SilverpeasException.ERROR,
          "root.NO_EX_MESSAGE");
    }
    return theValue;
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceIds(SearchContext searchContext,
      String axisId, String valueId,
      List<String> instanceIds) throws PdcException {
    List<Value> pertinentValues = new ArrayList<Value>();
    ClassificationPlan pdc = aClassificationPlan();
    UsedAxis theAxis = pdc.getAxis(axisId);
    if (instanceIds.contains(theAxis.getInstanceId())) {
      List<Value> values = pdc.getValuesUsedInClassification(axisId);
      List<SearchCriteria> criteria = searchContext.getCriterias();
      if (criteria.isEmpty()) {
        return values;
      }
      for (Value aValue : values) {
        for (SearchCriteria criterion : criteria) {
          if (criterion.getAxisId() == Integer.valueOf(aValue.getAxisId()) && criterion.getValue().
              equals(aValue.getFullPath())) {
            pertinentValues.add(aValue);
          }
        }
      }
    }
    return pertinentValues;
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
  public String getTreeId(String axisId) throws PdcException {
    return axisId;
  }

  @Override
  public Value getRoot(String axisId) throws PdcException {
    return getValue(axisId, "0");
  }

  public void addClassification(final PdcClassification classification) {
    if (COMPONENT_INSTANCE_ID.equals(classification.getComponentInstanceId())
        && (CONTENT_ID.equals(classification.getContentId()))) {
      this.positions.clear();
      for (ClassifyPosition position : classification.getClassifyPositions()) {
        addPosition(position);
      }
    }
  }

  public void addUsedAxis(final List<UsedAxis> axis) {
    axis.addAll(axis);
  }

  public PdcClassification getClassification(String contentId, String inComponentId) {
    PdcClassification classification = null;
    if (COMPONENT_INSTANCE_ID.equals(inComponentId) && CONTENT_ID.equals(contentId) && !positions.
        isEmpty()) {
      classification = aClassificationFromPositions(positions).ofContent(contentId).
          inComponentInstance(inComponentId);
    }
    return classification;
  }

  private List<ClassifyPosition> aListOfPositions() {
    return this.positions;
  }

  private void addPosition(final ClassifyPosition position) {
    ClassificationPlan pdc = aClassificationPlan();
    for (ClassifyValue classifyValue : position.getValues()) {
      if (classifyValue.getFullPath() == null || classifyValue.getFullPath().isEmpty()) {
        Value value = new Value();
        value.setAxisId(classifyValue.getAxisId());
        String path = classifyValue.getValue();
        path = path.substring(0, path.length() - 1);
        int indexOfTermId = path.lastIndexOf("/") + 1;
        value.setValuePK(new ValuePK(path.substring(indexOfTermId)));
        value.setPath(path.substring(0, indexOfTermId));
        classifyValue.setFullPath(pdc.getPathInTreeOfValue(value));
      }
    }
    position.setPositionId(positions.size());
    positions.add(position);
  }

  private void assertContentExists(String contentId, String inComponentId) throws PdcException {
    if (!COMPONENT_INSTANCE_ID.equals(inComponentId) || !CONTENT_ID.equals(contentId)) {
      throw new PdcException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR, "");
    }
  }

  private void assertComponentExists(String componentId) throws PdcException {
    if (!COMPONENT_INSTANCE_ID.equals(componentId)) {
      throw new PdcException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR, "");
    }
  }

  private String getContentIdOf(int silverObjectId) {
    return String.valueOf(silverObjectId);
  }

  private static String in(String component) {
    return component;
  }
}
