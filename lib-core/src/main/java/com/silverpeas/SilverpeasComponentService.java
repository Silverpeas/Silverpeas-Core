/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
 * A service specific to a Silverpeas component.
 * It defines the common methods a service of a Silverpeas component must provide.
 */
public interface SilverpeasComponentService {
  
  /**
   * Gets the content handled by an instance of the component with the specified unique identifier.
   * @param <T> the concrete type of the content.
   * @param contentId the unique identifier of the content to get.
   * @return a Silverpeas content.
   */
  <T extends SilverpeasContent> T getContent(String contentId);
  
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
