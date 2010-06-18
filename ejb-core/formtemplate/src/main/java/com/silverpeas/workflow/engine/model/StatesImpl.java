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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.States;

/**
 * Class implementing the representation of the &lt;states&gt; element of a Process Model.
 **/
public class StatesImpl implements Serializable, States {
  private List stateList;

  /**
   * Constructor
   */
  public StatesImpl() {
    stateList = new ArrayList();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#addState(com.silverpeas.workflow
   * .api.model.State)
   */
  public void addState(State state) {
    stateList.add(state);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#createState()
   */
  public State createState() {
    return new StateImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#getState(java.lang.String)
   */
  public State getState(String name) {
    boolean find = false;
    State state = null;

    for (int s = 0; !find && s < stateList.size(); s++) {
      state = (State) stateList.get(s);
      if (state != null && state.getName().equals(name))
        find = true;
    }
    if (find)
      return state;

    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#getStates()
   */
  public State[] getStates() {
    if (stateList == null)
      return null;

    return (State[]) stateList.toArray(new StateImpl[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#iterateState()
   */
  public Iterator iterateState() {
    return stateList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.States#removeState(java.lang.String)
   */
  public void removeState(String strStateName) throws WorkflowException {
    State state = createState();

    state.setName(strStateName);

    if (stateList == null)
      return;

    if (!stateList.remove(state))
      throw new WorkflowException("StatesImpl.removeState()", //$NON-NLS-1$
          "workflowEngine.EX_STATE_NOT_FOUND", // $NON-NLS-1$
          strStateName == null ? "<null>" //$NON-NLS-1$
              : strStateName);
  }
}