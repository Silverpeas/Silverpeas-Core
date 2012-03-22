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

package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.RecordTemplate;

/**
 * Interface describing a representation of the main &lt;processModel&gt; element of a Process
 * Model. The entry point to reading and manipulating the whole Process Model.
 */
public interface ProcessModel {
  /**
   * Get the id of this process model
   * @return process model id
   */
  public String getModelId();

  /**
   * Set the id of this process model
   * @param name process model id
   */
  public void setModelId(String modelId);

  /**
   * Get the name of this process model
   * @return process model's name
   */
  public String getName();

  /**
   * Set the name of this process model
   * @param process model's name
   */
  public void setName(String name);

  /**
   * Get label in specific language for the given role
   * @param lang label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String language);

  /**
   * Get all the labels
   * @return an object containing the collection of the labels
   */
  public ContextualDesignations getLabels();

  /**
   * Iterate through the Labels
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateLabel();

  /**
   * Create an object implementing ContextualDesignation Method needed primarily by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Add a label Method needed primarily by Castor
   */
  public void addLabel(ContextualDesignation label);

  /**
   * Get description in specific language for the given role
   * @param lang description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  public String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * @return an object containing the collection of the descriptions
   */
  public ContextualDesignations getDescriptions();

  /**
   * Iterate through the descriptions
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateDescription();

  /**
   * Add a description Method needed primarily by Castor
   */
  public void addDescription(ContextualDesignation description);

  /**
   * Get the presentation configuration
   * @return presentation configuration
   */
  public Presentation getPresentation();

  /**
   * Set the presentation's configuration
   * @param presentation presentation's configuration
   */
  public void setPresentation(Presentation presentation);

  /**
   * Create a Presentation
   * @return an object implementing Presentation
   */
  public Presentation createPresentation();

  /**
   * Get the participants definition
   * @return participants definition
   */
  public Participant[] getParticipants();

  /**
   * Get all the participants definitions
   */
  public Participants getParticipantsEx();

  /**
   * Set the participants definition
   * @param participants participants definition
   */
  public void setParticipants(Participants participants);

  /**
   * Create Participants
   * @return an object implementing Participants
   */
  public Participants createParticipants();

  /**
   * Get the roles definition
   * @return roles definition
   */
  public Role[] getRoles();

  /**
   * Get the Roles definitions
   */
  public Roles getRolesEx();

  /**
   * Get the role definition with given name
   * @param name role name
   * @return wanted role definition
   */
  public Role getRole(String name);

  /**
   * Set the roles definition
   * @param roles roles definition
   */
  public void setRoles(Roles roles);

  /**
   * Create Roles
   * @return an object implementing Roles
   */
  public Roles createRoles();

  /**
   * Get the states defined for this process model
   * @return states defined for this process model
   */
  public State[] getStates();

  /**
   * Get the states definitions
   */
  public States getStatesEx();

  /**
   * Get the state definition with given name
   * @param name state name
   * @return wanted state definition
   */
  public State getState(String name);

  /**
   * Set the states defined for this process model
   * @param states states defined for this process model
   */
  public void setStates(States states);

  /**
   * Create States
   * @return an object implementing States
   */
  public States createStates();

  /**
   * Get the actions defined for this process model
   * @return actions defined for this process model
   */
  public Action[] getActions();

  /**
   * Get the actions definitions
   */
  public Actions getActionsEx();

  /**
   * Get the action definition with given name
   * @param name action name
   * @return wanted action definition
   */
  public Action getAction(String name) throws WorkflowException;

  /**
   * Set the actions defined for this process model
   * @param actions actions defined for this process model
   */
  public void setActions(Actions actions);

  /**
   * Create Actions
   * @return an object implementing Actions
   */
  public Actions createActions();

  /**
   * Get the data folder defined for this process model
   * @return data folder defined for this process model. it contains all the items declarations
   */
  public DataFolder getDataFolder();

  /**
   * Set the data folder for this process model
   * @param data folder for this process model. it contains all the items declarations
   * @return
   */
  public void setDataFolder(DataFolder dataFolder);

  /**
   * Create DataFolder
   * @return an object implementing DataFolder
   */
  public DataFolder createDataFolder();

  /**
   * Get the user infos defined for this process model
   * @return user infos defined for this process model. It contains all the items necessary about
   * user to allow him to use the instance
   */
  public DataFolder getUserInfos();

  /**
   * Set the user infos defined for this process model It contains all the items necessary about
   * user to allow him to use the instance
   * @param userInfos user infos defined for this process model.
   */
  public void setUserInfos(DataFolder userInfos);

  /**
   * Get the forms definitions
   */
  public Forms getForms();

  /**
   * Get the form definition with given name
   * @param name action form
   * @return wanted form definition
   */
  public Form getForm(String name);

  /**
   * Get the form definition with given name for the given role
   * @param name action form
   * @return wanted form definition
   */
  public Form getForm(String name, String role);

  /**
   * Set the forms defined for this process model
   * @param forms forms defined for this process model.
   */
  public void setForms(Forms forms);

  /**
   * Create Forms
   * @return an object implementing Forms
   */
  public Forms createForms();

  /**
   * Create an object implementing QualifiedUsers
   */
  public QualifiedUsers createQualifiedUsers();

  /**
   * Create an object implementing RelatedUser
   */
  public RelatedUser createRelatedUser();

  /**
   * Returns the record set where are saved all the folder of the instance built from this model.
   */
  public RecordSet getFolderRecordSet() throws WorkflowException;

  public String getFolderRecordSetName();

  /**
   * Returns the record set where are saved all the data of the named form.
   */
  public RecordSet getFormRecordSet(String formName) throws WorkflowException;

  public String getFormRecordSetName(String formName);

  /**
   * Returns the form (if any) associated to the named action. Returns null if the action has no
   * form. Throws a WorkflowException if the action is unknown.
   */
  public Form getActionForm(String actionName) throws WorkflowException;

  /**
   * Returns the com.silverpeas.form.Form which be used to publish the form associated to the named
   * action. Returns null if the action has no form. Throws a WorkflowException if the action is
   * unknown.
   */
  public com.silverpeas.form.Form getPublicationForm(String actionName,
      String role, String language) throws WorkflowException;

  /**
   * Returns the action of kind create Throws a WorkflowException if there is no action of type
   * create
   */
  public Action getCreateAction(String role) throws WorkflowException;

  /**
   * Returns the com.silverpeas.form.Form which be used to publish the named form. We can give an
   * action name too. Returns null if the action exists but has no form. Throws a WorkflowException
   * if the action/form is unknown.
   */
  public com.silverpeas.form.Form getPresentationForm(String name, String role,
      String language) throws WorkflowException;

  /**
   * Returns the RecordTemplate which describes all the data of the process instance built from this
   * model.
   */
  public RecordTemplate getAllDataTemplate(String role, String lang);

  /**
   * Returns the RecordTemplate which describes a process instance in a row.
   */
  public RecordTemplate getRowTemplate(String role, String lang);

  /**
   * Returns an empty com.silverpeas.form.DataRecord which must be filled in order to process the
   * named action. Returns null if no form is required to process this action. Throws a
   * WorkflowException if the action is unknown.
   */
  public DataRecord getNewActionRecord(String actionName, String roleName,
      String lang, DataRecord data) throws WorkflowException;

  /**
   * Returns an empty com.silverpeas.form.DataRecord which must be filled in order to fill the user
   * information Throws a WorkflowException if problem encountered.
   */
  public DataRecord getNewUserInfosRecord(String roleName, String lang)
      throws WorkflowException;

  /**
   * Returns the roles under which an user can create a new instance
   */
  public String[] getCreationRoles() throws WorkflowException;
}
