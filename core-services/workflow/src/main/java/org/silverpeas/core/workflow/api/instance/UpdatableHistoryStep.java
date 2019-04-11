/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.workflow.api.instance;

import java.util.Date;

public interface UpdatableHistoryStep extends HistoryStep {

  /**
   * Set the process instance
   * @param processInstance process instance
   */
  void setProcessInstance(ProcessInstance processInstance);

  /**
   * Set the actor id of the action logged in this History step
   * @param userId the actor id
   */
  void setUserId(String userId);

  /**
   * Set the action name logged in this History step
   * @param action the action name
   */
  void setAction(String action);

  /**
   * Set the date when the action has been done
   * @param actionDate the action date
   */
  void setActionDate(Date actionDate);

  /**
   * Set the name of state that has been resolved
   * @param state the resolved state name
   */
  void setResolvedState(String state);

  /**
   * Set the name of state that must result from logged action
   * @param state state name
   */
  void setResultingState(String state);

  /**
   * Set the resulting status of action logged in this history step
   * @param actionStatus action status
   */
  void setActionStatus(ActionStatus actionStatus);

  /**
   * Set the role under which the user did the action
   * @param userRoleName the role's name
   */
  void setUserRoleName(String userRoleName);

  void setSubstituteId(String substituteId);
}