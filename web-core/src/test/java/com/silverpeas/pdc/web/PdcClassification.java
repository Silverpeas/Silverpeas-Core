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
package com.silverpeas.pdc.web;

import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import java.util.ArrayList;
import java.util.List;

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

  public String getResourceId() {
    return resourceId;
  }

  private void fill() {
    Thesaurus thesaurus = new Thesaurus();
    List<String> treeIds = thesaurus.getTreeIds();
    List<ClassifyValue> positionValues = new ArrayList<ClassifyValue>();
    for (int i = 0; i < treeIds.size(); i++) {
      String treeId = treeIds.get(i);
      List<Value> path = new ArrayList<Value>();
      path.addAll(thesaurus.getValuesFromTree(treeId));
      ClassifyValue positionValue =
              new ClassifyValue(Integer.valueOf(treeId), path.get(path.size() - 1).getPath());
      positionValue.setFullPath(path);
      positionValues.add(positionValue);
      if (i >= 1) {
        ClassifyPosition position = new ClassifyPosition(positionValues);
        this.positions.add(position);
        positionValues = new ArrayList<ClassifyValue>();
      }
    }
  }

  private void fillWithNoSynonyms() {
    Thesaurus thesaurus = new Thesaurus();
    List<ClassifyValue> positionValues = new ArrayList<ClassifyValue>();
    ClassifyValue positionValue = new ClassifyValue(100, "Technique");
    List<Value> path = new ArrayList<Value>();
    path.add(thesaurus.anI18NValue("100", "100", "Technique", "2011/06/16", "0", "/Technique", 0, 0,
            "-1"));
    positionValue.setFullPath(path);
    positionValues.add(positionValue);
    ClassifyPosition position = new ClassifyPosition(positionValues);
    this.positions.add(position);
  }
}
