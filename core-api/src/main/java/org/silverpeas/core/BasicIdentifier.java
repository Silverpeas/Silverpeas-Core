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

import java.util.Objects;

/**
 * A basic implementation of an identifier of a resource in Silverpeas. In this implementation, the
 * identifier of a resource can be made up of two parts: a global identifier that can be get with
 * the {@link #asString()} method and a local identifier that identifies the resource uniquely only
 * among others resources of the same type.
 * @author mmoquillon
 */
public class BasicIdentifier implements ResourceIdentifier{

  private static final int NO_LOCAL_ID = -1;

  private final int localId;
  private final String globalId;

  /**
   * Constructs a basic identifier with only the specified String representation of a unique
   * identifier.
   * @param uniqueId {@link String} representation of an identifier.
   */
  public BasicIdentifier(final String uniqueId) {
    this(NO_LOCAL_ID, uniqueId);
  }

  /**
   * Constructs a basic identifier with both a local identifier and a String representation of a
   * unique identifier.
   * @param localId an identifier local to the type of the identified resource.
   * @param globalId {@link String} representation of an identifier.
   */
  public BasicIdentifier(final int localId, final String globalId) {
    this.localId = localId;
    this.globalId = globalId;
  }

  /**
   * Gets the local identification value of this identifier if any.
   * @return an integer representing a local identification. -1 if no local identification is
   * carried by this identifier.
   */
  public int asLocalId() {
    return this.localId;
  }

  @Override
  public String asString() {
    return globalId;
  }

  /**
   * Is the local identification value carried by this identifier?
   * @return true if a local identification is defined for this identifier, false otherwise.
   */
  public boolean isLocalIdDefined() {
    return this.localId != NO_LOCAL_ID;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(asString());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BasicIdentifier) {
      return ((BasicIdentifier) obj).asString().equals(asString());
    }
    return false;
  }
}
  