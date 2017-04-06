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
package org.silverpeas.core.date;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A converter of date and datetime into different temporal types used in Silverpeas. It provides
 * methods to perform operations against temporal objects according their concrete type; the
 * temporal objects are then converted before passing them to the matching function.
 * @author mmoquillon
 */
public class TemporalConverter {

  private TemporalConverter() {

  }

  /**
   * Performs one of the specified functions according to the type of the specified temporal object.
   * The given temporal object is converted before passing it to one of the specified function.
   * @param temporal a temporal object to consume.
   * @param dateFunction a function that works on a {@link LocalDate} instance.
   * @param dateTimeFunction a function that works on a {@link OffsetDateTime} instance.
   * @param <T> the type of the return value.
   * @return T the return value of the function that was applied to the converted temporal object.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   */
  public static <T> T applyByType(java.time.temporal.Temporal temporal,
      Function<LocalDate, T> dateFunction, Function<OffsetDateTime, T> dateTimeFunction) {
    if (temporal instanceof LocalDate) {
      return dateFunction.apply(LocalDate.from(temporal));
    } else if (temporal instanceof OffsetDateTime) {
      return dateTimeFunction.apply(OffsetDateTime.from(temporal).withOffsetSameInstant(ZoneOffset.UTC));
    } else {
      throw new IllegalArgumentException(
          "Temporal parameters must be both of type LocalDate or OffsetDateTime");
    }
  }

  /**
   * Performs one of the specified functions according to the type of the specified temporal object.
   * The given temporal object is converted before passing it to one of the specified function.
   * @param temporal a temporal object to consume.
   * @param dateFunction a function that works on a {@link LocalDate} instance.
   * @param dateTimeFunction a function that works on a {@link OffsetDateTime} instance.
   * @param zonedDateTimeTFunction a function that works on a {@link ZonedDateTime} instance.
   * @param <T> the type of the return value.
   * @return T the return value of the function that was applied to the converted temporal object.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   */
  public static <T> T applyByType(java.time.temporal.Temporal temporal,
      Function<LocalDate, T> dateFunction, Function<OffsetDateTime, T> dateTimeFunction,
      Function<ZonedDateTime, T> zonedDateTimeTFunction) {
    if (temporal instanceof LocalDate) {
      return dateFunction.apply(LocalDate.from(temporal));
    } else if (temporal instanceof OffsetDateTime) {
      return dateTimeFunction.apply(OffsetDateTime.from(temporal));
    } else if (temporal instanceof ZonedDateTime) {
      return zonedDateTimeTFunction.apply(ZonedDateTime.from(temporal));
    } else {
      throw new IllegalArgumentException(
          "Temporal parameters must be both of type LocalDate or OffsetDateTime or ZonedDateTime");
    }
  }

  /**
   * Performs one of the specified consumer according to the type of the specified temporal object.
   * The given temporal object is converted before passing it to one of the specified consumers.
   * @param temporal a temporal object to consume.
   * @param dateConsumer a function that consumes a {@link LocalDate} instance.
   * @param dateTimeConsumer a function that consumers a {@link OffsetDateTime} instance.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   */
  public static void consumeByType(java.time.temporal.Temporal temporal,
      Consumer<LocalDate> dateConsumer, Consumer<OffsetDateTime> dateTimeConsumer) {
    applyByType(temporal, date -> {
      dateConsumer.accept(date);
      return null;
    }, dateTime -> {
      dateTimeConsumer.accept(dateTime);
      return null;
    });
  }

}
  