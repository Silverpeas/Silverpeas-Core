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
import java.util.Vector;

import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.api.model.Trigger;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;trigger&gt; element of a Process Model.
 **/
public class TriggerImpl extends AbstractReferrableObject implements Trigger, Serializable {
  private static final long serialVersionUID = -5923330362725539310L;
  private String name;
  private String className;
  private Vector<Parameter> parameters;

  /**
   * Constructor
   */
  public TriggerImpl() {
    super();
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    name = "";
    className = "";
    parameters = new Vector<>();
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
  public String getClassName() {
    return this.className;
  }

  /**
   * Set the value of the Parameter
   * @param parameter 's value
   */
  public void setClassName(String className) {
    this.className = className;
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

  public Parameter getParameter(String strName) {
    Parameter reference = new ParameterImpl();
    int idx;

    reference.setName(strName);
    idx = parameters.indexOf(reference);

    if (idx >= 0)
      return parameters.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * @see Item#createParameter()
   */
  public Parameter createParameter() {
    return new ParameterImpl();
  }

  /*
   * (non-Javadoc)
   * @see Item#addParameter(com.silverpeas.workflow
   * .api.model.Parameter)
   */
  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  /*
   * (non-Javadoc)
   * @see Item#iterateParameters()
   */
  public Iterator<Parameter> iterateParameter() {
    return parameters.iterator();
  }
}