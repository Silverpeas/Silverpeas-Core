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

import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import static org.mockito.Mockito.*;
import static com.silverpeas.pdc.web.TestConstants.*;

/**
 * A decorator of the PdcBm implementation by mocking some of its services for testing purpose.
 */
@Named("pdcBm")
public class PdcBmMock extends PdcBmImpl {

  private List<ClassifyPosition> positions = new ArrayList<ClassifyPosition>();

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
    String contentId = getContentIdOf(silverObjectId);
    if (!COMPONENT_INSTANCE_ID.equals(sComponentId) || !CONTENT_ID.equals(contentId)) {
      throw new PdcException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR, "");
    }
    return aListOfPositions();
  }

  private List<ClassifyPosition> aListOfPositions() {
    return this.positions;
  }
  
  public void addClassification(final PdcClassification classification) {
    if (COMPONENT_INSTANCE_ID.equals(classification.getComponentId()) &&
            CONTENT_ID.equals(classification.getResourceId())) {
      this.positions.clear();
      this.positions.addAll(classification.getPositions());
    }
  }
  
  private String getContentIdOf(int silverObjectId) {
    return String.valueOf(silverObjectId);
  }
}
