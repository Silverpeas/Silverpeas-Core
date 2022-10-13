/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.pdc.model.PdcException;

/**
 * An exception that is thrown when a position onto a PdC cannot be deleted within a given context
 * (for example, the PdC contains some mandatory axis and the position is the only one in the
 * classification of a resource onto the PdC).
 */
public class PdcPositionDeletionException extends PdcException {

  private static final long serialVersionUID = 6397066904320622572L;

  public PdcPositionDeletionException(final String message, final String... parameters) {
    super(message, parameters);
  }

  public PdcPositionDeletionException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public PdcPositionDeletionException(final Throwable cause) {
    super(cause);
  }
}
