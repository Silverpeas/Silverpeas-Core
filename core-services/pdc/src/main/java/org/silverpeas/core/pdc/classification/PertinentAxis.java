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

package org.silverpeas.core.pdc.classification;

public class PertinentAxis implements java.io.Serializable {

  private static final long serialVersionUID = -7770062847586756429L;
  private int nAxisId = -1;
  private int nbObjects = 0;
  private String sRootValue = "";

  // Constructor
  public PertinentAxis() {
  }

  public void setAxisId(int nGivenAxisId) {
    nAxisId = nGivenAxisId;
  }

  public int getAxisId() {
    return nAxisId;
  }

  public void setNbObjects(int nGivennbObjects) {
    nbObjects = nGivennbObjects;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setRootValue(String sGivenRootValue) {
    sRootValue = sGivenRootValue;
  }

  public String getRootValue() {
    return sRootValue;
  }
}