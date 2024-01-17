/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.i18n.AbstractI18NBean;

/**
 * This class contains headers of axis. And uses the persistence class for the DAO. The user can
 * access to the axis main information.
 */
public class AxisHeader extends AbstractI18NBean<AxisHeaderI18N> implements java.io.Serializable {

  // Class version identifier
  private static final long serialVersionUID = 5523411511012194843L;
  private AxisPK pk;
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

  @Override
  protected Class<AxisHeaderI18N> getTranslationType() {
    return AxisHeaderI18N.class;
  }

  //
  // Constructor
  //
  public AxisHeader() {
  }

  public AxisHeader(AxisPK pk, String name, String type, int order,
      String creationDate, String creatorId, int rootId) {
    this.pk = pk;
    setName(name);
    this.type = type;
    this.order = order;
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.rootId = rootId;
  }

  public AxisHeader(String id, String name, String type, int order, int rootId,
      String description) {
    this.pk = new AxisPK(id);
    setName(name);
    setDescription(description);
    this.type = type;
    this.order = order;
    this.rootId = rootId;
  }

  public AxisHeader(String id, String name, String type, int order, int rootId) {
    this.pk = new AxisPK(id);
    setName(name);
    this.type = type;
    this.order = order;
    this.rootId = rootId;
  }

  public AxisHeader(AxisHeaderPersistence persistence) {
    this.pk = (AxisPK) persistence.getPK();
    setName(persistence.getName());
    setDescription(persistence.getDescription());
    this.creationDate = persistence.getCreationDate();
    this.creatorId = persistence.getCreatorId();
    this.order = persistence.getAxisOrder();
    this.rootId = persistence.getRootId();
    this.type = persistence.getAxisType();
    setLanguage(persistence.getLang());
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

  public AxisPK getPK() {
    return pk;
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  @Override
  public String toString() {
    return "(pk = " + getPK() + ", langage = " + getLanguage() + ", name = "
        + getName() + ", type = " + getAxisType() + ", order = "
        + getAxisOrder() + ", creationDate = " + getCreationDate()
        + ", creatorId = " + getCreatorId() + ", rootId = " + getRootId()
        + ", description = " + getDescription() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AxisHeader other = (AxisHeader) obj;
    return this.pk == other.pk || (this.pk != null && this.pk.equals(other.pk));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }
}