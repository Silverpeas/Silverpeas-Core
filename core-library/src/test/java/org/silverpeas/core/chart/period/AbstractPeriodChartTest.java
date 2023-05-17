/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.chart.period;

import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.test.unit.UnitTest;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.util.function.UnaryOperator;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
public abstract class AbstractPeriodChartTest {

  @BeforeEach
  public void setUpMessageManager() {
    MessageManager.initialize();
    MessageManager.setLanguage("fr");
  }

  @SuppressWarnings("unchecked")
  protected String expJsChart(String title, String defaultPeriodType, String xLabel, String yLabel,
      UnaryOperator<JSONObject>... expectedData) {
    return JSONCodec.encodeObject(jsonObject -> {
      jsonObject.put("chartType", "period");
      jsonObject.put("title", title);
      jsonObject.putJSONObject("axis",
          axis -> axis.putJSONObject("x", xAxis -> xAxis.put("title", xLabel))
              .putJSONObject("y", yAxis -> yAxis.put("title", yLabel)));
      jsonObject.put("defaultPeriodType", defaultPeriodType);
      jsonObject.putJSONArray("items", jsonArray -> {
        for (UnaryOperator<JSONObject> data : expectedData) {
          jsonArray.addJSONObject(data);
        }
        return jsonArray;
      });
      return jsonObject;
    });
  }

  protected UnaryOperator<JSONObject> expItemAsJs(String title,
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
