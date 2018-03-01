/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test in the {@link Period} objects
 * @author mmoquillon
 */
public class PeriodTest {

  @Test
  public void periodBetweenTwoDates() {
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
  public void periodBetweenTwoDateTimes() {
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
  public void periodInTheCurrentDay() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(currentDay, currentDay);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(currentDay));
    assertThat(period.getEndDate(), is(currentDay.plusDays(1)));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodStartingAtMinDate() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(LocalDate.MIN, currentDay);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(currentDay));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodStartingAtMinDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.between(OffsetDateTime.MIN, now);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  public void periodEndingAtMaxDate() {
    LocalDate currentDay = LocalDate.now();
    Period period = Period.between(currentDay, LocalDate.MAX);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(currentDay));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodEndingAtMaxDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.between(now, OffsetDateTime.MAX);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  public void periodStartingAtMinDateTimeAndEndingAtMaxDate() {
    Period period = Period.between(LocalDate.MIN, LocalDate.MAX);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodStartingAtMinDateTimeAndEndingAtMaxDateTime() {
    Period period = Period.between(OffsetDateTime.MIN, OffsetDateTime.MAX);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void periodInNowShouldFail() {
    OffsetDateTime now = OffsetDateTime.now();
    Period.between(now, now);
  }

  @Test(expected = IllegalArgumentException.class)
  public void periodBetweenTwoDifferentTemporalShouldFail1() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    Period.between(yesterday, tomorrow);
  }

  @Test(expected = IllegalArgumentException.class)
  public void periodBetweenTwoDifferentTemporalShouldFail2() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    Period.between(yesterday, tomorrow);
  }

  @Test(expected = IllegalArgumentException.class)
  public void periodInDateEndingBeforeStartingShouldFail() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    Period.between(tomorrow, yesterday);
  }

  @Test(expected = IllegalArgumentException.class)
  public void periodInDateTimeEndingBeforeStartingShouldFail() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    Period.between(tomorrow, yesterday);
  }

  @Test(expected = NullPointerException.class)
  public void periodStartingAtUndefinedDateShouldFail() {
    Period.between(null, LocalDate.now());
  }

  @Test(expected = NullPointerException.class)
  public void periodStartingAtUndefinedDateTimeShouldFail() {
    Period.between(null, OffsetDateTime.now());
  }

  @Test(expected = NullPointerException.class)
  public void periodEndingAtUndefinedDateShouldFail() {
    Period.between(LocalDate.now(), null);
  }

  @Test(expected = NullPointerException.class)
  public void periodEndingAtUndefinedDateTimeShouldFail() {
    Period.between(OffsetDateTime.now(), null);
  }

  @Test
  public void periodExplicitlyStartingAtUndefinedDate() {
    LocalDate today = LocalDate.now();
    Period period = Period.betweenNullable(null, today);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(today));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodExplicitlyStartingAtUndefinedDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.betweenNullable(null, now);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(false));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  public void periodExplicitlyEndingAtUndefinedDate() {
    LocalDate today = LocalDate.now();
    Period period = Period.betweenNullable(today, null);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(today));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodExplicitlyEndingAtUndefinedDateTime() {
    OffsetDateTime now = OffsetDateTime.now();
    Period period = Period.betweenNullable(now, null);
    assertThat(period.startsAtMinDate(), is(false));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }

  @Test
  public void periodExplicitlyStartingAndEndingAtUndefinedDate() {
    Period period = Period.betweenNullable((LocalDate)null, null);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(LocalDate.MIN));
    assertThat(period.getEndDate(), is(LocalDate.MAX));
    assertThat(period.isInDays(), is(true));
  }

  @Test
  public void periodExplicitlyStartingAndEndingAtUndefinedDateTime() {
    Period period = Period.betweenNullable((OffsetDateTime) null, null);
    assertThat(period.startsAtMinDate(), is(true));
    assertThat(period.endsAtMaxDate(), is(true));
    assertThat(period.getStartDate(), is(OffsetDateTime.MIN));
    assertThat(period.getEndDate(), is(OffsetDateTime.MAX));
    assertThat(period.isInDays(), is(false));
  }
}
  