/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

/**
 * Interface describing a representation of the &lt;state&gt; element of a Process Model.
 */
public interface State {
  /**
   * Get the name of this state
   * @return state's name
   */
  String getName();

  /**
   * Set the name of this state
   * @param name state's name
   */
  void setName(String name);

  /**
   * Get label in specific language for the given role
   * @param language label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  String getLabel(String role, String language);

  /**
   * Get all the labels
   * @return an object containing the collection of the labels
   */
  ContextualDesignations getLabels();

  /**
   * Get all the activities
   * @return an object containing the collection of the activities
   */
  ContextualDesignations getActivities();

  /**
   * Get description in specific language for the given role
   * @param language description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * @return an object containing the collection of the descriptions
   */
  ContextualDesignations getDescriptions();

  /**
   * Create and return an object implementing AllowedActions
   */
  AllowedActions createAllowedActions();

  /**
   * Get timeout actions for this state
   * @return timeout actions
   */
  TimeOutAction[] getTimeOutActions();

  /**
   * Get actions available in this state
   * @return allowedActions allowed actions
   */
  Action[] getAllowedActions();

  Action[] getFilteredActions();

  /**
   * Get all the allowed actions
   * @return an object containing the collection of the allowed actions
   */
  AllowedActions getAllowedActionsEx();

  /**
   * Set the allowed actions
   */
  void setAllowedActions(AllowedActions allowedActions);

  void setFilteredActions(AllowedActions allowedActions);

  /**
   * Get all the users interested by this state
   * @return QualifiedUsers object containing interested users, or an empty QualifiedUsers object
   * but never <code>null</code>
   */
  QualifiedUsers getInterestedUsers();

  /**
   * Get all the users interested by this state
   * @return QualifiedUsers object containing interested users, or <code>null</code> if none are
   * defined
   */
  QualifiedUsers getInterestedUsersEx();

  /**
   * Set the users interested by this state
   */
  void setInterestedUsers(QualifiedUsers interestedUsers);

  /**
   * Get all the users who can act in this state
   * @return QualifiedUsers object containing theses users, or an empty QualifiedUsers object but
   * never <code>null</code>
   */
  QualifiedUsers getWorkingUsers();

  /**
   * Get all the users who can act in this state
   * @return QualifiedUsers object containing theses users, or <code>null</code> if none are defined
   */
  QualifiedUsers getWorkingUsersEx();

  /**
   * Set the users who can act in this state
   */
  void setWorkingUsers(QualifiedUsers workingUsers);

  /**
   * Get the timeout interval of this state
   * @return timeoutInterval interval in hours (as a int)
   */
  int getTimeoutInterval();

  /**
   * Set the timeout interval of this state
   * @param iHours interval in hours (as a int)
   */
  void setTimeoutInterval(int iHours);

  /**
   * Get the timeout action of this state Action that will played if timeout is trigerred
   * @return timeout action
   */
  Action getTimeoutAction();

  /**
   * Set the timeout action of this state Action that will played if timeout is trigerred
   * @param timeoutAction timeout action
   */
  void setTimeoutAction(Action timeoutAction);

  /**
   * Get flag for admin notification if true, the timeout manager will send a notification to all
   * supervisors
   * @return admin notification flag
   */
  boolean getTimeoutNotifyAdmin();

  /**
   * Set flag for admin notification if true, the timeout manager will send a notification to all
   * supervisors
   * @param timeoutAction notification flag
   */
  void setTimeoutNotifyAdmin(boolean timeoutAction);

}