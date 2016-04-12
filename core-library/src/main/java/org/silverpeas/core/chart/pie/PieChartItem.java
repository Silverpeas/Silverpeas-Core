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

import org.silverpeas.core.chart.AbstractChartItem;
import org.silverpeas.core.util.JSONCodec.JSONObject;

/**
 * @author Yohann Chastagnier
 */
public class PieChartItem extends AbstractChartItem<PieChartItem> {

  private final String label;
  private final Number value;

  PieChartItem(final String label, final Number value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public Number getValue() {
    return value;
  }

  @Override
  protected void completeJsonData(JSONObject itemAsJson) {
    itemAsJson.put("label", getLabel()).put("value", getValue());
  }
}
