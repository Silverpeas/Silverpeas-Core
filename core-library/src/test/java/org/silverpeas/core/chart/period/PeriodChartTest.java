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
import org.silverpeas.core.chart.ChartType;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
public class PeriodChartTest extends AbstractPeriodChartTest {

  @Test
  public void emptyPeriodChartWithoutTitle() {
    PeriodChart chartWithoutTitle = PeriodChart.withoutTitle();
    assertThat(chartWithoutTitle.getType(), is(ChartType.period));
    assertThat(chartWithoutTitle.getTitle(), isEmptyString());
    assertThat(chartWithoutTitle.getItems(), empty());
    assertThat(chartWithoutTitle.asJson(), is(expJsChart("", "unknown", "", "")));
  }

  @Test
  public void emptyPeriodChartWithTitle() {
    PeriodChart chartWithTitle = PeriodChart.fromTitle("Period chart title");
    assertThat(chartWithTitle.getType(), is(ChartType.period));
    assertThat(chartWithTitle.getTitle(), is("Period chart title"));
    assertThat(chartWithTitle.getItems(), empty());
    assertThat(chartWithTitle.asJson(), is(expJsChart("Period chart title", "unknown", "", "")));
  }

  @Test
  public void onePeriodAndOneValue() {
    long expectedTime = java.sql.Date.valueOf("2015-01-01").getTime();
    Date date = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration = 24L * 60 * 60 * 1000 * 365;
    PeriodChart chart = PeriodChart.withoutTitle();
    chart.forX(Period.from(date, PeriodType.year)).add(26);
    assertThat(chart.getItems(), hasSize(1));
    assertThat(chart.asJson(), is(expJsChart("", "year", "", "",
        expItemAsJs("", expectedTime, duration, true, "year", 26))));
  }

  @Test
  public void onePeriodAndTwoValues() {
    long expectedTime = java.sql.Date.valueOf("2015-07-01").getTime();
    Date date = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration = 24L * 60 * 60 * 1000 * 31;
    PeriodChart chart = PeriodChart.withoutTitle();
    chart.forX(Period.from(date, PeriodType.month)).add(26);
    chart.forX(Period.from(date, PeriodType.month)).add(38);
    assertThat(chart.getItems(), hasSize(1));
    assertThat(chart.asJson(), is(expJsChart("", "month", "", "",
        expItemAsJs("", expectedTime, duration, true, "month", 26, 38))));
  }

  @Test
  public void twoPeriodsAndAxisLabels() {
    long expectedTime1 = java.sql.Date.valueOf("2015-07-20").getTime();
    Date date1 = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration1 = 24L * 60 * 60 * 1000;
    long expectedTime2 = java.sql.Date.valueOf("2015-07-01").getTime();
    Date date2 = java.sql.Timestamp.valueOf("2015-07-20 13:56:23.256");
    long duration2 = 24L * 60 * 60 * 1000 * 31;

    PeriodChart chart = PeriodChart.fromTitle("Chart title");
    chart.getAxisX().setTitle("X axis");
    chart.getAxisY().setTitle("Y axis");
    chart.forX(date1, PeriodType.day).add(26).withTitle("Item 1");
    chart.forX(date2, PeriodType.month).add(38).withTitle("Item 2");
    assertThat(chart.getItems(), hasSize(2));
    assertThat(chart.asJson(), is(expJsChart("Chart title", "day", "X axis", "Y axis",
        expItemAsJs("Item 1", expectedTime1, duration1, false, "day", 26),
        expItemAsJs("Item 2", expectedTime2, duration2, true, "month", 38))));
  }
}