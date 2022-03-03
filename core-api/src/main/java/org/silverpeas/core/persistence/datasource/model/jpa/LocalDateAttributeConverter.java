/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import java.sql.Date;
import java.time.LocalDate;

import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants.MAX_DATE;
import static org.silverpeas.core.persistence.datasource.model.jpa.SQLDateTimeConstants.MIN_DATE;

/**
 * An automatic converter of {@link LocalDate} values to SQL {@link Date} values for
 * JPA 2.1 (JPA 2.1 was release before Java 8 and hence it doesn't support yet the new java time
 * API).
 * @author mmoquillon
 */
@Converter(autoApply = true)
public class LocalDateAttributeConverter implements
    AttributeConverter<LocalDate, Date> {

  @Override
  public Date convertToDatabaseColumn(LocalDate locDate) {
    if (locDate == null) {
      return null;
    }
    if (locDate.equals(LocalDate.MIN)) {
      return MIN_DATE;
    }
    if (locDate.equals(LocalDate.MAX)) {
      return MAX_DATE;
    }
    return Date.valueOf(locDate);
  }

  @Override
  public LocalDate convertToEntityAttribute(Date sqlDate) {
    if (sqlDate == null) {
      return null;
    }
    if (sqlDate.equals(MIN_DATE)) {
      return LocalDate.MIN;
    }
    if (sqlDate.equals(MAX_DATE)) {
      return LocalDate.MAX;
    }
    return sqlDate.toLocalDate();
  }
}
