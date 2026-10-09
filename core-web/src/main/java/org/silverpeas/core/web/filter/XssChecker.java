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

import java.util.List;
import java.util.Optional;

/**
 * Checks a value sent to Silverpeas against the XSS injections by composing the security checkers
 * it is given: a value is allowed only if all of them allow it, and the reason told for a rejection
 * is the one of the first checker rejecting the value. This composite knows none of the checkers
 * in particular, they are provided by the {@link SecurityCheckerProvider}.
 *
 * @author mmoquillon
 */
final class XssChecker implements SecurityChecker {

  private final List<SecurityChecker> checkers;

  /**
   * Constructs a checker composing the specified checkers, consulted in their order.
   *
   * @param checkers the security checkers to compose.
   */
  XssChecker(final List<SecurityChecker> checkers) {
    this.checkers = List.copyOf(checkers);
  }

  @Override
  public Optional<String> rejectionIn(final String value) {
    return checkers.stream()
        .map(c -> c.rejectionIn(value))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }
}
