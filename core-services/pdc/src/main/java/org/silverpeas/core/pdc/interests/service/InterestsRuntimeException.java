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

/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package org.silverpeas.core.pdc.interests.service;

import org.silverpeas.core.exception.SilverpeasRuntimeException;

public class InterestsRuntimeException extends SilverpeasRuntimeException {

  private static final long serialVersionUID = -8453982114851005023L;

  public InterestsRuntimeException(String callingClass, String message) {
    super(callingClass, ERROR, message);
  }

  public InterestsRuntimeException(String callingClass, String message,
      String extraParams) {
    super(callingClass, ERROR, message, extraParams);
  }

  public InterestsRuntimeException(String callingClass, String message,
      Exception nested) {
    super(callingClass, ERROR, message, nested);
  }

  public InterestsRuntimeException(String callingClass, String message,
      String extraParams, Exception nested) {
    super(callingClass, ERROR, message, extraParams, nested);
  }

  public String getModule() {
    return "Interests";
  }
}
