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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test in the {@link Period} objects
 * @author mmoquillon
 */
class PeriodTest {

  private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

  @BeforeAll
  static void setTimeZone() {
    // we set explicitly a time zone different of UTC to check the datetime are correctly
    // converted in UTC in our API
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
  }

  @AfterAll
  static void restoreTimeZone() {
    TimeZone.setDefault(DEFAULT_TIME_ZONE);
  }

  @Test
  void periodBetweenTwoDates() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    Period period = Period.between(yesterday, tomorrow);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(yesterday));
    assertThat(period.getEndDate(), is(tomorrow));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodBetweenTwoDateTimes() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    Period period = Period.between(yesterday, tomorrow);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(yesterday.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(tomorrow.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodBetweenTwoZonedDateTimes() {
    ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
    ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);
    Period period = Period.between(yesterday, tomorrow);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(yesterday.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(tomorrow.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodBetweenTwoDayDateTimes() {
    OffsetDateTime start = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    OffsetDateTime end = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    Period period = Period.between(start, end);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(start.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(end.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodInTheCurrentDay() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(currentDay, currentDay);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(currentDay));
    assertThat(period.getEndDate(), is(currentDay.plusDays(1)));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodStartingAtMinDate() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(LocalDate.MIN, currentDay);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(currentDay));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodStartingAtMinDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.between(OffsetDateTime.MIN, now);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodEndingAtMaxDate() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(currentDay, LocalDate.MAX);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(currentDay));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodEndingAtMaxDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.between(now, OffsetDateTime.MAX);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodStartingAtMinDateTimeAndEndingAtMaxDate() {
    Period period = Period.between(LocalDate.MIN, LocalDate.MAX);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodStartingAtMinDateTimeAndEndingAtMaxDateTime() {
    Period period = Period.between(OffsetDateTime.MIN, OffsetDateTime.MAX);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodInNowShouldFail() {
    OffsetDateTime now = OffsetDateTime.now();
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(now, now);
    });
  }

  @Test
  void periodBetweenTwoDifferentTemporalShouldFail1() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(yesterday, tomorrow);
    });
  }

  @Test
  void periodBetweenTwoDifferentTemporalShouldFail2() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(yesterday, tomorrow);
    });
  }

  @Test
  void periodInDateEndingBeforeStartingShouldFail() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(tomorrow, yesterday);
    });
  }

  @Test
  void periodInDateTimeEndingBeforeStartingShouldFail() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(tomorrow, yesterday);
    });
  }

  @Test
  void periodStartingAtUndefinedDateShouldFail() {
    LocalDate now = LocalDate.now();
    assertThrows(NullPointerException.class, () ->
    Period.between(null, now));
  }

  @Test
  void periodStartingAtUndefinedDateTimeShouldFail() {
    OffsetDateTime now = OffsetDateTime.now();
    assertThrows(NullPointerException.class, () ->
    Period.between(null, now));
  }

  @Test
  void periodEndingAtUndefinedDateShouldFail() {
    LocalDate now = LocalDate.now();
    assertThrows(NullPointerException.class, () ->
    Period.between(now, null));
  }

  @Test
  void periodEndingAtUndefinedDateTimeShouldFail() {
    OffsetDateTime now = OffsetDateTime.now();
    assertThrows(NullPointerException.class, () ->
    Period.between(now, null));
  }

  @Test
  void periodExplicitlyStartingAtUndefinedDate() {
    LocalDate today = LocalDate.now();
    Period period = Period.betweenNullable(null, today);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(today));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodExplicitlyStartingAtUndefinedDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.betweenNullable(null, now);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodExplicitlyEndingAtUndefinedDate() {
    LocalDate today = LocalDate.now();
    Period period = Period.betweenNullable(today, null);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(today));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodExplicitlyEndingAtUndefinedDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.betweenNullable(now, null);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodExplicitlyStartingAndEndingAtUndefinedDate() {
    Period period = Period.betweenNullable((LocalDate)null, null);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodExplicitlyStartingAndEndingAtUndefinedDateTime() {
    Period period = Period.betweenNullable((OffsetDateTime) null, null);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodWithBothNonDefinedStartAndEndTemporal() {
    Temporal start = null;
    Temporal end = null;
    Period period = Period.betweenNullable(start, end);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodWithNonDefinedStartTemporalAndEndingAtADefinedDate() {
    Temporal start = null;
    Temporal end = LocalDate.now().plusDays(1);
    Period period = Period.betweenNullable(start, end);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(end));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodWithNonDefinedStartTemporalAndEndingAtADefinedDateTime() {
    Temporal start = null;
    Temporal end = OffsetDateTime.now().plusDays(1).withOffsetSameInstant(ZoneOffset.UTC);
    Period period = Period.betweenNullable(start, end);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(end));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodStartingAtADefinedDateAndEndingAtAnUndefinedTemporal() {
    Temporal start = LocalDate.now().minusDays(1);
    Temporal end = null;
    Period period = Period.betweenNullable(start, end);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(start));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  void periodStartingAtADefinedDateTimeAndEndingAtAnUndefinedTemporal() {
    Temporal start = OffsetDateTime.now().minusDays(1).withOffsetSameInstant(ZoneOffset.UTC);
    Temporal end = null;
    Period period = Period.betweenNullable(start, end);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(start));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  void periodWithDifferentNullableTypeOfDateTime() {
    Temporal start = OffsetDateTime.now().minusDays(1);
    Temporal end = LocalDate.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.betweenNullable(start, end);
    });
  }

  @Test
  void periodWithDifferentTypeOfDateTime() {
    Temporal start = OffsetDateTime.now().minusDays(1);
    Temporal end = LocalDate.now().plusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      Period.between(start, end);
    });
  }
}
  