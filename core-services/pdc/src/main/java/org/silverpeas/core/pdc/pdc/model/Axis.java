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

import java.util.List;

/**
 * This class contains a full information about a tree. The user can access to an axe.
 */
public class Axis implements java.io.Serializable {

  private static final long serialVersionUID = 5450029132820518355L;

  /**
   * The object which contains attributes of an axe
   */
  private AxisHeader header;

  /**
   * The list which contains sorted values of a tree
   */
  private List<Value> values;

  public Axis(AxisHeader header, List<Value> values) {
    this.header = header;
    this.values = values;
  }

  /**
   * Returns attributs of an axe.
   * @return the AxisHeader object
   */
  public AxisHeader getAxisHeader() {
    return this.header;
  }

  /**
   * Returns the sorted List containing values of a tree.
   * @return the List
   */
  public List<Value> getValues() {
    return this.values;
  }

}