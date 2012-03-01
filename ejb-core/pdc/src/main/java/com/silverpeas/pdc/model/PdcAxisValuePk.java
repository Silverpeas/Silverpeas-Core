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
package com.silverpeas.pdc.model;

import java.io.Serializable;

/**
 * The composite primary key used to store values of PdC's axis.
 */
public class PdcAxisValuePk implements Serializable {
  private static final long serialVersionUID = 6046047003447540458L;
 
  private Long valueId;
  private Long axisId;
  
  public static PdcAxisValuePk aPdcAxisValuePk(String valueId, String axisId) {
    PdcAxisValuePk pk = new PdcAxisValuePk();
    pk.setValueId(Long.valueOf(valueId));
    pk.setAxisId(Long.valueOf(axisId));
    return pk;
  }
  
  public static PdcAxisValuePk aPdcAxisValuePk(long valueId, long axisId) {
    PdcAxisValuePk pk = new PdcAxisValuePk();
    pk.setValueId(valueId);
    pk.setAxisId(axisId);
    return pk;
  }

  public Long getAxisId() {
    return axisId;
  }

  public void setAxisId(Long axisId) {
    this.axisId = axisId;
  }

  public Long getValueId() {
    return valueId;
  }

  public void setValueId(Long valueId) {
    this.valueId = valueId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcAxisValuePk other = (PdcAxisValuePk) obj;
    if (this.valueId != other.valueId &&
            (this.valueId == null || !this.valueId.equals(other.valueId))) {
      return false;
    }
    if (this.axisId != other.axisId && (this.axisId == null || !this.axisId.equals(other.axisId))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 13 * hash + (this.valueId != null ? this.valueId.hashCode() : 0);
    hash = 13 * hash + (this.axisId != null ? this.axisId.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "PdcAxisValuePk{" + "valueId=" + valueId + ", axisId=" + axisId + '}';
  }
}
