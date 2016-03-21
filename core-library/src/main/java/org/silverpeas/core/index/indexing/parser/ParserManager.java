/**
* Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.index.indexing.parser;

import org.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.core.index.indexing.parser.tika.TikaParser;
import org.silverpeas.util.ArrayUtil;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

/**
* The ParserManager class manages all the parsers which will be used to parse the indexed files.
*/
public final class ParserManager {

  private ParserManager() {
  }

  /**
* The map giving the parser for a specific file format. The type of this map is : Map (String ->
* Parser).
*/
  private static final Map<String, Parser> parserMap = new HashMap<>();
  private static final Parser defaultParser = new TikaParser();

  /**
* At class initialization time, the parser's map is built and initialized from the
* Parsers.properties file.
*/
  static {
    init();
  }

  /**
* Get the parser for a given file format.
*
* @param format
* @return
*/
  static public Parser getParser(String format) {
    Parser parser = parserMap.get(format);
    if (parser == null) {
      return defaultParser;
    }
    return parser;
  }

  /**
* Set all the parsers declared in Parsers.properties file.
*/
  private static void init() {
    try {
      SettingBundle parsersConfiguration =
          ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.Parser");
      Set<String> formatNames = parsersConfiguration.keySet();
      for (String name: formatNames) {
        String newCall = parsersConfiguration.getString(name);
        if ("ignored".equals(newCall) || newCall.isEmpty()) {
          continue; // we skip ignored mime type
        }
        String className = getClassName(newCall);
        try {
          Class<?> classe = Class.forName(className);
          Class[] parametersClass = getParametersClass(newCall);
          Constructor<?> constructor = classe.getConstructor(parametersClass);
          Object[] parameters = getParameters(newCall);
          Parser parser = (Parser) constructor.newInstance(parameters);
          parserMap.put(name, parser);
        } catch (ClassNotFoundException e) {
          SilverTrace.error("indexing", "ParserManager", "indexing.MSG_UNKNOWN_PARSER_CLASS",
            name + ", " + className);
        } catch (Exception e) {
          SilverTrace.fatal("indexing", "ParserManager",
            "indexing.MSG_PARSER_INITIALIZATION_FAILED", name);
        }
      }
    } catch (MissingResourceException e) {
      SilverTrace.fatal("indexing", "ParserManager", "indexing.MSG_MISSING_PARSER_PROPERTIES");
    }
  }

  /**
* Returns the class name in a string like "className(args, args, ...)"
*/
  static private String getClassName(String newCall) {
    int par = newCall.indexOf('(');
    if (par == -1) {
      return newCall.trim();
    }
    return newCall.substring(0, par).trim();
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
    String argsString = newCall.substring(lPar + 1, rPar);
    StringTokenizer st = new StringTokenizer(argsString, ",", false);
    List<String> args = new ArrayList<String>(st.countTokens());
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
      return ArrayUtil.EMPTY_CLASS_ARRAY;
    }
    String argsString = newCall.substring(lPar + 1, rPar);
    StringTokenizer st = new StringTokenizer(argsString, ",", false);
    List<Class<?>> args = new ArrayList<Class<?>>(st.countTokens());
    while (st.hasMoreTokens()) {
      args.add(st.nextToken().getClass());
    }
    return args.toArray(new Class<?>[args.size()]);
  }

}
