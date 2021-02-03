/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jdbc.sql;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Split Processor on discriminant data.
 * @param <D> the type of list of discriminant data.
 * @param <T> the type of the entity into result.
 */
@FunctionalInterface
public interface SplitExecuteProcess<D, T> {

  /**
   * Processes on a slice of entire list of discriminant data.
   * @param discriminantSlice a slice of discriminant data.
   * @param result the result which has to be be filled.
   * @throws SQLException on SQL errors.
   */
  void execute(final Collection<D> discriminantSlice, final Map<D, List<T>> result) throws SQLException;
}
