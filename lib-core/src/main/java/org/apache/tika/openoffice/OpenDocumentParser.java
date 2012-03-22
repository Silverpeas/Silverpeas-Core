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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * OpenOffice parser
 */
public class OpenDocumentParser implements Parser {

  private static final Set<MediaType> SUPPORTED_TYPES =
      Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
      MediaType.application("vnd.sun.xml.writer"),
      MediaType.application("vnd.oasis.opendocument.text"),
      MediaType.application("vnd.oasis.opendocument.graphics"),
      MediaType.application("vnd.oasis.opendocument.presentation"),
      MediaType.application("vnd.oasis.opendocument.spreadsheet"),
      MediaType.application("vnd.oasis.opendocument.chart"),
      MediaType.application("vnd.oasis.opendocument.image"),
      MediaType.application("vnd.oasis.opendocument.formula"),
      MediaType.application("vnd.oasis.opendocument.text-master"),
      MediaType.application("vnd.oasis.opendocument.text-web"),
      MediaType.application("vnd.oasis.opendocument.text-template"),
      MediaType.application("vnd.oasis.opendocument.graphics-template"),
      MediaType.application("vnd.oasis.opendocument.presentation-template"),
      MediaType.application("vnd.oasis.opendocument.spreadsheet-template"),
      MediaType.application("vnd.oasis.opendocument.chart-template"),
      MediaType.application("vnd.oasis.opendocument.image-template"),
      MediaType.application("vnd.oasis.opendocument.formula-template"),
      MediaType.application("x-vnd.oasis.opendocument.text"),
      MediaType.application("x-vnd.oasis.opendocument.graphics"),
      MediaType.application("x-vnd.oasis.opendocument.presentation"),
      MediaType.application("x-vnd.oasis.opendocument.spreadsheet"),
      MediaType.application("x-vnd.oasis.opendocument.chart"),
      MediaType.application("x-vnd.oasis.opendocument.image"),
      MediaType.application("x-vnd.oasis.opendocument.formula"),
      MediaType.application("x-vnd.oasis.opendocument.text-master"),
      MediaType.application("x-vnd.oasis.opendocument.text-web"),
      MediaType.application("x-vnd.oasis.opendocument.text-template"),
      MediaType.application("x-vnd.oasis.opendocument.graphics-template"),
      MediaType.application("x-vnd.oasis.opendocument.presentation-template"),
      MediaType.application("x-vnd.oasis.opendocument.spreadsheet-template"),
      MediaType.application("x-vnd.oasis.opendocument.chart-template"),
      MediaType.application("x-vnd.oasis.opendocument.image-template"),
      MediaType.application("x-vnd.oasis.opendocument.formula-template"))));

  private Parser meta = new OpenDocumentMetaParser();

  private Parser content = new OpenDocumentContentParser();

  public Parser getMetaParser() {
    return meta;
  }

  public void setMetaParser(Parser meta) {
    this.meta = meta;
  }

  public Parser getContentParser() {
    return content;
  }

  public void setContentParser(Parser content) {
    this.content = content;
  }

  public Set<MediaType> getSupportedTypes(ParseContext context) {
    return SUPPORTED_TYPES;
  }

  public void parse(
      InputStream stream, ContentHandler handler,
      Metadata metadata, ParseContext context)
      throws IOException, SAXException, TikaException {
    ZipInputStream zip = new ZipInputStream(stream);
    ZipEntry entry = zip.getNextEntry();
    while (entry != null) {
      if (entry.getName().equals("mimetype")) {
        String type = IOUtils.toString(zip, "UTF-8");
        metadata.set(Metadata.CONTENT_TYPE, type);
      } else if (entry.getName().equals("meta.xml")) {
        meta.parse(zip, new DefaultHandler(), metadata, context);
        zip.closeEntry();
      } /*
         * else if (entry.getName().endsWith("content.xml")) { content.parse(zip, handler, metadata,
         * context); zip.closeEntry(); }
         */
      entry = zip.getNextEntry();
    }
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
