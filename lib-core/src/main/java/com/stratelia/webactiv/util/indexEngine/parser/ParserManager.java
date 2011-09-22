/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.indexEngine.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.parser.tika.TikaParser;

/**
 * The ParserManager class manages all the parsers which will be used to parse the indexed files.
 */
public final class ParserManager {

  /**
   * Set the parser for a given file format.
   * @param format
   * @param parser  
   */
  static public void setParser(String format, Parser parser) {
    parserMap.put(format, parser);
  }

  /**
   * Returns the set of all the known file formats. The returned set is a Set of String.
   * @return 
   */
  static public Set<String> getFormatNames() {
    return parserMap.keySet();
  }

  /**
   * Get the parser for a given file format.
   * @param format 
   * @return 
   */
  static public Parser getParser(String format) {
    Parser parser = parserMap.get(format);
    if (parser == null) {
      parser = new TikaParser();
    }
    return parser;
  }

  /**
   * Set all the parsers declared in Parsers.properties file.
   */
  static private void init() {
    Enumeration<String> formatNames = null;

    try {
      ResourceLocator MyResource = new ResourceLocator(
              "com.stratelia.webactiv.util.indexEngine.Parser", "");

      formatNames = MyResource.getKeys();
      while (formatNames.hasMoreElements()) {
        String name = "";
        String newCall = "";
        String className = "";

        try {
          name = formatNames.nextElement();
          newCall = MyResource.getString(name);
          if ("ignored".equals(newCall) || "".equals(newCall)) {
            continue; // we skip ignored mime type
          }
          className = getClassName(newCall);

          Class classe = Class.forName(className);
          Class[] parametersClass = getParametersClass(newCall);
          Constructor constructor = classe.getConstructor(parametersClass);
          Object[] parameters = getParameters(newCall);
          Parser parser = (Parser) constructor.newInstance(parameters);

          parserMap.put(name, parser);
          SilverTrace.debug("indexEngine", "ParserManager", "indexEngine.MSG_INIT_PARSER",
                  name + ", " + newCall);

        } catch (ClassNotFoundException e) {
          SilverTrace.error("indexEngine", "ParserManager", "indexEngine.MSG_UNKNOWN_PARSER_CLASS",
                  name + ", " + className);
        } catch (Exception e) {
          SilverTrace.fatal("indexEngine", "ParserManager",
                  "indexEngine.MSG_PARSER_INITIALIZATION_FAILED", name);
        }
      }
    } catch (MissingResourceException e) {
      SilverTrace.fatal("indexEngine", "ParserManager", "indexEngine.MSG_MISSING_PARSER_PROPERTIES");
    }
  }

  /**
   * Returns the class name in a string like "className(args, args, ...)"
   */
  static private String getClassName(String newCall) {
    int par = newCall.indexOf("(");
    if (par == -1) {
      return newCall.trim();
    } else {
      return newCall.substring(0, par).trim();
    }
  }

  /**
   * Returns the args values in a string like "className(args, args, ...)"
   */
  static private Object[] getParameters(String newCall) {
    int lPar = newCall.indexOf('(');
    int rPar = newCall.indexOf(')');
    if (lPar == -1 || rPar == -1 || lPar + 1 >= rPar) {
      return new Object[0];
    }

    List<String> args = new ArrayList<String>();
    String argsString = newCall.substring(lPar + 1, rPar);
    StringTokenizer st = new StringTokenizer(argsString, ",", false);
    while (st.hasMoreTokens()) {
      args.add(st.nextToken().trim());
    }
    return args.toArray();
  }

  /**
   * Returns the args types in a string like "className(args, args, ...)"
   */
  static private Class[] getParametersClass(String newCall) {
    int lPar = newCall.indexOf('(');
    int rPar = newCall.indexOf(')');
    if (lPar == -1 || rPar == -1 || lPar + 1 >= rPar) {
      return new Class[0];
    }

    List args = new ArrayList();
    String argsString = newCall.substring(lPar + 1, rPar);
    StringTokenizer st = new StringTokenizer(argsString, ",", false);
    while (st.hasMoreTokens()) {
      // always java.lang.String.
      args.add(st.nextToken().getClass());
    }
    return (Class[]) args.toArray(new Class[args.size()]);
  }
  /**
   * The map giving the parser for a specific file format. The type of this map is : Map (String ->
   * Parser).
   */
  static private final Map<String, Parser> parserMap;

  /**
   * At class initialization time, the parser's map is built and initialized from the
   * Parsers.properties file.
   */
  static {
    parserMap = new HashMap<String, Parser>();
    init();
  }

  private ParserManager() {
  }
}
