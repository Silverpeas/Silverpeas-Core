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

/**
 * Interface describing a representation of the &lt;state&gt; element of a Process Model.
 */
public interface State {
  /**
   * Get the name of this state
   * @return state's name
   */
  public String getName();

  /**
   * Set the name of this state
   * @param state 's name
   */
  public void setName(String name);

  /**
   * Get label in specific language for the given role
   * @param lang label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String language);

  /**
   * Get all the labels
   * @return an object containing the collection of the labels
   */
  public ContextualDesignations getLabels();

  /**
   * Iterate through the Labels
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateLabel();

  /**
   * Add a label Method needed primarily by Castor
   */
  public void addLabel(ContextualDesignation label);

  /**
   * Create an object implementing ContextualDesignation Method needed primarily by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Get activity in specific language for the given role
   * @param lang activity's language
   * @param role role for which the activity is
   * @return wanted activity as a String object. If activity is not found, search activity with
   * given role and default language, if not found again, return the default activity in given
   * language, if not found again, return the default activity in default language, if not found
   * again, return empty string.
   */
  public String getActivity(String role, String language);

  /**
   * Get all the activities
   * @return an object containing the collection of the activities
   */
  public ContextualDesignations getActivities();

  /**
   * Iterate through the activities
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateActivity();

  /**
   * Add an activity Method needed primarily by Castor
   */
  public void addActivity(ContextualDesignation description);

  /**
   * Get description in specific language for the given role
   * @param lang description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  public String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * @return an object containing the collection of the descriptions
   */
  public ContextualDesignations getDescriptions();

  /**
   * Iterate through the descriptions
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateDescription();

  /**
   * Add a description Method needed primarily by Castor
   */
  public void addDescription(ContextualDesignation description);

  /**
   * Create and return an object implementing AllowedActions
   */
  public AllowedActions createAllowedActions();

  /**
   * Create and return an object implementing TimeOutActions
   */
  public TimeOutActions createTimeOutActions();

  /**
   * Get timeout actions for this state
   * @return timeout actions
   */
  public TimeOutAction[] getTimeOutActions();

  /**
   * Get actions available in this state
   * @return allowedActions allowed actions
   */
  public Action[] getAllowedActions();

  public Action[] getFilteredActions();

  /**
   * Get all the timeout actions
   * @return an object containing the collection of the timeout actions
   */
  public TimeOutActions getTimeOutActionsEx();

  /**
   * Set the timeout actions
   */
  public void setTimeOutActions(TimeOutActions timeOutActions);

  /**
   * Get all the allowed actions
   * @return an object containing the collection of the allowed actions
   */
  public AllowedActions getAllowedActionsEx();

  /**
   * Set the allowed actions
   */
  public void setAllowedActions(AllowedActions allowedActions);

  public void setFilteredActions(AllowedActions allowedActions);

  /**
   * Create and return an object implementing QalifiedUsers
   */
  public QualifiedUsers createQualifiedUsers();

  /**
   * Get all the users interested by this state
   * @return QualifiedUsers object containing interested users, or an empty QualifiedUsers object
   * but never <code>null</code>
   */
  public QualifiedUsers getInterestedUsers();

  /**
   * Get all the users interested by this state
   * @return QualifiedUsers object containing interested users, or <code>null</code> if none are
   * defined
   */
  public QualifiedUsers getInterestedUsersEx();

  /**
   * Set the users interested by this state
   */
  public void setInterestedUsers(QualifiedUsers interestedUsers);

  /**
   * Get all the users who can act in this state
   * @return QualifiedUsers object containing theses users, or an empty QualifiedUsers object but
   * never <code>null</code>
   */
  public QualifiedUsers getWorkingUsers();

  /**
   * Get all the users who can act in this state
   * @return QualifiedUsers object containing theses users, or <code>null</code> if none are defined
   */
  public QualifiedUsers getWorkingUsersEx();

  /**
   * Set the users who can act in this state
   */
  public void setWorkingUsers(QualifiedUsers workingUsers);

  /**
   * Get the timeout interval of this state
   * @return timeoutInterval interval in hours (as a int)
   */
  public int getTimeoutInterval();

  /**
   * Set the timeout interval of this state
   * @param iHours interval in hours (as a int)
   */
  public void setTimeoutInterval(int iHours);

  /**
   * Get the timeout action of this state Action that will played if timeout is trigerred
   * @return timeout action
   */
  public Action getTimeoutAction();

  /**
   * Set the timeout action of this state Action that will played if timeout is trigerred
   * @param timeoutAction timeout action
   */
  public void setTimeoutAction(Action timeoutAction);

  /**
   * Get flag for admin notification if true, the timeout manager will send a notification to all
   * supervisors
   * @return admin notification flag
   */
  public boolean getTimeoutNotifyAdmin();

  /**
   * Set flag for admin notification if true, the timeout manager will send a notification to all
   * supervisors
   * @param admin notification flag
   */
  public void setTimeoutNotifyAdmin(boolean timeoutAction);

  /**
   * Get the pre-conditions to enter this state
   * @return state's Preconditions object containing re-conditions
   */
  // public Preconditions getPreconditions()

}