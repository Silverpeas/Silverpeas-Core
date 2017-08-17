/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.dummy.DummyRecordSet;
import org.silverpeas.core.contribution.content.form.form.HtmlForm;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.contribution.content.form.record.IdentifiedRecordTemplate;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.States;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRecordTemplate;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowTemplate;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Actions;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Forms;
import org.silverpeas.core.workflow.api.model.Participant;
import org.silverpeas.core.workflow.api.model.Participants;
import org.silverpeas.core.workflow.api.model.Presentation;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.RelatedUser;
import org.silverpeas.core.workflow.api.model.Role;
import org.silverpeas.core.workflow.api.model.Roles;
import org.silverpeas.core.workflow.api.model.UserInRole;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the main &lt;processModel&gt; element of a Process
 * Model.
 */
@XmlRootElement(name = "processModel")
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessModelImpl implements ProcessModel, Serializable {
  private static final long serialVersionUID = -4576686557632464607L;
  private String modelId;
  @XmlAttribute
  private String name;
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;
  @XmlElement(type = RolesImpl.class)
  private Roles roles;
  @XmlElement(type = PresentationImpl.class)
  private Presentation presentation;
  @XmlElement(type = ParticipantsImpl.class)
  private Participants participants;
  @XmlElement(type = StatesImpl.class)
  private States states;
  @XmlElement(type = ActionsImpl.class)
  private Actions actions;
  @XmlElement(type = DataFolderImpl.class)
  private DataFolder dataFolder;
  @XmlElement(type = DataFolderImpl.class)
  private DataFolder userInfos;
  @XmlElement(type = FormsImpl.class)
  private Forms forms;

  private HashMap<String, RecordTemplate> instanceDataTemplates = new HashMap<>();
  private HashMap<String, RecordTemplate> rowTemplates = new HashMap<>();

  /**
   * Constructor
   */
  public ProcessModelImpl() {
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
    presentation = createPresentation();
  }

  /**
   * Get the id of this process model
   * @return process model id
   */
  public String getModelId() {
    return this.modelId;
  }

  /**
   * Set the id of this process model
   * @param modelId process model id
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  /**
   * Get the name of this process model
   * @return process model's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the name of this process model
   * @param name process model's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the presentation configuration
   * @return presentation configuration
   */
  public Presentation getPresentation() {
    return presentation;
  }

  /**
   * Set the presentation's configuration
   * @param presentation presentation's configuration
   */
  public void setPresentation(Presentation presentation) {
    this.presentation = presentation;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createPresentation()
   */
  public Presentation createPresentation() {
    return new PresentationImpl();
  }

  /**
   * Get the participants definition
   * @return participants definition
   */
  public Participant[] getParticipants() {
    if (participants == null) {
      return new Participant[0];
    }
    return participants.getParticipants();
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getAllParticipants()
   */
  public Participants getParticipantsEx() {
    return participants;
  }

  /**
   * Set the participants definition
   * @param participants participants definition
   */
  public void setParticipants(Participants participants) {
    this.participants = participants;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createParticipants()
   */
  public Participants createParticipants() {
    return new ParticipantsImpl();
  }

  /**
   * Get the roles definition
   * @return roles definition
   */
  public Role[] getRoles() {
    if (roles == null) {
      return new Role[0];
    }
    return roles.getRoles();
  }

  /**
   * Get the role definition with given name
   * @param name role name
   * @return wanted role definition
   */
  public Role getRole(String name) {
    if (roles == null) {
      return null;
    }
    return roles.getRole(name);
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getAllRoles()
   */
  public Roles getRolesEx() {
    return roles;
  }

  /**
   * Set the roles definition
   * @param roles roles definition
   */
  public void setRoles(Roles roles) {
    this.roles = roles;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createRoles()
   */
  public Roles createRoles() {
    return new RolesImpl();
  }

  /**
   * Get the states defined for this process model
   * @return states defined for this process model
   */
  public State[] getStates() {
    if (states == null) {
      return new State[0];
    }
    return states.getStates();
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getAllStates()
   */
  public States getStatesEx() {
    return states;
  }

  /**
   * Get the state with the given name
   * @param name state name
   * @return wanted state
   */
  public State getState(String name) {
    if (states == null) {
      return null;
    }
    return states.getState(name);
  }

  /**
   * Set the states defined for this process model
   * @param states states defined for this process model
   */
  public void setStates(States states) {
    this.states = states;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createStates()
   */
  public States createStates() {
    return new StatesImpl();
  }

  /**
   * Get the actions defined for this process model
   * @return actions defined for this process model
   */
  public Action[] getActions() {
    if (actions == null) {
      return new Action[0];
    }
    return actions.getActions();
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getAllActions()
   */
  public Actions getActionsEx() {
    return actions;
  }

  /**
   * Get the action with the given name
   * @param name action name
   * @return the wanted action
   */
  public Action getAction(String name) throws WorkflowException {
    if (actions == null) {
      return null;
    }
    return actions.getAction(name);
  }

  /**
   * Set the actions defined for this process model
   * @param actions actions defined for this process model
   */
  public void setActions(Actions actions) {
    this.actions = actions;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createActions()
   */
  public Actions createActions() {
    return new ActionsImpl();
  }

  /**
   * Get the data folder defined for this process model
   * @return data folder defined for this process model. it contains all the items declarations
   */
  public DataFolder getDataFolder() {
    return dataFolder;
  }

  /**
   * Set the data folder defined for this process model
   * @param dataFolder data folder defined for this process model. it contains all the items
   * declarations
   */
  public void setDataFolder(DataFolder dataFolder) {
    this.dataFolder = dataFolder;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createDataFolder()
   */
  public DataFolder createDataFolder() {
    return new DataFolderImpl();
  }

  /**
   * Get the user infos defined for this process model It contains all the items necessary about
   * user to allow him to use the instance
   * @return user infos defined for this process model.
   */
  public DataFolder getUserInfos() {
    return userInfos;
  }

  /**
   * Set the user infos defined for this process model It contains all the items necessary about
   * user to allow him to use the instance
   * @param userInfos user infos defined for this process model.
   */
  public void setUserInfos(DataFolder userInfos) {
    this.userInfos = userInfos;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getAllForms()
   */
  public Forms getForms() {
    return forms;
  }

  /**
   * Get the form with the given name
   * @param name form name
   * @return the wanted form
   */
  public Form getForm(String name) {
    return getForm(name, null);
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getForm(java.lang.String, java.lang.String)
   */
  public Form getForm(String name, String role) {
    if (forms == null) {
      return null;
    }
    return forms.getForm(name, role);
  }

  /**
   * Set the forms defined for this process model
   * @param forms forms defined for this process model.
   */
  public void setForms(Forms forms) {
    this.forms = forms;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createForms()
   */
  public Forms createForms() {
    return new FormsImpl();
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getLabels()
   */
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  /**
   * Get label in specific language for the given role
   * @param role role for which the label is
   * @param language label's language
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  /**
   * Get description in specific language for the given role
   * @param role role for which the description is
   * @param language description's language
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createQualifiedUsers()
   */
  public QualifiedUsers createQualifiedUsers() {
    return new QualifiedUsersImpl();
  }

  /*
   * (non-Javadoc)
   * @see ProcessModel#createRelatedUser()
   */
  public RelatedUser createRelatedUser() {
    return new RelatedUserImpl();
  }

  /**
   * Returns the name of the record set where are saved all the folder of the instance built from
   * this model.
   */
  public String getFolderRecordSetName() {
    return modelId + ":" + "folder";
  }

  /**
   * Returns the name of the record set where are saved all the data of the named form.
   */
  public String getFormRecordSetName(String formName) {
    return modelId + ":" + "form:" + formName;
  }

  /**
   * Returns the record set where are saved all the folder of the instance built from this model.
   */
  public RecordSet getFolderRecordSet() throws WorkflowException {
    // Now, the folder is read from xml file and no more in database
    // This permit to add items to the folder
    IdentifiedRecordTemplate idTemplate;
    int templateId = -1;
    try {
      RecordSet recordSet = getGenericRecordSetManager().getRecordSet(getFolderRecordSetName());
      idTemplate = (IdentifiedRecordTemplate) recordSet.getRecordTemplate();
      templateId = idTemplate.getInternalId();
    } catch (FormException e) {
      throw new WorkflowException("ProcessModel", "workflowEngine.EXP_UNKNOWN_RECORD_SET",
          getFolderRecordSetName(), e);
    }

    RecordTemplate template = dataFolder.toRecordTemplate(null, null, false);
    idTemplate = new IdentifiedRecordTemplate(template);
    idTemplate.setExternalId(getFolderRecordSetName());
    idTemplate.setInternalId(templateId);
    return new GenericRecordSet(idTemplate);
  }

  /**
   * Returns the record set where are saved all the data of the named form.
   */
  public RecordSet getFormRecordSet(String formName) throws WorkflowException {
    try {
      RecordSet recordSet =
          getGenericRecordSetManager().getRecordSet(getFormRecordSetName(formName));

      /*
       * If recordset cannot be found, form is a new Form declared after peas instanciation : add it
       */
      if (recordSet instanceof DummyRecordSet) {
        Form form = getForm(formName);
        RecordTemplate template = form.toRecordTemplate(null, null);
        getGenericRecordSetManager().createRecordSet(getFormRecordSetName(formName), template);
        recordSet = getGenericRecordSetManager().getRecordSet(getFormRecordSetName(formName));
      }

      IdentifiedRecordTemplate template = (IdentifiedRecordTemplate) recordSet.getRecordTemplate();

      GenericRecordTemplate wrapped = (GenericRecordTemplate) template.getWrappedTemplate();

      GenericFieldTemplate fieldTemplate = null;
      String fieldName;
      String fieldType;
      boolean isMandatory;
      boolean isReadOnly;
      boolean isHidden;
      FormImpl form = (FormImpl) getForm(formName);
      FieldTemplate[] fields = form.toRecordTemplate(null, null).getFieldTemplates();

      for (int i = 0; i < fields.length; i++) {
        fieldName = fields[i].getFieldName();
        fieldType = fields[i].getTypeName();
        isMandatory = fields[i].isMandatory();
        isReadOnly = fields[i].isReadOnly();
        isHidden = fields[i].isHidden();

        fieldTemplate = new GenericFieldTemplate(fieldName, fieldType);
        fieldTemplate.setMandatory(isMandatory);
        fieldTemplate.setReadOnly(isReadOnly);
        fieldTemplate.setHidden(isHidden);

        wrapped.addFieldTemplate(fieldTemplate);
      }

      return recordSet;
    } catch (FormException e) {
      throw new WorkflowException("ProcessModel", "workflowEngine.EXP_UNKNOWN_RECORD_SET",
          getFormRecordSetName(formName), e);
    }
  }

  /**
   * Returns the form (if any) associated to the named action. Returns null if the action has no
   * form. Throws a WorkflowException if the action is unknown.
   */
  public Form getActionForm(String actionName) throws WorkflowException {
    Action action = getAction(actionName);
    return action.getForm();
  }

  /**
   * Returns the action of kind create Throws a WorkflowException if there is no action of type
   * create
   */
  public Action getCreateAction(String role) throws WorkflowException {
    Action[] someActions = getActions();

    for (Action action : someActions) {
      if ("create".equals(action.getKind())) {
        // Retrieve roles allowed to do this action
        QualifiedUsers creators = action.getAllowedUsers();
        UserInRole[] usersInRoles = creators.getUserInRoles();

        for (UserInRole usersInRole : usersInRoles) {
          if (role.equals(usersInRole.getRoleName())) {
            return action;
          }
        }
      }
    }

    throw new WorkflowException("ProcessModel", "workflowEngine.ERR_NO_CREATE_ACTION_DEFINED");
  }

  /**
   * Returns the Form which be used to publish the form associated to the named
   * action. Returns null if the action has no form. Throws a WorkflowException if the action is
   * unknown.
   */
  public org.silverpeas.core.contribution.content.form.Form getPublicationForm(String actionName, String roleName,
      String lang) throws WorkflowException {
    Action action = getAction(actionName);
    if (action == null || action.getForm() == null) {
      return null;
    }

    return getConcreteForm(action.getForm(), roleName, lang, false);
  }

  /**
   * Returns the Form which be used to publish the form associated to the named
   * action or form. Returns null if the action has no form.
   */
  public org.silverpeas.core.contribution.content.form.Form getPresentationForm(String name, String roleName, String lang)
      throws WorkflowException {
    Action action = null;
    Form form = null;

    if (!"presentationForm".equalsIgnoreCase(name)) {
      try {
        action = getAction(name);
      } catch (WorkflowException ignoredAtThisStep) {
        SilverLogger.getLogger(this).error("This is ignored", ignoredAtThisStep);
      }
    }

    if (action != null) {
      form = action.getForm();
    } else {
      form = getForm(name, roleName);
    }

    if (form == null) {
      return null;
    }

    return getConcreteForm(form, roleName, lang, true);
  }

  private org.silverpeas.core.contribution.content.form.Form getConcreteForm(Form form,
      String roleName, String lang, boolean readOnly) throws WorkflowException {
    try {
      if (StringUtil.isDefined(form.getHTMLFileName())) {
        HtmlForm htmlForm = new HtmlForm(form.toRecordTemplate(roleName, lang));
        htmlForm.setFileName(WorkflowHub.getProcessModelManager().getProcessModelDir() +
            form.getHTMLFileName());
        htmlForm.setName(form.getName());
        htmlForm.setTitle(form.getTitle(roleName, lang));
        return htmlForm;
      } else {
        XmlForm xmlForm = new XmlForm(form.toRecordTemplate(roleName, lang, readOnly));
        xmlForm.setName(form.getName());
        xmlForm.setTitle(form.getTitle(roleName, lang));
        return xmlForm;
      }
    } catch (FormException e) {
      throw new WorkflowException("ProcessModel", "workflowEngine.EXP_ILL_FORMED_FORM",
          form.getName(), e);
    }
  }

  /**
   * Returns an empty DataRecord which must be filled in order to process the
   * named action. Returns null if no form is required to process this action. Throws a
   * WorkflowException if the action is unknown.
   */
  public DataRecord getNewActionRecord(String actionName, String roleName, String lang,
      DataRecord data) throws WorkflowException {
    Action action = getAction(actionName);
    if (action == null || action.getForm() == null) {
      return null;
    }
    return action.getForm().getDefaultRecord(roleName, lang, data);
  }

  /**
   * Returns an empty DataRecord which must be filled in order to fill the user
   * information Throws a WorkflowException if problem encountered.
   */
  public DataRecord getNewUserInfosRecord(String roleName, String lang) throws WorkflowException {
    try {
      return this.getUserInfos().toRecordTemplate(roleName, lang, false).getEmptyRecord();
    } catch (FormException e) {
      throw new WorkflowException("ProcessModel", "workflowEngine.EXP_ILL_FORMED_FORM",
          "User Infos", e);
    }
  }

  /**
   * Returns the roles under which an user can create a new instance
   */
  public String[] getCreationRoles() throws WorkflowException {
    try {
      List<String> someRoles = new ArrayList<>();

      // Search for actions of kind create
      Action[] someActions = getActions();
      for (Action action : someActions) {
        if ("create".equals(action.getKind())) {
          // Retrieve roles allowed to do this action
          QualifiedUsers creators = action.getAllowedUsers();
          UserInRole[] usersInRoles = creators.getUserInRoles();

          for (UserInRole usersInRole : usersInRoles) {
            if (!someRoles.contains(usersInRole.getRoleName())) {
              someRoles.add(usersInRole.getRoleName());
            }
          }
        }
      }

      return someRoles.toArray(new String[someRoles.size()]);
    } catch (Exception e) {
      throw new WorkflowException("ProcessModel", "workflowEngine.EXP_FAIL_GET_CREATION_ROLES",
          this.name, e);
    }
  }

  /**
   * Returns the recordTemplate which describes the data record of the process instance built from
   * this model.
   */
  public RecordTemplate getAllDataTemplate(String role, String lang) {
    RecordTemplate template = instanceDataTemplates.get(role + "\n" + lang);

    if (template == null) {
      template =
          new ProcessInstanceRecordTemplate(this, role,
              lang);
      instanceDataTemplates.put(role + "\n" + lang, template);
    }
    return template;
  }

  /**
   * Returns the recordTemplate which describes the data record used to show process instance as a
   * row in list.
   */
  public RecordTemplate getRowTemplate(String role, String lang) {
    RecordTemplate template = rowTemplates.get(role + "\n" + lang);

    if (template == null) {
      template =
          new ProcessInstanceRowTemplate(this, role,
              lang);
      rowTemplates.put(role + "\n" + lang, template);
    }
    return template;
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  private GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }
}
