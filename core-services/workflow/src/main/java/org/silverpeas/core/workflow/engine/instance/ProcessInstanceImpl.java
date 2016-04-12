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
package org.silverpeas.core.workflow.engine.instance;

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
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is one implementation of interface UpdatableProcessInstance. It uses Castor library
 * to
 * read/write process instance information in database
 * @table SB_Workflow_ProcessInstance
 * @key-generator MAX
 */
public class ProcessInstanceImpl implements UpdatableProcessInstance {

  /**
   * Abstract process model
   */
  private transient ProcessModel model = null;
  /**
   * Flag that indicates validity of this processInstance
   */
  private transient boolean valid = false;
  /**
   * Flag that indicates if this instance is locked by admin
   * @field-name locked
   * @get-method isLockedByAdmin
   * @set-method setLockedByAdmin
   */
  private boolean locked = false;
  /**
   * Flag that indicates if this instance status is "error"
   * @field-name errorStatus
   */
  private boolean errorStatus = false;
  /**
   * Flag that indicates if this instance is in an active state for a long long time
   * @field-name timeoutStatus
   */
  private boolean timeoutStatus = false;
  /**
   * the instance Id
   * @field-name instanceId
   * @sql-type integer
   * @primary-key
   */
  private String instanceId = null;
  /**
   * the model Id
   * @field-name modelId
   */
  private String modelId = null;
  /**
   * Vector of all history step that trace events occured on this process instance
   * @field-name historySteps
   * @field-type HistoryStepImpl
   * @many-key instanceId
   * @set-method castor_setHistorySteps
   * @get-method castor_getHistorySteps
   */
  private Vector<HistoryStep> historySteps = null;
  /**
   * Vector of all questions asked on this process instance
   * @field-name questions
   * @field-type QuestionImpl
   * @many-key instanceId
   * @set-method castor_setQuestions
   * @get-method castor_getQuestions
   */
  private Vector<Question> questions = null;
  /**
   * The current history step used to add actomic operations in history
   */
  private transient HistoryStep currentStep = null;
  /**
   * the status of this instance regarding 'undo' process while true, the atomic operations are not
   * stored anymore
   */
  private transient boolean inUndoProcess = false;
  /**
   * Vector of all users who can see this process instance
   * @field-name interestedUsers
   * @field-type InterestedUser
   * @many-key instanceId
   * @set-method castor_setInterestedUsers
   * @get-method castor_getInterestedUsers
   */
  private Vector<InterestedUser> interestedUsers = null;
  /**
   * Vector of all users who can act on this process instance
   * @field-name workingUsers
   * @field-type WorkingUser
   * @set-method castor_setWorkingUsers
   * @get-method castor_getWorkingUsers
   */
  private Vector<WorkingUser> workingUsers = null;
  /**
   * Vector of all users who can have locked a state of this process instance
   * @field-name lockingUsers
   * @field-type LockingUser
   * @many-key instanceId
   * @set-method castor_setLockingUsers
   * @get-method castor_getLockingUsers
   */
  private Vector<LockingUser> lockingUsers = null;
  /**
   * Vector of all states that are due to be resolved for this process instance
   * @field-name activeStates
   * @field-type ActiveState
   * @many-key instanceId
   * @set-method castor_setActiveStates
   * @get-method castor_getActiveStates
   */
  private Vector<ActiveState> activeStates = null;
  /**
   * The DataRecord where are stored all the folder fields.
   */
  private transient DataRecord folder = null;
  /**
   * A Map action -> DataRecord
   */
  private transient Map<String, DataRecord> actionData = null;

  /**
   * Default constructor
   */
  public ProcessInstanceImpl() {
    historySteps = new Vector();
    questions = new Vector<>();
    interestedUsers = new Vector<>();
    workingUsers = new Vector<>();
    activeStates = new Vector<>();
    lockingUsers = new Vector<>();
    valid = false;
    folder = null;
    actionData = null;
  }

  /**
   * Get the workflow instance id
   * @return instance id
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set the workflow instance id
   * @param instanceId instance id
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Get the workflow model id
   * @return model id
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * Set the workflow model id
   * @param modelId model id
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  /**
   * Add an history step for this instance
   * @param step the history step to add
   */
  public void addHistoryStep(HistoryStep step) throws WorkflowException {
    ((HistoryStepImpl) step).setProcessInstance(this);
    historySteps.add(step);

    this.currentStep = step;
  }

  /**
   * Update an history step for this instance
   * @param step the history step to update
   */
  public void updateHistoryStep(HistoryStep step) throws WorkflowException {
    this.currentStep = step;
  }

  /**
   * Set a state active for this instance
   * @param state State to be activated
   */
  public void addActiveState(State state) throws WorkflowException {
    Date timeOutDate = computeTimeOutDate(state, 1);
    this.addActiveState(state.getName(), timeOutDate);
  }

  private Date computeTimeOutDate(State state, int order) {
    // checks if timeout actions have been defined on the state to add
    TimeOutAction[] timeOutActions = state.getTimeOutActions();
    Date timeOutDate = null;
    if (timeOutActions != null && timeOutActions.length > 0) {
      for (TimeOutAction timeOutAction : timeOutActions) {
        if (timeOutAction.getOrder() == order) {
          Calendar now = Calendar.getInstance();

          // Check if an item has been mapped to timeoutdate
          Item dateItem = timeOutAction.getDateItem();
          if (dateItem != null) {
            try {
              DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
              Field dateItemField = getField(dateItem.getName());
              timeOutDate = formatter.parse(dateItemField.getValue());
            } catch (Exception e) {
              SilverTrace.warn("workflowEngine", "ProcessInstanceImpl.computeTimeOutDate",
                  "root.ERR_BAD_DATE_ITEM",
                  "instanceid:" + getInstanceId() + ", date item =" + dateItem.getName());
            }
          } // if no item set, then use delay to compute next timeout
          else {
            String delay = timeOutAction.getDelay();
            if ((StringUtil.isDefined(delay)) && (delay.endsWith("d"))) {
              now.add(Calendar.DAY_OF_YEAR,
                  Integer.parseInt(delay.substring(0, delay.length() - 1)));
              timeOutDate = now.getTime();
            } else if ((StringUtil.isDefined(delay)) && (delay.endsWith("h"))) {
              now.add(Calendar.HOUR, Integer.parseInt(delay.substring(0, delay.length() - 1)));
              timeOutDate = now.getTime();
            } else {
              SilverTrace.warn("workflowEngine", "ProcessInstanceImpl.computeTimeOutDate",
                  "root.ERR_BAD_DELAY_FORMAT",
                  "instanceid:" + getInstanceId() + ", delay =" + delay);
            }
          }
          break;
        }
      }
    }

    return timeOutDate;
  }

  /**
   * Set a state active for this instance
   * @param state The name of state to be activated
   */
  private void addActiveState(String state, Date timeOutDate) throws WorkflowException {
    ActiveState activeState = new ActiveState(state);
    activeState.setProcessInstance(this);
    activeState.setTimeoutDate(timeOutDate);

    // if this active state is add in a "question" context, it must be marked as
    // in back status for a special treatment
    if (this.currentStep != null && this.currentStep.getAction().equals("#question#")) {
      activeState.setBackStatus(true);
    }

    // if this state wasn't already active, add it in list of active states
    if (!activeStates.contains(activeState)) {
      activeStates.add(activeState);
    }

    // add this operation in undo history
    if (!inUndoProcess) {
      this.addUndoHistoryStep("addActiveState", state);
    }
  }

  /**
   * Set a state inactive for this instance
   * @param state State to be desactivated
   */
  public void removeActiveState(State state) throws WorkflowException {
    this.removeActiveState(state.getName());
  }

  /**
   * Set a state inactive for this instance
   * @param state The name of state to be desactivated
   */
  private void removeActiveState(String state) throws WorkflowException {
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
   * Computes time out status : instance is in timeout if at least one active state is in timeout
   */
  private void computeTimeOutStatus() {
    if (this.activeStates == null || this.activeStates.isEmpty()) {
      this.timeoutStatus = false;
    } else {
      boolean oneTimeOutExists = false;
      for (ActiveState state : this.activeStates) {
        if (state.getTimeoutStatus() > 0) {
          oneTimeOutExists = true;
          break;
        }
      }
      this.timeoutStatus = oneTimeOutExists;
    }
  }

  /**
   * @param state
   */
  @Override
  public void addTimeout(State state) throws WorkflowException {
    boolean found = false;

    if (activeStates == null || activeStates.isEmpty()) {
      return;
    }
    for (int i = 0; (!found) && i < activeStates.size(); i++) {
      ActiveState activeState = activeStates.get(i);
      if (activeState.getState().equals(state.getName())) {
        found = true;
        activeState.setTimeoutStatus(activeState.getTimeoutStatus() + 1);
        Date nextTimeOutDate = computeTimeOutDate(state, activeState.getTimeoutStatus() + 1);
        activeState.setTimeoutDate(nextTimeOutDate);
        this.setTimeoutStatus(true);
      }
    }
  }

  /**
   * @param state
   */
  @Override
  public void removeTimeout(State state) throws WorkflowException {
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

  /**
   * Add an user in the working user list
   * @param user user to add
   * @param state state for which the user can make an action
   * @param role role name under which the user can make an action
   * @throws WorkflowException
   */
  @Override
  public void addWorkingUser(User user, State state, String role) throws WorkflowException {
    this.addWorkingUser(user, state.getName(), role, null);
  }

  @Override
  public void addWorkingUser(Actor actor, State state) throws WorkflowException {
    addWorkingUser(actor.getUser(), state.getName(), actor.getUserRoleName(), actor.getGroupId());
  }

  /**
   * Add an user in the working user list
   * @param user user to add
   * @param state name of state for which the user can make an action
   * @param role role name under which the user can make an action
   */
  private void addWorkingUser(User user, String state, String role, String groupId)
      throws WorkflowException {
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
    if (!workingUsers.contains(wkUser)) {
      workingUsers.add(wkUser);
    }

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep("addWorkingUser", user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep("addWorkingUser", state + "##" + role);
      }
    }
  }

  /**
   * Remove an user from the working user list
   * @param user user to remove
   * @param state state for which the user could make an action
   * @param role role name under which the user could make an action
   */
  @Override
  public void removeWorkingUser(User user, State state, String role) throws WorkflowException {
    this.removeWorkingUser(user, state.getName(), role);
  }

  /**
   * Remove an user from the working user list
   * @param user user to remove
   * @param state name of state for which the user could make an action
   * @param role role name under which the user could make an action
   */
  private void removeWorkingUser(User user, String state, String role) throws WorkflowException {
    WorkingUser userToDelete = null;

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
        this.addUndoHistoryStep("removeWorkingUser", user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep("removeWorkingUser", state + "##" + role);
      }
    }
  }

  /**
   * Add an user in the interested user list
   * @param user user to add
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   * @throws WorkflowException
   */
  public void addInterestedUser(User user, State state, String role) throws WorkflowException {
    this.addInterestedUser(user, state.getName(), role, null);
  }

  @Override
  public void addInterestedUser(Actor actor, State state) throws WorkflowException {
    this.addInterestedUser(actor.getUser(), state.getName(), actor.getUserRoleName(), actor.
        getGroupId());
  }

  /**
   * Add an user in the interested user list
   * @param user user to add
   * @param state the name of state for which the user is interested
   * @param role role name under which the user is interested
   */
  private void addInterestedUser(User user, String state, String role, String groupId)
      throws WorkflowException {

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
    if (!interestedUsers.contains(intUser)) {
      interestedUsers.add(intUser);
    }

    // add this operation in undo history
    if (!inUndoProcess) {
      if (user != null) {
        this.addUndoHistoryStep("addInterestedUser", user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep("addInterestedUser", state + "##" + role);
      }
    }
  }

  /**
   * Remove an user from the interested user list
   * @param user user to remove
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   * @throws WorkflowException
   */
  public void removeInterestedUser(User user, State state, String role) throws WorkflowException {
    this.removeInterestedUser(user, state.getName(), role);
  }

  /**
   * Remove an user from the interested user list
   * @param user user to remove
   * @param state the name of state for which the user is interested
   * @param role role name under which the user is interested
   */
  private void removeInterestedUser(User user, String state, String role) throws WorkflowException {
    InterestedUser userToDelete = null;

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
        this.addUndoHistoryStep("removeInterestedUser",
            user.getUserId() + "##" + state + "##" + role);
      } else {
        this.addUndoHistoryStep("removeInterestedUser", state + "##" + role);
      }
    }
  }

  /**
   * Add a question for this instance
   * @param question the question to add
   * @throws WorkflowException
   */
  public void addQuestion(Question question) throws WorkflowException {
    questions.add(question);
  }

  public void computeValid() {
    this.valid = (workingUsers.size() > 0);
  }

  /**
   * @return ProcessModel
   * @throws WorkflowException
   */
  public ProcessModel getProcessModel() throws WorkflowException {
    if (model == null) {
      ProcessModelManager modelManager = Workflow.getProcessModelManager();
      model = modelManager.getProcessModel(modelId);
    }
    return model;
  }

  /**
   * Creates this instance in database
   * @return the newly created instance id
   * @throws WorkflowException
   */
  public String create() throws WorkflowException {
    Database db = null;
    try {
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      db.create(this);
      db.commit();
      return this.getInstanceId();
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceImpl.create",
          "workflowEngine.EX_ERR_CASTOR_CREATE_INSTANCE", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Permanently removes this instance from database
   * @throws WorkflowException
   */
  public void delete() throws WorkflowException {
    Database db = null;
    try {
      db = WorkflowJDOManager.getDatabase();
      synchronized (db) {
        db.begin();
        db.remove(this);
        db.commit();
      }
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceImpl.delete",
          "workflowEngine.EX_ERR_CASTOR_DELETE_INSTANCE", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Store modifications of this instance in database
   * @throws WorkflowException
   */
  @Override
  public void update() throws WorkflowException {
    Database db = null;
    try {
      db = WorkflowJDOManager.getDatabase();
      synchronized (db) {
        db.begin();
        db.update(this);
        db.commit();
      }
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceImpl.update",
          "workflowEngine.EX_ERR_CASTOR_UPDATE_INSTANCE", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * @return HistoryStep[]
   */
  @Override
  public HistoryStep[] getHistorySteps() {
    if (historySteps != null) {
      Collections.sort(historySteps);
      return historySteps.toArray(new HistoryStep[historySteps.size()]);
    }
    return null;
  }

  /**
   * @return HistoryStep
   * @throws WorkflowException
   */
  public HistoryStep getHistoryStep(String stepId) throws WorkflowException {
    for (HistoryStep historyStep : historySteps) {
      if (historyStep.getId().equals(stepId)) {
        return historyStep;
      }
    }
    throw new WorkflowException("ProcessInstanceImpl.getHistoryStep",
        "workflowEngine.EX_ERR_HISTORYSTEP_NOT_FOUND", "instanceid=" + getInstanceId());
  }

  /**
   * @return Vector
   */
  public Vector<Participant> getParticipants() throws WorkflowException {
    Vector<Participant> participants = new Vector<>();
    for (int i = 0; i < historySteps.size(); i++) {
      HistoryStepImpl step = (HistoryStepImpl) historySteps.get(i);
      User user;
      try {
        user = WorkflowHub.getUserManager().getUser(step.getUserId());
      } catch (WorkflowException we) {
        user = null;
      }
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

  /**
   * Get the last user who resolved the given state
   * @param resolvedState the resolved state
   * @return this user as a Participant object
   * @throws WorkflowException
   */
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

  /**
   * Returns the folder as a DataRecord
   */
  public DataRecord getFolder() throws WorkflowException {
    if (folder == null) {
      String folderId = instanceId;

      try {
        RecordSet folderSet = getProcessModel().getFolderRecordSet();
        folder = folderSet.getRecord(folderId);

        if (folder == null) {
          createFolder();
        }
      } catch (FormException e) {
        throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_UNKNOWN_FOLDER",
            "folder=" + folderId, e);
      }
    }

    return folder;
  }

  /**
   * Returns a data record with all the accessible data in this instance.
   */
  public DataRecord getAllDataRecord(String role, String lang) throws WorkflowException {
    return new ProcessInstanceDataRecord(this, role, lang);
  }

  /**
   * Returns a data record with all the main data in this instance.
   */
  public DataRecord getRowDataRecord(String role, String lang) throws WorkflowException {
    return new ProcessInstanceRowRecord(this, role, lang);
  }

  /**
   * Creates a new empty folder.
   */
  private void createFolder() throws WorkflowException {
    String folderId = instanceId;

    try {

      RecordSet folderSet = getProcessModel().getFolderRecordSet();
      folder = folderSet.getEmptyRecord();
      folder.setId(folderId);
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_FOLDER_CREATE_FAILED",
          "folder=" + folderId, e);
    }
  }

  /**
   * Updates the folder with the data filled within an action.
   */
  private void updateFolder(DataRecord actionData) throws WorkflowException {
    try {
      RecordSet folderSet = getProcessModel().getFolderRecordSet();

      String fieldNames[] = folderSet.getRecordTemplate().getFieldNames();
      Field updatedField = null;

      for (int i = 0; i < fieldNames.length; i++) {
        try {
          updatedField = actionData.getField(fieldNames[i]);
          if (updatedField == null) {
            continue;
          }
        } catch (FormException e) {
          // the field i is not updated (unknown in the action context)
          continue;
        }

        setField(fieldNames[i], updatedField);
      }

      folderSet.save(getFolder());
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_FOLDER_UPDATE_FAILED",
          "folder=" + instanceId, e);
    }
  }

  /**
   * Returns the required field from the folder.
   */
  @Override
  public Field getField(String fieldName) throws WorkflowException {
    DataRecord folder = getFolder();
    if (folder == null) {
      throw new WorkflowException("ProcessInstanceImpl.getField",
          "workflowEngine.EX_ERR_GET_FOLDER", "instanceid=" + getInstanceId());
    }

    try {
      Field returnedField = folder.getField(fieldName);
      if (returnedField == null) {
        throw new WorkflowException("ProcessInstanceImpl.getField",
            "workflowEngine.EXP_UNKNOWN_ITEM",
            "instanceid=" + getInstanceId() + ", folder." + fieldName);
      }
      return returnedField;
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl.getField", "workflowEngine.EXP_UNKNOWN_ITEM",
          "instanceid=" + getInstanceId() + "folder." + fieldName, e);
    }
  }

  /**
   * Update the named field with the value of the given field.
   * @param fieldName
   * @param copiedField
   * @throws WorkflowException
   */
  public void setField(String fieldName, Field copiedField) throws WorkflowException {
    Field updatedField = getField(fieldName);

    try {
      if (updatedField.getTypeName().equals(copiedField.getTypeName())) {
        updatedField.setObjectValue(copiedField.getObjectValue());
      } else {
        updatedField.setValue(copiedField.getValue(""), "");
      }
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_ITEM_UPDATE_FAILED",
          "instanceid=" + getInstanceId() + "folder." + fieldName, e);
    }
  }

  /**
   * Get the data associated to the given action
   * @param actionName action name
   * @return
   * @throws WorkflowException
   */
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

  /**
   * @param formName
   * @param role
   * @param lang
   * @return DataRecord
   * @throws WorkflowException
   */
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
      DataRecordUtil.updateFields(fieldNames, data, getFolder());

      return data;
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_FORM_READ_FAILED",
          "instanceid=" + getInstanceId() + ",formname =" + formName, e);
    }
  }

  /**
   * Get a new data record associated to the given action
   * @param actionName action name
   * @return
   * @throws WorkflowException
   */
  @Override
  public DataRecord getNewActionRecord(String actionName) throws WorkflowException {
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
        for (int i = 0; i < inputs.length; i++) {
          if (inputs[i] != null && inputs[i].getItem() != null) {
            fNames.add(inputs[i].getItem().getName());
          }
        }
      } else {
        fNames = Collections.emptyList();
      }
      DataRecordUtil.updateFields(fNames.toArray(new String[fNames.size()]), data, getFolder());
      return data;
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_FORM_CREATE_FAILED",
          "instanceid=" + getInstanceId() + ", action=" + actionName, e);
    }
  }

  /**
   * Set the form associated to the given action
   * @param step
   * @param actionData
   */
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
      throw new WorkflowException("ProcessInstanceImpl", "workflowEngine.EXP_FORM_CREATE_FAILED",
          "instanceid=" + getInstanceId(), e);
    }
  }

  /**
   * Parse fields values and check ones that have wysiwyg displayer. In case of new process
   * instance, txt files may not have been created yet. if yes, value must start with
   * "xmlWysiwygField_"
   * @param step
   * @param actionData
   * @throws WorkflowException
   * @throws FormException
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
        SilverTrace.error("workflowEngine", "ProcessInstanceImpl.checkWysiwygData",
            "root.MSG_GEN_ENTER_METHOD",
            "instanceid=" + getInstanceId() + ", cannot retrieve field : " + fieldName);
      }
      FieldTemplate tmpl = template.getFieldTemplate(fieldName);

      if ("wysiwyg".equals(tmpl.getDisplayerName())) {
        if ((!updatedField.isNull()) &&
            (!updatedField.getStringValue().startsWith(WysiwygFCKFieldDisplayer.dbKey))) {
          WysiwygFCKFieldDisplayer displayer = new WysiwygFCKFieldDisplayer();
          PagesContext context =
              new PagesContext("dummy", "0", actionData.getLanguage(), false, getModelId(),
                  "dummy");
          context.setObjectId(instanceId);
          displayer.update(updatedField.getStringValue(), (TextField) updatedField, tmpl, context);
        }
      }
    }
  }

  /**
   * Parse fields values and check ones that have wysiwyg displayer. In case of new process
   * instance, txt files may not have been created yet. if yes, value must start with
   * "xmlWysiwygField_"
   * @param step
   * @param actionData
   * @throws WorkflowException
   * @throws FormException
   */
  private void updateWysiwygDataWithStepId(HistoryStep step, DataRecord actionData)
      throws WorkflowException, FormException {
    String actionName = step.getAction();
    Form form = getProcessModel().getActionForm(actionName);
    RecordTemplate template = form.toRecordTemplate(step.getUserRoleName(), "");
    String[] fieldNames = actionData.getFieldNames();

    for (int i = 0; i < fieldNames.length; i++) {
      String fieldName = fieldNames[i];
      Field updatedField = actionData.getField(fieldName);
      FieldTemplate tmpl = template.getFieldTemplate(fieldNames[i]);

      if ("wysiwyg".equals(tmpl.getDisplayerName())) {
        WysiwygFCKFieldDisplayer displayer = new WysiwygFCKFieldDisplayer();
        PagesContext context =
            new PagesContext("dummy", "0", actionData.getLanguage(), false, getModelId(), "dummy");
        context.setObjectId(instanceId);
        displayer.duplicateContent(updatedField, tmpl, context, "Step" + step.getId());
      }

      if ("file".equals(tmpl.getTypeName())) {
        String attachmentId = updatedField.getValue();
        if (StringUtil.isDefined(attachmentId)) {
          ForeignPK fromPK = new ForeignPK(instanceId, modelId);
          ForeignPK toPK = new ForeignPK("Step" + step.getId(), modelId);

          List<SimpleDocument> attachments = AttachmentServiceProvider
              .getAttachmentService().listDocumentsByForeignKey(fromPK, null);
          for (SimpleDocument attachment : attachments) {
            if (attachmentId.equals(attachment.getId())) {
              SimpleDocumentPK pk = AttachmentServiceProvider
                  .getAttachmentService().copyDocument(attachment, toPK);
              updatedField.setStringValue(pk.getId());
              break;
            }
          }
        }
      }
    }
  }

  /**
   * Returns the most recent step where this action was performed.
   */
  public HistoryStep getMostRecentStep(String actionName) {
    Date actionDate = null;
    HistoryStep mostRecentStep = null;
    HistoryStep step = null;

    for (int i = 0; i < historySteps.size(); i++) {
      step = historySteps.get(i);

      // if step matches the searched action, tests if the step is most recent
      if (step.getAction().equals(actionName)) {
        // choose this step, if no previous step found or action date is more
        // recent
        if (mostRecentStep == null || step.getActionDate().after(actionDate)) {
          mostRecentStep = step;
          actionDate = step.getActionDate();
        }
      }
    }

    return mostRecentStep;
  }

  /**
   * Get step saved by given user id.
   * @throws WorkflowException
   */
  public HistoryStep getSavedStep(String userId) throws WorkflowException {
    HistoryStep savedStep = null;
    HistoryStep step = null;
    for (int i = 0; i < historySteps.size(); i++) {
      step = historySteps.get(i);

      // if step matches the searched action, tests if the step is most recent
      if ((step.getActionStatus() == 3) && (step.getUser().getUserId().equals(userId))) {
        savedStep = step;
        break;
      }
    }

    return savedStep;
  }

  /**
   * Get the most recent step where an action has been performed on the given state. If no action
   * has been performed on this state, return the step that activate this state.
   */
  public HistoryStep getMostRecentStep(State state) {
    try {
      if (state == null) {
        return null;
      }

      HistoryStep mostRecentStep = getMostRecentStepOnState(state.getName());

      if (mostRecentStep != null) {
        return mostRecentStep;
      }

      return getOriginStep(state.getName());
    } catch (WorkflowException we) {
      return null;
    }
  }

  /**
   * Returns the most recent step where an action was performed on the given state.
   * @param stateName name of state for which we want the most recent step
   * @return the most recent step
   */
  private HistoryStep getMostRecentStepOnState(String stateName) {
    HistoryStepImpl step = null;
    HistoryStepImpl mostRecentStep = null;
    Date actionDate = null;
    boolean stepMatch = false;

    for (int i = 0; i < historySteps.size(); i++) {
      stepMatch = false;
      step = (HistoryStepImpl) historySteps.get(i);

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
      if (stepMatch) {
        // choose this step, if no previous step found or action date is more
        // recent
        if (mostRecentStep == null || step.getActionDate().after(actionDate)) {
          mostRecentStep = step;
          actionDate = step.getActionDate();
        }
      }
    }

    return mostRecentStep;
  }

  /**
   * Returns the most recent step where an action caused the activation of the given state
   * @param stateName name of state
   * @return the most recent step where an action caused the activation of the given state
   */
  private HistoryStep getOriginStep(String stateName) throws WorkflowException {
    OQLQuery query = null;
    QueryResults results;
    String stepId = null;
    Database db = null;
    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      query = db.
          getOQLQuery(
              "SELECT undoStep FROM UndoHistoryStep " +
                  "undoStep " +
                  "WHERE undoStep.instanceId = $1 " + "AND undoStep.action = \"addActiveState\" " +
                  "AND undoStep.parameters = $2");

      // Execute the query
      query.bind((Integer.parseInt(instanceId)));
      query.bind(stateName);
      results = query.execute();

      while (results.hasMore()) {
        UndoHistoryStep undoStep = (UndoHistoryStep) results.next();
        stepId = undoStep.getStepId();
      }

      db.commit();
    } catch (WorkflowException we) {
      throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
          "workflowEngine.EX_ERR_UNDO_STEP", "instanceid=" + getInstanceId(), we);
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstances",
          "workflowEngine.EX_ERR_CASTOR_UNDO_STEP", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

    if (stepId != null) {
      return getHistoryStep(stepId);
    } else {
      return null;
    }
  }

  /**
   * @return String[]
   */
  public String[] getActiveStates() {
    String[] states = null;

    if (activeStates == null || activeStates.isEmpty()) {
      states = ArrayUtil.EMPTY_STRING_ARRAY;
    } else {
      states = new String[activeStates.size()];
      for (int i = 0; i < activeStates.size(); i++) {
        states[i] = activeStates.get(i).getState();
      }
    }

    return states;
  }

  /**
   * Test is a active state is in back status
   * @param stateName name of active state
   * @return true if resolution of active state involves a cancel of actions
   */
  public boolean isStateInBackStatus(String stateName) {
    boolean result = false;

    for (int i = 0; i < activeStates.size(); i++) {
      if ((activeStates.get(i).getState()).equals(stateName) &&
          activeStates.get(i).getBackStatus() == true) {
        result = true;
      }
    }

    return result;
  }

  /**
   * @return Actor[]
   */
  public Actor[] getWorkingUsers() throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());
    for (int i = 0; i < workingUsers.size(); i++) {
      WorkingUser wkUser = workingUsers.get(i);
      actors.addAll(wkUser.toActors());
    }
    return actors.toArray(new Actor[actors.size()]);
  }

  /**
   * @param state
   * @return Actor[]
   */
  public Actor[] getWorkingUsers(String state) throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());

    for (int i = 0; i < workingUsers.size(); i++) {
      WorkingUser wkUser = workingUsers.get(i);
      if (wkUser.getState().equals(state)) {
        actors.addAll(wkUser.toActors());
      }
    }
    return actors.toArray(new Actor[actors.size()]);
  }

  @Override
  public void removeWorkingUsers(State state) throws WorkflowException {
    Iterator<WorkingUser> itWkUsers = workingUsers.iterator();
    while (itWkUsers.hasNext()) {
      WorkingUser wkUser = itWkUsers.next();
      if (wkUser.getState().equals(state.getName())) {
        // add this operation in undo history
        if (!inUndoProcess) {
          if (wkUser.getUserId() != null) {
            this.addUndoHistoryStep("removeWorkingUser",
                wkUser.getUserId() + "##" + state.getName() + "##" + wkUser.getRole());
          } else {
            this.addUndoHistoryStep("removeWorkingUser", state.getName() + "##" + wkUser.getRole());
          }
        }

        // remove it
        itWkUsers.remove();
      }
    }
  }

  @Override
  public void removeInterestedUsers(State state) throws WorkflowException {
    Iterator<InterestedUser> itIntUsers = interestedUsers.iterator();
    while (itIntUsers.hasNext()) {
      InterestedUser intUser = itIntUsers.next();
      if (intUser.getState().equals(state.getName())) {
        // add this operation in undo history
        if (!inUndoProcess) {
          if (intUser.getUserId() != null) {
            this.addUndoHistoryStep("removeInterestedUser",
                intUser.getUserId() + "##" + state.getName() + "##" + intUser.getRole());
          } else {
            this.addUndoHistoryStep("removeInterestedUser",
                state.getName() + "##" + intUser.getRole());
          }
        }

        // remove it
        itIntUsers.remove();
      }
    }
  }

  /**
   * @param state
   * @return Actor[]
   */
  public Actor[] getWorkingUsers(String state, String role) throws WorkflowException {
    List<Actor> actors = new ArrayList<>(workingUsers.size());
    for (WorkingUser wkUser : workingUsers) {
      if (wkUser.getState().equals(state) && wkUser.getRoles().contains(role)) {
        actors.addAll(wkUser.toActors());
      }
    }
    return actors.toArray(new Actor[actors.size()]);
  }

  /**
   * Returns all the state name assigned to the user.
   */
  public String[] getAssignedStates(User user, String roleName) throws WorkflowException {
    List<String> stateNames = new ArrayList<>();
    String userId = user.getUserId();

    for (WorkingUser wkUser : workingUsers) {
      boolean userMatch = wkUser.getUserId() != null && wkUser.getUserId().equals(userId);
      boolean usersRoleMatch =
          wkUser.getUsersRole() != null && wkUser.getUsersRole().equals(roleName);
      boolean userGroupsMatch = false;
      if (StringUtil.isDefined(wkUser.getGroupId())) {
        // check if one of userGroups matches with working group
        if (user.getGroupIds() != null) {
          userGroupsMatch = user.getGroupIds().contains(wkUser.getGroupId());
        }
      }
      boolean wkUserMatch = userMatch || usersRoleMatch || userGroupsMatch;
      if (wkUserMatch) {
        for (String role : wkUser.getRole().split(",")) {
          if (role.equals(roleName)) {
            stateNames.add(wkUser.getState());
          }
        }
      }

    }

    return stateNames.toArray(new String[stateNames.size()]);
  }

  /**
   * @param state
   * @return LockingUser
   */
  public LockingUser getLockingUser(String state) throws WorkflowException {
    // Constructs a new LockingUser to proceed search
    LockingUser searchedUser = new LockingUser();
    searchedUser.setState(state);

    int indexUser = lockingUsers.indexOf(searchedUser);
    if (indexUser != -1) {
      LockingUser foundUser = lockingUsers.get(indexUser);
      return foundUser;
    }
    return null;
  }

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
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
    LockingUser searchedUser = new LockingUser();
    LockingUser foundUser = null;
    searchedUser.setState(state);
    searchedUser.setUserId(user.getUserId());
    searchedUser.setProcessInstance(this);

    int indexUser = lockingUsers.indexOf(searchedUser);
    if (indexUser != -1) {
      foundUser = lockingUsers.get(indexUser);
    }

    if (foundUser != null) {
      // if lock found for this state,
      // test if user is the same as requested
      if (!foundUser.getUserId().equals(user.getUserId())) {
        throw new WorkflowException("ProcessInstanceImpl.lock",
            "workflowEngine.EX_ERR_INSTANCE_LOCKED_BY_ANOTHER_PERSON",
            "instanceid=" + getInstanceId());
      } else // no need to lock, already done
      {
        return;
      }
    }

    // No previous lock, creates one.
    lockingUsers.add(searchedUser);
  }

  /**
   * Un-locks this instance for the given instance and state
   * @param state state that have to be un-locked
   * @param user the current locking user
   */
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
    LockingUser searchedUser = new LockingUser();
    LockingUser foundUser = null;
    searchedUser.setState(state);
    searchedUser.setUserId(user.getUserId());
    searchedUser.setProcessInstance(this);

    int indexUser = lockingUsers.indexOf(searchedUser);
    if (indexUser != -1) {
      foundUser = lockingUsers.get(indexUser);
    }

    if (foundUser == null) {
      // no need to unlock, already done
      return;
    }

    // if lock found for this state,
    // test if user is the same as requested
    if (!foundUser.getUserId().equals(user.getUserId())) {
      throw new WorkflowException("ProcessInstanceImpl.unlock",
          "workflowEngine.EX_ERR_INSTANCE_LOCKED_BY_ANOTHER_PERSON",
          "instanceid=" + getInstanceId());
    }

    // Unlocks the previous one.
    lockingUsers.remove(searchedUser);
  }

  /**
   * Lock this instance for the engine
   */
  public void lock() throws WorkflowException {
    // Test if lock already exists
    if (locked) {
      throw new WorkflowException("ProcessInstanceImpl.lock()",
          "workflowEngine.EX_ERR_INSTANCE_ALREADY_LOCKED", "instanceid=" + getInstanceId());
    }

    locked = true;
  }

  /**
   * Unlock this instance for the engine
   */
  public void unLock() throws WorkflowException {
    // Test if the instance is locked
    if (locked) {
      locked = false;
    }
  }

  /**
   * Get the validity state of this instance
   * @return true is this instance is valid
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Get the lock Admin status of this instance
   * @return true is this instance is locked by admin
   */
  public boolean isLockedByAdmin() {
    return locked;
  }

  public int isLockedByAdminCastor() {
    if (isLockedByAdmin()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Set the lock Admin status of this instance
   * @param locked true is this instance is locked by admin
   */
  public void setLockedByAdmin(boolean locked) {
    this.locked = locked;
  }

  public void setLockedByAdminCastor(int locked) {
    this.locked = (locked == 1);
  }

  /**
   * Get the error status of this instance
   * @return true if this instance is in error
   */
  public boolean getErrorStatus() {
    return errorStatus;
  }

  public int getErrorStatusCastor() {
    if (getErrorStatus()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Set the error status of this instance
   * @param errorStatus true if this instance is in error
   */
  public void setErrorStatus(boolean errorStatus) {
    this.errorStatus = errorStatus;
  }

  public void setErrorStatusCastor(int errorStatus) {
    this.errorStatus = (errorStatus == 1);
  }

  /**
   * Get the timeout status of this instance
   * @return true if this instance is in an active state for a long long time
   */
  public boolean getTimeoutStatus() {
    return timeoutStatus;
  }

  public int getTimeoutStatusCastor() {
    if (getTimeoutStatus()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Set the timeout status of this instance
   * @param timeoutStatus true if this instance is in an active state for a long long time
   */
  public void setTimeoutStatus(boolean timeoutStatus) {
    this.timeoutStatus = timeoutStatus;
  }

  public void setTimeoutStatusCastor(int timeoutStatus) {
    this.timeoutStatus = (timeoutStatus == 1);
  }

  public List<User> getUsersInRole(String role) throws WorkflowException {
    UserManager userManager = WorkflowHub.getUserManager();
    User[] usersInRole = userManager.getUsersInRole(role, modelId);
    return Arrays.asList(usersInRole);
  }

  public List<User> getUsersInGroup(String groupId) throws WorkflowException {
    UserManager userManager = WorkflowHub.getUserManager();
    User[] usersInGroup = userManager.getUsersInGroup(groupId);
    return Arrays.asList(usersInGroup);
  }

  /**
   * Computes tuples role/user/state (stored in an Actor object) from a QualifiedUsers object
   * @param qualifiedUsers Users defined by their role or by a relation with a participant
   * @param state State for which these user were/may be actors
   * @return tuples role/user as an array of Actor objects
   */
  public Actor[] getActors(QualifiedUsers qualifiedUsers, State state) throws WorkflowException {
    List<Actor> actors = new ArrayList<>();
    UserManager userManager = WorkflowHub.getUserManager();
    UserInRole[] userInRoles = qualifiedUsers.getUserInRoles();
    RelatedUser[] relatedUsers = qualifiedUsers.getRelatedUsers();
    RelatedGroup[] relatedGroups = qualifiedUsers.getRelatedGroups();

    // Process first "user in Role"
    for (int i = 0; i < userInRoles.length; i++) {
      actors.add(new ActorImpl(null, userInRoles[i].getRoleName(), state));
    }

    // Then process related users
    for (int i = 0; i < relatedUsers.length; i++) {
      User[] users = null;
      String relation = relatedUsers[i].getRelation();

      if (relatedUsers[i].getParticipant() != null) {
        String resolvedState = relatedUsers[i].getParticipant().getResolvedState();

        Participant participant = this.getParticipant(resolvedState);
        if (participant != null) {
          users = ArrayUtil.add(users, participant.getUser());
        }
      } else if (relatedUsers[i].getFolderItem() != null) {
        String fieldName = relatedUsers[i].getFolderItem().getName();
        Field field = getField(fieldName);
        if (field instanceof UserField) {
          String userId = field.getStringValue();
          if (StringUtil.isDefined(userId)) {
            users = ArrayUtil.add(users, userManager.getUser(userId));
          }
        } else if (field instanceof MultipleUserField) {
          MultipleUserField multipleUserField = (MultipleUserField) field;
          String[] userIds = multipleUserField.getUserIds();
          users = ArrayUtil.addAll(users, userManager.getUsers(userIds));
        }
      }

      if (!ArrayUtil.isEmpty(users)) {
        for (User user : users) {
          if (relation != null && relation.length() != 0 && !relation.equals("itself")) {
            user = userManager.getRelatedUser(user, relation, modelId);
          }

          // Get the role to which affect the user
          // if no role defined in related user
          // then get the one defined in qualifiedUser
          String role = relatedUsers[i].getRole();
          if (role == null) {
            role = qualifiedUsers.getRole();
          }

          if (user != null) {
            actors.add(new ActorImpl(user, role, state));
          }
        }
      }
    }
    if (relatedGroups != null) {
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
    return actors.toArray(new Actor[actors.size()]);
  }

  /**
   * Add a undo step in history
   * @param action action description
   * @param params params concatenated as "param1##param2...paramN"
   */
  private void addUndoHistoryStep(String action, String params) throws WorkflowException {
    UndoHistoryStep undoStep = new UndoHistoryStep();
    undoStep.setStepId(this.currentStep.getId());
    undoStep.setInstanceId(instanceId);
    undoStep.setAction(action);
    undoStep.setParameters(params);

    Database db = null;
    try {
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      db.create(undoStep);
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceImpl.addUndoHistoryStep",
          "workflowEngine.EX_ERR_CASTOR_CREATE_UNDO_HISTORYSTEP", "instanceid=" + getInstanceId(),
          pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Undo all atomic operations that had occured for a given historyStep
   * @param historyStep the historyStep when the atomic operations had occured
   */
  private void undoStep(HistoryStep historyStep) throws WorkflowException {
    OQLQuery query = null;
    QueryResults results;
    Database db = null;

    try {
      // Mark this instance as beeing in undo process
      // to avoid storing atomic operation done here
      this.inUndoProcess = true;

      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      query = db.
          getOQLQuery(
              "SELECT undoStep FROM UndoHistoryStep " +
                  "undoStep " +
                  "WHERE undoStep.stepId = $1 ");

      // Execute the query
      query.bind((Integer.parseInt(historyStep.getId())));
      results = query.execute();

      while (results.hasMore()) {
        UndoHistoryStep undoStep = (UndoHistoryStep) results.next();
        String action = undoStep.getAction();
        StringTokenizer st = new StringTokenizer(undoStep.getParameters(), "##");

        if (action.equals("addActiveState")) {
          String state = undoStep.getParameters();
          this.removeActiveState(state);
        } else if (action.equals("removeActiveState")) {
          String state = undoStep.getParameters();
          this.addActiveState(state, null);
        } else if (action.equals("addWorkingUser")) {
          // The number of parameters must be : 3 or 2
          if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
            throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
                "workflowEngine.EX_ERR_ILLEGAL_PARAMETERS", "instanceid=" + getInstanceId() +
                ", method addWorkingUser - found:" + st.countTokens() + " instead of 2 or 3");
          }

          String userId = (st.countTokens() == 3) ? st.nextToken() : null;
          String state = st.nextToken();
          String role = st.nextToken();
          User user = WorkflowHub.getUserManager().getUser(userId);

          this.removeWorkingUser(user, state, role);
          this.unLock(state, user);
        } else if (action.equals("removeWorkingUser")) {
          // The number of parameters must be : 3 or 2
          if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
            throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
                "workflowEngine.EX_ERR_ILLEGAL_PARAMETERS", "instanceid=" + getInstanceId() +
                ", method addWorkingUser - found:" + st.countTokens() + " instead of 2 or 3");
          }

          String userId = (st.countTokens() == 3) ? st.nextToken() : null;
          String state = st.nextToken();
          String role = st.nextToken();
          User user = WorkflowHub.getUserManager().getUser(userId);

          this.addWorkingUser(user, state, role, null);
        } else if (action.equals("addInterestedUser")) {
          // The number of parameters must be : 3 or 2
          if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
            throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
                "workflowEngine.EX_ERR_ILLEGAL_PARAMETERS", "instanceid=" + getInstanceId() +
                ", method addInterestedUser - found:" + st.countTokens() + " instead of 2 or 3");
          }

          String userId = (st.countTokens() == 3) ? st.nextToken() : null;
          String state = st.nextToken();
          String role = st.nextToken();
          User user = WorkflowHub.getUserManager().getUser(userId);

          this.removeInterestedUser(user, state, role);
        } else if (action.equals("removeInterestedUser")) {
          // The number of parameters must be : 3 or 2
          if ((st.countTokens() != 3) && (st.countTokens() != 2)) {
            throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
                "workflowEngine.EX_ERR_ILLEGAL_PARAMETERS", "instanceid=" + getInstanceId() +
                ", method removeInterestedUser - found:" + st.countTokens() + " instead of 2 or 3");
          }

          String userId = (st.countTokens() == 3) ? st.nextToken() : null;
          String state = st.nextToken();
          String role = st.nextToken();
          User user = WorkflowHub.getUserManager().getUser(userId);

          this.addInterestedUser(user, state, role, null);
        } else {
          throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
              "workflowEngine.EXP_UNKNOWN_ACTION", "instanceid=" + getInstanceId());
        }

        // as the atomic operation has been undone, remove it from undoHistory
        db.remove(undoStep);
      }

      db.commit();
    } catch (WorkflowException we) {
      throw new WorkflowException("ProcessInstanceManagerImpl.undoStep",
          "workflowEngine.EX_ERR_UNDO_STEP", "instanceid=" + getInstanceId(), we);
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstances",
          "workflowEngine.EX_ERR_CASTOR_UNDO_STEP", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
      this.inUndoProcess = false;
    }
  }

  /**
   * Cancel all the atomic operations since the step where first action had occured
   * @param state the name of state where ac action has been discussed
   * @param actionDate date of state re-resolving
   */
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
            (!steps[i].getAction().equals("#question#")) &&
            (!steps[i].getAction().equals("#response#"))) {
          stop = true;
        }
      }
    }
  }

  /**
   * Get all the steps where given user (with given role) can go back from the given state
   * @param user user that can do the back actions
   * @param roleName role name of this user
   * @param roleName role name of this user
   * @param stateName name of state where user want to go back from
   * @return an array of HistoryStep objects
   */
  public HistoryStep[] getBackSteps(User user, String roleName, String stateName)
      throws WorkflowException {
    String stepId = null;
    List<String> stepIds = new ArrayList<>();
    List<HistoryStep> steps = new ArrayList<>();
    OQLQuery query = null;
    QueryResults results;
    HistoryStep[] allSteps = this.getHistorySteps();
    Database db = null;
    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      query = db.
          getOQLQuery(
              "SELECT undoStep FROM UndoHistoryStep " +
                  "undoStep " +
                  "WHERE undoStep.instanceId = $1 " + "AND undoStep.action = $2 " +
                  "AND undoStep.parameters = $3 ");

      // Search for all steps that activates the given state
      // Tests if user is a working user for this state
      WorkingUser wkUser = new WorkingUser();
      wkUser.setUserId(user.getUserId());
      wkUser.setState(stateName);
      wkUser.setRole(roleName);
      if (workingUsers.contains(wkUser)) {
        // Execute the query
        query.bind(Integer.parseInt(instanceId));
        query.bind("addActiveState");
        query.bind(stateName);
        results = query.execute();

        while (results.hasMore()) {
          stepId = ((UndoHistoryStep) results.next()).getStepId();
          if (!stepIds.contains(stepId)) {
            stepIds.add(stepId);
          }
        }
      }
      db.commit();

      // Build vector of HistoryStep found
      for (int i = 0; i < allSteps.length; i++) {
        ActiveState state = new ActiveState(allSteps[i].getResolvedState());

        if (stepIds.contains(allSteps[i].getId()) &&
            (!allSteps[i].getAction().equals("#question#")) &&
            (!allSteps[i].getAction().equals("#response#")) &&
            (allSteps[i].getResolvedState() != null) && (!activeStates.contains(state))) {
          steps.add(allSteps[i]);
        }
      }

      return steps.toArray(new HistoryStep[steps.size()]);
    } catch (WorkflowException we) {
      throw new WorkflowException("ProcessInstanceImpl.getBackSteps",
          "workflowEngine.EX_ERR_GET_BACKSTEPS", "instanceid=" + getInstanceId(), we);
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceImpl.getBackSteps",
          "workflowEngine.EX_ERR_CASTOR_GET_BACKSTEPS", "instanceid=" + getInstanceId(), pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Search for the step with given id
   * @param stepId the search step id
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
          "instanceid=" + getInstanceId() + ", stepid : " + stepId);
    }

    return foundStep;
  }

  /**
   * Add a question
   * @param content question text
   * @param stepId id of destination step for the question
   * @param fromState the state where the question was asked
   * @param fromUser the user who asked the question
   * @return The state to which the question is
   * @throws WorkflowException
   */
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

  /**
   * Answer a question
   * @param content response text
   * @param questionId id of question corresponding to this response
   * @return The state where the question was asked
   * @throws WorkflowException
   */
  @Override
  public State answerQuestion(String content, String questionId) throws WorkflowException {
    Question question = null;
    // search for the question with given id
    for (int i = 0; question == null && i < questions.size(); i++) {
      if (questions.get(i).getId().equals(questionId)) {
        question = questions.get(i);
      }
    }
    // if question not found, throw exception
    if (question == null) {
      throw new WorkflowException("ProcessInstanceImpl.answerQuestion",
          "workflowEngine.ERR_QUESTION_NOT_FOUND",
          "instanceid=" + getInstanceId() + ", questionid : " + questionId);
    }
    // put the answer in question
    question.answer(content);
    // return the state where the question was asked
    return question.getTargetState();
  }

  /**
   * Get all the questions asked to the given state
   * @param stateName given state name
   * @return all the questions (not yet answered) asked to the given state
   */
  @Override
  public Question[] getPendingQuestions(String stateName) {
    List<Question> questionsAsked = new ArrayList<>();
    for (int i = 0; i < questions.size(); i++) {
      Question question = questions.get(i);
      if (question.getTargetState().getName().equals(stateName) &&
          question.getResponseDate() == null) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new Question[questionsAsked.size()]);
  }

  /**
   * Get all the questions asked from the given state
   * @param stateName given state name
   * @return all the questions (not yet answered) asked from the given state
   */
  public Question[] getSentQuestions(String stateName) {
    Vector<Question> questionsAsked = new Vector<>();
    Question question = null;

    for (int i = 0; i < questions.size(); i++) {
      question = questions.get(i);
      if (question.getFromState().getName().equals(stateName) &&
          question.getResponseDate() == null) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new QuestionImpl[questionsAsked.size()]);
  }

  /**
   * Get all the questions asked from the given state and that have been aswered
   * @param stateName given state name
   * @return all the answered questions asked from the given state
   */
  public Question[] getRelevantQuestions(String stateName) {
    Vector<Question> questionsAsked = new Vector<>();
    Question question = null;

    for (int i = 0; i < questions.size(); i++) {
      question = questions.get(i);
      if (question.getFromState().getName().equals(stateName) &&
          question.getResponseDate() != null && question.isRelevant()) {
        questionsAsked.add(question);
      }
    }
    return questionsAsked.toArray(new QuestionImpl[questionsAsked.size()]);
  }

  /**
   * Cancel a question without response 1 - make a fictive answer 2 - remove active state 3 -
   * remove
   * working user 4 - recurse in question target state, if questions have been asked in cascade
   * @param question the question to cancel
   */
  public void cancelQuestion(Question question) throws WorkflowException {
    // 0 - recurse if necessary
    Question[] questions = getSentQuestions(question.getTargetState().getName());
    for (int i = 0; questions != null && i < questions.length; i++) {
      cancelQuestion(questions[i]);
    }

    // 1 - make a fictive answer
    State state = answerQuestion("", question.getId());

    // 2 - remove active state
    removeActiveState(state);

    // 3 - remove working user
    HistoryStep step = getMostRecentStepOnState(state.getName());
    removeWorkingUser(step.getUser(), state, step.getUserRoleName());
  }

  /**
   * Get all the questions asked in this processInstance
   * @return all the questions
   */
  public Question[] getQuestions() {
    return questions.toArray(new QuestionImpl[questions.size()]);
  }

  // METHODS FOR CASTOR

  /**
   * Set the instance history steps
   * @param historySteps history steps
   */
  public void castor_setHistorySteps(Vector historySteps) {
    this.historySteps = historySteps;
  }

  /**
   * Get the instance history steps
   * @return history steps as a Vector
   */
  public Vector castor_getHistorySteps() {
    return historySteps;
  }

  /**
   * Set the instance questions
   * @param questions questions
   */
  public void castor_setQuestions(Vector<Question> questions) {
    this.questions = questions;
  }

  /**
   * Get the instance questions
   * @return questions as a Vector
   */
  public Vector<Question> castor_getQuestions() {
    return questions;
  }

  /**
   * Set users who can see this process instance
   * @param interestedUsers users as a Vector
   * @return
   */
  public void castor_setInterestedUsers(Vector<InterestedUser> interestedUsers) {
    this.interestedUsers = interestedUsers;
  }

  /**
   * Get users who can see this process instance
   * @return users as a Vector
   */
  public Vector<InterestedUser> castor_getInterestedUsers() {
    return interestedUsers;
  }

  /**
   * Set users who can act on this process instance
   * @param workingUsers users as a Vector
   * @return
   */
  public void castor_setWorkingUsers(Vector<WorkingUser> workingUsers) {
    this.workingUsers = workingUsers;
  }

  /**
   * Get users who can act on this process instance
   * @return users as a Vector
   */
  public Vector<WorkingUser> castor_getWorkingUsers() {
    return workingUsers;
  }

  /**
   * Set users who have locked a state of this process instance
   * @param lockingUsers users as a Vector
   */
  public void castor_setLockingUsers(Vector<LockingUser> lockingUsers) {
    this.lockingUsers = lockingUsers;
  }

  /**
   * Get users who have locked a state of this process instance
   * @return users as a Vector
   */
  public Vector<LockingUser> castor_getLockingUsers() {
    return lockingUsers;
  }

  /**
   * Set states that are due to be resolved for this process instance
   * @param activeStates states as a Vector
   */
  public void castor_setActiveStates(Vector<ActiveState> activeStates) {
    this.activeStates = activeStates;
  }

  /**
   * Get states that are due to be resolved for this process instance
   * @return states as a Vector
   */
  public Vector<ActiveState> castor_getActiveStates() {
    return activeStates;
  }

  /**
   * Returns this instance title.
   * @param role
   * @param lang
   * @return
   */
  public String getTitle(String role, String lang) {
    String title = null;
    Presentation template = null;

    try {
      template = getProcessModel().getPresentation();
    } catch (WorkflowException e) {
    }
    if (template != null) {
      title = template.getTitle(role, lang);
      LazyProcessInstanceDataRecord dataRecord =
          new LazyProcessInstanceDataRecord(this, role, lang);
      title = DataRecordUtil.applySubstitution(title, dataRecord, lang);
    }

    if (title == null) {
      title = "" + instanceId;
    }

    return title;
  }

  /**
   * Returns the timeout action to be launched after given date
   * @throws WorkflowException
   */
  public ActionAndState getTimeOutAction(Date dateRef) throws WorkflowException {

    // Parse active states
    if (this.activeStates != null && !this.activeStates.isEmpty()) {
      for (ActiveState activeState : activeStates) {

        // Look for an active state with a timeoutDate in the past
        if (activeState.getTimeoutDate() != null && activeState.getTimeoutDate().before(dateRef)) {
          // found, now look which timeout is concerned
          int timeoutStatus = activeState.getTimeoutStatus();

          // then parse all timeoutAction to return the right one (the one with order =
          // timeoutstatus+1)
          State state = getProcessModel().getState(activeState.getState());
          TimeOutAction[] actions = state.getTimeOutActions();
          for (int i = 0; actions != null && i < actions.length; i++) {
            if (actions[i].getOrder() == (timeoutStatus + 1)) {
              return new ActionAndState(actions[i].getAction(), state);
            }
          }
        }
      }
    }

    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ProcessInstanceImpl)) {
      return false;
    }
    ProcessInstance instance = (ProcessInstance) obj;
    return instance.getInstanceId().equals(this.instanceId);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    return hash;
  }
}
