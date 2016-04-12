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
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Consequences;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;action&gt; element of a Process Model.
 **/
public class ActionImpl extends AbstractReferrableObject implements Action, AbstractDescriptor,
    Serializable {
  private static final long serialVersionUID = -6984785710903135661L;
  private String name;
  private String kind;
  private ContextualDesignations labels;
  private ContextualDesignations descriptions;
  private QualifiedUsers allowedUsers;
  private Form form;
  private Consequences consequences;

  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public ActionImpl() {
    reset();
  }

  /**
   * Constructor
   * @param name action name
   */
  public ActionImpl(String name) {
    this();
    this.name = name;
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new SpecificLabelListHelper();
    descriptions = new SpecificLabelListHelper();
    kind = "update";
  }

  /*
   * (non-Javadoc)
   * @see Action#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return labels.createContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see Action#getLabels()
   */
  public ContextualDesignations getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * @see Action#getLabel(java.lang.String, java.lang.String)
   */
  public String getLabel(String role, String language) {
    return labels.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see Action#addLabel(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addLabel(ContextualDesignation label) {
    labels.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see Action#iterateLabel()
   */
  public Iterator<ContextualDesignation> iterateLabel() {
    return labels.iterateContextualDesignation();
  }

  /**
   * Get all the users allowed to execute this action
   * @return object containing QualifiedUsers
   */
  public QualifiedUsers getAllowedUsers() {
    return allowedUsers;
  }

  /**
   * Get all the consequences of this action
   * @return Consequences objects
   */
  public Consequences getConsequences() {
    return consequences;
  }

  /**
   * Get the form associated with this action
   * @return form object
   */
  public Form getForm() {
    return form;
  }

  /**
   * Get the name of this action
   * @return action's name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the kind of this action (update, create or delete)
   * @return action's kind
   */
  public String getKind() {
    return kind;
  }

  /*
   * (non-Javadoc)
   * @see Action#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return descriptions;
  }

  /*
   * (non-Javadoc)
   * @see Action#getDescription(java.lang.String,
   * java.lang.String)
   */
  public String getDescription(String role, String language) {
    return descriptions.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see Action#addDescription(com.silverpeas.
   * workflow.api.model.ContextualDesignation)
   */
  public void addDescription(ContextualDesignation description) {
    descriptions.addContextualDesignation(description);
  }

  /*
   * (non-Javadoc)
   * @see Action#iterateDescription()
   */
  public Iterator<ContextualDesignation> iterateDescription() {
    return descriptions.iterateContextualDesignation();
  }

  /**
   * Create and return an object implementing QalifiedUsers
   */
  public QualifiedUsers createQualifiedUsers() {
    return new QualifiedUsersImpl();
  }

  /**
   * Set the list of users allowed to execute this action
   * @param allowedUsers allowed users
   **/
  public void setAllowedUsers(QualifiedUsers allowedUsers) {
    this.allowedUsers = allowedUsers;
  }

  /**
   * Create and return and object implementing Consequences
   */
  public Consequences createConsequences() {
    return new ConsequencesImpl();
  }

  /**
   * Set the consequences of this action
   * @param consequences
   */
  public void setConsequences(Consequences consequences) {
    this.consequences = consequences;
  }

  /**
   * Set the form associated to this action
   * @param form associated form
   **/
  public void setForm(Form form) {
    this.form = form;
  }

  /**
   * Set the name of this action
   * @param name action's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the kind of this action
   * @param kind action's kind
   */
  public void setKind(String kind) {
    this.kind = kind;
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

  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  public int getId() {
    return id;
  }

  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  public AbstractDescriptor getParent() {
    return parent;
  }

  public boolean hasId() {
    return hasId;
  }
}
