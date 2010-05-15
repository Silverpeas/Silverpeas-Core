/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.AllowedAction;
import com.silverpeas.workflow.api.model.AllowedActions;

/**
 * Class implementing the representation of the &lt;allowedActions&gt; element of a Process Model.
 **/
public class ActionRefs implements Serializable, AllowedActions {
  private Vector actionRefList;

  /**
   * Constructor
   */
  public ActionRefs() {
    actionRefList = new Vector();
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.AllowedActions#addAllowedAction(com.
   * silverpeas.workflow.api.model.AllowedAction)
   */
  public void addAllowedAction(AllowedAction allowedAction) {
    actionRefList.add(allowedAction);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AllowedActions#createAllowedAction()
   */
  public AllowedAction createAllowedAction() {
    return new ActionRef();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AllowedActions#iterateAllowedAction()
   */
  public Iterator iterateAllowedAction() {
    return actionRefList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AllowedActions#getAllowedActions()
   */
  public Action[] getAllowedActions() {
    Action[] result = null;

    if (actionRefList == null)
      return null;

    // construct the Action array
    result = new ActionImpl[actionRefList.size()];
    for (int i = 0; i < actionRefList.size(); i++) {
      result[i] = ((AllowedAction) actionRefList.get(i)).getAction();
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AllowedActions#getAllowedAction(java. lang.String)
   */
  public AllowedAction getAllowedAction(String strActionName) {
    AllowedAction allowedAction = new ActionRef();
    Action action = new ActionImpl();
    int idx;

    action.setName(strActionName);
    allowedAction.setAction(action);

    idx = actionRefList.indexOf(allowedAction);

    if (idx >= 0)
      return (AllowedAction) actionRefList.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AllowedActions#removeAllowedAction(java .lang.String)
   */
  public void removeAllowedAction(String strAllowedActionName)
      throws WorkflowException {
    AllowedAction actionRef = createAllowedAction();
    Action action = new ActionImpl();

    action.setName(strAllowedActionName);
    actionRef.setAction(action);
    if (!actionRefList.remove(actionRef))
      throw new WorkflowException("ActionRefs.removeAllowedAction(String)",
          "workflowEngine.EX_ALLOWED_ACTION_NOT_FOUND",
          strAllowedActionName == null ? "<null>" : strAllowedActionName);
  }
}