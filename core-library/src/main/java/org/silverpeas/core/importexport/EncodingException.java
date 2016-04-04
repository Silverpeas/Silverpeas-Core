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

package org.silverpeas.core.importexport;

/**
 * A runtime exception that is thrown when an error occurs while encoding a Silverpeas object into a
 * formatted string or stream. It is a runtime exception because such exceptions shouldn't occur in
 * an usual way.
 */
public class EncodingException extends RuntimeException {

  private static final long serialVersionUID = -2690487094203535737L;

  /**
   * Constructs a new encoding exception with the specified cause.
   * @param thrwbl the cause of this exception.
   */
  public EncodingException(final Throwable thrwbl) {
    super(thrwbl);
  }

  /**
   * Constructs a new encoding exception with the specified message and the specified cause.
   * @param message the message.
   * @param thrwbl the cause.
   */
  public EncodingException(String message, final Throwable thrwbl) {
    super(message, thrwbl);
  }

  /**
   * Constructs a new encoding exception with the specified message.
   * @param message the message.
   */
  public EncodingException(String message) {
    super(message);
  }

}
