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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface describing a representation of the &lt;consequence&gt; element of a Process Model.
 */
public interface Consequence {
  /**
   * Get the item on which the comparison will be processed
   * @return the item name
   */
  public String getItem();

  /**
   * Set the item on which the comparison will be processed
   * @param the item
   */
  public void setItem(String strName);

  /**
   * Get the operator used to compare item and value
   * @return an operator (ie : =, >, >=, <, <=, !=)
   */
  public String getOperator();

  /**
   * Set the operator used to compare item and value
   * @param the operator (ie : =, >, >=, <, <=, !=)
   */
  public void setOperator(String strOperator);

  /**
   * Get the value to compare with item
   * @return the value
   */
  public String getValue();

  /**
   * Set the value to compare with item
   * @param the value
   */
  public void setValue(String strValue);

  /**
   * Check if the consequence is verified
   * @return true if comparison is verified, false otherwise.
   */
  public boolean isVerified(String itemValue);

  /**
   * Get the target states
   * @return the target states as a Vector
   */
  public State[] getTargetStates();

  /**
   * Get the target state with the given state name
   * @param strStateName the name of the state
   * @return the target state or <code>null</code> if not found
   */
  public State getTargetState(String strStateName);

  /**
   * Iterate through Target States.
   * @return an Iterator
   */
  public Iterator<StateSetter> iterateTargetState();

  /**
   * Create a new state Setter object
   * @return a StateSetter object
   */
  public StateSetter createStateSetter();

  /**
   * Add a new Target State to the collection
   * @param stateSetter object to be added
   */
  void addTargetState(StateSetter stateSetter);

  /**
   * Get the state to un-set with the given state name
   * @param strStateName the name of the state
   * @return the state to un-set or <code>null</code> if not found
   */
  public State getUnsetState(String strStateName);

  /**
   * Get the states to un-set
   * @return the un-set states as a Vector
   */
  public State[] getUnsetStates();

  /**
   * Iterate through Unset States.
   * @return an Iterator
   */
  public Iterator<StateSetter> iterateUnsetState();

  /**
   * Add a new Unset State to the collection
   * @param stateSetter object to be added
   */
  public void addUnsetState(StateSetter stateSetter);

  /**
   * Add a new notifiedUser to the collection
   * @param stateSetter object to be added
   */
  public void addNotifiedUsers(QualifiedUsers notifyUsers);

  /**
   * Get the flag that specifies if instance has to be removed
   * @return true if instance has to be removed
   */
  public boolean getKill();

  /**
   * Set the flag that specifies if instance has to be removed
   * @param kill true if instance has to be removed
   */
  public void setKill(boolean kill);

  /**
   * Get all the users that have to be notified
   * @return QualifiedUsers object containing notified users or an empty QualifiedUsers object but
   * never a <code>null</code>
   */
  public List<QualifiedUsers> getNotifiedUsers();

  /**
   * Set all the users that have to be notified
   * @param QualifiedUsers object containing notified users
   */
  public void setNotifiedUsers(List<QualifiedUsers> notifiedUsersList);

  public Triggers createTriggers();

  public Triggers getTriggers();
}