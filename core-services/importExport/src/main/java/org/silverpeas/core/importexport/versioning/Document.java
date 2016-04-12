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
package org.silverpeas.core.importexport.versioning;

import java.util.Date;

import org.silverpeas.core.ForeignPK;

import org.silverpeas.core.WAPrimaryKey;

public class Document implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
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
  private int typeWorkList;
  private int currentWorkListOrder;
  private int orderNumber;

  private Date alertDate = null; // date d'alerte pour la notification
  // intermediaire
  private Date expiryDate = null; // date d'expiration

  private VersionsType versionsType; // used by import/export engine

  public Document() {
  }

  public Document(DocumentPK pk, WAPrimaryKey foreignKey, String name,
      String description, int status, int ownerId, Date lastCheckOutDate,
      String additionalInfo, String instanceId, int typeWorkList, int currentWorkListOrder) {
    this.pk = pk;
    this.foreignKey = foreignKey;
    this.name = name;
    this.description = description;
    this.status = status;
    this.ownerId = ownerId;
    this.lastCheckOutDate = lastCheckOutDate;
    this.additionalInfo = additionalInfo;
    this.instanceId = instanceId;
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
        + ", instanceId = " + instanceId + ", typeWorkList = " + typeWorkList
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

  public int getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Document other = (Document) obj;
    if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
      return false;
    }
    if (this.foreignKey != other.foreignKey &&
        (this.foreignKey == null || !this.foreignKey.equals(other.foreignKey))) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description
        .equals(other.description)) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    if (this.ownerId != other.ownerId) {
      return false;
    }
    if (this.lastCheckOutDate != other.lastCheckOutDate &&
        (this.lastCheckOutDate == null || !this.lastCheckOutDate.equals(other.lastCheckOutDate))) {
      return false;
    }
    if ((this.additionalInfo == null) ? (other.additionalInfo != null) : !this.additionalInfo
        .equals(other.additionalInfo)) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId
        .equals(other.instanceId)) {
      return false;
    }
    if (this.typeWorkList != other.typeWorkList) {
      return false;
    }
    if (this.currentWorkListOrder != other.currentWorkListOrder) {
      return false;
    }
    if (this.orderNumber != other.orderNumber) {
      return false;
    }
    if (this.alertDate != other.alertDate &&
        (this.alertDate == null || !this.alertDate.equals(other.alertDate))) {
      return false;
    }
    if (this.expiryDate != other.expiryDate &&
        (this.expiryDate == null || !this.expiryDate.equals(other.expiryDate))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    hash = 41 * hash + (this.foreignKey != null ? this.foreignKey.hashCode() : 0);
    hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 41 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 41 * hash + this.status;
    hash = 41 * hash + this.ownerId;
    hash = 41 * hash + (this.lastCheckOutDate != null ? this.lastCheckOutDate.hashCode() : 0);
    hash = 41 * hash + (this.additionalInfo != null ? this.additionalInfo.hashCode() : 0);
    hash = 41 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 41 * hash + this.typeWorkList;
    hash = 41 * hash + this.currentWorkListOrder;
    hash = 41 * hash + this.orderNumber;
    hash = 41 * hash + (this.alertDate != null ? this.alertDate.hashCode() : 0);
    hash = 41 * hash + (this.expiryDate != null ? this.expiryDate.hashCode() : 0);
    return hash;
  }

}