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
package org.silverpeas.core.persistence.datasource.model.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MAX_TIMESTAMP;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MIN_TIMESTAMP;

/**
 * A converter of {@link Instant} and {@link Timestamp} to take into account {@link Instant#MIN}
 * and {@link Instant#MAX} as a workaround of the Hibernate limitation with the Java Time API:
 * <a href="https://hibernate.atlassian.net/browse/HHH-13482">bug HHH-13482</a>
 * @author mmoquillon
 */
@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(Instant instant) {
    if (instant == null) {
      return null;
    }
    if (instant.equals(Instant.MIN)) {
      return MIN_TIMESTAMP;
    }
    if (instant.equals(Instant.MAX)) {
      return MAX_TIMESTAMP;
    }

    return Timestamp.valueOf(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
        .toLocalDateTime());
  }

  @Override
  public Instant convertToEntityAttribute(Timestamp sqlTimestamp) {
    if (sqlTimestamp == null) {
      return null;
    }
    if (sqlTimestamp.equals(MIN_TIMESTAMP)) {
      return Instant.MIN;
    }
    if (sqlTimestamp.equals(MAX_TIMESTAMP)) {
      return Instant.MAX;
    }
    return sqlTimestamp.toLocalDateTime().atOffset(ZoneOffset.UTC).toInstant();
  }
}
