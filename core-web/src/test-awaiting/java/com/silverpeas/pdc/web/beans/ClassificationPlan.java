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
package com.silverpeas.pdc.web.beans;

import org.silverpeas.core.util.StringUtil;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.AxisHeaderI18N;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.silverpeas.pdc.web.TestConstants.COMPONENT_INSTANCE_ID;
import static com.silverpeas.pdc.web.TestConstants.FRENCH;

/**
 * It represents the PdC to use in the tests. As such it defines the differents axis and their
 * values. Axis values can be either some single terms or some hierarchical semantic trees.
 */
public class ClassificationPlan {

  private static final ClassificationPlan instance = new ClassificationPlan();
  private List<UsedAxis> pdcAxis = new ArrayList<UsedAxis>();

  static {
    instance.fill();
  }

  /**
   * Gets an instance of the classification plan to use in the tests.
   *
   * @return a ClassificationPlan instance.
   */
  public static ClassificationPlan aClassificationPlan() {
    return instance;
  }

  /**
   * Gets the axis of this classification plan ready to be used in a classification.
   *
   * @return an unmodifiable list with the axis of this classification plan.
   */
  public List<UsedAxis> getUsedAxis() {
    return Collections.unmodifiableList(pdcAxis);
  }

  /**
   * Gets the axis of this classification plan.
   *
   * @return an unmodifiable list with the axis of this classification plan.
   */
  public List<Axis> getAxis() {
    List<Axis> axis = new ArrayList<Axis>(pdcAxis.size());
    for (UsedAxis aUsedAxis : pdcAxis) {
      String axisId = String.valueOf(aUsedAxis.getAxisId());
      List<Value> values = getValuesOfAxisById(axisId);
      Axis anAxis = new Axis(getAxisHeader(axisId), values);
      axis.add(anAxis);
    }
    return Collections.unmodifiableList(axis);
  }

  /**
   * Gets the axis of this classification plan that are used in the classification of a content.
   *
   * @return the axis used in a classification of contents.
   */
  public List<UsedAxis> getAxisUsedInClassification() {
    List<UsedAxis> allUsedAxis = new ArrayList<UsedAxis>();
    List<AxisHeader> headers = getAxisHeaders(null);
    for (AxisHeader aHeader : headers) {
      List<Value> usedValues = getValuesUsedInClassification(aHeader.getPK().getId());
      if (!usedValues.isEmpty()) {
        UsedAxis usedAxis = new UsedAxis(aHeader.getPK().getId(), "", aHeader.getRootId(), 0, 0, 1);
        usedAxis._setAxisHeader(aHeader);
        usedAxis._setAxisName(aHeader.getName());
        usedAxis._setAxisType(aHeader.getAxisType());
        usedAxis._setBaseValueName(aHeader.getName());
        usedAxis._setAxisRootId(0);
        usedAxis._setAxisValues(usedValues);
        allUsedAxis.add(usedAxis);
      }
    }
    return allUsedAxis;
  }

  public UsedAxis getAxis(String axisId) {
    int index = Integer.valueOf(axisId) - 1;
    return pdcAxis.get(index);
  }

  /**
   * Gets the headers of the axis of this classification plan.
   *
   * @param type the type of axis: primary or secondary. If null, both are taken into account.
   * @return a list of AxisHeader instances.
   */
  public List<AxisHeader> getAxisHeaders(String type) {
    List<AxisHeader> axisHeaders = new ArrayList<AxisHeader>(pdcAxis.size());
    for (UsedAxis usedAxis : pdcAxis) {
      if (!StringUtil.isDefined(type) || usedAxis._getAxisType().equals(type)) {
        axisHeaders.add(getAxisHeader(String.valueOf(usedAxis.getAxisId())));
      }
    }
    return axisHeaders;
  }

  /**
   * Gets some meta information about the specified axis.
   *
   * @param axisId the unique identifier of the axis in the classification plan.
   * @return an AxisHeader instance.
   */
  public AxisHeader getAxisHeader(String axisId) {
    int index = Integer.valueOf(axisId) - 1;
    UsedAxis axis = pdcAxis.get(index);
    AxisHeader header = new AxisHeader(axisId, axis._getAxisName(), axis._getAxisType(),
        axis.getAxisId(), axis.getAxisId());
    AxisHeaderI18N translationInFrench = new AxisHeaderI18N(axis.getAxisId(), FRENCH,
        axis._getAxisName(), "");
    header.addTranslation(translationInFrench);
    return header;
  }

  public List<Value> getValuesUsedInClassification(String axisId) {
    List<Value> usedValues = new ArrayList<Value>();
    List<Value> allValues = getValuesOfAxisById(axisId);
    for (Value value : allValues) {
      if (value.getNbObjects() > 0) {
        usedValues.add(value);
      }
    }
    return usedValues;
  }

  /**
   * Gets the values of the axis identified by the specified name.
   *
   * @param axisName the name of the axis.
   * @return an unmodifiable list of the axis' values or null if no such axis exists with the
   * specified name.
   */
  public List<Value> getValuesOfAxisByName(String axisName) {
    List<Value> axisValues = null;
    for (UsedAxis axis : pdcAxis) {
      if (axis._getAxisName().equals(axisName)) {
        axisValues = Collections.unmodifiableList(axis._getAxisValues());
        break;
      }
    }
    return axisValues;
  }

  /**
   * Gets the values of the axis identified by the specified identifier.
   *
   * @param anAxisId the unique identifier of the axis.
   * @return a unmodifiable list of the axis' values or null if no such axis exists with the
   * specified identifier.
   */
  public List<Value> getValuesOfAxisById(String anAxisId) {
    int index = Integer.valueOf(anAxisId) - 1;
    UsedAxis axis = pdcAxis.get(index);
    return Collections.unmodifiableList(axis._getAxisValues());
  }

  /**
   * Gets the path in the hierarchic tree of the specified value.
   *
   * @param aValue the value.
   * @return a list of values, each of them representing a node in the path in the tree upto the
   * specified value.
   */
  public List<Value> getPathInTreeOfValue(final Value aValue) {
    List<Value> valuesPath = new ArrayList<Value>();
    List<Value> axisValues = getValuesOfAxisById(aValue.getAxisId());
    String[] pathOfIds = aValue.getFullPath().split("/");
    for (String id : pathOfIds) {
      for (Value anAxisValue : axisValues) {
        if (anAxisValue.getPK().getId().equals(id)) {
          valuesPath.add(anAxisValue);
        }
      }
    }
    return valuesPath;
  }

  protected static Value aValue(String id, String treeId, String name, String path, int level,
      int order, String fatherId) {
    Random random = new Random();
    Value value = new Value(id, treeId, name, "2011/06/14", "0", path, level, order, fatherId);
    value.setAxisId(Integer.valueOf(treeId));
    value.setLanguage(FRENCH);
    value.setNbObjects(random.nextInt(10));
    int nodeId = Integer.valueOf(id);
    TreeNodeI18N translation = new TreeNodeI18N(nodeId, FRENCH, name, "");
    value.addTranslation(translation);
    return value;
  }

  private ClassificationPlan() {
  }

  private void fill() {
    UsedAxis anAxis = new UsedAxis("1", COMPONENT_INSTANCE_ID, 1, 0, 1, 1);
    anAxis._setAxisName("Pays");
    anAxis._setAxisType("P");
    anAxis._setAxisRootId(1);
    List<Value> values = new ArrayList<Value>();
    values.add(aValue("0", "1", "Pays", "/", 0, 0, "-1"));
    values.add(aValue("1", "1", "France", "/0/", 1, 0, "0"));
    values.add(aValue("2", "1", "Isère", "/0/1/", 2, 0, "1"));
    values.add(aValue("3", "1", "Grenoble", "/0/1/2/", 3, 0, "2"));
    values.add(aValue("4", "1", "Charente-Maritime", "/0/1/", 2, 1, "1"));
    values.add(aValue("5", "1", "Royan", "/0/1/4/", 3, 0, "4"));
    values.add(aValue("6", "1", "Italie", "/0/", 0, 1, "-1"));
    anAxis._setAxisValues(values);
    pdcAxis.add(anAxis);

    anAxis = new UsedAxis("2", COMPONENT_INSTANCE_ID, 2, 0, 0, 1);
    anAxis._setAxisName("Période");
    anAxis._setAxisType("P");
    anAxis._setAxisRootId(2);
    values = new ArrayList<Value>();
    values.add(aValue("0", "2", "Période", "/", 0, 0, "-1"));
    values.add(aValue("1", "2", "Antiquité", "/0/", 1, 0, "0"));
    values.add(aValue("2", "2", "Moyen-Age", "/0/", 1, 1, "0"));
    values.add(aValue("3", "2", "Renaissance", "/0/", 1, 2, "0"));
    anAxis._setAxisValues(values);
    pdcAxis.add(anAxis);

    anAxis = new UsedAxis("3", COMPONENT_INSTANCE_ID, 3, 0, 0, 0);
    anAxis._setAxisName("Religion");
    anAxis._setAxisType("P");
    anAxis._setAxisRootId(3);
    values = new ArrayList<Value>();
    values.add(aValue("0", "3", "Religion", "/", 0, 0, "-1"));
    values.add(aValue("1", "3", "Judaïsme", "/0/", 1, 0, "0"));
    values.add(aValue("2", "3", "Christianisme", "/0/", 1, 1, "0"));
    values.add(aValue("3", "3", "Islam", "/0/", 1, 2, "0"));
    anAxis._setAxisValues(values);
    pdcAxis.add(anAxis);

    anAxis = new UsedAxis("4", COMPONENT_INSTANCE_ID, 4, 0, 0, 0);
    anAxis._setAxisName("Technologie");
    anAxis._setAxisType("P");
    anAxis._setAxisRootId(4);
    values = new ArrayList<Value>();
    values.add(aValue("0", "4", "Technologie", "/", 0, 0, "-1"));
    anAxis._setAxisValues(values);
    pdcAxis.add(anAxis);
  }
}
