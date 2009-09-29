/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util.indexEngine.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Executable used to test a parser.
 */
public class TestParser {
  static public void main(String[] argv) {
    if (argv.length != 2) {
      System.err.println("usage: java com...TestParser  mime-type file");
      System.exit(1);
    }
    Parser parser = ParserManager.getParser(argv[0]);
    if (parser == null) {
      System.err.println("unknown mime-type : " + argv[0]);
      System.exit(1);
    }
    Reader reader = parser.getReader(argv[1], null);
    if (parser == null) {
      System.err.println("unknown file : " + argv[1]);
      System.exit(1);
    }

    BufferedReader bReader = new BufferedReader(reader);
    try {
      String line;
      while ((line = bReader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      System.err.println("io error");
      System.exit(1);
    }
  }
}
