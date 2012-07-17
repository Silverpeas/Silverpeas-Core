/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.converter.openoffice;

import com.silverpeas.converter.DocumentFormatException;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.converter.DocumentFormatConversion;
import com.silverpeas.converter.DocumentFormatConversionException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.net.ConnectException;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;

/**
 * A document format converter using the OpenOffice API to perform its task. This class is the
 * common one of all of the API document conversion implementation based on the OpenOffice API.
 */
public abstract class OpenOfficeConverter implements DocumentFormatConversion {

  private static final ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.converter.openoffice", "");
  private static final String OPENOFFICE_PORT = "openoffice.port";
  private static final String OPENOFFICE_HOST = "openoffice.host";

  @Override
  public abstract DocumentFormat[] getSupportedFormats();

  /**
   * Is the specified document in the format on which the converter works?
   * @param document the document to check its format is supported.
   * @return true if the format of the document is supported by this converter, false otherwise.
   */
  public abstract boolean isDocumentSupported(final File document);

  @Override
  public File convert(final File source, final DocumentFormat format) {
    if (!isFormatSupported(format)) {
      throw new DocumentFormatException("The conversion of the file to the format " + format.
          toString() + " isn't supported");
    }
    if (!isDocumentSupported(source)) {
      throw new DocumentFormatException("The format of the file " + source.getName() + " isn't "
          + "supported by this converter");
    }
    String fileName = FilenameUtils.getBaseName(source.getName()) + "." + format.name();
    File destination = new File(FileRepositoryManager.getTemporaryPath() + fileName);
    OpenOfficeConnection connection = null;
    try {
      connection = openConnection();
      DocumentConverter converter = getOpenOfficeDocumentConverterFrom(connection);
      converter.convert(source, destination);
    } catch (Exception e) {
      throw new DocumentFormatConversionException(e.getMessage(), e);
    } finally {
      closeConnection(connection);
    }

    return destination;
  }

  /**
   * Opens a connection to an OpenOffice service. This methods wraps the way the connection(s)
   * life-cycle is managed.
   * @return a connection to an OpenOffice service.
   * @throws ConnectException if no connection can be opened with an OpenOffice service. This can
   * occurs when no OpenOffice service is available for example.
   */
  protected OpenOfficeConnection openConnection() throws ConnectException {
    String host = settings.getString(OPENOFFICE_HOST, "localhost");
    int port = settings.getInteger(OPENOFFICE_PORT, 8100);
    OpenOfficeConnection connection = new SocketOpenOfficeConnection(host, port);
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
  protected OpenOfficeDocumentConverter getOpenOfficeDocumentConverterFrom(
      final OpenOfficeConnection connection) {
    return new OpenOfficeDocumentConverter(connection);
  }

  private boolean isFormatSupported(final DocumentFormat format) {
    return Arrays.asList(getSupportedFormats()).contains(format);
  }
}
