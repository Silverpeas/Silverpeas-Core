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

package org.silverpeas.core.webapi.pdc;

/**
 * A value of a PdC's axis either used in a position of a classification the PdC or in as a axis
 * value. This value has the particularity to be localized according to the language of the user to
 * which it has to be displayed.
 */
public interface LocalizedValue {

  /**
   * Maximum number of value terms to render in the value rendering. If the path of value is greater
   * than this number, then it is truncated. An axis value is in fact a path of terms, each of them
   * refining the parent term. In order to avoid a long value rendering, the path is usually
   * truncated between the first terms and the last ones. This parameter indicates the maximum
   * number of terms in a path to display.
   */
  final int MAX_NUMBER_OF_RENDERED_PATH_NODE = 5;

  /**
   * Number of value terms to render before and after the the truncation separator in a truncated
   * axis value rendering. An axis value is in fact a path of terms, each of them refining the
   * parent term. In order to avoid a long value rendering, the path is usually truncated between
   * the first terms and the last ones. This parameter indicates the number of terms to print out
   * before and after the truncation.
   */
  final int NUMBER_OF_RENDERED_PATH_NODE_IN_TRUNCATION = 2;

  /**
   * The truncator symbol to use in a truncated value rendering.
   */
  final String TRUNCATION_SEPARATOR = " ... ";

  /**
   * The path separator to use in an axis value rendering.
   */
  final String SEPARATOR_PATH = " / ";

  /**
   * Gets the path of the value whose the terms are translated in a given language.
   * @return the translated path to render.
   */
  String getLocalizedPath();

  /**
   * Gets the language in which the path of the value is localized.
   * @return
   */
  String getLanguage();
}
