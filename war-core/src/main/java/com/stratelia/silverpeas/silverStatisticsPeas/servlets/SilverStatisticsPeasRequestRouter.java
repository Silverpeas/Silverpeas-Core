/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.silverStatisticsPeas.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silverStatisticsPeas.control.SilverStatisticsPeasSessionController;
import com.stratelia.silverpeas.silverStatisticsPeas.vo.AxisStatsFilter;
import com.stratelia.silverpeas.silverStatisticsPeas.vo.CrossAxisStatsFilter;
import com.stratelia.silverpeas.silverStatisticsPeas.vo.CrossStatisticVO;
import com.stratelia.silverpeas.silverStatisticsPeas.vo.StatisticVO;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.jCharts.Chart;
import org.jCharts.nonAxisChart.PieChart2D;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class SilverStatisticsPeasRequestRouter extends
    ComponentRequestRouter<SilverStatisticsPeasSessionController> {

  private static final long serialVersionUID = -7422373100761515806L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public SilverStatisticsPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SilverStatisticsPeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "SilverStatisticsPeas";
  }

  /**
   * This method has to be implemented by the component request router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param statsSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SilverStatisticsPeasSessionController statsSC,
      HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + statsSC.getUserId()
        + " Function=" + function);

    String userProfile = statsSC.getUserProfile();
    if ("A".equals(userProfile)
        || SilverStatisticsPeasSessionController.SPACE_ADMIN.equals(userProfile)) {
      request.setAttribute("UserProfile", userProfile);
    } else {
      SilverTrace.info("silverStatisticsPeas",
          "SilverStatisticsPeasRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "userProfile=" + userProfile);
      return null;
    }

    Calendar today = Calendar.getInstance();
    String currentMonth = "" + today.get(Calendar.MONTH);
    String currentYear = "" + today.get(Calendar.YEAR);

    try {
      if (function.startsWith("Main")) {
        // We only enter in the main case on the first access to the
        // silverStatistics pages
        request.setAttribute("ConnectedUsersList", statsSC
            .getConnectedUsersList());
        destination = "/silverStatisticsPeas/jsp/connections.jsp";
      } else if (function.equals("KickSession")) {
        statsSC.KickSession(request.getParameter("theSessionId"));
        request.setAttribute("ConnectedUsersList", statsSC.getConnectedUsersList());
        destination = "/silverStatisticsPeas/jsp/connections.jsp";
      } else if (function.equals("DisplayNotifySession")) {
        request.setAttribute("userDetail", statsSC.getTargetUserDetail(request
            .getParameter("theUserId")));
        request.setAttribute("action", "NotifyUser");
        destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
      } else if (function.equals("DisplayNotifyAllSessions")) {
        request.setAttribute("action", "NotifyAllUsers");
        destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
      } else if (function.equals("ToAlert")) {
        statsSC
            .notifySession(request.getParameter("theUserId"), request.getParameter("messageAux"));
        request.setAttribute("action", "Close");
        destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
      } else if (function.equals("ToAlertAllUsers")) {
        statsSC.notifyAllSessions(statsSC.getConnectedUsersList(), request
            .getParameter("messageAux"));
        request.setAttribute("action", "Close");
        destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
      } else if (function.startsWith("ViewConnections")) {
        statsSC.setMonthBegin(currentMonth);
        statsSC.setYearBegin(currentYear);
        statsSC.setMonthEnd(currentMonth);
        statsSC.setYearEnd(currentYear);
        statsSC.setActorDetail("0");
        statsSC.setFilterType("");
        statsSC.setFilterLib("");
        statsSC.setFilterId("");

        // init formulaire
        request.setAttribute("MonthBegin", statsSC.getMonth(currentMonth));
        request.setAttribute("YearBegin", statsSC.getYearConnection(currentYear));
        request.setAttribute("MonthEnd", statsSC.getMonth(currentMonth));
        request.setAttribute("YearEnd", statsSC.getYearConnection(currentYear));
        request.setAttribute("ActorDetail", statsSC.getDetail("0"));
        request.setAttribute("FilterType", "");
        request.setAttribute("FilterLib", "");
        request.setAttribute("FilterId", "");

        destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
      } else if (function.startsWith("ValidateViewConnection")) {
        // save request param
        saveConnectionParam(request, statsSC);

        String hostMonthBegin = statsSC.getMonthBegin();
        String hostYearBegin = statsSC.getYearBegin();
        String hostMonthEnd = statsSC.getMonthEnd();
        String hostYearEnd = statsSC.getYearEnd();
        String hostDateBegin = getRequestDate(hostYearBegin, hostMonthBegin);
        String hostDateEnd = getRequestDate(hostYearEnd, hostMonthEnd);
        String hostStatDetail = statsSC.getActorDetail();
        String filterType = statsSC.getFilterType();
        String filterId = statsSC.getFilterId();

        // compute result
        if ("0".equals(hostStatDetail))// All
        {
          if (!StringUtil.isDefined(filterType)) // no filter
          {
            request.setAttribute("ResultData", statsSC.getStatsConnexionAllAll(hostDateBegin,
                hostDateEnd));

            // graphiques
            request.setAttribute("GraphicDistinctUser", Boolean.TRUE);
            Chart loginChart = statsSC.getDistinctUserConnectionsChart(hostDateBegin, hostDateEnd);
            request.getSession(true).setAttribute(ChartServlet.LOGINCHART, loginChart);

            Chart userChart = statsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
            request.getSession(true).setAttribute(ChartServlet.USERCHART,
                userChart);

          } else if ("0".equals(filterType)) // filter group
          {
            request.setAttribute("ResultData",
                statsSC.getStatsConnexionAllGroup(hostDateBegin,
                hostDateEnd, filterId));

            // graphiques
            Chart userChart = statsSC.getUserConnectionsGroupChart(
                hostDateBegin, hostDateEnd, filterId);
            request.getSession(true).setAttribute(ChartServlet.USERCHART,
                userChart);
          } else if ("1".equals(filterType)) // filter user
          {
            request.setAttribute("ResultData", statsSC.getStatsConnexionAllUser(hostDateBegin,
                hostDateEnd, filterId));

            // graphiques
            Chart userChart =
                statsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, filterId);
            request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
          }
        } else if ("1".equals(hostStatDetail))// Groups
        {
          if (filterType.equals("")) // no filter
          {
            request.setAttribute("ResultData", statsSC
                .getStatsConnexionGroupAll(hostDateBegin, hostDateEnd));

            String entiteId = request.getParameter("EntiteId");

            if (entiteId != null) {
              // graphiques
              Chart userChart =
                  statsSC.getUserConnectionsGroupChart(hostDateBegin, hostDateEnd, entiteId);
              request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
            } else {
              // graphiques
              Chart userChart = statsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
              request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
            }
          } else if ("0".equals(filterType)) // filter group
          {
            request.setAttribute("ResultData", statsSC.getStatsConnexionGroupUser(hostDateBegin,
                hostDateEnd, filterId));

            // graphiques
            Chart userChart =
                statsSC.getUserConnectionsGroupChart(hostDateBegin, hostDateEnd, filterId);
            request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
          }
        } else if (hostStatDetail.equals("2"))// Users
        {
          if (!StringUtil.isDefined(filterType)) // no filter
          {
            request.setAttribute("ResultData", statsSC.getStatsConnexionUserAll(hostDateBegin,
                hostDateEnd));

            String entiteId = request.getParameter("EntiteId");

            if (entiteId != null) {
              // graphiques
              Chart userChart =
                  statsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, entiteId);
              request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
            } else {
              // graphiques
              Chart userChart = statsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
              request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
            }
          } else if (filterType.equals("1")) // filter user
          {
            request.setAttribute("ResultData", statsSC.getStatsConnexionUserUser(hostDateBegin,
                hostDateEnd, filterId));

            // graphiques
            Chart userChart =
                statsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, filterId);
            request.getSession(true).setAttribute(ChartServlet.USERCHART, userChart);
          }
        }

        restoreConnectionParam(request, statsSC);
        destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
      } else if (function.startsWith("ExportViewConnection")) {
        String hostMonthBegin = statsSC.getMonthBegin();
        String hostYearBegin = statsSC.getYearBegin();
        String hostMonthEnd = statsSC.getMonthEnd();
        String hostYearEnd = statsSC.getYearEnd();
        String hostDateBegin = getRequestDate(hostYearBegin, hostMonthBegin);
        String hostDateEnd = getRequestDate(hostYearEnd, hostMonthEnd);
        String hostStatDetail = statsSC.getActorDetail();
        String filterType = statsSC.getFilterType();
        String filterId = statsSC.getFilterId();

        // compute result
        if ("0".equals(hostStatDetail))// All
        {
          if ("".equals(filterType)) { // no filter
            request.setAttribute("ResultData", statsSC.getStatsConnexionAllAll(hostDateBegin,
                hostDateEnd));
          } else if ("0".equals(filterType)) { // filter group
            request.setAttribute("ResultData", statsSC.getStatsConnexionAllGroup(hostDateBegin,
                hostDateEnd, filterId));

          } else if ("1".equals(filterType)) { // filter user
            request.setAttribute("ResultData", statsSC.getStatsConnexionAllUser(hostDateBegin,
                hostDateEnd, filterId));
          }
        } else if ("1".equals(hostStatDetail)) { // Groups
          if ("".equals(filterType)) { // no filter
            request.setAttribute("ResultData", statsSC
                .getStatsConnexionGroupAll(hostDateBegin, hostDateEnd));
          } else if ("0".equals(filterType)) { // filter group
            request.setAttribute("ResultData", statsSC
                .getStatsConnexionGroupUser(hostDateBegin, hostDateEnd, filterId));
          }
        } else if (hostStatDetail.equals("2")) { // Users
          if ("".equals(filterType)) { // no filter
            request.setAttribute("ResultData", statsSC
                .getStatsConnexionUserAll(hostDateBegin, hostDateEnd));
          } else if ("1".equals(filterType)) { // filter user
            request.setAttribute("ResultData",
                statsSC.getStatsConnexionUserUser(hostDateBegin, hostDateEnd, filterId));
          }
        }

        destination = "/silverStatisticsPeas/jsp/exportViewConnection.jsp";
      } else if (function.startsWith("CallUserPanel")) {
        // save request param
        saveConnectionParam(request, statsSC);

        // init user panel
        destination = statsSC.initUserPanel();
      } else if (function.startsWith("ReturnFromUserPanel")) {
        // get user panel data (update FilterType and FilterLib, FilterId)
        statsSC.retourUserPanel();

        // restore request param
        restoreConnectionParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
      } else if (function.startsWith("ViewFrequence")) {
        statsSC.setMonthBegin(currentMonth);
        statsSC.setYearBegin(currentYear);
        statsSC.setMonthEnd(currentMonth);
        statsSC.setYearEnd(currentYear);
        statsSC.setFrequenceDetail("0");

        // init formulaire
        request.setAttribute("MonthBegin", statsSC.getMonth(currentMonth));
        request.setAttribute("YearBegin", statsSC.getYearConnection(currentYear));
        request.setAttribute("MonthEnd", statsSC.getMonth(currentMonth));
        request.setAttribute("YearEnd", statsSC.getYearConnection(currentYear));
        request.setAttribute("FrequenceDetail", statsSC.getFrequenceDetail("0"));

        destination = "/silverStatisticsPeas/jsp/viewFrequence.jsp";
      } else if (function.startsWith("ValidateViewFrequence")) {
        // save request param
        String hostMonthBegin = request.getParameter("MonthBegin");
        if (hostMonthBegin != null && !hostMonthBegin.equals("")) {
          statsSC.setMonthBegin(request.getParameter("MonthBegin"));
          statsSC.setYearBegin(request.getParameter("YearBegin"));
          statsSC.setMonthEnd(request.getParameter("MonthEnd"));
          statsSC.setYearEnd(request.getParameter("YearEnd"));
          statsSC.setFrequenceDetail(request.getParameter("FrequenceDetail"));
        }

        hostMonthBegin = statsSC.getMonthBegin();
        String hostYearBegin = statsSC.getYearBegin();
        String hostMonthEnd = statsSC.getMonthEnd();
        String hostYearEnd = statsSC.getYearEnd();
        String hostDateBegin = getRequestDate(hostYearBegin, hostMonthBegin);
        String hostDateEnd = getRequestDate(hostYearEnd, hostMonthEnd);
        String hostStatDetail = statsSC.getFrequenceDetail();

        // graphiques
        Chart userFqChart = statsSC.getUserConnectionsFqChart(
            hostDateBegin, hostDateEnd, hostStatDetail);
        request.getSession(true).setAttribute(ChartServlet.USERFQCHART, userFqChart);
        request.setAttribute("Graphic", Boolean.TRUE);

        request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
        request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
        request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
        request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
        request.setAttribute("FrequenceDetail", statsSC.getFrequenceDetail(statsSC
            .getFrequenceDetail()));

        destination = "/silverStatisticsPeas/jsp/viewFrequence.jsp";
      } else if (function.startsWith("ViewAccess")) {
        // Onglet Acces
        statsSC.setAccessMonthBegin(currentMonth);
        statsSC.setAccessYearBegin(currentYear);
        statsSC.setAccessFilterLibGroup("");
        statsSC.setAccessFilterIdGroup("");
        statsSC.setAccessFilterLibUser("");
        statsSC.setAccessFilterIdUser("");
        statsSC.setAccessSpaceId("");
        statsSC.clearCurrentStats();

        // init formulaire access
        request.setAttribute("MonthBegin", statsSC.getMonth(currentMonth));
        request.setAttribute("YearBegin", statsSC.getYearAccess(currentYear));
        request.setAttribute("FilterLibGroup", statsSC.getAccessFilterLibGroup());
        request.setAttribute("FilterIdGroup", statsSC.getAccessFilterIdGroup());
        request.setAttribute("FilterLibUser", statsSC.getAccessFilterLibUser());
        request.setAttribute("FilterIdUser", statsSC.getAccessFilterIdUser());
        request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
        request.setAttribute("Path", statsSC.getPath());

        destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
      } else if (function.startsWith("ValidateViewAccess")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        String hostMonthBegin = statsSC.getAccessMonthBegin();
        String hostYearBegin = statsSC.getAccessYearBegin();
        String filterIdGroup = statsSC.getAccessFilterIdGroup();
        String filterIdUser = statsSC.getAccessFilterIdUser();
        String spaceId = statsSC.getAccessSpaceId();

        // compute result
        PieChart2D pieChart = statsSC.getUserVentilChart(
            getRequestDate(hostYearBegin, hostMonthBegin), filterIdGroup,
            filterIdUser, spaceId);
        request.getSession(true).setAttribute(ChartServlet.USERVENTILCHART,
            pieChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        // restore request param
        restoreAccessParam(request, statsSC);
        destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
      } else if (function.startsWith("AccessCallUserPanelGroup")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        // init user panel
        destination = statsSC.initAccessUserPanelGroup();
      } else if (function.startsWith("AccessReturnFromUserPanelGroup")) {
        // get user panel data (update FilterLib, FilterId)
        statsSC.retourAccessUserPanelGroup();

        // restore request param
        restoreAccessParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
      } else if (function.startsWith("AccessCallUserPanelUser")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        // init user panel
        destination = statsSC.initAccessUserPanelUser();
      } else if (function.startsWith("AccessReturnFromUserPanelUser")) {
        // get user panel data (update FilterLib, FilterId)
        statsSC.retourAccessUserPanelUser();

        // restore request param
        restoreAccessParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
      } else if (function.startsWith("ExportAccess.txt")) {
        // compute result
        request.setAttribute("FilterIdGroup", statsSC.getAccessFilterIdGroup());
        request.setAttribute("FilterIdUser", statsSC.getAccessFilterIdUser());
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        destination = "/silverStatisticsPeas/jsp/exportDataAccess.jsp";
      } else if (function.startsWith("ViewEvolutionAccess")) {
        String entite = request.getParameter("Entite");
        String entiteId = request.getParameter("Id");

        String filterLibGroup = statsSC.getAccessFilterLibGroup();
        String filterIdGroup = statsSC.getAccessFilterIdGroup();
        String filterLibUser = statsSC.getAccessFilterLibUser();
        String filterIdUser = statsSC.getAccessFilterIdUser();

        // compute result
        Chart lineChart =
            statsSC.getEvolutionUserChart(entite, entiteId, filterLibGroup, filterIdGroup,
            filterLibUser, filterIdUser);
        request.getSession(true).setAttribute(ChartServlet.EVOLUTIONUSERCHART, lineChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        // restore request param
        request.setAttribute("Entite", entite);
        request.setAttribute("Id", entiteId);
        restoreAccessParam(request, statsSC);
        destination = "/silverStatisticsPeas/jsp/viewEvolutionAccess.jsp";
      } else if (function.startsWith("ViewVolumeServices")) {
        // Onglet Volume
        if (!userProfile.equals("A")) {
          return getDestination("ViewVolumePublication", statsSC, request);
        }

        PieChart2D pieChart = statsSC.getVolumeServicesChart();
        request.getSession(true).setAttribute(ChartServlet.KMINSTANCESCHART, pieChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        destination = "/silverStatisticsPeas/jsp/viewVolumeServices.jsp";
      } else if (function.startsWith("ViewVolumePublication")) {
        statsSC.setAccessMonthBegin(currentMonth);
        statsSC.setAccessYearBegin(currentYear);
        statsSC.setAccessFilterLibGroup("");
        statsSC.setAccessFilterIdGroup("");
        statsSC.setAccessFilterLibUser("");
        statsSC.setAccessFilterIdUser("");
        statsSC.setAccessSpaceId("");
        statsSC.clearCurrentStats();

        // init formulaire
        request.setAttribute("MonthBegin", statsSC.getMonth(currentMonth));
        request.setAttribute("YearBegin", statsSC.getYearVolume(currentYear));
        request.setAttribute("FilterLibGroup", statsSC.getAccessFilterLibGroup());
        request.setAttribute("FilterIdGroup", statsSC.getAccessFilterIdGroup());
        request.setAttribute("FilterLibUser", statsSC.getAccessFilterLibUser());
        request.setAttribute("FilterIdUser", statsSC.getAccessFilterIdUser());
        request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
        request.setAttribute("Path", statsSC.getPath());

        destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
      } else if (function.startsWith("ValidateViewVolume")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        String hostMonthBegin = statsSC.getAccessMonthBegin();
        String hostYearBegin = statsSC.getAccessYearBegin();
        String filterIdGroup = statsSC.getAccessFilterIdGroup();
        String filterIdUser = statsSC.getAccessFilterIdUser();
        String spaceId = statsSC.getAccessSpaceId();

        // compute result
        PieChart2D pieChart =
            statsSC.getPubliVentilChart(getRequestDate(hostYearBegin, hostMonthBegin),
            filterIdGroup, filterIdUser, spaceId);
        request.getSession(true).setAttribute(ChartServlet.PUBLIVENTILCHART, pieChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        // restore request param
        restoreVolumeParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
      } else if (function.startsWith("VolumeCallUserPanelGroup")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        // init user panel
        destination = statsSC.initVolumeUserPanelGroup();
      } else if (function.startsWith("VolumeReturnFromUserPanelGroup")) {
        // get user panel data (update FilterLib, FilterId)
        statsSC.retourAccessUserPanelGroup();

        // restore request param
        restoreVolumeParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
      } else if (function.startsWith("VolumeCallUserPanelUser")) {
        // save request param
        saveAccessVolumeParam(request, statsSC);

        // init user panel
        destination = statsSC.initVolumeUserPanelUser();
      } else if (function.startsWith("VolumeReturnFromUserPanelUser")) {
        // get user panel data (update FilterLib, FilterId)
        statsSC.retourAccessUserPanelUser();

        // restore request param
        restoreVolumeParam(request, statsSC);

        destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
      }

      // Nbre de fichiers joints sur le serveur
      else if (function.startsWith("ViewVolumeServer")) {
        statsSC.setAccessSpaceId(request.getParameter("SpaceId"));
        String spaceId = statsSC.getAccessSpaceId();

        // compute result
        PieChart2D pieChart = statsSC.getDocsVentilChart(spaceId);
        request.getSession(true).setAttribute(ChartServlet.DOCVENTILCHART, pieChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());
        request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
        request.setAttribute("Path", statsSC.getPath());

        destination = "/silverStatisticsPeas/jsp/viewVolumeServer.jsp";
      } else if (function.startsWith("ViewVolumeSizeServer")) {
        statsSC.setAccessSpaceId(request.getParameter("SpaceId"));
        String spaceId = statsSC.getAccessSpaceId();

        // compute result
        PieChart2D pieChart = statsSC.getDocsSizeVentilChart(spaceId);
        request.getSession(true).setAttribute(ChartServlet.DOCSIZEVENTILCHART, pieChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());
        request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
        request.setAttribute("Path", statsSC.getPath());

        destination = "/silverStatisticsPeas/jsp/viewVolumeSizeServer.jsp";
      } else if (function.startsWith("ViewEvolutionVolumeSizeServer")) {
        // compute result
        Chart lineChart = statsSC.getEvolutionDocsSizeChart();
        request.getSession(true).setAttribute(ChartServlet.EVOLUTIONDOCSIZECHART, lineChart);
        request.setAttribute("StatsData", statsSC.getCurrentStats());

        destination = "/silverStatisticsPeas/jsp/viewEvolutionVolumeSizeServer.jsp";
      } else if (function.startsWith("ViewPDCAccess")) {
        // Initialize statistics session controller parameter
        statsSC.setMonthBegin(currentMonth);
        statsSC.setYearBegin(currentYear);
        statsSC.setMonthEnd(currentMonth);
        statsSC.setYearEnd(currentYear);
        statsSC.clearCurrentStats();

        // init formulaire access
        request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
        request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
        request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
        request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
        // Add setter on PDC
        // request.setAttribute("PrimaryAxis", statsSC.getPrimaryAxis());
        // request.setAttribute("StatsData", statsSC.getAxisStats(statsFilter));

        destination = "/silverStatisticsPeas/jsp/viewAccessPDC.jsp";
      } else if (function.startsWith("ValidateViewPDCAccess")) {
        // save request param
        saveAccessPDCParam(request, statsSC);

        String monthBegin = getMonthParam(statsSC.getMonthBegin());
        String yearBegin = statsSC.getYearBegin();
        String monthEnd = getMonthParam(statsSC.getMonthEnd());
        String yearEnd = statsSC.getYearEnd();

        AxisStatsFilter axisStatsFilter =
            new AxisStatsFilter(monthBegin, yearBegin, monthEnd, yearEnd);

        // Retrieve selected axis from request
        String axisId = request.getParameter("AxisId");
        if (StringUtil.isDefined(axisId)) {
          // set this data inside AxisStatsFilter
          axisStatsFilter.setAxisId(Integer.parseInt(axisId));
        }
        String axisValue = request.getParameter("AxisValue");
        if (StringUtil.isDefined(axisValue)) {
          axisStatsFilter.setAxisValue(axisValue);
        }

        // compute result
        List<StatisticVO> axisStats = statsSC.getAxisStats(axisStatsFilter);
        // restore request param
        request.setAttribute("StatsData", axisStats);
        request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
        request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
        request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
        request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
        request.setAttribute("AxisId", axisId);
        request.setAttribute("AxisValue", axisValue);

        destination = "/silverStatisticsPeas/jsp/viewAccessPDC.jsp";
      } else if (function.startsWith("ViewCrossPDCAccess")) {
        statsSC.setMonthBegin(currentMonth);
        statsSC.setYearBegin(currentYear);
        statsSC.setMonthEnd(currentMonth);
        statsSC.setYearEnd(currentYear);

        statsSC.clearCurrentStats();

        // init formulaire access
        request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
        request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
        request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
        request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
        // Add setter on PDC
        request.setAttribute("PrimaryAxis", statsSC.getPrimaryAxis());
        // request.setAttribute("StatsData", statsSC.getAxisStats(statsFilter));
        destination = "/silverStatisticsPeas/jsp/viewCrossPDCAccess.jsp";
      } else if (function.startsWith("ValidateViewCrossPDCAccess")) {
        // save request param
        saveAccessPDCParam(request, statsSC);

        String monthBegin = getMonthParam(statsSC.getMonthBegin());
        String yearBegin = statsSC.getYearBegin();
        String monthEnd = getMonthParam(statsSC.getMonthEnd());
        String yearEnd = statsSC.getYearEnd();

        // Retrieve selected axis from request
        int firstAxisId = Integer.parseInt(request.getParameter("FirstAxis"));
        int secondAxisId = Integer.parseInt(request.getParameter("SecondAxis"));

        // Initialize cross axis stats filter
        CrossAxisStatsFilter axisStatsFilter =
            new CrossAxisStatsFilter(monthBegin, yearBegin, monthEnd, yearEnd, firstAxisId,
            secondAxisId);

        CrossStatisticVO crossAxisStats = statsSC.getCrossAxisStats(axisStatsFilter);

        request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
        request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
        request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
        request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
        // Add PDC data inside request
        request.setAttribute("PrimaryAxis", statsSC.getPrimaryAxis());
        request.setAttribute("FirstAxis", firstAxisId);
        request.setAttribute("SecondAxis", secondAxisId);
        request.setAttribute("StatsData", crossAxisStats);

        destination = "/silverStatisticsPeas/jsp/viewCrossPDCAccess.jsp";
      } else {
        destination = "/silverStatisticsPeas/jsp/" + function;
      }

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
   * @param monthParam string month parameter
   * @return String representation of the month with two digits
   */
  private String getMonthParam(String monthParam) {
    String monthBegin = "" + (Integer.parseInt(monthParam) + 1);
    if (monthBegin.length() < 2) {
      monthBegin = "0" + monthBegin;
    }
    return monthBegin;
  }

  /**
   * Set statistics session controller attributes from HttpServletRequest
   * @param request which contains the parameters
   * @param statsSC the statistics session controller Object
   */
  private void saveConnectionParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    String hostMonthBegin = request.getParameter("MonthBegin");
    if (hostMonthBegin != null && !hostMonthBegin.equals("")) {
      statsSC.setMonthBegin(request.getParameter("MonthBegin"));
      statsSC.setYearBegin(request.getParameter("YearBegin"));
      statsSC.setMonthEnd(request.getParameter("MonthEnd"));
      statsSC.setYearEnd(request.getParameter("YearEnd"));
      statsSC.setActorDetail(request.getParameter("ActorDetail"));
      statsSC.setFilterType(request.getParameter("FilterType"));
      statsSC.setFilterLib(request.getParameter("FilterLib"));
      statsSC.setFilterId(request.getParameter("FilterId"));
    }
  }

  /**
   * Set connection parameter in request attributes
   * @param request the HttpServlet
   * @param statsSC the SilverStatisticsPeasSessionController object
   */
  private void restoreConnectionParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getMonthBegin()));
    request.setAttribute("YearBegin", statsSC.getYearConnection(statsSC.getYearBegin()));
    request.setAttribute("MonthEnd", statsSC.getMonth(statsSC.getMonthEnd()));
    request.setAttribute("YearEnd", statsSC.getYearConnection(statsSC.getYearEnd()));
    request.setAttribute("ActorDetail", statsSC.getDetail(statsSC.getActorDetail()));
    request.setAttribute("FilterType", statsSC.getFilterType());
    request.setAttribute("FilterLib", statsSC.getFilterLib());
    request.setAttribute("FilterId", statsSC.getFilterId());
  }

  private void saveAccessVolumeParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    String hostMonthBegin = request.getParameter("MonthBegin");
    if (hostMonthBegin != null && !hostMonthBegin.equals("")) {
      statsSC.setAccessMonthBegin(request.getParameter("MonthBegin"));
      statsSC.setAccessYearBegin(request.getParameter("YearBegin"));
      statsSC.setAccessFilterLibGroup(request.getParameter("FilterLibGroup"));
      statsSC.setAccessFilterIdGroup(request.getParameter("FilterIdGroup"));
      statsSC.setAccessFilterLibUser(request.getParameter("FilterLibUser"));
      statsSC.setAccessFilterIdUser(request.getParameter("FilterIdUser"));
      statsSC.setAccessSpaceId(request.getParameter("SpaceId"));
    }
  }

  private void restoreAccessParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getAccessMonthBegin()));
    request.setAttribute("YearBegin", statsSC.getYearAccess(statsSC.getAccessYearBegin()));
    request.setAttribute("FilterLibGroup", statsSC.getAccessFilterLibGroup());
    request.setAttribute("FilterIdGroup", statsSC.getAccessFilterIdGroup());
    request.setAttribute("FilterLibUser", statsSC.getAccessFilterLibUser());
    request.setAttribute("FilterIdUser", statsSC.getAccessFilterIdUser());
    request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
    request.setAttribute("Path", statsSC.getPath());
  }

  private void restoreVolumeParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    request.setAttribute("MonthBegin", statsSC.getMonth(statsSC.getAccessMonthBegin()));
    request.setAttribute("YearBegin", statsSC.getYearVolume(statsSC.getAccessYearBegin()));
    request.setAttribute("FilterLibGroup", statsSC.getAccessFilterLibGroup());
    request.setAttribute("FilterIdGroup", statsSC.getAccessFilterIdGroup());
    request.setAttribute("FilterLibUser", statsSC.getAccessFilterLibUser());
    request.setAttribute("FilterIdUser", statsSC.getAccessFilterIdUser());
    request.setAttribute("SpaceId", statsSC.getAccessSpaceId());
    request.setAttribute("Path", statsSC.getPath());
  }

  /**
   * Format a year and month parameter
   * @param sYear the year to format
   * @param sMonth the month to format
   * @return a request date string with the following format sYear-sMonth-01
   */
  private String getRequestDate(String sYear, String sMonth) {
    String month = Integer.toString(Integer.parseInt(sMonth) + 1);
    if (month.length() < 2) {
      month = "0" + month;
    }
    return sYear + "-" + month + "-" + "01";
  }

  /**
   * Set silver statistics session controller attributes from request
   * @param request the current http request
   * @param statsSC the statistics session controller.
   */
  private void saveAccessPDCParam(HttpServletRequest request,
      SilverStatisticsPeasSessionController statsSC) {
    String monthBegin = request.getParameter("MonthBegin");
    if (StringUtil.isDefined(monthBegin)) {
      statsSC.setMonthBegin(request.getParameter("MonthBegin"));
      statsSC.setYearBegin(request.getParameter("YearBegin"));
      statsSC.setMonthEnd(request.getParameter("MonthEnd"));
      statsSC.setYearEnd(request.getParameter("YearEnd"));
    }
  }
}
