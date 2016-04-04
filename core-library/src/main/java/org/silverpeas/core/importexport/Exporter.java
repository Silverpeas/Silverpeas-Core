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
 * This interface defines the features an exporter of serializable resources in Silverpeas have to
 * satisfy. All exporter in Silverpeas should implement this interface. An exporter in Silverpeas is
 * defined for a specific type of serializable resources and it has the responsability to know how
 * to export them into a specific or a specified format.
 * @param <T> The type of the serializable resources to export.
 */
public interface Exporter<T extends Serializable> {

  /**
   * Exports the specified serializable resource according to the export information carried by the
   * specified export descriptor. The serializable resource is exported by using either the writer
   * or the output stream provided by the descriptor. According to the kind of the writer or of the
   * output stream, the way the resource is actually exported can be customized (export in a file,
   * in a string, through a web service, ...). Once the export is done (with success or failure),
   * the writer and the output stream is closed.
   * @param descriptor the export descriptor in which information about the export process is
   * indicated.
   * @param serializable the serializable resource to export.
   * @throws ExportException when an unexpected error occurs while exporting the resource.
   */
  void export(final ExportDescriptor descriptor, final T serializable) throws ExportException;

}
