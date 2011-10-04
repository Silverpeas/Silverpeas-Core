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

import com.silverpeas.pdc.model.PdcClassification;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import java.util.ArrayList;
import java.util.List;

/**
 * A classification on the PdC enriched to be used in tests.
 */
public class TestPdcClassification extends PdcClassification {
  private static final long serialVersionUID = 3802281273787399719L;
  
  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();
  
  public static PdcClassification aClassificationFromPositions(final List<ClassifyPosition> positions) {
    TestPdcClassification classification = new TestPdcClassification();
    classification.setClassifyPositions(positions);
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
  
}
