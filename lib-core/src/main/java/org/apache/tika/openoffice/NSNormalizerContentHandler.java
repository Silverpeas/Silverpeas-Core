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

package org.apache.tika.openoffice;

import java.io.IOException;
import java.io.StringReader;

import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Content handler decorator that:
 * <ul>
 * <li>Maps old OpenOffice 1.0 Namespaces to the OpenDocument ones</li>
 * <li>Returns a fake DTD when parser requests OpenOffice DTD</li>
 * </ul>
 */
public class NSNormalizerContentHandler extends ContentHandlerDecorator {

  private static final String OLD_NS =
      "http://openoffice.org/2000/";

  private static final String NEW_NS =
      "urn:oasis:names:tc:opendocument:xmlns:";

  private static final String DTD_PUBLIC_ID =
      "-//OpenOffice.org//DTD OfficeDocument 1.0//EN";

  public NSNormalizerContentHandler(ContentHandler handler) {
    super(handler);
  }

  private String mapOldNS(String ns) {
    if (ns != null && ns.startsWith(OLD_NS)) {
      return NEW_NS + ns.substring(OLD_NS.length()) + ":1.0";
    } else {
      return ns;
    }
  }

  @Override
  public void startElement(
      String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    AttributesImpl natts = new AttributesImpl();
    for (int i = 0; i < atts.getLength(); i++) {
      natts.addAttribute(
          mapOldNS(atts.getURI(i)), atts.getLocalName(i),
          atts.getQName(i), atts.getType(i), atts.getValue(i));
    }
    super.startElement(mapOldNS(namespaceURI), localName, qName, atts);
  }

  @Override
  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    super.endElement(mapOldNS(namespaceURI), localName, qName);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri)
      throws SAXException {
    super.startPrefixMapping(prefix, mapOldNS(uri));
  }

  /**
   * do not load any DTDs (may be requested by parser). Fake the DTD by returning a empty string as
   * InputSource
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws IOException, SAXException {
    if ((systemId != null && systemId.toLowerCase().endsWith(".dtd"))
        || DTD_PUBLIC_ID.equals(publicId)) {
      return new InputSource(new StringReader(""));
    } else {
      return super.resolveEntity(publicId, systemId);
    }
  }

}
