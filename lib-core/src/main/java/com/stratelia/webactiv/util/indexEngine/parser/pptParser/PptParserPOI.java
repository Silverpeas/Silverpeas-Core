package com.stratelia.webactiv.util.indexEngine.parser.pptParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.poi.hslf.extractor.PowerPointExtractor;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;

/**
 * a pptParser uses POI to extract the text wich will be indexed
 * 
 * @author $Author: neysseri $
 */
public class PptParserPOI implements Parser {
  public PptParserPOI() {
  }

  public Reader getReader(String path, String encoding) {
    Reader reader = null;
    InputStream file = null;

    try {
      file = new FileInputStream(path);

      PowerPointExtractor extractor = new PowerPointExtractor(file);

      String text = extractor.getText(true, true);

      reader = new StringReader(text);
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "PptParserPOI",
          "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
    } finally {
      try {
        file.close();
      } catch (IOException ioe) {
        SilverTrace.error("indexEngine", "PptParserPOI.getReader()",
            "indexEngine.MSG_IO_ERROR_WHILE_CLOSING", path, ioe);
      }
    }
    return reader;
  }
}