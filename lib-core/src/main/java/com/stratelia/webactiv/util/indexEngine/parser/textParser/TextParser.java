package com.stratelia.webactiv.util.indexEngine.parser.textParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;

/*
 * CVS Informations
 * 
 * $Id: TextParser.java,v 1.2 2007/12/05 10:32:35 neysseri Exp $
 * 
 * $Log: TextParser.java,v $
 * Revision 1.2  2007/12/05 10:32:35  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:48  nchaix
 * no message
 *
 * Revision 1.3  2002/01/09 17:22:29  dwenzek
 * mise en place des silvertrace
 *
 */

/**
 * A TextParser parse a text file without any processing.
 * 
 * @author $Author: neysseri $
 */
public class TextParser implements Parser {

  /**
   * Constructor declaration
   */
  public TextParser() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param path
   * @param encoding
   * 
   * @return
   */
  public Reader getReader(String path, String encoding) {
    Reader reader = null;

    try {
      InputStream file = new FileInputStream(path);

      if (encoding == null) {
        reader = new InputStreamReader(file);
      } else {
        reader = new InputStreamReader(file, encoding);
      }
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "TextParser",
          "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
    }
    return reader;
  }

}
