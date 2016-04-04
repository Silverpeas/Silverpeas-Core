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

package com.silverpeas.pdc.model;

import com.silverpeas.pdc.TestResources;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import com.sun.star.drawing.Position3D;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * A unit test class on the conversions between the following model beans:
 * <ul>
 *  <li>PdcAxisValue in ClassifyValue,</li>
 *  <li>PdcPosition in ClassifyPosition.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pdc.xml", "/spring-pdc-embbed-datasource.xml"})
public class PdcModelObjectsConversionsTest {

  @Inject
  private TestResources resources;


  public PdcModelObjectsConversionsTest() {
  }

  @Test
  public void convertFromAPredefinedClasssificationAPdcAxisValueInClassifyValue() {
    String componentId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification classification = resources.predefinedClassificationForComponentInstance(
            componentId);
    ClassifyPosition classifyPosition = classification.getClassifyPositions().get(0);
    ClassifyValue classifyValue = classifyPosition.getListClassifyValue().get(0);
    PdcPosition position = classification.getPositions().iterator().next();
    PdcAxisValue value = position.getValues().iterator().next();

    assertThat(classifyValue.getValue(), is(value.getValuePath() + "/"));

    List<Value> expectedFullPath = fullPathFrom(value);
    List<Value> actualFullPath = classifyValue.getFullPath();
    assertThat(actualFullPath.size(), is(expectedFullPath.size()));
    for (int i = 0; i < expectedFullPath.size(); i++) {
      Value expectedValue = expectedFullPath.get(i);
      Value actualValue = actualFullPath.get(i);
      assertThat(actualValue.getPK().getId(), is(expectedValue.getPK().getId()));
      assertThat(actualValue.getTreeId(), is(expectedValue.getTreeId()));
      assertThat(actualValue.getName(), is(expectedValue.getName()));
      assertThat(actualValue.getFatherId(), is(expectedValue.getFatherId()));
      assertThat(actualValue.getPath(), is(expectedValue.getPath()));
    }
  }

  @Test
  public void convertFromAPredefinedClasssificationAPdcPositionInClassifyPosition() {
    String componentId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification classification = resources.predefinedClassificationForComponentInstance(
            componentId);
    ClassifyPosition classifyPosition = classification.getClassifyPositions().get(0);
    PdcPosition position = classification.getPositions().iterator().next();
    assertThat(classifyPosition.getPositionId(), is(Integer.valueOf(position.getId())));
    assertThat(classifyPosition.getListClassifyValue().size(), is(position.getValues().size()));
  }

  private List<Value> fullPathFrom(final PdcAxisValue axisValue) {
    List<Value> fullPath = new ArrayList<Value>();
    List<? extends TreeNode> treeNodeParents = axisValue.getTreeNodeParents();
    TreeNode lastValue = axisValue.getTreeNode();
    for (TreeNode aTreeNode : treeNodeParents) {
      fullPath.add(new Value(aTreeNode.getPK().getId(), aTreeNode.getTreeId(), aTreeNode.getName(),
              aTreeNode.getDescription(), aTreeNode.getCreationDate(),
              aTreeNode.getCreatorId(), aTreeNode.getPath(), aTreeNode.getLevelNumber(), aTreeNode.
              getOrderNumber(), aTreeNode.getFatherId()));
    }
    fullPath.add(new Value(lastValue.getPK().getId(), lastValue.getTreeId(), lastValue.
            getName(), lastValue.getDescription(), lastValue.getCreationDate(),
            lastValue.getCreatorId(), lastValue.getPath(), lastValue.getLevelNumber(),
            lastValue.getOrderNumber(), lastValue.getFatherId()));
    return fullPath;
  }

}
