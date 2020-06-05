/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc.sql.setters;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A setter of typed parameters of a {@link PreparedStatement} instance.
 * @author mmoquillon
 */
public interface SqlTypedParameterSetter {

  /**
   * Specifies the concrete types of the parameter this setter supports.
   * @return a list of supported parameter types.
   */
  List<Class<?>> getSupportedTypes();

  /**
   * Sets the nth parameter of the specified prepared statement with the given value.
   * @param statement the prepared statement.
   * @param idx the index of the parameter to set in the prepared statement.
   * @param value the value with which the parameter has to be set.
   * @throws SQLException if an error occurs while setting the parameter.
   */
  void setParameter(final PreparedStatement statement, int idx, final Object value)
      throws SQLException;
}
