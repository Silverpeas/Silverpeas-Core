/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

/**
 * Defines the context under which a user in Silverpeas accesses the JCR. Each context carries its
 * own security policy that will be used by the access manager of the JCR to control access rights
 * on the nodes and properties of the JCR. The access context is a way for Silverpeas to customize
 * the permissions checking. The access context of the user is invoked only when the access isn't
 * granted by the default access rights control.
 * @author mmoquillon
 */
public interface AccessContext {

  /**
   * Defines a no peculiar context a user has while accessing the JCR. In this case, no peculiar
   * access control is performed: it refuses by default all permissions to access an item at a given
   * path in the JCR.
   */
  AccessContext EMPTY = (jcrPath, permissions) -> false;

  boolean isGranted(String jcrPath, long permissions);
}
