/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.pdc.pdc.model;

import java.util.List;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Nicolas EYSSERIC
 */
public class ClassifyValue extends org.silverpeas.core.pdc.classification.Value implements
        java.io.Serializable {

  private static final long serialVersionUID = 4641811783387127570L;
  private List<Value> fullPath = null;

  private String axisName = null;

  public ClassifyValue() {
  }

  public ClassifyValue(int nGivenAxisId, String sGivenValue) {
    super(nGivenAxisId, sGivenValue);
  }

  public List<Value> getFullPath() {
    return this.fullPath;
  }

  public void setFullPath(List<Value> fullPath) {
    this.fullPath = fullPath;
  }

  public String getAxisName() {
    return this.axisName;
  }

  public void setAxisName(String axisName) {
    this.axisName = axisName;
  }

  @Override
  public String toString() {
    return "ClassifyValue object :[ AxisId=" + getAxisId() + ", " + " value=" + getValue();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ClassifyValue other = (ClassifyValue) obj;
    if (getAxisId() > -1 && isDefined(getValue()) && other.getAxisId() > -1 && isDefined(other.
            getValue())) {
      return getAxisId() == other.getAxisId() && getValue().equals(other.getValue());
    }
    if (this.fullPath != other.fullPath && (this.fullPath == null || !this.fullPath.equals(
            other.fullPath))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 83 * hash + (this.fullPath != null ? this.fullPath.hashCode() : 0);
    hash = 83 * hash + (this.axisName != null ? this.axisName.hashCode() : 0);
    return hash;
  }
}