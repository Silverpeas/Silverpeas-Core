/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.instance;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.ProcessModelManager;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;
import org.silverpeas.core.workflow.engine.datarecord.LazyProcessInstanceDataRecord;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceDataRecord;
import org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowRecord;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.workflow.api.instance.ActionStatus.SAVED;

/**
 * This class is one implementation of interface UpdatableProcessInstance.
 */
@Entity
@Table(name = "sb_workflow_processinstance")
@AttributeOverride(name = "id", column = @javax.persistence.Column(name = "instanceid"))
public class ProcessInstanceImpl
    extends BasicJpaEntity<ProcessInstanceImpl, UniqueIntegerIdentifier>
    implements UpdatableProcessInstance {

  private static final String QUESTION_ACTION = "#question#";
  private static final String ADD_ACTIVE_STATE = "addActiveState";
  private static final String ADD_WORKING_USER = "addWorkingUser";
  private static final String REMOVE_WORKING_USER = "removeWorkingUser";
  private static final String ADD_INTERESTED_USER = "addInterestedUser";
  private static final String REMOVE_INTERESTED_USER = "removeInterestedUser";
  private static final String INSTANCEID_PARAM = "instanceid=";
  private static final String PROCESS_INSTANCE_IMPL = "ProcessInstanceImpl";
  private static final String DUMMY = "dummy";
  private static final String FOLDER_PARAM = "folder=";
  private static final String WORKFLOW_ENGINE_EX_ERR_ILLEGAL_PARAMETERS =
      "workflowEngine.EX_ERR_ILLEGAL_PARAMETERS";
  private static final String INSTEAD_OF_2_OR_3 = " instead of 2 or 3";
  /**
   * Abstract process model
   */
  @Transient
  private transient ProcessModel model = null;
  /**
   * Flag that indicates validity of this processInstance
   */
  @Transient
  private boolean valid = false;
  /**
   * Flag that indicates if this instance is locked by admin
   */
  @Column
  private int locked = 0;
  /**
   * Flag that indicates if this instance status is "error"
   */
  @Column
  private int errorStatus = 0;
  /**
   * Flag that indicates if this instance is in an active state for a long long time
   */
  @Column
  private int timeoutStatus = 0;
  /**
   * the model Id
   */
  @Column
  private String modelId = null;
  /**
   * All history steps that trace events occurred on this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<HistoryStepImpl> historySteps = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<UndoHistoryStep> undoSteps = new HashSet<>();

  /**
   * Vector of all questions asked on this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<QuestionImpl> questions = new HashSet<>();
  /**
   * The current history step used to add atomic operations in history
   */
  @Transient
  private transient HistoryStep currentStep = null;
  /**
   * the status of this instance regarding 'undo' process while true, the atomic operations are not
   * stored anymore
   */
  @Transient
  private boolean inUndoProcess = false;
  /**
   * All users who can see this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<InterestedUser> interestedUsers = new HashSet<>();
  /**
   * All users who can act on this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<WorkingUser> workingUsers = new HashSet<>();
  /**
   * All users who can have locked a state of this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<LockingUser> lockingUsers = new HashSet<>();
  /**
   * A1l states that are due to be resolved for this process instance
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy =
      "processInstance")
  private Set<ActiveState> activeStates = new HashSet<>();
  /**
   * The DataRecord where are stored all the folder fields.
   */
  @Transient
  private DataRecord folder = null;
  /**
   * A Map action -> DataRecord
   */
  @Transient
  private Map<String, DataRecord> actionData = null;

  /**
   * Default constructor
   */
  public ProcessInstanceImpl() {
    // This constructor is necessary with JAXB
  }

  @Override
  public String getInstanceId() {
    return getId();
  }

  @Override
  public void setInstanceId(String instanceId) {
    setId(instanceId);
  }

  @Override
  public String getModelId() {
    return modelId;
  }

  @Override
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  @Override
  public void addHistoryStep(HistoryStep step) {
    ((HistoryStepImpl) step).setProcessInstance(this);
    historySteps.add((HistoryStepImpl) step);

    this.currentStep = step;
  }

  @Override
  public void updateHistoryStep(HistoryStep step) {
    this.currentStep = step;
  }

  @Override
  public void addActiveState(State state) {
    Date timeOutDate = computeTimeOutDate(state, 1);
    this.addActiveState(state.getName(), timeOutDate);
  }

  private Date computeTimeOutDate(State state, int order) {
    // checks if timeout actions have been defined on the state to add
    TimeOutAction[] timeOutActions = state.getTimeOutActions();
    Date timeOutDate = null;
    if (timeOutActions != null) {
      for (TimeOutAction timeOutAction : timeOutActions) {
        if (timeOutAction.getOrder() == order) {
          // Check if an item has been mapped to timeoutdate
          Item dateItem = timeOutAction.getDateItem();
          if (dateItem != null) {
            timeOutDate = parseTimeOutFromDateField(dateItem);
          } else {
            timeOutDate = computeNextTimeOutByDelay(timeOutAction);
          }
          break;
        }
      }
    }

    return timeOutDate;
  }

  private Date parseTimeOutFromDateField(final Item dateItem) {
    Date timeOutDate = null;
    try {
      DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
      Field dateItemField = getField(dateItem.getName());
      timeOutDate = formatter.parse(dateItemField.getValue());
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return timeOutDate;
  }

  private Date computeNextTimeOutByDelay(final TimeOutAction timeOutAction) {
    // if no item set, then use delay to compute next timeout
    Calendar now = Calendar.getInstance();
    Date timeOutDate = null;
    String delay = timeOutAction.getDelay();
    final int delayAsInt = Integer.parseInt(delay.substring(0, delay.length() - 1));
    if (StringUtil.isDefined(delay) && delay.endsWith("m")) {
      now.add(Calendar.MONTH, delayAsInt);
      timeOutDate = now.getTime();
    } else if (StringUtil.isDefined(delay) && delay.endsWith("d")) {
      now.add(Calendar.DAY_OF_YEAR, delayAsInt);
      timeOutDate = now.getTime();
    } else if (StringUtil.isDefined(delay) && delay.endsWith("h")) {
      now.add(Calendar.HOUR, delayAsInt);
      timeOutDate = now.getTime();
    } else if (StringUtil.isDefined(delay) && StringUtils.isNumeric(delay)) {
      // If no unit is specified, we consider the value as a number of minutes
      now.add(Calendar.MINUTE, Integer.parseInt(delay));
      timeOutDate = now.getTime();
    } else {
      SilverLogger.getLogger(this)
          .warn(
              "Bad delay format {0} in the computation of the timout date for instance id" + " {1}",
              delay, getId());
    }
    return timeOutDate;
  }

  /**
   * Adds a state active for this instance
   * @param state the name of state to be activated
   * @param timeOutDate a timeout datetime.
   */
  private void addActiveState(String state, Date timeOutDate) {
    ActiveState activeState = new ActiveState(state);
    activeState.setProcessInstance(this);
    activeState.setTimeoutDate(timeOutDate);

    // if this active state is add in a "question" context, it must be marked as
    // in back status for a special treatment
    if (this.currentStep != null && this.currentStep.getAction().equals(QUESTION_ACTION)) {
      activeState.setBackStatus(true);
    }

    // if this state wasn't already active, add it in list of active states
    activeStates.add(activeState);

    // add this operation in undo history
    if (!inUndoProcess) {
      this.addUndoHistoryStep(ADD_ACTIVE_STATE, state);
    }
  }

  @Override
  public void removeActiveState(State state) {
    this.removeActiveState(state.getName());
  }

  /**
   * Set a state inactive for this instance
   * @param state The name of state to be deactivated
   */
  private void removeActiveState(String state) {
    ActiveState activeState = new ActiveState(state);

    // try to find and delete the right active state
    activeStates.remove(activeState);

    // add this operation in undo history
    if (!inUndoProcess) {
      this.addUndoHistoryStep("removeActiveState", state);
    }

    // computes timeout status
    computeTimeOutStatus();
  }

  /**
   * Computes time out status: instance is in timeout if at least one active state is in timeout
   */
  private void computeTimeOutStatus() {
    if (CollectionUtil.isEmpty(activeStates)) {
      setTimeoutStatus(false);
    } else {
      boolean oneTimeOutExists = false;
      for (ActiveState state : this.activeStates) {
        if (state.getTimeoutStatus() > 0) {
          oneTimeOutExists = true;
          break;
        }
      }
      setTimeoutStatus(oneTimeOutExists);
    }
  }

  @Override
  public void addTimeout(State state) {
    if (CollectionUtil.isEmpty(activeStates)) {
      return;
    }
    for (ActiveState activeState : activeStates) {
      if (activeState.getState().equals(state.getName())) {
        activeState.setTimeoutStatus(activeState.getTimeoutStatus() + 1);
        Date nextTimeOutDate = computeTimeOutDate(state, activeState.getTimeoutStatus() + 1);
        activeState.setTimeoutDate(nextTimeOutDate);
        this.setTimeoutStatus(true);
        return;
      }
    }
  }


  @Override
  public void removeTimeout(State state) {
    boolean found = false;
    if (activeStates == null || activeStates.isEmpty()) {
      return;
    }
    for (ActiveState activeState : activeStates) {
      if (activeState.getState().equals(state.getName())) {
        activeState.setTimeoutStatus(0);
      } else if (activeState.getTimeoutStatus() > 0) {
        found = true;
      }
    }

    if (!found) {
      this.setTimeoutStatus(false);
    }
  }

  @Override
  public void addWorkingUser(User user, State state, String role) {
    this.addWorkingUser(user, getStateName(state), role, null);
  }

  @Override
  public void addWorkingUser(Actor actor, State state) {
    addWorkingUser(actor.getUser(), getStateName(state), actor.getUserRoleName(),
        actor.getGroupId());
  }

  /**
   * Add an user in the working user list
   * @param user user to add
   * @param state name of state for which the user can make an action
   * @param role role name under which the user can make an action
   */
  private void addWorkingUser(User user, String state, String role, String groupId) {
    WorkingUser wkUser = new WorkingUser();

    /*
     * 3 use cases, define working user : - by userId - by a role - by a groupId
     */
    if (user != null) {
      wkUser.setUserId(user.getUserId());
    } else if (StringUtil.isDefined(groupId)) {
      wkUser.setGroupId(groupId);
    } else {
      wkUser.setUsersRole(role);
    }
    wkUser.setState(state);
    wkUser.setRole(role);
    wkUser.setProcessInstance(this);
    workingUsers.add(wkUser);

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep(ADD_WORKING_USER, user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep(ADD_WORKING_USER, state + "##" + role);
      }
    }
  }

  @Override
  public void removeWorkingUser(User user, State state, String role) {
    this.removeWorkingUser(user, state.getName(), role);
  }

  /**
   * Remove an user from the working user list
   * @param user user to remove
   * @param state name of state for which the user could make an action
   * @param role role name under which the user could make an action
   */
  private void removeWorkingUser(User user, String state, String role) {
    WorkingUser userToDelete;

    // Build virtual working user to find the true one end delete it
    userToDelete = new WorkingUser();
    if (user != null) {
      userToDelete.setUserId(user.getUserId());
    } else {
      userToDelete.setUsersRole(role);
    }
    userToDelete.setState(state);
    userToDelete.setRole(role);

    // try to find and delete the right working user
    workingUsers.remove(userToDelete);

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep(REMOVE_WORKING_USER, user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep(REMOVE_WORKING_USER, state + "##" + role);
      }
    }
  }

  @Override
  public void addInterestedUser(User user, State state, String role) {
    this.addInterestedUser(user, state.getName(), role, null);
  }

  @Override
  public void addInterestedUser(Actor actor, State state) {
    this.addInterestedUser(actor.getUser(), state.getName(), actor.getUserRoleName(), actor.
        getGroupId());
  }

  /**
   * Add an user in the interested user list
   * @param user user to add
   * @param state the name of state for which the user is interested
   * @param role role name under which the user is interested
   */
  private void addInterestedUser(User user, String state, String role, String groupId) {

    InterestedUser intUser = new InterestedUser();
    /*
     * 3 use cases, define working user : - by userId - by a role - by a groupId
     */
    if (user != null) {
      intUser.setUserId(user.getUserId());
    } else if (StringUtil.isDefined(groupId)) {
      intUser.setGroupId(groupId);
    } else {
      intUser.setUsersRole(role);
    }
    intUser.setState(state);
    intUser.setRole(role);
    intUser.setProcessInstance(this);
    interestedUsers.add(intUser);

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep(ADD_INTERESTED_USER, user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep(ADD_INTERESTED_USER, state + "##" + role);
      }
    }
  }

  @Override
  public void removeInterestedUser(User user, State state, String role) {
    this.removeInterestedUser(user, state.getName(), role);
  }


  private void removeInterestedUser(User user, String state, String role) {
    InterestedUser userToDelete;

    // Build virtual interestedUser user to find the true one end delete it
    userToDelete = new InterestedUser();
    if (user != null) {
      userToDelete.setUserId(user.getUserId());
    } else {
      userToDelete.setUsersRole(role);
    }
    userToDelete.setState(state);
    userToDelete.setRole(role);

    // try to find and delete the right interestedUser user
    interestedUsers.remove(userToDelete);

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep(REMOVE_INTERESTED_USER,
            user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep(REMOVE_INTERESTED_USER, state + "##" + role);
      }
    }
  }

  /**
   * Add a question to this instance
   * @param question the question to add
   */
  public void addQuestion(Question question) {
    questions.add((QuestionImpl) question);
  }

  @Override
  public void computeValid() {
    this.valid = !workingUsers.isEmpty();
  }

  @Override
  public ProcessModel getProcessModel() throws WorkflowException {
    if (model == null) {
      ProcessModelManager modelManager = Workflow.getProcessModelManager();
      model = modelManager.getProcessModel(modelId);
    }
    return model;
  }

  @Override
  public HistoryStep[] getHistorySteps() {
    if (historySteps != null) {
      List<HistoryStep> steps = new ArrayList<>(historySteps);
      Collections.sort(steps);
      return steps.toArray(new HistoryStep[0]);
    }
    return new HistoryStep[0];
  }

  @Override
  public HistoryStep getHistoryStep(String stepId) throws WorkflowException {
    for (HistoryStep historyStep : historySteps) {
      if (historyStep.getId().equals(stepId)) {
        return historyStep;
      }
    }
    throw new WorkflowException("ProcessInstanceImpl.getHistoryStep",
        "workflowEngine.EX_ERR_HISTORYSTEP_NOT_FOUND", INSTANCEID_PARAM + getId());
  }

  @Override
  public List<Participant> getParticipants() throws WorkflowException {
    List<Participant> participants = new ArrayList<>();
    for (HistoryStep step : historySteps) {
      User user = step.getUser();
      State state;
      if (step.getResolvedState() == null) {
        state = null;
      } else {
        state = this.getProcessModel().getState(step.getResolvedState());
      }
      ParticipantImpl participant = new ParticipantImpl(user, step.getUserRoleName(), state, step.
          getAction());
      participants.add(participant);
    }
    return participants;
  }

  @Override
  public Participant getParticipant(String resolvedState) throws WorkflowException {
    // Get the most recent step
    HistoryStep step = this.getMostRecentStepOnState(resolvedState);

    // Get the user who worked at this step
    User user;
    try {
      user = step.getUser();
    } catch (WorkflowException we) {
      user = null;
    }

    // Get the state
    State state;
    if (step.getResolvedState() == null) {
      state = null;
    } else {
      state = this.getProcessModel().getState(step.getResolvedState());
    }

    // return the participant
    return new ParticipantImpl(user, step.getUserRoleName(),
        state, step.getAction());
  }

  @Override
  public DataRecord getFolder() throws WorkflowException {
    if (folder == null) {
      String folderId = getId();

      try {
        RecordSet folderSet = getProcessModel().getFolderRecordSet();
        folder = folderSet.getRecord(folderId);

        if (folder == null) {
          createFolder();
        }
      } catch (FormException e) {
        throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_UNKNOWN_FOLDER",
            FOLDER_PARAM + folderId, e);
      }
    }

    return folder;
  }

  @Override
  public DataRecord getAllDataRecord(String role, String lang) throws WorkflowException {
    return new ProcessInstanceDataRecord(this, role, lang);
  }

  @Override
  public DataRecord getRowDataRecord(String role, String lang) throws WorkflowException {
    return new ProcessInstanceRowRecord(this, role, lang);
  }

  /**
   * Creates a new empty folder.
   */
  private void createFolder() throws WorkflowException {
    try {
      RecordSet folderSet = getProcessModel().getFolderRecordSet();
      folder = folderSet.getEmptyRecord();
      folder.setId(getId());
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_FOLDER_CREATE_FAILED",
          FOLDER_PARAM + getId(), e);
    }
  }

  @Override
  public void updateFolder(DataRecord actionData) throws WorkflowException {
    try {
      RecordSet folderSet = getProcessModel().getFolderRecordSet();

      String[] fieldNames = folderSet.getRecordTemplate().getFieldNames();
      setUpdatedFields(actionData, fieldNames);

      folderSet.save(getFolder());
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_FOLDER_UPDATE_FAILED",
          FOLDER_PARAM + getId(), e);
    }
  }

  private void setUpdatedFields(final DataRecord actionData, final String[] fieldNames)
      throws WorkflowException {
    Field updatedField;
    for (final String fieldName : fieldNames) {
      try {
        updatedField = actionData.getField(fieldName);
        if (updatedField != null) {
          setField(fieldName, updatedField);
        }
      } catch (FormException e) {
        // the field i is not updated (unknown in the action context)
      }
    }
  }

  @Override
  public Field getField(String fieldName) throws WorkflowException {
    DataRecord theFolder = getFolder();
    if (theFolder == null) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EX_ERR_GET_FOLDER",
          INSTANCEID_PARAM + getId());
    }

    try {
      Field returnedField = theFolder.getField(fieldName);
      if (returnedField == null) {
        throw new WorkflowException(PROCESS_INSTANCE_IMPL,
            "workflowEngine.EXP_UNKNOWN_ITEM",
            INSTANCEID_PARAM + getId() + ", folder." + fieldName);
      }
      return returnedField;
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_UNKNOWN_ITEM",
          INSTANCEID_PARAM + getId() + "folder." + fieldName, e);
    }
  }

  @Override
  public void setField(String fieldName, Field copiedField) throws WorkflowException {
    Field updatedField = getField(fieldName);

    try {
      if (updatedField.getTypeName().equals(copiedField.getTypeName())) {
        updatedField.setObjectValue(copiedField.getObjectValue());
      } else {
        updatedField.setValue(copiedField.getValue(""), "");
      }
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_ITEM_UPDATE_FAILED",
          INSTANCEID_PARAM + getId() + "folder." + fieldName, e);
    }
  }

  @Override
  public DataRecord getActionRecord(String actionName) throws WorkflowException {
    if (actionData == null) {
      actionData = new HashMap<>(0);
    }

    DataRecord data = actionData.get(actionName);
    if (data == null) {
      HistoryStep step = getMostRecentStep(actionName);
      if (step != null) {
        data = step.getActionRecord();
        if (data == null) {
          return null;
        }
        actionData.put(actionName, data);
      }
    }

    return data;
  }

  @Override
  public DataRecord getFormRecord(String formName, String role, String lang)
      throws WorkflowException {
    try {
      Form form = getProcessModel().getForm(formName, role);
      if (form == null) {
        return null;
      }

      String[] fieldNames = form.toRecordTemplate(role, lang).getFieldNames();
      DataRecord data = form.getDefaultRecord(role, lang, getAllDataRecord(role, lang));
      DataRecordUtil.updateFields(fieldNames, data, getFolder(), lang);

      return data;
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_FORM_READ_FAILED",
          INSTANCEID_PARAM + getId() + ",formname =" + formName, e);
    }
  }

  @Override
  public DataRecord getNewActionRecord(String actionName, String language)
      throws WorkflowException {
    try {
      Form form = getProcessModel().getActionForm(actionName);
      if (form == null) {
        return null;
      }

      DataRecord data =
          getProcessModel().getNewActionRecord(actionName, "", "", getAllDataRecord("", ""));
      Input[] inputs = form.getInputs();
      List<String> fNames;
      if (inputs != null) {
        fNames = new ArrayList<>(inputs.length);
        for (final Input input : inputs) {
          if (input != null && input.getItem() != null) {
            fNames.add(input.getItem().getName());
          }
        }
      } else {
        fNames = Collections.emptyList();
      }
      DataRecordUtil.updateFields(fNames.toArray(new String[0]), data, getFolder(), language);
      return data;
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_FORM_CREATE_FAILED",
          INSTANCEID_PARAM + getId() + ", action=" + actionName, e);
    }
  }

  @Override
  public void saveActionRecord(HistoryStep step, DataRecord actionData) throws WorkflowException {
    // special case : wysiwyg, check if data has been put into file and not kept in value field
    try {
      // first update data folder
      checkWysiwygData(step, actionData);
      updateFolder(actionData);

      // then save action record
      updateWysiwygDataWithStepId(step, actionData);
      step.setActionRecord(actionData);
    } catch (FormException e) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_FORM_CREATE_FAILED",
          INSTANCEID_PARAM + getId(), e);
    }
  }

  /**
   * Parse fields values and check ones that have wysiwyg renderer. In case of new process instance,
   * txt files may not have been created yet. if yes, value must start with "xmlWysiwygField_"
   * @param step a step in the history of this process instance.
   * @param actionData the data of the action related by the step.
   * @throws WorkflowException if an error occurs with the workflow.
   * @throws FormException if an error occurs while parsing the form.
   */
  private void checkWysiwygData(HistoryStep step, DataRecord actionData)
      throws WorkflowException, FormException {
    String actionName = step.getAction();
    Form form = getProcessModel().getActionForm(actionName);
    RecordTemplate template = form.toRecordTemplate(step.getUserRoleName(), "");
    String[] fieldNames = actionData.getFieldNames();

    for (String fieldName : fieldNames) {
      Field updatedField = actionData.getField(fieldName);
      if (updatedField == null) {
        SilverLogger.getLogger(this)
            .error("Cannot retrieve field {0} for instance id {1}", fieldName, getId());
      }
      FieldTemplate tmpl = template.getFieldTemplate(fieldName);

      if ("wysiwyg".equals(tmpl.getDisplayerName()) && updatedField != null &&
          !updatedField.isNull() &&
          !updatedField.getStringValue().startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
        WysiwygFCKFieldDisplayer displayer = new WysiwygFCKFieldDisplayer();
        PagesContext context =
            new PagesContext(DUMMY, "0", actionData.getLanguage(), false, getModelId(), DUMMY);
        context.setObjectId(getId());
        displayer.update(updatedField.getStringValue(), (TextField) updatedField, tmpl, context);
      }
    }
  }

  /**
   * Parse fields values and check ones that have wysiwyg renderer. In case of new process instance,
   * txt files may not have been created yet. if yes, value must start with "xmlWysiwygField_"
   * @param step a step in the history of this process instance.
   * @param actionData the data of the action related by the step.
   * @throws WorkflowException if an error occurs with the workflow.
   * @throws FormException if an error occurs while parsing the form.
   */
  private void updateWysiwygDataWithStepId(HistoryStep step, DataRecord actionData)
      throws WorkflowException, FormException {
    String actionName = step.getAction();
    Form form = getProcessModel().getActionForm(actionName);
    RecordTemplate template = form.toRecordTemplate(step.getUserRoleName(), "");
    String[] fieldNames = actionData.getFieldNames();

    for (String fieldName : fieldNames) {
      Field updatedField = actionData.getField(fieldName);
      FieldTemplate tmpl = template.getFieldTemplate(fieldName);

      if ("wysiwyg".equals(tmpl.getDisplayerName())) {
        WysiwygFCKFieldDisplayer displayer = new WysiwygFCKFieldDisplayer();
        PagesContext context =
            new PagesContext(DUMMY, "0", actionData.getLanguage(), false, getModelId(), DUMMY);
        context.setObjectId(getId());
        displayer.duplicateContent(updatedField, tmpl, context, "Step" + step.getId());
      }

      if ("file".equals(tmpl.getTypeName())) {
        String attachmentId = updatedField.getValue();
        if (StringUtil.isDefined(attachmentId)) {
          ResourceReference fromPK = new ResourceReference(getId(), modelId);
          ResourceReference toPK = new ResourceReference("Step" + step.getId(), modelId);

          updateFileField(updatedField, attachmentId, fromPK, toPK);
        }
      }
    }
  }

  private void updateFileField(final Field updatedField, final String attachmentId,
      final ResourceReference fromPK, final ResourceReference toPK) throws FormException {
    List<SimpleDocument> attachments =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKey(fromPK, null);
    for (SimpleDocument attachment : attachments) {
      if (attachmentId.equals(attachment.getId())) {
        SimpleDocumentPK pk =
            AttachmentServiceProvider.getAttachmentService().copyDocument(attachment, toPK);
        updatedField.setStringValue(pk.getId());
        break;
      }
    }
  }

  @Override
  public HistoryStep getMostRecentStep(String actionName) {
    Date actionDate = null;
    HistoryStep mostRecentStep = null;

    for (HistoryStep step : historySteps) {
      // if step matches the searched action, tests if the step is most recent
      // choose this step, if no previous step found or action date is more
      // recent
      if (step.getAction().equals(actionName) &&
          (mostRecentStep == null || step.getActionDate().after(actionDate))) {
        mostRecentStep = step;
        actionDate = step.getActionDate();
      }
    }

    return mostRecentStep;
  }

  @Override
  public HistoryStep getSavedStep(String userId) throws WorkflowException {
    HistoryStep savedStep = null;
    for (HistoryStep step : historySteps) {
      // if step matches the searched action, tests if the step is most recent
      if ((step.getActionStatus() == SAVED) && (step.getUser().getUserId().equals(userId))) {
        savedStep = step;
        break;
      }
    }

    return savedStep;
  }

  /**
   * Returns the most recent step where an action was performed on the given state.
   * @param stateName name of state for which we want the most recent step
   * @return the most recent step
   */
  private HistoryStep getMostRecentStepOnState(String stateName) {
    HistoryStep mostRecentStep = null;
    Date actionDate = null;
    boolean stepMatch;

    for (HistoryStep step : historySteps) {
      stepMatch = false;

      // special case : searched stateName is null or empty (the step is
      // representing the creation)
      if (stateName == null || stateName.length() == 0) {
        if (step.getResolvedState() == null || step.getResolvedState().length() == 0) {
          stepMatch = true;
        }
      } else if (step.getResolvedState() != null && step.getResolvedState().equals(stateName)) {
        stepMatch = true;
      }

      // if step matches the searched state, tests if the step is most recent
      // choose this step, if no previous step found or action date is more
      // recent
      if (stepMatch && (mostRecentStep == null || step.getActionDate().after(actionDate))) {
        mostRecentStep = step;
        actionDate = step.getActionDate();
      }
    }
    Objects.requireNonNull(mostRecentStep);
    return mostRecentStep;
  }

  @Override
  public String[] getActiveStates() {
    if (CollectionUtil.isEmpty(activeStates)) {
      return ArrayUtil.emptyStringArray();
    } else {
      List<String> stateNames = new ArrayList<>();
      for (ActiveState state : activeStates) {
        stateNames.add(state.getState());
      }
      return stateNames.toArray(new String[activeStates.size()]);
    }
  }

  @Override
  public boolean isStateInBackStatus(String stateName) {
    for (ActiveState activeState : activeStates) {
      if (activeState.getState().equals(stateName) && activeState.getBackStatus()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Actor[] getWorkingUsers() throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());
    for (WorkingUser wkUser : workingUsers) {
      actors.addAll(wkUser.toActors());
    }
    return actors.toArray(new Actor[0]);
  }

  @Override
  public Actor[] getWorkingUsers(String state) throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());
    for (WorkingUser wkUser : workingUsers) {
      if (wkUser.getState().equals(state)) {
        actors.addAll(wkUser.toActors());
      }
    }
    return actors.toArray(new Actor[0]);
  }

  @Override
  public void removeWorkingUsers(State state) {
    Iterator<WorkingUser> itWkUsers = workingUsers.iterator();
    while (itWkUsers.hasNext()) {
      WorkingUser wkUser = itWkUsers.next();
      if (wkUser.getState().equals(state.getName())) {
        // add this operation in undo history
        if (!inUndoProcess) {
          if (wkUser.getUserId() != null) {
            this.addUndoHistoryStep(REMOVE_WORKING_USER,
                wkUser.getUserId() + "##" + state.getName() + "##" + wkUser.getRole());
          } else {
            this.addUndoHistoryStep(REMOVE_WORKING_USER, state.getName() + "##" + wkUser.getRole());
          }
        }

        // remove it
        itWkUsers.remove();
      }
    }
  }

  @Override
  public void removeInterestedUsers(State state) {
    Iterator<InterestedUser> itIntUsers = interestedUsers.iterator();
    while (itIntUsers.hasNext()) {
      InterestedUser intUser = itIntUsers.next();
      if (intUser.getState().equals(state.getName())) {
        // add this operation in undo history
        if (!inUndoProcess) {
          if (intUser.getUserId() != null) {
            this.addUndoHistoryStep(REMOVE_INTERESTED_USER,
                intUser.getUserId() + "##" + state.getName() + "##" + intUser.getRole());
          } else {
            this.addUndoHistoryStep(REMOVE_INTERESTED_USER,
                state.getName() + "##" + intUser.getRole());
          }
        }

        // remove it
        itIntUsers.remove();
      }
    }
  }

  @Override
  public Actor[] getWorkingUsers(String state, String role) throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());
    for (WorkingUser wkUser : workingUsers) {
      if (wkUser.getState().equals(state) && wkUser.getRoles().contains(role)) {
        actors.addAll(wkUser.toActors());
      }
    }
    return actors.toArray(new Actor[0]);
  }

  @Override
  public String[] getAssignedStates(User user, String roleName) {
    List<String> stateNames = new ArrayList<>();
    String userId = user.getUserId();

    for (WorkingUser wkUser : workingUsers) {
      boolean userMatch = wkUser.getUserId() != null && wkUser.getUserId().equals(userId);
      boolean usersRoleMatch =
          wkUser.getUsersRole() != null && wkUser.getUsersRole().equals(roleName);
      boolean userGroupsMatch = false;
      if (StringUtil.isDefined(wkUser.getGroupId())) {
        // check if one of userGroups matches with working group
        userGroupsMatch = user.getGroupIds().contains(wkUser.getGroupId());
      }
      if (userMatch || usersRoleMatch || userGroupsMatch) {
        Stream.of(wkUser.getRole().split(",")).forEach(role -> {
          if (role.equals(roleName)) {
            stateNames.add(wkUser.getState());
          }
        });
      }

    }

    return stateNames.toArray(new String[0]);
  }

  @Override
  public String[] getAllAssignedStates(final User user) {
    List<String> groupIds = user.getGroupIds();
    Predicate<WorkingUser> isUser = u ->
        StringUtil.isDefined(u.getUserId()) && u.getUserId().equals(user.getUserId());
    Predicate<WorkingUser> sameGroup = u ->
        StringUtil.isDefined(u.getGroupId()) && groupIds.contains(u.getGroupId());
    return workingUsers.stream()
        .filter(Objects::nonNull)
        .filter(isUser.or(sameGroup))
        .map(WorkingUser::getState)
        .distinct()
        .toArray(String[]::new);
  }

  @Override
  public LockingUser getLockingUser(String state) {
    for (LockingUser lockingUser : lockingUsers) {
      if (state.equals(lockingUser.getState())) {
        return lockingUser;
      }
    }
    return null;
  }

  @Override
  public void lock(State state, User user) throws WorkflowException {
    this.lock(state.getName(), user);
  }

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  private void lock(String state, User user) throws WorkflowException {
    // Test if lock already exists
    LockingUser foundUser = null;
    for (LockingUser lockingUser : lockingUsers) {
      if (lockingUser.getState().equals(state)) {
        foundUser = lockingUser;
        break;
      }
    }

    if (foundUser != null) {
      // if lock found for this state,
      // test if user is the same as requested
      if (!foundUser.getUserId().equals(user.getUserId())) {
        throw new WorkflowException("ProcessInstanceImpl.lock",
            "workflowEngine.EX_ERR_INSTANCE_LOCKED_BY_ANOTHER_PERSON", INSTANCEID_PARAM + getId());
      } else {
        // no need to lock, already done
        return;
      }
    }

    // No previous lock, creates one.
    LockingUser searchedUser = new LockingUser();
    searchedUser.setState(state);
    searchedUser.setUserId(user.getUserId());
    searchedUser.setProcessInstance(this);
    lockingUsers.add(searchedUser);
  }

  @Override
  public void unLock(State state, User user) throws WorkflowException {
    if (state == null) {
      this.unLock("", user);
    } else {
      this.unLock(state.getName(), user);
    }
    this.setLockedByAdmin(false);
  }

  /**
   * Un-locks this instance for the given instance and state
   * @param state state that have to be un-locked
   * @param user the current locking user
   */
  private void unLock(String state, User user) throws WorkflowException {
    // Test if lock already exists
    LockingUser foundUser = null;
    for (LockingUser lockingUser : lockingUsers) {
      if (lockingUser.getState().equals(state)) {
        foundUser = lockingUser;
        break;
      }
    }

    if (foundUser == null) {
      // no need to unlock, already done
      return;
    }

    // if lock found for this state,
    // test if user is the same as requested
    if (!foundUser.getUserId().equals(user.getUserId())) {
      throw new WorkflowException("ProcessInstanceImpl.unlock",
          "workflowEngine.EX_ERR_INSTANCE_LOCKED_BY_ANOTHER_PERSON", INSTANCEID_PARAM + getId());
    }

    // Unlocks the previous one.
    LockingUser searchedUser = new LockingUser();
    searchedUser.setState(state);
    searchedUser.setUserId(user.getUserId());
    searchedUser.setProcessInstance(this);
    lockingUsers.remove(searchedUser);
  }

  @Override
  public void lock() throws WorkflowException {
    // Test if lock already exists
    if (isLockedByAdmin()) {
      throw new WorkflowException("ProcessInstanceImpl.lock()",
          "workflowEngine.EX_ERR_INSTANCE_ALREADY_LOCKED", INSTANCEID_PARAM + getId());
    }
    setLockedByAdmin(true);
  }

  @Override
  public void unLock() {
    // Test if the instance is locked
    if (isLockedByAdmin()) {
      setLockedByAdmin(false);
    }
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  @Override
  public boolean isLockedByAdmin() {
    return locked == 1;
  }

  /**
   * Set the lock Admin status of this instance
   * @param locked true is this instance is locked by admin
   */
  public void setLockedByAdmin(boolean locked) {
    this.locked = locked ? 1 : 0;
  }

  @Override
  public boolean getErrorStatus() {
    return errorStatus != 0;
  }

  @Override
  public void setErrorStatus(boolean errorStatus) {
    this.errorStatus = errorStatus ? 1 : 0;
  }

  @Override
  public boolean getTimeoutStatus() {
    return timeoutStatus == 1;
  }

  @Override
  public void setTimeoutStatus(boolean timeoutStatus) {
    this.timeoutStatus = timeoutStatus ? 1 : 0;
  }

  @Override
  public List<User> getUsersInRole(String role) {
    UserManager userManager = WorkflowHub.getUserManager();
    User[] usersInRole = userManager.getUsersInRole(role, modelId);
    return Arrays.asList(usersInRole);
  }

  @Override
  public List<User> getUsersInGroup(String groupId) {
    UserManager userManager = WorkflowHub.getUserManager();
    User[] usersInGroup = userManager.getUsersInGroup(groupId, modelId);
    return Arrays.asList(usersInGroup);
  }

  @SuppressWarnings("FuseStreamOperations")
  @Override
  public Actor[] getActors(QualifiedUsers qualifiedUsers, State state) throws WorkflowException {
    UserInRole[] userInRoles = qualifiedUsers.getUserInRoles();
    RelatedUser[] relatedUsers = qualifiedUsers.getRelatedUsers();
    RelatedGroup[] relatedGroups = qualifiedUsers.getRelatedGroups();

    // Process first "user in Role"
    List<Actor> actors = Stream.of(userInRoles)
        .map(u -> new ActorImpl(null, u.getRoleName(), state))
        .collect(Collectors.toCollection(ArrayList::new));

    // Then process related users
    setActorsFromRelatedUsers(qualifiedUsers, state, relatedUsers, actors);

    if (relatedGroups != null) {
      setActorsFromRelatedGroups(qualifiedUsers, state, relatedGroups, actors);
    }
    return actors.toArray(new Actor[0]);
  }

  private void setActorsFromRelatedGroups(final QualifiedUsers qualifiedUsers, final State state,
      final RelatedGroup[] relatedGroups, final List<Actor> actors) throws WorkflowException {
    // Finally, process related groups
    for (RelatedGroup relatedGroup : relatedGroups) {
      if (relatedGroup != null && relatedGroup.getFolderItem() != null) {
        String fieldName = relatedGroup.getFolderItem().getName();
        Field field = getField(fieldName);
        String groupId = field.getStringValue();
        if (StringUtil.isDefined(groupId)) {
          // Get the role to which affect the group
          // if no role defined in related group
          // then get the one defined in qualifiedUser
          String role = relatedGroup.getRole();
          if (role == null) {
            role = qualifiedUsers.getRole();
          }
          actors.add(new ActorImpl(null, role, state, groupId));
        }
      }
    }
  }

  private void setActorsFromRelatedUsers(final QualifiedUsers qualifiedUsers, final State state,
      final RelatedUser[] relatedUsers, final List<Actor> actors)
      throws WorkflowException {
    UserManager userManager = WorkflowHub.getUserManager();
    for (RelatedUser relatedUser : relatedUsers) {
      List<User> users = findUsersInRelation(userManager, relatedUser);

      String relation = relatedUser.getRelation();
      for (User user : users) {
        if (relation != null && relation.length() != 0 && !relation.equals("itself")) {
          user = userManager.getRelatedUser(user, relation, modelId);
        }

        // Get the role to which affect the user
        // if no role defined in related user
        // then get the one defined in qualifiedUser
        String role = relatedUser.getRole();
        if (role == null) {
          role = qualifiedUsers.getRole();
        }

        if (user != null) {
          actors.add(new ActorImpl(user, role, state));
        }
      }
    }
  }

  private List<User> findUsersInRelation(final UserManager userManager,
      final RelatedUser relatedUser)
      throws WorkflowException {
    List<User> users = new ArrayList<>();
    if (relatedUser.getParticipant() != null) {
      String resolvedState = relatedUser.getParticipant().getResolvedState();

      Participant participant = this.getParticipant(resolvedState);
      if (participant != null) {
        users.add(participant.getUser());
      }
    } else if (relatedUser.getFolderItem() != null) {
      String fieldName = relatedUser.getFolderItem().getName();
      Field field = getField(fieldName);
      if (field instanceof UserField) {
        String userId = field.getStringValue();
        if (StringUtil.isDefined(userId)) {
          users.add(userManager.getUser(userId));
        }
      } else if (field instanceof MultipleUserField) {
        MultipleUserField multipleUserField = (MultipleUserField) field;
        String[] userIds = multipleUserField.getUserIds();
        users.addAll(Arrays.asList(userManager.getUsers(userIds)));
      }
    }
    return users;
  }

  /**
   * Add a undo step in history
   * @param action action description
   * @param params params concatenated as "param1##param2...paramN"
   */
  private void addUndoHistoryStep(String action, String params) {
    UndoHistoryStep undoStep = new UndoHistoryStep();
    undoStep.setStepId(this.currentStep.getId());
    undoStep.setInstance(this);
    undoStep.setAction(action);
    undoStep.setParameters(params);

    undoSteps.add(undoStep);
  }

  private List<UndoHistoryStep> getByStepId(String id) {
    List<UndoHistoryStep> undoStepsOfStep = new ArrayList<>();
    for (UndoHistoryStep undoStep : undoSteps) {
      if (undoStep.getId().equals(id)) {
        undoStepsOfStep.add(undoStep);
      }
    }
    return undoStepsOfStep;
  }

  /**
   * Undo all atomic operations that had occurred for a given historyStep
   * @param historyStep the historyStep when the atomic operations had occurred
   * @throws WorkflowException if an error occurs.
   */
  @SuppressWarnings("StringTokenizerDelimiter")
  private void undoStep(HistoryStep historyStep) throws WorkflowException {
    try {
      // Mark this instance as being in undo process
      // to avoid storing atomic operation done here
      this.inUndoProcess = true;

      List<UndoHistoryStep> someUndoSteps = getByStepId(historyStep.getId());
      for (UndoHistoryStep undoStep : someUndoSteps) {
        String action = undoStep.getAction();
        StringTokenizer st = new StringTokenizer(undoStep.getParameters(), "##");
        if (ADD_ACTIVE_STATE.equals(action)) {
          String state = undoStep.getParameters();
          this.removeActiveState(state);
        } else if ("removeActiveState".equals(action)) {
          String state = undoStep.getParameters();
          this.addActiveState(state, null);
        } else if (ADD_WORKING_USER.equals(action)) {
          undoAddWorkingUser(st);
        } else if (REMOVE_WORKING_USER.equals(action)) {
          undoRemoveWorkingUser(st);
        } else if (ADD_INTERESTED_USER.equals(action)) {
          // The number of parameters must be : 3 or 2
          undoAddInterestedUser(st);
        } else if (REMOVE_INTERESTED_USER.equals(action)) {
          // The number of parameters must be : 3 or 2
          undoRemoveInterestedUser(st);
        } else {
          throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EXP_UNKNOWN_ACTION",
              INSTANCEID_PARAM + getId());
        }

        // as the atomic operation has been undone, remove it from undoHistory
        undoSteps.remove(undoStep);
      }
    } catch (WorkflowException we) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, "workflowEngine.EX_ERR_UNDO_STEP",
          INSTANCEID_PARAM + getId(), we);
    } finally {
      this.inUndoProcess = false;
    }
  }

  private void undoRemoveInterestedUser(final StringTokenizer st) throws WorkflowException {
    if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, WORKFLOW_ENGINE_EX_ERR_ILLEGAL_PARAMETERS,
          INSTANCEID_PARAM + getId() + ", method removeInterestedUser - found:" + st.countTokens() +
              INSTEAD_OF_2_OR_3);
    }

    String userId = (st.countTokens() == 3) ? st.nextToken() : null;
    String state = st.nextToken();
    String role = st.nextToken();
    User user = WorkflowHub.getUserManager().getUser(userId);

    this.addInterestedUser(user, state, role, null);
  }

  private void undoAddInterestedUser(final StringTokenizer st) throws WorkflowException {
    if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, WORKFLOW_ENGINE_EX_ERR_ILLEGAL_PARAMETERS,
          INSTANCEID_PARAM + getId() + ", method addInterestedUser - found:" + st.countTokens() +
              INSTEAD_OF_2_OR_3);
    }

    String userId = (st.countTokens() == 3) ? st.nextToken() : null;
    String state = st.nextToken();
    String role = st.nextToken();
    User user = WorkflowHub.getUserManager().getUser(userId);

    this.removeInterestedUser(user, state, role);
  }

  private void undoRemoveWorkingUser(final StringTokenizer st) throws WorkflowException {
    final int maxParametersCount = 3;
    final int minParametersCount = 2;
    if (st.countTokens() != maxParametersCount && st.countTokens() != minParametersCount) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, WORKFLOW_ENGINE_EX_ERR_ILLEGAL_PARAMETERS,
          INSTANCEID_PARAM + getId() + ", method removeWorkingUser - found:" + st.countTokens() +
              INSTEAD_OF_2_OR_3);
    }

    String userId = (st.countTokens() == 3) ? st.nextToken() : null;
    String state = st.nextToken();
    String role = st.nextToken();
    User user = WorkflowHub.getUserManager().getUser(userId);

    this.addWorkingUser(user, state, role, null);
  }

  private void undoAddWorkingUser(final StringTokenizer st) throws WorkflowException {
    final int maxParametersCount = 3;
    final int minParametersCount = 2;
    if (st.countTokens() != maxParametersCount && st.countTokens() != minParametersCount) {
      throw new WorkflowException(PROCESS_INSTANCE_IMPL, WORKFLOW_ENGINE_EX_ERR_ILLEGAL_PARAMETERS,
          INSTANCEID_PARAM + getId() + ", method addWorkingUser - found:" + st.countTokens() +
              INSTEAD_OF_2_OR_3);
    }

    String userId = (st.countTokens() == 3) ? st.nextToken() : null;
    String state = st.nextToken();
    String role = st.nextToken();
    User user = WorkflowHub.getUserManager().getUser(userId);

    this.removeWorkingUser(user, state, role);
    this.unLock(state, user);
  }

  @Override
  public void reDoState(String state, Date actionDate) throws WorkflowException {
    // Get the most recent step that logged an action to this state (=the action
    // that is now the One)
    HistoryStep step = this.getMostRecentStepOnState(state);

    // Undo all steps between now and the action date of above step
    HistoryStep[] steps = this.getHistorySteps();
    boolean started = false;
    boolean stop = false;

    for (int i = steps.length - 1; !stop && i >= 0; i--) {
      if (steps[i].getId().equals(step.getId())) {
        started = true;
      } else if (started) {
        this.undoStep(steps[i]);
        if (steps[i].getResolvedState().equals(state) &&
            (!steps[i].getAction().equals(QUESTION_ACTION)) &&
            (!steps[i].getAction().equals("#response#"))) {
          stop = true;
        }
      }
    }
  }

  @Override
  public HistoryStep[] getBackSteps(User user, String roleName, String stateName)
      throws WorkflowException {
    List<String> stepIds = new ArrayList<>();
    List<HistoryStep> steps = new ArrayList<>();
    HistoryStep[] allSteps = this.getHistorySteps();
    try {
      // Search for all steps that activates the given state
      // Tests if user is a working user for this state
      WorkingUser wkUser = new WorkingUser();
      wkUser.setUserId(user.getUserId());
      wkUser.setState(stateName);
      wkUser.setRole(roleName);
      if (workingUsers.contains(wkUser)) {
        JdbcSqlQuery query = JdbcSqlQuery.select("stepid from sb_workflow_undo_step")
            .where("instanceid = ? ", Integer.parseInt(getId()))
            .and("action = ? ", ADD_ACTIVE_STATE)
            .and("parameters = ? ", stateName);
        List<Integer> results = query.execute(row -> row.getInt(1));
        for (Integer result : results) {
          String stepId = String.valueOf(result);
          if (!stepIds.contains(stepId)) {
            stepIds.add(stepId);
          }
        }
      }

      // Build vector of HistoryStep found
      for (final HistoryStep allStep : allSteps) {
        ActiveState state = new ActiveState(allStep.getResolvedState());
        if (stepIds.contains(allStep.getId()) &&
            (!allStep.getAction().equals(QUESTION_ACTION)) &&
            (!allStep.getAction().equals("#response#")) &&
            (allStep.getResolvedState() != null) && (!activeStates.contains(state))) {
          steps.add(allStep);
        }
      }

      return steps.toArray(new HistoryStep[0]);
    } catch (SQLException e) {
      throw new WorkflowException("ProcessInstanceImpl.getBackSteps",
          "workflowEngine.EX_ERR_GET_BACKSTEPS", INSTANCEID_PARAM + getId(), e);
    }
  }

  /**
   * Search for the step with given id
   * @param stepId the search step id
   * @throws WorkflowException if an error occurs during the search.
   */
  private HistoryStep getStep(String stepId) throws WorkflowException {
    HistoryStep[] steps = getHistorySteps();
    HistoryStep foundStep = null;

    // Search for the step with given id
    for (int i = 0; i < steps.length && foundStep == null; i++) {
      if (steps[i].getId().equals(stepId)) {
        foundStep = steps[i];
      }
    }

    if (foundStep == null) {
      throw new WorkflowException("ProcessInstanceImpl.getStep",
          "workflowEngine.EX_ERR_HISTORYSTEP_NOT_FOUND",
          INSTANCEID_PARAM + getId() + ", stepid : " + stepId);
    }

    return foundStep;
  }

  @Override
  public State addQuestion(String content, String stepId, State fromState, User fromUser)
      throws WorkflowException {
    HistoryStep step = getStep(stepId);
    State targetState = getProcessModel().getState(step.getResolvedState());
    Participant participant = getParticipant(targetState.getName());
    // Save the question
    QuestionImpl question = new QuestionImpl(this, content,
        fromState.getName(), step.getResolvedState(), fromUser, participant.getUser());
    addQuestion(question);
    return getProcessModel().getState(step.getResolvedState());
  }

  @Override
  public State answerQuestion(String content, String questionId) throws WorkflowException {
    Question question = null;
    // search for the question with given id
    for (Question aQuestion : questions) {
      if (aQuestion.getId().equals(questionId)) {
        question = aQuestion;
      }
    }

    // if question not found, throw exception
    if (question == null) {
      throw new WorkflowException("ProcessInstanceImpl.answerQuestion",
          "workflowEngine.ERR_QUESTION_NOT_FOUND",
          INSTANCEID_PARAM + getId() + ", questionid : " + questionId);
    }
    // put the answer in question
    question.answer(content);
    // return the state where the question was asked
    return question.getTargetState();
  }

  @Override
  public Question[] getPendingQuestions(String stateName) {
    List<Question> questionsAsked = new ArrayList<>();
    for (Question question : questions) {
      if (question.getTargetState().getName().equals(stateName) &&
          question.getResponseDate() == null) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new Question[0]);
  }

  @Override
  public Question[] getSentQuestions(String stateName) {
    List<Question> questionsAsked = new ArrayList<>();

    for (Question question : questions) {
      if (question.getFromState().getName().equals(stateName) &&
          question.getResponseDate() == null) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new Question[0]);
  }

  @Override
  public Question[] getRelevantQuestions(String stateName) {
    List<Question> questionsAsked = new ArrayList<>();

    for (Question question : questions) {
      if (question.getFromState().getName().equals(stateName) &&
          question.getResponseDate() != null && question.isRelevant()) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new Question[0]);
  }

  @Override
  public void cancelQuestion(Question question) throws WorkflowException {
    // 0 - recurse if necessary
    Question[] theQuestions = getSentQuestions(question.getTargetState().getName());
    for (int i = 0; theQuestions != null && i < theQuestions.length; i++) {
      cancelQuestion(theQuestions[i]);
    }

    // 1 - make a fictive answer
    State state = answerQuestion("", question.getId());

    // 2 - remove active state
    removeActiveState(state);

    // 3 - remove working user
    HistoryStep step = getMostRecentStepOnState(state.getName());
    Objects.requireNonNull(step);
    removeWorkingUser(step.getUser(), state, step.getUserRoleName());
  }

  @Override
  public Question[] getQuestions() {
    return questions.toArray(new Question[0]);
  }

  /**
   * Add active state to the process instance
   * @param activeState state to add.
   */
  public void addActiveState(ActiveState activeState) {
    activeState.setProcessInstance(this);
    this.activeStates.add(activeState);
  }

  @Override
  public String getTitle(String role, String lang) {
    String title = null;
    Presentation template = null;

    try {
      template = getProcessModel().getPresentation();
    } catch (WorkflowException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    if (template != null) {
      title = template.getTitle(role, lang);
      try {
        LazyProcessInstanceDataRecord dataRecord =
            new LazyProcessInstanceDataRecord(this, role, lang);
        title = DataRecordUtil.applySubstitution(title, dataRecord, lang);
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }

    if (title == null) {
      title = getId();
    }

    return title;
  }

  @Override
  public ActionAndState getTimeOutAction(Date dateRef) throws WorkflowException {

    // Parse active states
    if (this.activeStates != null && !this.activeStates.isEmpty()) {
      for (ActiveState activeState : activeStates) {

        // Look for an active state with a timeoutDate in the past
        if (activeState.getTimeoutDate() != null && activeState.getTimeoutDate().before(dateRef)) {
          // found, now look which timeout is concerned
          int theTimeoutStatus = activeState.getTimeoutStatus();

          // then parse all timeoutAction to return the right one (the one with order =
          // timeoutstatus+1)
          State state = getProcessModel().getState(activeState.getState());
          TimeOutAction[] actions = state.getTimeOutActions();
          Mutable<ActionAndState> foundActionAndState = Mutable.empty();
          Stream.of(actions)
              .filter(a -> a.getOrder() == theTimeoutStatus + 1)
              .findFirst()
              .ifPresent(a -> foundActionAndState.set(new ActionAndState(a.getAction(), state)));
          return foundActionAndState.get();
        }
      }
    }

    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProcessInstanceImpl)) {
      return false;
    }
    ProcessInstance instance = (ProcessInstance) obj;
    return instance.getInstanceId().equals(getId());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (getId() != null ? getId().hashCode() : 0);
    return hash;
  }

  private String getStateName(State state) {
    return (state != null) ? state.getName() : "";
  }

  /**
   * If the 7 lists @OneToMany were eagerly, the SQL query could be super huge when getting data,
   * even for only one process instance.<br> As an example, if it exists 5 lines into each list, the
   * SQL query loads 5x5x5x5x5x5x5=78125 lines for one process instance!!! Not amazing...<br> Graph
   * entity is not the solution as it should do here the same thing that the EAGER fetch directive
   * on @ManyToOne.<br> So there is yet 2 options:
   * <ul>
   *   <li>loading the things by sub SQL queries</li>
   *   <li>performing manually the data fetching after the process instance load</li>
   * </ul>
   * Second option is chosen.
   * @return itself.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  ProcessInstanceImpl fetchAll() {
    historySteps.size();
    undoSteps.size();
    questions.size();
    interestedUsers.size();
    workingUsers.size();
    lockingUsers.size();
    activeStates.size();
    return this;
  }
}