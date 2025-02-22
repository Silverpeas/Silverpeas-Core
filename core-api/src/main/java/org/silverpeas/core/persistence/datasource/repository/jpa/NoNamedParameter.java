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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.TemporalType;

/**
 * This class permits to indicate empty parameters to JPA repository framework methods that
 * performed JPA queries.
 * @author Yohann Chastagnier
 */
public class NoNamedParameter extends NamedParameters {

  @Override
  public NamedParameters add(final String name, final Object value) {
    throw new NotImplementedException("No parameter is handled");
  }

  @Override
  public NamedParameters add(final String name, final Object value,
      final TemporalType temporalType) {
    throw new NotImplementedException("No parameter is handled");
  }
}
