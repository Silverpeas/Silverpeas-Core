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
import java.util.Vector;

import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.Triggers;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.workflow.api.model.AbstractDescriptor;
import org.silverpeas.core.workflow.api.model.Consequence;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.StateSetter;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;consequence&gt; element of a Process Model.
 */
public class ConsequenceImpl extends AbstractReferrableObject implements Consequence,
    AbstractDescriptor, Serializable {

  private static final long serialVersionUID = -905677587105320693L;
  private String item;
  private String operator;
  private String value;
  private boolean kill;
  private Vector<StateSetter> targetStateList;
  private Vector<StateSetter> unsetStateList;
  private List<QualifiedUsers> notifiedUsersList;
  private int step;
  private Triggers triggers;
  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////
  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public ConsequenceImpl() {
    targetStateList = new Vector<>();
    unsetStateList = new Vector<>();
    notifiedUsersList = new ArrayList<>();
    triggers = new TriggersImpl();
    kill = false;
  }

  /*
   * (non-Javadoc) @see Consequence#getTargetState(java.lang.
   * String)
   */
  public State getTargetState(String strStateName) {
    for (int i = 0; i < targetStateList.size(); i++) {
      if (targetStateList.get(i).getState().getName().equals(
          strStateName)) {
        return targetStateList.get(i).getState();
      }
    }

    return null;
  }

  /**
   * Get the target states
   * @return the target states as a Vector
   */
  public State[] getTargetStates() {
    if (targetStateList == null) {
      return null;
    }

    State[] states = new StateImpl[targetStateList.size()];
    for (int i = 0; i < targetStateList.size(); i++) {
      StateRef ref = (StateRef) targetStateList.get(i);
      states[i] = ref.getState();
    }

    return states;
  }

  /*
   * (non-Javadoc) @see Consequence#addTargetState(com.silverpeas
   * .workflow.api.model.StateSetter)
   */
  public void addTargetState(StateSetter stateSetter) {
    targetStateList.add(stateSetter);
  }

  /*
   * (non-Javadoc) @see Consequence#createStateSetter()
   */
  public StateSetter createStateSetter() {
    return new StateRef();
  }

  /*
   * (non-Javadoc) @see Consequence#iterateTargetState()
   */
  public Iterator<StateSetter> iterateTargetState() {
    return targetStateList.iterator();
  }

  /*
   * (non-Javadoc) @see Consequence#iterateTargetState()
   */
  public Iterator<QualifiedUsers> iterateNotifiedUsers() {
    return notifiedUsersList.iterator();
  }

  /*
   * (non-Javadoc) @see Consequence#getUnsetState(java.lang.String
   * )
   */
  public State getUnsetState(String strStateName) {
    for (int i = 0; i < unsetStateList.size(); i++) {
      if (unsetStateList.get(i).getState().getName().equals(
          strStateName)) {
        return unsetStateList.get(i).getState();
      }
    }

    return null;
  }

  /**
   * Get the states to unset
   * @return the states to unset as a Vector
   */
  public State[] getUnsetStates() {
    if (unsetStateList == null) {
      return null;
    }

    State[] states = new StateImpl[unsetStateList.size()];
    for (int i = 0; i < unsetStateList.size(); i++) {
      StateRef ref = (StateRef) unsetStateList.get(i);
      states[i] = ref.getState();
    }

    return states;
  }

  /*
   * (non-Javadoc) @see Consequence#addUnsetState(com.silverpeas
   * .workflow.api.model.StateSetter)
   */
  public void addUnsetState(StateSetter stateSetter) {
    unsetStateList.add(stateSetter);
  }

  /*
   * (non-Javadoc) @see Consequence#iterateUnsetState()
   */
  public Iterator<StateSetter> iterateUnsetState() {
    return unsetStateList.iterator();
  }

  /**
   * Get the flag that specifies if instance has to be removed
   * @return true if instance has to be removed
   */
  public boolean getKill() {
    return kill;
  }

  /**
   * Set the flag that specifies if instance has to be removed
   * @param kill true if instance has to be removed
   */
  public void setKill(boolean kill) {
    this.kill = kill;
  }

  /**
   * Get all the users that have to be notified
   * @return QualifiedUsers object containing notified users
   */
  public List<QualifiedUsers> getNotifiedUsers() {
    return this.notifiedUsersList;
  }

  /**
   * Set all the users that have to be notified
   * @param QualifiedUsers object containing notified users
   */
  public void setNotifiedUsers(List<QualifiedUsers> notifiedUsersList) {
    this.notifiedUsersList = notifiedUsersList;
  }

  /**
   * Create and return an object implementing QalifiedUsers
   */
  public QualifiedUsers createQualifiedUsers() {
    return new QualifiedUsersImpl();
  }

  /*
   * (non-Javadoc) @see Consequence#getItem()
   */
  public String getItem() {
    return item;
  }

  /*
   * (non-Javadoc) @see Consequence#getOperator()
   */
  public String getOperator() {
    return operator;
  }

  /*
   * (non-Javadoc) @see Consequence#getValue()
   */
  public String getValue() {
    return value;
  }

  /**
   * Check if the consequence is verified or not
   * @param itemValue - the value of the folder item (specified in xml attribute 'item'
   * @return true if the consequence is verified
   */
  @Override
  public boolean isVerified(String itemValue) {
    if (getItem() == null && getOperator() == null && getValue() == null) {
      return true;
    }

    boolean processValueAsString = false;
    boolean processValueAsInt = StringUtil.isInteger(itemValue);

    // Like we don't know field type
    // We try to parse value as an int
    int iValue = -9999;
    float fValue = -9999F;
    if (processValueAsInt) {
      iValue = Integer.parseInt(itemValue);
    } else {
      if (StringUtil.isFloat(itemValue)) {
        fValue = Float.parseFloat(itemValue);
      } else {
        processValueAsString = true;
      }
    }

    if (getValue() != null && getValue().length() == 0) {
      processValueAsInt = false;
      processValueAsString = true;
    }

    if ("=".equals(getOperator())) {
      if (processValueAsString) {
        return itemValue.equalsIgnoreCase(getValue());
      } else if (processValueAsInt) {
        return iValue == getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) == 0;
      }
    } else if ("!=".equals(getOperator())) {
      if (processValueAsString) {
        return !itemValue.equalsIgnoreCase(getValue());
      } else if (processValueAsInt) {
        return iValue != getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) != 0;
      }
    } else if (getOperator().equals(">")) {
      if (processValueAsString) {
        return itemValue.compareTo(getValue()) > 0;
      } else if (processValueAsInt) {
        return iValue > getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) > 0;
      }
    } else if (getOperator().equals(">=")) {
      if (processValueAsString) {
        return itemValue.compareTo(getValue()) >= 0;
      } else if (processValueAsInt) {
        return iValue >= getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) >= 0;
      }
    } else if (getOperator().equals("<")) {
      if (processValueAsString) {
        return itemValue.compareTo(getValue()) < 0;
      } else if (processValueAsInt) {
        return iValue < getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) < 0;
      }
    } else if (getOperator().equals("<=")) {
      if (processValueAsString) {
        return itemValue.compareTo(getValue()) <= 0;
      } else if (processValueAsInt) {
        return iValue <= getValueAsInt();
      } else {
        return Float.compare(fValue, getValueAsFloat()) <= 0;
      }
    } else if (getOperator().equals("contains")) {
      if (processValueAsString) {
        return itemValue.contains(getValue());
      }
      return false;
    }

    return false;
  }

  private float getValueAsFloat() {
    return Float.parseFloat(getValue());
  }

  private int getValueAsInt() {
    return Integer.parseInt(getValue());
  }

  @Override
  public void setItem(String string) {
    item = string;
  }

  @Override
  public void setOperator(String string) {
    operator = string;
  }

  public void setValue(String string) {
    value = string;
  }

  public void setStep(int id) {
    step = id;
  }

  public int getStep() {
    return step;
  }

  public Triggers createTriggers() {
    return new TriggersImpl();
  }

  public Triggers getTriggers() {
    return triggers;
  }

  public void setTriggers(Triggers triggers) {
    this.triggers = triggers;
  }

  /**
   * *********** Implemented methods ****************************************
   */
  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * (non-Javadoc) @see AbstractDescriptor#setId(int)
   */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc) @see AbstractDescriptor#getId()
   */
  @Override
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc) @see
   * AbstractDescriptor#setParent(com.silverpeas
   * .workflow.api.model.AbstractDescriptor)
   */
  @Override
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc) @see AbstractDescriptor#getParent()
   */
  @Override
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc) @see AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }

  /*
   * (non-Javadoc) @see AbstractReferrableObject#getKey()
   */
  @Override
  public String getKey() {
    StringBuilder sb = new StringBuilder();

    if (item != null) {
      sb.append(item);
    }
    sb.append('|');
    if (operator != null) {
      sb.append(operator);
    }
    sb.append('|');
    if (value != null) {
      sb.append(value);
    }
    return sb.toString();
  }

  @Override
  public void addNotifiedUsers(QualifiedUsers notifyUsers) {
    notifiedUsersList.add(notifyUsers);
  }
}