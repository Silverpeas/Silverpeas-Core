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

package org.silverpeas.core.workflow.api.instance;

import java.util.Date;

public interface UpdatableHistoryStep extends HistoryStep {
  /**
   * Set the process instance
   * @param instance process instance
   */
  public void setProcessInstance(ProcessInstance processInstance);

  /**
   * Set the actor id of the action logged in this History step
   * @param userId the actor id
   */
  public void setUserId(String userId);

  /**
   * Set the action name logged in this History step
   * @param action the action name
   */
  public void setAction(String action);

  /**
   * Set the date when the action has been done
   * @param actionDate the action date
   */
  public void setActionDate(Date actionDate);

  /**
   * Set the name of state that has been resolved
   * @param state the resolved state name
   */
  public void setResolvedState(String state);

  /**
   * Set the name of state that must result from logged action
   * @param state state name
   */
  public void setResultingState(String state);

  /**
   * Set the resulting status of action logged in this history step
   * <ul>
   * <li>-1 : Process failed
   * <li>0 : To Be Processed
   * <li>1 : Processed
   * <li>2 : Affectations Done
   * </ul>
   * @param actionStatus action status
   */
  public void setActionStatus(int actionStatus);

  /**
   * Set the role under which the user did the action
   * @param userRoleName the role's name
   */
  public void setUserRoleName(String userRoleName);
}