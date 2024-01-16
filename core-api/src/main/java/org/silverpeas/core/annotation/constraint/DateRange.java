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
package org.silverpeas.core.annotation.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Annotation for JSR-303 validator. It validates the range of dates/date times are coherent:
 * </p>
 * <ul>
 *   <li>In the case of date: the start date must be either before or equal the end date</li>
 *   <li>In the case of datetime: the start datetime must be before the end datetime</li>
 * </ul>
 * It is expected that the dates/date times are {@link java.time.temporal.Temporal} objects of the
 * same concrete type.
 */
@Target( { TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface DateRange {

  String message() default "Either the end and start date aren't of the same type or the end date" +
      " isn't after the start date";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * The name of the field representing the temporal starting a range.
   * @return the field name of the range start temporal.
   */
  String start();

  /**
   * The name of the field representing the temporal ending a range.
   * @return the field name of the range end temporal.
   */
  String end();
}
