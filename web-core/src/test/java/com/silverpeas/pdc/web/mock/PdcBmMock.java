/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.pdc.web.mock;

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.pdc.model.ValuePK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;

import static org.mockito.Mockito.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;
import static com.silverpeas.pdc.web.beans.TestPdcClassification.*;

/**
 * A decorator of the PdcBm implementation by mocking some of its services for testing purpose.
 */
@Named("pdcBm")
public class PdcBmMock extends PdcBmImpl {

  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();
  private List<UsedAxis> axis = new ArrayList<UsedAxis>();

  public PdcBmMock() {
    PdcBm pdcService = mock(PdcBm.class);
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
    return aListOfPositions();
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
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    if (!instanceId.equals(COMPONENT_INSTANCE_ID)) {
      return new ArrayList<UsedAxis>();
    }
    ClassificationPlan pdc = aClassificationPlan();
    return pdc.getAxis();
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
