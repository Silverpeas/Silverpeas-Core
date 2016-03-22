/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import org.silverpeas.core.workflow.api.model.AbstractDescriptor;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Input;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;input&gt; element of a Process Model.
 **/
public class ItemRef extends AbstractReferrableObject implements Input, AbstractDescriptor,
    Serializable {
  private static final long serialVersionUID = 4356623937044121281L;

  private Item item;
  private boolean readonly = false; // only used in forms construction
  private boolean mandatory = false; // only used in forms construction
  private String displayerName = null; // only used in forms construction
  private String value = null; // default value
  private ContextualDesignations labels; // collection of labels

  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public ItemRef() {
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new SpecificLabelListHelper();
  }

  /**
   * Get the referred item
   */
  public Item getItem() {
    return item;
  }

  /**
   * Get value of readOnly attribute
   * @return true if item must be readonly
   */
  public boolean isReadonly() {
    return this.readonly;
  }

  /**
   * Get value of mandatory attribute
   * @return true if item must be filled
   */
  public boolean isMandatory() {
    return this.mandatory;
  }

  /**
   * Get name of displayer used to show the item
   * @return displayer name
   */
  public String getDisplayerName() {
    return this.displayerName;
  }

  /**
   * Get default value
   * @return default value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Set the referred item
   * @param item Item to refer
   */
  public void setItem(Item item) {
    this.item = item;
  }

  /**
   * Set the readonly attribute
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /**
   * Set value of mandatory attribute
   * @param mandatory true if item must be filled
   */
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  /**
   * Set name of displayer used to show the item
   * @param displayerName displayer name
   */
  public void setDisplayerName(String displayerName) {
    this.displayerName = displayerName;
  }

  /**
   * Set default value
   * @param value default value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * @see Input#getLabels()
   */
  public ContextualDesignations getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * @see Input#getLabel(java.lang.String, java.lang.String)
   */
  public String getLabel(String role, String language) {
    return labels.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see Input#addLabel(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addLabel(ContextualDesignation label) {
    labels.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see Input#iterateLabel()
   */
  public Iterator<ContextualDesignation> iterateLabel() {
    return labels.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see Input#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return labels.createContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    StringBuffer sb = new StringBuffer();

    if (item != null)
      sb.append(item.getName());

    sb.append("|");

    if (value != null)
      sb.append(value);

    return sb.toString();
  }

  /************* Implemented methods *****************************************/
  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#setId(int)
   */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getId()
   */
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#setParent(com.silverpeas
   * .workflow.api.model.AbstractDescriptor)
   */
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getParent()
   */
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }
}