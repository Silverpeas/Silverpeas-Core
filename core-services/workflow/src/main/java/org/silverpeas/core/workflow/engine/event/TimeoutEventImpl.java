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

package org.silverpeas.core.workflow.engine.event;

import java.util.Date;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

/**
 * A TimeoutEvent object is the description of an instance, that is in the same active state since
 * too long Those descriptions are sent to the timeout manager
 */
public class TimeoutEventImpl implements TimeoutEvent {
  /**
   * A TimeoutEventImpl object is built from a processInstance, a state and an action
   */
  public TimeoutEventImpl(ProcessInstance processInstance, State resolvedState,
      Action action) {
    this.processInstance = processInstance;
    this.resolvedState = resolvedState;
    this.action = action;
    this.actionDate = new Date();
  }

  /**
   * Returns the actor.
   */
  public User getUser() {
    return null;
  }

  /**
   * Returns the process instance. Returns null when the task is an instance creation.
   */
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Returns the state/activity resolved by the user.
   */
  public State getResolvedState() {
    return resolvedState;
  }

  /**
   * Returns the name of the action chosen to resolve the activity.
   */
  public String getActionName() {
    return action.getName();
  }

  /**
   * Returns the action date.
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Returns the data filled when the action was processed.
   */
  public DataRecord getDataRecord() {
    return null;
  }

  /**
   * Returns the role name of the actor
   */
  public String getUserRoleName() {
    return "supervisor";
  }

  /*
   * Internal states.
   */
  private ProcessInstance processInstance = null;
  private Action action = null;
  private State resolvedState = null;
  private Date actionDate = null;
}
