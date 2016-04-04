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

package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;

public class CompleteUsedAxis extends SilverpeasBean implements java.io.Serializable {

  private static final long serialVersionUID = -4374704275756625743L;
  private Axis axis = null;
  private UsedAxis usedAxis = null;

  public CompleteUsedAxis() {
  }

  public CompleteUsedAxis(Axis axis, UsedAxis usedAxis) {
    this.axis = axis;
    this.usedAxis = usedAxis;
  }

  public void setAxis(Axis axis) {
    this.axis = axis;
  }

  public void setUsedAxis(UsedAxis usedAxis) {
    this.usedAxis = usedAxis;
  }

  public Axis getAxis() {
    return this.axis;
  }

  public UsedAxis getUsedAxis() {
    return this.usedAxis;
  }

}