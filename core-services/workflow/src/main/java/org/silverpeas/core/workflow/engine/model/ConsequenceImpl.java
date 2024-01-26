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

import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.workflow.api.model.Consequence;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.StateSetter;
import org.silverpeas.core.workflow.api.model.Triggers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Class implementing the representation of the &lt;consequence&gt; element of a Process Model.
 */
@XmlRootElement(name = "consequence")
@XmlAccessorType(XmlAccessType.NONE)
public class ConsequenceImpl implements Consequence, Serializable {

  private static final long serialVersionUID = -905677587105320693L;
  @XmlAttribute
  private String item;
  @XmlAttribute
  private String operator;
  @XmlAttribute
  private String value;
  @XmlElement
  private boolean kill;
  @XmlElement(name = "set", type = StateRef.class)
  private List<StateSetter> targetStateList;
  @XmlElement(name = "unset", type = StateRef.class)
  private List<StateSetter> unsetStateList;
  @XmlElement(name = "notify", type = QualifiedUsersImpl.class)
  private List<QualifiedUsers> notifiedUsersList;
  @XmlElement(type = TriggersImpl.class)
  private Triggers triggers;

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
    return getState(strStateName, targetStateList);
  }

  private State getState(String stateName, List<StateSetter> states) {
    for (StateSetter state : states) {
      if (state.getState().getName().equals(stateName)) {
        return state.getState();
      }
    }
    return null;
  }

  /**
   * Get the target states
   * @return the target states as a Vector
   */
  public State[] getTargetStates() {
    return getStates(targetStateList);
  }

  private State[] getStates(List<StateSetter> stateSetters) {
    if (stateSetters == null) {
      return new State[0];
    }

    State[] states = new StateImpl[stateSetters.size()];
    for (int i = 0; i < stateSetters.size(); i++) {
      StateRef ref = (StateRef) stateSetters.get(i);
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
   * (non-Javadoc) @see Consequence#getUnsetState(java.lang.String
   * )
   */
  public State getUnsetState(String strStateName) {
    return getState(strStateName, unsetStateList);
  }

  /**
   * Get the states to unset
   * @return the states to unset as a Vector
   */
  public State[] getUnsetStates() {
    return getStates(unsetStateList);
  }

  /*
   * (non-Javadoc) @see Consequence#addUnsetState(com.silverpeas
   * .workflow.api.model.StateSetter)
   */
  public void addUnsetState(StateSetter stateSetter) {
    unsetStateList.add(stateSetter);
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
   * @param notifiedUsersList object containing notified users
   */
  public void setNotifiedUsers(List<QualifiedUsers> notifiedUsersList) {
    this.notifiedUsersList = notifiedUsersList;
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

  public Triggers getTriggers() {
    return triggers;
  }

  public void setTriggers(Triggers triggers) {
    this.triggers = triggers;
  }

  @Override
  public void addNotifiedUsers(QualifiedUsers notifyUsers) {
    notifiedUsersList.add(notifyUsers);
  }
}