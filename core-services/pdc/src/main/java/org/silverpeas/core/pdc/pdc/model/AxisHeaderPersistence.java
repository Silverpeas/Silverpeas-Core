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
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;

/**
 * This class contains headers of axis. And uses the persistence class for the DAO. The user can
 * access to the axis main information.
 */
public class AxisHeaderPersistence extends SilverpeasBean implements java.io.Serializable {

  private static final long serialVersionUID = 4467724185924160419L;
  /**
   * The name of the axe
   */
  private String name = null;
  /**
   * The type of the axe
   */
  private String type = null;
  /**
   * The date of creation of the axe
   */
  private String creationDate = null;
  /**
   * The id of the owner of the axe
   */
  private String creatorId = null;
  /**
   * The order of the axe
   */
  private int order = -1;
  /**
   * The rootId of the axe
   */
  private int rootId = -1;
  /**
   * The description of the axe
   */
  private String description = null;
  private String lang = null;

  public AxisHeaderPersistence() {
  }

  public AxisHeaderPersistence(AxisHeader axis) {
    setPK(axis.getPK());
    this.name = axis.getName();
    this.description = axis.getDescription();
    this.creationDate = axis.getCreationDate();
    this.creatorId = axis.getCreatorId();
    this.order = axis.getAxisOrder();
    this.rootId = axis.getRootId();
    this.type = axis.getAxisType();
    this.lang = axis.getLanguage();
  }

  /**
   * Returns the name of the axe.
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * set a name for an axe
   * @param name - the name of the axe
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the type of the axe.
   * @return the type
   */
  public String getAxisType() {
    return this.type;
  }

  /**
   * set a type for an axe
   * @param type - the type of the axe
   */
  public void setAxisType(String type) {
    this.type = type;
  }

  /**
   * Returns the order of the axe.
   * @return the order
   */
  public int getAxisOrder() {
    return this.order;
  }

  /**
   * set an order for an axe
   * @param order - the order of the axe
   */
  public void setAxisOrder(int order) {
    this.order = order;
  }

  /**
   * Returns the id of the axe root.
   * @return the root id
   */
  public int getRootId() {
    return this.rootId;
  }

  /**
   * set a root id for an axe
   * @param rootId - the id of the axe root
   */
  public void setRootId(int rootId) {
    this.rootId = rootId;
  }

  /**
   * Returns the date of creation of the axe.
   * @return the creationDate
   */
  public String getCreationDate() {
    return this.creationDate;
  }

  /**
   * set a date of creation for an axe
   * @param creationDate - the date of creation of the axe
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Returns the id of the axe owner.
   * @return the creatorId
   */
  public String getCreatorId() {
    return this.creatorId;
  }

  /**
   * set the id of the axe owner.
   * @param creatorId - the id of the axe owner.
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Returns the description of the axe.
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * set a description for an axe
   * @param description - the description of the axe
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public String getLang() {
    return this.lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  @Override
  public String toString() {
    return "(pk = " + getPK() + ", name = " + getName() + ", type = "
        + getAxisType() + ", order = " + getAxisOrder() + ", creationDate = "
        + getCreationDate() + ", creatorId = " + getCreatorId() + ", rootId = "
        + getRootId() + ", description = " + getDescription() + ")";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  @Override
  public String _getTableName() {
    return "SB_Pdc_Axis";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AxisHeaderPersistence other = (AxisHeaderPersistence) obj;
    return getPK().getId().equals(other.getPK().getId());
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 71 * hash + (this.getPK() != null ? this.getPK().hashCode() : 0);
    return hash;
  }
}