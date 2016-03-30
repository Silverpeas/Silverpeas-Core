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

import org.junit.Test;
import org.silverpeas.core.chart.ChartType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
public class PieChartTest extends AbstractPieChartTest {

  @Test
  public void emptyPieChartWithoutTitle() {
    PieChart chartWithoutTitle = PieChart.withoutTitle();
    assertThat(chartWithoutTitle.getType(), is(ChartType.pie));
    assertThat(chartWithoutTitle.getTitle(), isEmptyString());
    assertThat(chartWithoutTitle.getItems(), empty());
    assertThat(chartWithoutTitle.asJson(), is(expJsChart("")));
  }

  @Test
  public void emptyPieChartWithTitle() {
    PieChart chartWithTitle =
        PieChart.fromTitle("Pie chart title").addExtra("youpi", "tralala").addExtra("26", "98");
    assertThat(chartWithTitle.getType(), is(ChartType.pie));
    assertThat(chartWithTitle.getTitle(), is("Pie chart title"));
    assertThat(chartWithTitle.getItems(), empty());
    assertThat(chartWithTitle.asJson(), is(expJsChartWithExtra("Pie chart title",
            extra -> extra.put("youpi", "tralala").put("26", "98"))));
  }

  @Test
  public void onePiePart() {
    PieChart chart = PieChart.withoutTitle();
    chart.withTitle("Pie Chart");
    chart.add("Label 1", 26);
    assertThat(chart.getItems(), hasSize(1));
    assertThat(chart.asJson(), is(expJsChart("Pie Chart", expItemAsJs("", "Label 1", 26))));
  }

  @Test
  public void twoPieParts() {
    PieChart chart = PieChart.withoutTitle();
    chart.add("Label 1", 26);
    chart.add("Label 2", 38).withTitle("Second attempt");
    assertThat(chart.getItems(), hasSize(2));
    assertThat(chart.asJson(), is(expJsChart("", expItemAsJs("", "Label 1", 26),
        expItemAsJs("Second attempt", "Label 2", 38))));
  }
}