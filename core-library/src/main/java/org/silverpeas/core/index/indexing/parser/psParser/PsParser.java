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

/*
 * author
 * Mohammed Hguig
 */

package org.silverpeas.core.index.indexing.parser.psParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.silverpeas.core.index.indexing.parser.PipedParser;

import javax.inject.Named;

/**
 * the psParser parse a postscript file
 */
@Named("psParser")
public class PsParser extends PipedParser {

  public PsParser() {
  }

  /**
   * outPutContent read the text content of a ps file and store it in out to be ready to be indexed
   */

  public void outPutContent(Writer out, String path, String encoding)
      throws IOException {

    BufferedReader buffer = null;

    try {
      InputStream file = new FileInputStream(path);
      if (encoding != null) {
        buffer = new BufferedReader(new InputStreamReader(file, encoding));
      } else {
        buffer = new BufferedReader(new InputStreamReader(file));
      }

      outPutChar(out, buffer);
    } finally {
      if (buffer != null)
        buffer.close();
    }
  }

  /**
   * read the text content between parses ( and )
   */

  public void outPutChar(Writer out, BufferedReader buffer) throws IOException {
    int ch, para = 0, last = 0;
    char charr;

    while ((ch = buffer.read()) != -1) {
      charr = (char) ch;
      switch (ch) {
        case '%':
          if (para == 0) {
            buffer.readLine();
          } else {
            out.write(charr);
          }
        case '\n':
          if (last == 1) {
            out.write("");
            out.write('\n');
            last = 0;
          }
          break;
        case '(':
          if (para++ > 0) {
            out.write(charr);
          }
          break;
        case ')':
          if (para-- > 1) {
            out.write(charr);
          } else {
            out.write(' ');
          }
          last = 1;
          break;
        case '\\':
          if (para > 0)
            switch (charr = (char) buffer.read()) {
              case '(':
              case ')':
                out.write(charr);
                break;
              case 't':
                out.write('\t');
                break;
              case 'n':
                out.write('\n');
                break;
              case '\\':
                out.write('\\');
                break;
              case '0':
              case '1':
              case '2':
              case '3':
              case '4':
              case '5':
              case '6':
              case '7':
                out.write('\\');
              default:
                out.write(charr);
                break;
            }
          break;
        default:
          if (para > 0) {
            out.write(charr);
          }
      }

    }
  }
}
