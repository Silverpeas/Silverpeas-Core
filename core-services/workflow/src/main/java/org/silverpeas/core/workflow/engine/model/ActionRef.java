/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;

import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.AllowedAction;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * Class implementing the representation of the &lt;allow&gt; element of a Process Model.
 **/
@XmlAccessorType(XmlAccessType.NONE)
public class ActionRef extends AbstractReferrableObject implements Serializable, AllowedAction {
  private static final long serialVersionUID = -8866828437819862983L;

  @XmlIDREF
  @XmlAttribute(name = "action")
  // The reference to the allowed action
  private ActionImpl action;

  /**
   * Get the referred action
   */
  public Action getAction() {
    return action;
  }

  /**
   * Set the referred action
   * @param action action to refer
   */
  public void setAction(Action action) {
    this.action = (ActionImpl) action;
  }

  public String getActionName() {
    if (action != null) {
      return action.getName();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (getAction() != null) {
      return getAction().getName();
    } else {
      return "";
    }
  }
}