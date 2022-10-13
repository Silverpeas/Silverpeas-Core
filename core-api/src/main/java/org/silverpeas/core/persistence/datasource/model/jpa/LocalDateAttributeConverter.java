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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.model.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MAX_DATE;
import static org.silverpeas.core.persistence.datasource.SQLDateTimeConstants.MIN_DATE;

/**
 * A converter of {@link LocalDate} and {@link Date} to take into account {@link LocalDate#MIN} and
 * {@link LocalDate#MAX} as a workaround of the Hibernate limitation with the Java Time API:
 * <a href="https://hibernate.atlassian.net/browse/HHH-13482">bug HHH-13482</a>
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
    if (sqlDate.toLocalDate().equals(MIN_DATE.toLocalDate())) {
      return LocalDate.MIN;
    }
    if (sqlDate.toLocalDate().equals(MAX_DATE.toLocalDate())) {
      return LocalDate.MAX;
    }
    return sqlDate.toLocalDate();
  }
}
