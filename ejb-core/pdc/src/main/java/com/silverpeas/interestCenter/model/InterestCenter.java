/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.model;

import java.util.List;

import com.stratelia.silverpeas.classifyEngine.Criteria;

public class InterestCenter implements Cloneable, java.io.Serializable {

  private static final long serialVersionUID = -7711570385270494209L;

  /** This constant indicates that pk reference in class was not initilized */
  public static final int NULLID = -1;

  private int id;
  private String name;
  private String query;
  private String workSpaceID;
  private String peasID;
  private String authorID;
  private java.util.Date afterDate;
  private java.util.Date beforeDate;
  private List<Criteria> pdcContext;
  private int ownerID;

  /**
   * Default constuctor
   */
  public InterestCenter() {
    this.id = NULLID;
    this.ownerID = NULLID;
  }

  /**
   * Full constructor
   */
  public InterestCenter(int iD, String name, String query, String workSpaceID,
      String peasID, String authorID, java.util.Date afterDate,
      java.util.Date beforeDate, List<Criteria> pcdContext, int ownerID) {
    this.id = iD;
    this.name = name;
    this.query = query;
    this.workSpaceID = workSpaceID;
    this.peasID = peasID;
    this.authorID = authorID;
    this.afterDate = afterDate;
    this.beforeDate = beforeDate;
    this.pdcContext = pcdContext;
    this.ownerID = ownerID;
  }

  public int getId() {
    return id;
  }

  public void setId(int iD) {
    this.id = iD;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getQuery() {
    return (query == null) ? "" : query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getWorkSpaceID() {
    return workSpaceID;
  }

  public void setWorkSpaceID(String workSpaceID) {
    this.workSpaceID = workSpaceID;
  }

  public String getPeasID() {
    return peasID;
  }

  public void setPeasID(String peasID) {
    this.peasID = peasID;
  }

  public String getAuthorID() {
    return authorID;
  }

  public void setAuthorID(String authorID) {
    this.authorID = authorID;
  }

  public java.util.Date getAfterDate() {
    return afterDate;
  }

  public void setAfterDate(java.util.Date afterDate) {
    this.afterDate = afterDate;
  }

  public java.util.Date getBeforeDate() {
    return beforeDate;
  }

  public void setBeforeDate(java.util.Date beforeDate) {
    this.beforeDate = beforeDate;
  }

  public List<Criteria> getPdcContext() {
    return pdcContext;
  }

  public void setPdcContext(List<Criteria> pdcContext) {
    this.pdcContext = pdcContext;
  }

  public int getOwnerID() {
    return ownerID;
  }

  public void setOwnerID(int ownerID) {
    this.ownerID = ownerID;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "InterestCenter object : [ ID = " + id + ", name = " + name
        + ", query = " + query + ", workSpaceID = " + workSpaceID
        + ", peaseID = " + peasID + ", authorID = " + authorID
        + ", afterDate = " + afterDate + ", beforeDate = " + beforeDate
        + ", pcdContext = " + pdcContext + ", ownerID = " + ownerID + " ];";
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
