/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core;

/**
 * Interface of all identifiers of resources managed by Silverpeas. An identifier of a resource
 * qualifies uniquely a resource in Silverpeas whatever its type. This interface is the more
 * generic representation of an identifier in Silverpeas from which all conceptual or technical
 * identifiers should extend. It encapsulates the way the identifier is implemented.
 * @author mmoquillon
 */
public interface ResourceIdentifier {

  /**
   * Gets the value of this identifier as a String.
   * @return the String representation of this identifier.
   */
  String asString();
}
