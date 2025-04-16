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
package org.silverpeas.core.web.filter.exception;

import org.silverpeas.kernel.SilverpeasException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User: Yohann Chastagnier
 * Date: 05/03/14
 */
public abstract class WebSecurityException extends SilverpeasException {

  /**
   * Default constructor.
   * @param message the reason of the exception
   */
  protected WebSecurityException(final String message) {
    super(message + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
  }
}
