/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.model;

import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Input;
import org.silverpeas.core.workflow.api.model.Item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;input&gt; element of a Process Model.
 **/
@XmlRootElement(name = "input")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemRef implements Input, Serializable {
  private static final long serialVersionUID = 4356623937044121281L;
  @XmlIDREF
  @XmlAttribute
  private ItemImpl item;
  // only used in forms construction
  @XmlAttribute
  private boolean readonly = false;
  // only used in forms construction
  @XmlAttribute
  private boolean mandatory = false;
  // only used in forms construction
  @XmlAttribute
  private String displayerName = null;
  // default value
  @XmlAttribute
  private String value = null;
  // collection of labels
  @XmlElement(name="label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;

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
    labels = new ArrayList<>();
  }

  /**
   * Get the referred item
   */
  @Override
  public Item getItem() {
    return item;
  }

  /**
   * Get value of readOnly attribute
   * @return true if item must be readonly
   */
  @Override
  public boolean isReadonly() {
    return this.readonly;
  }

  /**
   * Get value of mandatory attribute
   * @return true if item must be filled
   */
  @Override
  public boolean isMandatory() {
    return this.mandatory;
  }

  /**
   * Get name of displayer used to show the item
   * @return displayer name
   */
  @Override
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
  @Override
  public void setItem(Item item) {
    this.item = (ItemImpl) item;
  }

  /**
   * Set the readonly attribute
   */
  @Override
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /**
   * Set value of mandatory attribute
   * @param mandatory true if item must be filled
   */
  @Override
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  /**
   * Set name of displayer used to show the item
   * @param displayerName displayer name
   */
  @Override
  public void setDisplayerName(String displayerName) {
    this.displayerName = displayerName;
  }

  /**
   * Set default value
   * @param value default value
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * @see Input#getLabels()
   */
  @Override
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  /*
   * (non-Javadoc)
   * @see Input#getLabel(java.lang.String, java.lang.String)
   */
  @Override
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

}