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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Actions;

/**
 * Class implementing the representation of the &lt;actions&gt; element of a Process Model.
 **/
public class ActionsImpl implements Serializable, Actions {
  // private Hashtable actionList;
  private List actionList;

  /**
   * Constructor
   */
  public ActionsImpl() {
    actionList = new ArrayList();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#addAction(com.silverpeas.workflow
   * .api.model.Action)
   */
  public void addAction(Action action) {
    actionList.add(action);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#createAction()
   */
  public Action createAction() {
    return new ActionImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#getAction(java.lang.String)
   */
  public Action getAction(String name) throws WorkflowException {
    boolean find = false;
    Action action = null;

    for (int a = 0; !find && a < actionList.size(); a++) {
      action = (Action) actionList.get(a);
      if (action != null && action.getName().equals(name))
        find = true;
    }

    if (find)
      return action;
    else
      throw new WorkflowException("ActionsImpl.getAction(String)",
          "WorkflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL", name);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#getActions()
   */
  public Action[] getActions() {
    if (actionList == null)
      return null;

    return (Action[]) actionList.toArray(new ActionImpl[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#iterateAction()
   */
  public Iterator iterateAction() {
    return actionList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Actions#removeAction(java.lang.String)
   */
  public void removeAction(String strActionName) throws WorkflowException {
    Action action = createAction();

    action.setName(strActionName);

    if (actionList == null)
      return;

    if (!actionList.remove(action))
      throw new WorkflowException("ActionsImpl.removeAction()", //$NON-NLS-1$
          "workflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL", // $NON-NLS-1$
          strActionName == null ? "<null>" //$NON-NLS-1$
              : strActionName);
  }
}