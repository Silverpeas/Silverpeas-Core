/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.CollectionUtil.RuptureContext;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.workflow.api.UpdatableProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.util.CollectionUtil.findNextRupture;

/**
 * A ProcessInstanceManager implementation
 */
@Singleton
public class ProcessInstanceManagerImpl implements UpdatableProcessInstanceManager {

  private static final String MODEL_ID_CRITERION = "I.modelId = ?";
  private static final String SB_WORKFLOW_PROCESS_INSTANCE_TABLE = "SB_Workflow_ProcessInstance I";

  @Inject
  private SilverpeasCalendar calendar;

  @Inject
  private ProcessInstanceRepository repository;

  @Override
  public List<ProcessInstance> getProcessInstances(String peasId, User user, String role)
      throws WorkflowException {
    return getProcessInstances(peasId, user, role, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProcessInstance> getProcessInstances(String peasId, User user, String role,
      String[] userRoles, String[] userGroupIds) throws WorkflowException {

    final SilverpeasList<ProcessInstanceImpl> instances;
    final JdbcSqlQuery select;

    if ("supervisor".equals(role)) {
      select = JdbcSqlQuery
          .createSelect("I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus")
          .from(SB_WORKFLOW_PROCESS_INSTANCE_TABLE)
          .where(MODEL_ID_CRITERION, peasId)
          .orderBy("I.instanceId DESC");
    } else {
      select = JdbcSqlQuery
          .createSelect("*")
          .from("(")

          .addSqlPart("SELECT I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus")
          .from("SB_Workflow_InterestedUser intUser")
          .join(SB_WORKFLOW_PROCESS_INSTANCE_TABLE).on("I.instanceId = intUser.instanceId")
          .where(MODEL_ID_CRITERION, peasId)
          .and("(intUser.userId = ?", user.getUserId());
      if (ArrayUtil.isNotEmpty(userRoles)) {
        select
          .or("intUser.usersRole").in(userRoles);
      }
      if (ArrayUtil.isNotEmpty(userGroupIds)) {
        select
          .or("(intUser.groupId is not null")
          .and("intUser.groupId").in(userGroupIds)
          .addSqlPart(")");
      }
      select
          .addSqlPart(")")
          .and("intUser.role = ?", role)

          .union()

          .addSqlPart("SELECT I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus")
          .from("SB_Workflow_WorkingUser wkUser")
          .join(SB_WORKFLOW_PROCESS_INSTANCE_TABLE).on("I.instanceId = wkUser.instanceId")
          .where(MODEL_ID_CRITERION, peasId)
          .and("(wkUser.userId = ?", user.getUserId());
      if (ArrayUtil.isNotEmpty(userRoles)) {
        select
          .or("wkUser.usersRole").in(userRoles);
      }
      if (ArrayUtil.isNotEmpty(userGroupIds)) {
        select
          .or("(wkUser.groupId is not null")
          .and("wkUser.groupId").in(userGroupIds)
          .addSqlPart(")");
      }
      select
          .addSqlPart(")")
          .and("(wkUser.role = ?", role)
          .or("wkUser.role like ?", "%," + role)
          .or("wkUser.role like ?", role + ",%")
          .or("wkUser.role like ?)", "%," + role + ",%")

          .addSqlPart(") u")
          .orderBy("u.instanceId DESC");
    }

    try (Connection connection = DBUtil.openConnection()) {
      final List<Integer> instanceIds = new LinkedList<>();
      instances = select.executeWith(connection, r -> {
        ProcessInstanceImpl instance = new ProcessInstanceImpl();
        int i = 1;
        final int instanceId = r.getInt(i++);
        instance.setInstanceId(String.valueOf(instanceId));
        instance.setModelId(r.getString(i++));
        instance.setLockedByAdmin(r.getBoolean(i++));
        instance.setErrorStatus(r.getBoolean(i++));
        instance.setTimeoutStatus(r.getBoolean(i));
        instanceIds.add(instanceId);
        return instance;
      });

      if (!instanceIds.isEmpty()) {

        // getting History
        final RuptureContext<ProcessInstanceImpl> ruptureContext = RuptureContext.newOne(instances);
        JdbcSqlQuery.executeBySplittingOn(instanceIds, (idBatch, result)-> JdbcSqlQuery
            .createSelect("*")
            .from("SB_Workflow_HistoryStep")
            .where("instanceId").in(idBatch)
            .orderBy("instanceId DESC, id ASC")
            .executeWith(connection, r -> {
              int i = 1;
              final String instanceId = r.getString(i++);
              final HistoryStepImpl historyStep = new HistoryStepImpl(r.getInt(i++));
              historyStep.setUserId(r.getString(i++));
              historyStep.setUserRoleName(r.getString(i++));
              historyStep.setAction(r.getString(i++));
              historyStep.setActionDate(r.getDate(i++));
              historyStep.setResolvedState(r.getString(i++));
              historyStep.setResultingState(r.getString(i++));
              historyStep.setActionStatus(ActionStatus.from(r.getInt(i)));
              findNextRupture(ruptureContext, p -> p.getInstanceId().equals(instanceId))
                  .ifPresent(p -> p.addHistoryStep(historyStep));
              return null;
            }));

        // getting Active States
        ruptureContext.reset();
        JdbcSqlQuery.executeBySplittingOn(instanceIds, (idBatch, result)-> JdbcSqlQuery
            .createSelect("*")
            .from("SB_Workflow_ActiveState")
            .where("instanceId").in(idBatch)
            .orderBy("instanceId DESC, id ASC")
            .executeWith(connection, rs -> {
              int i = 1;
              final ActiveState state = new ActiveState(rs.getInt(i++));
              final String instanceId = rs.getString(i++);
              state.setState(rs.getString(i++));
              state.setBackStatus(rs.getBoolean(i++));
              state.setTimeoutStatus(rs.getInt(i));
              findNextRupture(ruptureContext, p -> p.getInstanceId().equals(instanceId))
                  .ifPresent(p -> p.addActiveState(state));
              return null;
            }));
      }

      return (List) instances;
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstances",
          "EX_ERR_GET_INSTANCES", "sql query : " + select.getSqlQuery(), se);
    }
  }

  /**
   * Get the process instances for a given instance id
   * @param instanceId id of searched instance
   * @return the searched process instance
   * @throws WorkflowException
   */
  @Override
  public ProcessInstance getProcessInstance(String instanceId) throws WorkflowException {
    try {
      return repository.getById(instanceId);
    } catch (Exception e) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstance",
          "EX_ERR_GET_INSTANCE", "Process instance #"+instanceId+" not found !");
    }
  }

  /**
   * Creates a new process instance
   * @param modelId model id
   * @return the new ProcessInstance object
   * @throws WorkflowException
   */
  @Override
  @Transactional
  public synchronized ProcessInstance createProcessInstance(String modelId)
      throws WorkflowException {
    ProcessInstanceImpl instance = new ProcessInstanceImpl();
    instance.setModelId(modelId);
    repository.save(instance);
    return instance;
  }

  /**
   * Removes a new process instance
   * @param instanceId instance id
   * @throws WorkflowException
   */
  @Override
  @Transactional
  public void removeProcessInstance(String instanceId) throws WorkflowException {
    ProcessInstance instance = repository.getById(instanceId);

    // Delete forms data associated with this instance
    removeProcessInstanceData(instance);

    WorkflowHub.getErrorManager().removeErrorsOfInstance(instanceId);

    repository.delete((ProcessInstanceImpl) instance);
  }

  public void removeProcessInstanceData(ProcessInstance instance) throws WorkflowException {
    String id = instance.getInstanceId();
    String componentId = instance.getModelId();

    // delete attachments
    AttachmentServiceProvider.getAttachmentService().deleteAllAttachments(id, componentId);

    // delete folder
    try {
      RecordSet folderRecordSet = instance.getProcessModel().getFolderRecordSet();
      folderRecordSet.delete(id);
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceManagerImpl.removeProcessInstanceData",
          "EX_ERR_CANT_REMOVE_FOLDER", e);
    }

    // delete history steps
    HistoryStep[] steps = instance.getHistorySteps();
    if (steps != null) {
      for (HistoryStep step : steps) {
        if (!"#question#".equals(step.getAction()) && !"#response#".equals(step.getAction())) {
          step.deleteActionRecord();
        }
      }
    }

    // delete associated to do
    calendar.removeToDoFromExternal("useless", componentId, id + "##%");
  }

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  @Transactional
  public void lock(ProcessInstance instance, State state, User user) throws WorkflowException {
    ProcessInstance copyInstance = repository.getById(instance.getInstanceId());
    copyInstance.lock(state, user);
    repository.save((ProcessInstanceImpl) copyInstance);
  }

  /**
   * unlocks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  @Transactional
  public void unlock(ProcessInstance instance, State state, User user) throws WorkflowException {
    ProcessInstance copyInstance = repository.getById(instance.getInstanceId());
    copyInstance.unLock(state, user);
    repository.save((ProcessInstanceImpl) copyInstance);
  }

  /**
   * Build a new HistoryStep.
   * @return an object implementing HistoryStep interface.
   */
  @Override
  public HistoryStep createHistoryStep() {
    return new HistoryStepImpl();
  }

  /**
   * Builds an actor from a user and a role.
   */
  @Override
  public Actor createActor(User user, String roleName, State state) {
    return new ActorImpl(user, roleName, state);
  }

  /**
   * Get the list of process instances for which timeout date is over
   * @return an array of ProcessInstance objects
   * @throws WorkflowException
   */
  @SuppressWarnings("unchecked")
  @Override
  public SilverpeasList<ProcessInstance> getTimeOutProcessInstances() throws WorkflowException {
    try {
      JdbcSqlQuery query = JdbcSqlQuery
          .createSelect("instanceid")
          .from("SB_Workflow_ActiveState")
          .where("timeoutDate < ? ", new Timestamp((new Date()).getTime()));
      List<String> ids = query.execute(row -> String.valueOf(row.getInt(1)));
      Set<String> instanceIds = new HashSet<>(ids);
      return (SilverpeasList) repository.getById(instanceIds);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_GET_TIMEOUT_INSTANCES", se);
    }
  }
}