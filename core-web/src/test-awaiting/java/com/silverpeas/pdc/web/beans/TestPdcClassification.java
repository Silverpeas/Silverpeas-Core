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

package com.silverpeas.pdc.web.beans;

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import java.util.ArrayList;
import java.util.List;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;
import static com.silverpeas.pdc.model.PdcAxisValue.*;
import static com.silverpeas.pdc.model.PdcClassificationHelper.*;

/**
 * A classification on the PdC enriched to be used in tests.
 */
public class TestPdcClassification extends PdcClassification {

  private static final long serialVersionUID = 3802281273787399719L;
  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();

  public static PdcClassification aClassificationFromPositions(
          final List<ClassifyPosition> positions) {
    TestPdcClassification classification = new TestPdcClassification();
    classification.setClassifyPositions(positions);
    classification.buildPdcPositions();
    return classification;
  }

  @Override
  public List<ClassifyPosition> getClassifyPositions() {
    return positions;
  }

  public void setClassifyPositions(final List<ClassifyPosition> positions) {
    this.positions.clear();
    this.positions.addAll(positions);
  }

  private void buildPdcPositions() {
    ClassificationPlan pdc = aClassificationPlan();
    for (ClassifyPosition classifyPosition : positions) {
      PdcPosition pdcPosition = newPdcPositionWithId((long) classifyPosition.getPositionId());
      for (ClassifyValue classifyValue : classifyPosition.getValues()) {
        String id =
                classifyValue.getValue().substring(classifyValue.getValue().lastIndexOf("/") + 1);
        String axisId = String.valueOf(classifyValue.getAxisId());
        List<Value> axisValues = pdc.getValuesOfAxisById(axisId);
        for (Value anAxisValue : axisValues) {
          if (anAxisValue.getPK().getId().equals(id)) {
            pdcPosition.getValues().add(aPdcAxisValueFromTreeNode(anAxisValue));
            break;
          }
        }
      }
      getPositions().add(pdcPosition);
    }
  }
}
