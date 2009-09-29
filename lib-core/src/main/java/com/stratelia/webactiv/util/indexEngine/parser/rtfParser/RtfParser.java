package com.stratelia.webactiv.util.indexEngine.parser.rtfParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.PipedParser;

/**
 * ExcelParser parse an excel file
 * 
 * @author $Author: neysseri $
 */

public class RtfParser extends PipedParser {
  /**
   * Pattern used to remove {\*\ts...} RTF keywords, which cause NPE in Java
   * 1.4.
   */
  // private static final Pattern TS_REMOVE_PATTERN =
  // Pattern.compile("\\{\\\\\\*\\\\ts[^\\}]*\\}", Pattern.DOTALL);

  public RtfParser() {
  }

  public void outPutContent(Writer out, String path, String encoding)
      throws IOException {
    FileInputStream in = new FileInputStream(path);

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
  }
}