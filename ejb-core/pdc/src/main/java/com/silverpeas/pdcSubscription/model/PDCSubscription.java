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
/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.model;

import java.util.ArrayList;

public class PDCSubscription implements java.io.Serializable, Cloneable {

  public static final int NULL_ID = -1;

  private int id = NULL_ID;
  private String name;
  private ArrayList pdcContext;
  private int ownerId = NULL_ID;

  protected PDCSubscription() {
  }

  public PDCSubscription(int id, String name, ArrayList pdcContext, int ownerId) {
    this.id = id;
    this.name = name;
    this.pdcContext = pdcContext;
    this.ownerId = ownerId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList getPdcContext() {
    return pdcContext;
  }

  public void setPdcContext(ArrayList pdcContext) {
    this.pdcContext = pdcContext;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(int ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "PDCSubscription object : [ id = " + id + ", name = " + name
        + ", ownerId = " + ownerId + ", pdcContext = " + pdcContext + " ];";
  }

  /**
   * Support Cloneable Interface
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
  }

}
