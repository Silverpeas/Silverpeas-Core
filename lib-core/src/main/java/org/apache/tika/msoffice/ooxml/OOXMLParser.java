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

package org.apache.tika.msoffice.ooxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Office Open XML (OOXML) parser.
 */
public class OOXMLParser implements Parser {

  private static final Set<MediaType> SUPPORTED_TYPES =
      Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
      MediaType.application("x-tika-ooxml"),
      MediaType.application("vnd.openxmlformats-officedocument.presentationml.presentation"),
      MediaType.application("vnd.ms-powerpoint.presentation.macroenabled.12"),
      MediaType.application("vnd.openxmlformats-officedocument.presentationml.template"),
      MediaType.application("vnd.openxmlformats-officedocument.presentationml.slideshow"),
      MediaType.application("vnd.ms-powerpoint.slideshow.macroenabled.12"),
      MediaType.application("vnd.ms-powerpoint.addin.macroenabled.12"),
      MediaType.application("vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
      MediaType.application("vnd.ms-excel.sheet.macroenabled.12"),
      MediaType.application("vnd.openxmlformats-officedocument.spreadsheetml.template"),
      MediaType.application("vnd.ms-excel.template.macroenabled.12"),
      MediaType.application("vnd.ms-excel.addin.macroenabled.12"),
      MediaType.application("vnd.openxmlformats-officedocument.wordprocessingml.document"),
      MediaType.application("vnd.ms-word.document.macroenabled.12"),
      MediaType.application("vnd.openxmlformats-officedocument.wordprocessingml.template"),
      MediaType.application("vnd.ms-word.template.macroenabled.12"))));

  public Set<MediaType> getSupportedTypes(ParseContext context) {
    return SUPPORTED_TYPES;
  }

  public void parse(
      InputStream stream, ContentHandler handler,
      Metadata metadata, ParseContext context)
      throws IOException, SAXException, TikaException {
    OOXMLExtractorFactory.parse(stream, handler, metadata, context);
  }

  /**
   * @deprecated This method will be removed in Apache Tika 1.0.
   */
  public void parse(
      InputStream stream, ContentHandler handler, Metadata metadata)
      throws IOException, SAXException, TikaException {
    parse(stream, handler, metadata, new ParseContext());
  }

}
