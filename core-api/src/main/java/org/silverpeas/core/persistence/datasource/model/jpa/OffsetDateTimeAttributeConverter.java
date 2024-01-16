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
package org.silverpeas.core.persistence.datasource.model.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants
    .MAX_TIMESTAMP;
import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants
    .MIN_TIMESTAMP;

/**
 * An automatic converter of {@link java.time.OffsetDateTime} values to SQL {@link Timestamp} values
 * for JPA 2.1 (JPA 2.1 was release before Java 8 and hence it doesn't support yet the new java time
 * API).
 * @author mmoquillon
 */
@Converter(autoApply = true)
public class OffsetDateTimeAttributeConverter
    implements AttributeConverter<OffsetDateTime, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    if (dateTime.equals(OffsetDateTime.MIN)) {
      return MIN_TIMESTAMP;
    }
    if (dateTime.equals(OffsetDateTime.MAX)) {
      return MAX_TIMESTAMP;
    }
    return Timestamp.valueOf(dateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
  }

  @Override
  public OffsetDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
    if (sqlTimestamp == null) {
      return null;
    }
    if (sqlTimestamp.equals(MIN_TIMESTAMP)) {
      return OffsetDateTime.MIN;
    } else if (sqlTimestamp.equals(MAX_TIMESTAMP)) {
      return OffsetDateTime.MAX;
    }
    return  sqlTimestamp.toLocalDateTime().atOffset(ZoneOffset.UTC);
  }
}
