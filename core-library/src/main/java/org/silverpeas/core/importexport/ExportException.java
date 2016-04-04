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
 * Exception thrown when the export of a Silverpeas resource failed. It is a business exception
 * that occurs when a business operation invoked by a user fails.
 */
public class ExportException extends Exception {

  private static final long serialVersionUID = 7791895284880044020L;

  /**
   * Constructs a new ExportException by specifying the cause.
   * @param thrwbl the cause of this exception.
   */
  public ExportException(Throwable thrwbl) {
    super(thrwbl);
  }

  /**
   * Constructs a new ExportException with the specified message and cause.
   * @param message the message about the problem.
   * @param thrwbl the cause of the exception.
   */
  public ExportException(String message, Throwable thrwbl) {
    super(message, thrwbl);
  }

  /**
   * Constructs a new ExportException with the specified message.
   * @param message the message about the problem.
   */
  public ExportException(String message) {
    super(message);
  }

}
