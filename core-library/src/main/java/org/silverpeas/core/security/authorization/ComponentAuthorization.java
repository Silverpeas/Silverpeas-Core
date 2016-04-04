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

package org.silverpeas.core.security.authorization;

/**
 * Component security provides a way to check a user have enough rights to access a given object in
 * a Silverpeas component instance. Each Silverpeas component should implements this interface
 * according to the objects or resources it manages.
 */
public interface ComponentAuthorization {

  /**
   * Check if a user is authorized to access to a given object in a defined component.
   * @param componentId - id of the component
   * @param userId - id of the user
   * @param objectId - id of the object
   * @return true if user is authorized to access to component's object, false otherwise.
   */
  boolean isAccessAuthorized(String componentId, String userId,
      String objectId);

  /**
   * Check if a user is authorized to access to a given object in a defined component. Usefull if
   * component uses several objects.
   * @param componentId - id of the component
   * @param userId - id of the user
   * @param objectId - id of the object
   * @param objectType - type of the object (ex : PublicationDetail, NodeDetail...)
   * @return true if user is authorized to access to component's object, false otherwise.
   */
  boolean isAccessAuthorized(String componentId, String userId,
      String objectId, String objectType);

  /**
   * Check if a user is authorized to access to a given object in a defined component. Usefull if
   * component uses several objects.
   * @param componentId - id of the component
   * @param userId - id of the user
   * @param objectId - id of the object
   * @param objectType - type of the object (ex : PublicationDetail, NodeDetail...)
   * @return true if user is authorized to access to component's object, false otherwise.
   */
  boolean isObjectAvailable(String componentId, String userId,
      String objectId, String objectType);

  void enableCache();

  void disableCache();

}