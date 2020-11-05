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

package org.silverpeas.core.cmis.model;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * A CMIS type representing a type of a resource in Silverpeas. Each of these Silverpeas specific
 * CMIS type is related to a basic CMIS type.
 * @author mmoquillon
 */
public enum TypeId {

  /**
   * The CMIS object represents a Silverpeas space.
   */
  SILVERPEAS_SPACE(BaseTypeId.CMIS_FOLDER, "slvp:space"),

  /**
   * The CMIS object represents a Silverpeas application.
   */
  SILVERPEAS_APPLICATION(BaseTypeId.CMIS_FOLDER, "slvp:application"),

  /**
   * The CMIS object represents a folder of user contributions in a Silverpeas application.
   */
  SILVERPEAS_FOLDER(BaseTypeId.CMIS_FOLDER, "slvp:folder"),

  /**
   * The CMIS object represents a publication in a Silverpeas application.
   */
  SILVERPEAS_PUBLICATION(BaseTypeId.CMIS_FOLDER, "slvp:publication"),

  /**
   * The CMIS object represents a document in a Silverpeas application.
   */
  SILVERPEAS_DOCUMENT(BaseTypeId.CMIS_DOCUMENT, "slvp:document");

  private final String value;
  private final BaseTypeId baseTypeId;

  TypeId(final BaseTypeId baseTypeId, final String value) {
    this.baseTypeId = baseTypeId;
    this.value = value;
  }

  /**
   * Gets the qualified name of this type in CMIS.
   * @return a String representation of this type in CMIS.
   */
  public String value() {
    return value;
  }

  /**
   * Gets the base CMIS type from which this type is derived.
   * @return a {@link BaseTypeId} enumeration value defining the base CMIS type of this type.
   */
  public BaseTypeId getBaseTypeId() {
    return baseTypeId;
  }

  /**
   * Gets the instance of the {@link TypeId} enumeration from the specified qualified name of a
   * Silverpeas custom type in CMIS. If the given name isn't known, then an
   * {@link IllegalArgumentException} is thrown.
   * @param v a qualified name of a {@link TypeId} enumeration value.
   * @return the corresponding {@link TypeId} instance.
   */
  public static TypeId fromValue(String v) {
    for (TypeId c : TypeId.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}
