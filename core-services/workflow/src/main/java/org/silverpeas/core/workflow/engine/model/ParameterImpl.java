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

import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;parameter&gt; element of a Process Model.
 **/
public class ParameterImpl extends AbstractReferrableObject implements Parameter, Serializable {
  private static final long serialVersionUID = -4800175503456654951L;
  private String name;
  private String value;

  /**
   * Constructor
   */
  public ParameterImpl() {
    super();
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    name = "";
    value = "";
  }

  /**
   * Get the name of the Parameter
   * @return parameter's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the name of the Parameter
   * @param parameter 's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the value of the Parameter
   * @return parameter's value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Set the value of the Parameter
   * @param parameter 's value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (name == null)
      return "";
    else
      return name;
  }
}