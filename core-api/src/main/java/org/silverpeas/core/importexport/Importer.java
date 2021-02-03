/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport;

import java.util.function.Consumer;

/**
 * An importer of a resource encoded into a format of a given MIME type.
 * <p>
 * All importers in Silverpeas should implement this interface. An importer in Silverpeas is
 * defined for a specific type of resources and it has the responsibility to process the import
 * of such types of resources from a specific format of given MIME type.
 * </p>
 * @param <T> The type of the resource to import.
 */
public interface Importer<T> {

  /**
   * Imports a resource of Silverpeas serialized into a specific format of a given MIME type from
   * either the reader or the input stream provided by the specified descriptor.
   * The result of the import is then passed to the specified consumer.
   * @param descriptor the import descriptor that describes how the import has to be done.
   * @param consumer the consumer that takes the resource that was decoded. It ends the import
   * process by, for example, saving it into Silverpeas.
   * @throws ImportException when an unexpected error occurs while importing the resource.
   */
  void imports(final ImportDescriptor descriptor, Consumer<T> consumer) throws ImportException;

}
