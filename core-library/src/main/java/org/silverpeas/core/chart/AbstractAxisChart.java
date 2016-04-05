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

import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractAxisChart<XAXIS_DATA_TYPE, YAXIS_DATA_TYPE, ITEM_TYPE extends
    AbstractAxisChartItem<XAXIS_DATA_TYPE, YAXIS_DATA_TYPE, ITEM_TYPE>>
    extends AbstractChart<ITEM_TYPE> {

  private ChartAxis x = new ChartAxis();
  private ChartAxis y = new ChartAxis();
  private Map<XAXIS_DATA_TYPE, ITEM_TYPE> itemIndexedByX = new HashMap<>();

  /**
   * Gets the {@link ChartAxis} that represents the x axis.
   * @return the {@link ChartAxis} of x axis.
   */
  public ChartAxis getAxisX() {
    return x;
  }

  /**
   * Gets the {@link ChartAxis} that represents the y axis.
   * @return the {@link ChartAxis} of y axis.
   */
  public ChartAxis getAxisY() {
    return y;
  }

  @Override
  protected void computeExtraDataAsJson(final JSONObject jsonChart) {
    super.computeExtraDataAsJson(jsonChart);
    jsonChart.putJSONObject("axis", axes -> {
      axes.putJSONObject("x", getAxisX().asJson());
      axes.putJSONObject("y", getAxisY().asJson());
      return axes;
    });
  }

  /**
   * Gets the item associated to the given x value.
   * @param xValue the x value.
   * @return the associated item, null if none.
   */
  protected ITEM_TYPE getItemFrom(final XAXIS_DATA_TYPE xValue) {
    return itemIndexedByX.get(xValue);
  }

  @Override
  protected <T extends AbstractChart<ITEM_TYPE>> T add(final ITEM_TYPE item) {
    itemIndexedByX.put(item.getX(), item);
    return super.add(item);
  }

  /**
   * Creates a new item with the given parameter as x value.
   * @return a new instance of char item.
   */
  @SuppressWarnings("unchecked")
  private ITEM_TYPE createFor(XAXIS_DATA_TYPE x) {
    try {
      Class<XAXIS_DATA_TYPE> xClass =
          ((Class<XAXIS_DATA_TYPE>) ((ParameterizedType) this.getClass().
              getGenericSuperclass()).getActualTypeArguments()[0]);
      Class<ITEM_TYPE> itemClass = ((Class<ITEM_TYPE>) ((ParameterizedType) this.getClass().
          getGenericSuperclass()).getActualTypeArguments()[2]);
      Constructor<ITEM_TYPE> constructor = itemClass.getDeclaredConstructor(xClass);
      constructor.setAccessible(true);
      return constructor.newInstance(x);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates or gets the item associated to the given x value.
   * @param xValue the x value.
   * @return
   */
  public ITEM_TYPE forX(final XAXIS_DATA_TYPE xValue) {
    ITEM_TYPE item = getItemFrom(xValue);
    if (item == null) {
      item = createFor(xValue);
      add(item);
    }
    return item;
  }
}
