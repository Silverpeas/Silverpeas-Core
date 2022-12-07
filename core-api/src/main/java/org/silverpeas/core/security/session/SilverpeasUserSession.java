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

package org.silverpeas.core.security.session;

import org.silverpeas.core.admin.user.model.User;

/**
 * Represents a session opened by a {@link org.silverpeas.core.admin.user.model.User} in Silverpeas.
 * It is a Silverpeas user session, distinct to any technical sessions.
 * @author silveryocha
 */
public interface SilverpeasUserSession {

  /**
   * Gets the unique identifier of the session.
   * @return the unique identifier as string.
   */
  String getId();

  /**
   * Gets the user which as opened to session.
   * @return a {@link User} instance.
   */
  User getUser();

  /**
   * Sets an attribute named by the specified name with the specified value. If no attributes exists
   * with the specified name, then it is added to the session. The attributes are a way for the
   * applications to put in the user session some peculiar and required information.
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to set.
   * @param value the value of the attribute to set.
   */
  <T> void setAttribute(String name, T value);

  /**
   * Gets the value of the attribute named by the specified name. The attributes are a way for the
   * applications to put in the user session some peculiar and required information.
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to get.
   * @return the value of the attribute or null if no such attribute exists.
   */
  <T> T getAttribute(String name);

  /**
   * Unsets the specified attribute. The consequence of an unset is the attribute is then removed
   * from the session and cannot be retrieved later.
   * @param name the name of the attribute to unset.
   */
  void unsetAttribute(String name);
}
