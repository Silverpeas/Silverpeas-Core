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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.workflow.external.impl.ExternalActionImpl;

import javax.inject.Named;

/**
 * Interface describing a representation of the &lt;trigger&gt; element of a Process Model.
 */
public interface Trigger {
  /**
   * Get the name of the Trigger
   * @return parameter's name
   */
  String getName();

  /**
   * Set the name of the Trigger
   * @param name parameter's name
   */
  void setName(String name);

  /**
   * Get the handler name of the Trigger.
   * @return the handler name filled into {@link Named} annotations of {@link ExternalActionImpl}
   * implementations.
   */
  String getHandler();

  /**
   * Set the handler name of the Trigger.
   * @param handlerName the handler name.
   */
  void setHandler(String handlerName);

  /**
   * Get the parameter specified by name
   * @param strName the parameter name
   * @return the parameters
   */
  Parameter getParameter(String strName);

  /**
   * Add a Parameter to the collection
   */
  void addParameter(Parameter parameter);
}
