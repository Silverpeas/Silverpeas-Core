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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc.sql.setters;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A setter of SQL parameters of type date that includes java.sql.{@link java.sql.Date},
 * {@link java.util.Date} and {@link LocalDate}.
 * @author mmoquillon
 */
@Technical
@Bean
public class SqlDateParamSetter extends SqlTemporalParamSetter {

  @Override
  public List<Class<?>> getSupportedTypes() {
    return Arrays.asList(java.sql.Date.class, Date.class, LocalDate.class);
  }

  @Override
  public void setParameter(final PreparedStatement statement, final int idx, final Object value)
      throws SQLException {
    if (value instanceof java.sql.Date) {
      statement.setDate(idx, (java.sql.Date) value);
    } else if (isADate(value)) {
      statement.setDate(idx,
          new java.sql.Date(toInstant(value).toEpochMilli()));
    } else {
      throwTypeNotSupported(value.getClass());
    }
  }

  private boolean isADate(final Object parameter) {
    return parameter instanceof Date || parameter instanceof LocalDate;
  }
}
  