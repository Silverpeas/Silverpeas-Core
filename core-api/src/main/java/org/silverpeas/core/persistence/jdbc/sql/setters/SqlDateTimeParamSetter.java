/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.core.date.DateTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * A setter of SQL parameters of type datetime that includes the following types {@link DateTime},
 * {@link Instant}, {@link java.time.LocalDate}, {@link OffsetDateTime} and {@link ZonedDateTime}.
 * @author mmoquillon
 */
class SqlDateTimeParamSetter extends SqlTemporalParamSetter {

  @Override
  public List<Class<?>> getSupportedTypes() {
    return Arrays.asList(Timestamp.class, DateTime.class, Instant.class, LocalDateTime.class,
        OffsetDateTime.class, ZonedDateTime.class);
  }

  @Override
  public void setParameter(final PreparedStatement statement, final int idx, final Object value)
      throws SQLException {
    if (value instanceof Timestamp) {
      statement.setTimestamp(idx, (Timestamp) value);
    } else if (isADateTime(value)) {
      statement.setTimestamp(idx, new Timestamp(toInstant(value).toEpochMilli()));
    } else {
      throwTypeNotSupported(value.getClass());
    }
  }

  private boolean isADateTime(final Object parameter) {
    if (parameter instanceof DateTime) {
      return true;
    }
    if (parameter instanceof Instant) {
      return true;
    }
    if (parameter instanceof LocalDateTime) {
      return true;
    }
    if (parameter instanceof OffsetDateTime) {
      return true;
    }
    return parameter instanceof ZonedDateTime;
  }
}
  