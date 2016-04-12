/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.HashMap;
import java.util.Map;

public class DocPieChartBuilder extends AbstractPieChartBuilder {

  private final String spaceId;
  private final LocalizationBundle message;

  public DocPieChartBuilder(String spaceId, LocalizationBundle message) {
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
    String title = message.getString("silverStatisticsPeas.VolumeDocsNumber") + " ";

    try {
      if (StringUtil.isDefined(this.spaceId) && (!"WA0".equals(this.spaceId))) {
        SpaceInstLight space = AdministrationServiceProvider.getAdminService().getSpaceInstLightById(this.spaceId);
        title += message.getString("silverStatisticsPeas.FromSpace") + " [" + space.getName() + "]";
      }
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "DocPieChartBuilder.getChartTitle()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return title;
  }

  /*
   * Implements getCmpStats of AbstractPieChartBuilder class (non-Javadoc)
   * @see com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder
   * #getCmpStats()
   */
  @Override
  public Map<String, String[]> getCmpStats() {
    try {
      return SilverStatisticsPeasDAOVolumeServer.getStatsVentil();
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "DocPieChartBuilder.getCmpStats()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return new HashMap<String, String[]>(0);
  }
}
