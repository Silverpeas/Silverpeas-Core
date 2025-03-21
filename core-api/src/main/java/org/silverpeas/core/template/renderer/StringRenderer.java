/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.template.renderer;

import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 * Some useful formatting usable from string templates.
 * <p>
 *   For example:
 *   <code>$title;format="toUpper"$</code>
 * </p>
 */
public class StringRenderer implements AttributeRenderer<String> {

  @Override
  public String toString(String value, String formatName, Locale locale) {
    if (formatName == null) {
      return value;
    } else if ("toUpperFirstChar".equals(formatName)) {
      return StringUtils.capitalize(value);
    } else if ("toLowerFirstChar".equals(formatName)) {
      return StringUtils.uncapitalize(value);
    } else if ("toUpper".equals(formatName)) {
      return value.toUpperCase();
    } else if ("toLower".equals(formatName)) {
      return value.toLowerCase();
    } else {
      throw new IllegalArgumentException("Unsupported format name");
    }
  }
}
