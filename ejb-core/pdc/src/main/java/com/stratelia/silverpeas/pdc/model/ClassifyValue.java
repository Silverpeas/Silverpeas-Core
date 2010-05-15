/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.pdc.model;

import java.util.List;

/**
 * @author Nicolas EYSSERIC
 */
public class ClassifyValue extends com.stratelia.silverpeas.classifyEngine.Value implements
    java.io.Serializable {

  private List fullPath = null;

  private String axisName = null;

  public ClassifyValue() {
  }

  public ClassifyValue(int nGivenAxisId, String sGivenValue) {
    super(nGivenAxisId, sGivenValue);
  }

  // return a list of Value objects
  public List getFullPath() {
    return this.fullPath;
  }

  public void setFullPath(List fullPath) {
    this.fullPath = fullPath;
  }

  public String getAxisName() {
    return this.axisName;
  }

  public void setAxisName(String axisName) {
    this.axisName = axisName;
  }

  public String toString() {
    return "ClassifyValue object :[ AxisId=" + getAxisId() + ", " + " value="
        + getValue();
  }
}