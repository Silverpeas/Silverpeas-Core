/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.index.indexing.parser.rtfParser;

import org.silverpeas.core.index.indexing.parser.PipedParser;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.io.IOUtils;

import javax.inject.Named;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/**
 * ExcelParser parse an excel file
 * @author $Author: neysseri $
 */
@Named("rtfParser")
public class RtfParser extends PipedParser {

  public RtfParser() {
  }

  public void outPutContent(Writer out, String path, String encoding) throws IOException {

    FileInputStream in = null;
    try {
      in = new FileInputStream(path);
      byte[] buffer = new byte[in.available()];
      in.read(buffer, 0, in.available());

      // RTF always uses ASCII, so we don't need to care about the encoding
      String input = new String(buffer);

      String result = null;
      try {
        // use build in RTF parser from Swing API
        RTFEditorKit rtfEditor = new RTFEditorKit();
        Document doc = rtfEditor.createDefaultDocument();
        rtfEditor.read(new StringReader(input), doc, 0);

        result = doc.getText(0, doc.getLength());
      } catch (Exception e) {
        SilverTrace.warn("indexing", "RtfParser.outPutContent()", "", e);
      }
      out.write(result);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}