/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package com.stratelia.silverpeas.versioning.model;

public class Reader implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  /** This constant indicates that pk reference in class was not initilized */
  public static final int NULLID = -1;

  private int userId = NULLID;
  private int documentId = NULLID;
  private String instanceId;
  private int saved;

  public Reader() {
  }

  public Reader(int userId, int documentId, String instanceId, int saved) {
    this.userId = userId;
    this.documentId = documentId;
    this.instanceId = instanceId;
    this.saved = saved;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getDocumentId() {
    return documentId;
  }

  public void setDocumentId(int documentId) {
    this.documentId = documentId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getSaved() {
    return saved;
  }

  public void setSaved(int saved) {
    this.saved = saved;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "Reader object : [ userId = " + userId + ", documentId = "
        + documentId + ", instanceId = " + instanceId + ", saved = " + saved
        + " ];";
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
