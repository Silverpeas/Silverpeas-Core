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
package com.silverpeas.workflow.api.model;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;action&gt; element of a
 * Process Model.
 **/
public interface Action {
  /**
   * Get the name of this action
   * 
   * @return action's name
   */
  public String getName();

  /**
   * Set the name of this action
   * 
   * @param action
   *          's name
   */
  public void setName(String strName);

  /**
   * Get the kind of this action
   * 
   * @return action's kind
   */
  public String getKind();

  /**
   * Set the kind of this action
   * 
   * @param action
   *          's kind
   */
  public void setKind(String kind);

  /**
   * Get description in specific language for the given role
   * 
   * @param lang
   *          description's language
   * @param role
   *          role for which the description is
   * @return wanted description as a String object. If description is not found,
   *         search description with given role and default language, if not
   *         found again, return the default description in given language, if
   *         not found again, return the default description in default
   *         language, if not found again, return empty string.
   */
  public String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * 
   * @return an object containing the collection of the descriptions
   */
  public ContextualDesignations getDescriptions();

  /**
   * Iterate through the descriptions
   * 
   * @return an iterator
   */
  public Iterator iterateDescription();

  /**
   * Add a description Method needed primarily by Castor
   */
  public void addDescription(ContextualDesignation description);

  /**
   * Get label in specific language for the given role
   * 
   * @param lang
   *          label's language
   * @param role
   *          role for which the label is
   * @return wanted label as a String object. If label is not found, search
   *         label with given role and default language, if not found again,
   *         return the default label in given language, if not found again,
   *         return the default label in default language, if not found again,
   *         return empty string.
   */
  public String getLabel(String role, String language);

  /**
   * Get all the labels
   * 
   * @return an object containing the collection of the labels
   */
  public ContextualDesignations getLabels();

  /**
   * Iterate through the Labels
   * 
   * @return an iterator
   */
  public Iterator iterateLabel();

  /**
   * Add a label Method needed primarily by Castor
   */
  public void addLabel(ContextualDesignation label);

  /**
   * Create an object implementing ContextualDesignation Method needed primarily
   * by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Create and return an object implementing QalifiedUsers
   */
  public QualifiedUsers createQualifiedUsers();

  /**
   * Set the list of users allowed to execute this action
   * 
   * @param allowedUsers
   *          allowed users
   */
  public void setAllowedUsers(QualifiedUsers allowedUsers);

  /**
   * Get all the users allowed to execute this action
   * 
   * @return an array of User objects
   */
  public QualifiedUsers getAllowedUsers();

  /**
   * Get all the consequences of this action
   * 
   * @return Consequences objects
   */
  public Consequences getConsequences();

  /**
   * Create and return and object implementing Consequences
   */
  public Consequences createConsequences();

  /**
   * Set the consequences of this action
   * 
   * @param consequences
   */
  public void setConsequences(Consequences consequences);

  /**
   * Get the form associated with this action
   * 
   * @return Form object
   */
  public Form getForm();

  /**
   * Set the form associated with this action
   * 
   * @param Form
   *          object
   */
  public void setForm(Form form);
}