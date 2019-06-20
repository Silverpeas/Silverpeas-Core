/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

/**
 * Interface describing a representation of the &lt;action&gt; element of a Process Model.
 **/
public interface Action {
  /**
   * Get the name of this action
   * @return action's name
   */
  String getName();

  /**
   * Set the name of this action
   * @param strName 's name
   */
  void setName(String strName);

  /**
   * Get the kind of this action
   * @return action's kind
   */
  String getKind();

  /**
   * Set the kind of this action
   * @param kind an instance of Kind object
   */
  void setKind(String kind);

  /**
   * Get description in specific language for the given role
   * @param role role for which the description is
   * @param language description's language
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * @return an object containing the collection of the descriptions
   */
  ContextualDesignations getDescriptions();

  /**
   * Get label in specific language for the given role
   * @param role role for which the label is
   * @param language label's language
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  String getLabel(String role, String language);

  /**
   * Get all the labels
   * @return an object containing the collection of the labels
   */
  ContextualDesignations getLabels();

  /**
   * Set the list of users allowed to execute this action
   * @param allowedUsers allowed users
   */
  void setAllowedUsers(QualifiedUsers allowedUsers);

  /**
   * Get all the users allowed to execute this action
   * @return an array of User objects
   */
  QualifiedUsers getAllowedUsers();

  /**
   * Get all the consequences of this action
   * @return Consequences objects
   */
  Consequences getConsequences();

  /**
   * Create and return and object implementing Consequences
   */
  Consequences createConsequences();

  /**
   * Set the consequences of this action
   * @param consequences the consequences
   */
  void setConsequences(Consequences consequences);

  /**
   * Get the form associated with this action
   * @return Form object
   */
  Form getForm();

  /**
   * Set the form associated with this action
   * @param form instance of Form object
   */
  void setForm(Form form);
}