/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.ui.DisplayI18NHelper;

import java.io.Serializable;

/**
 * @author Yohann Chastagnier
 */
public interface SilverpeasComponentInstance extends Serializable {

  /**
   * Gets the unique identifier of the component instance.
   * @return a unique identifier as string.
   */
  String getId();

  /**
   * Gets the component name of the component instance.
   * @return the name of the component instance.
   */
  String getName();

  /**
   * Gets the name of the component (from a functional point of view).
   * @return the name of the component.
   */
  String getLabel();

  /**
   * Gets the translated name of the component according to given language (from a functional point
   * of view).<br/>
   * If no translation exists for given language, then the one of {@link
   * DisplayI18NHelper#defaultLanguage} is returned.
   * @return the translated name of the component.
   */
  String getLabel(String language);

  /**
   * Gets the description of the component (from a functional point of view).
   * @return the description of the component.
   */
  String getDescription();

  /**
   * Gets the translated description of the component according to given language (from a functional
   * point of view).<br/>
   * If no translation exists for given language, then the one of {@link
   * DisplayI18NHelper#defaultLanguage} is returned.
   * @return the translated description of the component.
   */
  String getDescription(String language);

  /**
   * Indicates if the component instance is a personal one.<br/>
   * A personal component instance is linked to a user.
   * @return true if it is a personal one, false otherwise.
   */
  default boolean isPersonal() {
    return false;
  }
}
