/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.jstl.constant.reflect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that parses the simple field name and its declaring class from the fully
 * qualified name (path) of the field.
 *
 */
public class FieldPathParser {

  private static final String REGEX_FIELD_PATH = "((\\w+\\.)+)(\\w+)";
  private static final int GROUP_INDEX_CLASS = 1;
  private static final int GROUP_INDEX_FIELD = 3;
  private String className;
  private String fieldName;

  /**
   * Constructor for the parser. The fully qualified field name is parsed into its simple name and
   * its declaring class name.
   *
   * @param path the fully qualified name (path) of the field
   */
  public FieldPathParser(String path) {
    Matcher matcher = Pattern.compile(REGEX_FIELD_PATH).matcher(path);
    matcher.find();
    this.className = matcher.group(GROUP_INDEX_CLASS);
    this.className = className.substring(0, className.length() - 1);
    this.fieldName = matcher.group(GROUP_INDEX_FIELD);
  }

  public String getDeclaringClassName() {
    return this.className;
  }

  public String getFieldName() {
    return this.fieldName;
  }
}
