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
 * "http://www.silverpeas.org/legal/licensing"
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static org.silverpeas.core.date.TemporalConverter.asZonedDateTime;
import static org.silverpeas.core.util.ResourceLocator.getLocalizationBundle;

/**
 * A formatter of date and datetime in String into different formats, both localized and ISO.
 * It provides both methods to perform formatting of temporal objects in text and methods to
 * perform formatting of date/datetime text representation into their temporal counterpart.
 * @author mmoquillon
 */
public class TemporalFormatter {

  private static final String DATE_BUNDLE = "org.silverpeas.util.date.multilang.date";

  private TemporalFormatter() {
  }

  /**
   * <p>
   * Formatter enough flexible to parse and format any kind of temporal among the followings:
   * </p>
   * <ul>
   * <li>{@link ZonedDateTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link LocalDate}</li>
   * </ul>
   */
  private static final DateTimeFormatter flexibleFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm:ss.n[XXX]['['VV']']]");

  /**
   * <p>
   * Formats the specified ISO-8601 representation of a date/datetime into its corresponding
   * {@link java.time.temporal.Temporal} object.
   * @param iso8601DateTime an ISO-8601 representation of a date or of a datetime.
   * @param strict should be the parsing of the specified string strict? If true, a
   * {@link DateTimeParseException} exception will be thrown if the string isn't correctly
   * formatted. Otherwise null is simply returned.
   * @return the {@link java.time.temporal.Temporal} instance decoded from the specified string.
   * According to the date/datetime representation, the following temporal is returned:
   * </p>
   * <ul>
   * <li>a datetime with an offset indication: returns a {@link OffsetDateTime} instance</li>
   * <li>a datetime with a Zone id indication: returns a {@link ZonedDateTime} instance</li>
   * <li>a datetime without any offset nor zone id indication: returns a {@link LocalDateTime}
   * instance</li>
   * <li>a date: returns a {@link LocalDate} instance</li>
   * </ul>
   */
  public static java.time.temporal.Temporal toTemporal(final String iso8601DateTime,
      final boolean strict) {
    Objects.requireNonNull(iso8601DateTime);
    try {
      return (Temporal) flexibleFormatter.parseBest(iso8601DateTime, ZonedDateTime::from,
          OffsetDateTime::from, LocalDateTime::from, LocalDate::from);
    } catch (DateTimeParseException e) {
      if (strict) {
        throw e;
      }
      return null;
    }
  }

  /**
   * <p>
   * Formats the specified temporal into an ISO-8601 string representation. The rules of formatting
   * are based upon the {@link DateTimeFormatter} that fully follows the ISO-8601 specification.
   * </p>
   * <p>
   * If the temporal is a date, then the ISO-8601 representation will be of an ISO local date.
   * If the temporal is a local datetime, it will be converted without any Zone Offset indication,
   * otherwise the offset will be printed out.
   * </p>
   * <p>
   * Examples:
   * <pre>
   *   {@code TemporalConverter.toIso8601(OffsetDateTime.now(), false)}
   *   {@code Result: 2018-03-13T15:11+01:00}
   *
   *   {@code TemporalConverter.toIso8601(OffsetDateTime.now(), true)}
   *   {@code Result: 2018-03-13T15:11:14.971+01:00}
   *
   *   {@code TemporalConverter.toIso8601(LocalDateTime.now(), false)}
   *   {@code Result: 2018-03-13T15:11}
   *
   *   {@code TemporalConverter.toIso8601(LocalDateTime.now(), true)}
   *   {@code Result: 2018-03-13T15:11:14.971}
   *
   *   {@code TemporalConverter.toIso8601(LocalDate, false)}
   *   {@code Result: 2018-03-13}
   *
   *   {@code TemporalConverter.toIso8601(LocalDate, true)}
   *   {@code Result: 2018-03-13}
   * </pre>
   * </p>
   * @param temporal a {@link Temporal} object to convert.
   * @param withSeconds if in the ISO-8601 string the seconds have to be represented (seconds
   * following by nanoseconds can be optional an part according to the ISO 8601 specification).
   * For displaying, usually the seconds aren't meaningful whereas for the datetime transport from
   * one source to a target point this can be important.
   * @return an ISO-8601 string representation of the temporal.
   */
  public static String toIso8601(final Temporal temporal, final boolean withSeconds) {
    Objects.requireNonNull(temporal);

    if (!temporal.isSupported(ChronoUnit.HOURS)) {
      return DateTimeFormatter.ISO_LOCAL_DATE.format(temporal);
    }

    if (withSeconds) {
      if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(temporal);
      }
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(temporal);
    }
    final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2);
    if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
      builder.appendOffsetId();
    }
    return builder.toFormatter().format(temporal);
  }

  /**
   * <p>
   * Formats the specified temporal into a string representation that conforms to the l10n rules of
   * the country/language identified by the given ISO 632-1 locale code. The rules of formatting
   * are based upon the {@link DateTimeFormatter} that fully follows the l10n rules of several
   * country/language calendar systems.
   * </p>
   * <p>
   * If the temporal is a date, then the localized representation will be of a local date.
   * If the temporal is a local datetime, then the localized representation will be of a localized
   * date + localized time. The seconds and nanoseconds aren't printed out as localized datetime
   * are meaningful only for displaying usage (for transport, the ISO-8601 is always used).
   * </p>
   * <p>
   * Examples:
   * <pre>
   *   {@code TemporalConverter.toLocalized(OffsetDateTime.now(), "fr")}
   *   {@code Result: 13/03/2018 15:11}
   *
   *   {@code TemporalConverter.toLocalized(LocalDate.now(), "fr")}
   *   {@code Result: 13/03/2018}
   * </pre>
   * </p>
   * @param temporal a {@link Temporal} object to convert.
   * @param language an ISO 632-1 language code.
   * @return a localized string representation of the temporal. The localized representation is
   * based upon the l10n standard rules for the specified ISO 632-1 code.
   */
  public static String toLocalized(final Temporal temporal, final String language) {
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(language);
    final String pattern;
    if (temporal.isSupported(ChronoUnit.HOURS)) {
      pattern = getDateTimePattern(language);
    } else {
      pattern = getDatePattern(language);
    }
    return DateTimeFormatter.ofPattern(pattern).format(temporal);
  }

  /**
   * @see #toLocalized(Temporal, String)
   * @return only time part if time exists, empty otherwise.
   */
  public static String toLocalizedTime(final Temporal temporal, final String language) {
    Objects.requireNonNull(temporal);
    Objects.requireNonNull(language);
    final String result;
    if (temporal.isSupported(ChronoUnit.HOURS)) {
      result = DateTimeFormatter.ofPattern(getTimePattern(language)).format(temporal);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * <p>
   * Formats the specified temporal, for the specified zone identifier, into a string representation
   * that conforms to the l10n rules of the country/language identified by the given ISO 632-1
   * locale code. The rules of formatting are based upon the {@link DateTimeFormatter} that fully
   * follows the l10n rules of several country/language calendar systems. If the zone id of the
   * temporal differs from the specified one, then the zone id of the temporal will be also
   * formatted and represented into the returned result. Otherwise, this method will behave as the
   * {@link #toLocalized(Temporal, String)} one.
   * </p>
   * <p>
   * This method is useful to format temporal expressed in a given timezone in its localized
   * representation and for which the renderer is on a possible different timezone.
   * </p>
   * <p>
   * If the temporal is a date, then the localized representation will be of a local date.
   * If the temporal is a local datetime, then the localized representation will be of a localized
   * date + localized time.
   * If the temporal is in another timezone than the specified one, then the localized
   * representation of the temporal will be expressed with its timezone. For datetime, the
   * seconds and nanoseconds aren't represented as localized datetime are meaningful only for
   * displaying usage (for transport, the ISO-8601 is always used).
   * </p>
   * <p>
   * Examples:
   * <pre>
   *   {@code TemporalConverter.toLocalized(OffsetDateTime.now(ZoneId.of("Europe/Paris")),
   *   ZoneId.of("America/Cancun"), "fr")}
   *   {@code Result: 13/03/2018 09:11 (Europe/Paris)}
   *
   *   {@code TemporalConverter.toLocalized(OffsetDateTime.now(ZoneId.of("America/Cancun")),
   *   ZoneId.of("America/Cancun"), "fr")}
   *   {@code Result: 13/03/2018 09:11}
   * </pre>
   * </p>
   * @param temporal a {@link Temporal} object to convert.
   * @param zoneId the zone id of the renderer or any objects that will handle the localized
   * representation of the temporal.
   * @param language an ISO 632-1 language code.
   * @return a localized string representation of the temporal. The localized representation is
   * based upon the l10n standard rules for the specified ISO 632-1 code.
   */
  public static String toLocalizedDate(final Temporal temporal, final ZoneId zoneId,
      final String language) {
    final LocalDate localDate = TemporalConverter.asLocalDate(temporal, zoneId);
    return toLocalized(localDate, language);
  }

  /**
   * <p>
   * Formats the specified temporal, for the specified zone identifier, into a string representation
   * that conforms to the l10n rules of the country/language identified by the given ISO 632-1
   * locale code. The rules of formatting are based upon the {@link DateTimeFormatter} that fully
   * follows the l10n rules of several country/language calendar systems. If the zone id of the
   * temporal differs from the specified one, then the zone id of the temporal will be also
   * formatted and represented into the returned result. Otherwise, this method will behave as the
   * {@link #toLocalized(Temporal, String)} one.
   * </p>
   * <p>
   * This method is useful to format temporal expressed in a given timezone in its localized
   * representation and for which the renderer is on a possible different timezone.
   * </p>
   * <p>
   * If the temporal is a date, then the localized representation will be of a local date.
   * If the temporal is a local datetime, then the localized representation will be of a localized
   * date + localized time.
   * If the temporal is in another timezone than the specified one, then the localized
   * representation of the temporal will be expressed with its timezone. For datetime, the
   * seconds and nanoseconds aren't represented as localized datetime are meaningful only for
   * displaying usage (for transport, the ISO-8601 is always used).
   * </p>
   * <p>
   * Examples:
   * <pre>
   *   {@code TemporalConverter.toLocalized(OffsetDateTime.now(ZoneId.of("Europe/Paris")),
   *   ZoneId.of("America/Cancun"), "fr")}
   *   {@code Result: 13/03/2018 09:11 (Europe/Paris)}
   *
   *   {@code TemporalConverter.toLocalized(OffsetDateTime.now(ZoneId.of("America/Cancun")),
   *   ZoneId.of("America/Cancun"), "fr")}
   *   {@code Result: 13/03/2018 09:11}
   * </pre>
   * </p>
   * @param temporal a {@link Temporal} object to convert.
   * @param zoneId the zone id of the renderer or any objects that will handle the localized
   * representation of the temporal.
   * @param language an ISO 632-1 language code.
   * @return a localized string representation of the temporal. The localized representation is
   * based upon the l10n standard rules for the specified ISO 632-1 code.
   */
  public static String toLocalized(final Temporal temporal, final ZoneId zoneId,
      final String language) {
    if (temporal.isSupported(ChronoUnit.HOURS) && temporal.get(ChronoField.OFFSET_SECONDS) !=
        ZonedDateTime.now(zoneId).get(ChronoField.OFFSET_SECONDS)) {
      final ZoneId actualZoneId = asZonedDateTime(temporal).getZone();
      final String pattern = getDateTimePattern(language);
      return toZonedFormat(pattern, temporal, actualZoneId);
    } else {
      return toLocalized(temporal, language);
    }
  }

  /**
   * @see #toLocalized(Temporal, ZoneId, String)
   * @return only time part if time exists, empty otherwise.
   */
  public static String toLocalizedTime(final Temporal temporal, final ZoneId zoneId,
      final String language) {
    if (temporal.isSupported(ChronoUnit.HOURS) && temporal.get(ChronoField.OFFSET_SECONDS) !=
        ZonedDateTime.now(zoneId).get(ChronoField.OFFSET_SECONDS)) {
      final ZoneId actualZoneId = asZonedDateTime(temporal).getZone();
      final String pattern = getTimePattern(language);
      return toZonedFormat(pattern, temporal, actualZoneId);
    } else {
      return toLocalizedTime(temporal, language);
    }
  }

  private static String toZonedFormat(final String pattern, final Temporal temporal,
      final ZoneId actualZoneId) {
    return new DateTimeFormatterBuilder().appendPattern(pattern).appendLiteral(" (")
        .parseCaseSensitive().appendZoneOrOffsetId().appendLiteral(")").toFormatter()
        .withZone(actualZoneId).format(temporal);
  }

  /**
   * Gets the date pattern according to given locale.
   * @param locale the ISO 631-1 locale.
   * @return the pattern as string.
   */
  private static String getDatePattern(final String locale) {
    return getLocalizationBundle(DATE_BUNDLE, locale).getString("dateOutputFormat");
  }

  /**
   * Gets the time pattern according to given locale.
   * @param locale the ISO 631-1 locale.
   * @return the pattern as string.
   */
  private static String getTimePattern(final String locale) {
    return getLocalizationBundle(DATE_BUNDLE, locale).getString("hourOutputFormat");
  }

  /**
   * Gets the date time pattern according to given locale.
   * @param locale the ISO 631-1 locale.
   * @return the pattern as string.
   */
  private static String getDateTimePattern(final String locale) {
    return getLocalizationBundle(DATE_BUNDLE, locale).getString("dateTimeOutputFormat");
  }
}
  