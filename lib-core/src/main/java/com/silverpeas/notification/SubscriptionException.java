/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.notification;

/**
 * An exception that is thrown when a subscription failed.
 */
public class SubscriptionException extends RuntimeException {
  private static final long serialVersionUID = -2643000212677483215L;

  /**
   * Creates a new instance of <code>SubscriptionException</code> without detail message.
   */
  public SubscriptionException() {
  }

  /**
   * Constructs an instance of <code>SubscriptionException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public SubscriptionException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>SubscriptionException</code> with the specified cause.
   * @param cause the cause of this exception.
   */
  public SubscriptionException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs an instance of <code>SubscriptionException</code> with the specified detail message
   * and for the specified cause.
   * @param msg the detail message.
   * @param cause the cause of this exception.
   */
  public SubscriptionException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
