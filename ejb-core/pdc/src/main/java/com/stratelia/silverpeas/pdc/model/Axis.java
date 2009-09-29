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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
 * This class contains a full information about a tree. The user can access to
 * an axe.
 * 
 * @author Sébastien Antonio
 */
public class Axis implements java.io.Serializable {

  /**
   * The object which contains attributs of an axe
   */
  private AxisHeader header = null;

  /**
   * The list which contains sorted values of a tree
   */
  private List values = null;

  //
  // Constructor
  //

  public Axis(AxisHeader header, List values) {
    this.header = header;
    this.values = values;
  }

  //
  // public methods
  //

  /**
   * Returns attributs of an axe.
   * 
   * @return the AxisHeader object
   */
  public AxisHeader getAxisHeader() {
    return this.header;
  }

  /**
   * Returns the sorted List containing values of a tree.
   * 
   * @return the List
   */
  public List getValues() {
    return this.values;
  }

}