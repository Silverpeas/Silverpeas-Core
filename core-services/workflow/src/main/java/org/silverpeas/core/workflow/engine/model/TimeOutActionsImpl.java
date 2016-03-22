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

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import org.silverpeas.core.workflow.api.model.TimeOutAction;
import org.silverpeas.core.workflow.api.model.TimeOutActions;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;timeoutActions&gt; element of a Process Model.
 **/
public class TimeOutActionsImpl implements Serializable, TimeOutActions {
  private static final long serialVersionUID = 7973279591384251532L;
  private List<TimeOutAction> timeoutActionList;

  /**
   * Constructor
   */
  public TimeOutActionsImpl() {
    timeoutActionList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#createAllowedAction()
   */
  public TimeOutAction createTimeoutAction() {
    return new TimeOutActionImpl();
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#getAllowedActions()
   */
  public TimeOutAction[] getTimeOutActions() {

    if (timeoutActionList == null) {
      return null;
    }
    return timeoutActionList.toArray(new TimeOutAction[timeoutActionList.size()]);
  }

  @Override
  public void addTimeOutAction(TimeOutAction timeOutAction) {
    timeoutActionList.add(timeOutAction);
  }

  @Override
  public TimeOutAction createTimeOutAction() {
    return new TimeOutActionImpl();
  }

  @Override
  public Iterator<TimeOutAction> iterateTimeOutAction() {
    return timeoutActionList.iterator();
  }

}