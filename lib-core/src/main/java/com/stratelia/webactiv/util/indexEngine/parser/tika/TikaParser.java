/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.stratelia.webactiv.util.indexEngine.parser.tika;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;
import com.stratelia.webactiv.util.indexEngine.parser.ParserHelper;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.odf.OpenDocumentParser;

/**
 *
 * @author ehugonnet
 */
public class TikaParser implements Parser {

  private final Tika tika = initTika();

  private final Tika initTika() {
    TikaConfig configuration = TikaConfig.getDefaultConfig();
    ParseContext context = new ParseContext();
    CompositeParser parser = ((CompositeParser) configuration.getParser());
    org.apache.tika.parser.Parser openOfficeParser = new OpenDocumentParser();
    Map<MediaType, org.apache.tika.parser.Parser> parsers = parser.getParsers(context);
    for (MediaType type : openOfficeParser.getSupportedTypes(context)) {
      parsers.put(type, openOfficeParser);
    }
    org.apache.tika.parser.Parser officeParser = new OfficeParser();
    for (MediaType type : officeParser.getSupportedTypes(context)) {
      parsers.put(type, officeParser);
    }
    org.apache.tika.parser.Parser ooxmlParser = new OOXMLParser();
    for (MediaType type : ooxmlParser.getSupportedTypes(context)) {
      parsers.put(type, ooxmlParser);
    }
    parser.setParsers(parsers);
    return new Tika(configuration);
  }

  @Override
  public Reader getReader(String path, String encoding) { 
    try {
      return tika.parse(ParserHelper.getContent(path));
    } catch (IOException ex) {
      SilverTrace.error("util", "OpenxmlParser.getReader", "root.EX_LOAD_IO_EXCEPTION", ex);
    }
    return new StringReader("");
  }
}
