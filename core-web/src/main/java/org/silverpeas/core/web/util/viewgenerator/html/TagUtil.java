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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.StringUtil;

import java.util.Optional;

/**
 * @author Yohann Chastagnier
 */
public class TagUtil {

  private static final int ANGULAR_JS_LENGTH = 10;

  /**
   * Hidden constructor.
   */
  private TagUtil() {
  }

  /**
   * Centralizes all the possibilities about the conversion of action into href.
   * @param action the action.
   * @return the formatted href.
   */
  public static String formatHrefFromAction(final String action) {
    String href = "href=\"" + action + "\"";
    if (action.startsWith("angularjs:")) {
      href = action.substring(ANGULAR_JS_LENGTH);
      if (href.contains("{{") && href.contains("}}")) {
        href = "ng-href=\"" + href + "\"";
      } else {
        href = "href=\"#\" ng-click=\"" + href + "\"";
      }
    }
    return href;
  }

  /**
   * Centralizes the formatting of a string value in order to get a dom id compatible value.
   * @param value any kind of value.
   * @return a dom id compatible value.
   */
  public static String formatForDomId(Object value) {
    return Optional.ofNullable(value)
        .map(Object::toString)
        .map(StringUtil::normalizeByRemovingAccent)
        .map(v -> v.replaceAll("[=\\- ]", "_"))
        .orElse(StringUtil.EMPTY);
  }
}
