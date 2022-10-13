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
package org.silverpeas.core.annotation.constraint;

import org.silverpeas.core.date.TemporalConverter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * The validator of a date range constraint.
 */
public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

  /**
   * The field representing the start date in a range.
   */
  private String startDateFieldName;
  /**
   * The field representing the end date in a range.
   */
  private String endDateFieldName;

  @Override
  public void initialize(DateRange constraintAnnotation) {
    this.startDateFieldName = constraintAnnotation.start();
    this.endDateFieldName = constraintAnnotation.end();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    boolean isValid;
    try {
      Field startDateField = object.getClass().getDeclaredField(startDateFieldName);
      Field endDateField = object.getClass().getDeclaredField(endDateFieldName);
      startDateField.setAccessible(true);
      endDateField.setAccessible(true);
      Temporal startDate = (Temporal) startDateField.get(object);
      Temporal endDate = (Temporal) endDateField.get(object);
      isValid = startDate.getClass().equals(endDate.getClass());
      if (isValid) {
        if (startDate instanceof LocalDate && endDate instanceof LocalDate) {
          LocalDate startLocalDate = TemporalConverter.asLocalDate(startDate);
          LocalDate endLocalDate = TemporalConverter.asLocalDate(endDate);
          isValid = startLocalDate.isBefore(endLocalDate) || startLocalDate.isEqual(endLocalDate);
        } else {
          OffsetDateTime startDateTime = TemporalConverter.asOffsetDateTime(startDate);
          OffsetDateTime endDateTime = TemporalConverter.asOffsetDateTime(endDate);
          isValid = startDateTime.isBefore(endDateTime);
        }
      }
    } catch (Exception ex) {
      isValid = false;
    }
    return isValid;
  }
}
