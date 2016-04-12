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

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.AllowedAction;
import org.silverpeas.core.workflow.api.model.AllowedActions;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;allowedActions&gt; element of a Process Model.
 **/
public class ActionRefs implements Serializable, AllowedActions {

  private static final long serialVersionUID = -8973726281784516470L;
  private List<AllowedAction> actionRefList;

  /**
   * Constructor
   */
  public ActionRefs() {
    actionRefList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.AllowedActions#addAllowedAction(com.
   * silverpeas.workflow.api.model.AllowedAction)
   */
  @Override
  public void addAllowedAction(AllowedAction allowedAction) {
    actionRefList.add(allowedAction);
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#createAllowedAction()
   */
  @Override
  public AllowedAction createAllowedAction() {
    return new ActionRef();
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#iterateAllowedAction()
   */
  @Override
  public Iterator<AllowedAction> iterateAllowedAction() {
    return actionRefList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#getAllowedActions()
   */
  @Override
  public Action[] getAllowedActions() {
    Action[] result = null;

    if (actionRefList == null) {
      return null;
    }

    // construct the Action array
    result = new ActionImpl[actionRefList.size()];
    for (int i = 0; i < actionRefList.size(); i++) {
      result[i] = actionRefList.get(i).getAction();
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#getAllowedAction(java. lang.String)
   */
  @Override
  public AllowedAction getAllowedAction(String strActionName) {
    AllowedAction allowedAction = new ActionRef();
    Action action = new ActionImpl();
    action.setName(strActionName);
    allowedAction.setAction(action);
    int idx = actionRefList.indexOf(allowedAction);

    if (idx >= 0) {
      return actionRefList.get(idx);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see AllowedActions#removeAllowedAction(java .lang.String)
   */
  @Override
  public void removeAllowedAction(String strAllowedActionName)
      throws WorkflowException {
    AllowedAction actionRef = createAllowedAction();
    Action action = new ActionImpl();

    action.setName(strAllowedActionName);
    actionRef.setAction(action);
    if (!actionRefList.remove(actionRef)) {
      throw new WorkflowException("ActionRefs.removeAllowedAction(String)",
          "workflowEngine.EX_ALLOWED_ACTION_NOT_FOUND",
          strAllowedActionName == null ? "<null>" : strAllowedActionName);
    }
  }
}