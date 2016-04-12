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

package org.silverpeas.core.util;

/**
 * ArgumentAssertion gathers all of the assertion operations that can be used for for checking the
 * arguments specified in method call are correct. If an assertion failed, then an
 * IllegalArgumentException is thrown. Using a such class to check the arguments before any code
 * execution in a method ensures at the beginning the published <i>contract</i> of a method is well
 * satisfied. It permits to detect in an explicit way the violation of that <i>contract</i> and thus
 * to fix it in a fast way. Without any argument checking, the violation detection could be done in
 * a more or less deep stacktrace execution of the method and in an implicit way, so letting it
 * difficult to identify and to fix it.
 */
public final class ArgumentAssertion {

  /**
   * Asserts the specified String argument is well defined, that is it is not null and not empty.
   * @param arg the argument to assert.
   * @param msg the message to pass if the argument isn't well defined.
   */
  public static void assertDefined(final String arg, final String msg) {
    if (StringUtil.isNotDefined(arg)) {
      throwIllegalArgumentException(msg);
    }
  }

  /**
   * Asserts the specified String argument is not defined, that is it is null or empty.
   * @param arg the argument to assert.
   * @param msg the message to pass if the argument isn't well verified.
   */
  public static void assertNotDefined(final String arg, final String msg) {
    if (StringUtil.isDefined(arg)) {
      throwIllegalArgumentException(msg);
    }
  }

  /**
   * Asserts the specified argument is not null.
   * @param arg the argument to assert.
   * @param msg the message to pass if the argument is null.
   */
  public static void assertNotNull(final Object arg, final String msg) {
    if (arg == null) {
      throwIllegalArgumentException(msg);
    }
  }

  /**
   * Throws an IllegalArgumentException with the specified message.
   * @param msg the message to pass.
   */
  private static void throwIllegalArgumentException(final String msg) {
    throw new IllegalArgumentException(msg);
  }
}
