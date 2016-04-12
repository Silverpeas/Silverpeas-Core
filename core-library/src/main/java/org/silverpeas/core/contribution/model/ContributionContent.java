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

package org.silverpeas.core.contribution.model;

import java.io.Serializable;

/**
 * The content of a contribution in Silverpeas. It is the more generic representation of a content
 * embedded into a user contribution. All conceptual representation of a content supported in
 * Silverpeas should implement this interface.
 * @param <T> the type the content's data.
 * @author mmoquillon
 */
public interface ContributionContent<T> extends Serializable {

  /**
   * Gets the data of a content. A data can be a text, a structure, a binary stream, and so on. The
   * type of the data should be represented by a Java type.
   * @return the data of the content.
   */
  T getData();

  /**
   * Does the content is empty?
   * @return true if this content doesn't contain any data, false otherwise.
   */
  default boolean isEmpty() {
    return getData() != null;
  }
}
