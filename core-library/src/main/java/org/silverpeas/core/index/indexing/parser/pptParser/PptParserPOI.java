/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.parser.pptParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.extractor.PowerPointExtractor;

import org.silverpeas.core.index.indexing.parser.Parser;

import org.silverpeas.core.silvertrace.SilverTrace;

import javax.inject.Named;

/**
 * a pptParser uses POI to extract the text which will be indexed
 *
 * @author $Author: neysseri $
 */
@Named("powerPointParser")
public class PptParserPOI implements Parser {

  public PptParserPOI() {
  }

  @Override
  public Reader getReader(String path, String encoding) {
    Reader reader = null;
    InputStream file = null;
    try {
      file = new FileInputStream(path);
      PowerPointExtractor extractor = new PowerPointExtractor(file);
      String text = extractor.getText(true, true);
      reader = new StringReader(text);
    } catch (Exception e) {
      SilverTrace.error("indexing", "PptParserPOI", "indexing.MSG_IO_ERROR_WHILE_READING",
          path, e);
    } finally {
      IOUtils.closeQuietly(file);
    }
    return reader;
  }
}