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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.States;

/**
 * Class implementing the representation of the &lt;states&gt; element of a Process Model.
 **/
public class StatesImpl implements Serializable, States {

  private static final long serialVersionUID = -2580715672830095678L;
  private List<State> stateList;

  /**
   * Constructor
   */
  public StatesImpl() {
    stateList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see States#addState(com.silverpeas.workflow
   * .api.model.State)
   */
  @Override
  public void addState(State state) {
    stateList.add(state);
  }

  /*
   * (non-Javadoc)
   * @see States#createState()
   */
  public State createState() {
    return new StateImpl();
  }

  /*
   * (non-Javadoc)
   * @see States#getState(java.lang.String)
   */
  @Override
  public State getState(String name) {
    for (State state : stateList) {
      if (state != null && state.getName().equals(name)) {
        return state;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see States#getStates()
   */
  @Override
  public State[] getStates() {
    if (stateList == null) {
      return null;
    }
    return stateList.toArray(new State[stateList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see States#iterateState()
   */
  public Iterator<State> iterateState() {
    return stateList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see States#removeState(java.lang.String)
   */
  @Override
  public void removeState(String strStateName) throws WorkflowException {
    if (stateList == null) {
      return;
    }
    State state = createState();
    state.setName(strStateName);

    if (!stateList.remove(state)) {
      throw new WorkflowException("StatesImpl.removeState()", "workflowEngine.EX_STATE_NOT_FOUND",
          strStateName == null ? "<null>" : strStateName);
    }
  }
}