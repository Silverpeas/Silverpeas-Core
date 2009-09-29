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

import java.util.ArrayList;
import java.util.Date;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.versioning.importExport.VersionsType;
import com.stratelia.webactiv.util.WAPrimaryKey;

public class Document implements java.io.Serializable, Cloneable {

  public final static int STATUS_CHECKINED = 0;
  public final static int STATUS_CHECKOUTED = 1;

  private DocumentPK pk;
  private WAPrimaryKey foreignKey;
  private String name;
  private String description;
  private int status;
  private int ownerId;
  private Date lastCheckOutDate;
  private String additionalInfo;
  private String instanceId;
  private ArrayList workList;
  private ArrayList readList;
  private int typeWorkList;
  private int currentWorkListOrder;

  private Date alertDate = null; // date d'alerte pour la notification
  // intermédiaire
  private Date expiryDate = null; // date d'expiration

  private VersionsType versionsType; // used by import/export engine

  public Document() {
  }

  public Document(DocumentPK pk, WAPrimaryKey foreignKey, String name,
      String description, int status, int ownerId, Date lastCheckOutDate,
      String additionalInfo, String instanceId, ArrayList workList,
      ArrayList readList, int typeWorkList, int currentWorkListOrder) {
    this.pk = pk;
    this.foreignKey = foreignKey;
    this.name = name;
    this.description = description;
    this.status = status;
    this.ownerId = ownerId;
    this.lastCheckOutDate = lastCheckOutDate;
    this.additionalInfo = additionalInfo;
    this.instanceId = instanceId;
    this.workList = workList;
    this.readList = readList;
    this.typeWorkList = typeWorkList;
    this.currentWorkListOrder = currentWorkListOrder;
  }

  public DocumentPK getPk() {
    return pk;
  }

  public void setPk(DocumentPK pk) {
    this.pk = pk;
  }

  public WAPrimaryKey getForeignKey() {
    return foreignKey;
  }

  public void setForeignKey(ForeignPK foreignKey) {
    this.foreignKey = foreignKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(int ownerId) {
    this.ownerId = ownerId;
  }

  public Date getLastCheckOutDate() {
    return lastCheckOutDate;
  }

  public void setLastCheckOutDate(Date lastCheckOutDate) {
    this.lastCheckOutDate = lastCheckOutDate;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public ArrayList getWorkList() {
    return workList;
  }

  public void setWorkList(ArrayList workList) {
    this.workList = workList;
  }

  public ArrayList getReadList() {
    return readList;
  }

  public void setReadList(ArrayList readList) {
    this.readList = readList;
  }

  public int getTypeWorkList() {
    return typeWorkList;
  }

  public void setTypeWorkList(int typeWorkList) {
    this.typeWorkList = typeWorkList;
  }

  public int getCurrentWorkListOrder() {
    return currentWorkListOrder;
  }

  public void setCurrentWorkListOrder(int currentWorkListOrder) {
    this.currentWorkListOrder = currentWorkListOrder;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "Worker object : [ pk = " + pk + ", foreignKey = " + foreignKey
        + ", name = " + name + ", description = " + description + ", status = "
        + status + ", ownerId = " + ownerId + ", lastCheckOutDate = "
        + lastCheckOutDate + ", additionalInfo = " + additionalInfo
        + ", instanceId = " + instanceId + ", workList = " + workList
        + ", readList = " + readList + ", typeWorkList = " + typeWorkList
        + ", currentWorkListOrder = " + currentWorkListOrder + " ];";
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

  public Date getAlertDate() {
    return alertDate;
  }

  public void setAlertDate(Date alertDate) {
    this.alertDate = alertDate;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }

  public VersionsType getVersionsType() {
    return versionsType;
  }

  public void setVersionsType(VersionsType versionsType) {
    this.versionsType = versionsType;
  }
}