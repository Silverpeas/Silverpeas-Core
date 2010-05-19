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

package com.stratelia.silverpeas.pdc.model;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This class contains headers of axis. And uses the persistence class for the DAO. The user can
 * access to the axis main information.
 */
public class AxisHeader extends AbstractI18NBean implements java.io.Serializable {

  // Class version identifier
  private static final long serialVersionUID = 5523411511012194843L;

  private WAPrimaryKey pk;

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

  //
  // Constructor
  //

  public AxisHeader() {
  }

  public AxisHeader(AxisPK pk, String name, String type, int order,
      String creationDate, String creatorId, int rootId) {
    setPK(pk);
    this.name = name;
    this.type = type;
    this.order = order;
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.rootId = rootId;
  }

  public AxisHeader(AxisPK pk, String name, String type, int order,
      String creationDate, String creatorId, int rootId, String description) {
    setPK(pk);
    this.name = name;
    this.type = type;
    this.order = order;
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.rootId = rootId;
    this.description = description;
  }

  public AxisHeader(String id, String name, String type, int order, int rootId,
      String description) {
    setPK(new AxisPK(id));
    this.name = name;
    this.type = type;
    this.order = order;
    this.rootId = rootId;
    this.description = description;
  }

  public AxisHeader(String id, String name, String type, int order, int rootId) {
    setPK(new AxisPK(id));
    this.name = name;
    this.type = type;
    this.order = order;
    this.rootId = rootId;
  }

  public AxisHeader(AxisHeaderPersistence persistence) {
    this.pk = persistence.getPK();
    this.name = persistence.getName();
    this.description = persistence.getDescription();
    this.creationDate = persistence.getCreationDate();
    this.creatorId = persistence.getCreatorId();
    this.order = persistence.getAxisOrder();
    this.rootId = persistence.getRootId();
    this.type = persistence.getAxisType();
    setLanguage(persistence.getLang());
  }

  //
  // public methods
  //

  /**
   * Returns the name of the axe.
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  public String getName(String language) {
    if (!I18NHelper.isI18N)
      return getName();

    AxisHeaderI18N s = (AxisHeaderI18N) getTranslations().get(language);
    if (s == null)
      s = (AxisHeaderI18N) getNextTranslation();

    return s.getName();
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

  public String getDescription(String language) {
    if (!I18NHelper.isI18N)
      return getDescription();

    AxisHeaderI18N s = (AxisHeaderI18N) getTranslations().get(language);
    if (s == null)
      s = (AxisHeaderI18N) getNextTranslation();

    return s.getDescription();
  }

  /**
   * set a description for an axe
   * @param description - the description of the axe
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public WAPrimaryKey getPK() {
    return pk;
  }

  public void setPK(WAPrimaryKey value) {
    pk = value;
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  public String toString() {
    return "(pk = " + getPK() + ", langage = " + getLanguage() + ", name = "
        + getName() + ", type = " + getAxisType() + ", order = "
        + getAxisOrder() + ", creationDate = " + getCreationDate()
        + ", creatorId = " + getCreatorId() + ", rootId = " + getRootId()
        + ", description = " + getDescription() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AxisHeader))
      return false;
    return (getPK().getId().equals(((AxisHeader) obj).getPK().getId()));
  }

}