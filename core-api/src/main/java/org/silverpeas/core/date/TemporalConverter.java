/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A converter of date and datetime into different temporal types used in Silverpeas. It provides
 * methods to perform operations against temporal objects according their concrete type; the
 * temporal objects are then converted before passing them to the matching function. It provides
 * also convenient methods to convert a temporal, whatever its type, to another type like a {@link
 * LocalDate} or an {@link OffsetDateTime}. It provides also methods dedicated to convert a string
 * representation of a date or a datetime to its corresponding temporal instance and vice-versa.
 * @author mmoquillon
 */
public class TemporalConverter {

  private TemporalConverter() {
  }

  /**
   * Performs one of the specified conversion function against the given temporal object according
   * to its concrete type. The result of the conversion is then returned.
   * @param temporal a {@link Temporal} object to convert.
   * @param conversions one or more conversion functions.
   * @param <T> the concrete type of the converted value.
   * @return the value obtained by the application of the correct conversion function to the given
   * {@link Temporal} object.
   * @throws IllegalArgumentException exception if no conversion functions are passed or
   * the concrete type of the {@link Temporal} object isn't accepted by any of the conversion
   * functions.
   */
  @SafeVarargs
  public static <T> T applyByType(Temporal temporal,
      Conversion<? extends Temporal, T>... conversions) {
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(conversions);
    if (conversions.length == 0) {
      throw new IllegalArgumentException("Expected at least one conversion function");
    }
    return Stream.of(conversions)
        .filter(c -> c.accepts(temporal))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Temporal parameter isn't of the expected type: " + Stream.of(conversions)
                .map(Conversion::getAcceptedType)
                .collect(Collectors.joining())))
        .apply(temporal);
  }

  /**
   * Performs one of the specified consumer according to the type of the specified temporal object.
   * The given temporal object is converted before passing it to one of the specified consumers.
   * @param temporal a temporal object to consume.
   * @param dateConsumer a function that consumes a {@link LocalDate} instance.
   * @param dateTimeConsumer a function that consumers a {@link OffsetDateTime} instance.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or {@link
   * OffsetDateTime} instances.
   */
  public static void consumeByType(Temporal temporal,
      Consumer<LocalDate> dateConsumer, Consumer<OffsetDateTime> dateTimeConsumer) {
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(dateTimeConsumer);
    Objects.requireNonNull(dateConsumer);
    applyByType(temporal, Conversion.of(LocalDate.class, date -> {
      dateConsumer.accept(date);
      return null;
    }), Conversion.of(OffsetDateTime.class, dateTime -> {
      dateTimeConsumer.accept(dateTime);
      return null;
    }));
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
   * <li>{@link Instant}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link OffsetDateTime} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a {@link LocalDate} instance then the
   * date is converted into an {@link OffsetDateTime} instance by taking the start of the day in
   * UTC/Greenwich. If the temporal is a {@link LocalDateTime} instance then the time is converted
   * in UTC/Greenwich. If the temporal is an {@link Instant} then it is converted into an
   * {@link OffsetDateTime} instance with the zone offset at UTC/Greenwich.
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link OffsetDateTime} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by this
   * converter.
   */
  public static OffsetDateTime asOffsetDateTime(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        Conversion.of(LocalDate.class, t -> t.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()),
        Conversion.of(LocalDateTime.class, t -> t.atOffset(ZoneOffset.UTC)),
        Conversion.of(OffsetDateTime.class, Function.identity()),
        Conversion.of(ZonedDateTime.class, ZonedDateTime::toOffsetDateTime),
        Conversion.of(Instant.class, t -> t.atOffset(ZoneOffset.UTC)));
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
   * <li>{@link Instant}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already a {@link ZonedDateTime} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a {@link LocalDate} instance then the
   * date is converted into a {@link ZonedDateTime} instance by taking the start of the day in
   * UTC/Greenwich. If the temporal is a {@link LocalDateTime} instance then the time is converted
   * in UTC/Greenwich. If the temporal is an {@link Instant} then it is converted into a
   * {@link ZonedDateTime} instance with the zone Id at UTC/Greenwich.
   * </p>
   * @param temporal the temporal to convert.
   * @return a {@link ZonedDateTime} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by this
   * converter.
   */
  public static ZonedDateTime asZonedDateTime(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        Conversion.of(LocalDate.class, t -> t.atStartOfDay(ZoneOffset.UTC)),
        Conversion.of(LocalDateTime.class, t -> t.atZone(ZoneId.of(ZoneOffset.UTC.getId()))),
        Conversion.of(OffsetDateTime.class, OffsetDateTime::toZonedDateTime),
        Conversion.of(ZonedDateTime.class, Function.identity()),
        Conversion.of(Instant.class, t -> t.atZone(ZoneOffset.UTC)));
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
   * <li>{@link Instant}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link LocalDate} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a datetime, then only the date is
   * returned. If the temporal is an {@link Instant} then the time part is set at UTC/Greenwich
   * before returning only the date part as a {@link LocalDate} instance.
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link LocalDate} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by this
   * converter.
   */
  public static LocalDate asLocalDate(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        Conversion.of(LocalDate.class, Function.identity()),
        Conversion.of(LocalDateTime.class, LocalDateTime::toLocalDate),
        Conversion.of(OffsetDateTime.class, OffsetDateTime::toLocalDate),
        Conversion.of(ZonedDateTime.class, ZonedDateTime::toLocalDate),
        Conversion.of(Instant.class, t -> LocalDate.ofInstant(t, ZoneOffset.UTC)));
  }

  /**
   * <p>
   * Converts the specified temporal instance to an {@link LocalDate} instance after applying the
   * timezone conversion. The temporal instance must be of one of the following type:</p>
   * <ul>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link Instant}</li>
   * </ul>
   * <p>
   * Any other types aren't supported and as such an {@link IllegalArgumentException} is thrown.
   * </p>
   * <p>
   * If the temporal is already an {@link LocalDate} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a datetime, then it is converted to the
   * specified timezone for a same instant before returning only the date (the day can change once
   * the timezone applied onto the time).
   * </p>
   * @param temporal the temporal to convert.
   * @return an {@link LocalDate} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by this
   * converter.
   */
  public static LocalDate asLocalDate(final Temporal temporal, final ZoneId zoneId) {
    Objects.requireNonNull(temporal);
    return TemporalConverter.applyByType(temporal,
        Conversion.of(LocalDate.class, Function.identity()),
        Conversion.of(LocalDateTime.class, t ->
            t.atZone(ZoneId.of(ZoneOffset.UTC.getId()))
                .withZoneSameInstant(zoneId)
                .toLocalDate()),
        Conversion.of(OffsetDateTime.class, t -> t.atZoneSameInstant(zoneId).toLocalDate()),
        Conversion.of(ZonedDateTime.class, t -> t.withZoneSameInstant(zoneId).toLocalDate()),
        Conversion.of(Instant.class, t -> LocalDate.ofInstant(t,zoneId)));
  }

  /**
   * Converts the specified temporal in an {@link Instant} instance. If the temporal is a date then
   * it is expressed as a datetime in UTC/Greenwich. If the temporal is a local datetime, then the
   * time is expressed in UTC/Greenwich.
   * @param temporal the temporal to convert.
   * @return an {@link Instant} instance.
   */
  public static Instant asInstant(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    return applyByType(temporal, LOCAL_DATE_TO_INSTANT, LOCAL_DATE_TIME_TO_INSTANT,
        OFFSET_DATE_TIME_TO_INSTANT, ZONED_DATE_TIME_TO_INSTANT, INSTANT_TO_INSTANT);
  }

  /**
   * Converts the specified temporal in a Date instance. The {@link Date} instance is created from
   * the number of milliseconds of the temporal from the epoch of 1970-01-01T00:00:00Z.
   * <p>
   * Before getting the number of milliseconds from the epoch of 1970-01-01T00:00:00Z, some timezone
   * conversions can be required. If the temporal is a date then it is converted into a datetime
   * in UTC/Greenwich. If the temporal is a local datetime, therefore the time is considered
   * expressed in the default timezone and hence the time is converted in UTC/Greenwich. For any
   * others temporal value, their timezone is taken into account to convert the time in
   * UTC/Greenwich.
   * </p>
   * @param temporal a date or a datetime.
   * @return a {@link Date} instance.
   */
  public static Date asDate(final Temporal temporal) {
    Objects.requireNonNull(temporal);
    Instant instant = applyByType(temporal, LOCAL_DATE_TO_INSTANT, LOCAL_DATE_TIME_TO_INSTANT,
        OFFSET_DATE_TIME_TO_INSTANT, ZONED_DATE_TIME_TO_INSTANT, INSTANT_TO_INSTANT);
    return Date.from(instant);
  }

  private static final Conversion<Instant, Instant> INSTANT_TO_INSTANT =
      Conversion.of(Instant.class, t -> {
        if (t.equals(Instant.MIN)) {
          return Instant.MIN;
        }
        if (t.equals(Instant.MAX)) {
          return Instant.MAX;
        }
        return t;
      });

  private static final Conversion<LocalDate, Instant> LOCAL_DATE_TO_INSTANT =
      Conversion.of(LocalDate.class, t -> {
        if (t.equals(LocalDate.MIN)) {
          return Instant.MIN;
        }
        if (t.equals(LocalDate.MAX)) {
          return Instant.MAX;
        }
        return t.atStartOfDay(ZoneOffset.UTC).toInstant();
      });

  private static final Conversion<LocalDateTime, Instant> LOCAL_DATE_TIME_TO_INSTANT =
      Conversion.of(LocalDateTime.class, t -> {
        if (t.equals(LocalDateTime.MIN)) {
          return Instant.MIN;
        }
        if (t.equals(LocalDateTime.MAX)) {
          return Instant.MAX;
        }
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(t);
        return t.toInstant(offset);
      });

  private static final Conversion<OffsetDateTime, Instant> OFFSET_DATE_TIME_TO_INSTANT =
      Conversion.of(OffsetDateTime.class, t -> {
        if (t.equals(OffsetDateTime.MIN)) {
          return Instant.MIN;
        }
        if (t.equals(OffsetDateTime.MAX)) {
          return Instant.MAX;
        }
        return t.toInstant();
      });

  private static final Conversion<ZonedDateTime, Instant> ZONED_DATE_TIME_TO_INSTANT =
      Conversion.of(ZonedDateTime.class, t -> {
        if (t.toOffsetDateTime().equals(OffsetDateTime.MIN)) {
          return Instant.MIN;
        }
        if (t.toOffsetDateTime().equals(OffsetDateTime.MAX)) {
          return Instant.MAX;
        }
        return t.toInstant();
      });

  /**
   * A conversion function of a temporal object into a value of type R.
   * @param <T> the concrete type of the temporal object this conversion function accepts.
   * @param <R> the concrete type of the converted value.
   */
  public static class Conversion<T extends Temporal, R> {

    private final Class<T> type;
    private final Function<T, R> converter;

    /**
     * Constructs a conversion function that will apply to the specified concrete type of
     * {@link Temporal} objects and that will return a value of the specified concrete type R.
     * @param type the expected concrete type of the {@link Temporal} objects to convert.
     * @param converter the temporal conversion implementation.
     * @param <T> the accepted {@link Temporal} concrete type.
     * @param <R> the type to which the temporal objects will be converted.
     * @return a {@link Conversion} instance.
     */
    public static <T extends Temporal, R> Conversion<T, R> of(Class<T> type,
        Function<T, R> converter) {
      return new Conversion<>(type, converter);
    }

    private Conversion(Class<T> type, Function<T, R> converter) {
      this.type = type;
      this.converter = converter;
    }

    /**
     * Does this conversion function accepts to the type of the specified temporal object.
     * @param temporal a {@link Temporal} object.
     * @return true if this function known how to convert the concrete type of the given
     * {@link Temporal} object to a value of type R. False otherwise.
     */
    public boolean accepts(Temporal temporal) {
      return type.isAssignableFrom(temporal.getClass());
    }

    /**
     * Applies the conversion to the specified {@link Temporal} object.
     * @param temporal a {@link Temporal} object. Must be of type T otherwise a
     * {@link ClassCastException} is thrown.
     * @return a value of type R, issued from the conversion of the {@link Temporal} object.
     */
    public R apply(Temporal temporal) {
      return converter.apply(type.cast(temporal));
    }

    /**
     * Gets the simple name of the concrete type of {@link Temporal} objects this conversion
     * function accepts as argument.
     * @return the simple name of the {@link Temporal} class accepted by this conversion function.
     */
    public String getAcceptedType() {
      return type.getSimpleName();
    }
  }
}
