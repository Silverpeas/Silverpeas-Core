/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.test.integration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Provider of date and datetime for the integration tests, set at the maximum resolution supported
 * by the Silverpeas datastores.
 * <p>
 * The datastore used in production by Silverpeas is a PostgreSQL database whose {@code TIMESTAMP}
 * type is capped, as stated by the SQL standard, to a <strong>microsecond</strong> resolution: it
 * cannot store a finer precision. Since Java 9, however, the {@code now()} factory methods of the
 * {@link java.time} API carry the resolution of the underlying system clock, which is the
 * nanosecond on most Linux platforms. As a consequence, a datetime obtained from {@code now()} and
 * persisted then reloaded doesn't equal anymore its in-memory counterpart, because the datastore
 * has silently dropped its sub-microsecond part. This makes any integration test comparing such a
 * datetime with its persisted-and-reloaded value fail.
 * </p>
 * <p>
 * This class centralizes that constraint: the datetimes it provides are truncated to the
 * microsecond, so that they survive a persistence round-trip and can be safely compared with the
 * value reloaded from the datastore. Integration tests requiring a current datetime (or having to
 * compare a datetime with its reloaded counterpart) should get it from here instead of directly
 * from the {@link java.time} API.
 * </p>
 * @author mmoquillon
 */
public final class TestDateTimes {

  /**
   * The maximum temporal resolution supported by the Silverpeas datastores (a PostgreSQL
   * {@code TIMESTAMP} is capped to the microsecond).
   */
  public static final ChronoUnit DATASTORE_PRECISION = ChronoUnit.MICROS;

  private TestDateTimes() {
  }

  /**
   * The current datetime, at the offset of the system, set at the maximum resolution supported by
   * the Silverpeas datastores.
   * @return the current {@link OffsetDateTime} truncated to the microsecond.
   */
  public static OffsetDateTime now() {
    return atDatastorePrecision(OffsetDateTime.now());
  }

  /**
   * The current instant, set at the maximum resolution supported by the Silverpeas datastores.
   * @return the current {@link Instant} truncated to the microsecond.
   */
  public static Instant nowAsInstant() {
    return atDatastorePrecision(Instant.now());
  }

  /**
   * Truncates the specified datetime to the maximum resolution supported by the Silverpeas
   * datastores.
   * @param dateTime the datetime to truncate.
   * @return the specified {@link OffsetDateTime} truncated to the microsecond.
   */
  public static OffsetDateTime atDatastorePrecision(final OffsetDateTime dateTime) {
    return dateTime.truncatedTo(DATASTORE_PRECISION);
  }

  /**
   * Truncates the specified datetime to the maximum resolution supported by the Silverpeas
   * datastores.
   * @param dateTime the datetime to truncate.
   * @return the specified {@link ZonedDateTime} truncated to the microsecond.
   */
  public static ZonedDateTime atDatastorePrecision(final ZonedDateTime dateTime) {
    return dateTime.truncatedTo(DATASTORE_PRECISION);
  }

  /**
   * Truncates the specified datetime to the maximum resolution supported by the Silverpeas
   * datastores.
   * @param dateTime the datetime to truncate.
   * @return the specified {@link LocalDateTime} truncated to the microsecond.
   */
  public static LocalDateTime atDatastorePrecision(final LocalDateTime dateTime) {
    return dateTime.truncatedTo(DATASTORE_PRECISION);
  }

  /**
   * Truncates the specified time to the maximum resolution supported by the Silverpeas datastores.
   * @param time the time to truncate.
   * @return the specified {@link OffsetTime} truncated to the microsecond.
   */
  public static OffsetTime atDatastorePrecision(final OffsetTime time) {
    return time.truncatedTo(DATASTORE_PRECISION);
  }

  /**
   * Truncates the specified time to the maximum resolution supported by the Silverpeas datastores.
   * @param time the time to truncate.
   * @return the specified {@link LocalTime} truncated to the microsecond.
   */
  public static LocalTime atDatastorePrecision(final LocalTime time) {
    return time.truncatedTo(DATASTORE_PRECISION);
  }

  /**
   * Truncates the specified instant to the maximum resolution supported by the Silverpeas
   * datastores.
   * @param instant the instant to truncate.
   * @return the specified {@link Instant} truncated to the microsecond.
   */
  public static Instant atDatastorePrecision(final Instant instant) {
    return instant.truncatedTo(DATASTORE_PRECISION);
  }
}
