/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.util;

import org.silverpeas.core.util.lang.SystemWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A resolver of variables in some properties value. For example, the variable ${env.FOO} in a
 * property will be resolved by fetching the value of the <code>FOO</code> environment variable,
 * and ${sys.TOTO} will be resolved by fetching the value of the <code>FOO</code> system property.
 * @author mmoquillon
 */
class VariableResolver {

  private static final Pattern VAR_PATTERN =
      Pattern.compile(".*(\\$\\{)((env|sys)\\..*)(\\}).*$");
  private static final Pattern VAR_REPLACEMENT = Pattern.compile("\\$\\{(env|sys)\\..*\\}");

  /**
   * Resolves the specified value. If it contains no variables, then nothing is performed.
   * Otherwise, the variable will be replaced by its value.
   * @param value the value to resolve. It will be treated only if it is a String with some
   * variable declarations.
   * @return the resolved variable.
   */
  static Object resolve(Object value) {
    if (value != null && value instanceof String) {
      value = resolve((String) value);
    }
    return value;
  }

  /**
   * Resolves the specified value. If it contains no variables, then nothing is performed.
   * Otherwise, the variable will be replaced by its value.
   * @param value the value to resolve.
   * @return the resolved variable.
   */
  static String resolve(String value) {
    String resolvedValue = value;
    if (value != null && !value.trim().isEmpty()) {
      Matcher matching = VAR_PATTERN.matcher(value);
      if (matching.matches()) {
        String[] statement = matching.group(2).split("\\.");
        String resolution;
        switch (statement[0]) {
          case "env":
            resolution = SystemWrapper.get().getenv(statement[1]);
            break;
          default:
            resolution = SystemWrapper.get().getProperty(statement[1]);
        }
        matching = VAR_REPLACEMENT.matcher(value);
        resolvedValue = matching.replaceAll(resolution.replaceAll("\\\\", "/"));
      }
    }
    return resolvedValue;
  }
}
