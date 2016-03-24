/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import java.util.ArrayList;
import java.util.List;

/**
 * A criterion on the axis' value to take into account in a search, selection or any process that
 * supports filtering.
 */
public class AxisValueCriterion extends SearchCriteria {

  private static final long serialVersionUID = 6685902813764382082L;

  /**
   * Converts the axis' values encoded into the specified string into a list of criterion on an
   * axis' value.
   *
   * @param flattenAxisValues the string of comma-separated axis' values. Each value is represented
   * by the token 'axis id':'value id' where the identifier of the value is its path from the root
   * value of the axis it belongs to.
   * @return a list of AxisValueCriterion instances for each value encoded into the string.
   */
  public static List<AxisValueCriterion> fromFlattenedAxisValues(String flattenAxisValues) {
    List<AxisValueCriterion> criteria = new ArrayList<>();
    if (StringUtil.isDefined(flattenAxisValues)) {
      String[] allAxisValues = flattenAxisValues.split(",");
      for (String anAxisValue : allAxisValues) {
        String[] axisValuePair = anAxisValue.split(":");
        AxisValueCriterion criterion = new AxisValueCriterion(axisValuePair[0], axisValuePair[1]);
        criteria.add(criterion);
      }
    }
    return criteria;
  }

  /**
   * Constructs a new criterion on the specified axis' value
   *
   * @param axisId the unique identifier of the axis.
   * @param valuePath the path of the value of the axis above from the root axis value.
   */
  public AxisValueCriterion(String axisId, String valuePath) {
    super(Integer.valueOf(axisId), valuePath);
  }

  /**
   * Gets the path of the axis' value from the root axis' value.
   *
   * @return the value path.
   */
  public String getValuePath() {
    return getValue();
  }
}
