/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import java.io.Serializable;

/**
 * Interface describing a representation of the &lt;parameter&gt; element of a Process Model.
 */
public interface Parameter extends Serializable {
  /**
   * Get the name of the Parameter
   * @return parameter's name
   */
  String getName();

  /**
   * Set the name of the Parameter
   * @param name parameter's name
   */
  void setName(String name);

  /**
   * Get the value of the Parameter
   * @return parameter's value
   */
  String getValue();

  /**
   * Set the value of the Parameter
   * @param value parameter's value
   */
  void setValue(String value);
}
