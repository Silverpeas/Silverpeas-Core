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

package org.apache.tika.msoffice;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

public class HSLFExtractor extends AbstractPOIFSExtractor {
  public HSLFExtractor(ParseContext context) {
    super(context);
  }

  protected void parse(
      POIFSFileSystem filesystem, XHTMLContentHandler xhtml)
      throws IOException, SAXException, TikaException {
    PowerPointExtractor powerPointExtractor =
        new PowerPointExtractor(filesystem);
    xhtml.element("p", powerPointExtractor.getText(true, true));

    List<OLEShape> shapeList = powerPointExtractor.getOLEShapes();
    for (OLEShape shape : shapeList) {
      TikaInputStream stream =
          TikaInputStream.get(shape.getObjectData().getData());
      try {
        String mediaType = null;
        if ("Excel.Chart.8".equals(shape.getProgID())) {
          mediaType = "application/vnd.ms-excel";
        }
        handleEmbeddedResource(
            stream, Integer.toString(shape.getObjectID()),
            mediaType, xhtml, false);
      } finally {
        stream.close();
      }
    }
  }
}
