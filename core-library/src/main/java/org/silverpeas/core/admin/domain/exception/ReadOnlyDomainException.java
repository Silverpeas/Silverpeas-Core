/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.admin.domain.exception;

import org.silverpeas.core.admin.service.AdminException;

/**
 * Thrown when the modifications of a user cannot be written into the repository of their user
 * domain (an LDAP directory for example) because this repository is accessed in read-only. It is
 * a misconfiguration of the user domain: some data of the users are declared updatable whereas
 * the repository refuses to write them.
 * @author mmoquillon
 */
public class ReadOnlyDomainException extends AdminException {

  private static final long serialVersionUID = -4123906843447219315L;

  public ReadOnlyDomainException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
