/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.event;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Date;

/**
 * A GenericEvent object is the description of an action on an activity Those descriptions are sent
 * to the workflow engine by the workflow tools when the user has done an action in a process
 * instance.
 */
public interface GenericEvent {
  /**
   * Returns the actor.
   */
  User getUser();

  /**
   * Returns the role name of the actor
   */
  String getUserRoleName();

  /**
   * Returns the process instance.
   */
  ProcessInstance getProcessInstance();

  /**
   * Returns the state/activity resolved by the user.
   */
  State getResolvedState();

  /**
   * Returns the action date.
   */
  Date getActionDate();

  /**
   * Returns the name of the action choosen to resolve the activity.
   */
  String getActionName();

  /**
   * Returns the data associated to this event.
   */
  DataRecord getDataRecord();

  User getSubstitute();

  void setSubstitute(User substitute);

  /**
   * Returns substitute if defined, regular user otherwise
   * @return a User
   */
  User getUserOrSubstitute();
}