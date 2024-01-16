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
package org.silverpeas.core.workflow.engine.error;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "sb_workflow_error")
@NamedQueries({@NamedQuery(name = "processInstance.findErrors",
    query = "SELECT error FROM WorkflowErrorImpl error WHERE error.instanceId = :id"),
    @NamedQuery(name = "processInstance.deleteErrors",
        query = "DELETE WorkflowErrorImpl error WHERE error.instanceId = :id")})
public class WorkflowErrorImpl extends BasicJpaEntity<WorkflowErrorImpl, UniqueIntegerIdentifier> implements WorkflowError {

  @Column
  private int instanceId = -1;
  @Column
  private int stepId;
  @Column
  private String errorMessage = null;
  @Column
  private String stackTrace = null;
  @Column
  private String userId = null;
  @Column
  private String actionName = null;
  @Column
  private Date actionDate = null;
  @Column
  private String userRole = null;
  @Column
  private String stateName = null;

  @Transient
  private State state = null;

  @Transient
  private ProcessInstance processInstance = null;

  @Transient
  private User user = null;

  @Transient
  private Action action = null;

  protected WorkflowErrorImpl() {
  }

  /**
   * A WorkflowErrorImpl is build from a process instance, a generic event, a history step and a
   * Exception
   */
  public WorkflowErrorImpl(ProcessInstance instance, GenericEvent event, Exception exception) {
    this.processInstance = instance;
    this.instanceId = Integer.parseInt(instance.getInstanceId());
    this.errorMessage = exception.getMessage();
    this.actionName = event.getActionName();
    this.actionDate = event.getActionDate();
    this.userRole = event.getUserRoleName();

    this.user = event.getUser();
    if (user != null) {
      this.userId = event.getUser().getUserId();
    }
    this.state = event.getResolvedState();
    if (state != null) {
      this.stateName = event.getResolvedState().getName();
    }
    // Convert stack trace to String
    ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(baoStream);
    exception.printStackTrace(printStream);
    this.stackTrace = baoStream.toString().trim();
  }

  /**
   * @return ProcessInstance
   */
  @Override
  public ProcessInstance getProcessInstance() throws WorkflowException {
    if (processInstance == null && instanceId != -1) {
      processInstance =
          Workflow.getProcessInstanceManager().getProcessInstance(String.valueOf(instanceId));
    }

    return processInstance;
  }

  /**
   * @return error message
   */
  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @return stack trace
   */
  @Override
  public String getStackTrace() {
    return stackTrace;
  }

  /**
   * @return user
   */
  @Override
  public User getUser() throws WorkflowException {
    if (user == null && userId != null) {
      user = Workflow.getUserManager().getUser(userId);
    }

    return user;
  }

  /**
   * @return action
   */
  @Override
  public Action getAction() throws WorkflowException {
    if (action == null && actionName != null && getProcessInstance() != null) {
      action = getProcessInstance().getProcessModel().getAction(actionName);
    }

    return action;
  }

  /**
   * Get action date
   */
  @Override
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * @return resolved state
   */
  @Override
  public State getResolvedState() throws WorkflowException {
    if (state == null && stateName != null && getProcessInstance() != null) {
      state = getProcessInstance().getProcessModel().getState(stateName);
    }

    return state;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}