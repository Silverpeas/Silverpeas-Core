/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core;

/**
 * An object in Silverpeas that is identifiable by a unique identifier encoded as a String. For a
 * better precision in the representation of the identifier, id est when such an identifier is a
 * complex object, then prefer to use the {@link IdentifiableResource} interface.
 * @author mmoquillon
 */
public interface Identifiable {

  /**
   * Gets the unique identifier of the object.
   * @return the identifier encoded as a String. If the identifier is a complex one, that is made up
   * of several identification parts, then the returned representation should take care of such
   * a structure.
   */
  String getId();
}
