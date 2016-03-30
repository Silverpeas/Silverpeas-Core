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
package org.silverpeas.core.chart.pie;

import org.silverpeas.core.chart.AbstractChart;
import org.silverpeas.core.chart.ChartType;

/**
 * @author Yohann Chastagnier
 */
public class PieChart extends AbstractChart<PieChartItem> {

  /**
   * Initializes a new chart with the specified title that will provides the data in order to
   * display them on a pie graphic.
   * @param title the main title of the chart.
   * @return a new instance of {@link PieChart}.
   */
  public static PieChart fromTitle(String title) {
    return new PieChart().withTitle(title);
  }

  /**
   * Initializes a new chart without title that will provides the data in order to display them on
   * a pie graphic.
   * @return a new instance of {@link PieChart}.
   */
  public static PieChart withoutTitle() {
    return new PieChart();
  }

  private PieChart() {
  }

  @Override
  public ChartType getType() {
    return ChartType.pie;
  }

  /**
   * Adds a new pie part.<br/>
   * @param label the label.
   * @param value the value.
   * @return the {@link PieChartItem} that represents the given value.
   */
  public PieChartItem add(final String label, final Number value) {
    PieChartItem item = new PieChartItem(label, value);
    getItems().add(item);
    return item;
  }
}
