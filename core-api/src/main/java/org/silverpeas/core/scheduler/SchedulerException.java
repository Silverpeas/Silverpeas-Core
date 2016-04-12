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

package org.silverpeas.core.scheduler;

/**
 * Exception thrown when an error occurs within the scheduler.
 */
public class SchedulerException extends Exception {

  private static final long serialVersionUID = 8770810910927989722L;

  /**
   * Constructs an empty SchedulerException.
   */
  public SchedulerException() {
    super();
  }

  /**
   * Constructs a SchedulerException with the specified message.
   * @param aMessage the message about that exception.
   */
  public SchedulerException(final String aMessage) {
    super(aMessage);
  }

  /**
   * Constructs a SchedulerException with the specified cause.
   * @param cause the exception that caused this one.
   */
  public SchedulerException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a SchedulerException with the specified message and cause.
   * @param message a message about that exception.
   * @param cause the exception that caused this one.
   */
  public SchedulerException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
