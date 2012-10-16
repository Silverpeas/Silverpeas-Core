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

package com.silverpeas.workflow.external.impl;

import com.silverpeas.workflow.api.event.GenericEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.external.ExternalAction;

public abstract class ExternalActionImpl implements ExternalAction {

  private ProcessInstance process;
  private GenericEvent event;
  private Trigger trigger;

  public void setProcessInstance(ProcessInstance process) {
    this.process = process;
  }

  public void setEvent(GenericEvent event) {
    this.event = event;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public Parameter getTriggerParameter(String paramName) {
    return trigger.getParameter(paramName);
  }

  public ProcessInstance getProcessInstance() {
    return process;
  }

  public GenericEvent getEvent() {
    return event;
  }

  public abstract void execute();

}
