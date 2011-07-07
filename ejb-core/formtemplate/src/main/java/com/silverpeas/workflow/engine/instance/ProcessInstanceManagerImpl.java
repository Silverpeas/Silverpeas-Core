/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

// TODO : reporter dans CVS (done)
package com.silverpeas.workflow.engine.instance;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;

import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.WorkflowHub;
import com.silverpeas.workflow.engine.jdo.WorkflowJDOManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;

/**
 * A ProcessInstanceManager implementation
 */
public class ProcessInstanceManagerImpl implements UpdatableProcessInstanceManager {
  private String dbName = JNDINames.WORKFLOW_DATASOURCE;
  private static String COLUMNS =
      " I.instanceId, I.modelId, I.locked, I.errorStatus, I.timeoutStatus ";

  /**
   * @return the DB connection
   */
  private Connection getConnection() throws WorkflowException {
    try {
      Connection con = DBUtil.makeConnection(dbName);
      return con;
    } catch (Exception e) {
      throw new WorkflowException("ProcessInstanceManagerImpl.getConnection()",
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Get the list of process instances for a given peas Id, user and role.
   * @param peasId id of processManager instance
   * @param user user for who the process instance list is
   * @param role role name of the user for who the process instance list is (useful when user has
   * different roles)
   * @return an array of ProcessInstance objects
   */
  /*
   * public ProcessInstance[] getProcessInstances(String peasId, User user, String role) throws
   * WorkflowException { Database db = null; Connection con = null; PreparedStatement prepStmt =
   * null; ResultSet rs = null; String selectQuery = ""; OQLQuery query = null; QueryResults
   * results; Vector instances = new Vector(); try { // Constructs the query db =
   * WorkflowJDOManager.getDatabase(true); // db = JDOManager.getDatabase(false); db.begin(); //
   * Need first to make a SQL query to find all concerned instances ids // Due to the operator
   * EXISTS that is not yet supported by Castor OQL->SQL translator con = this.getConnection(); if
   * (role.equals("supervisor")) { selectQuery =
   * "select DISTINCT instanceId from SB_Workflow_ProcessInstance instance where modelId = ?" ;
   * prepStmt = con.prepareStatement(selectQuery); prepStmt.setString(1, peasId); } else {
   * selectQuery =
   * "select DISTINCT instanceId from SB_Workflow_ProcessInstance instance where modelId = ? and exists "
   * ; selectQuery +=
   * "( select instanceId from SB_Workflow_InterestedUser intUser where userId = ? and role = ? and instance.instanceId = intUser.instanceId "
   * ; selectQuery += "UNION "; selectQuery +=
   * "select instanceId from SB_Workflow_WorkingUser wkUser where userId = ? and role = ?  and instance.instanceId = wkUser.instanceId)"
   * ; prepStmt = con.prepareStatement(selectQuery); prepStmt.setString(1, peasId);
   * prepStmt.setString(2, user.getUserId()); prepStmt.setString(3, role); prepStmt.setString(4,
   * user.getUserId()); prepStmt.setString(5, role); } rs = prepStmt.executeQuery(); StringBuffer
   * queryBuf = new StringBuffer(); queryBuf.append(
   * "SELECT distinct instance FROM com.silverpeas.workflow.engine.instance.ProcessInstanceImpl instance"
   * ); queryBuf.append(" WHERE instanceId IN LIST("); while (rs.next()) { String instanceId =
   * rs.getString(1); queryBuf.append("\""); queryBuf.append(instanceId); queryBuf.append("\"");
   * queryBuf.append(" ,"); } queryBuf.append(" \"-1\")"); query =
   * db.getOQLQuery(queryBuf.toString()); // Execute the query try { //results =
   * query.execute(org.exolab.castor.jdo.Database.ReadOnly); results =
   * query.execute(Database.ReadOnly); // get the instance if any while (results.hasMore()) {
   * ProcessInstance instance = (ProcessInstance) results.next(); if (!instances.contains(instance))
   * instances.add(instance); } } catch (Exception ex) { SilverTrace.warn( "workflowEngine",
   * "ProcessInstanceManagerImpl", "workflowEngine.EX_PROBLEM_GETTING_INSTANCES", ex); }
   * db.commit(); SilverTrace.info( "workflowEngine", "ProcessInstanceManagerImpl",
   * "root.MSG_GEN_PARAM_VALUE", " query : " + queryBuf.toString()); SilverTrace.info(
   * "workflowEngine", "ProcessInstanceManagerImpl", "root.MSG_GEN_PARAM_VALUE", " nb instances : "
   * + instances.size()); return (ProcessInstance[]) instances.toArray(new ProcessInstance[0]); }
   * catch (SQLException se) { throw new WorkflowException(
   * "ProcessInstanceManagerImpl.getProcessInstances", "EX_ERR_CASTOR_GET_INSTANCES", "sql query : "
   * + selectQuery, se); } catch (PersistenceException pe) { throw new WorkflowException(
   * "ProcessInstanceManagerImpl.getProcessInstances", "EX_ERR_CASTOR_GET_INSTANCES", pe); } finally
   * { WorkflowJDOManager.closeDatabase(db); try { DBUtil.close(rs, prepStmt); if (con != null)
   * con.close(); } catch (SQLException se) { SilverTrace.error( "workflowEngine",
   * "ProcessInstanceManagerImpl.getProcessInstances", "root.EX_RESOURCE_CLOSE_FAILED", se); } } }
   */

  public ProcessInstance[] getProcessInstances(String peasId, User user,
      String role) throws WorkflowException {
    return getProcessInstances(peasId, user, role, null, null);
  }

  public ProcessInstance[] getProcessInstances(String peasId, User user,
      String role, String[] userRoles, String[] userGroupIds) throws WorkflowException {
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.getProcessInstances()",
        "root.MSG_GEN_ENTER_METHOD", "peasId = "+peasId+", user = "+user.getUserId()+", role = "+role);
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    StringBuffer selectQuery = new StringBuffer();
    Vector<ProcessInstanceImpl> instances = new Vector<ProcessInstanceImpl>();

    try {
      // Need first to make a SQL query to find all concerned instances ids
      // Due to the operator EXISTS that is not yet supported by Castor OQL->SQL
      // translator
      con = this.getConnection();

      if (role.equals("supervisor")) {
        selectQuery.append("select * from SB_Workflow_ProcessInstance instance where modelId = ?");
        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, peasId);
      } else {
        selectQuery.append("select ").append(COLUMNS)
            .append(" from SB_Workflow_ProcessInstance I ");
        selectQuery.append("where I.modelId = ? ");
        selectQuery.append("and exists (");
        selectQuery
            .append(
            "select instanceId from SB_Workflow_InterestedUser intUser where I.instanceId = intUser.instanceId and (");
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
        selectQuery
            .append(
            "select instanceId from SB_Workflow_WorkingUser wkUser where I.instanceId = wkUser.instanceId and (");
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
        
        SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.getProcessInstances()",
            "root.MSG_GEN_PARAM_VALUE", "SQL query = "+selectQuery.toString());

        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, peasId);
        prepStmt.setString(2, user.getUserId());
        prepStmt.setString(3, role);
        prepStmt.setString(4, user.getUserId());
        prepStmt.setString(5, role);
        prepStmt.setString(6, "%,"+role);
        prepStmt.setString(7, role+",%");
        prepStmt.setString(8, "%,"+role+",%");
      }
      rs = prepStmt.executeQuery();

      ProcessInstanceImpl instance = null;
      while (rs.next()) {
        instance = new ProcessInstanceImpl();
        instance.setInstanceId(rs.getString(1));
        instance.setModelId(rs.getString(2));
        instance.setLockedByAdmin(rs.getBoolean(3));
        instance.setErrorStatus(rs.getBoolean(4));
        instance.setTimeoutStatus(rs.getBoolean(5));

        instances.add(instance);
      }

      // getHistory
      prepStmt =
          con
              .prepareStatement("select * from SB_Workflow_HistoryStep where instanceId = ? order by id asc");

      for (int i = 0; i < instances.size(); i++) {
        instance = instances.get(i);

        prepStmt.setInt(1, Integer.parseInt(instance.getInstanceId()));

        rs = prepStmt.executeQuery();
        HistoryStepImpl historyStep = null;
        while (rs.next()) {
          historyStep = new HistoryStepImpl();
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

      // getActiveStates
      Vector<ActiveState> states = null;
      prepStmt =
          con
              .prepareStatement("select * from SB_Workflow_ActiveState where instanceId = ? order by id asc");

      for (int i = 0; i < instances.size(); i++) {
        instance = (ProcessInstanceImpl) instances.get(i);

        prepStmt.setInt(1, Integer.parseInt(instance.getInstanceId()));

        rs = prepStmt.executeQuery();
        ActiveState state = null;
        states = new Vector<ActiveState>();
        while (rs.next()) {
          state = new ActiveState();
          state.setId(String.valueOf(rs.getInt(1)));
          state.setState(rs.getString(3));
          state.setBackStatus(rs.getBoolean(4));
          state.setTimeoutStatus(rs.getInt(5));
          state.setProcessInstance(instance);

          states.add(state);
        }
        instance.castor_setActiveStates(states);
      }

      SilverTrace.info("workflowEngine", "ProcessInstanceManagerImpl",
          "root.MSG_GEN_PARAM_VALUE", " nb instances : " + instances.size());
      return (ProcessInstance[]) instances.toArray(new ProcessInstance[0]);
    } catch (SQLException se) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getProcessInstances",
          "EX_ERR_CASTOR_GET_INSTANCES", "sql query : " + selectQuery, se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null)
          con.close();
      } catch (SQLException se) {
        SilverTrace.error("workflowEngine",
            "ProcessInstanceManagerImpl.getProcessInstances",
            "root.EX_RESOURCE_CLOSE_FAILED", se);
      }
    }
  }
  
  private String getSQLClauseIn(String[] items)
  {
    StringBuffer result = new StringBuffer();
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
    Vector<ProcessInstance> instances = new Vector<ProcessInstance>();

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(false);
      db.begin();

      // Need first to make a SQL query to find all concerned instances ids
      // Due to the operator EXISTS that is not yet supported by Castor OQL->SQL
      // translator
      con = this.getConnection();

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

      StringBuffer queryBuf = new StringBuffer();
      queryBuf
          .append("SELECT distinct instance FROM com.silverpeas.workflow.engine.instance.ProcessInstanceImpl instance");
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
        results = query.execute(org.exolab.castor.jdo.Database.ReadOnly);

        // get the instance if any
        while (results.hasMore()) {
          ProcessInstance instance = (ProcessInstance) results.next();
          instances.add(instance);
        }
      } catch (Exception ex) {
        SilverTrace.warn("workflowEngine", "ProcessInstanceManagerImpl",
            "workflowEngine.EX_PROBLEM_GETTING_INSTANCES", ex);
      }

      db.commit();

      return (ProcessInstance[]) instances.toArray(new ProcessInstance[0]);
    } catch (SQLException se) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getProcessInstancesInState",
          "EX_ERR_CASTOR_GET_INSTANCES_IN_STATE", "sql query : " + selectQuery,
          se);
    } catch (PersistenceException pe) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getProcessInstancesInState",
          "EX_ERR_CASTOR_GET_INSTANCES_IN_STATE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);

      try {
        DBUtil.close(rs, prepStmt);
        if (con != null)
          con.close();
      } catch (SQLException se) {
        SilverTrace.error("workflowEngine",
            "ProcessInstanceManagerImpl.getProcessInstancesInState",
            "root.EX_RESOURCE_CLOSE_FAILED", se);
      }
    }
  }

  /**
   * Get the process instances for a given instance id
   * @param instanceId id of searched instance
   * @return the searched process instance
   */
  public ProcessInstance getProcessInstance(String instanceId)
      throws WorkflowException {
    ProcessInstanceImpl instance;

    Database db = null;
    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      instance = (ProcessInstanceImpl) db.load(ProcessInstanceImpl.class,
          instanceId);
      db.commit();

      return instance;
    } catch (PersistenceException pe) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getProcessInstance",
          "EX_ERR_CASTOR_GET_INSTANCE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Creates a new process instance
   * @param modelId model id
   * @return the new ProcessInstance object
   */
  public ProcessInstance createProcessInstance(String modelId)
      throws WorkflowException {
    ProcessInstanceImpl instance = new ProcessInstanceImpl();
    instance.setModelId(modelId);
    instance.create();

    return (ProcessInstance) instance;
  }

  /**
   * Removes a new process instance
   * @param instanceId instance id
   */
  public void removeProcessInstance(String instanceId) throws WorkflowException {
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstance()",
    "root.MSG_GEN_ENTER_METHOD","InstanceId="+instanceId);
    ProcessInstanceImpl instance;
    Database db = null;
    try {
      // Delete forms data associated with this instance
      removeProcessInstanceData(instanceId);

      // Constructs the query
      db = WorkflowJDOManager.getDatabase();
      db.begin();
      instance = (ProcessInstanceImpl) db.load(ProcessInstanceImpl.class,
          instanceId);
      db.remove(instance);
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.removeProcessInstance",
          "EX_ERR_CASTOR_REMOVE_INSTANCE", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

    WorkflowHub.getErrorManager().removeErrorsOfInstance(instanceId);
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstance()",
    "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete forms data associated with this instance
   * @param instanceId instance id
   */
  private void removeProcessInstanceData(String instanceId)
      throws WorkflowException {
    ProcessInstance instance = getProcessInstance(instanceId);

    removeProcessInstanceData(instance);
  }

  public void removeProcessInstanceData(ProcessInstance instance)
      throws WorkflowException {
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_ENTER_METHOD");

    ForeignPK foreignPK = new ForeignPK(instance.getInstanceId(), instance
        .getModelId());

    // delete attachments
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_PARAM_VALUE", "Delete attachments foreignPK = "+foreignPK);
    AttachmentController.deleteAttachmentByCustomerPK(foreignPK);

    // delete versioning
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_PARAM_VALUE", "Delete versiong foreignPK = "+foreignPK);
    try {
      getVersioningBm().deleteDocumentsByForeignPK(foreignPK);
    } catch (Exception e) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.removeProcessInstanceData",
          "EX_ERR_CANT_REMOVE_VERSIONNING_FILES", e);
    }

    // delete folder
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_PARAM_VALUE", "Delete folder");
    try {
      RecordSet folderRecordSet = instance.getProcessModel()
          .getFolderRecordSet();
      folderRecordSet.delete(instance.getFolder());
    } catch (FormException e) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.removeProcessInstanceData",
          "EX_ERR_CANT_REMOVE_FOLDER", e);
    }

    // delete history steps
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_PARAM_VALUE", "Delete history steps");
    HistoryStep[] steps = instance.getHistorySteps();
    for (int i = 0; steps != null && i < steps.length; i++) {
      if (!steps[i].getAction().equals("#question#")
          && !steps[i].getAction().equals("#response#"))
        steps[i].deleteActionRecord();
    }

    // delete associated todos
    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
        "root.MSG_GEN_PARAM_VALUE", "Delete associated todos");
    TodoBackboneAccess tbba = new TodoBackboneAccess();
    tbba.removeEntriesFromExternal("useless", foreignPK.getInstanceId(),
        foreignPK.getId() + "##%");

    SilverTrace.info("worflowEngine", "ProcessInstanceManagerImpl.removeProcessInstanceData()",
    "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  public void lock(ProcessInstance instance, State state, User user)
      throws WorkflowException {
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      UpdatableProcessInstance copyInstance = (UpdatableProcessInstance) db
          .load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // lock instance for user
        copyInstance.lock(state, user);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("ProcessInstanceManagerImpl.lock",
            "workflowEngine.EX_ERR_LOCK", we);
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
  public void unlock(ProcessInstance instance, State state, User user)
      throws WorkflowException {
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      UpdatableProcessInstance copyInstance = (UpdatableProcessInstance) db
          .load(ProcessInstanceImpl.class, instance.getInstanceId());

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
   * Build a new HistoryStep Return an object implementing HistoryStep interface
   */
  public HistoryStep createHistoryStep() {
    return (HistoryStep) new HistoryStepImpl();
  }

  /**
   * Builds an actor from a user and a role.
   */
  public Actor createActor(User user, String roleName, State state) {
    return new ActorImpl(user, roleName, state);
  }

  public VersioningBm getVersioningBm() throws WorkflowException {
    VersioningBm versioningBm = null;
    try {
      VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioningBm = versioningBmHome.create();
    } catch (Exception e) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getVersioningBm()",
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return versioningBm;
  }

  /**
   * Get the list of process instances for which timeout date is over
   * @return an array of ProcessInstance objects
   * @throws WorkflowException 
   */
  public ProcessInstance[] getTimeOutProcessInstances() throws WorkflowException {
    Database db = null;
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String selectQuery = "";
    OQLQuery query = null;
    QueryResults results;
    Set<ProcessInstance> instances = new HashSet<ProcessInstance>();

    try {
      // Constructs the query
      db = WorkflowJDOManager.getDatabase(false);
      db.begin();

      // Need first to make a SQL query to find all concerned instances ids
      con = this.getConnection();

      selectQuery = "SELECT DISTINCT activeState.instanceId ";
      selectQuery +=
          "FROM SB_Workflow_ActiveState activeState ";
      selectQuery += "WHERE activeState.timeoutDate < ? ";

      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
      rs = prepStmt.executeQuery();

      StringBuffer queryBuf = new StringBuffer();
      queryBuf
          .append("SELECT distinct instance FROM com.silverpeas.workflow.engine.instance.ProcessInstanceImpl instance");
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
        results = query.execute(org.exolab.castor.jdo.Database.ReadOnly);

        // get the instance if any
        while (results.hasMore()) {
          ProcessInstance instance = (ProcessInstance) results.next();
          instances.add(instance);
        }
      } catch (Exception ex) {
        SilverTrace.warn("workflowEngine", "ProcessInstanceManagerImpl.getTimeOutProcessInstances",
            "workflowEngine.EX_PROBLEM_GETTING_INSTANCES", ex);
      }

      db.commit();

      return (ProcessInstance[]) instances.toArray(new ProcessInstance[0]);
    } catch (SQLException se) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_CASTOR_GET_TIMEOUT_INSTANCES", "sql query : " + selectQuery,
          se);
    } catch (PersistenceException pe) {
      throw new WorkflowException(
          "ProcessInstanceManagerImpl.getTimeOutProcessInstances",
          "EX_ERR_CASTOR_GET_TIMEOUT_INSTANCES", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);

      try {
        DBUtil.close(rs, prepStmt);
        if (con != null)
          con.close();
      } catch (SQLException se) {
        SilverTrace.error("workflowEngine",
            "ProcessInstanceManagerImpl.getTimeOutProcessInstances",
            "root.EX_RESOURCE_CLOSE_FAILED", se);
      }
    }
  }  
  
}