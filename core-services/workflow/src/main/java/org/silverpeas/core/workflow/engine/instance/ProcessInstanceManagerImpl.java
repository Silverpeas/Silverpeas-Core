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

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.workflow.api.UpdatableProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
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
import java.util.Vector;

/**
 * A ProcessInstanceManager implementation
 */
@Singleton
public class ProcessInstanceManagerImpl implements UpdatableProcessInstanceManager {

  private static String COLUMNS =
      " I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus ";

  @Inject
  private SilverpeasCalendar calendar;

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
      // Need first to make a SQL query to find all concerned instances ids
      // Due to the operator EXISTS that is not yet supported by Castor OQL->SQL translator
      con = DBUtil.openConnection();

      if (role.equals("supervisor")) {

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
        if ((userRoles != null) && (userRoles.length > 0)) {
          selectQuery.append(" or intUser.usersRole in (");
          selectQuery.append(getSQLClauseIn(userRoles));
          selectQuery.append(")");
        }
        if (userGroupIds != null && userGroupIds.length > 0) {
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
        if ((userRoles != null) && (userRoles.length > 0)) {
          selectQuery.append(" or wkUser.usersRole in (");
          selectQuery.append(getSQLClauseIn(userRoles));
          selectQuery.append(")");
        }
        if (userGroupIds != null && userGroupIds.length > 0) {
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
          HistoryStepImpl historyStep = new HistoryStepImpl();
          historyStep.setId(String.valueOf(rs.getInt(2)));
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
        Vector<ActiveState> states = new Vector<>();
        while (rs.next()) {
          ActiveState state = new ActiveState();
          state.setId(String.valueOf(rs.getInt(1)));
          state.setState(rs.getString(3));
          state.setBackStatus(rs.getBoolean(4));
          state.setTimeoutStatus(rs.getInt(5));
          state.setProcessInstance(instance);
          states.add(state);
        }
        instance.castor_setActiveStates(states);
      }


      return instances.toArray(new ProcessInstance[instances.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstances",
          "EX_ERR_CASTOR_GET_INSTANCES", "sql query : " + selectQuery, se);
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
   * Get the list of process instances for a given peas Id, that have the given state activated and
   * @param peasId id of processManager instance
   * @param state activated state
   * @return an array of ProcessInstance objects
   */
  public ProcessInstance[] getProcessInstancesInState(String peasId, State state)
      throws WorkflowException {
    Database db = null;
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String selectQuery = "";
    OQLQuery query = null;
    QueryResults results;
    List<ProcessInstance> instances = new ArrayList<>();

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(false);
      db.begin();

      // Need first to make a SQL query to find all concerned instances ids
      // Due to the operator EXISTS that is not yet supported by Castor OQL->SQL
      // translator
      con = DBUtil.openConnection();

      selectQuery = "SELECT DISTINCT instance.instanceId ";
      selectQuery +=
          "FROM SB_Workflow_ActiveState activeState, SB_Workflow_ProcessInstance instance ";
      selectQuery += "WHERE activeState.state = ? ";
      selectQuery += "AND activeState.timeoutStatus = 0 ";
      selectQuery += "AND instance.instanceId = activeState.instanceId ";
      selectQuery += "AND activeState.backStatus = 0 ";
      selectQuery += "AND instance.modelId = ?";

      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, state.getName());
      prepStmt.setString(2, peasId);
      rs = prepStmt.executeQuery();

      StringBuilder queryBuf = new StringBuilder();
      queryBuf.
          append("SELECT distinct instance FROM org.silverpeas.core.workflow.engine.instance" +
              ".ProcessInstanceImpl instance");
      queryBuf.append(" WHERE instanceId IN LIST(");

      while (rs.next()) {
        String instanceId = rs.getString(1);
        queryBuf.append("\"");
        queryBuf.append(instanceId);
        queryBuf.append("\"");
        queryBuf.append(" ,");
      }
      queryBuf.append(" \"-1\")");

      query = db.getOQLQuery(queryBuf.toString());

      // Execute the query
      try {
        results = query.execute(org.exolab.castor.jdo.Database.READONLY);

        // get the instance if any
        while (results.hasMore()) {
          ProcessInstance instance = (ProcessInstance) results.next();
          instances.add(instance);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }

      db.commit();

      return instances.toArray(new ProcessInstance[instances.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstancesInState",
          "EX_ERR_CASTOR_GET_INSTANCES_IN_STATE", "sql query : " + selectQuery, se);
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstancesInState",
          "EX_ERR_CASTOR_GET_INSTANCES_IN_STATE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
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
    ProcessInstanceImpl instance;

    Database db = null;
    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      instance = (ProcessInstanceImpl) db.load(ProcessInstanceImpl.class, instanceId);
      db.commit();

      return instance;
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getProcessInstance",
          "EX_ERR_CASTOR_GET_INSTANCE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Creates a new process instance
   * @param modelId model id
   * @return the new ProcessInstance object
   * @throws WorkflowException
   */
  @Override
  public synchronized ProcessInstance createProcessInstance(String modelId) throws WorkflowException {

    ProcessInstanceImpl instance = new ProcessInstanceImpl();
    instance.setModelId(modelId);
    instance.create();
    return instance;
  }

  /**
   * Removes a new process instance
   * @param instanceId instance id
   * @throws WorkflowException
   */
  @Override
  public void removeProcessInstance(String instanceId) throws WorkflowException {

    ProcessInstanceImpl instance;
    Database db = null;
    try {
      // Delete forms data associated with this instance
      removeProcessInstanceData(instanceId);

      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      instance = (ProcessInstanceImpl) db.load(ProcessInstanceImpl.class, instanceId);
      db.remove(instance);
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.removeProcessInstance",
          "EX_ERR_CASTOR_REMOVE_INSTANCE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

    WorkflowHub.getErrorManager().removeErrorsOfInstance(instanceId);

  }

  /**
   * Delete forms data associated with this instance
   * @param instanceId instance id
   */
  private void removeProcessInstanceData(String instanceId) throws WorkflowException {
    ProcessInstance instance = getProcessInstance(instanceId);

    removeProcessInstanceData(instance);
  }

  public void removeProcessInstanceData(ProcessInstance instance) throws WorkflowException {


    ForeignPK foreignPK = new ForeignPK(instance.getInstanceId(), instance.getModelId());

    // delete attachments

    List<SimpleDocument> attachments =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKey(foreignPK, null);
    for (SimpleDocument attachment : attachments) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(attachment);
    }

    // delete folder

    try {
      RecordSet folderRecordSet = instance.getProcessModel().getFolderRecordSet();
      folderRecordSet.delete(instance.getFolder());
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceManagerImpl.removeProcessInstanceData",
          "EX_ERR_CANT_REMOVE_FOLDER", e);
    }

    // delete history steps

    HistoryStep[] steps = instance.getHistorySteps();
    for (int i = 0; steps != null && i < steps.length; i++) {
      if (!steps[i].getAction().equals("#question#") &&
          !steps[i].getAction().equals("#response#")) {
        steps[i].deleteActionRecord();
      }
    }

    // delete associated todos
    calendar
        .removeToDoFromExternal("useless", foreignPK.getInstanceId(), foreignPK.getId() + "##%");
  }

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  public void lock(ProcessInstance instance, State state, User user) throws WorkflowException {
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      UpdatableProcessInstance copyInstance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // lock instance for user
        copyInstance.lock(state, user);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("ProcessInstanceManagerImpl.lock", "workflowEngine.EX_ERR_LOCK",
            we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.lock",
          "workflowEngine.EX_ERR_CASTOR_LOCK", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * unlocks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  public void unlock(ProcessInstance instance, State state, User user) throws WorkflowException {
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      UpdatableProcessInstance copyInstance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // unlock instance for user
        copyInstance.unLock(state, user);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("ProcessInstanceManagerImpl.unlock",
            "workflowEngine.EX_ERR_UNLOCK", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.unlock",
          "workflowEngine.EX_ERR_CASTOR_UNLOCK", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
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
    Database db = null;
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String selectQuery = "";
    QueryResults results;
    Set<ProcessInstance> instances = new HashSet<>();

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(false);
      db.begin();

      // Need first to make a SQL query to find all concerned instances ids
      con = DBUtil.openConnection();

      selectQuery =
          "SELECT DISTINCT activeState.instanceId FROM SB_Workflow_ActiveState activeState WHERE " +
              "activeState.timeoutDate < ? ";

      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
      rs = prepStmt.executeQuery();

      StringBuilder queryBuf = new StringBuilder();
      queryBuf.append("SELECT distinct instance FROM org.silverpeas.core.workflow.engine.instance" +
          ".ProcessInstanceImpl instance");
      queryBuf.append(" WHERE instanceId IN LIST(");

      while (rs.next()) {
        String instanceId = rs.getString(1);
        queryBuf.append("\"");
        queryBuf.append(instanceId);
        queryBuf.append("\"");
        queryBuf.append(" ,");
      }
      queryBuf.append(" \"-1\")");

      OQLQuery query = db.getOQLQuery(queryBuf.toString());

      // Execute the query
      try {
        results = query.execute(org.exolab.castor.jdo.Database.READONLY);

        // get the instance if any
        while (results.hasMore()) {
          ProcessInstance instance = (ProcessInstance) results.next();
          instances.add(instance);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      db.commit();
      return instances.toArray(new ProcessInstance[instances.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_CASTOR_GET_TIMEOUT_INSTANCES", "sql query : " + selectQuery, se);
    } catch (PersistenceException pe) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_CASTOR_GET_TIMEOUT_INSTANCES", pe);

    } finally {
      WorkflowJDOManager.closeDatabase(db);
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
    }
  }
}