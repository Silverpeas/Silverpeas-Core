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

import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import java.util.ArrayList;
import java.util.List;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;

/**
 * A classification of a resource on the PdC. This is a representation of a classification for
 * testing purpose.
 */
public class PdcClassification {

  private String resourceId;
  private String componentId;
  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();

  public static PdcClassification aPdcClassification() {
    PdcClassification classification = new PdcClassification();
    classification.fill();
    return classification;
  }

  public static PdcClassification aPdcClassificationWithoutAnySynonyms() {
    PdcClassification classification = new PdcClassification();
    classification.fillWithNoSynonyms();
    return classification;
  }

  public static PdcClassification anEmptyPdcClassification() {
    return new PdcClassification();
  }

  public PdcClassification onResource(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  public PdcClassification inComponent(String componentId) {
    this.componentId = componentId;
    return this;
  }

  public String getComponentId() {
    return componentId;
  }

  public List<ClassifyPosition> getPositions() {
    return positions;
  }

  public void setPositions(final List<ClassifyPosition> positions) {
    this.positions.clear();
    this.positions.addAll(positions);
  }

  public String getResourceId() {
    return resourceId;
  }

  private PdcClassification() {

  }

  private void fill() {
    ClassificationPlan pdc = aClassificationPlan();
    
    List<ClassifyValue> positionValues = new ArrayList<ClassifyValue>();
    List<Value> values = pdc.getValuesOfAxisByName("Pays");
    Value value = findTerm("Grenoble", values);
    ClassifyValue classifyValue = new ClassifyValue(toValueId(value.getAxisId()),
            toValue(value.getFullPath()));
    classifyValue.setFullPath(pdc.getPathInTreeOfValue(value));
    positionValues.add(classifyValue);  
    values = pdc.getValuesOfAxisByName("Période");
    value = findTerm("Moyen-Age", values);
    classifyValue = new ClassifyValue(toValueId(value.getAxisId()),
            toValue(value.getFullPath()));
    classifyValue.setFullPath(pdc.getPathInTreeOfValue(value));
    positionValues.add(classifyValue);
    positions.add(new ClassifyPosition(positionValues));
    
    positionValues = new ArrayList<ClassifyValue>();
    values = pdc.getValuesOfAxisByName("Religion");
    value = findTerm("Christianisme", values);
    classifyValue = new ClassifyValue(toValueId(value.getAxisId()),
            toValue(value.getFullPath()));
    classifyValue.setFullPath(pdc.getPathInTreeOfValue(value));
    positionValues.add(classifyValue);  
    positions.add(new ClassifyPosition(positionValues));
  }

  private void fillWithNoSynonyms() {
    ClassificationPlan pdc = aClassificationPlan();
    List<ClassifyValue> positionValues = new ArrayList<ClassifyValue>();
    List<Value> values = pdc.getValuesOfAxisByName("Technologie");
    Value value = values.get(0);
    ClassifyValue classifyValue = new ClassifyValue(toValueId(value.getAxisId()),
            toValue(value.getFullPath()));
    classifyValue.setFullPath(pdc.getPathInTreeOfValue(value));
    positionValues.add(classifyValue);
    positions.add(new ClassifyPosition(positionValues));
  }
  
  private Value findTerm(String term, final List<Value> inValues) {
    Value valueOfTerm = null;
    for (Value value : inValues) {
      if (value.getName().equals(term)) {
        valueOfTerm = value;
        break;
      }
    }
    if (valueOfTerm == null) {
      throw new RuntimeException("The term to find should exist!");
    }
    return valueOfTerm;
  }
  
  private String toValue(String pathOfTerm) {
    return pathOfTerm.substring(0, pathOfTerm.length() - 1);
  }
  
  private int toValueId(String id) {
    return Integer.valueOf(id);
  }
}
