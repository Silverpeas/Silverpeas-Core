/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.variables;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class VariableScheduledValueTest {

  @Test
  @DisplayName("A variable's value is defined in a given period of days")
  void createAValueScheduledInADefinedDaysPeriod() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    VariableScheduledValue value =
        new VariableScheduledValue("value", Period.between(yesterday, tomorrow));

    assertThat(value.getValue(), is("value"));
    assertThat(value.getPeriod().getStartDate(), is(yesterday));
    assertThat(value.getPeriod().getEndDate(), is(tomorrow));
  }

  @Test
  @DisplayName("A variable's value is defined in a given period of time")
  void createAValueScheduledInADefinedDatetimePeriod() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
    VariableScheduledValue value =
        new VariableScheduledValue("value", Period.between(yesterday, tomorrow));

    assertThat(value.getValue(), is("value"));
    assertThat(value.getPeriod().getStartDate(), is(yesterday.toLocalDate()));
    assertThat(value.getPeriod().getEndDate(), is(tomorrow.toLocalDate()));
  }

  @Test
  @DisplayName("A variable's value is defined forever")
  void createAValueScheduledForever() {
    VariableScheduledValue value = new VariableScheduledValue("value", Period.indefinite());

    assertThat(value.getValue(), is("value"));
    assertThat(value.getPeriod().isIndefinite(), is(true));
    assertThat(value.getPeriod().getStartDate(), is(LocalDate.MIN));
    assertThat(value.getPeriod().getEndDate(), is(LocalDate.MAX));
  }
}
