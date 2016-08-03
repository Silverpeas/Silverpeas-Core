/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
    return (locDateTime == null ? null : Timestamp.valueOf(locDateTime));
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
    return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
  }
}
