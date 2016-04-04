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

package org.silverpeas.core.contribution.converter;

import org.silverpeas.core.contribution.converter.option.FilterOption;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface defines the ability to convert a document in a given format into a specified
 * another format. An object with a such property should implement this interface. A converter
 * takes
 * into account a specific format of documents and provides the capability to convert it into
 * another format. It can support only a subset of available conversions in Silverpeas.
 */
public interface DocumentFormatConversion {

  /**
   * Converts the specified document in the specified format. The format should be supported by the
   * converter. If an error occurs while converting the specified file, then a runtime exception
   * DocumentFormatConversionException is thrown.
   * @param source the document to convert.
   * @param inFormat the format into which the document has to be converted.
   * @param options additional options such as "PageRange"
   * @return the file with the converted document.
   */
  File convert(File source, DocumentFormat inFormat, FilterOption... options);

  /**
   * Converts the specified document in the specified format. The format should be supported by the
   * converter. If an error occurs while converting the specified file, then a runtime exception
   * DocumentFormatConversionException is thrown.
   * @param source the document to convert.
   * @param destination the converted document.
   * @param inFormat the format into which the document has to be converted.
   * @return the destination file.
   */
  File convert(File source, File destination, DocumentFormat inFormat, FilterOption... options);

  /**
   * Converts the specified inputstream/format in the specified outputstream/format.
   * @param source the source stream to convert.
   * @param inFormat the format from which the document has to be converted.
   * @param destination the converted stream.
   * @param outFormat the format into which the document has to be converted.
   */
  void convert(InputStream source, DocumentFormat inFormat, OutputStream destination,
      DocumentFormat outFormat, FilterOption... options);


  /**
   * Gets the formats of documents supported by the converter.
   * @return an array with the different formats into which the object implementing this interface
   *         can convert a document.
   */
  DocumentFormat[] getSupportedFormats();
}
