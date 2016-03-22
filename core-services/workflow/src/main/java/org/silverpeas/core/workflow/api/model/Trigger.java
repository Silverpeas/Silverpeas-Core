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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;trigger&gt; element of a Process Model.
 */
public interface Trigger {
  /**
   * Get the name of the Trigger
   * @return parameter's name
   */
  public String getName();

  /**
   * Set the name of the Trigger
   * @param parameter 's name
   */
  public void setName(String name);

  /**
   * Get the className of the Trigger
   * @return className
   */
  public String getClassName();

  /**
   * Set the className of the Trigger
   * @param className
   */
  public void setClassName(String className);

  /**
   * Get the parameter specified by name
   * @param strName the parameter name
   * @return the parameters
   */
  public Parameter getParameter(String strName);

  /**
   * Create an object implementing Parameter
   */
  public Parameter createParameter();

  /**
   * Add a Parameter to the collection
   */
  public void addParameter(Parameter parameter);

  /**
   * Return an Iterator over the parameters collection
   */
  public Iterator<Parameter> iterateParameter();
}
