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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.importexport;

import java.util.function.Supplier;

/**
 * An exporter of Silverpeas  resource into a format of a given MIME type.
 * <p>
 * All exporters in Silverpeas should implement this interface. An exporter in Silverpeas is
 * defined for a specific type of resources and it has the responsibility to process the export
 * of such types of resources into a specific format of a given MIME type.
 * </p>
 * @param <T> The type of the resources to export.
 */
public interface Exporter<T> {

  /**
   * Exports a resource of Silverpeas provided by the given supplier according to the export
   * information carried by the specified export descriptor. The  export descriptor supplies the
   * output stream or the writer to use in the export process. Once the export is done
   * (with success or failure), the writer and the output stream is closed.
   * @param descriptor the export descriptor that describes how the export has to be done.
   * @param supplier the supplier that provides what resource to export. It starts the export
   * process by, for example, getting the resource from the Silverpeas data source.
   * @throws ExportException when an unexpected error occurs while exporting the resource.
   */
  void exports(final ExportDescriptor descriptor, final Supplier<T> supplier)
      throws ExportException;

}
