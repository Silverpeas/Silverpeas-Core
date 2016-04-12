/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

/**
 * This interface extends access controller interface for a Component resource.
 * @author Yohann Chastagnier
 */
public interface ComponentAccessControl extends AccessController<String> {

  /**
   * Indicates is the rights are set on node as well as the component.
   * @param instanceId the identifier of the component instance.
   * @return true if rights are enabled at node level, false otherwise.
   */
  boolean isRightOnTopicsEnabled(String instanceId);

  /**
   * Indicates if publication sharing is enabled on the component instance represented by the given
   * identifier.
   * @param instanceId the identifier of the component instance.
   * @return true if file sharing is enabled, false otherwise.
   */
  boolean isPublicationSharingEnabled(String instanceId);

  /**
   * Indicates that the rights are set on node as well as the component.
   * @param instanceId the identifier of the component instance.
   * @return true if co-writing is enabled, false otherwise.
   */
  boolean isCoWritingEnabled(String instanceId);

  /**
   * Indicates if file sharing is enabled on the component instance represented by the given
   * identifier.
   * @param instanceId the identifier of the component instance.
   * @return true if file sharing is enabled, false otherwise.
   */
  boolean isFileSharingEnabled(String instanceId);

  /**
   *  Indicates if the specified component instance satisfy the topic tracking behavior.
   * @param instanceId the identifier of the component instance.
   * @return true if the topic tracking is supported, false otherwise.
   */
  boolean isTopicTrackerSupported(String instanceId);
}
