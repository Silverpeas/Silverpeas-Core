/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.filter;

import java.util.Optional;

/**
 * A checker of the values sent to Silverpeas against a kind of injection. Each implementation
 * knows one kind of injection and tells the reason for which it rejects a value, so that the
 * writer of a content can fix it. The checkers are provided by the
 * {@link SecurityCheckerProvider} and composed by the {@link XssChecker}: a new kind of injection
 * is covered by a new implementation registered in the provider, without any other change.
 *
 * @author mmoquillon
 */
interface SecurityChecker {

  /**
   * The length beyond which the excerpt of a rejected value is cut.
   */
  int EXCERPT_LENGTH = 80;

  /**
   * Gets the reason for which the given value is rejected.
   *
   * @param value the value to check.
   * @return the reason of the rejection, with an excerpt of what has been detected, or nothing if
   * the value is allowed.
   */
  Optional<String> rejectionIn(final String value);

  /**
   * Is the given value allowed?
   *
   * @param value the value to check.
   * @return true if the value is allowed, false if it is rejected.
   */
  default boolean areAllAllowedIn(final String value) {
    return rejectionIn(value).isEmpty();
  }

  /**
   * Builds the reason of a rejection: the given reason followed by an excerpt of the text from the
   * specified position.
   *
   * @param reason the reason of the rejection.
   * @param text the rejected text.
   * @param start the position in the text of what has been detected.
   * @return the reason of the rejection.
   */
  static Optional<String> rejection(final String reason, final String text, final int start) {
    return Optional.of(reason + " in " + excerptOf(text, start));
  }

  /**
   * Gets a quoted excerpt of the given text from the specified position, cut beyond
   * {@link #EXCERPT_LENGTH} characters.
   *
   * @param text the text.
   * @param start the position from which the text is excerpted.
   * @return the excerpt between double quotes, ended by an ellipsis if it has been cut.
   */
  static String excerptOf(final String text, final int start) {
    final int end = Math.min(text.length(), start + EXCERPT_LENGTH);
    return "\"" + text.substring(start, end) + (end < text.length() ? "…" : "") + "\"";
  }
}
