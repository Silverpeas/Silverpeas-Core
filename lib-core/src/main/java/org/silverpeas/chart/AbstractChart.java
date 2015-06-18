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
package org.silverpeas.chart;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.silverpeas.util.StringUtil.defaultStringIfNotDefined;

/**
 * Common implementation between each chart.
 * @author Yohann Chastagnier
 */
public abstract class AbstractChart<CHART_ITEM_TYPE extends ChartItem>
    implements Chart<CHART_ITEM_TYPE> {

  private String title = "";
  private List<CHART_ITEM_TYPE> items = new ArrayList<CHART_ITEM_TYPE>();
  private Map<String, Object> extra = null;

  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Adds an extra information associated to the chart, but not necessary for the chart
   * rendering. Useful to provide data from a treatment to an other one.
   * @param key the key at which the given information is registered.
   * @param value the value registered.
   * @param <T>
   * @return the instance of the chart itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractChart<CHART_ITEM_TYPE>> T addExtra(final String key, Object value) {
    if (extra == null) {
      extra = new LinkedHashMap<String, Object>();
    }
    extra.put(key, value);
    return (T) this;
  }

  /**
   * Gets the value of an information associated to the chart, but not necessary for the chart
   * rendering. Useful to provide data from a treatment to an other one.
   * @param key the ket at which the extra data is registered.
   * @return the extra data as it has been registered.
   */
  public Object getExtra(String key) {
    return extra == null ? null : extra.get(key);
  }

  /**
   * Sets the title of the chart.
   * @param title the title of the chart.
   * @return the instance of the chart itself.
   */
  @SuppressWarnings("unchecked")
  public final <T extends AbstractChart<CHART_ITEM_TYPE>> T withTitle(final String title) {
    this.title = defaultStringIfNotDefined(title);
    return (T) this;
  }

  @Override
  public final String asJson() {
    JSONArray dataAsJson = new JSONArray();
    for (ChartItem data : getItems()) {
      try {
        dataAsJson.put(new JSONObject(new JSONTokener(data.asJson())));
      } catch (ParseException ignore) {
      }
    }
    JSONObject jsonChart = new JSONObject();
    jsonChart.put("chartType", getType().name());
    jsonChart.put("title", getTitle());
    jsonChart.put("items", dataAsJson);
    if (extra != null) {
      JSONObject jsonExtra = new JSONObject();
      for (Map.Entry<String, Object> entry : extra.entrySet()) {
        jsonExtra.put(entry.getKey(), entry.getValue());
      }
      jsonChart.put("extra", jsonExtra);
    }
    computeExtraDataAsJson(jsonChart);
    return jsonChart.toString();
  }

  /**
   * Overriding this method if necessary.
   * @param jsonChart the json object that represents the chart.
   */
  @SuppressWarnings("UnusedParameters")
  protected void computeExtraDataAsJson(JSONObject jsonChart) {
  }

  @Override
  public final List<CHART_ITEM_TYPE> getItems() {
    return items;
  }

  /**
   * Adds an item into the list of chart items.
   * @param item the chart item to add.
   * @param <T>
   * @return the instance of the chart itself.
   */
  @SuppressWarnings("unchecked")
  protected <T extends AbstractChart<CHART_ITEM_TYPE>> T add(CHART_ITEM_TYPE item) {
    items.add(item);
    return (T) this;
  }
}
