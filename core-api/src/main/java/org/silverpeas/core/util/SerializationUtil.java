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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.util;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Base64;

/**
 * @author Yohann Chastagnier
 * @see SerializationUtils
 */
public class SerializationUtil extends SerializationUtils {


  /**
   * <p>Serializes an {@code Object} to a string forstorage/serialization.</p>
   * @param obj the object to serialize to bytes
   * @return a string with the converted Serializable
   * @throws SerializationException (runtime) if the serialization fails
   */
  public static String serializeAsString(Serializable obj) {
    return Base64.getEncoder().encodeToString(serialize(obj));
  }


  /**
   * <p>Deserializes a single {@code Object} from a string.</p>
   * @param objectStringData the serialized object, must not be null
   * @return the deserialized object
   * @throws IllegalArgumentException if {@code objectData} is {@code null}
   * @throws SerializationException (runtime) if the serialization fails
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deserializeFromString(String objectStringData) {
    if (objectStringData == null) {
      throw new IllegalArgumentException("data must exist");
    }
    return (T) SerializationUtils.deserialize(Base64.getDecoder().decode(objectStringData));
  }
}
