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

import org.silverpeas.core.util.JSONCodec;

import java.util.function.Function;

/**
 * @author Yohann Chastagnier
 */
public class AbstractPeriodChartTest {

  @SuppressWarnings("unchecked")
  protected String expJsChart(String title, String defaultPeriodType, String xLabel, String yLabel,
      Function<JSONCodec.JSONObject, JSONCodec.JSONObject>... expectedData) {
    return JSONCodec.encodeObject(jsonObject -> {
      jsonObject.put("chartType", "period");
      jsonObject.put("title", title);
      jsonObject.putJSONObject("axis",
          axis -> axis.putJSONObject("x", xAxis -> xAxis.put("title", xLabel))
              .putJSONObject("y", yAxis -> yAxis.put("title", yLabel)));
      jsonObject.put("defaultPeriodType", defaultPeriodType);
      jsonObject.putJSONArray("items", jsonArray -> {
        for (Function<JSONCodec.JSONObject, JSONCodec.JSONObject> data : expectedData) {
          jsonArray.addJSONObject(data);
        }
        return jsonArray;
      });
      return jsonObject;
    });
  }

  protected Function<JSONCodec.JSONObject, JSONCodec.JSONObject> expItemAsJs(String title,
      long expectedTime, long duration, final boolean durationAsPrimitive, String periodType,
      Number... values) {
    return (jsonObject -> {
      jsonObject.put("title", title).putJSONObject("x", xJson -> {
        xJson.put("periodType", periodType);
        xJson.put("startTime", expectedTime);
        if (durationAsPrimitive) {
          xJson.put("duration", duration);
        } else {
          xJson.put("duration", Long.valueOf(duration));
        }
        return xJson;
      }).putJSONArray("y", jsonArray -> {
        for (Number value : values) {
          jsonArray.add(value);
        }
        return jsonArray;
      });
      return jsonObject;
    });
  }
}
