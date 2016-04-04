/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chart.pie.PieChart;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p/>
 * @author BERTINL
 */
public abstract class AbstractPieChartBuilder {

  private static final int LEGEND_MAX_LENGTH = 100;
  private Map<String, StatItem> statsByInstance = null;
  protected static final String FINESSE_TOUS = "FINESSE_TOUS";
  protected static final String FINESSE_GROUPE = "FINESSE_GROUPE";
  protected static final String FINESSE_USER = "FINESSE_USER";
  private String niveauFinesse = FINESSE_TOUS;

  private void buildStatsByInstance() {

    // 0 - init new hashtable
    statsByInstance = new HashMap<String, StatItem>();

    // 1 - Get stats for KM access
    // Hashtable key=componentId, value=new String[3] {tout, groupe, user}
    Map<String, String[]> cmpStats = getCmpStats();

    // 2 - build statItems
    Iterator<String> it = cmpStats.keySet().iterator();
    String[] tabValue = null;
    while (it.hasNext()) {
      String cmpId = it.next();

      tabValue = cmpStats.get(cmpId);
      ComponentInstLight cmp = null;
      try {
        cmp = AdministrationServiceProvider.getAdminService().getComponentInstLight(cmpId);
      } catch (AdminException e) {
        SilverTrace.error("silverStatisticsPeas",
            "AbstractPieChartBuilder.buildStatsByInstance()",
            "root.EX_SQL_QUERY_FAILED", e);
      }
      if (cmp != null) {
        long[] countValues = new long[3];
        countValues[0] = 0;
        countValues[1] = 0;
        countValues[2] = 0;

        if (tabValue[0] != null) {
          countValues[0] = Long.parseLong(tabValue[0]);
        }
        if (tabValue[1] != null) {
          countValues[1] = Long.parseLong(tabValue[1]);
        }
        if (tabValue[2] != null) {
          countValues[2] = Long.parseLong(tabValue[2]);
        }

        StatItem item = new StatItem(cmpId, cmp.getLabel(), countValues);
        statsByInstance.put(cmpId, item);
      }
    }

    if (tabValue != null) {
      if (tabValue[1] != null) {
        niveauFinesse = FINESSE_GROUPE;
      }
      if (tabValue[2] != null) {
        niveauFinesse = FINESSE_USER;
      }
    }
  }

  private boolean isIdBelongsTo(String spaceId, String[] tabAllSpaceIds) {
    // spaceId de type WA123
    if ((spaceId != null) && (!spaceId.startsWith("WA"))) {
      spaceId = "WA" + spaceId;
    }

    for (String tabAllSpaceId : tabAllSpaceIds) {
      String theSpaceId = tabAllSpaceId; // de type WA123
      if ((theSpaceId != null) && (!theSpaceId.startsWith("WA"))) {
        theSpaceId = "WA" + theSpaceId;
      }
      if (theSpaceId != null && theSpaceId.equals(spaceId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param spaceId the space identifier
   * @param currentStats list of current statistics
   * @return a PieChart in 2D
   */
  public PieChart getChart(String spaceId, Vector<String[]> currentStats) {
    PieChart pieChart = null;
    try {
      // Get statistics
      if (statsByInstance == null) {
        buildStatsByInstance();
      }

      // Get subspaces ids and components
      String[] tabSpaceIds = null; // de type WA123
      String[] componentIds = null;

      // build instance list
      UserDetail userDetail = UserDetail.getCurrentRequester();
      if (!StringUtil.isDefined(spaceId)) {
        if (userDetail.isAccessAdmin()) {// Admin
          tabSpaceIds = AdministrationServiceProvider.getAdminService().getAllRootSpaceIds(); // de type WA123
        } else {// Manager d'espaces ou de sous-espaces
          // manager d'espace
          List<String> listSpaceIds = new ArrayList<String>();
          String[] tabManageableSpaceIds = AdministrationServiceProvider.getAdminService()
              .getUserManageableSpaceIds(userDetail.getId()); // de type
          // 123
          for (String manageableSpaceId : tabManageableSpaceIds) {
            SpaceInstLight espace =
                AdministrationServiceProvider.getAdminService().getSpaceInstLightById(manageableSpaceId);
            int level = espace.getLevel();
            boolean trouve = false;
            while (level > 0) {
              String idEspace = espace.getFatherId();
              espace = AdministrationServiceProvider.getAdminService().getSpaceInstLightById(idEspace);
              level--;
              if (isIdBelongsTo(idEspace, tabManageableSpaceIds)) {
                trouve = true;
                break;
              }
            }
            if (!trouve) {
              listSpaceIds.add(manageableSpaceId);
            }
          }
          tabSpaceIds = listSpaceIds.toArray(new String[listSpaceIds.size()]);
        }
        componentIds = new String[0];
      } else {
        tabSpaceIds = AdministrationServiceProvider.getAdminService().getAllSubSpaceIds(spaceId); // de type WA123
        componentIds = AdministrationServiceProvider.getAdminService().getAllComponentIds(spaceId);
      }

      // build data
      List<String> legend = new ArrayList<String>();
      List<String> counts = new ArrayList<String>();
      currentStats.clear();
      String[] allComponentsIds;

      // first managesubspaces
      for (String tabSpaceId : tabSpaceIds) {
        long count1 = 0L;
        long count2 = 0L;
        long count3 = 0L;

        SpaceInstLight space = AdministrationServiceProvider.getAdminService().getSpaceInstLightById(tabSpaceId);
        allComponentsIds = AdministrationServiceProvider.getAdminService().getAllComponentIdsRecur(tabSpaceId);

        for (String allComponentsId : allComponentsIds) {
          StatItem item = statsByInstance.get(allComponentsId);
          if (item != null) {
            count1 += item.getCountValues()[0];
            count2 += item.getCountValues()[1];
            count3 += item.getCountValues()[2];
          }
        }

        if (FINESSE_TOUS.equals(niveauFinesse)) {
          counts.add(String.valueOf(count1));
        } else if (FINESSE_GROUPE.equals(niveauFinesse)) {
          counts.add(String.valueOf(count2));
        } else if (FINESSE_USER.equals(niveauFinesse)) {
          counts.add(String.valueOf(count3));
        }

        legend.add(StringUtil.truncate(space.getName(), LEGEND_MAX_LENGTH));
        currentStats.add(new String[] { "SPACE", tabSpaceId, space.getName(),
            String.valueOf(count1),
            String.valueOf(count2), String.valueOf(count3) });
      }

      // then manage components
      for (String componentId : componentIds) {
        StatItem item = statsByInstance.get(componentId);
        if (item != null) {
          long count1 = item.getCountValues()[0];
          long count2 = item.getCountValues()[1];
          long count3 = item.getCountValues()[2];

          if (FINESSE_TOUS.equals(niveauFinesse)) {
            counts.add(String.valueOf(count1));
          } else if (FINESSE_GROUPE.equals(niveauFinesse)) {
            counts.add(String.valueOf(count2));
          } else if (FINESSE_USER.equals(niveauFinesse)) {
            counts.add(String.valueOf(count3));
          }
          legend.add(StringUtil.truncate(item.getName(), LEGEND_MAX_LENGTH));
          currentStats.add(new String[] { "CMP", componentId, item.getName(),
              String.valueOf(count1),
              String.valueOf(count2), String.valueOf(count3) });
        }
      }

      pieChart = getPieChartFrom(legend, counts).withTitle(getChartTitle());
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "AbstractPieChartBuilder.getChart()", "root.EX_SQL_QUERY_FAILED", e);
    }

    return pieChart;
  }

  @SuppressWarnings("unchecked")
  protected PieChart getPieChartFrom(List<String> labels, List values) {
    PieChart chart = PieChart.withoutTitle();
    Iterator<String> itLabels = labels.iterator();
    Iterator<Object> itValues = values.iterator();
    while (itLabels.hasNext()) {
      chart.add(itLabels.next(), Long.valueOf(String.valueOf(itValues.next())));
    }
    return chart;
  }

  protected void setScope(String scope) {
    niveauFinesse = scope;
  }

  public abstract String getChartTitle();

  // Hashtable key=componentId, value=new
  // String[3] {tout, groupe, user}
  abstract Map<String, String[]> getCmpStats();
}
