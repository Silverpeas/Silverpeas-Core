/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.system;

import java.io.Serializable;

/**
 * It is a generic representation of a transition between two states of a state-machine, whatever
 * the state-machine is. All classes presenting a transition with business logic should extend
 * this class and adds the treatment specific to the transition they represent.
 * @param <T> the type representing a state in the state-machine.
 * @author mmoquillon
 */
public class StateTransition<T extends Serializable> implements Serializable {

  private final T before;
  private final T after;

  /**
   * Constructs a new instance representing a transition between the two specified states.
   * @param beforeTransition the state before the transition is occurred.
   * @param afterTransition the state after the transition is occurred.
   * @param <T> the serializable type of the state.
   * @return a new transition between two states of type T.
   */
  public static <T extends Serializable> StateTransition<T> transitionBetween(T beforeTransition,
      T afterTransition) {
    return new StateTransition<>(beforeTransition, afterTransition);
  }

  /**
   * Constructs a new instance representing a transition between the two specified states.
   * @param beforeTransition the state before the transition is occurred.
   * @param afterTransition the state after the transition is occurred.
   */
  public StateTransition(T beforeTransition, T afterTransition) {
    this.before = beforeTransition;
    this.after = afterTransition;
  }

  /**
   * Gets the state before the transition occurred.
   * @return the state before this transition.
   */
  public T getBefore() {
    return before;
  }

  /**
   * Gets the state after the transition occurred.
   * @return the state after this transition.
   */
  public T getAfter() {
    return after;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final StateTransition that = (StateTransition) o;

    if (after != null ? !after.equals(that.after) : that.after != null) {
      return false;
    }
    if (before != null ? !before.equals(that.before) : that.before != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = before != null ? before.hashCode() : 0;
    result = 31 * result + (after != null ? after.hashCode() : 0);
    return result;
  }
}
