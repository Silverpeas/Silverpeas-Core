/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * Interface describing a representation of one of the following elements of a Process Model:
 * <ul>
 * <li>&lt;activity&gt;</li>
 * <li>&lt;description&gt;</li>
 * <li>&lt;label&gt;</li>
 * <li>&lt;title&gt;</li>
 * </ul>
 */
public interface ContextualDesignation extends Serializable {

  /**
   * Get the content of the designation
   * @return a string value
   */
  String getContent();

  /**
   * Set the content of the designation
   * @param strContent new value
   */
  void setContent(String strContent);

  /**
   * Get the role name for this designation
   */
  String getRole();

  /**
   * Set the role name for this designation
   */
  void setRole(String strRole);

  /**
   * Get the language of this designation
   */
  String getLanguage();

  /**
   * Set the language of this designation
   */
  void setLanguage(String strLang);
}
