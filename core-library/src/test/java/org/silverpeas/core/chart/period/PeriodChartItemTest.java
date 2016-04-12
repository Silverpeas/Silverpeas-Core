/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.chart.period;

import org.junit.Test;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.JSONCodec.encodeObject;

/**
 * @author Yohann Chastagnier
 */
public class PeriodChartItemTest extends AbstractPeriodChartTest {

  @Test
  public void withoutTitleAndWithoutValue() {
    PeriodChartItem item = new PeriodChartItem(Period.UNDEFINED);
    long expectedTime = Period.UNDEFINED.getBeginDatable().getTime();
    long duration = Period.UNDEFINED.getElapsedTimeData().getTimeAsLong();
    assertThat(item.asJson(),
        is(encodeObject(expItemAsJs("", expectedTime, duration, false, "unknown"))));
  }

  @Test
  public void withTitleButWithoutValue() {
    PeriodChartItem item = new PeriodChartItem(Period.UNDEFINED).withTitle("A title");
    long expectedTime = Period.UNDEFINED.getBeginDatable().getTime();
    long duration = Period.UNDEFINED.getElapsedTimeData().getTimeAsLong();
    assertThat(item.asJson(),
        is(encodeObject(expItemAsJs("A title", expectedTime, duration, false, "unknown"))));
  }

  @Test
  public void withoutTitleAndWithOneValue() {
    long expectedTime = java.sql.Date.valueOf("2015-07-20").getTime();
    Date date = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration = 24L * 60 * 60 * 1000;
    PeriodChartItem item = new PeriodChartItem(Period.from(date, PeriodType.day)).add(26);
    assertThat(item.asJson(),
        is(encodeObject(expItemAsJs("", expectedTime, duration, false, "day", 26))));
  }

  @Test
  public void withTitleAndTwoValues() {
    long expectedTime = java.sql.Date.valueOf("2015-07-01").getTime();
    Date date = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration = 24L * 60 * 60 * 1000 * 31;
    PeriodChartItem item =
        new PeriodChartItem(Period.from(date, PeriodType.month)).withTitle("A " + "title");
    item.add(26).add(38);
    assertThat(item.asJson(),
        is(encodeObject(expItemAsJs("A title", expectedTime, duration, false, "month", 26, 38))));
  }
}