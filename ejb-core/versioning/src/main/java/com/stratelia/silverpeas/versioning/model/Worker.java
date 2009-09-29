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
 * Date: Oct 16, 2002
 */
package com.stratelia.silverpeas.versioning.model;

public class Worker implements java.io.Serializable, Cloneable {

  /** This constant indicates that pk reference in class was not initialized */
  public static final int NULLID = -1;

  private int id = NULLID;
  private int documentId = NULLID;
  private int order;
  private boolean approval;
  private boolean writer;
  private String instanceId;
  private String type; // 'U' or 'G'
  private boolean saved;
  private boolean used;
  private int listType; // 0=Non ordered 1=Non ordered with approval 2=Ordered

  // with approval

  public Worker() {
  }

  /**
   * @deprecated
   * @param id
   * @param documentId
   * @param order
   * @param approval
   * @param writer
   * @param instanceId
   */
  public Worker(int id, int documentId, int order, boolean approval,
      boolean writer, String instanceId) {
    this.id = id;
    this.documentId = documentId;
    this.order = order;
    this.approval = approval;
    this.writer = writer;
    this.instanceId = instanceId;
  }

  public Worker(int id, int documentId, int order, boolean approval,
      boolean writer, String instanceId, String type, boolean saved,
      boolean used, int listType) {
    this.id = id;
    this.documentId = documentId;
    this.order = order;
    this.approval = approval;
    this.writer = writer;
    this.instanceId = instanceId;
    this.type = type;
    this.saved = saved;
    this.used = used;
    this.listType = listType;
  }

  public int getId() {
    return id;
  }

  public int getUserId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public boolean isApproval() {
    return approval;
  }

  public void setApproval(boolean approval) {
    this.approval = approval;
  }

  public boolean isWriter() {
    return writer;
  }

  public void setWriter(boolean writer) {
    this.writer = writer;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isSaved() {
    return saved;
  }

  public void setSaved(boolean saved) {
    this.saved = saved;
  }

  public boolean isUsed() {
    return used;
  }

  public void setUsed(boolean used) {
    this.used = used;
  }

  public int getListType() {
    return listType;
  }

  public void setListType(int value) {
    this.listType = value;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "Worker object : [ id = " + id + ", documentId = " + documentId
        + ", order = " + order + ", writer = " + writer + ", approval = "
        + approval + ", type = " + type + ", saved = " + saved
        + ", listType = " + listType + " ];";
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
