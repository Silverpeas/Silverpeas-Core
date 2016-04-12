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

package org.silverpeas.core.workflow.engine.task;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

/**
 * A creation task is the first task of a creation instance.
 */
public class CreationTaskImpl extends AbstractTaskImpl {
  String[] actionNames = null;
  ProcessInstance processInstance = null;

  /**
   * Builds a CreationTaskImpl.
   */
  public CreationTaskImpl(User user, String roleName, ProcessModel processModel)
      throws WorkflowException {
    super(user, roleName, processModel);
    Action creation = processModel.getCreateAction(roleName);
    actionNames = new String[] { creation.getName() };
  }

  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Returns the state to be resolved by the user.
   */
  public State getState() {
    return null;
  }

  /**
   * Returns the action names list from which the user must choose to resolve the activity.
   */
  public String[] getActionNames() {
    return actionNames;
  }

  /**
   * no back action possible, return null.
   */
  public HistoryStep[] getBackSteps() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getPendingQuestions() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getSentQuestions() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getRelevantQuestions() {
    return null;
  }
}
