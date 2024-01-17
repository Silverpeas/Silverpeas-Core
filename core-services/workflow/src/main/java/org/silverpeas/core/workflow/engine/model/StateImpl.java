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

import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.AllowedActions;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.TimeOutAction;
import org.silverpeas.core.workflow.api.model.TimeOutActions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;state&gt; element of a Process Model.
 **/
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.NONE)
public class StateImpl implements State, Serializable {
  private static final long serialVersionUID = -3019436287850255663L;

  @XmlAttribute
  @XmlID
  private String name;

  // collection of labels
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  // collection of descriptions
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;
  // collection of activities
  @XmlElement(name = "activity", type = SpecificLabel.class)
  private List<ContextualDesignation> activities;
  @XmlElement(type = QualifiedUsersImpl.class)
  private QualifiedUsers workingUsers;
  @XmlElement(type = QualifiedUsersImpl.class)
  private QualifiedUsers interestedUsers;
  @XmlElement(type = ActionRefs.class)
  private AllowedActions allowedActions;
  private AllowedActions filteredActions;
  @XmlElement(type = TimeOutActionsImpl.class)
  private TimeOutActions timeOutActions;
  @XmlIDREF
  @XmlAttribute
  private ActionImpl timeOutAction;
  @XmlAttribute
  private int timeoutInterval;
  @XmlAttribute
  private boolean timeoutNotifyAdmin;


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
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
    activities = new ArrayList<>();
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
    return new SpecificLabelListHelper(labels);
  }

  /*
   * (non-Javadoc)
   * @see State#getLabel(java.lang.String, java.lang.String)
   */
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  // //////////////////
  // activities
  // //////////////////

  /*
   * (non-Javadoc)
   * @see State#getActivities()
   */
  public ContextualDesignations getActivities() {
    return new SpecificLabelListHelper(activities);
  }

  /*
   * (non-Javadoc)
   * @see State#getActivity(java.lang.String, java.lang.String)
   */
  public String getActivity(String role, String language) {
    return getActivities().getLabel(role, language);
  }

  // //////////////////
  // descriptions
  // //////////////////

  /*
   * (non-Javadoc)
   * @see State#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  /*
   * (non-Javadoc)
   * @see State#getDescription(java.lang.String, java.lang.String)
   */
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
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
    if (allowedActions == null) {
      return new Action[0];
    }
    return allowedActions.getAllowedActions();
  }

  /**
   * Get timeout actions for this state
   * @return timeout actions
   */
  public TimeOutAction[] getTimeOutActions() {
    if (timeOutActions == null) {
      return new TimeOutAction[0];
    }

    return timeOutActions.getTimeOutActions();
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

  public Action[] getFilteredActions() {
    if (filteredActions == null) {
      return new Action[0];
    }

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
    if (interestedUsers == null) {
      return new QualifiedUsersImpl();
    } else {
      return this.interestedUsers;
    }
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

  /*
   * (non-Javadoc)
   * @see State#getWorkingUsers()
   */
  public QualifiedUsers getWorkingUsers() {
    if (workingUsers == null) {
      return new QualifiedUsersImpl();
    } else {
      return this.workingUsers;
    }
  }

  /*
   * (non-Javadoc)
   * @see State#getWorkingUsersEx()
   */
  public QualifiedUsers getWorkingUsersEx() {
    return workingUsers;
  }

  /*
   * (non-Javadoc)
   *
   * State#setAllowedActions(com.silverpeas.workflow.api.model
   * .AllowedActions)
   */
  public void setAllowedActions(AllowedActions allowedActions) {
    this.allowedActions = allowedActions;
  }

  /**
   * Set all the users interested by this state
   * @param interestedUsers object containing interested users
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
   * Set all the users who can act in this state
   * @param workingUsers object containing these users
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
   * Get the timeout action of this state Action that will played if timeout is triggered
   * @return timeout action
   */
  public Action getTimeoutAction() {
    return timeOutAction;
  }

  /*
   * (non-Javadoc)
   *
   * State#setTimeoutAction(com.silverpeas.workflow.api.model.
   * Action)
   */
  public void setTimeoutAction(Action timeoutAction) {
    this.timeOutAction = (ActionImpl) timeoutAction;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final StateImpl state = (StateImpl) o;

    return name.equals(state.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}