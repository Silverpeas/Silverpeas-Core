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
import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
import java.util.ArrayList;
import java.util.List;
import static com.silverpeas.pdc.web.TestConstants.*;

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
    List<ClassifyValue> positionValues = new ArrayList<ClassifyValue>();
    ClassifyValue positionValue = new ClassifyValue(1, "/Pays/France/Isère");
    List<Value> path = new ArrayList<Value> ();
    path.add(anI18NValue("1", "1", "Pays", "2011/06/14", "0", "/Pays", 1, 1, "-1"));
    path.add(anI18NValue("2", "1", "France", "2011/06/14", "0", "/Pays/France", 2, 1, "1"));
    path.add(anI18NValue("3", "1", "Isère", "2011/06/14", "0", "/Pays/France/Isère", 3, 1, "2"));
    positionValue.setFullPath(path);
    positionValues.add(positionValue);
    
    positionValue = new ClassifyValue(2, "/Période/Moyen-Age");
    path = new ArrayList<Value> ();
    path.add(anI18NValue("4", "2", "Période", "2011/06/14", "0", "/Période", 1, 1, "-1"));
    path.add(anI18NValue("5", "2", "Moyen-Age", "2011/06/14", "0", "/Période/Moyen-Age", 2, 1, "4"));
    positionValue.setFullPath(path);
    positionValues.add(positionValue);
    
    ClassifyPosition position = new ClassifyPosition(positionValues);
    this.positions.add(position);

    positionValues = new ArrayList<ClassifyValue>();
    positionValue = new ClassifyValue(3, "/Religion/Christianisme");
    path = new ArrayList<Value> ();
    path.add(anI18NValue("6", "3", "Religion", "2011/06/14", "0", "/Religion", 1, 1, "-1"));
    path.add(anI18NValue("7", "3", "Christianisme", "2011/06/14", "0", "/Religion/Christianisme", 2, 1, "6"));
    positionValue.setFullPath(path);
    positionValues.add(positionValue);
    
    position = new ClassifyPosition(positionValues);
    this.positions.add(position);
  }
  
  private Value anI18NValue(String id, String treeId, String name, String creationDate,
      String creatorId, String path, int level, int order, String fatherId) {
    Value value = new Value(id, treeId, name, creationDate, creatorId, path, level, order, fatherId);
    value.setLanguage(FRENCH);
    TreeNodeI18N translation = new TreeNodeI18N(Integer.valueOf(id), FRENCH, name, "");
    value.addTranslation(translation);
    return value;
  }
}
