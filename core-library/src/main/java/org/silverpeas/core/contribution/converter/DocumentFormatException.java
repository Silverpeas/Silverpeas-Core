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

package org.silverpeas.core.contribution.converter;

/**
 * Exception thrown when the format of a document or the format in which a document has to be
 * converted isn't supported by a converter.
 */
public class DocumentFormatException extends RuntimeException {
  private static final long serialVersionUID = -4921928945224013781L;

  /**
   * Creates a new instance of <code>DocumentFormatException</code> without detail message.
   */
  public DocumentFormatException() {
  }

  /**
   * Constructs an instance of <code>DocumentFormatException</code> with the specified detail
   * message.
   * @param msg the detail message.
   */
  public DocumentFormatException(String msg) {
    super(msg);
  }
}
