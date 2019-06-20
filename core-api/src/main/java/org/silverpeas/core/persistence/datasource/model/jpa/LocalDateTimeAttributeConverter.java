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
import java.time.LocalDateTime;

import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants
    .MAX_TIMESTAMP;
import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants
    .MIN_TIMESTAMP;

/**
 * An automatic converter of {@link LocalDateTime} values to SQL {@link Timestamp} values for
 * JPA 2.1 (JPA 2.1 was release before Java 8 and hence it doesn't support yet the new java time
 * API).
 * @author mmoquillon
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements
    AttributeConverter<LocalDateTime, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(LocalDateTime locDateTime) {
    if (locDateTime == null) {
      return null;
    }
    if (locDateTime.equals(LocalDateTime.MIN)) {
      return MIN_TIMESTAMP;
    }
    if (locDateTime.equals(LocalDateTime.MAX)) {
      return MAX_TIMESTAMP;
    }
    return Timestamp.valueOf(locDateTime);
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
    if (sqlTimestamp == null) {
      return null;
    }
    if (sqlTimestamp.equals(MIN_TIMESTAMP)) {
      return LocalDateTime.MIN;
    } else if (sqlTimestamp.equals(MAX_TIMESTAMP)) {
      return LocalDateTime.MAX;
    }
    return sqlTimestamp.toLocalDateTime();
  }
}
