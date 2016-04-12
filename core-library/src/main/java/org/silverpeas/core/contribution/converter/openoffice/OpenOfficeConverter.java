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

package org.silverpeas.core.contribution.converter.openoffice;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.DocumentFormatConversion;
import org.silverpeas.core.contribution.converter.DocumentFormatConversionException;
import org.silverpeas.core.contribution.converter.DocumentFormatException;
import org.silverpeas.core.contribution.converter.option.FilterOption;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

/**
 * A document format converter using the OpenOffice API to perform its task. This class is the
 * common one of all of the API document conversion implementation based on the OpenOffice API.
 */
public abstract class OpenOfficeConverter implements DocumentFormatConversion {

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.converter.openoffice");
  private static final String OPENOFFICE_PORT = "openoffice.port";
  private static final String OPENOFFICE_HOST = "openoffice.host";

  /**
   * Is the specified document in the format on which the converter works?
   * @param document the document to check its format is supported.
   * @return true if the format of the document is supported by this converter, false otherwise.
   */
  public abstract boolean isDocumentSupported(final File document);

  @Override
  public File convert(final File source, final DocumentFormat inFormat,
      final FilterOption... options) {
    final String fileName = FilenameUtils.getBaseName(source.getName()) + '.' + inFormat.name();
    final File destination = new File(FileRepositoryManager.getTemporaryPath() + fileName);
    return convert(source, destination, inFormat, options);
  }

  @Override
  public File convert(final File source, final File destination, final DocumentFormat inFormat,
      final FilterOption... options) {
    if (!isFormatSupported(inFormat)) {
      throw new DocumentFormatException("The conversion of the file to the format " +
          inFormat.toString() + " isn't supported");
    }
    if (!isDocumentSupported(source)) {
      throw new DocumentFormatException("The format of the file " + source.getName() + " isn't " +
          "supported by this converter");
    }
    OpenOfficeConnection connection = null;
    try {
      connection = openConnection();
      convert(getOpenOfficeDocumentConverterFrom(connection), source, destination, options);
    } catch (final Exception e) {
      throw new DocumentFormatConversionException(e.getMessage(), e);
    } finally {
      closeConnection(connection);
    }

    return destination;
  }

  @Override
  public void convert(final InputStream source, final DocumentFormat inFormat,
      final OutputStream destination, final DocumentFormat outFormat,
      final FilterOption... options) {
    if (!isFormatSupported(outFormat)) {
      throw new DocumentFormatException("The conversion of the stream to the format " +
          outFormat.toString() + " isn't supported");
    }
    OpenOfficeConnection connection = null;
    try {
      connection = openConnection();
      convert(getOpenOfficeDocumentConverterFrom(connection), source, inFormat, destination,
          outFormat, options);
    } catch (final Exception e) {
      throw new DocumentFormatConversionException(e.getMessage(), e);
    } finally {
      closeConnection(connection);
    }
  }

  /**
   * Technical converting operations
   * @param documentConverter
   * @param source
   * @param destination
   * @param options
   */
  protected void convert(final SilverpeasOpenOfficeDocumentConverter documentConverter,
      final File source, final File destination, final FilterOption... options) {

    // Options
    applyFilterOptions(documentConverter, options);

    // Conversion
    documentConverter.convert(source, destination);
  }

  /**
   * Technical converting operations
   * @param documentConverter
   * @param source
   * @param inFormat
   * @param destination
   * @param outFormat
   * @param options
   */
  protected void convert(final SilverpeasOpenOfficeDocumentConverter documentConverter,
      final InputStream source, final DocumentFormat inFormat, final OutputStream destination,
      final DocumentFormat outFormat, final FilterOption... options) {

    // Options
    applyFilterOptions(documentConverter, options);

    // Conversion
    documentConverter.convert(source, inFormat, destination, outFormat);
  }

  /**
   * Applying options about the conversion.
   * @param documentConverter
   * @param options
   */
  private void applyFilterOptions(final SilverpeasOpenOfficeDocumentConverter documentConverter,
      final FilterOption... options) {
    if (options != null) {
      for (final FilterOption option : options) {
        documentConverter.addFilterData(option);
      }
    }
  }

  /**
   * Opens a connection to an OpenOffice service. This methods wraps the way the connection(s)
   * life-cycle is managed.
   * @return a connection to an OpenOffice service.
   * @throws ConnectException if no connection can be opened with an OpenOffice service. This can
   * occurs when no OpenOffice service is available for example.
   */
  protected OpenOfficeConnection openConnection() throws ConnectException {
    final String host = settings.getString(OPENOFFICE_HOST, "localhost");
    final int port = settings.getInteger(OPENOFFICE_PORT, 8100);
    final OpenOfficeConnection connection = new SocketOpenOfficeConnection(host, port);
    connection.connect();
    return connection;
  }

  /**
   * Closes the connection to an OpenOffice service. This methods wraps the way the connection(s)
   * life-cycle is managed.
   * @param connection the connection to release.
   */
  protected void closeConnection(final OpenOfficeConnection connection) {
    if (connection != null && connection.isConnected()) {
      connection.disconnect();
    }
  }

  /**
   * Gets a converter from the OpenOffice service at the end-point of the connection. This method
   * wraps the way the OpenOfficeDocumentConverter instances are managed.
   * @param connection the connection an OpenOffice service.
   * @return a converter of documents.
   */
  protected SilverpeasOpenOfficeDocumentConverter getOpenOfficeDocumentConverterFrom(
      final OpenOfficeConnection connection) {
    return new SilverpeasOpenOfficeDocumentConverter(connection);
  }

  private boolean isFormatSupported(final DocumentFormat format) {
    return Arrays.asList(getSupportedFormats()).contains(format);
  }
}
