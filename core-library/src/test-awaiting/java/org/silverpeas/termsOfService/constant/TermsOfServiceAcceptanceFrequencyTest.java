/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.termsOfService.constant;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.termsOfService.constant.TermsOfServiceAcceptanceFrequency.*;

/**
 * User: Yohann Chastagnier
 * Date: 10/09/13
 */
public class TermsOfServiceAcceptanceFrequencyTest {

  private static final String LOCALE = "fr";
  // Saturday
  private static final Date FIRST_OF_JUNE = java.sql.Date.valueOf("2013-06-01");
  // Sunday
  private static final Date LAST_OF_JUNE = Timestamp.valueOf("2013-06-30 23:59:59.0");

  @Test
  public void testDecode() {
    assertThat(decode(null), is(NEVER));
    assertThat(decode(""), is(NEVER));
    assertThat(decode("sjdsl"), is(NEVER));
    assertThat(decode("always"), is(ALWAYS));
    for (final TermsOfServiceAcceptanceFrequency current : values()) {
      assertThat(decode(current.name()), is(current));
    }
  }

  @Test
  public void testIsAcceptanceDateExpired() {

    // This test is to don't forget to add or remove test block below in case of upgrade of the
    // enum.
    assertThat(values().length, is(7));

    // NEVER
    assertThat(NEVER.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(false));
    assertThat(NEVER.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(
        NEVER.isAcceptanceDateExpired(FIRST_OF_JUNE, java.sql.Date.valueOf("1970-01-01"), LOCALE),
        is(false));

    // ALWAYS
    assertThat(ALWAYS.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(ALWAYS.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(true));
    assertThat(
        ALWAYS.isAcceptanceDateExpired(FIRST_OF_JUNE, java.sql.Date.valueOf("1970-01-01"), LOCALE),
        is(true));

    // ONE
    assertThat(ONE.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(ONE.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(
        ONE.isAcceptanceDateExpired(FIRST_OF_JUNE, java.sql.Date.valueOf("1970-01-01"), LOCALE),
        is(false));

    // DAILY
    assertThat(DAILY.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(DAILY.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(DAILY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMilliseconds(FIRST_OF_JUNE, -1),
            LOCALE), is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addDays(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addWeeks(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(DAILY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMonths(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addYears(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(DAILY
        .isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMilliseconds(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        DAILY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMonths(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        DAILY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addYears(LAST_OF_JUNE, -1), LOCALE),
        is(true));

    // WEEKLY
    assertThat(WEEKLY.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(WEEKLY.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(WEEKLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMilliseconds(FIRST_OF_JUNE, -1),
            LOCALE), is(false));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addDays(FIRST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(WEEKLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addWeeks(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(WEEKLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMonths(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(WEEKLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addYears(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(WEEKLY
        .isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMilliseconds(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -6), LOCALE),
        is(false));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMonths(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        WEEKLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addYears(LAST_OF_JUNE, -1), LOCALE),
        is(true));

    // MONTHLY
    assertThat(MONTHLY.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(MONTHLY.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMilliseconds(FIRST_OF_JUNE, -1),
            LOCALE), is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addDays(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addWeeks(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMonths(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addYears(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMilliseconds(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -29), LOCALE),
        is(false));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -4), LOCALE),
        is(false));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -5), LOCALE),
        is(true));
    assertThat(MONTHLY
        .isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMonths(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        MONTHLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addYears(LAST_OF_JUNE, -1), LOCALE),
        is(true));

    // YEARLY
    assertThat(YEARLY.isAcceptanceDateExpired(FIRST_OF_JUNE, null, LOCALE), is(true));
    assertThat(YEARLY.isAcceptanceDateExpired(FIRST_OF_JUNE, FIRST_OF_JUNE, LOCALE), is(false));
    assertThat(YEARLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMilliseconds(FIRST_OF_JUNE, -1),
            LOCALE), is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addDays(FIRST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(YEARLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addWeeks(FIRST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(YEARLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addMonths(FIRST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(YEARLY
        .isAcceptanceDateExpired(FIRST_OF_JUNE, DateUtils.addYears(FIRST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(YEARLY
        .isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMilliseconds(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addDays(LAST_OF_JUNE, -150), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addWeeks(LAST_OF_JUNE, -25), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMonths(LAST_OF_JUNE, -1), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addMonths(LAST_OF_JUNE, -5), LOCALE),
        is(false));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addYears(LAST_OF_JUNE, -1), LOCALE),
        is(true));
    assertThat(
        YEARLY.isAcceptanceDateExpired(LAST_OF_JUNE, DateUtils.addYears(LAST_OF_JUNE, 1), LOCALE),
        is(true));
  }
}
