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
package com.silverpeas.workflowdesigner.control;

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.InstanciationException;
import org.apache.commons.fileupload.FileItem;

import com.silverpeas.workflow.api.ProcessModelManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Actions;
import com.silverpeas.workflow.api.model.AllowedActions;
import com.silverpeas.workflow.api.model.Columns;
import com.silverpeas.workflow.api.model.Consequence;
import com.silverpeas.workflow.api.model.Consequences;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.DataFolder;
import com.silverpeas.workflow.api.model.Form;
import com.silverpeas.workflow.api.model.Forms;
import com.silverpeas.workflow.api.model.Input;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.api.model.Participants;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.QualifiedUsers;
import com.silverpeas.workflow.api.model.RelatedUser;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.api.model.Roles;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.States;
import com.silverpeas.workflow.api.model.UserInRole;
import com.silverpeas.workflow.engine.WorkflowHub;
import com.silverpeas.workflow.engine.model.ProcessModelManagerImpl;
import com.silverpeas.workflowdesigner.model.WorkflowDesignerException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;
import com.stratelia.webactiv.beans.admin.instance.control.SPProfile;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Set;

public class WorkflowDesignerSessionController extends AbstractComponentSessionController {

  public static final String TYPE_USER = "user";
  public static final String FORMS = "forms";
  public static final String DATA_FOLDER = "dataFolder";
  public static final String USER_INFOS = "userInfos";
  public static final String ACTIONS = "actions";
  public static final String INPUTS = "inputs";
  public static final String STATES = "states";
  public static final String PARAMETERS = "parameters";
  public static final String PARTICIPANTS = "participants";
  public static final String PRESENTATION = "presentation";
  public static final String ROLES = "roles";
  public static final String RELATED_USER = "relatedUser";
  public static final String WORKING_USERS = "workingUsers";
  public static final String INTERESTED_USERS = "interestedUsers";
  public static final String NOTIFIED_USERS = "notifiedUsers";
  public static final String ALLOWED_USERS = "allowedUsers";
  public static final String CONSEQUENCES = "consequences";
  public static final String FORM_TYPE_PRESENTATION = "presentationForm";
  public static final String FORM_TYPE_PRINT = "printForm";
  public static final String FORM_TYPE_ACTION = "action";
  public static final String NEW_ELEMENT_NAME = "New"; // an initial name for new model elements 
  public static final String TITLES = "titles";
  public static final String ACTIVITIES = "activities";
  public static final String DESCRIPTIONS = "descriptions";
  public static final String LABELS = "labels";
  private static final String CONTEXT_DELIMS = "/[]";
  private ProcessModel m_processModel; // The process model being edited.
  private String m_strProcessModelFileName; // we have to store the file path as
  // well, since it is not included in
  // the ProcessModel object...
  private String m_strReferencedInComponent; // the name of a Component

  // Descriptor where the current
  // process model is referenced
  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public WorkflowDesignerSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.workflowdesigner.multilang.workflowDesignerBundle",
        "com.silverpeas.workflowdesigner.settings.workflowDesignerIcons",
        "com.silverpeas.workflowdesigner.settings.workflowDesigner");
  }

  /**
   * Get the list of all process models available for edition
   * @return A list of Strings containing relative paths to process model file names.
   * @throws WorkflowDesignerException
   */
  public List listProcessModels() throws WorkflowDesignerException {
    try {
      return Workflow.getProcessModelManager().listProcessModels();
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.getPorcesModels",
          SilverpeasException.ERROR, "workflowDesigner.EX_GETTING_RPOCES_MODELS_FAILED",
          e);
    }
  }

  /**
   * Create a new ProcessModel descriptor that is not yet saved in a XML file.
   * @return ProcessModel object
   */
  public ProcessModel createProcessModel() throws WorkflowDesignerException {
    try {
      m_processModel = Workflow.getProcessModelManager().createProcessModelDescriptor();
      m_strProcessModelFileName = NEW_ELEMENT_NAME;
      m_strReferencedInComponent = null;

      return m_processModel;
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.createPorcesModel",
          SilverpeasException.ERROR, "workflowDesigner.EX_CREATING_PROCESS_DESCRIPTIOR_FAILED",
          e);
    }
  }

  /**
   * Load the process model from the specified file, cache it before returning.
   * @param strProcessFileName relative path and the file name
   * @return An object implementing the ProcessModel interface
   * @throws WorkflowDesignerException
   */
  public ProcessModel loadProcessModel(String strProcessFileName)
      throws WorkflowDesignerException {
    try {
      // Load the process model from the XML file
      //
      m_processModel = Workflow.getProcessModelManager().loadProcessModel(
          strProcessFileName, false);
      m_strProcessModelFileName = strProcessFileName;

      // check if a component descriptor exists for this process model
      // store this info in an attribute
      //
      m_strReferencedInComponent = findComponentDescriptor(m_strProcessModelFileName);

      return m_processModel;
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.loadProcesModel",
          SilverpeasException.ERROR, "workflowDesigner.EX_LOADING_PROCES_MODEL_FAILED",
          e);
    }
  }

  /**
   * Save the currently cached process model in a XML file
   * @param strProcessModelFileName the relative path and the name of the file
   * @throws WorkflowDesignerException when the saving goes wrong...
   */
  public void saveProcessModel(String strProcessModelFileName)
      throws WorkflowDesignerException {
    try {
      // Is this the first save of a process model?
      //
      if (strProcessModelFileName != null
          && NEW_ELEMENT_NAME.equals(m_strProcessModelFileName)) {
        // Make sure that you are not overwriting sth.
        //
        List processList = Workflow.getProcessModelManager().listProcessModels();

        strProcessModelFileName = strProcessModelFileName.replace('\\',
            File.separatorChar);
        strProcessModelFileName = strProcessModelFileName.replace('/',
            File.separatorChar);

        if (processList.indexOf(strProcessModelFileName) >= 0) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.saveProcesModel",
              SilverpeasException.ERROR, "workflowDesigner.EX_PROCESS_MODEL_EXISTS");
        }
        // Cache the file name if it is the first save
        //
        m_strProcessModelFileName = strProcessModelFileName;
      }

      // FIXME Before saving verify that the model is valid
      //

      Workflow.getProcessModelManager().saveProcessModel(m_processModel,
          m_strProcessModelFileName);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.saveProcesModel",
          SilverpeasException.ERROR, "workflowDesigner.EX_SAVING_PROCESS_MODEL_FAILED",
          e);
    }
  }

  /**
   * Removes the process model descriptor file from the filesystem
   * @param strProcessModelFileName the relative path to the process file name
   * @throws WorkflowDesignerException when something goes wrong
   */
  public void removeProcessModel(String strProcessModelFileName)
      throws WorkflowDesignerException {
    try {
      Workflow.getProcessModelManager().deleteProcessModelDescriptor(
          strProcessModelFileName);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeProcesModel",
          SilverpeasException.ERROR, "workflowDesigner.EX_REMOVING_PROCESS_DESCRIPTIOR_FAILED",
          e);
    }
  }

  /**
   * An object implementing the ProcessModel interface containing the currently loaded model or null
   * if none.
   */
  public ProcessModel getProcessModel() {
    return m_processModel;
  }

  /**
   * Is it a new Process Model that has not yet been saved to a file?
   */
  public Boolean isNewProcessModel() {
    return Boolean.valueOf(NEW_ELEMENT_NAME.equals(m_strProcessModelFileName));
  }

  /**
   * The relative path and file name of the currently loaded model or null if none.
   */
  public String getProcessFileName() {
    return m_strProcessModelFileName;
  }

  /**
   * The name of the component descriptor that references this process model definition
   * @return the component descriptor file name (without .xml) or <code>null</code> if no components
   * reference this process model.
   */
  public String getComponentDescriptorName() {
    return m_strReferencedInComponent;
  }

  /**
   * Update the header of the cached process model
   * @param processModel the reference object
   */
  public void updateProcessModelHeader(ProcessModel processModel) {
    // Update the current process Model
    //
    m_processModel.setName(processModel.getName());
  }

  /**
   * Create a new columns object to be added to the model
   * @return an object implementing Columns
   */
  public Columns addColumns() {
    Columns columns = m_processModel.getPresentation().createColumns();

    columns.setRoleName(NEW_ELEMENT_NAME);
    return columns;
  }

  /**
   * Update or insert a new columns section of the presentation element of the cached process model
   * @param source the reference object
   * @throws WorkflowDesignerException
   */
  public void updateColumns(Columns source, String strRoleOriginal)
      throws WorkflowDesignerException {
    Columns check = m_processModel.getPresentation().getColumnsByRole(
        source.getRoleName());

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strRoleOriginal)) {
      // If a 'columns' element with the same name as the new element
      // already exists we have a problem...
      //
      if (check != null) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateColumns",
            SilverpeasException.ERROR, "workflowDesigner.EX_COLUMNS_ALREADY_EXISTS");
      }
    } else // Existing object
    {
      // If a 'columns' element with the same name as the element's new name
      // already exists we have a problem...
      //
      if (check != null && !strRoleOriginal.equals(source.getRoleName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateColumns",
            SilverpeasException.ERROR, "workflowDesigner.EX_COLUMNS_ALREADY_EXISTS");
      }

      try {
        m_processModel.getPresentation().deleteColumns(strRoleOriginal);
      } catch (WorkflowException e) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateColumns",
            SilverpeasException.ERROR, "workflowDesigner.EX_UPDATING_COLUMNS_FAILED",
            e);
      }
    }

    m_processModel.getPresentation().addColumns(source);
  }

  /**
   * Removes the 'columns' object specified by the role name
   * @param strRoleName the value of the role attribute
   * @throws WorkflowDesignerException when something goes wrong
   */
  public void deleteColumns(String strRoleName)
      throws WorkflowDesignerException {
    try {
      m_processModel.getPresentation().deleteColumns(strRoleName);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.deleteColumns",
          SilverpeasException.ERROR, "workflowDesigner.EX_DELETING_COLUMNS_FAILED",
          e);
    }
  }

  /**
   * Create a new role object to be added to the model
   * @return an object implementing Role
   */
  public Role createRole() {
    Roles roles = m_processModel.getRolesEx();
    Role role;

    if (roles == null) {
      roles = m_processModel.createRoles();
      m_processModel.setRoles(roles);
    }

    role = roles.createRole();
    role.setName(NEW_ELEMENT_NAME);
    return role;
  }

  /**
   * Update or insert a new role element of the cached process model
   * @param source the data carrier object
   * @param strNameOriginal the original name of the object
   * @throws WorkflowDesignerException if another object with the same name already exists
   */
  public void updateRole(Role source, String strNameOriginal)
      throws WorkflowDesignerException {
    Role check = m_processModel.getRolesEx().getRole(source.getName());

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'role' element's name does not clash with other elements
      // add it to the collection
      //
      if (check == null) {
        m_processModel.getRolesEx().addRole(source);
      } else {
        // If a 'role' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateRole",
            SilverpeasException.ERROR, "workflowDesigner.EX_ROLE_EXISTS");
      }
    } else // Existing object
    {
      // If a 'role' element with the same name as the element's new name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateRole",
            SilverpeasException.ERROR, "workflowDesigner.EX_ROLE_EXISTS");
      }

      // Update the name
      //
      m_processModel.getRolesEx().getRole(strNameOriginal).setName(
          source.getName());
    }
  }

  /**
   * Remove the role specified
   * @param strRoleName the name of the role
   * @throws WorkflowDesignerException if the role cannot be found or is referenced elsewhere.
   */
  public void removeRole(String strRoleName) throws WorkflowDesignerException {
    Map mapDesignations = collectContextualDesignations(), mapQualifiedUsers;
    Iterator iterKeys = mapDesignations.keySet().iterator(), iterDesignation, iterRelatedUser;
    ContextualDesignations designations;
    ContextualDesignation designation;
    QualifiedUsers qualifiedUsers;
    RelatedUser relatedUser;

    if (strRoleName == null) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
          SilverpeasException.ERROR, "workflowDesigner.EX_ROLE_NOT_FOUND");
    }
    // Check if this role is referenced elsewhere ...
    //
    // ... in Contextual Designation ( labels, descriptions, titles, activities)
    //
    while (iterKeys.hasNext()) {
      designations = (ContextualDesignations) iterKeys.next();
      iterDesignation = designations.iterateContextualDesignation();

      while (iterDesignation.hasNext()) {
        designation = (ContextualDesignation) iterDesignation.next();

        if (strRoleName.equals(designation.getRole())) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
              SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
              (String) mapDesignations.get(designations)
              + " ( role: '"
              + strRoleName
              + "'"
              + (designation.getLanguage() == null ? "" : (", lang: '"
              + designation.getLanguage() + "'")) + " )");
        }
      }
    }

    // ... in Columns
    //
    if (m_processModel.getPresentation() != null
        && m_processModel.getPresentation().getColumnsByRole(strRoleName) != null) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
          SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
          "presentation, (columns: '" + strRoleName + "')");
    }

    // ... in Forms
    //
    if (m_processModel.getForms() != null) {
      Iterator<Form> iterForm = m_processModel.getForms().iterateForm();
      while (iterForm.hasNext()) {
        Form form = iterForm.next();
        if (strRoleName.equals(form.getRole())) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
              SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
              "form: '" + form.getName() + "', (role: '" + strRoleName + "')");
        }
      }
    }

    // ... in Qualified users ( role, userInRole, relatedUsers )
    //
    mapQualifiedUsers = collectQualifiedUsers();
    iterKeys = mapQualifiedUsers.keySet().iterator();

    while (iterKeys.hasNext()) {
      qualifiedUsers = (QualifiedUsers) iterKeys.next();

      if (strRoleName.equals(qualifiedUsers.getRole())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
            SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
            (String) mapQualifiedUsers.get(qualifiedUsers) + " (role: '"
            + strRoleName + "')");
      }

      if (qualifiedUsers.getUserInRole(strRoleName) != null) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
            SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
            (String) mapQualifiedUsers.get(qualifiedUsers) + " (userInRole: '"
            + strRoleName + "')");
      }

      // in Related Users
      //
      iterRelatedUser = qualifiedUsers.iterateRelatedUser();

      while (iterRelatedUser.hasNext()) {
        relatedUser = (RelatedUser) iterRelatedUser.next();

        if (strRoleName.equals(relatedUser.getRole())) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
              SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
              (String) mapQualifiedUsers.get(qualifiedUsers)
              + " (relatedUser: ["
              + (relatedUser.getParticipant() == null ? ""
              : (" participant: '"
              + relatedUser.getParticipant().getName() + "'"))
              + (relatedUser.getFolderItem() == null ? ""
              : (" folderItem: '"
              + relatedUser.getFolderItem().getName() + "'"))
              + (relatedUser.getRelation() == null ? "" : (" relation: '"
              + relatedUser.getRelation() + "'")) + " role: '"
              + strRoleName + "'" + " ])");
        }
      }
    }

    try {
      m_processModel.getRolesEx().removeRole(strRoleName);
      m_processModel.setRoles(null);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeRole",
          SilverpeasException.ERROR, "workflowDesigner.EX_ROLE_NOT_FOUND");
    }
  }

  /**
   * Create a new participant object to be added to the model
   * @return an object implementing Participant
   */
  public Participant createParticipant() {
    Participants participants = m_processModel.getParticipantsEx();
    Participant participant;

    if (participants == null) {
      participants = m_processModel.createParticipants();
      m_processModel.setParticipants(participants);
    }

    participant = participants.createParticipant();
    participant.setName(NEW_ELEMENT_NAME);
    return participant;
  }

  /**
   * Update or insert a new participant element of the cached process model
   * @param source the reference object
   * @throws WorkflowDesignerException
   */
  public void updateParticipant(Participant source, String strNameOriginal)
      throws WorkflowDesignerException {
    Participant check = m_processModel.getParticipantsEx().getParticipant(
        source.getName()), participant;

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'participant' element's name does not clash with other
      // elements
      // add it to the collection
      //
      if (check == null) {
        m_processModel.getParticipantsEx().addParticipant(source);
      } else {
        // If a 'participant' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateParticipant",
            SilverpeasException.ERROR, "workflowDesigner.EX_PARTICIPANT_EXISTS");
      }
    } else // Existing object
    {
      // If a 'particiapnt' element with the same name as the element's new name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateParticipant",
            SilverpeasException.ERROR, "workflowDesigner.EX_PARTICIPANT_EXISTS");
      }

      // Update the name and resolved state
      //
      participant = m_processModel.getParticipantsEx().getParticipant(
          strNameOriginal);
      participant.setName(source.getName());
      participant.setResolvedState(source.getResolvedState());
    }
  }

  /**
   * Remove the participant specified
   * @param strParticipantName the name of the participant
   * @throws WorkflowDesignerException if the participant cannot be found or is referenced
   * elsewhere.
   */
  public void removeParticipant(String strParticipantName) throws WorkflowDesignerException {
    Participant reference = m_processModel.getParticipantsEx().getParticipant(strParticipantName);

    if (reference == null) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeParticipant",
          SilverpeasException.ERROR, "workflowDesigner.EX_PARTICIPANT_NOT_FOUND");
    }
    // Check if this participant is referenced elsewhere ...
    //
    // ... in Qualified users ( relatedUsers )
    //
    Map mapQualifiedUsers = collectQualifiedUsers();
    Iterator iterKeys = mapQualifiedUsers.keySet().iterator();

    while (iterKeys.hasNext()) {
      QualifiedUsers qualifiedUsers = (QualifiedUsers) iterKeys.next();

      // in Related Users
      Iterator<RelatedUser> iterRelatedUser = qualifiedUsers.iterateRelatedUser();

      while (iterRelatedUser.hasNext()) {
        RelatedUser relatedUser = iterRelatedUser.next();

        if (reference == relatedUser.getParticipant()) {
          throw new WorkflowDesignerException(
              "WorkflowDesignerSessionController.removeParticipant",
              SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
              (String) mapQualifiedUsers.get(qualifiedUsers) + " (relatedUser: ["
              + (relatedUser.getParticipant() == null ? "" : (" participant: '"
              + relatedUser.getParticipant().getName() + "'"))
              + (relatedUser.getFolderItem() == null ? "" : (" folderItem: '"
              + relatedUser.getFolderItem().getName() + "'"))
              + (relatedUser.getRelation() == null ? "" : (" relation: '"
              + relatedUser.getRelation() + "'"))
              + (relatedUser.getRole() == null ? "" : (" role: '"
              + relatedUser.getRole() + "'")) + " ])");
        }
      }
    }

    try {
      m_processModel.getParticipantsEx().removeParticipant(strParticipantName);     
      m_processModel.setParticipants(null);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeParticipant",
          SilverpeasException.ERROR, "workflowDesigner.EX_PARTICIPANT_NOT_FOUND");
    }
  }

  /**
   * Create a new state object to be added to the model
   * @return an object implementing State
   */
  public State createState() {
    States states = m_processModel.getStatesEx();
    State state;

    if (states == null) {
      states = m_processModel.createStates();
      m_processModel.setStates(states);
    }

    state = states.createState();
    state.setName(NEW_ELEMENT_NAME);
    return state;
  }

  /**
   * Update or insert a new state element of the cached process model
   * @param source the reference object
   * @throws WorkflowDesignerException
   */
  public void updateState(State source, String strNameOriginal)
      throws WorkflowDesignerException {
    State check = m_processModel.getStatesEx().getState(source.getName()), state;

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'state' element's name does not clash with other elements
      // add it to the collection
      //
      if (check == null) {
        m_processModel.getStatesEx().addState(source);
      } else {
        // If a 'state' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateState",
            SilverpeasException.ERROR, "workflowDesigner.EX_STATE_EXISTS");
      }
    } else // Existing object
    {
      // If a 'state' element with the same name as the element's new name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateState",
            SilverpeasException.ERROR, "workflowDesigner.EX_STATE_EXISTS");
      }

      // Update the state attributes
      //
      state = m_processModel.getStatesEx().getState(strNameOriginal);
      state.setName(source.getName());
      state.setTimeoutAction(source.getTimeoutAction());
      state.setTimeoutInterval(source.getTimeoutInterval());
      state.setTimeoutNotifyAdmin(source.getTimeoutNotifyAdmin());
      state.setAllowedActions(source.getAllowedActionsEx());
    }
  }

  /**
   * Remove the state specified
   * @param strStateName the name of the state
   * @throws WorkflowDesignerException if the state cannot be found or is referenced elsewhere.
   */
  public void removeState(String strStateName) throws WorkflowDesignerException {
    Actions actions = m_processModel.getActionsEx();
    Participants participants = m_processModel.getParticipantsEx();

    // Check if this state is not referenced in Actions' consequences
    if (actions != null) {
      Iterator<Action> iterAction = actions.iterateAction();

      while (iterAction.hasNext()) {
        Action action = iterAction.next();

        Consequences consequences = action.getConsequences();

        if (consequences != null) {
          Iterator<Consequence> iterConsequence = consequences.iterateConsequence();

          while (iterConsequence.hasNext()) {
            Consequence consequence = iterConsequence.next();
            if (consequence.getTargetState(strStateName) != null || consequence.getUnsetState(
                strStateName) != null) {
              throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeState",
                  SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED", "action : '"
                  + action.getName() + "' (set/unset state : '" + strStateName + "')");
            }
          }
        }
      }
    }

    // Check if this state is not referenced in Participants
    //
    if (participants != null) {
      Iterator<Participant> iterParticipant = participants.iterateParticipant();

      while (iterParticipant.hasNext()) {
        Participant participant = iterParticipant.next();

        if (participant.getResolvedState().equals(strStateName)) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeState",
              SilverpeasException.ERROR,
              "workflowDesigner.EX_ELEMENT_REFERENCED", "participant : '"
              + participant.getName()
              + "' (resolved state : '" + strStateName + "')");
        }
      }
    }

    try {
      m_processModel.getStatesEx().removeState(strStateName);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeState",
          SilverpeasException.ERROR, "workflowDesigner.EX_STATE_NOT_FOUND");
    }
  }

  /**
   * Update Qualified Users referenced by the context, create the object if it was <code>null</code>
   * before.
   * @param source the new Qualified Users
   * @param strContext the context of the QualifiedUsers being updated
   * @throws WorkflowException when the update goes wrong
   * @throws WorkflowDesignerException when the update goes wrong
   */
  public void updateQualifiedUsers(QualifiedUsers source, String strContext)
      throws WorkflowException, WorkflowDesignerException {
    QualifiedUsers qualifiedUsers = null;
    qualifiedUsers = findQualifiedUsers(strContext);

    if (qualifiedUsers == null) {
      setQualifiedUsers(source, strContext);
    } else {
      // Update the qualified users.
      qualifiedUsers.setRole(source.getRole());
      qualifiedUsers.setMessage(source.getMessage());
      qualifiedUsers.removeUserInRoles();
      Iterator<UserInRole> iter = source.iterateUserInRole();

      while (iter.hasNext()) {
        qualifiedUsers.addUserInRole(iter.next());
      }
    }
  }

  /**
   * Find a related user corresponding to the criteria given
   * @param strContext the context
   * @param strParticipant the name of the participant, may be <code>null</code>
   * @param strFolderItem the name of the data folder item, may be <code>null</code>
   * @param strRelation the relation, may be <code>null</code>
   * @param strRole the name of the role, may be <code>null</code>
   * @return an object implementing RelatedUser
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public RelatedUser findRelatedUser(String strContext, String strParticipant,
      String strFolderItem, String strRelation, String strRole)
      throws WorkflowDesignerException, WorkflowException {
    RelatedUser reference = m_processModel.createRelatedUser();
    QualifiedUsers qualifiedUsers = null;

    if (strContext == null) {
      return null;
    }

    // Initialise the reference object
    //
    if (strParticipant == null) {
      reference.setParticipant(null);
    } else {
      reference.setParticipant(m_processModel.getParticipantsEx().getParticipant(strParticipant));
    }

    if (strFolderItem == null) {
      reference.setFolderItem(null);
    } else {
      reference.setFolderItem(m_processModel.getDataFolder().getItem(
          strFolderItem));
    }

    reference.setRelation(strRelation);
    reference.setRole(strRole);

    // Check the context
    //
    qualifiedUsers = findQualifiedUsers(strContext);
    if (qualifiedUsers != null) {
      return qualifiedUsers.getRelatedUser(reference);
    }

    return null;
  }

  /**
   * Update a related user corresponding to the criteria given
   * @param strContext the context
   * @param strParticipantOriginal the original name of the participant, may be <code>null</code>
   * @param strFolderItemOriginal the original name of the data folder item, may be
   * <code>null</code>
   * @param strRelationOriginal the original relation, may be <code>null</code>
   * @param strRoleOriginal the original name of the role, may be <code>null</code>
   * @return an object implementing RelatedUser
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public void updateRelatedUser(RelatedUser source, String strContext,
      String strParticipantOriginal, String strFolderItemOriginal,
      String strRelationOriginal, String strRoleOriginal)
      throws WorkflowDesignerException, WorkflowException {
    String strParticipant = source.getParticipant() == null ? null : source.getParticipant().getName(), strFolderItem = source.
        getFolderItem() == null ? null
        : source.getFolderItem().getName();
    RelatedUser check = findRelatedUser(strContext, strParticipant,
        strFolderItem, source.getRelation(), source.getRole()), relatedUser, reference;
    QualifiedUsers qualifiedUsers = findQualifiedUsers(strContext);

    // Is it a new object or an existing one?
    if (NEW_ELEMENT_NAME.equals(strRoleOriginal)) {
      // If the new 'relatedUser' element's name does not clash with other
      // elements add it to the collection
      if (check == null) {
        qualifiedUsers.addRelatedUser(source);
      } else {
        // If a 'state' element with the same attributes as the new element
        // already exists we have a problem...
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateRelatedUser",
            SilverpeasException.ERROR, "workflowDesigner.EX_RELATED_USER_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'relatedUser' element with the same attributes as the
      // new element
      // already exists we have a problem...
      // So if an object with the same attributes has been found ('check')
      // it should be the object that is being modified, just that the attribute
      // values did not change.
      // Therefore we compare all the attributes with their initial values.
      //
      if (check != null
          && !((strParticipantOriginal == null && strParticipant == null || strParticipantOriginal != null
          && strParticipantOriginal.equals(strParticipant))
          && (strFolderItemOriginal == null && strFolderItem == null || strFolderItemOriginal != null
          && strFolderItemOriginal.equals(strFolderItem))
          && (strRelationOriginal == null && source.getRelation() == null || strRelationOriginal != null
          && strRelationOriginal.equals(source.getRelation())) && (strRoleOriginal == null
          && source.getRole() == null || strRoleOriginal != null
          && strRoleOriginal.equals(source.getRole())))) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateRelatedUser",
            SilverpeasException.ERROR, "workflowDesigner.EX_RELATED_USER_EXISTS");
      }

      // Update the relatedUser
      //
      reference = m_processModel.createRelatedUser();

      if (strParticipantOriginal != null) {
        reference.setParticipant(m_processModel.getParticipantsEx().getParticipant(
            strParticipantOriginal));
      } else {
        reference.setParticipant(null);
      }

      if (strFolderItemOriginal != null) {
        reference.setFolderItem(m_processModel.getDataFolder().getItem(
            strFolderItemOriginal));
      } else {
        reference.setFolderItem(null);
      }

      reference.setRelation(strRelationOriginal);
      reference.setRole(strRoleOriginal);

      relatedUser = qualifiedUsers.getRelatedUser(reference);
      relatedUser.setParticipant(source.getParticipant());
      relatedUser.setFolderItem(source.getFolderItem());
      relatedUser.setRelation(source.getRelation());
      relatedUser.setRole(source.getRole());
    }
  }

  /**
   * Remove the related user specified
   * @param reference the reference for the related user to remove
   * @param strContext the context
   * @throws WorkflowDesignerException if the related user cannot be found or something goes wrong
   * @throws WorkflowException if the related user cannot be found or when something goes wrong
   */
  public void removeRelatedUser(RelatedUser reference, String strContext)
      throws WorkflowDesignerException, WorkflowException {
    QualifiedUsers qualifiedUsers = findQualifiedUsers(strContext);
    qualifiedUsers.removeRelatedUser(reference);
  }

  /**
   * Create a new action object to be added to the model
   * @return an object implementing Action
   */
  public Action createAction() {
    Actions actions = m_processModel.getActionsEx();
    Action action;

    if (actions == null) {
      actions = m_processModel.createActions();
      m_processModel.setActions(actions);
    }

    action = actions.createAction();
    action.setName(NEW_ELEMENT_NAME);
    return action;
  }

  /**
   * Update or insert a new action element of the cached process model
   * @param source the reference object
   * @param strNameOriginal 
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public void updateAction(Action source, String strNameOriginal)
      throws WorkflowDesignerException, WorkflowException {
    Action check = null, action;

    try {
      check = m_processModel.getActionsEx().getAction(source.getName());
    } catch (WorkflowException e) {
      // We expect an exception when the action has not been found
      //
      if (!"WorkflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL".equals(e.getMessage())) {
        throw e;
      }
    }

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'action' element's name does not clash with other elements
      // add it to the collection
      //
      if (check == null) {
        m_processModel.getActionsEx().addAction(source);
      } else {
        // If a 'action' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateAction",
            SilverpeasException.ERROR, "workflowDesigner.EX_ACTION_EXISTS");
      }
    } else // Existing object
    {
      // If a 'action' element with the same name as the element's new name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateAction",
            SilverpeasException.ERROR, "workflowDesigner.EX_ACTION_EXISTS");
      }

      // Update the action attributes
      //
      action = m_processModel.getActionsEx().getAction(strNameOriginal);
      action.setName(source.getName());
      action.setForm(source.getForm());
      action.setKind(source.getKind());
    }
  }

  /**
   * Remove the action specified
   * @param strActionName the name of the action
   * @throws WorkflowException if the action cannot be found
   * @throws WorkflowDesignerException if the action is referenced elsewhere
   */
  public void removeAction(String strActionName) throws WorkflowException, WorkflowDesignerException {
    States states = m_processModel.getStatesEx();

    // check if this action is referenced in states
    //
    if (states != null) {
      Iterator<State> iterState = states.iterateState();
      while (iterState.hasNext()) {
        State state = iterState.next();

        // Might be a timeout action
        //
        if (state.getTimeoutAction() != null
            && state.getTimeoutAction().getName().equals(strActionName)) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeAction",
              SilverpeasException.ERROR,
              "workflowDesigner.EX_ELEMENT_REFERENCED", "state : '"
              + state.getName()
              + "' (timeoutAction : '" + strActionName + "')");
        }
        AllowedActions allowedActions = state.getAllowedActionsEx();

        // or it might be an allowed action
        //
        if (allowedActions != null) {
          if (allowedActions.getAllowedAction(strActionName) != null) {
            throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeAction",
                SilverpeasException.ERROR,
                "workflowDesigner.EX_ELEMENT_REFERENCED", "state : '"
                + state.getName()
                + "' (allowedAction : '" + strActionName + "')");
          }
        }
      }
    }

    m_processModel.getActionsEx().removeAction(strActionName);

    // Was this the last action defined?
    //
    if (!m_processModel.getActionsEx().iterateAction().hasNext()) {
      m_processModel.setActions(null);
    }
  }

  /**
   * Update or insert a new consequence element of the cached process model
   * @param source the reference object
   * @param strContext the context of Consequence
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public void updateConsequence(Consequence source, String strContext)
      throws WorkflowDesignerException, WorkflowException {
    Action action = findAction(strContext);
    Consequences consequences = action.getConsequences();
    int idxCheck = -1, idxConsequence = Integer.parseInt(strContext.substring(strContext.lastIndexOf(
        '/') + 1));

    // If it's a new object the 'Consequences' may not yet exits
    //
    if (consequences == null) {
      consequences = action.createConsequences();
      action.setConsequences(consequences);
    } else {
      // Check if identical consequence exists
      idxCheck = consequences.getConsequenceList().indexOf(source);
    }

    // Is it a new object or an existing one?
    //
    if (consequences.getConsequenceList().size() == idxConsequence) {
      // If the new 'consequence' element's condition does not clash with other
      // elements
      // add it to the collection
      //
      if (idxCheck < 0) {
        consequences.addConsequence(source);
      } else {
        // If a 'consequence' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateConsequence",
            SilverpeasException.ERROR, "workflowDesigner.EX_CONSEQUENCE_EXISTS");
      }
    } else // Existing object
    {
      Consequence consequence; // the consequence being updated

      // If a different 'consequence' element with the same attributes
      // already exists we have a problem...
      //
      if (idxCheck >= 0 && idxCheck != idxConsequence) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateConsequence",
            SilverpeasException.ERROR, "workflowDesigner.EX_CONSEQUENCE_EXISTS");
      }

      // Update the consequence attributes
      // get the consequence by number, replace it with the source one, updating
      // the source...
      //
      consequence = consequences.getConsequenceList().set(idxConsequence, source);
      source.setNotifiedUsers(consequence.getNotifiedUsers());
    }
  }

  /**
   * Move the consequence specified by the context inside the collection
   * @param strContext the context of the consequence
   * @param iConsequence the current index of the consequence in the collection
   * @param nDirection the offset and the direction to move by
   * @throws WorkflowDesignerException if the consequence cannot be found.
   */
  public void moveConsequence(String strContext, int iConsequence, int nDirection) throws
      WorkflowDesignerException {
    try {
      Action action = findAction(strContext);
      Consequences consequences = action.getConsequences();

      // Move the consequence within the consequences collection
      //
      if (consequences != null) {
        Consequence consequence = consequences.getConsequenceList().remove(iConsequence);
        consequences.getConsequenceList().add(iConsequence + nDirection,
            consequence);
      }
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.moveConsequence",
          SilverpeasException.ERROR, "workflowDesigner.EX_CONSEQUENCE_NOT_FOUND");
    }
  }

  /**
   * Remove the consequence specified by the context
   * @param strContext the context of the consequence
   * @throws WorkflowDesignerException if the consequence cannot be found.
   */
  public void removeConsequence(String strContext)
      throws WorkflowDesignerException {
    try {
      Action action = findAction(strContext);
      Consequences consequences = action.getConsequences();
      int idxConsequence = Integer.parseInt(strContext.substring(strContext.lastIndexOf('/') + 1));
      // If it is the last consequence to be removed, remove the consequences
      // object as well...
      if (consequences.getConsequenceList().size() == 1) {
        action.setConsequences(null);
      } else {
        consequences.getConsequenceList().remove(idxConsequence);
      }
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeConsequence",
          SilverpeasException.ERROR, "workflowDesigner.EX_CONSEQUENCE_NOT_FOUND");
    }
  }

  /**
   * Create a new form object to be added to the model
   * @return an object implementing Form
   */
  public Form createForm() {
    Forms forms = m_processModel.getForms();
    Form form;

    if (forms == null) {
      forms = m_processModel.createForms();
      m_processModel.setForms(forms);
    }

    form = forms.createForm();
    form.setName(NEW_ELEMENT_NAME);
    return form;
  }

  /**
   * Find the form by context
   * @param strContext the context
   * @return a Form object
   */
  public Form findForm(String strContext) {
    String[] astrElements;
    String strRoleName;
    Form form = null;

    if (strContext != null) {
      astrElements = strContext.split("[,/\\[\\]]", -2);

      try {
        if (FORMS.equals(astrElements[0])) {
          if ("".equals(astrElements[2])) {
            strRoleName = null;
          } else {
            strRoleName = astrElements[2];
          }

          form = m_processModel.getForms().getForm(astrElements[1], strRoleName);
        }
      } catch (RuntimeException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      }
    }
    return form;
  }

  /**
   * Update Form referenced by the context.
   * @param source the object carrying the new values
   * @param strContext the context of the form being updated
   * @param strNameOriginal the original name of the form
   * @throws WorkflowException when the update goes wrong
   * @throws WorkflowDesignerException when the update goes wrong
   */
  public void updateForm(Form source, String strContext,
      String strNameOriginal, String strRoleOriginal) throws WorkflowException,
      WorkflowDesignerException {
    Form form, check = null;

    check = m_processModel.getForms().getForm(source.getName(),
        source.getRole());

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'Form' element does not clash with other elements
      // with the same name add it to the collection
      //
      if (check == null // or CHECK not really identical to source
          || (source.getRole() != null && !source.getRole().equals(
          check.getRole()))) {
        // add the object to the collection;
        //
        m_processModel.getForms().addForm(source);
      } else {
        // If an 'Form' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateForm",
            SilverpeasException.ERROR, "workflowDesigner.EX_FORM_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'Form' element with the same name and role as the
      // element's new name
      // already exists we have a problem...
      // So if an object with the same attributes has been found ('check')
      // it should be the object that is being modified, just that the attribute
      // values did not change.
      // Therefore we compare all the attributes with their initial values.
      //
      if (check != null && (check.getRole() == null && source.getRole() == null // the
          // "check's"
          // name
          // role
          // &
          // role
          // are
          // really
          // ...
          || source.getRole() != null && source.getRole().equals(check.getRole())) // ...
          // the
          // same
          // as
          // "source's";
          && !(strNameOriginal.equals(source.getName()) // name & role didn't
          // change
          && (strRoleOriginal == null && source.getRole() == null || strRoleOriginal != null
          && strRoleOriginal.equals(source.getRole())))) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateForm",
            SilverpeasException.ERROR, "workflowDesigner.EX_FORM_EXISTS");
      }

      // Update the form.
      //
      form = findForm(strContext);
      form.setName(source.getName());
      form.setRole(source.getRole());
      form.setHTMLFileName(source.getHTMLFileName());
    }
  }

  /**
   * Remove the form described by the context
   * @param strContext the context
   * @throws WorkflowException if the form cannot be found
   * @throws WorkflowDesignerException if the form is referenced elsewhere
   */
  public void removeForm(String strContext) throws WorkflowException,
      WorkflowDesignerException {
    String[] astrElements;
    String strRoleName;

    if (strContext != null) {
      astrElements = strContext.split("[,/\\[\\]]", -2);

      try {
        if (FORMS.equals(astrElements[0])) {
          if ("".equals(astrElements[2])) {
            strRoleName = null;
          } else {
            strRoleName = astrElements[2];
          }

          // Check if the form is no referenced in actions
          //
          if (m_processModel.getActionsEx() != null) {
            Action action;
            Iterator iterAction = m_processModel.getActionsEx().iterateAction();

            while (iterAction.hasNext()) {
              action = (Action) iterAction.next();

              if (action.getForm() != null
                  && astrElements[1].equals(action.getForm().getName())) {
                throw new WorkflowDesignerException(
                    "WorkflowDesignerSessionController.removeForm()",
                    SilverpeasException.ERROR,
                    "workflowDesigner.EX_ELEMENT_REFERENCED", "action: '"
                    + action.getName() + "' : form");
              }
            }
          }

          m_processModel.getForms().removeForm(astrElements[1], strRoleName);

          // Was this the last form?
          //
          if (!m_processModel.getForms().iterateForm().hasNext()) {
            m_processModel.setForms(null);
          }
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        // Thrown when no token was found where expected
        // re-throw an exception
        //
        throw new WorkflowException("WorkflowDesignerSessionController.removeForm",
            SilverpeasException.ERROR, "workflowEngine.EX_FORM_NOT_FOUND", e);
      }
    }
  }

  /**
   * Find the input by context
   * @param strContext the context
   * @return a Input object
   */
  public Input findInput(String strContext) {
    String[] astrElements;
    Input input = null;
    Form form = findForm(strContext);

    if (form != null) {
      astrElements = strContext.split("[,/\\[\\]]", -2);

      try {
        if (astrElements[5].length() > 0) {
          input = form.getInput(Integer.parseInt(astrElements[5]));
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      } catch (NumberFormatException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      }
    }
    return input;
  }

  /**
   * Update or insert a new input element of the cached process model
   * @param source the reference object
   * @param strContext the context of Input
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public void updateInput(Input source, String strContext)
      throws WorkflowDesignerException, WorkflowException {
    Form form = findForm(strContext);
    Input check;
    Input input = findInput(strContext); // the input being updated

    // Check if identical input exists
    //
    check = form.getInput(source);

    // Is it a new object or an existing one?
    //
    if (input == null) {
      // If the new 'input' element's is not identical with other element
      // add it to the collection
      //
      if (check == null) {
        form.addInput(source);
      } else {
        // If a 'input' element with the same item/value as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateInput",
            SilverpeasException.ERROR, "workflowDesigner.EX_INPUT_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'input' element with the same attributes
      // already exists we have a problem...
      //
      if (check != null && check != input) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateInput",
            SilverpeasException.ERROR, "workflowDesigner.EX_INPUT_EXISTS");
      }

      // Update the input attributes
      //
      input.setDisplayerName(source.getDisplayerName());
      input.setItem(source.getItem());
      input.setMandatory(source.isMandatory());
      input.setReadonly(source.isReadonly());
      input.setValue(source.getValue());
    }
  }

  /**
   * Remove the input specified by the context
   * @param strContext the context of the input
   * @throws WorkflowDesignerException if the input cannot be found.
   * @throws WorkflowException if the input cannot be found
   */
  public void removeInput(String strContext) throws WorkflowDesignerException,
      WorkflowException {
    Form form = findForm(strContext);
    String[] astrElements;

    if (form != null) {
      astrElements = strContext.split("[,/\\[\\]]", -2);

      try {
        if (astrElements[5].length() > 0) {
          form.removeInput(Integer.parseInt(astrElements[5]));
          return;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        // Thrown when no token was found where expected
        //
      } catch (NumberFormatException e) {
        // Thrown when the token could not have been interpreted
        //
      }
    }

    throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeInput",
        SilverpeasException.ERROR, "workflowDesigner.EX_INPUT_NOT_FOUND");
  }

  /**
   * Create a new ContextualDesignation object to be added to the model
   * @return an object implementing ContextualDesignation
   */
  public ContextualDesignation createDesignation() {
    ContextualDesignation designation = m_processModel.createDesignation();

    designation.setLanguage(NEW_ELEMENT_NAME);
    return designation;
  }

  /**
   * Update or insert a new ContextualDesignation element of the cached process model
   * @param strContext
   * @param source the reference object
   * @param strLangOriginal
   * @param strRoleOriginal
   * @throws WorkflowDesignerException
   */
  public void updateContextualDesignations(String strContext,
      ContextualDesignation source, String strLangOriginal,
      String strRoleOriginal) throws WorkflowDesignerException {
    ContextualDesignations designations = findContextualDesignations(strContext);
    ContextualDesignation check = designations.getSpecificLabel(source.getRole(),
        source.getLanguage());

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strLangOriginal)) {
      // If the new 'ContextualDesignation' element does not clash with other
      // elements
      // with the same role & language add it to the collection
      //
      if (check == null) {
        designations.addContextualDesignation(source);
      } else {
        // If a 'ContextualDesignation' element with the same role & language as
        // the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.updateContextualDesignations",
            SilverpeasException.ERROR, "workflowDesigner.EX_CONTEXTUAL_DESIGNATION_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'ContextualDesignation' element with the same role &
      // language as the element's new name
      // already exists we have a problem...
      //
      if (check != null
          && !(strLangOriginal.equals(source.getLanguage()) && strRoleOriginal.equals(
          source.getRole()))) {
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.updateContextualDesignations",
            SilverpeasException.ERROR, "workflowDesigner.EX_CONTEXTUAL_DESIGNATION_EXISTS");
      }

      // Update the contextual designation
      //
      check = designations.getSpecificLabel(strRoleOriginal, strLangOriginal);
      check.setRole(source.getRole());
      check.setLanguage(source.getLanguage());
      check.setContent(source.getContent());
    }
  }

  /**
   * Finds the contextual designation with the given attributes in the specified context
   * @param strContext the context of the designation
   * @param strLanguage the language
   * @param strRole the role name
   * @return contextual designation of the given role & name or <code>null</code>
   * @throws WorkflowDesignerException if something goes wrong
   */
  public ContextualDesignation findContextualDesignation(String strContext,
      String strRole, String strLanguage) throws WorkflowDesignerException {
    return findContextualDesignations(strContext).getSpecificLabel(strRole,
        strLanguage);
  }

  /**
   * Removes the contextual designation with the attributes as the reference object in the specified
   * context
   * @param strContext the context of the designation
   * @param contextualDesignation the reference object
   * @throws WorkflowDesignerException when something goes wrong e.g. designation not found
   */
  public void removeContextualDesignation(String strContext,
      ContextualDesignation contextualDesignation)
      throws WorkflowDesignerException {
    try {
      findContextualDesignations(strContext).removeContextualDesignation(
          contextualDesignation);
    } catch (WorkflowException e) {
      throw new WorkflowDesignerException(
          "WorkflowDesignerSessionController.removeContextualDesignation",
          SilverpeasException.ERROR, "workflowDesigner.EX_CONTEXTUAL_DESIGNATION_NOT_FOUND");
    }
  }

  /**
   * Finds the contextual designations object in the specified context
   * @param strContext the context of the designations
   * @return ContextualDesignations collection or <code>null</code> if nothing found
   * @throws WorkflowDesignerException when something goes wrong
   */
  private ContextualDesignations findContextualDesignations(String strContext)
      throws WorkflowDesignerException {
    StringTokenizer strtok;
    String strElement;
    ContextualDesignations designations = null;

    if (strContext != null) {
      try {
        strtok = new StringTokenizer(strContext, "/[]@'=,");

        if (strtok.hasMoreTokens()) {
          strElement = strtok.nextToken();

          if (LABELS.equals(strElement)) {
            designations = m_processModel.getLabels();
          } else if (DESCRIPTIONS.equals(strElement)) {
            designations = m_processModel.getDescriptions();
          } else if (ROLES.equals(strElement)) {
            Role role;
            strElement = strtok.nextToken();
            role = m_processModel.getRolesEx().getRole(strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = role.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = role.getDescriptions();
            }
          } else if (PRESENTATION.equals(strElement)) {
            strElement = strtok.nextToken();

            if (TITLES.equals(strElement)) {
              designations = m_processModel.getPresentation().getTitles();
            }
          } else if (PARTICIPANTS.equals(strElement)) {
            Participant participant;
            strElement = strtok.nextToken();
            participant = m_processModel.getParticipantsEx().getParticipant(
                strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = participant.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = participant.getDescriptions();
            }
          } else if (STATES.equals(strElement)) {
            State state;
            strElement = strtok.nextToken();
            state = m_processModel.getStatesEx().getState(strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = state.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = state.getDescriptions();
            } else if (ACTIVITIES.equals(strElement)) {
              designations = state.getActivities();
            }
          } else if (ACTIONS.equals(strElement)) {
            Action action;
            strElement = strtok.nextToken();
            action = m_processModel.getActionsEx().getAction(strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = action.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = action.getDescriptions();
            }
          } else if (USER_INFOS.equals(strElement)) {
            Item item;
            strElement = strtok.nextToken();
            item = m_processModel.getUserInfos().getItem(strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = item.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = item.getDescriptions();
            }
          } else if (DATA_FOLDER.equals(strElement)) {
            Item item;
            strElement = strtok.nextToken();
            item = m_processModel.getDataFolder().getItem(strElement);
            strElement = strtok.nextToken();

            if (LABELS.equals(strElement)) {
              designations = item.getLabels();
            } else if (DESCRIPTIONS.equals(strElement)) {
              designations = item.getDescriptions();
            }
          } else if (FORMS.equals(strElement)) {
            Form form;
            String strFormName;

            strFormName = strtok.nextToken();
            strElement = strtok.nextToken();

            // If the next token is not a keyword, must be the optional role
            // name
            // eg. forms[form-name,role-name]/titles
            //
            if (!TITLES.equals(strElement) && !INPUTS.equals(strElement)) {
              form = m_processModel.getForms().getForm(strFormName, strElement);
            } else {
              form = m_processModel.getForms().getForm(strFormName);
            }

            if (TITLES.equals(strElement)) {
              designations = form.getTitles();
            } else if (INPUTS.equals(strElement)) {
              Input input;
              strElement = strtok.nextToken();
              input = form.getInputs()[Integer.parseInt(strElement)];

              if (LABELS.equals(strElement)) {
                designations = input.getLabels();
              }
            }

          }
        }
      } catch (RuntimeException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      } catch (WorkflowException e) {
        // Throw when no action has been found,
        // do nothing, just return null...
      }
    }

    if (designations == null) {
      throw new WorkflowDesignerException(
          "WorkflowDesignerSessionController.getContextualDesignations",
          SilverpeasException.ERROR, "workflowDesigner.EX_CONTEXTUAL_DESIGNATIONS_NOT_FOUND");
    }
    return designations;
  }

  /**
   * Collect all the object of the type ContextualDesignations instantiated in the Process Model
   * @return a map, where the key is the reference to the object and the value is a textual
   * description of the object location
   */
  private Map<ContextualDesignations, String> collectContextualDesignations() {
    Map<ContextualDesignations, String> map = new IdentityHashMap<ContextualDesignations, String>();
    Roles roles = m_processModel.getRolesEx();

    // Process Model Labels & Descriptions
    //
    map.put(m_processModel.getDescriptions(), "processModel: " + DESCRIPTIONS);
    map.put(m_processModel.getLabels(), "processModel: " + LABELS);

    // Role Labels & Descriptions
    //
    if (roles != null) {
      Iterator<Role> iterRole = roles.iterateRole();
      while (iterRole.hasNext()) {
        Role role = iterRole.next();
        map.put(role.getDescriptions(), "role: '" + role.getName() + "' : description");
        map.put(role.getLabels(), "role: '" + role.getName() + "' : label");
      }
    }

    // Presentation - titles
    //
    if (m_processModel.getPresentation() != null) {
      map.put(m_processModel.getPresentation().getTitles(),
          WorkflowDesignerSessionController.PRESENTATION + ": title");
    }

    // Participants
    //
    if (m_processModel.getParticipantsEx() != null) {
      Iterator<Participant> iterParticipant = m_processModel.getParticipantsEx().iterateParticipant();

      while (iterParticipant.hasNext()) {
        Participant participant = iterParticipant.next();
        map.put(participant.getDescriptions(), "participant: '"
            + participant.getName() + "' : description");
        map.put(participant.getLabels(), "participant: '"
            + participant.getName() + "' : label");
      }
    }

    // States
    //
    if (m_processModel.getStatesEx() != null) {
      Iterator<State> iterState = m_processModel.getStatesEx().iterateState();
      while (iterState.hasNext()) {
        State state = iterState.next();
        map.put(state.getDescriptions(), "state: '" + state.getName() + "' : description");
        map.put(state.getLabels(), "state: '" + state.getName() + "' : label");
        map.put(state.getActivities(), "state: '" + state.getName() + "' : activity");
      }
    }

    // Actions
    //
    if (m_processModel.getActionsEx() != null) {
      Iterator<Action> iterAction = m_processModel.getActionsEx().iterateAction();
      while (iterAction.hasNext()) {
        Action action = iterAction.next();
        map.put(action.getDescriptions(), "action: '" + action.getName() + "' : description");
        map.put(action.getLabels(), "action: '" + action.getName() + "' : label");
      }
    }

    // UserInfos
    //
    if (m_processModel.getUserInfos() != null) {
      Iterator<Item> iterItem = m_processModel.getUserInfos().iterateItem();
      while (iterItem.hasNext()) {
        Item item = iterItem.next();
        map.put(item.getDescriptions(), "userInfos item: '" + item.getName() + "' : description");
        map.put(item.getLabels(), "userInfos item: '" + item.getName() + "' : label");
      }
    }

    // DataFolder
    //
    if (m_processModel.getDataFolder() != null) {
      Iterator<Item> iterItem = m_processModel.getDataFolder().iterateItem();
      while (iterItem.hasNext()) {        
      Item item =  iterItem.next();
        map.put(item.getDescriptions(), "dataFolder item: '" + item.getName()
            + "' : description");
        map.put(item.getLabels(), "dataFolder item: '" + item.getName() + "' : label");
      }
    }

    // Forms
    //
    if (m_processModel.getForms() != null) {
      Iterator<Form> iterForm = m_processModel.getForms().iterateForm(), iterInput;    
      while (iterForm.hasNext()) {
        Form form = iterForm.next();
        String strFormId;
        if (form.getRole() == null) {
          strFormId = "form: '" + form.getName() + "'";
        } else {
          strFormId = "form [ name: '" + form.getName() + "', role: '" + form.getRole() + "' ]";
        }

        map.put(form.getTitles(), strFormId + " : title");

        // Inputs
        //
        iterInput = form.iterateInput();

        while (iterInput.hasNext()) {
          Input input = (Input) iterInput.next();
          map.put(input.getLabels(), strFormId
              + " : input"
              + (input.getItem() == null ? "" : (" [ item: '"
              + input.getItem().getName() + "' ] : label"))
              + (input.getValue() == null ? "" : (" [ value: '"
              + input.getValue() + "' ] : label")));
        }
      }
    }

    return map;
  }

  /**
   * Create a new item object to be added to the model
   * @return an object implementing Item
   */
  public Item createItem(String strContext) {
    String[] astrElements;
    Item item = null;

    if (strContext != null) {
      astrElements = strContext.split("[,/\\[\\]]");

      if (USER_INFOS.equals(astrElements[0])) {
        DataFolder items = m_processModel.getUserInfos();

        if (items == null) {
          items = m_processModel.createDataFolder();
          m_processModel.setUserInfos(items);
        }
        item = items.createItem();
        item.setName(NEW_ELEMENT_NAME);
      } else if (DATA_FOLDER.equals(astrElements[0])) {
        DataFolder items = m_processModel.getDataFolder();

        if (items == null) {
          items = m_processModel.createDataFolder();
          m_processModel.setDataFolder(items);
        }
        item = items.createItem();
        item.setName(NEW_ELEMENT_NAME);
      }
    }
    return item;
  }

  /**
   * Find the Item specified by the context
   * @param strConxtext the context
   * @return an Item object or <code>null</code>
   */
  public Item findItem(String strContext) {
    StringTokenizer strtok;
    String strElement;
    Item item = null;

    if (strContext != null) {
      strtok = new StringTokenizer(strContext, CONTEXT_DELIMS);

      try {
        strElement = strtok.nextToken();

        if (DATA_FOLDER.equals(strElement)) {
          strElement = strtok.nextToken();
          item = m_processModel.getDataFolder().getItem(strElement);
        } else if (USER_INFOS.equals(strElement)) {
          strElement = strtok.nextToken();
          item = m_processModel.getUserInfos().getItem(strElement);
        }
      } catch (NoSuchElementException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      }
    }
    return item;
  }

  /**
   * Update Item referenced by the context.
   * @param source the object carrying the new values
   * @param strContext the context of the item being updated
   * @param strNameOriginal the original name of the item
   * @throws WorkflowException when the update goes wrong
   * @throws WorkflowDesignerException when the update goes wrong
   */
  public void updateItem(Item source, String strContext, String strNameOriginal)
      throws WorkflowException, WorkflowDesignerException {
    Item item, check = null;
    String strCollection = "";
    int idx = strContext.indexOf("/");

    if (idx > 0) {
      strCollection = strContext.substring(0, idx);
    }

    // Look for an object with the same name...
    //
    if (DATA_FOLDER.equals(strCollection)) {
      check = m_processModel.getDataFolder().getItem(source.getName());
    } else if (USER_INFOS.equals(strCollection)) {
      check = m_processModel.getUserInfos().getItem(source.getName());
    }

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'Item' element does not clash with other elements
      // with the same name add it to the collection
      //
      if (check == null) {
        // add the object to the appropriate collection;
        //
        if (DATA_FOLDER.equals(strCollection)) {
          m_processModel.getDataFolder().addItem(source);
        } else if (USER_INFOS.equals(strCollection)) {
          m_processModel.getUserInfos().addItem(source);
        }
      } else {
        // If an 'Item' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateItem",
            SilverpeasException.ERROR, "workflowDesigner.EX_ITEM_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'Item' element with the same name as the element's new
      // name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateItem",
            SilverpeasException.ERROR, "workflowDesigner.EX_ITEM_EXISTS");
      }

      // Update the item.
      //
      item = findItem(strContext);
      item.setName(source.getName());
      item.setComputed(source.isComputed());
      item.setFormula(source.getFormula());
      item.setMapTo(source.getMapTo());
      item.setReadonly(source.isReadonly());
      item.setType(source.getType());
    }
  }

  /**
   * Remove the item described by the context
   * @param strContext the context
   * @throws WorkflowException when the item cannot be found
   * @throws WorkflowDesignerException when the item is referenced elsewhere
   */
  public void removeItem(String strContext) throws WorkflowException,
      WorkflowDesignerException {
    StringTokenizer strtok;
    String strElement, strItemName = null;
    DataFolder items;
    Map mapQualifiedUsers;
    Iterator iterKeys, iterRelatedUser;
    QualifiedUsers qualifiedUsers;
    RelatedUser relatedUser;

    if (strContext == null) {
      throw new WorkflowException("WorkflowDesignerSessionController.removeItem()",
          "workflowEngine.EX_ITEM_NOT_FOUND"); // $NON-NLS-1$
    }
    strtok = new StringTokenizer(strContext, CONTEXT_DELIMS);

    // Determine the item name and the collection to remove from
    //
    try {
      strElement = strtok.nextToken();

      if (DATA_FOLDER.equals(strElement)) {
        strItemName = strtok.nextToken();
        items = m_processModel.getDataFolder();
      } else if (USER_INFOS.equals(strElement)) {
        strItemName = strtok.nextToken();
        items = m_processModel.getUserInfos();
      } else {
        throw new WorkflowException("WorkflowDesignerSessionController.removeItem()",
            "workflowEngine.EX_ITEM_NOT_FOUND", // $NON-NLS-1$
            strItemName == null ? "<null>"
            : strItemName);
      }
    } catch (NoSuchElementException e) {
      throw new WorkflowException("WorkflowDesignerSessionController.removeItem()",
          "workflowEngine.EX_ITEM_NOT_FOUND", e); // $NON-NLS-1$
    }

    // check if the item is not referenced elsewhere...
    //
    // ... if it is a UserInfos item...
    //
    if (items == m_processModel.getUserInfos()) {
      // ... in UserInfos
      //
      if (m_processModel.getUserInfos() != null) {
        Item item;
        Iterator iterItem = m_processModel.getUserInfos().iterateItem();

        while (iterItem.hasNext()) {
          item = (Item) iterItem.next();

          if (strItemName.equals(item.getMapTo())
              && !strItemName.equals(item.getName())) {
            throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem()",
                SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
                "userInfos item: '" + item.getName() + "' : mapTo");
          }
        }
      }

      // ... in DataFolder
      //
      if (m_processModel.getDataFolder() != null) {
        Item item;
        Iterator iterItem = m_processModel.getDataFolder().iterateItem();

        while (iterItem.hasNext()) {
          item = (Item) iterItem.next();

          if (strItemName.equals(item.getMapTo())) {
            throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem()",
                SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
                "dataFolder item: '" + item.getName() + "' : mapTo");
          }
        }
      }
    } else if (items == m_processModel.getDataFolder()) {
      // ... if it is a dataFolder item ...
      //
      // ... in Columns
      //
      if (m_processModel.getPresentation() != null) {
        Iterator iterColumns = m_processModel.getPresentation().iterateColumns();
        Columns columns;

        while (iterColumns.hasNext()) {
          columns = (Columns) iterColumns.next();

          if (columns.getColumn(strItemName) != null) {
            throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem()",
                SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
                "presentation, (columns: '" + columns.getRoleName() + "')");
          }
        }
      }

      // ... in Consequence
      //
      // Check if this state is not referenced in Actions' consequences
      //
      if (m_processModel.getActionsEx() != null) {
        Action action;
        Consequences consequences;
        Iterator iterAction = m_processModel.getActionsEx().iterateAction();

        while (iterAction.hasNext()) {
          action = (Action) iterAction.next();

          consequences = action.getConsequences();

          if (consequences != null) {
            Consequence consequence;
            Iterator iterConsequence = consequences.iterateConsequence();

            while (iterConsequence.hasNext()) {
              consequence = (Consequence) iterConsequence.next();

              if (strItemName.equals(consequence.getItem())) {
                throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem",
                    SilverpeasException.ERROR,
                    "workflowDesigner.EX_ELEMENT_REFERENCED", "action : '"
                    + action.getName() + "' (item : '" + strItemName + "')");
              }
            }
          }
        }
      }

      // ... in Input (in Forms)
      //
      if (m_processModel.getForms() != null) {
        Iterator iterForm = m_processModel.getForms().iterateForm(), iterInput;
        Form form;
        Input input;
        String strFormId;

        while (iterForm.hasNext()) {
          form = (Form) iterForm.next();
          if (form.getRole() == null) {
            strFormId = "form: '" + form.getName() + "'";
          } else {
            strFormId = "form [ name: '" + form.getName() + "', role: '"
                + form.getRole() + "' ]";
          }

          // Inputs
          //
          iterInput = form.iterateInput();

          while (iterInput.hasNext()) {
            input = (Input) iterInput.next();

            if (input.getItem() != null
                && strItemName.equals(input.getItem().getName())) {
              throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem",
                  SilverpeasException.ERROR,
                  "workflowDesigner.EX_ELEMENT_REFERENCED", strFormId
                  + " : input" + " [ item: '" + strItemName + "' ]");
            }
          }
        }
      }
    }

    // ... and in both cases in relatedUsers ( in Qualified users )
    //
    mapQualifiedUsers = collectQualifiedUsers();
    iterKeys = mapQualifiedUsers.keySet().iterator();

    while (iterKeys.hasNext()) {
      qualifiedUsers = (QualifiedUsers) iterKeys.next();

      // in Related Users
      //
      iterRelatedUser = qualifiedUsers.iterateRelatedUser();

      while (iterRelatedUser.hasNext()) {
        relatedUser = (RelatedUser) iterRelatedUser.next();

        // if it is a dataFolder item check relatedUsers' folder Item
        // If it is a userInfos item check relatedUsers' relation
        //
        if ((items == m_processModel.getDataFolder()
            && relatedUser.getFolderItem() != null && strItemName.equals(relatedUser.getFolderItem().
            getName()))
            || (items == m_processModel.getUserInfos() && strItemName.equals(
            relatedUser.getRelation()))) {
          throw new WorkflowDesignerException("WorkflowDesignerSessionController.removeItem()",
              SilverpeasException.ERROR, "workflowDesigner.EX_ELEMENT_REFERENCED",
              (String) mapQualifiedUsers.get(qualifiedUsers)
              + " (relatedUser: ["
              + (relatedUser.getParticipant() == null ? ""
              : (" participant: '"
              + relatedUser.getParticipant().getName() + "'"))
              + (relatedUser.getFolderItem() == null ? ""
              : (" folderItem: '"
              + relatedUser.getFolderItem().getName() + "'"))
              + (relatedUser.getRelation() == null ? "" : (" relation: '"
              + relatedUser.getRelation() + "'"))
              + (relatedUser.getRole() == null ? "" : (" role: '"
              + relatedUser.getRole() + "'")) + " ])");
        }
      }
    }

    items.removeItem(strItemName);

    // Was this the last item?
    //
    if (!items.iterateItem().hasNext()) {
      m_processModel.setUserInfos(null);
    }
  }

  /**
   * Update Parameter referenced by the context and parameter name.
   * @param source the object carrying the new values
   * @param strContext the context of the item being updated
   * @param strNameOriginal the original name of the parameter
   * @throws WorkflowException when the update goes wrong
   * @throws WorkflowDesignerException when the update goes wrong
   */
  public void updateParameter(Parameter source, String strContext,
      String strNameOriginal) throws WorkflowException,
      WorkflowDesignerException {
    Item item = findItem(strContext);
    Parameter parameter, check = null;

    // Look for an object with the same name...
    //
    check = item.getParameter(source.getName());

    // Is it a new object or an existing one?
    //
    if (NEW_ELEMENT_NAME.equals(strNameOriginal)) {
      // If the new 'Parameter' element does not clash with other elements
      // with the same name add it to the collection
      //
      if (check == null) {
        item.addParameter(source);
      } else {
        // If a 'Parameter' element with the same name as the new element
        // already exists we have a problem...
        //
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateParameter",
            SilverpeasException.ERROR, "workflowDesigner.EX_PARAMETER_EXISTS");
      }
    } else // Existing object
    {
      // If a different 'Parameter' element with the same name as the element's
      // new name
      // already exists we have a problem...
      //
      if (check != null && !strNameOriginal.equals(source.getName())) {
        throw new WorkflowDesignerException("WorkflowDesignerSessionController.updateParameter",
            SilverpeasException.ERROR, "workflowDesigner.EX_PARAMETER_EXISTS");
      }

      // Update the parameter.
      //
      parameter = item.getParameter(strNameOriginal);
      parameter.setName(source.getName());
      parameter.setValue(source.getValue());
    }
  }

  /**
   * Remove the parameter described by the context and name
   * @param strContext the context
   * @param strName the name of the parameter
   * @throws WorkflowException
   */
  public void removeParameter(String strContext, String strName)
      throws WorkflowException {
    Item item = findItem(strContext);

    item.removeParameter(strName);
  }

  /**
   * Find the QualifiedUsers specified by the context
   * @param strConxtext the context
   * @return a QualifiedUsers object or <code>null</code>
   */
  public QualifiedUsers findQualifiedUsers(String strContext)
      throws WorkflowException {
    StringTokenizer strtok;
    String strElement;
    QualifiedUsers qualifiedUsers = null;

    if (strContext != null) {
      strtok = new StringTokenizer(strContext, "/");

      try {
        if (strtok.hasMoreTokens()) {
          strElement = strtok.nextToken();

          if (STATES.equals(strElement)) {
            State state;

            strElement = strtok.nextToken();
            state = m_processModel.getStatesEx().getState(strElement);
            strElement = strtok.nextToken();

            if (WORKING_USERS.equals(strElement)) {
              qualifiedUsers = state.getWorkingUsersEx();
            } else if (INTERESTED_USERS.equals(strElement)) {
              qualifiedUsers = state.getInterestedUsersEx();
            }
          } else if (ACTIONS.equals(strElement)) {
            Action action;

            strElement = strtok.nextToken();
            action = m_processModel.getActionsEx().getAction(strElement);
            strElement = strtok.nextToken();

            if (ALLOWED_USERS.equals(strElement)) {
              qualifiedUsers = action.getAllowedUsers();
            } else if (CONSEQUENCES.equals(strElement)) {
              // notifiedUsers
              //
              Consequence consequence;

              strElement = strtok.nextToken(); // consequence no.
              consequence = (Consequence) action.getConsequences().getConsequenceList().get(Integer.
                  parseInt(strElement));
              strElement = strtok.nextToken(); // notified users

              if (NOTIFIED_USERS.equals(strElement)) {
                qualifiedUsers = consequence.getNotifiedUsersEx();
              }
            }
          }
        }
      } catch (NoSuchElementException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      } catch (ArrayIndexOutOfBoundsException e) {
        // Thrown when no consequence was found at the specified index
        // do nothing, just return null...
      } catch (NumberFormatException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      }
    }
    return qualifiedUsers;
  }

  /**
   * Set the QualifiedUsers specified by the context to the given value
   * @param qualifiedUsers the new value of qualified users
   * @param strContext the context
   * @throws WorkflowDesignerException When the qualified users' parent could not be found
   */
  public void setQualifiedUsers(QualifiedUsers qualifiedUsers, String strContext)
      throws WorkflowException, WorkflowDesignerException {
    StringTokenizer strtok;
    String strElement;

    if (strContext != null) {
      strtok = new StringTokenizer(strContext, "/");

      try {
        if (strtok.hasMoreTokens()) {
          strElement = strtok.nextToken();

          if (STATES.equals(strElement)) {
            State state;

            strElement = strtok.nextToken();
            state = m_processModel.getStatesEx().getState(strElement);
            strElement = strtok.nextToken();

            if (WORKING_USERS.equals(strElement)) {
              state.setWorkingUsers(qualifiedUsers);
            } else if (INTERESTED_USERS.equals(strElement)) {
              state.setInterestedUsers(qualifiedUsers);
            }
          } else if (ACTIONS.equals(strElement)) {
            Action action;

            strElement = strtok.nextToken();
            action = m_processModel.getActionsEx().getAction(strElement);
            strElement = strtok.nextToken();

            if (ALLOWED_USERS.equals(strElement)) {
              action.setAllowedUsers(qualifiedUsers);
            } else if (CONSEQUENCES.equals(strElement)) {
              // notifiedUsers
              //
              Consequence consequence;

              strElement = strtok.nextToken(); // consequence no.
              consequence = (Consequence) action.getConsequences().getConsequenceList().get(Integer.
                  parseInt(strElement));
              strElement = strtok.nextToken(); // consequence no.

              if (NOTIFIED_USERS.equals(strElement)) {
                consequence.setNotifiedUsers(qualifiedUsers);
              }
            }
          }
        }
      } catch (RuntimeException e) {
        // Thrown when no token was found where expected
        // or the number coludn't to be interpreted,
        // or the index is out of bounds
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.setQualifiedUsers",
            SilverpeasException.ERROR,
            "workflowDesigner.EX_QUALIFIED_USERS_NOT_FOUND");
      }
    }
  }

  /**
   * Collect all the objects of the type QalifiedUsers instantiated in the Process Model
   * @return a map, where the key is the reference to the object and the value is a textual
   * description of the object location
   */
  private Map collectQualifiedUsers() {
    Map map = new IdentityHashMap();

    // States
    //
    if (m_processModel.getStatesEx() != null) {
      Iterator iterState = m_processModel.getStatesEx().iterateState();
      State state;

      while (iterState.hasNext()) {
        state = (State) iterState.next();

        if (state.getWorkingUsersEx() != null) {
          map.put(state.getWorkingUsersEx(), "state: '" + state.getName()
              + "' : workingUsers");
        }

        if (state.getInterestedUsersEx() != null) {
          map.put(state.getInterestedUsersEx(), "state: '" + state.getName()
              + "' : interestedUsers");
        }
      }
    }

    // Actions
    //
    if (m_processModel.getActionsEx() != null) {
      Action action;
      Iterator iterAction = m_processModel.getActionsEx().iterateAction(), iterConsequence;

      while (iterAction.hasNext()) {
        action = (Action) iterAction.next();

        if (action.getAllowedUsers() != null) {
          map.put(action.getAllowedUsers(), "action: '" + action.getName()
              + "' : allowedUsers");
        }

        if (action.getConsequences() != null) {
          Consequence consequence;

          iterConsequence = action.getConsequences().iterateConsequence();

          while (iterConsequence.hasNext()) {
            consequence = (Consequence) iterConsequence.next();

            if (consequence.getNotifiedUsersEx() != null) {
              map.put(consequence.getNotifiedUsersEx(), "action: '"
                  + action.getName()
                  + "' : consequence: ["
                  + (consequence.getItem() == null ? "Default" : (" item: '"
                  + consequence.getItem() + "', operator: '"
                  + consequence.getOperator() + "', value: '"
                  + consequence.getValue() + "'")) + " ]");
            }
          }
        }
      }
    }

    return map;
  }

  /**
   * Get the Action specified by the context
   * @param strConxtext the context
   * @return an Action object or <code>null</code>
   */
  public Action findAction(String strContext) throws WorkflowException {
    StringTokenizer strtok;
    String strElement;
    Action action = null;

    if (strContext != null) {
      strtok = new StringTokenizer(strContext, "/");

      try {
        strElement = strtok.nextToken();

        if (ACTIONS.equals(strElement)) {
          strElement = strtok.nextToken();
          action = m_processModel.getActionsEx().getAction(strElement);
        }
      } catch (NoSuchElementException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      }
    }
    return action;
  }

  /**
   * Find the Consequence specified by the context
   * @param strConxtext the context
   * @return an Consequence object or <code>null</code>
   */
  public Consequence findConsequence(String strContext)
      throws WorkflowException {
    StringTokenizer strtok;
    String strElement;
    Action action = findAction(strContext);
    Consequence consequence = null;

    if (strContext != null) {
      strtok = new StringTokenizer(strContext, "/");

      try {
        if (action != null) {
          strElement = strtok.nextToken(); // actions/
          strElement = strtok.nextToken(); // <name>
          strElement = strtok.nextToken(); // consequences/
          strElement = strtok.nextToken(); // <consequence-no>

          consequence = (Consequence) action.getConsequences().getConsequenceList().get(Integer.
              parseInt(strElement));
        }

      } catch (NoSuchElementException e) {
        // Thrown when no token was found where expected
        // do nothing, just return null...
      } catch (NumberFormatException e) {
        // Thrown when token could not be interpreted
        // do nothing, just return null...
      } catch (ArrayIndexOutOfBoundsException e) {
        // Thrown when no consequence was found at the specified index
        // do nothing, just return null...
      }
    }
    return consequence;
  }

  /**
   * Returns the language codes as configured in the properties
   * @param fDefault if <code>true</code> the 'default' option shall be included
   * @return an array of language codes
   */
  public String[] retrieveLanguageCodes(boolean fDefault) {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "languages"), ",");
    ArrayList list = new ArrayList();

    if (fDefault) {
      list.add("default");
    }

    while (strtok.hasMoreTokens()) {
      list.add(strtok.nextToken());
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns names of available languages as configured in the properties, localised for the current
   * user
   * @param fDefault if <code>true</code> the 'default' option shall be included
   * @return an array of language names
   */
  public String[] retrieveLanguageNames(boolean fDefault) {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "languages"), ",");
    Locale locale = new Locale(getLanguage());
    ArrayList list = new ArrayList();

    if (fDefault) {
      list.add(getString("workflowDesigner.default"));
    }

    while (strtok.hasMoreTokens()) {
      Locale inLocale = new Locale(strtok.nextToken());

      list.add(inLocale.getDisplayLanguage(locale));
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns the item type codes as configured in the properties
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @return an array of codes
   */
  public String[] retrieveItemTypeCodes(boolean fNone) {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "itemTypes"), ",");
    ArrayList list = new ArrayList();

    if (fNone) {
      list.add("");
    }

    while (strtok.hasMoreTokens()) {
      list.add(strtok.nextToken());
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns a list of comparison operators as configured in the properties,
   * @param fNone if <code>true</code> the 'none' option shall be included
   * @return an array of operators
   */
  public String[] retrieveOperators(boolean fNone) {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "operators"), ",");
    ArrayList list = new ArrayList();

    if (fNone) {
      list.add("");
    }

    while (strtok.hasMoreTokens()) {
      list.add(strtok.nextToken());
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns the action kind codes as configured in the properties retrieve
   * @return an array of codes
   */
  public String[] retrieveActionKindCodes() {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "actionKinds"), ",");
    ArrayList list = new ArrayList();

    while (strtok.hasMoreTokens()) {
      list.add(strtok.nextToken());
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns the input displayer type codes as configured in the properties
   * @return an array of codes
   */
  public String[] retrieveDisplayerNames() {
    StringTokenizer strtok = new StringTokenizer(getSettings().getString(
        "displayerNames"), ",");
    ArrayList list = new ArrayList();

    list.add("");
    while (strtok.hasMoreTokens()) {
      list.add(strtok.nextToken());
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of action names
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveActionNames(boolean fNone) {
    ArrayList list = new ArrayList();
    Actions actions = getProcessModel().getActionsEx();

    if (fNone) {
      list.add("");
    }

    if (actions != null) {
      Iterator iterAction = actions.iterateAction();

      while (iterAction.hasNext()) {
        list.add(((Action) iterAction.next()).getName());
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of role names
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @param fDefault if <code>true</code> the 'default' option shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveRoleNames(boolean fNone, boolean fDefault) {
    ArrayList list = new ArrayList();
    Roles roles = getProcessModel().getRolesEx();

    if (fNone) {
      list.add("");
    }

    if (fDefault) {
      list.add("default");
    }

    if (roles != null) {
      Iterator iterRole = roles.iterateRole();

      while (iterRole.hasNext()) {
        list.add(((Role) iterRole.next()).getName());
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of state names
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveStateNames(boolean fNone) {
    ArrayList list = new ArrayList();
    States states = getProcessModel().getStatesEx();

    if (fNone) {
      list.add("");
    }

    if (states != null) {
      Iterator iterState = states.iterateState();

      while (iterState.hasNext()) {
        list.add(((State) iterState.next()).getName());
      }
    }
    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of participant names
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveParticipantNames(boolean fNone) {
    ArrayList list = new ArrayList();
    Participants participants = getProcessModel().getParticipantsEx();

    if (fNone) {
      list.add("");
    }

    if (participants != null) {
      Iterator iterParticipant = participants.iterateParticipant();

      while (iterParticipant.hasNext()) {
        list.add(((Participant) iterParticipant.next()).getName());
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of user info item names, optionally those where the type = 'user'
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @param fUsersOnly if <code>true</code> only the items of type 'user' shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveUserInfoItemNames(boolean fNone, boolean fUsersOnly) {
    ArrayList list = new ArrayList();
    DataFolder userInfos = getProcessModel().getUserInfos();

    if (fNone) {
      list.add("");
    }

    if (userInfos != null) {
      Iterator iterInfosItem = userInfos.iterateItem();
      Item item;

      while (iterInfosItem.hasNext()) {
        item = (Item) iterInfosItem.next();
        if (!fUsersOnly || TYPE_USER.equals(item.getType())) {
          list.add(item.getName());
        }
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of data folder item names, optionally those where the type = 'user'
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @param fUsersOnly if <code>true</code> only the items of type 'user' shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveFolderItemNames(boolean fNone, boolean fUsersOnly) {
    ArrayList list = new ArrayList();
    DataFolder dataFolder = getProcessModel().getDataFolder();

    if (fNone) {
      list.add("");
    }

    if (dataFolder != null) {
      Iterator iterFolderItem = dataFolder.iterateItem();
      Item item;

      while (iterFolderItem.hasNext()) {
        item = (Item) iterFolderItem.next();
        if (!fUsersOnly || TYPE_USER.equals(item.getType())) {
          list.add(item.getName());
        }
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Produce a list of form names, only forms other than 'presentationForm' and 'printForm'
   * @param fNone if <code>true</code> the 'None' option shall be included
   * @return an array of Strings or an empty array.
   */
  public String[] retrieveFormNames(boolean fNone) {
    ArrayList list = new ArrayList();
    Forms forms = getProcessModel().getForms();
    String strName;

    if (fNone) {
      list.add("");
    }

    if (forms != null) {
      Iterator iterForm = forms.iterateForm();

      while (iterForm.hasNext()) {
        strName = ((Form) iterForm.next()).getName();

        if (!(FORM_TYPE_PRINT.equals(strName) || FORM_TYPE_PRESENTATION.equals(strName))) {
          list.add(strName);
        }
      }
    }

    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Generates component descriptor file in the appropriate directory, reloads the cache to take the
   * new file into account, stores the new descriptor's name to be able to access it later
   * @param strLabel the label that will be put in the descriptor
   * @param strDescription the description that will be put in the descriptor
   * @param strRole the role name to select role labels (for profiles)
   * @param strLanguage the language code to select role labels (for profiles)
   * @throws WorkflowDesignerException when something goes wrong
   */
  public void generateComponentDescriptor(String strLabel,
      String strDescription, String strRole, String strLanguage)
      throws WorkflowDesignerException {
    List listSPProfile = new ArrayList();
    WAComponent waComponent;
    Role role;
    SPParameters spParameters = new SPParameters();
    SPParameter spParameter = new SPParameter(
        ComponentsInstanciatorIntf.PROCESS_XML_FILE_NAME, getSettings().getString(
        "componentDescriptor.parameterLabel"),
        m_strProcessModelFileName.replaceAll("\\\\", "/"), true, null, null,
        null, null, null, null);

    // Create the list of roles, to be placed as profiles in the component
    // descriptor
    //
    if (m_processModel.getRolesEx() != null) {
      Iterator iterRole = m_processModel.getRolesEx().iterateRole();

      // 'supervisor' must be present in the list
      //
      if (m_processModel.getRolesEx().getRole("supervisor") == null) {
        listSPProfile.add(new SPProfile("supervisor", getSettings().getString(
            "componentDescriptor.supervisor")));
      }

      while (iterRole.hasNext()) {
        role = (Role) iterRole.next();
        listSPProfile.add(new SPProfile(role.getName(), role.getLabel(strRole,
            strLanguage)));
      }
    } else {
      listSPProfile.add(new SPProfile("supervisor", getSettings().getString(
          "componentDescriptor.supervisor")));
    }

    waComponent = new WAComponent(m_processModel.getName(), strLabel,
        strDescription, getSettings().getString("componentDescriptor.suite"),
        true, false, getSettings().getString(
        "componentDescriptor.managerInstanciator"), new String[0],
        listSPProfile);
    waComponent.setRequestRouter(getSettings().getString(
        "componentDescriptor.managerRequestRouter"));
    spParameters.addParameter(spParameter);
    waComponent.setSPParameters(spParameters);

    try {
      waComponent.writeToXml();

      // Save the descriptor name
      //
      m_strReferencedInComponent = m_processModel.getName();
    } catch (InstanciationException e) {
      throw new WorkflowDesignerException(
          "WorkflowDesignerSessionController.generateComponentDescriptor()",
          SilverpeasException.FATAL,
          "workflowDesigner.EX_ERR_WRITE_WA_COMPONENTS", e);
    }

    // Clear the descriptor cache and load it again
    //
    rebuildComponentDescriptorCache();
  }

  /**
   * Finds the component that references the given process model
   * @param strProcessModelFileName the relative path to the file containing the process model
   * description
   * @return the name of the component descriptor file name (without .XML ) or <code>null</code> if
   * no components reference the given model.
   */
  private String findComponentDescriptor(String strProcessModelFileName) {
    Map<String, WAComponent> waComponents = Instanciateur.getWAComponents();
    Set<Map.Entry<String, WAComponent>> enumWAComponent = waComponents.entrySet();
    if (strProcessModelFileName != null) {
      strProcessModelFileName = strProcessModelFileName.replaceAll("\\\\", "/");
      // Look for the descriptor in the cache, return the key if found.
      for (Map.Entry<String, WAComponent> componentEntry : enumWAComponent) {
        String strComponentName = componentEntry.getKey();
        WAComponent waComponent = componentEntry.getValue();
        if (waComponent.getSPParameters() != null) {
          SPParameter spParameter = waComponent.getSPParameters().getParameter(
              ComponentsInstanciatorIntf.PROCESS_XML_FILE_NAME);
          if (spParameter != null && strProcessModelFileName.equals(spParameter.getValue())) {
            return strComponentName;
          }
        }
      }
    }

    return null;
  }

  /**
   * Clear the Component Descriptor cache and load it again
   * @throws WorkflowDesignerException when something goes wrong
   */
  private void rebuildComponentDescriptorCache()
      throws WorkflowDesignerException {
    try {
      Instanciateur.rebuildWAComponentCache();
    } catch (InstanciationException e) {
      throw new WorkflowDesignerException(
          "WorkflowDesignerSessionController.rebuildComponentDescriptorCache()",
          SilverpeasException.FATAL,
          "workflowDesigner.EX_ERR_READ_WA_COMPONENTS", e);
    }
  }

  /**
   * Upload given process model in workflow repository.
   * @param model process model file
   * @throws WorkflowDesignerException , WorkflowException
   */
  public void uploadProcessModel(FileItem model)
      throws WorkflowDesignerException, WorkflowException {
    String name = model.getName();
    if (name != null) {
      name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
      name = replaceSpecialChars(name);

      ProcessModelManager manager = WorkflowHub.getProcessModelManager();
      String workflowRepositoryDir = manager.getProcessModelDir();

      // on vérifie qu'un modèle portant ce nom n'exista pas déjà
      File modelFile = new File(workflowRepositoryDir + name);
      if (modelFile.exists()) {
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.uploadProcessModel()",
            SilverpeasException.ERROR,
            "workflowDesigner.EX_ERR_MODEL_ALREADY_EXISTS");
      }

      // upload du fichier
      try {
        model.write(modelFile);
      } catch (Exception e) {
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.uploadProcessModel()",
            SilverpeasException.ERROR,
            "workflowDesigner.EX_ERR_IMPORT_MODEL_FAILED", e);
      }

      // on charge le modele pour vérifier qu'il est valide
      try {
        ProcessModel processModel = manager.loadProcessModel(name, false);
      } catch (Exception e) {
        // le modèle est invalide, on le supprime
        modelFile.delete();
        throw new WorkflowDesignerException(
            "WorkflowDesignerSessionController.uploadProcessModel()",
            SilverpeasException.ERROR,
            "workflowDesigner.EX_ERR_TRY_TO_IMPORT_INVALID_MODEL", e);
      }

    }
  }

  public synchronized void clearCache() {
    ProcessModelManagerImpl pmmi = new ProcessModelManagerImpl();
    pmmi.clearProcessModelCache();
  }

  public static String replaceSpecialChars(String toParse) {
    return FileServerUtils.replaceInvalidPathChars(toParse);
  }
}
