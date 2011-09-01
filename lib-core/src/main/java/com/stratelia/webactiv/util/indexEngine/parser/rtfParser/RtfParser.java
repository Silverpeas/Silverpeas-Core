/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.indexEngine.parser.rtfParser;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.PipedParser;
import org.apache.commons.io.IOUtils;

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

public class RtfParser extends PipedParser {
  /**
   * Pattern used to remove {\*\ts...} RTF keywords, which cause NPE in Java 1.4.
   */
  // private static final Pattern TS_REMOVE_PATTERN =
  // Pattern.compile("\\{\\\\\\*\\\\ts[^\\}]*\\}", Pattern.DOTALL);

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

      // workaround to remove RTF keywords that cause a NPE in Java 1.4
      // this is a known bug in Java 1.4 that was fixed in 1.5
      // please see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5042109 for
      // the official bug report
      // input = TS_REMOVE_PATTERN.matcher(input).replaceAll("");

      String result = null;
      try {
        // use build in RTF parser from Swing API
        RTFEditorKit rtfEditor = new RTFEditorKit();
        Document doc = rtfEditor.createDefaultDocument();
        rtfEditor.read(new StringReader(input), doc, 0);

        result = doc.getText(0, doc.getLength());
      } catch (Exception e) {
        SilverTrace.warn("indexEngine", "RtfParser.outPutContent()", "", e);
      }

      SilverTrace.debug("indexEngine", "RtfParser.outPutContent",
          "root.MSG_GEN_EXIT_METHOD", result);

      out.write(result);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}