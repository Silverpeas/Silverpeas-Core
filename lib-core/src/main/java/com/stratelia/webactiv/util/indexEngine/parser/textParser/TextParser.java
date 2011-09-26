/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.indexEngine.parser.textParser;


import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
   * @param path
   * @param encoding
   * @return
   */
  @Override
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
