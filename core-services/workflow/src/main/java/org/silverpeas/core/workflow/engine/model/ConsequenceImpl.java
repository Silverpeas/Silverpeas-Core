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
import org.silverpeas.kernel.util.StringUtil;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Class implementing the representation of the &lt;consequence&gt; element of a Process Model.
 */
@XmlRootElement(name = "consequence")
@XmlAccessorType(XmlAccessType.NONE)
public class ConsequenceImpl implements Consequence {

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

  @Override
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

  @Override
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

  @Override
  public void addTargetState(StateSetter stateSetter) {
    targetStateList.add(stateSetter);
  }

  @Override
  public State getUnsetState(String strStateName) {
    return getState(strStateName, unsetStateList);
  }

  @Override
  public State[] getUnsetStates() {
    return getStates(unsetStateList);
  }

  @Override
  public void addUnsetState(StateSetter stateSetter) {
    unsetStateList.add(stateSetter);
  }

  @Override
  public boolean getKill() {
    return kill;
  }

  @Override
  public void setKill(boolean kill) {
    this.kill = kill;
  }

  @Override
  public List<QualifiedUsers> getNotifiedUsers() {
    return this.notifiedUsersList;
  }

  @Override
  public void setNotifiedUsers(List<QualifiedUsers> notifiedUsersList) {
    this.notifiedUsersList = notifiedUsersList;
  }

  @Override
  public String getItem() {
    return item;
  }

  @Override
  public String getOperator() {
    return operator;
  }

  @Override
  public String getValue() {
    return value;
  }

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

    if (getValue() != null && getValue().isEmpty()) {
      processValueAsInt = false;
      processValueAsString = true;
    }

    return processOperator(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
  }

  private boolean processOperator(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if ("=".equals(getOperator())) {
      return processEquals(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if ("!=".equals(getOperator())) {
      return processInequals(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if (getOperator().equals(">")) {
      return processGreater(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if (getOperator().equals(">=")) {
      return processGreaterOrEquals(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if (getOperator().equals("<")) {
      return processLesser(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if (getOperator().equals("<=")) {
      return processLesserOrEquals(itemValue, processValueAsString, processValueAsInt, iValue, fValue);
    } else if (getOperator().equals("contains")) {
      return processContains(itemValue, processValueAsString);
    }

    return false;
  }

  private boolean processContains(String itemValue, boolean processValueAsString) {
    if (processValueAsString) {
      return itemValue.contains(getValue());
    }
    return false;
  }

  private boolean processLesserOrEquals(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return itemValue.compareTo(getValue()) <= 0;
    } else if (processValueAsInt) {
      return iValue <= getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) <= 0;
    }
  }

  private boolean processLesser(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return itemValue.compareTo(getValue()) < 0;
    } else if (processValueAsInt) {
      return iValue < getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) < 0;
    }
  }

  private boolean processGreaterOrEquals(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return itemValue.compareTo(getValue()) >= 0;
    } else if (processValueAsInt) {
      return iValue >= getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) >= 0;
    }
  }

  private boolean processGreater(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return itemValue.compareTo(getValue()) > 0;
    } else if (processValueAsInt) {
      return iValue > getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) > 0;
    }
  }

  private boolean processInequals(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return !itemValue.equalsIgnoreCase(getValue());
    } else if (processValueAsInt) {
      return iValue != getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) != 0;
    }
  }

  private boolean processEquals(String itemValue, boolean processValueAsString, boolean processValueAsInt, int iValue, float fValue) {
    if (processValueAsString) {
      return itemValue.equalsIgnoreCase(getValue());
    } else if (processValueAsInt) {
      return iValue == getValueAsInt();
    } else {
      return Float.compare(fValue, getValueAsFloat()) == 0;
    }
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

  @Override
  public void setValue(String string) {
    value = string;
  }

  @Override
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