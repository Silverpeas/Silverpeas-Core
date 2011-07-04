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
package com.silverpeas.pdc.web.beans;

import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
import java.util.List;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static com.silverpeas.pdc.web.TestConstants.*;

/**
 * It represents the PdC to use in the tests. As such it defines the differents axis and their values.
 * Axis values can be either some single terms or some hierarchical semantic trees.
 */
public class ClassificationPlan {

  private static final ClassificationPlan instance = new ClassificationPlan();
  private Set<UsedAxis> pdcAxis = new HashSet<UsedAxis>();

  /**
   * Gets an instance of the classification plan to use in the tests.
   * @return a ClassificationPlan instance.
   */
  public static ClassificationPlan aClassificationPlan() {
    ClassificationPlan pdc = new ClassificationPlan();
    pdc.fill();
    return pdc;
  }

  /**
   * Gets the axis of this classification plan.
   * @return a set of the axis of this classification plan.
   */
  public Set<UsedAxis> getAxis() {
    return pdcAxis;
  }

  /**
   * Gets the values of the axis identified by the specified name.
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
   * @param anAxisId the unique identifier of the axis.
   * @return a unmodifiable list of the axis' values or null if no such axis exists with the
   * specified identifier.
   */
  public List<Value> getValuesOfAxisById(String anAxisId) {
    List<Value> axisValues = null;
    for (UsedAxis axis : pdcAxis) {
      String id = String.valueOf(axis.getAxisId());
      if (id.equals(anAxisId)) {
        axisValues = Collections.unmodifiableList(axis._getAxisValues());
        break;
      }
    }
    return axisValues;
  }

  /**
   * Gets the path in the hierarchic tree of the specified value.
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
    Value value = new Value(id, treeId, name, "2011/06/14", "0", path, level, order, fatherId);
    value.setAxisId(Integer.valueOf(treeId));
    value.setLanguage(FRENCH);
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
    values.add(aValue("3", "1", "Grenoble", "/0/1/2", 3, 0, "2"));
    values.add(aValue("4", "1", "Charente-Maritime", "/0/1/", 2, 1, "1"));
    values.add(aValue("5", "1", "Royan", "/0/1/4/", 3, 0, "4"));
    values.add(aValue("6", "1", "Italie", "/", 0, 1, "-1"));
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
    values.add(aValue("100", "4", "Technologie", "/", 0, 0, "-1"));
    anAxis._setAxisValues(values);
    pdcAxis.add(anAxis);
  }
}
