/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.converter.openoffice;

import org.apache.commons.io.FilenameUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.DocumentFormatConversion;
import org.silverpeas.core.contribution.converter.DocumentFormatConversionException;
import org.silverpeas.core.contribution.converter.DocumentFormatException;
import org.silverpeas.core.contribution.converter.option.FilterOption;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

import static org.silverpeas.core.util.MimeTypes.RTF_MIME_TYPE;

/**
 * A document format converter using the OpenOffice API to perform its task. This class is the
 * common one of all of the API document conversion implementation based on the OpenOffice API.
 */
public abstract class OpenOfficeConverter implements DocumentFormatConversion {

  @Inject
  private OpenOfficeService service;

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
  public File convert(final File source, final File destination, final DocumentFormat outFormat,
      final FilterOption... options) {
    if (isNotFormatSupported(outFormat)) {
      throw new DocumentFormatException("The conversion of the file to the format " +
          outFormat.toString() + " isn't supported");
    }
    if (!isDocumentSupported(source)) {
      throw new DocumentFormatException("The format of the file " + source.getName() + " isn't " +
          "supported by this converter");
    }
    try {
      DocumentConverter converter = getDocumentConverter(options);
      converter.convert(source).to(destination).as(documentFormat(outFormat)).execute();
    } catch (final Exception e) {
      throw new DocumentFormatConversionException(e.getMessage(), e);
    }
    return destination;
  }

  @Override
  public void convert(final InputStream source, final DocumentFormat inFormat,
      final OutputStream destination, final DocumentFormat outFormat,
      final FilterOption... options) {
    if (isNotFormatSupported(outFormat)) {
      throw new DocumentFormatException("The conversion of the stream to the format " +
          outFormat.toString() + " isn't supported");
    }
    try {
      DocumentConverter converter = getDocumentConverter(options);
      converter.convert(source)
          .as(documentFormat(inFormat))
          .to(destination)
          .as(documentFormat(outFormat))
          .execute();
    } catch (final Exception e) {
      throw new DocumentFormatConversionException(e.getMessage(), e);
    }
  }

  private boolean isNotFormatSupported(final DocumentFormat format) {
    return !Arrays.asList(getSupportedFormats()).contains(format);
  }

  private DocumentConverter getDocumentConverter(final FilterOption... options) {
    OfficeManager manager = service.getOfficeManager();
    LocalConverter.Builder builder = LocalConverter.builder().officeManager(manager);
    if (options.length > 0) {
      final Map<String, Object> filterData = new HashMap<>();
      final Map<String, Object> customProperties = new HashMap<>();
      for (final FilterOption option : options) {
        filterData.put(option.getName(), option.getValue());
      }
      customProperties.put("FilterData", filterData);
      builder = builder.storeProperties(customProperties);
    }
    return builder.build();
  }

  private org.jodconverter.core.document.DocumentFormat documentFormat(final DocumentFormat format) {
    String mimeType = format.getMimeType();
    org.jodconverter.core.document.DocumentFormat docFormat =
        DefaultDocumentFormatRegistry.getFormatByMediaType(mimeType);
    if (docFormat == null) {
      if (RTF_MIME_TYPE.equals(mimeType)) {
        mimeType = mimeType.replaceFirst("application", "text");
      }
      docFormat = DefaultDocumentFormatRegistry.getFormatByMediaType(mimeType);
    }
    if (docFormat == null) {
      throw new UnknownFormatConversionException(
          "Unknown document format for MIME-TYPE: " + format.getMimeType());
    }
    return docFormat;
  }
}
