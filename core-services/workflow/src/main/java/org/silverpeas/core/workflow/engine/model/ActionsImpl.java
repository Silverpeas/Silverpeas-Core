/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;actions&gt; element of a Process Model.
 **/
@XmlRootElement(name = "actions")
@XmlAccessorType(XmlAccessType.NONE)
public class ActionsImpl implements Serializable, Actions {
  private static final long serialVersionUID = -8221333788348737417L;
  @XmlElement(name = "action", type = ActionImpl.class)
  private List<Action> actionList;

  /**
   * Constructor
   */
  public ActionsImpl() {
    actionList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see Actions#addAction(com.silverpeas.workflow
   * .api.model.Action)
   */
  @Override
  public void addAction(Action action) {
    actionList.add(action);
  }

  /*
   * (non-Javadoc)
   * @see Actions#createAction()
   */
  @Override
  public Action createAction() {
    return new ActionImpl();
  }

  /*
   * (non-Javadoc)
   * @see Actions#getAction(java.lang.String)
   */
  @Override
  public Action getAction(String name) throws WorkflowException {
    for (Action action : actionList) {
      if (action != null && action.getName().equals(name)) {
        return action;
      }
    }
    throw new WorkflowException("ActionsImpl.getAction(String)",
        "WorkflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL", name);
  }

  /*
   * (non-Javadoc)
   * @see Actions#getActions()
   */
  @Override
  public Action[] getActions() {
    if (actionList == null) {
      return new Action[0];
    }
    return actionList.toArray(new Action[actionList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see Actions#iterateAction()
   */
  @Override
  public Iterator<Action> iterateAction() {
    return actionList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Actions#removeAction(java.lang.String)
   */
  @Override
  public void removeAction(String strActionName) throws WorkflowException {
    if (actionList == null) {
      return;
    }
    Action action = createAction();
    action.setName(strActionName);

    if (!actionList.remove(action)) {
      throw new WorkflowException("ActionsImpl.removeAction()",
          "workflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL",
          strActionName == null ? "<null>" : strActionName);
    }
  }
}