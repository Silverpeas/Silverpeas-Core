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
package org.silverpeas.chart.period;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Yohann Chastagnier
 */
public class AbstractPeriodChartTest {

  protected String expJsChart(String title, String defaultPeriodType, String xLabel, String yLabel,
      JSONObject... expectedData) {
    JSONArray jsonArray = new JSONArray();
    for (JSONObject data : expectedData) {
      jsonArray.put(data);
    }
    return new JSONObject().put("title", title).put("chartType", "period")
        .put("defaultPeriodType", defaultPeriodType).put("items", jsonArray)
        .put("axis", new JSONObject().put("x", new JSONObject().put("title", xLabel))
            .put("y", new JSONObject().put("title", yLabel))).toString();
  }

  protected JSONObject expJsItem(String title, long expectedTime, long duration,
      final boolean durationAsPrimitive, String periodType, Number... values) {
    JSONObject x = new JSONObject().put("startTime", expectedTime).put("periodType", periodType);
    if (durationAsPrimitive) {
      x.put("duration", duration);
    } else {
      x.put("duration", Long.valueOf(duration));
    }
    JSONArray jsonArray = new JSONArray();
    for (Number value : values) {
      jsonArray.put(value);
    }
    return new JSONObject().put("title", title).put("x", x).put("y", jsonArray);
  }
}
