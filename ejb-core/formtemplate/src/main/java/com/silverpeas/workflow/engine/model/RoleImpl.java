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

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import com.silverpeas.workflow.api.model.AbstractDescriptor;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;role&gt; element of a Process Model.
 **/
public class RoleImpl extends AbstractReferrableObject implements AbstractDescriptor, Role,
    Serializable {
  private static final long serialVersionUID = 1005254939500303606L;
  private String name;
  private ContextualDesignations labels; // collection of labels
  private ContextualDesignations descriptions; // collection of descriptions

  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public RoleImpl() {
    reset();
  }

  /**
   * Constructor
   * @param name role nama
   */
  public RoleImpl(String name) {
    this();
    this.name = name;
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new SpecificLabelListHelper();
    descriptions = new SpecificLabelListHelper();
  }

  /**
   * Get the name of the Role
   * @return role's name
   */
  public String getName() {
    return this.name;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  // //////////////////
  // labels
  // //////////////////

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#getLabels()
   */
  public ContextualDesignations getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#getLabel(java.lang.String, java.lang.String)
   */
  public String getLabel(String role, String language) {
    return labels.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#addLabel(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addLabel(ContextualDesignation label) {
    labels.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#iterateLabel()
   */
  public Iterator<ContextualDesignation> iterateLabel() {
    return labels.iterateContextualDesignation();
  }

  // //////////////////
  // descriptions
  // //////////////////

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return descriptions;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#getDescription(java.lang.String, java.lang.String)
   */
  public String getDescription(String role, String language) {
    return descriptions.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#addDescription(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addDescription(ContextualDesignation description) {
    descriptions.addContextualDesignation(description);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#iterateDescription()
   */
  public Iterator<ContextualDesignation> iterateDescription() {
    return descriptions.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Role#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return labels.createContextualDesignation();
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  public String getKey() {
    return name;
  }

  /************* Implemented methods *****************************************/
  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setId(int)
   */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getId()
   */
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setParent(com.silverpeas
   * .workflow.api.model.AbstractDescriptor)
   */
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getParent()
   */
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }
}