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
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chart.pie.PieChart;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
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
    statsByInstance = new HashMap<>();

    // 1 - Get stats for KM access
    Map<String, String[]> cmpStats = getCmpStats();

    // 2 - build statItems
    buildStatItems(cmpStats);
  }

  private void buildStatItems(final Map<String, String[]> cmpStats) {
    Mutable<String[]> tabValue = Mutable.empty();
    for (Map.Entry<String, String[]> stat : cmpStats.entrySet()) {
      String componentId = stat.getKey();
      tabValue.set(stat.getValue());
      OrganizationController.get().getComponentInstance(componentId)
          .ifPresent(i -> addStateItemForComponentInstance(tabValue.get(), i));
    }
    if (tabValue.isPresent()) {
      if (tabValue.get()[1] != null) {
        niveauFinesse = FINESSE_GROUPE;
      }
      if (tabValue.get()[2] != null) {
        niveauFinesse = FINESSE_USER;
      }
    }
  }

  private void addStateItemForComponentInstance(final String[] tabValue,
      final SilverpeasComponentInstance cmp) {
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

    StatItem item = new StatItem(cmp.getId(), cmp.getLabel(), countValues);
    statsByInstance.put(cmp.getId(), item);
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
  public PieChart getChart(String spaceId, List<String[]> currentStats) {
    PieChart pieChart = null;
    try {
      // Get statistics
      if (statsByInstance == null) {
        buildStatsByInstance();
      }

      // Get subspaces ids and components
      List<String> tabSpaceIds = new ArrayList<>(); // of type WA123
      List<String> componentIds = new ArrayList<>();

      // build instance list
      buildInstanceList(spaceId, tabSpaceIds, componentIds);

      // build data
      List<String> legend = new ArrayList<>();
      List<String> counts = new ArrayList<>();
      currentStats.clear();

      // first manage subspaces
      buildStatsForSubspaces(currentStats, tabSpaceIds, legend, counts);

      // then manage components
      buildStatsForComponents(currentStats, componentIds, legend, counts);

      pieChart = getPieChartFrom(legend, counts).withTitle(getChartTitle());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }

    return pieChart;
  }

  private void buildStatsForComponents(final List<String[]> currentStats,
      final List<String> componentIds, final List<String> legend, final List<String> counts) {
    for (String componentId : componentIds) {
      StatItem item = statsByInstance.get(componentId);
      if (item != null) {
        long count1 = item.getCountValues()[0];
        long count2 = item.getCountValues()[1];
        long count3 = item.getCountValues()[2];

        updateCountByFinesse(counts, count1, count2, count3);
        legend.add(StringUtil.truncate(item.getName(), LEGEND_MAX_LENGTH));
        currentStats.add(new String[]{"CMP", componentId, item.getName(), String.valueOf(count1),
            String.valueOf(count2), String.valueOf(count3)});
      }
    }
  }

  private void buildStatsForSubspaces(final List<String[]> currentStats,
      final List<String> tabSpaceIds, final List<String> legend, final List<String> counts)
      throws AdminException {
    for (String tabSpaceId : tabSpaceIds) {
      long count1 = 0L;
      long count2 = 0L;
      long count3 = 0L;

      SpaceInstLight space =
          AdministrationServiceProvider.getAdminService().getSpaceInstLightById(tabSpaceId);
      final String[] allComponentsIds =
          AdministrationServiceProvider.getAdminService().getAllComponentIdsRecur(tabSpaceId);

      for (String allComponentsId : allComponentsIds) {
        StatItem item = statsByInstance.get(allComponentsId);
        if (item != null) {
          count1 += item.getCountValues()[0];
          count2 += item.getCountValues()[1];
          count3 += item.getCountValues()[2];
        }
      }

      updateCountByFinesse(counts, count1, count2, count3);

      legend.add(StringUtil.truncate(space.getName(), LEGEND_MAX_LENGTH));
      currentStats.add(new String[]{"SPACE", tabSpaceId, space.getName(), String.valueOf(count1),
          String.valueOf(count2), String.valueOf(count3)});
    }
  }

  private void updateCountByFinesse(final List<String> counts, final long count1, final long count2,
      final long count3) {
    if (FINESSE_TOUS.equals(niveauFinesse)) {
      counts.add(String.valueOf(count1));
    } else if (FINESSE_GROUPE.equals(niveauFinesse)) {
      counts.add(String.valueOf(count2));
    } else if (FINESSE_USER.equals(niveauFinesse)) {
      counts.add(String.valueOf(count3));
    }
  }

  private void buildInstanceList(final String spaceId, final List<String> tabSpaceIds,
      final List<String> componentIds) throws AdminException {
    UserDetail userDetail = UserDetail.getCurrentRequester();
    if (!StringUtil.isDefined(spaceId)) {
      if (userDetail.isAccessAdmin()) {
        tabSpaceIds.addAll(
            Arrays.asList(AdministrationServiceProvider.getAdminService().getAllRootSpaceIds()));
      } else {
        List<String> listSpaceIds = new ArrayList<>();
        String[] tabManageableSpaceIds = AdministrationServiceProvider.getAdminService()
            .getUserManageableSpaceIds(userDetail.getId()); // of type 123
        buildManageableSubSpaces(listSpaceIds, tabManageableSpaceIds);
        tabSpaceIds.addAll(listSpaceIds);
      }
      componentIds.clear();
    } else {
      tabSpaceIds.addAll(Arrays.asList(
          AdministrationServiceProvider.getAdminService().getAllSubSpaceIds(spaceId)));
      componentIds.addAll(Arrays.asList(
          AdministrationServiceProvider.getAdminService().getAllComponentIds(spaceId)));
    }
  }

  private void buildManageableSubSpaces(final List<String> listSpaceIds,
      final String[] tabManageableSpaceIds) throws AdminException {
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

  abstract Map<String, String[]> getCmpStats();
}
