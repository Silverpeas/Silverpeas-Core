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

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Interface implemented by all Tika OOXML extractors.
 * @see org.apache.poi.POIXMLTextExtractor
 */
public interface OOXMLExtractor {

  /**
   * Returns the opened document.
   * @see POIXMLTextExtractor#getDocument()
   */
  POIXMLDocument getDocument();

  /**
   * {@link POIXMLTextExtractor#getMetadataTextExtractor()} not yet supported for OOXML by POI.
   */
  MetadataExtractor getMetadataExtractor();

  /**
   * Parses the document into a sequence of XHTML SAX events sent to the given content handler.
   */
  void getXHTML(ContentHandler handler, Metadata metadata, ParseContext context)
      throws SAXException, XmlException, IOException, TikaException;
}
