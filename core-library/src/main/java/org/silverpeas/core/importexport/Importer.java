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

package org.silverpeas.core.importexport;

import java.io.Serializable;

/**
 * This interface defines the features an immporter of serializable resources in Silverpeas have to
 * satisfy. All importer in Silverpeas should implement this interface. An importer in Silverpeas is
 * defined for a specific type of serializable resources and it has the responsability to know how
 * to import them from a specific or a specified format.
 * @param <T> The type of the serializable resources to import.
 */
public interface Importer<T extends Serializable> {

  /**
   * Imports a serialized resource from either the reader or the input stream and according to the
   * import parameters carried by the specified descriptor. The resource is deserialized in an
   * instance of T.
   * @param descriptor the import descriptor in which information about the import process is
   * indicated.
   * @throws ImportException when an unexpected error occurs while importing the resource.
   * @return an instance of T corresponfding to the imported resource.
   */
  T importFrom(final ImportDescriptor descriptor) throws ImportException;

}
