/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.HashMap;
import java.util.Map;

public class DocSizePieChartBuilder extends AbstractPieChartBuilder {

  private final String currentUserId;
  private final String spaceId;
  private final ResourceLocator message;

  public DocSizePieChartBuilder(String currentUserId, String spaceId, ResourceLocator message) {
    this.currentUserId = currentUserId;
    this.spaceId = spaceId;
    this.message = message;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder
   * #getChartTitle()
   */
  @Override
  public String getChartTitle() {
    String title = message.getString("silverStatisticsPeas.VolumeDocsSize") + " ";
    try {
      if (StringUtil.isDefined(this.spaceId) && (!this.spaceId.equals("WA0"))) {
        SpaceInstLight space = AdminReference.getAdminService().getSpaceInstLightById(this.spaceId);
        title += message.getString("silverStatisticsPeas.FromSpace") + " [" + space.getName() + "]";
      }
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "DocSizePieChartBuilder.getChartTitle()",
          "root.EX_SQL_QUERY_FAILED", e);
    }

    return title;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder
   * #getCmpStats()
   */
  @Override
  Map<String, String[]> getCmpStats() {
    try {
      return SilverStatisticsPeasDAOVolumeServer.getStatsSizeVentil(this.currentUserId);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "DocSizePieChartBuilder.getCmpStats()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return new HashMap<String, String[]>(0);
  }
}
