/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.api.model.Trigger;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * Class implementing the representation of the &lt;trigger&gt; element of a Process Model.
 **/
@XmlRootElement(name = "trigger")
@XmlAccessorType(XmlAccessType.NONE)
public class TriggerImpl extends AbstractReferrableObject implements Trigger, Serializable {
  private static final long serialVersionUID = -5923330362725539310L;
  @XmlAttribute
  private String name;
  @XmlAttribute
  private String handler;
  @XmlElement(name = "param", type = ParameterImpl.class)
  private List<Parameter> parameters;

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
    parameters = new Vector<>();
  }

  /**
   * Get the name of the Parameter
   * @return parameter's name
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Set the name of the Parameter
   * @param name parameter's name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getHandler() {
    return this.handler;
  }

  @Override
  public void setHandler(final String handlerName) {
    this.handler = handlerName;
  }

  @Override
  public String getKey() {
    if (name == null) {
      return "";
    }
    return name;
  }

  @Override
  public Parameter getParameter(String strName) {
    Parameter reference = new ParameterImpl();
    reference.setName(strName);
    int idx = parameters.indexOf(reference);

    if (idx >= 0) {
      return parameters.get(idx);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see Item#addParameter(com.silverpeas.workflow
   * .api.model.Parameter)
   */
  @Override
  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }
}