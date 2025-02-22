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
package org.silverpeas.core.datereminder.exception;

/**
 * An exception that is thrown when a date reminder validation fails.
 *
 * @author Cécile Bonin
 */
public class DateReminderValidationException extends DateReminderException {

  private static final long serialVersionUID = -6824029730156116372L;

  /**
   * Constructs an instance of <code>DateReminderValidationException</code> with the specified detail
   * message.
   *
   * @param msg the detail message.
   */
  public DateReminderValidationException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>DateReminderValidationException</code> with the specified detail
   * message and with the specified cause.
   *
   * @param message the detail message.
   * @param cause the cause of this exception.
   */
  public DateReminderValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}