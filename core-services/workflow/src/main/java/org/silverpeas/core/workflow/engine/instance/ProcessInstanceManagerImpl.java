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
package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.workflow.api.UpdatableProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A ProcessInstanceManager implementation
 */
@Singleton
public class ProcessInstanceManagerImpl implements UpdatableProcessInstanceManager {

  private static String COLUMNS =
      " I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus ";

  @Inject
  private SilverpeasCalendar calendar;

  @Inject
  private ProcessInstanceRepository repository;

  @Override
  public ProcessInstance[] getProcessInstances(String peasId, User user, String role)
      throws WorkflowException {
    return getProcessInstances(peasId, user, role, null, null);
  }

  @Override
  public ProcessInstance[] getProcessInstances(String peasId, User user, String role,
      String[] userRoles, String[] userGroupIds) throws WorkflowException {
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    StringBuilder selectQuery = new StringBuilder();
    List<ProcessInstanceImpl> instances = new ArrayList<>();
    try {
      con = DBUtil.openConnection();

      if ("supervisor".equals(role)) {
        selectQuery.append("SELECT * from SB_Workflow_ProcessInstance instance where modelId = ?");
        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, peasId);
      } else {
        selectQuery.append("select ").append(COLUMNS)
            .append(" from SB_Workflow_ProcessInstance I ");
        selectQuery.append("where I.modelId = ? ");
        selectQuery.append("and exists (");
        selectQuery.
            append(
                "select instanceId from SB_Workflow_InterestedUser intUser where I.instanceId = " +
                    "intUser.instanceId and (");
        selectQuery.append("intUser.userId = ? ");
        if (ArrayUtil.isNotEmpty(userRoles)) {
          selectQuery.append(" or intUser.usersRole in (");
          selectQuery.append(getSQLClauseIn(userRoles));
          selectQuery.append(")");
        }
        if (ArrayUtil.isNotEmpty(userGroupIds)) {
          selectQuery.append(" or (intUser.groupId is not null");
          selectQuery.append(" and intUser.groupId in (");
          selectQuery.append(getSQLClauseIn(userGroupIds));
          selectQuery.append("))");
        }
        selectQuery.append(") and intUser.role = ? ");
        selectQuery.append("union ");
        selectQuery.
            append("select instanceId from SB_Workflow_WorkingUser wkUser where I.instanceId = " +
                "wkUser.instanceId and (");
        selectQuery.append("wkUser.userId = ? ");
        if (ArrayUtil.isNotEmpty(userRoles)) {
          selectQuery.append(" or wkUser.usersRole in (");
          selectQuery.append(getSQLClauseIn(userRoles));
          selectQuery.append(")");
        }
        if (ArrayUtil.isNotEmpty(userGroupIds)) {
          selectQuery.append(" or (wkUser.groupId is not null");
          selectQuery.append(" and wkUser.groupId in (");
          selectQuery.append(getSQLClauseIn(userGroupIds));
          selectQuery.append("))");
        }
        selectQuery.append(") and ");

        // role can be multiple (e.g: "role1,role2,...,roleN")
        selectQuery.append("( wkUser.role = ? ");
        selectQuery.append(" or wkUser.role like ? ");
        selectQuery.append(" or wkUser.role like ? ");
        selectQuery.append(" or wkUser.role like ? ");
        selectQuery.append(")");

        selectQuery.append(")");
        selectQuery.append("order by I.instanceId desc");

        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, peasId);
        prepStmt.setString(2, user.getUserId());
        prepStmt.setString(3, role);
        prepStmt.setString(4, user.getUserId());
        prepStmt.setString(5, role);
        prepStmt.setString(6, "%," + role);
        prepStmt.setString(7, role + ",%");
        prepStmt.setString(8, "%," + role + ",%");
      }
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        ProcessInstanceImpl instance = new ProcessInstanceImpl();
        instance.setInstanceId(rs.getString(1));
        instance.setModelId(rs.getString(2));
        instance.setLockedByAdmin(rs.getBoolean(3));
        instance.setErrorStatus(rs.getBoolean(4));
        instance.setTimeoutStatus(rs.getBoolean(5));
        instances.add(instance);
      }

      // getHistory
      prepStmt = con.prepareStatement(
          "SELECT * FROM SB_Workflow_HistoryStep WHERE instanceId = ? ORDER BY id ASC");

      for (ProcessInstanceImpl instance : instances) {
        prepStmt.setInt(1, Integer.parseInt(instance.getInstanceId()));
        rs = prepStmt.executeQuery();
        while (rs.next()) {
          HistoryStepImpl historyStep = new HistoryStepImpl(rs.getInt(2));
          historyStep.setUserId(rs.getString(3));
          historyStep.setUserRoleName(rs.getString(4));
          historyStep.setAction(rs.getString(5));
          historyStep.setActionDate(rs.getDate(6));
          historyStep.setResolvedState(rs.getString(7));
          historyStep.setResultingState(rs.getString(8));
          historyStep.setActionStatus(rs.getInt(9));
          historyStep.setProcessInstance(instance);

          instance.addHistoryStep(historyStep);
        }
      }

      prepStmt = con.prepareStatement(
          "SELECT * FROM SB_Workflow_ActiveState WHERE instanceId = ? ORDER BY id ASC");

      for (ProcessInstanceImpl instance : instances) {
        prepStmt.setInt(1, Integer.parseInt(instance.getInstanceId()));
        rs = prepStmt.executeQuery();
        List<ActiveState> states = new ArrayList<>();
        while (rs.next()) {
          ActiveState state = new ActiveState(rs.getInt(1));
          state.setState(rs.getString(3));
          state.setBackStatus(rs.getBoolean(4));
          state.setTimeoutStatus(rs.getInt(5));
          state.setProcessInstance(instance);
          states.add(state);
        }
        instance.setActiveStates(states);
      }

      return instances.toArray(new ProcessInstance[instances.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstances",
          "EX_ERR_GET_INSTANCES", "sql query : " + selectQuery, se);
    } finally {
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
    }
  }

  private String getSQLClauseIn(String[] items) {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (String item : items) {
      if (!first) {
        result.append(", ");
      }
      result.append("'").append(item).append("'");
      first = false;
    }
    return result.toString();
  }

  /**
   * Get the process instances for a given instance id
   * @param instanceId id of searched instance
   * @return the searched process instance
   * @throws WorkflowException
   */
  @Override
  public ProcessInstance getProcessInstance(String instanceId) throws WorkflowException {
    return repository.getById(instanceId);
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

    // delete associated todo
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
  @Override
  public ProcessInstance[] getTimeOutProcessInstances() throws WorkflowException {
    try {
      JdbcSqlQuery query = JdbcSqlQuery.createSelect("instanceid from SB_Workflow_ActiveState")
          .where("timeoutDate < ? ", new Timestamp((new Date()).getTime()));
      List<String> ids = query.execute(row -> String.valueOf(row.getInt(1)));
      Set<String> instanceIds = new HashSet<>();
      for (String id : ids) {
        instanceIds.add(id);
      }

      SilverpeasList<ProcessInstanceImpl> instances = repository.getById(instanceIds);

      return instances.toArray(new ProcessInstance[instances.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_GET_TIMEOUT_INSTANCES", se);
    }
  }
}