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

import org.silverpeas.core.chart.AbstractAxisChart;
import org.silverpeas.core.chart.ChartType;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.util.Date;

/**
 * @author Yohann Chastagnier
 */
public class PeriodChart extends AbstractAxisChart<Period, Number, PeriodChartItem> {

  private PeriodType defaultPeriodType = PeriodType.unknown;

  /**
   * Initializes a new chart with the specified title that will provides the data in order to
   * display them on a temporal graphic.
   * @param title the main title of the chart.
   * @return a new instance of {@link PeriodChart}.
   */
  public static PeriodChart fromTitle(String title) {
    return new PeriodChart().withTitle(title);
  }

  /**
   * Initializes a new chart without title that will provides the data in order to display them on
   * a temporal graphic.
   * @return a new instance of {@link PeriodChart}.
   */
  public static PeriodChart withoutTitle() {
    return new PeriodChart();
  }

  private PeriodChart() {
  }

  @Override
  public ChartType getType() {
    return ChartType.period;
  }

  /**
   * Gets the default period type.
   * @return the default {@link PeriodType}.
   */
  public PeriodType getDefaultPeriodType() {
    return defaultPeriodType;
  }

  /**
   * Sets the default period type.
   * @param defaultPeriodType the default period type.
   * @return the instance of the chart itself.
   */
  public PeriodChart setDefaultPeriodType(final PeriodType defaultPeriodType) {
    this.defaultPeriodType = defaultPeriodType;
    return this;
  }

  @Override
  protected void computeExtraDataAsJson(final JSONObject jsonChart) {
    super.computeExtraDataAsJson(jsonChart);
    jsonChart.put("defaultPeriodType", defaultPeriodType.getName());
  }

  /**
   * Adds a value associated to a period.
   * The {@link PeriodType} of the period must not be {@link PeriodType#unknown}.
   * @param dateReference a date of reference.
   * @param periodType the period to represents.
   * @return the {@link PeriodChartItem} that represents the given values.
   */
  public PeriodChartItem forX(final Date dateReference, PeriodType periodType) {
    if (periodType == PeriodType.unknown) {
      throw new IllegalArgumentException("The period type must be known");
    }
    return forX(Period.from(dateReference, periodType));
  }

  @Override
  public PeriodChartItem forX(final Period period) {
    if (period.getPeriodType() != PeriodType.unknown &&
        period.getPeriodType().ordinal() > getDefaultPeriodType().ordinal()) {
      setDefaultPeriodType(period.getPeriodType());
    }
    return super.forX(period);
  }
}
