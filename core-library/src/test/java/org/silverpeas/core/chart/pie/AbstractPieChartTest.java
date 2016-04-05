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

import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.util.function.Function;

/**
 * @author Yohann Chastagnier
 */
public class AbstractPieChartTest {

  @SuppressWarnings("unchecked")
  protected String expJsChart(String title, Function<JSONObject, JSONObject>... expectedData) {
    return expJsChartWithExtra(title, null, expectedData);
  }

  @SuppressWarnings("unchecked")
  protected String expJsChartWithExtra(String title, Function<JSONObject, JSONObject> extra,
      Function<JSONObject, JSONObject>... expectedData) {
    return JSONCodec.encodeObject(jsonObject -> {
      jsonObject.put("chartType", "pie");
      jsonObject.put("title", title);
      jsonObject.putJSONArray("items", jsonArray -> {
        for (Function<JSONObject, JSONObject> data : expectedData) {
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

  protected Function<JSONObject, JSONObject> expItemAsJs(String title, String label,
      Function<JSONObject, JSONObject> extra, Number value) {
    return (jsonObject -> {
      jsonObject.put("title", title).put("label", label).put("value", value);
      if (extra != null) {
        jsonObject.putJSONObject("extra", extra);
      }
      return jsonObject;
    });
  }

  protected Function<JSONObject, JSONObject> expItemAsJs(String title, String label, Number value) {
    return expItemAsJs(title, label, null, value);
  }

  protected Function<JSONObject, JSONObject> expItemAsJs(String title) {
    return expItemAsJs(title, "", null, null);
  }

  protected Function<JSONObject, JSONObject> expItemAsJs(String title,
      Function<JSONObject, JSONObject> extra) {
    return expItemAsJs(title, "", extra, null);
  }
}
