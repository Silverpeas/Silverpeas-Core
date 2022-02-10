/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.workflow.engine.event;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.task.Task;

/**
 * A TaskSavedEvent object is the description of an activity that is not finished but saved to be
 * continued later. Those descriptions are sent to the workflow engine by the workflow tools when
 * the user has save a task in a process instance.
 */
public class TaskSavedEventImpl extends AbstractTaskEvent implements TaskSavedEvent {
  /**
   * A TaskSavedEvent is built from a resolved task, a choosen action and a filled form.
   */
  public TaskSavedEventImpl(Task resolvedTask, String actionName, DataRecord data) {
    super(resolvedTask, actionName, data);
    this.firstTimeSaved = false;
  }

  @Override
  public boolean isFirstTimeSaved() {
    return firstTimeSaved;
  }

  @Override
  public void setFirstTimeSaved(boolean firstTimeSaved) {
    this.firstTimeSaved = firstTimeSaved;
  }

  /**
   * Internal states.
   */
  private boolean firstTimeSaved;

}