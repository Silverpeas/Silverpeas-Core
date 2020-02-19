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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A converter of date and datetime into different temporal types used in Silverpeas. It provides
 * methods to perform operations against temporal objects according their concrete type; the
 * temporal objects are then converted before passing them to the matching function. It provides
 * also convenient methods to convert a temporal, whatever its type, to another type like a
 * {@link LocalDate} or an {@link OffsetDateTime}. It provides also methods dedicated to convert
 * a string representation of a date or a datetime to its corresponding temporal instance and
 * vice-versa.
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
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(dateFunction);
    Objects.requireNonNull(dateTimeFunction);
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
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(dateFunction);
    Objects.requireNonNull(dateTimeFunction);
    Objects.requireNonNull(zonedDateTimeTFunction);
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

  public static <T> T applyByType(java.time.temporal.Temporal temporal,
      Function<LocalDate, T> localDateFunction, Function<LocalDateTime, T> localDateTimeFunction,
      Function<OffsetDateTime, T> offsetDateTimeFunction,
      Function<ZonedDateTime, T> zonedDateTimeFunction) {
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(localDateFunction);
    Objects.requireNonNull(localDateTimeFunction);
    Objects.requireNonNull(zonedDateTimeFunction);
    Objects.requireNonNull(offsetDateTimeFunction);

    if (temporal instanceof LocalDate) {
      return localDateFunction.apply(LocalDate.from(temporal));
    } else if (temporal instanceof OffsetDateTime) {
      return offsetDateTimeFunction.apply(OffsetDateTime.from(temporal));
    } else if (temporal instanceof ChronoZonedDateTime) {
      return zonedDateTimeFunction.apply(ZonedDateTime.from(temporal));
    } else if (temporal instanceof LocalDateTime) {
      return localDateTimeFunction.apply(LocalDateTime.from(temporal));
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
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(dateTimeConsumer);
    Objects.requireNonNull(dateConsumer);
    applyByType(temporal, date -> {
      dateConsumer.accept(date);
      return null;
    }, dateTime -> {
      dateTimeConsumer.accept(dateTime);
      return null;
    });
  }

  /**
   * <p>
   * Converts the specified temporal instance to an {@link OffsetDateTime} instance. The temporal
   * instance must be of one of the following type:</p>
   * <ul>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link OffsetDateTime} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a {@link LocalDate} instance then the
   * date is converted into an {@link OffsetDateTime} instance by taking the start of the day in
   * UTC/Greenwich. If the temporal is a {@link LocalDateTime} instance then the time is converted
   * in UTC/Greenwich.
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link OffsetDateTime} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by
   * this converter.
   */
  public static OffsetDateTime asOffsetDateTime(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        date -> date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
        localDateTime -> localDateTime.atOffset(ZoneOffset.UTC),
        offsetDateTime -> offsetDateTime,
        ZonedDateTime::toOffsetDateTime);
  }

  /**
   * <p>
   * Converts the specified temporal instance to a {@link ZonedDateTime} instance. The temporal
   * instance must be of one of the following type:</p>
   * <ul>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already a {@link ZonedDateTime} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a {@link LocalDate} instance then the
   * date is converted into a {@link ZonedDateTime} instance by taking the start of the day in
   * UTC/Greenwich. If the temporal is a {@link LocalDateTime} instance then the time is converted
   * in UTC/Greenwich.
   * </p>
   * @param temporal the temporal to convert.
   * @return a {@link ZonedDateTime} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by
   * this converter.
   */
  public static ZonedDateTime asZonedDateTime(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        date -> date.atStartOfDay(ZoneOffset.UTC),
        localDateTime -> localDateTime.atZone(ZoneId.of(ZoneOffset.UTC.getId())),
        OffsetDateTime::toZonedDateTime,
        zonedDateTime -> zonedDateTime);
  }

  /**
   * <p>
   * Converts the specified temporal instance to an {@link LocalDate} instance. The temporal
   * instance must be of one of the following type:</p>
   * <ul>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link LocalDate} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a datetime, then only the date is
   * returned.
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link LocalDate} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by
   * this converter.
   */
  public static LocalDate asLocalDate(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        date -> date,
        LocalDateTime::toLocalDate,
        OffsetDateTime::toLocalDate,
        ZonedDateTime::toLocalDate);
  }

  /**
   * <p>
   * Converts the specified temporal instance to an {@link LocalDate} instance. The temporal
   * instance must be of one of the following type:</p>
   * <ul>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link LocalDate} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a datetime, then only the date is
   * returned.
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link LocalDate} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by
   * this converter.
   */
  public static LocalDate asLocalDate(final Temporal temporal, final ZoneId zoneId) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        d -> d,
        l -> l.atZone(ZoneId.of(ZoneOffset.UTC.getId())).withZoneSameInstant(zoneId).toLocalDate(),
        o -> o.atZoneSameInstant(zoneId).toLocalDate(),
        z -> z.withZoneSameInstant(zoneId).toLocalDate());
  }

  /**
   * Converts the specified temporal in a Date instance. If the temporal is a date then it is
   * converted into a datetime in UTC/Greenwich. If the temporal is a local datetime, then the time
   * is set in UTC/Greenwich.
   * @param temporal a date or a datetime.
   * @return a {@link Date} instance.
   */
  public static Date asDate(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    Long epochMilli =
        applyByType(temporal, localDate2EpochMilli, localDateTime2EpochMilli,
            offsetDateTime2EpochMilli, zonedDateTime2EpochMilli);
    return new Date(epochMilli);
  }

  private static Function<LocalDate, Long> localDate2EpochMilli = t -> {
    if (t.equals(LocalDate.MIN)) {
      return Long.MIN_VALUE;
    }
    if (t.equals(LocalDate.MAX)) {
      return Long.MAX_VALUE;
    }
    return t.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
  };

  private static Function<LocalDateTime, Long> localDateTime2EpochMilli = t -> {
    if (t.equals(LocalDateTime.MIN)) {
      return Long.MIN_VALUE;
    }
    if (t.equals(LocalDateTime.MAX)) {
      return Long.MAX_VALUE;
    }
    return t.toInstant(ZoneOffset.UTC).toEpochMilli();
  };

  private static Function<OffsetDateTime, Long> offsetDateTime2EpochMilli = t -> {
    if (t.equals(OffsetDateTime.MIN)) {
      return Long.MIN_VALUE;
    }
    if (t.equals(OffsetDateTime.MAX)) {
      return Long.MAX_VALUE;
    }
    return t.toInstant().toEpochMilli();
  };

  private static Function<ZonedDateTime, Long> zonedDateTime2EpochMilli = t -> {
    if (t.toOffsetDateTime().equals(OffsetDateTime.MIN)) {
      return Long.MIN_VALUE;
    }
    if (t.toOffsetDateTime().equals(OffsetDateTime.MAX)) {
      return Long.MAX_VALUE;
    }
    return t.toInstant().toEpochMilli();
  };
}
  