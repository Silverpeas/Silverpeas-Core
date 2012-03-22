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

package com.silverpeas.util;

import com.google.common.io.Closeables;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.msoffice.OfficeParser;
import org.apache.tika.msoffice.ooxml.OOXMLParser;
import org.apache.tika.openoffice.OpenDocumentParser;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MetadataExtractor {

  public MetadataExtractor() {
  }

  /**
   * Return Metadata of a document.
   * @param fileName
   * @return Metadata
   */
  public MetaData extractMetadata(String fileName) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(fileName);
      return getMetadata(inputStream);
    } catch (IOException ex) {
      SilverTrace.warn("MetadataExtractor.getMetadata()", "SilverpeasException.WARNING",
          "util.EXE_CANT_GET_SUMMARY_INFORMATION" + ex.getMessage(), ex);
      return new MetaData(new Metadata());
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  private MetaData getMetadata(InputStream inputStream) throws IOException {
    Metadata metadata = new Metadata();
    TikaConfig configuration = TikaConfig.getDefaultConfig();
    ParseContext context = new ParseContext();
    CompositeParser parser = ((CompositeParser) configuration.getParser());
    Parser openOfficeParser = new OpenDocumentParser();
    Map<MediaType, Parser> parsers = parser.getParsers(context);
    for (MediaType type : openOfficeParser.getSupportedTypes(context)) {
      parsers.put(type, openOfficeParser);
    }
    Parser officeParser = new OfficeParser();
    for (MediaType type : officeParser.getSupportedTypes(context)) {
      parsers.put(type, officeParser);
    }
    Parser ooxmlParser = new OOXMLParser();
    for (MediaType type : ooxmlParser.getSupportedTypes(context)) {
      parsers.put(type, ooxmlParser);
    }
    parser.setParsers(parsers);
    Tika tika = new Tika(configuration);
    tika.parse(inputStream, metadata);
    return new MetaData(metadata);
  }

  public MetaData extractMetadata(File file) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return getMetadata(inputStream);
    } catch (IOException ex) {
      SilverTrace.warn("MetadataExtractor.getMetadata()", "SilverpeasException.WARNING",
          "util.EXE_CANT_GET_SUMMARY_INFORMATION" + ex.getMessage(), ex);
      return new MetaData(new Metadata());
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }
}
