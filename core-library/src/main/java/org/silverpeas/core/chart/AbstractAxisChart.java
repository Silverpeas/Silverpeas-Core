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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.chart;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.JSONCodec.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractAxisChart<X, Y, I extends
    AbstractAxisChartItem<X, Y, I>>
    extends AbstractChart<I> {

  private ChartAxis x = new ChartAxis();
  private ChartAxis y = new ChartAxis();
  private Map<X, I> itemIndexedByX = new HashMap<>();

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
  protected I getItemFrom(final X xValue) {
    return itemIndexedByX.get(xValue);
  }

  @Override
  protected <T extends AbstractChart<I>> T add(final I item) {
    itemIndexedByX.put(item.getX(), item);
    return super.add(item);
  }

  /**
   * Creates a new item with the given parameter as x value.
   * @return a new instance of char item.
   */
  @SuppressWarnings("unchecked")
  private I createFor(X x) {
    try {
      Class<X> xClass =
          ((Class<X>) ((ParameterizedType) this.getClass().
              getGenericSuperclass()).getActualTypeArguments()[0]);
      Class<I> itemClass = ((Class<I>) ((ParameterizedType) this.getClass().
          getGenericSuperclass()).getActualTypeArguments()[2]);
      Constructor<I> constructor = itemClass.getDeclaredConstructor(xClass);
      constructor.setAccessible(true);
      return constructor.newInstance(x);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Creates or gets the item associated to the given x value.
   * @param xValue the x value.
   * @return
   */
  public I forX(final X xValue) {
    I item = getItemFrom(xValue);
    if (item == null) {
      item = createFor(xValue);
      add(item);
    }
    return item;
  }
}
