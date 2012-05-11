/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.api.event;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * A TaskDoneEvent object is the description of a done activity. Those descriptions are sent to the
 * workflow engine by the workflow tools when the user has done a task in a process instance.
 */
public interface TaskDoneEvent extends GenericEvent {
  /**
   * Returns the process model (peas).
   */
  public ProcessModel getProcessModel();

  /**
   * Set the process instance.
   */
  public void setProcessInstance(ProcessInstance instance);

  /**
   * Set a flag to indicate if action comes from a resumed action.
   */
  public void setResumingAction(boolean isResumingInstance);

  /**
   * Get the flag to indicate if action comes from a resumed action.
   */
  public boolean isResumingAction();
}