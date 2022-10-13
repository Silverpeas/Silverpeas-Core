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
package org.silverpeas.core.chart.pie;

import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.util.function.UnaryOperator;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
public abstract class AbstractPieChartTest {

  @SuppressWarnings("unchecked")
  protected String expJsChart(String title, UnaryOperator<JSONObject>... expectedData) {
    return expJsChartWithExtra(title, null, expectedData);
  }

  @SuppressWarnings("unchecked")
  protected String expJsChartWithExtra(String title, UnaryOperator<JSONObject> extra,
      UnaryOperator<JSONObject>... expectedData) {
    return JSONCodec.encodeObject(jsonObject -> {
      jsonObject.put("chartType", "pie");
      jsonObject.put("title", title);
      jsonObject.putJSONArray("items", jsonArray -> {
        for (UnaryOperator<JSONObject> data : expectedData) {
          jsonArray.addJSONObject(data);
        }
        return jsonArray;
      });
      if (extra != null) {
        jsonObject.putJSONObject("extra", extra);
      }
      return jsonObject;
    });
  }

  protected UnaryOperator<JSONObject> expItemAsJs(String title, String label,
      UnaryOperator<JSONObject> extra, Number value) {
    return (jsonObject -> {
      jsonObject.put("title", title).put("label", label).put("value", value);
      if (extra != null) {
        jsonObject.putJSONObject("extra", extra);
      }
      return jsonObject;
    });
  }

  protected UnaryOperator<JSONObject> expItemAsJs(String title, String label, Number value) {
    return expItemAsJs(title, label, null, value);
  }

  protected UnaryOperator<JSONObject> expItemAsJs(String title) {
    return expItemAsJs(title, "", null, null);
  }

  protected UnaryOperator<JSONObject> expItemAsJs(String title,
      UnaryOperator<JSONObject> extra) {
    return expItemAsJs(title, "", extra, null);
  }
}
