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
import java.util.Iterator;

import org.silverpeas.core.workflow.api.model.AbstractDescriptor;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.AllowedActions;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.TimeOutAction;
import org.silverpeas.core.workflow.api.model.TimeOutActions;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;state&gt; element of a Process Model.
 **/
public class StateImpl extends AbstractReferrableObject implements State, AbstractDescriptor,
    Serializable {
  private static final long serialVersionUID = -3019436287850255663L;
  private String name;
  private ContextualDesignations labels; // collection of labels
  private ContextualDesignations descriptions; // collection of descriptions
  private ContextualDesignations activities; // collection of activities
  private QualifiedUsers workingUsers;
  private QualifiedUsers interestedUsers;
  private AllowedActions allowedActions;
  private AllowedActions filteredActions;
  private TimeOutActions timeOutActions;
  private Action timeOutAction;
  private int timeoutInterval;
  private boolean timeoutNotifyAdmin;

  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public StateImpl() {
    reset();
  }

  /**
   * Constructor
   * @param name state name
   */
  public StateImpl(String name) {
    this();
    this.name = name;
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new SpecificLabelListHelper();
    descriptions = new SpecificLabelListHelper();
    activities = new SpecificLabelListHelper();
    timeOutAction = null;
    timeoutInterval = -1;
    timeoutNotifyAdmin = true;
  }

  // //////////////////
  // labels
  // //////////////////

  /*
   * (non-Javadoc)
   * @see State#getLabels()
   */
  public ContextualDesignations getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * @see State#getLabel(java.lang.String, java.lang.String)
   */
  public String getLabel(String role, String language) {
    return labels.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.State#addLabel(com.silverpeas.workflow.api.model.
   * ContextualDesignation)
   */
  public void addLabel(ContextualDesignation label) {
    labels.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see State#iterateLabel()
   */
  public Iterator<ContextualDesignation> iterateLabel() {
    return labels.iterateContextualDesignation();
  }

  // //////////////////
  // activities
  // //////////////////

  /*
   * (non-Javadoc)
   * @see State#getActivities()
   */
  public ContextualDesignations getActivities() {
    return activities;
  }

  /*
   * (non-Javadoc)
   * @see State#getActivity(java.lang.String, java.lang.String)
   */
  public String getActivity(String role, String language) {
    return activities.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.State#addActivity(com.silverpeas.workflow.api.model.
   * ContextualDesignation)
   */
  public void addActivity(ContextualDesignation label) {
    activities.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see State#iterateActivity()
   */
  public Iterator<ContextualDesignation> iterateActivity() {
    return activities.iterateContextualDesignation();
  }

  // //////////////////
  // descriptions
  // //////////////////

  /*
   * (non-Javadoc)
   * @see State#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return descriptions;
  }

  /*
   * (non-Javadoc)
   * @see State#getDescription(java.lang.String, java.lang.String)
   */
  public String getDescription(String role, String language) {
    return descriptions.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @seecom.silverpeas.workflow.api.model.State#addDescription(com.silverpeas.workflow.api.model.
   * ContextualDesignation)
   */
  public void addDescription(ContextualDesignation description) {
    descriptions.addContextualDesignation(description);
  }

  /*
   * (non-Javadoc)
   * @see State#iterateDescription()
   */
  public Iterator<ContextualDesignation> iterateDescription() {
    return descriptions.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see State#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return labels.createContextualDesignation();
  }

  // //////////////////
  // Miscellaneous
  // //////////////////

  /**
   * Get actions available in this state
   * @return allowedActions allowed actions
   */
  public Action[] getAllowedActions() {
    // check for allowedActions attribute
    if (allowedActions == null)
      return null;

    return allowedActions.getAllowedActions();
  }

  /**
   * Get timeout actions for this state
   * @return timeout actions
   */
  public TimeOutAction[] getTimeOutActions() {
    if (timeOutActions == null)
      return null;

    return timeOutActions.getTimeOutActions();
  }

  public TimeOutActions getTimeOutActionsEx() {
    return timeOutActions;
  }

  /*
   * (non-Javadoc)
   * @see State#getAllAllowedActions()
   */
  public AllowedActions getAllowedActionsEx() {
    return allowedActions;
  }

  public AllowedActions createAllowedActions() {
    return new ActionRefs();
  }

  public TimeOutActions createTimeOutActions() {
    return new TimeOutActionsImpl();
  }

  public Action[] getFilteredActions() {
    if (filteredActions == null)
      return null;

    return filteredActions.getAllowedActions();
  }

  @Override
  public void setFilteredActions(AllowedActions allowedActions) {
    filteredActions = allowedActions;
  }

  /*
   * (non-Javadoc)
   * @see State#getInterestedUsers()
   */
  public QualifiedUsers getInterestedUsers() {
    if (interestedUsers == null)
      return new QualifiedUsersImpl();
    else
      return this.interestedUsers;
  }

  /*
   * (non-Javadoc)
   * @see State#getInterestedUsersEx()
   */
  public QualifiedUsers getInterestedUsersEx() {
    return interestedUsers;
  }

  /**
   * Get the name of this state
   * @return state's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the pre-conditions to enter this state
   * @return state's Preconditions object containing re-conditions
   */
  /*
   * public Preconditions getPreconditions() { return this.preconditions; }
   */

  /*
   * (non-Javadoc)
   * @see State#getWorkingUsers()
   */
  public QualifiedUsers getWorkingUsers() {
    if (workingUsers == null)
      return new QualifiedUsersImpl();
    else
      return this.workingUsers;
  }

  /*
   * (non-Javadoc)
   * @see State#getWorkingUsersEx()
   */
  public QualifiedUsers getWorkingUsersEx() {
    return workingUsers;
  }

  /**
   *
   */
  public void setTimeOutActions(TimeOutActions timeOutActions) {
    this.timeOutActions = timeOutActions;
  }

  /*
   * (non-Javadoc)
   * @see
   * State#setAllowedActions(com.silverpeas.workflow.api.model
   * .AllowedActions)
   */
  public void setAllowedActions(AllowedActions allowedActions) {
    this.allowedActions = allowedActions;
  }

  /*
   * (non-Javadoc)
   * @see State#createQualifiedUsers()
   */
  public QualifiedUsers createQualifiedUsers() {
    return new QualifiedUsersImpl();
  }

  /**
   * Set all the users interested by this state
   * @param QualifiedUsers object containing interested users
   */
  public void setInterestedUsers(QualifiedUsers interestedUsers) {
    this.interestedUsers = interestedUsers;
  }

  /*
   * (non-Javadoc)
   * @see State#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the pre-conditions to enter this state
   * @param state's Preconditions object containing re-conditions
   */
  /*
   * public void setPreconditions(Preconditions preconditions) { this.preconditions = preconditions;
   * }
   */

  /**
   * Set all the users who can act in this state
   * @param QualifiedUsers object containing these users
   */
  public void setWorkingUsers(QualifiedUsers workingUsers) {
    this.workingUsers = workingUsers;
  }

  /*
   * (non-Javadoc)
   * @see State#getTimeoutInterval()
   */
  public int getTimeoutInterval() {
    return timeoutInterval;
  }

  /*
   * (non-Javadoc)
   * @see State#setTimeoutInterval(int)
   */
  public void setTimeoutInterval(int hours) {
    timeoutInterval = hours;
  }

  /**
   * Get the timeout interval of this state
   * @return timeoutInterval interval in hours (as a String)
   */
  public String castor_getTimeoutInterval() {
    if (timeoutInterval != -1)
      return String.valueOf(timeoutInterval);
    else
      return null;
  }

  /**
   * Set the timeout interval of this state
   * @param timeoutInterval interval in hours
   */
  public void castor_setTimeoutInterval(String timeoutInterval) {
    try {
      this.timeoutInterval = (Integer.valueOf(timeoutInterval)).intValue();
    } catch (NumberFormatException e) {
      this.timeoutInterval = -1;
    }
  }

  /**
   * Get the timeout action of this state Action that will played if timeout is triggered
   * @return timeout action
   */
  public Action getTimeoutAction() {
    return timeOutAction;
  }

  /*
   * (non-Javadoc)
   * @see
   * State#setTimeoutAction(com.silverpeas.workflow.api.model.
   * Action)
   */
  public void setTimeoutAction(Action timeoutAction) {
    this.timeOutAction = timeoutAction;
  }

  /**
   * Get flag for admin notification if true, the timeout manager will send a notification to all
   * supervisors
   * @return admin notification flag
   */
  public boolean getTimeoutNotifyAdmin() {
    return timeoutNotifyAdmin;
  }

  /*
   * (non-Javadoc)
   * @see State#setTimeoutNotifyAdmin(boolean)
   */
  public void setTimeoutNotifyAdmin(boolean timeoutAction) {
    this.timeoutNotifyAdmin = timeoutAction;
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  public String getKey() {
    return (this.name);
  }

  /************* Implemented methods *****************************************/
  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#setId(int)
   */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getId()
   */
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see
   * AbstractDescriptor#setParent(com.silverpeas.workflow.api.
   * model.AbstractDescriptor)
   */
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getParent()
   */
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }
}