/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * A service providing the transverse operations related to a given Silverpeas component. A
 * Silverpeas component aims to manage a given type of content and it is based upon some settings
 * and i18n messages. Some capabilities of a Silverpeas component are provided by a service that is
 * a bean with business logic transverse methods; the core business logic of the component should
 * be provided by the resources themselves (see the DDD (Domain Driven Development) approach).
 */
public interface SilverpeasComponentService<T extends SilverpeasContent> {

  /**
   * Gets the content handled by an instance of the component with the specified unique identifier.
   * @param <T> the concrete type of the content.
   * @param contentId the unique identifier of the content to get.
   * @return a Silverpeas content.
   */
  T getContentById(String contentId);

  /**
   * Gets the settings of this Silverpeas component.
   * @return a ResourceLocator instance giving access the settings.
   */
  ResourceLocator getComponentSettings();

  /**
   * Gets the localized messages defined in this Silverpeas component.
   * @param language the language in which the messages has to be localized. If empty or null, then
   * the bundle with default messages is returned.
   * @return a ResourceLocator instance giving access the localized messages.
   */
  ResourceLocator getComponentMessages(String language);

}
