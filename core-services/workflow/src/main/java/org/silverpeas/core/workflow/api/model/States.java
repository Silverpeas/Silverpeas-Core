/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.workflow.api.WorkflowException;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;states&gt; element of a Process Model.
 */
public interface States extends Serializable {

  /**
   * Iterate through the State objects
   * @return an iterator
   */
  Iterator<State> iterateState();

  /**
   * Create an State
   * @return an object implementing State
   */
  State createState();

  /**
   * Add an state to the collection
   * @param state to be added
   */
  void addState(State state);

  /**
   * Get the states defined for this process model
   * @return states defined for this process model
   */
  State[] getStates();

  /**
   * Get the state definition with given name
   * @param name state name
   * @return wanted state definition
   */
  State getState(String name);

  /**
   * Remove an state from the collection
   * @param strStateName the name of the state to be removed.
   * @throws WorkflowException when the state cannot be found
   */
  void removeState(String strStateName) throws WorkflowException;
}
