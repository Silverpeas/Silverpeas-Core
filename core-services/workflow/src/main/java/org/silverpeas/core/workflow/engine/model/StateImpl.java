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

import org.silverpeas.core.workflow.api.model.*;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class implementing the representation of the &lt;state&gt; element of a Process Model.
 **/
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.NONE)
public class StateImpl implements State {
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

  @Override
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  @Override
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  @Override
  public ContextualDesignations getActivities() {
    return new SpecificLabelListHelper(activities);
  }

  @Override
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  @Override
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

  @Override
  public Action[] getAllowedActions() {
    // check for allowedActions attribute
    if (allowedActions == null) {
      return new Action[0];
    }
    return allowedActions.getAllowedActions();
  }

  @Override
  public TimeOutAction[] getTimeOutActions() {
    if (timeOutActions == null) {
      return new TimeOutAction[0];
    }

    return timeOutActions.getTimeOutActions();
  }

  @Override
  public AllowedActions getAllowedActionsEx() {
    return allowedActions;
  }

  @Override
  public AllowedActions createAllowedActions() {
    return new ActionRefs();
  }

  @Override
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

  @Override
  public QualifiedUsers getInterestedUsers() {
    return Objects.requireNonNullElseGet(interestedUsers, QualifiedUsersImpl::new);
  }

  @Override
  public QualifiedUsers getInterestedUsersEx() {
    return interestedUsers;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public QualifiedUsers getWorkingUsers() {
    return Objects.requireNonNullElseGet(workingUsers, QualifiedUsersImpl::new);
  }

  @Override
  public QualifiedUsers getWorkingUsersEx() {
    return workingUsers;
  }

  @Override
  public void setAllowedActions(AllowedActions allowedActions) {
    this.allowedActions = allowedActions;
  }

  @Override
  public void setInterestedUsers(QualifiedUsers interestedUsers) {
    this.interestedUsers = interestedUsers;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setWorkingUsers(QualifiedUsers workingUsers) {
    this.workingUsers = workingUsers;
  }

  @Override
  public int getTimeoutInterval() {
    return timeoutInterval;
  }

  @Override
  public void setTimeoutInterval(int hours) {
    timeoutInterval = hours;
  }

  @Override
  public Action getTimeoutAction() {
    return timeOutAction;
  }

  @Override
  public void setTimeoutAction(Action timeoutAction) {
    this.timeOutAction = (ActionImpl) timeoutAction;
  }

  @Override
  public boolean getTimeoutNotifyAdmin() {
    return timeoutNotifyAdmin;
  }

  @Override
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