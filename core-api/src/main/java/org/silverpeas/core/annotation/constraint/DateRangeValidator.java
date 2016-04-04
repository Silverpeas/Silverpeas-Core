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
package org.silverpeas.core.annotation.constraint;

import org.silverpeas.core.date.Datable;
import org.silverpeas.core.date.Date;
import org.silverpeas.core.date.DateTime;
import java.lang.reflect.Field;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
    this.startDateFieldName = constraintAnnotation.startDate();
    this.endDateFieldName = constraintAnnotation.endDate();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    boolean isValid;
    try {
      Field startDateField = object.getClass().getDeclaredField(startDateFieldName);
      Field endDateField = object.getClass().getDeclaredField(endDateFieldName);
      startDateField.setAccessible(true);
      endDateField.setAccessible(true);
      Datable<?> startDate = (Datable<?>) startDateField.get(object);
      Datable<?> endDate = (Datable<?>) endDateField.get(object);
      if (startDate instanceof Date || endDate instanceof Date) {
        Date start = new Date(startDate.asDate());
        Date end = new Date(endDate.asDate());
        isValid = start.isBefore(end) || start.isEqualTo(end);
      } else {
        DateTime start = (DateTime) startDate;
        DateTime end = (DateTime) endDate;
        isValid = start.isBefore(end) || start.isEqualTo(end);
      }
    } catch (Exception ex) {
      isValid = false;
    }
    return isValid;
  }
}
