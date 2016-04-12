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
package org.silverpeas.core.chart;

import org.silverpeas.core.util.JSONCodec.JSONArray;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractAxisChartItem<XAXIS_DATA_TYPE, YAXIS_DATA_TYPE, ITEM_TYPE extends
    AbstractAxisChartItem<XAXIS_DATA_TYPE, YAXIS_DATA_TYPE, ITEM_TYPE>>
    extends AbstractChartItem<ITEM_TYPE> {

  private final XAXIS_DATA_TYPE x;
  private List<YAXIS_DATA_TYPE> yValues = new ArrayList<YAXIS_DATA_TYPE>();

  protected AbstractAxisChartItem(XAXIS_DATA_TYPE x) {
    this.x = x;
  }

  /**
   * Gets the data of x axis.
   * @return
   */
  public XAXIS_DATA_TYPE getX() {
    return x;
  }

  /**
   * Gets the value
   * @return
   */
  public List<YAXIS_DATA_TYPE> getYValues() {
    return yValues;
  }

  /**
   * Add a y value associated to the x one.
   * @param y the value to add
   * @return the instance of the chart item itself.
   */
  @SuppressWarnings("unchecked")
  public ITEM_TYPE add(final YAXIS_DATA_TYPE y) {
    getYValues().add(y);
    return (ITEM_TYPE) this;
  }

  @Override
  protected void completeJsonData(JSONObject itemAsJson) {
    itemAsJson.putJSONObject("x", jsonObject -> {
      performXValue(jsonObject, getX());
      return jsonObject;
    });
    itemAsJson.putJSONArray("y", datasetsAsJson -> {
      for (YAXIS_DATA_TYPE yValue : getYValues()) {
        performYValue(datasetsAsJson, yValue);
      }
      return datasetsAsJson;
    });
  }

  /**
   * Complete the given json object with the given X value.
   * @param jsonObject the json object to complete.
   * @param xValue the value on x axis.
   */
  abstract protected void performXValue(JSONObject jsonObject, XAXIS_DATA_TYPE xValue);

  /**
   * Complete the given json array with the given Y value associated to the X one.
   * @param jsonArray the json array to complete.
   * @param yValue a y value from {@link #getYValues()}.
   */
  abstract protected void performYValue(JSONArray jsonArray, YAXIS_DATA_TYPE yValue);
}
