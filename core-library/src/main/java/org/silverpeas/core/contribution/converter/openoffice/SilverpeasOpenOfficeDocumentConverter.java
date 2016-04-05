/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeException;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import org.silverpeas.core.contribution.converter.option.FilterOption;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.MimeTypes.RTF_MIME_TYPE;

/**
 * This Object is kind of patch of JODConverter tools in the aim to add some conversion options.
 * @author Yohann Chastagnier
 */
public class SilverpeasOpenOfficeDocumentConverter extends OpenOfficeDocumentConverter {

  private static final Logger logger =
      LoggerFactory.getLogger(SilverpeasOpenOfficeDocumentConverter.class);

  /**
   * Optional filter data option
   */
  private final List<FilterOption> filterData = new ArrayList<FilterOption>();

  /**
   * Default constructor
   * @param connection
   */
  public SilverpeasOpenOfficeDocumentConverter(final OpenOfficeConnection connection) {
    super(connection, new ExtendedDocumentFormatRegistry());
  }

  /**
   * Adds a filter
   * @param option
   */
  public void addFilterData(final FilterOption option) {
    filterData.add(option);
  }

  /**
   * Same function as that of OpenOfficeDocumentConverter completed with FilterData processing.
   */
  @Override
  protected void convertInternal(final File inputFile, final DocumentFormat inputFormat,
      final File outputFile, final DocumentFormat outputFormat) {
    final Map<String, Object> loadProperties = new HashMap<String, Object>();
    loadProperties.putAll(getDefaultLoadProperties());
    loadProperties.putAll(inputFormat.getImportOptions());

    final Map<String, Object> storeProperties =
        outputFormat.getExportOptions(inputFormat.getFamily());

    // Filter Data options
    if (!filterData.isEmpty()) {
      final List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
      for (final FilterOption option : filterData) {
        propertyValues.add(property(option.getName(), option.getValue()));
      }

      loadProperties.put("FilterData", propertyValues.toArray(new PropertyValue[]{}));
      storeProperties.put("FilterData", propertyValues.toArray(new PropertyValue[]{}));
    }

    synchronized (openOfficeConnection) {
      final XFileIdentifierConverter fileContentProvider =
          openOfficeConnection.getFileContentProvider();
      final String inputUrl =
          fileContentProvider.getFileURLFromSystemPath("", inputFile.getAbsolutePath());
      final String outputUrl =
          fileContentProvider.getFileURLFromSystemPath("", outputFile.getAbsolutePath());

      loadAndExport(inputUrl, loadProperties, outputUrl, storeProperties);
    }
  }

  /**
   * Same function as that of OpenOfficeDocumentConverter
   */
  private void loadAndExport(final String inputUrl, final Map<String, Object> loadProperties,
      final String outputUrl, final Map<String, Object> storeProperties)
      throws OpenOfficeException {
    XComponent document;
    try {
      document = loadDocument(inputUrl, loadProperties);
    } catch (final ErrorCodeIOException errorCodeIOException) {
      throw new OpenOfficeException(
          "conversion failed: could not load input document; OOo errorCode: " +
              errorCodeIOException.ErrCode, errorCodeIOException);
    } catch (final Exception otherException) {
      throw new OpenOfficeException("conversion failed: could not load input document",
          otherException);
    }
    if (document == null) {
      throw new OpenOfficeException("conversion failed: could not load input document");
    }

    refreshDocument(document);

    try {
      storeDocument(document, outputUrl, storeProperties);
    } catch (final ErrorCodeIOException errorCodeIOException) {
      throw new OpenOfficeException(
          "conversion failed: could not save output document; OOo errorCode: " +
              errorCodeIOException.ErrCode, errorCodeIOException);
    } catch (final Exception otherException) {
      throw new OpenOfficeException("conversion failed: could not save output document",
          otherException);
    }
  }

  /**
   * Same function as that of OpenOfficeDocumentConverter
   */
  private XComponent loadDocument(final String inputUrl, final Map<String, Object> loadProperties)
      throws com.sun.star.io.IOException, IllegalArgumentException {
    final XComponentLoader desktop = openOfficeConnection.getDesktop();
    return desktop.loadComponentFromURL(inputUrl, "_blank", 0, toPropertyValues(loadProperties));
  }

  /**
   * Same function as that of OpenOfficeDocumentConverter
   */
  private void storeDocument(final XComponent document, final String outputUrl,
      final Map<String, Object> storeProperties) throws com.sun.star.io.IOException {
    try {
      final XStorable storable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);
      storable.storeToURL(outputUrl, toPropertyValues(storeProperties));
    } finally {
      final XCloseable closeable =
          (XCloseable) UnoRuntime.queryInterface(XCloseable.class, document);
      if (closeable != null) {
        try {
          closeable.close(true);
        } catch (final CloseVetoException closeVetoException) {
          logger.warn("document.close() vetoed");
        }
      } else {
        document.dispose();
      }
    }
  }

  /**
   * Conversion from streams.
   * @param source
   * @param inFormat
   * @param destination
   * @param outFormat
   * @param options
   */
  public void convert(final InputStream source,
      final org.silverpeas.core.contribution.converter.DocumentFormat inFormat, final OutputStream destination,
      final org.silverpeas.core.contribution.converter.DocumentFormat outFormat, final FilterOption... options) {
    convert(source, getDocumentFormat(inFormat), destination, getDocumentFormat(outFormat));
  }

  /**
   * Gets the JODConverter document format from a Silverpeas document format.
   * @param documentFormat
   * @return
   */
  private com.artofsolving.jodconverter.DocumentFormat getDocumentFormat(
      org.silverpeas.core.contribution.converter.DocumentFormat documentFormat) {
    String mimeType = documentFormat.getMimeType();
    com.artofsolving.jodconverter.DocumentFormat format =
        getDocumentFormatRegistry().getFormatByMimeType(mimeType);
    if (format == null) {
      if (RTF_MIME_TYPE.equals(mimeType)) {
        mimeType = mimeType.replaceFirst("application", "text");
      }
      format = getDocumentFormatRegistry().getFormatByMimeType(mimeType);
    }
    if (format == null) {
      throw new java.lang.IllegalArgumentException(
          "unknown document format for MIME-TYPE: " + documentFormat.getMimeType());
    }
    return format;
  }
}
