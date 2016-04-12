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

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.web.silverstatistics.vo.AccessPublicationVO;
import org.silverpeas.web.silverstatistics.vo.AxisStatsFilter;
import org.silverpeas.web.silverstatistics.vo.CrossAxisAccessVO;
import org.silverpeas.web.silverstatistics.vo.CrossAxisStatsFilter;
import org.silverpeas.web.silverstatistics.vo.CrossStatisticVO;
import org.silverpeas.web.silverstatistics.vo.StatisticAxisVO;
import org.silverpeas.web.silverstatistics.vo.StatisticVO;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.chart.period.PeriodChart;
import org.silverpeas.core.chart.pie.PieChart;
import org.silverpeas.core.date.period.PeriodType;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.memory.MemoryUnit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import static org.silverpeas.core.util.ResourceLocator.getGeneralLocalizationBundle;

/**
 * Class declaration
 * @author
 */
public class SilverStatisticsPeasSessionController extends AbstractComponentSessionController {

  public static final int INDICE_VALUE = 0;
  public static final int INDICE_LIB = 1;
  private String monthBegin = null;
  private String yearBegin = null;
  private String monthEnd = null;
  private String yearEnd = null;
  private String actorDetail = null;
  private String frequenceDetail = null;
  private String filterType = null;
  private String filterLib = null;
  private String filterId = null;
  private String accessMonthBegin = null;
  private String accessYearBegin = null;
  private String accessFilterLibGroup = null;
  private String accessFilterIdGroup = null;
  private String accessFilterLibUser = null;
  private String accessFilterIdUser = null;
  private String accessSpaceId = null;
  /**
   * current stats list
   */
  private Vector<String[]> currentStats = new Vector<>();
  private Vector<String[]> path = new Vector<>();
  private Collection<String> yearsConnection = null;
  private Collection<String> yearsAccess = null;
  private Collection<String> yearsVolume = null;
  private PdcManager pdcManager = null;

  // init attributes
  private void initYears() {
    try {
      yearsConnection = initYears(SilverStatisticsPeasDAOConnexion.getYears());
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.initYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }

    try {
      yearsAccess = initYears(SilverStatisticsPeasDAOAccesVolume.getAccessYears());
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.initYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }

    try {
      yearsVolume = initYears(SilverStatisticsPeasDAOAccesVolume.getVolumeYears());
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.initYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }
  }

  /**
   * Initializes a new collection of years from the one given as parameter.
   * If the given one does not exist or is empty, then the returned collection is filled with the
   * year of the current date.
   * @param yearsFromStatistics
   * @return a never null collection of years as string.
   */
  private Collection<String> initYears(Collection<String> yearsFromStatistics) {
    Collection<String> result = new LinkedHashSet<String>();
    if (yearsFromStatistics != null) {
      result.addAll(yearsFromStatistics);
    }
    result.add(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
    return result;
  }

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SilverStatisticsPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.silverStatisticsPeas.multilang.silverStatisticsBundle",
        "org.silverpeas.silverStatisticsPeas.settings.silverStatisticsIcons");
    setComponentRootName(URLUtil.CMP_SILVERSTATISTICSPEAS);
    initYears();
  }

  public UserAccessLevel getUserProfile() {
    SilverTrace
        .info("silverStatisticsPeas", "SilverStatisticsPeasSessionController.getUserProfile()",
            "root.MSG_GEN_ENTER_METHOD");
    UserAccessLevel userProfile = getUserDetail().getAccessLevel();
    AdminController ac = ServiceProvider.getService(AdminController.class);
    String[] manageableSpaceRootIds = ac.getUserManageableSpaceRootIds(getUserId());
    if (!UserAccessLevel.ADMINISTRATOR.equals(userProfile) &&
        manageableSpaceRootIds != null && manageableSpaceRootIds.length > 0) {
      userProfile = UserAccessLevel.SPACE_ADMINISTRATOR;
    }
    SilverTrace
        .info("silverStatisticsPeas", "SilverStatisticsPeasSessionController.getUserProfile()",
            "root.MSG_GEN_PARAM_VALUE", "userProfile=" + userProfile);
    return userProfile;
  }

  public Collection<SessionInfo> getConnectedUsersList() {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    return sessionManagement.getConnectedUsersList();
  }

  public Collection<String[]> getStatsConnexionAllAll(String dateBegin, String dateEnd) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllAll(dateBegin, dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getStatsConnexionAllAll()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  /**
   * @return
   */
  private String formatDate(String date) {// date au format AAAA-MM-JJ -> Mois
    // AAAA
    String dateFormate = "";

    String mois = date.substring(5, 7);
    String annee = date.substring(0, 4);

    if ("01".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.January");
    } else if ("02".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.February");
    } else if ("03".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.March");
    } else if ("04".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.April");
    } else if ("05".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.May");
    } else if ("06".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.June");
    } else if ("07".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.July");
    } else if ("08".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.August");
    } else if ("09".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.September");
    } else if ("10".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.October");
    } else if ("11".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.November");
    } else if ("12".equals(mois)) {
      dateFormate += this.getString("silverStatisticsPeas.December");
    }

    return dateFormate + " " + annee;
  }

  /**
   * @return
   */
  public PeriodChart getDistinctUserConnectionsChart(String dateBegin, String dateEnd) {
    PeriodChart axisChart = null;
    try {
      // new Collection[]{dates, counts};
      Collection[] statsUsers = SilverStatisticsPeasDAOConnexion.getStatsUser(dateBegin, dateEnd);

      // title
      String title = this.getString("silverStatisticsPeas.ConnectionNumberOfDistinctUsers") + " ";
      String mois = dateBegin.substring(5, 7);
      if ("04".equals(mois) || "08".equals(mois) || "10".equals(mois)) {// Avril,
        // Aout,
        // Octobre
        title += this.getString("silverStatisticsPeas.FromAprilAugustOctober");

      } else {
        title += this.getString("silverStatisticsPeas.From") + " ";
      }

      title += formatDate(dateBegin) + " ";
      title += this.getString("silverStatisticsPeas.To") + " ";
      title += formatDate(dateEnd);

      axisChart = getMonthPeriodChartFrom((List<String>) statsUsers[0], (List) statsUsers[1])
          .withTitle(title);
      axisChart.getAxisY()
          .setTitle(getGeneralLocalizationBundle(getLanguage()).getString("GML.users"));
    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getDistinctUserConnectionsChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  /**
   * @return
   */
  public PeriodChart getUserConnectionsChart(String dateBegin, String dateEnd) {
    PeriodChart axisChart = null;
    try {
      Collection[] statsConnection =
          SilverStatisticsPeasDAOConnexion.getStatsConnexion(dateBegin, dateEnd);

      // title
      StringBuilder title = new StringBuilder();
      title.append(this.getString("silverStatisticsPeas.LoginNumber")).append(" ");
      String mois = dateBegin.substring(5, 7);
      if ("04".equals(mois) || "08".equals(mois) || "10".equals(mois)) {// Avril,
        // Aout, Octobre
        title.append(this.getString("silverStatisticsPeas.FromAprilAugustOctober"));
      } else {
        title.append(this.getString("silverStatisticsPeas.From")).append(" ");
      }

      title.append(formatDate(dateBegin)).append(" ");
      title.append(this.getString("silverStatisticsPeas.To")).append(" ");
      title.append(formatDate(dateEnd));

      axisChart =
          getMonthPeriodChartFrom((List<String>) statsConnection[0], (List) statsConnection[1])
              .withTitle(title.toString());
      axisChart.getAxisY().setTitle(this.getString("silverStatisticsPeas.Connections"));
    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public Collection<String[]> getStatsConnexionAllUser(String dateBegin, String dateEnd,
      String idUser) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion
          .getStatsConnexionAllUser(dateBegin, dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser", "root.EX_SQL_QUERY_FAILED",
          e);
    }
    return c;
  }

  /**
   * @return
   */
  public PeriodChart getUserConnectionsUserChart(String dateBegin, String dateEnd, String idUser) {
    PeriodChart axisChart = null;
    try {
      UserDetail userDetail = UserDetail.getById(idUser);
      String lastName = "";
      if (userDetail != null) {
        lastName = userDetail.getLastName();
      }
      Collection[] statsConnection =
          SilverStatisticsPeasDAOConnexion.getStatsUserConnexion(dateBegin, dateEnd, idUser);

      // title
      String title = this.getString("silverStatisticsPeas.LoginNumber") + " " +
          this.getString("silverStatisticsPeas.OfUser") + " " + lastName + " ";
      String mois = dateBegin.substring(5, 7);
      if ("04".equals(mois) || "08".equals(mois) || "10".equals(mois)) {// Avril,
        // Aout,
        // Octobre
        title += this.getString("silverStatisticsPeas.FromAprilAugustOctober");
      } else {
        title += this.getString("silverStatisticsPeas.From") + " ";
      }

      title += formatDate(dateBegin) + " ";
      title += this.getString("silverStatisticsPeas.To") + " ";
      title += formatDate(dateEnd);

      axisChart =
          getMonthPeriodChartFrom((List<String>) statsConnection[0], (List) statsConnection[1])
              .withTitle(title);
      axisChart.getAxisY().setTitle(this.getString("silverStatisticsPeas.Connections"));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsUserChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  /**
   * donne les stats pour un groupe seulement cad 2 info, la collection contient donc un seul
   * element
   * @param dateBegin a begin date string representation yyyy/MM/dd
   * @param dateEnd an end date string representation yyyy/MM/dd
   * @param idGroup a user group identifier
   * @return
   */
  public Collection<String[]> getStatsConnexionAllGroup(String dateBegin, String dateEnd,
      String idGroup) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion
          .getStatsConnexionAllGroup(dateBegin, dateEnd, Integer.parseInt(idGroup));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getStatsConnexionAllGroup()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  /**
   * @param dateBegin a begin date string representation yyyy/MM/dd
   * @param dateEnd an end date string representation yyyy/MM/dd
   * @param idGroup a user group identifier
   * @return an {@link PeriodChart} that represents the statistic data.
   */
  public PeriodChart getUserConnectionsGroupChart(String dateBegin, String dateEnd,
      String idGroup) {
    PeriodChart axisChart = null;
    try {
      Collection[] statsConnection =
          SilverStatisticsPeasDAOConnexion.getStatsGroupConnexion(dateBegin, dateEnd, idGroup);

      // title
      String title = this.getString("silverStatisticsPeas.LoginNumber") + " " +
          this.getString("silverStatisticsPeas.OfGroup") + " " +
          AdministrationServiceProvider.getAdminService().getGroupName(idGroup) + " ";
      String mois = dateBegin.substring(5, 7);
      if ("04".equals(mois) || "08".equals(mois) || "10".equals(mois)) {// Avril,
        // Aout,
        // Octobre
        title += this.getString("silverStatisticsPeas.FromAprilAugustOctober");
      } else {
        title += this.getString("silverStatisticsPeas.From") + " ";
      }

      title += formatDate(dateBegin) + " ";
      title += this.getString("silverStatisticsPeas.To") + " ";
      title += formatDate(dateEnd);

      axisChart =
          getMonthPeriodChartFrom((List<String>) statsConnection[0], (List) statsConnection[1])
              .withTitle(title.toString());
      axisChart.getAxisY().setTitle(this.getString("silverStatisticsPeas.Connections"));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsGroupChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  // donne pour chaque groupe ses stats cad 3 infos par groupe, la collection
  // contient auant d'elements que de groupes
  public Collection<String[]> getStatsConnexionGroupAll(String dateBegin, String dateEnd) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll(dateBegin, dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll", "root.EX_SQL_QUERY_FAILED",
          e);
    }
    return c;
  }

  // donne pour un chaque groupe d'un user les stats cad 3 infos par groupe, la
  // collection contient autant d'elements
  // que de groupes dont le user fait parti
  public Collection<String[]> getStatsConnexionGroupUser(String dateBegin, String dateEnd,
      String idUser) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion
          .getStatsConnexionAllGroup(dateBegin, dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupUser", "root.EX_SQL_QUERY_FAILED",
          e);
    }
    return c;
  }

  // donne pour chaque user ses stats, cad 3 infos, la collection contient
  // autant d'elements que de users
  public Collection<String[]> getStatsConnexionUserAll(String dateBegin, String dateEnd) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll(dateBegin, dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll", "root.EX_SQL_QUERY_FAILED",
          e);
    }
    return c;
  }

  // donne pour chaque user d'un groupe ses stats, cad 3 infos, la collection
  // contient autant d'elements que de users dans le groupe
  public Collection<String[]> getStatsConnexionUserUser(String dateBegin, String dateEnd,
      String idUser) {
    Collection<String[]> c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion
          .getStatsConnexionUserUser(dateBegin, dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserUser", "root.EX_SQL_QUERY_FAILED",
          e);
    }
    return c;
  }

  protected Selection communInitUserPanel(String compoName, String operation) {
    String m_context = URLUtil.getApplicationURL();
    String hostSpaceName = getString("silverStatisticsPeas.statistics");// getSpaceLabel();
    Pair<String, String> hostComponentName =
        new Pair<>(getComponentLabel(), m_context + getComponentUrl() + compoName);
    String hostUrl = m_context + getComponentUrl() + operation;

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    sel.setMultiSelect(false);
    sel.setPopupMode(true);

    return sel;
  }

  public void KickSession(String sessionId) {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    sessionManagement.closeSession(sessionId);
  }

  public UserDetail getTargetUserDetail(String userId) {
    return getUserDetail(userId);
  }

  public void notifySession(String userId, String message) {
    try {
      NotificationSender notificationSender = new NotificationSender(null);
      NotificationMetaData notifMetaData = new NotificationMetaData();

      notifMetaData.setTitle("");
      notifMetaData.setContent(message);
      notifMetaData.setSource(getUserDetail().getDisplayedName());
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipient(new UserRecipient(userId));

      notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
    } catch (Exception ex) {
      SilverTrace
          .error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.NotifySession",
              "root.EX_SQL_QUERY_FAILED", ex);
    }
  }

  /**
   * Méthode d'envoi de notification aux utilisateurs connectés
   * @param listUserDetail - liste des utilisateurs connectés
   * @param message
   */
  public void notifyAllSessions(Collection<SessionInfo> listUserDetail, String message) {
    List<String> notifiedUsers = new ArrayList<String>();

    if (listUserDetail != null) {
      for (SessionInfo sessionInfo : listUserDetail) {
        if (!notifiedUsers.contains(sessionInfo.getUserDetail().getId()) ||
            sessionInfo.getUserDetail().isAccessGuest()) {
          notifySession(sessionInfo.getUserDetail().getId(), message);
          notifiedUsers.add(sessionInfo.getUserDetail().getId());
        }
      }
    }
  }

  /*
   * Initialisation du UserPanel
   */
  public String initUserPanel() {
    communInitUserPanel("ViewConnections", "ReturnFromUserPanel");
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourUserPanel() {
    Selection sel = getSelection();
    String theUser = sel.getFirstSelectedElement();
    String theGroup = sel.getFirstSelectedSet();

    // update FilterType and FilterLib, FilterId
    if (theGroup != null && theGroup.length() != 0) {
      setFilterType("0");
      setFilterId(theGroup);
      setFilterLib(getOrganisationController().getGroup(theGroup).getName());
    } else if (theUser != null && theUser.length() != 0) {
      setFilterType("1");
      setFilterId(theUser);
      setFilterLib(getOrganisationController().getUserDetail(theUser).getLastName());
    }
  }

  /**
   * @return
   */
  /**
   * @return
   */
  public PeriodChart getUserConnectionsFqChart(String dateBegin, String dateEnd,
      String statDetail) {
    PeriodChart axisChart = null;
    try {
      int minFreq = 0;
      int maxFreq = 0;
      if (statDetail.equals("0")) {
        minFreq = 0;
        maxFreq = 5;
      } else if (statDetail.equals("1")) {
        minFreq = 5;
        maxFreq = 10;
      } else if (statDetail.equals("2")) {
        minFreq = 10;
        maxFreq = 15;
      } else if (statDetail.equals("3")) {
        minFreq = 15;
        maxFreq = 20;
      } else if (statDetail.equals("4")) {
        minFreq = 20;
        maxFreq = 25;
      } else if (statDetail.equals("5")) {
        minFreq = 25;
        maxFreq = 999;
      }

      Collection[] statsUsersFq =
          SilverStatisticsPeasDAOConnexion.getStatsUserFq(dateBegin, dateEnd, minFreq, maxFreq);

      // title
      String title = this.getString("silverStatisticsPeas.ConnectionNumberOfDistinctUsers") + " ";
      title += this.getString("silverStatisticsPeas.From") + " " + minFreq + " " +
          this.getString("silverStatisticsPeas.To") + " " + maxFreq + " " +
          this.getString("silverStatisticsPeas.Times") + " ";

      String mois = dateBegin.substring(5, 7);
      if ("04".equals(mois) || "08".equals(mois) || "10".equals(mois)) {// Avril,
        // Aout,
        // Octobre
        title += this.getString("silverStatisticsPeas.FromAprilAugustOctober");
      } else {
        title += this.getString("silverStatisticsPeas.From") + " ";
      }

      title += formatDate(dateBegin) + " ";
      title += this.getString("silverStatisticsPeas.To") + " ";
      title += formatDate(dateEnd);

      axisChart = getMonthPeriodChartFrom((List<String>) statsUsersFq[0], (List) statsUsersFq[1])
          .withTitle(title);
      axisChart.getAxisY().setTitle(getGeneralLocalizationBundle(getLanguage()).getString("GML.users"));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsFqChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public String initAccessUserPanelGroup() {
    Selection sel =
        communInitUserPanel("AccessCallUserPanelGroup", "AccessReturnFromUserPanelGroup");
    sel.setElementSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourAccessUserPanelGroup() {
    Selection sel = getSelection();
    String theGroup = sel.getFirstSelectedSet();

    // update FilterType and FilterLib, FilterId
    if (theGroup != null && theGroup.length() != 0) {
      setAccessFilterIdGroup(theGroup);
      setAccessFilterLibGroup(getOrganisationController().getGroup(theGroup).getName());
    }
  }

  public String initAccessUserPanelUser() {
    Selection sel = communInitUserPanel("AccessCallUserPanelUser", "AccessReturnFromUserPanelUser");
    sel.setSetSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourAccessUserPanelUser() {
    Selection sel = getSelection();
    String theUser = sel.getFirstSelectedElement();

    if (theUser != null && theUser.length() != 0) {
      setAccessFilterIdUser(theUser);
      setAccessFilterLibUser(getOrganisationController().getUserDetail(theUser).getLastName());
    }
  }

  public String initVolumeUserPanelGroup() {
    Selection sel =
        communInitUserPanel("VolumeCallUserPanelGroup", "VolumeReturnFromUserPanelGroup");
    sel.setElementSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourVolumeUserPanelGroup() {
    Selection sel = getSelection();
    String theGroup = sel.getFirstSelectedSet();

    // update FilterType and FilterLib, FilterId
    if (theGroup != null && theGroup.length() != 0) {
      setAccessFilterIdGroup(theGroup);
      setAccessFilterLibGroup(getOrganisationController().getGroup(theGroup).getName());
    }
  }

  public String initVolumeUserPanelUser() {
    Selection sel = communInitUserPanel("VolumeCallUserPanelUser", "VolumeReturnFromUserPanelUser");
    sel.setSetSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourVolumeUserPanelUser() {
    Selection sel = getSelection();
    String theUser = sel.getFirstSelectedElement();

    if (theUser != null && theUser.length() != 0) {
      setAccessFilterIdUser(theUser);
      setAccessFilterLibUser(getOrganisationController().getUserDetail(theUser).getLastName());
    }
  }

  public PieChart getUserVentilChart(String dateStat, String filterIdGroup,
      String filterIdUser, String spaceId) {
    UserPieChartBuilder userBuilder =
        new UserPieChartBuilder(dateStat, formatDate(dateStat), filterIdGroup, filterIdUser,
            spaceId, this.getMultilang());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, currentStats);
  }

  /**
   * @return
   */
  public PeriodChart getEvolutionUserChart(String entite, String entiteId,
      String filterLibGroup, String filterIdGroup, String filterLibUser,
      String filterIdUser) {
    PeriodChart axisChart = null;
    try {
      currentStats.clear();
      Collection<String[]> statsUserAccess = SilverStatisticsPeasDAOAccesVolume.
          getStatsUserEvolution(entite, entiteId, filterIdGroup, filterIdUser);
      Iterator<String[]> itStats = statsUserAccess.iterator();
      List<String> x = new ArrayList<String>();
      List<Long> y = new ArrayList<Long>();
      while (itStats.hasNext()) {
        String[] values = itStats.next();
        x.add(values[0]);
        y.add(Long.valueOf(values[1]));
        currentStats.add(new String[]{values[0], values[1]});
      }

      String title = getString("silverStatisticsPeas.EvolutionAccessDeb");
      if ("SPACE".equals(entite)) {
        SpaceInstLight space = getOrganisationController().getSpaceInstLightById(entiteId);
        if (!filterIdGroup.equals("") && filterIdUser.equals("")) {
          title += " " + this.getString("silverStatisticsPeas.EvolutionAccessGroup") + " " +
              filterLibGroup;
        }
        if (!filterIdUser.equals("")) {
          title += " " + this.getString("silverStatisticsPeas.EvolutionAccessUser") + " " +
              filterLibUser;
        }
        title += " " + this.getString("silverStatisticsPeas.EvolutionAccessSpace") + " [" +
            space.getName() + "]";
      } else {// CMP
        ComponentInstLight cmp = getOrganisationController().getComponentInstLight(entiteId);
        if (!filterIdGroup.equals("") && filterIdUser.equals("")) {
          title += " " + this.getString("silverStatisticsPeas.EvolutionAccessGroup") + " " +
              filterLibGroup;
        }
        if (!filterIdUser.equals("")) {
          title += " " + this.getString("silverStatisticsPeas.EvolutionAccessUser") + " " +
              filterLibUser;
        }
        title += " " + this.getString("silverStatisticsPeas.EvolutionAccessService") + " " +
            cmp.getLabel();
      }

      axisChart = getMonthPeriodChartFrom(x, y).withTitle(title);
      axisChart.getAxisY().setTitle(this.getString("silverStatisticsPeas.Access"));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getEvolutionUserChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  /**
   * @param spaceId
   */
  private void buildPath(String spaceId) {
    if (StringUtil.isDefined(spaceId) && (!spaceId.equals("WA0"))) {
      try {
        SpaceInstLight space =
            AdministrationServiceProvider.getAdminService().getSpaceInstLightById(spaceId);
        path.insertElementAt(new String[]{spaceId, space.getName()}, 0);
        buildPath("WA" + space.getFatherId());
      } catch (AdminException e) {
        SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.buildPath",
            "root.EX_SQL_QUERY_FAILED", e);
      }

    }
  }

  /**
   * @param spaceId
   */
  private void resetPath(String spaceId) {
    path.clear();
    buildPath(spaceId);
  }

  /**
   * @return
   */
  public PieChart getVolumeServicesChart() {
    PieChart pieChart = null;
    try {
      Collection[] statsKMsInstances = SilverStatisticsPeasDAOVolumeServices.
          getStatsInstancesServices();

      // build stats array collection
      currentStats.clear();
      String[] kms =
          (String[]) statsKMsInstances[0].toArray(new String[statsKMsInstances[0].size()]);
      String[] counts =
          (String[]) statsKMsInstances[1].toArray(new String[statsKMsInstances[1].size()]);
      for (int i = 0; i < kms.length; i++) {
        currentStats.add(new String[]{kms[i], counts[i]});
      }

      pieChart =
          getPieChartFrom((List<String>) statsKMsInstances[0], (List) statsKMsInstances[1])
              .withTitle(getString("silverStatisticsPeas.ServicesNumber"));
    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getVolumeServicesChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return pieChart;
  }

  /**
   * @return
   */
  public PieChart getPubliVentilChart(String dateStat, String filterIdGroup,
      String filterIdUser, String spaceId) {
    PubliPieChartBuilder publiBuilder =
        new PubliPieChartBuilder(dateStat, formatDate(dateStat), filterIdGroup, filterIdUser,
            spaceId, this.getMultilang());
    resetPath(spaceId);
    return publiBuilder.getChart(spaceId, currentStats);
  }

  /**
   * @return
   */
  public PieChart getDocsVentilChart(String spaceId) {
    DocPieChartBuilder userBuilder = new DocPieChartBuilder(spaceId, this.getMultilang());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, currentStats);
  }

  /**
   * @param spaceId
   * @return
   */
  public PieChart getDocsSizeVentilChart(String spaceId) {
    DocSizePieChartBuilder userBuilder = new DocSizePieChartBuilder(spaceId, this.getMultilang());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, currentStats);
  }

  /**
   * @return
   */
  public PeriodChart getEvolutionDocsSizeChart() {
    PeriodChart axisChart = null;
    try {
      currentStats.clear();
      Collection<String[]> statsDocsSize =
          SilverStatisticsPeasDAOVolumeServer.getStatsVolumeServer();
      Iterator<String[]> itStats = statsDocsSize.iterator();
      String[] values;
      List<String> dates = new ArrayList<String>(statsDocsSize.size());
      List<BigDecimal> size = new ArrayList<BigDecimal>(statsDocsSize.size());
      while (itStats.hasNext()) {
        values = itStats.next();
        dates.add(values[0]);
        size.add(UnitUtil.convertTo(new BigDecimal(values[2]), MemoryUnit.KB, MemoryUnit.MB)
            .setScale(2, BigDecimal.ROUND_DOWN)); // size en Mo

        currentStats.add(new String[]{values[0], values[1], values[2]});
      }

      axisChart = getMonthPeriodChartFrom(dates, size)
          .withTitle(getString("silverStatisticsPeas.EvolutionAttachmentsTotalSize"));
      axisChart.getAxisY()
          .setTitle(getGeneralLocalizationBundle(getLanguage()).getString("GML.size") +
              "(" + MemoryUnit.MB.getLabel() + ")");

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getEvolutionDocsSizeChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public String checkYearConnection(String yearValue) {
    return checkYear(yearValue, yearsConnection);
  }

  public String checkYearAccess(String yearValue) {
    return checkYear(yearValue, yearsAccess);
  }

  public String checkYearVolume(String yearValue) {
    return checkYear(yearValue, yearsVolume);
  }

  private String checkYear(String yearValue, Collection<String> years) {
    return years.contains(yearValue) ? yearValue : years.iterator().next();
  }

  public Collection<String[]> getFormYearConnection(String yearValue) {
    return getFormYear(yearValue, yearsConnection);
  }

  public Collection<String[]> getFormYearAccess(String yearValue) {
    return getFormYear(yearValue, yearsAccess);
  }

  public Collection<String[]> getFormYearVolume(String yearValue) {
    return getFormYear(yearValue, yearsVolume);
  }

  private Collection<String[]> getFormYear(String yearValue, Collection<String> years) {
    List<String[]> myList = new ArrayList<String[]>();
    String stat[] = null;

    if (years != null) {
      for (String indice : years) {
        stat = new String[2];
        stat[INDICE_VALUE] = indice;
        stat[INDICE_LIB] = stat[INDICE_VALUE];

        if (stat[INDICE_VALUE].equals(yearValue)) {
          stat[INDICE_VALUE] += " selected";
        }
        myList.add(stat);
      }
    }

    return myList;
  }

  public Collection<String[]> getFormMonth(String monthValue) {
    ArrayList<String[]> myList = new ArrayList<String[]>();
    String stat[] = null;

    for (int i = 0; i < 12; i++) {
      stat = new String[2];
      stat[INDICE_VALUE] = Integer.toString(i);
      stat[INDICE_LIB] = "GML.mois" + i;

      if (stat[INDICE_VALUE].equals(monthValue)) {
        stat[INDICE_VALUE] += " selected";
      }
      myList.add(stat);
    }

    return myList;
  }

  public Collection<String[]> getDetail(String value) {
    List<String[]> myList = new ArrayList<String[]>();
    String stat[] = null;

    stat = new String[2];
    stat[INDICE_VALUE] = "0";
    stat[INDICE_LIB] = "GML.allMP";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "1";
    stat[INDICE_LIB] = "silverStatisticsPeas.group";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "2";
    stat[INDICE_LIB] = "GML.user";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    return myList;
  }

  public Collection<String[]> getFrequenceDetail(String value) {
    List<String[]> myList = new ArrayList<String[]>();
    String stat[] = null;

    stat = new String[2];
    stat[INDICE_VALUE] = "0";
    stat[INDICE_LIB] = "[0 - 5[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "1";
    stat[INDICE_LIB] = "[5 - 10[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "2";
    stat[INDICE_LIB] = "[10 - 15[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "3";
    stat[INDICE_LIB] = "[15 - 20[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "4";
    stat[INDICE_LIB] = "[20 - 25[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    stat = new String[2];
    stat[INDICE_VALUE] = "5";
    stat[INDICE_LIB] = "[25 - ++[";

    if (stat[INDICE_VALUE].equals(value)) {
      stat[INDICE_VALUE] += " selected";
    }

    myList.add(stat);

    return myList;
  }

  /**
   *
   */
  public SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.silverStatisticsPeas.settings.silverStatisticsSettings");
  }

  // Accesseeurs set
  public void setMonthBegin(String s) {
    monthBegin = s;
  }

  public void setYearBegin(String s) {
    yearBegin = s;
  }

  public void setMonthEnd(String s) {
    monthEnd = s;
  }

  public void setYearEnd(String s) {
    yearEnd = s;
  }

  public void setActorDetail(String s) {
    actorDetail = s;
  }

  public void setFrequenceDetail(String s) {
    frequenceDetail = s;
  }

  public void setFilterType(String s) {
    filterType = s;
  }

  public void setFilterLib(String s) {
    filterLib = s;
  }

  public void setFilterId(String s) {
    filterId = s;
  }

  public void setAccessMonthBegin(String s) {
    accessMonthBegin = s;
  }

  public void setAccessYearBegin(String s) {
    accessYearBegin = s;
  }

  public void setAccessFilterLibGroup(String s) {
    accessFilterLibGroup = s;
  }

  public void setAccessFilterIdGroup(String s) {
    accessFilterIdGroup = s;
  }

  public void setAccessFilterLibUser(String s) {
    accessFilterLibUser = s;
  }

  public void setAccessFilterIdUser(String s) {
    accessFilterIdUser = s;
  }

  public void setAccessSpaceId(String s) {
    accessSpaceId = s;
    resetPath(accessSpaceId);
  }

  // Accesseeurs get
  public String getMonthBegin() {
    return monthBegin;
  }

  public String getYearBegin() {
    return yearBegin;
  }

  public String getMonthEnd() {
    return monthEnd;
  }

  public String getYearEnd() {
    return yearEnd;
  }

  public String getActorDetail() {
    return actorDetail;
  }

  public String getFrequenceDetail() {
    return frequenceDetail;
  }

  public String getFilterType() {
    return filterType;
  }

  public String getFilterLib() {
    return filterLib;
  }

  public String getFilterId() {
    return filterId;
  }

  public String getAccessMonthBegin() {
    return accessMonthBegin;
  }

  public String getAccessYearBegin() {
    return accessYearBegin;
  }

  public String getAccessFilterLibGroup() {
    return accessFilterLibGroup;
  }

  public String getAccessFilterIdGroup() {
    return accessFilterIdGroup;
  }

  public String getAccessFilterLibUser() {
    return accessFilterLibUser;
  }

  public String getAccessFilterIdUser() {
    return accessFilterIdUser;
  }

  public String getAccessSpaceId() {
    return accessSpaceId;
  }

  /**
   * @return Returns the currentStats.
   */
  public Vector<String[]> getCurrentStats() {
    return currentStats;
  }

  /**
   * @return Returns the currentStats.
   */
  public void clearCurrentStats() {
    this.currentStats.clear();
  }

  /**
   * @return Returns the path.
   */
  public Vector<String[]> getPath() {
    return path;
  }

  /**
   * Add PdC access in order to make PdC statistics
   */
  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = (PdcManager) new GlobalPdcManager();
    }
    return pdcManager;
  }

  /**
   * @return @throws PdcException
   */
  public List<StatisticAxisVO> getPrimaryAxis() throws PdcException {
    List<StatisticAxisVO> statsAxes = new ArrayList<StatisticAxisVO>();
    List<AxisHeader> axes = getPdcManager().getAxisByType("P");
    for (AxisHeader axisHeader : axes) {
      StatisticAxisVO axis =
          new StatisticAxisVO(axisHeader.getPK().getId(), axisHeader.getName(), axisHeader.
              getDescription(), false);
      statsAxes.add(axis);
    }
    return statsAxes;
  }

  /**
   * Retrieve statistics on axis
   * @param statsFilter an axis stats filter
   * @return a Statistic value object
   */
  public List<StatisticVO> getAxisStats(AxisStatsFilter statsFilter) {
    // Result list
    List<StatisticVO> stats = new ArrayList<StatisticVO>();

    // Retrieve all the list of components
    List<String> components = buildCustomComponentListWhereToSearch();

    // Global silver content declaration
    List<GlobalSilverContent> gSC = null;

    int curAxisId = statsFilter.getAxisId();

    try {
      // Build day query
      String firstDayStr = statsFilter.getYearBegin() + "/" + statsFilter.getMonthBegin() + "/01";
      String lastDayStr = statsFilter.getYearEnd() + "/" + statsFilter.getMonthEnd() + "/31";

      // Retrieve statistics on componentIds
      List<AccessPublicationVO> accessPublis =
          SilverStatisticsPeasDAO.getListPublicationAccess(firstDayStr, lastDayStr);

      if (curAxisId == 0) {
        // Retrieve publication axis
        List<AxisHeader> axis = getPdcManager().getAxisByType("P");

        // Retrieve publications on axis
        for (AxisHeader axisHeader : axis) {
          String axisId = axisHeader.getPK().getId();
          int nbAxisAccess = 0;
          // String axisVlue = axisHeader.get
          gSC = getPdCPublications(axisId, "/0/", components);
          nbAxisAccess = computeAxisAccessStatistics(accessPublis, gSC);
          StatisticVO curStat =
              new StatisticVO(axisId, axisHeader.getName(), axisHeader.getDescription(),
                  nbAxisAccess);
          stats.add(curStat);
        }
      } else {
        String axisValue = statsFilter.getAxisValue();
        boolean axisFilter = false;
        int curLevel = 1;
        if (StringUtil.isDefined(axisValue)) {
          axisFilter = true;
          // Retrieve current value level + 1
          curLevel = StringUtils.countMatches(axisValue, "/") - 1;
        }

        List<Value> values = getPdcManager().getAxisValues(statsFilter.getAxisId());
        for (Value curValue : values) {
          String curAxisValue = curValue.getFullPath();
          int nbAxisAccess = 0;
          // Check axis level number
          if (axisFilter && curAxisValue.startsWith(axisValue) &&
              curValue.getLevelNumber() == curLevel) {
            // Retrieve all the current axis publications
            gSC = getPdCPublications(Integer.toString(curAxisId), curAxisValue, components);
            nbAxisAccess = computeAxisAccessStatistics(accessPublis, gSC);
            // Create a new statistic value object
            StatisticVO curStat =
                new StatisticVO(Integer.toString(curAxisId), curValue.getName(), curValue.
                    getDescription(), nbAxisAccess);
            curStat.setAxisValue(curValue.getFullPath());
            curStat.setAxisLevel(curValue.getLevelNumber());
            // Add this statistic to list
            stats.add(curStat);
          } else if (!axisFilter && curValue.getLevelNumber() == 1) {
            // Retrieve all the current axis publications
            gSC = getPdCPublications(Integer.toString(curAxisId), curAxisValue, components);
            nbAxisAccess = computeAxisAccessStatistics(accessPublis, gSC);
            // Create a new statistic value object
            StatisticVO curStat = new StatisticVO(Integer.toString(curAxisId), curValue.getName(),
                curValue.getDescription(), nbAxisAccess);
            curStat.setAxisValue(curValue.getFullPath());
            curStat.setAxisLevel(curValue.getLevelNumber());
            // Add this statistic to list
            stats.add(curStat);
          }
        }
      }
    } catch (PdcException e) {
      SilverTrace.error("silverStatisticsPeas",
          SilverStatisticsPeasSessionController.class.getName() + ".getAxisStats",
          "Problem to access the PDC");
    } catch (SQLException sqlEx) {
      SilverTrace.error("silverStatisticsPeas",
          SilverStatisticsPeasSessionController.class.getName() + ".getAxisStats",
          "Problem to retrieve statistics on axis.");
    }
    return stats;
  }

  /**
   * SEARCH ONLY PDC publications on current selected axis
   * @param axisId
   * @param valueId
   * @param componentIds
   * @return
   */
  private List<GlobalSilverContent> getPdCPublications(String axisId, String valueId,
      List<String> componentIds) {
    // Create search context with a new search criteria
    SearchContext context = new SearchContext(this.getUserId());
    SearchCriteria criteria = new SearchCriteria(Integer.parseInt(axisId), valueId);
    context.addCriteria(criteria);

    // Retrieve the list of PDC publications using EJB call
    List<GlobalSilverContent> silverContentsMetier = null;
    PdcManager pdcManager = PdcManager.get();
    if (pdcManager != null) {
      if (!componentIds.isEmpty()) {
        try {
          silverContentsMetier =
              pdcManager.findGlobalSilverContents(context, componentIds, true, true);
        } catch (Exception e) {
          SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.getPdCPublications", "getPdCPublications exception", e);
        }
      }
    }
    return silverContentsMetier;
  }

  /**
   * SEARCH ONLY PDC publications on current selected axis
   * @param searchContext
   * @param componentIds
   * @return
   */
  private List<GlobalSilverContent> getPdCPublications(SearchContext searchContext,
      List<String> componentIds) {
    // Retrieve the list of PDC publications using EJB call
    List<GlobalSilverContent> silverContentsMetier = null;
    PdcManager pdcManager = PdcManager.get();
    if (pdcManager != null) {
      if (componentIds.size() > 0) {
        try {
          silverContentsMetier =
              pdcManager.findGlobalSilverContents(searchContext, componentIds, true, true);
        } catch (Exception e) {
          SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasSessionController.getPdCPublications", "getPdCPublications exception", e);
        }
      }
    }
    return silverContentsMetier;
  }

  /**
   * This method allow user to search over multiple component selection
   */
  public List<String> buildCustomComponentListWhereToSearch() {
    List<String> componentList = new ArrayList<String>();
    String[] allowedComponentIds = getUserAvailComponentIds();
    // Il n'y a pas de restriction sur un espace particulier
    for (String allowedComponentId : allowedComponentIds) {
      if (isSearchable(allowedComponentId)) {
        componentList.add(allowedComponentId);
      }
    }
    return componentList;
  }

  private boolean isSearchable(String componentId) {
    if (componentId.startsWith("silverCrawler") || componentId.startsWith("gallery") ||
        componentId.startsWith("kmelia")) {
      boolean isPrivateSearch = "yes".equalsIgnoreCase(getOrganisationController().
          getComponentParameterValue(componentId, "privateSearch"));
      return !isPrivateSearch;
    } else {
      return true;
    }
  }

  /**
   * Retrieve cross axis statistics
   * @param statsFilter
   * @return list of statistic value object
   */
  public CrossStatisticVO getCrossAxisStats(CrossAxisStatsFilter statsFilter) {
    // Cross PDC statistics
    CrossStatisticVO crossStat = null;
    List<List<CrossAxisAccessVO>> stats = new ArrayList<List<CrossAxisAccessVO>>();

    // Retrieve all the list of components
    List<String> components = buildCustomComponentListWhereToSearch();

    // Retrieve axis identifier
    int firstAxisId = statsFilter.getFirstAxisId();
    int secondAxisId = statsFilter.getSecondAxisId();
    try {
      // Build day query
      String firstDayStr = statsFilter.getYearBegin() + "/" + statsFilter.getMonthBegin() + "/01";
      String lastDayStr = statsFilter.getYearEnd() + "/" + statsFilter.getMonthEnd() + "/31";

      // Retrieve statistics on componentIds
      List<AccessPublicationVO> accessPublis =
          SilverStatisticsPeasDAO.getListPublicationAccess(firstDayStr, lastDayStr);

      // Retrieve the list of values from selected axes
      List<Value> firstValues = getPdcManager().getAxisValues(statsFilter.getFirstAxisId());
      List<Value> secondValues = getPdcManager().getAxisValues(statsFilter.getSecondAxisId());

      // Header and first row list declaration
      List<String> headerColumn = new ArrayList<String>();
      List<String> firstRow = new ArrayList<String>();

      // Initialize header column name
      for (Value value : secondValues) {
        headerColumn.add(value.getName());
      }

      for (Value firstValue : firstValues) {
        List<CrossAxisAccessVO> listRowStats = new ArrayList<CrossAxisAccessVO>();
        String fValue = firstValue.getFullPath();
        String fValueName = firstValue.getName();
        firstRow.add(fValueName);
        SearchCriteria firstSC = new SearchCriteria(firstAxisId, fValue);
        for (Value secondValue : secondValues) {
          // Loop variable declaration
          int nbAxisAccess = 0;
          String sValue = secondValue.getFullPath();
          // String sValueName = secondValue.getName();

          // Build PDC search context
          SearchContext searchCtx = new SearchContext(this.getUserId());
          searchCtx.addCriteria(firstSC);
          searchCtx.addCriteria(new SearchCriteria(secondAxisId, sValue));
          // Retrieve publication on current cross axis
          List<GlobalSilverContent> gSC = getPdCPublications(searchCtx, components);
          nbAxisAccess = computeAxisAccessStatistics(accessPublis, gSC);
          CrossAxisAccessVO crossAxisAccess =
              new CrossAxisAccessVO(firstAxisId, secondAxisId, fValue, sValue, nbAxisAccess);
          listRowStats.add(crossAxisAccess);

        }
        stats.add(listRowStats);
      }
      crossStat = new CrossStatisticVO(headerColumn, firstRow, stats);
    } catch (PdcException e) {
      SilverTrace.error("silverStatisticsPeas",
          SilverStatisticsPeasSessionController.class.getName() + ".getCrossAxisStats",
          "Problem to access the PDC", e);
    } catch (SQLException sqlEx) {
      SilverTrace.error("silverStatisticsPeas",
          SilverStatisticsPeasSessionController.class.getName() + ".getCrossAxisStats",
          "Problem to access statistics table", sqlEx);
    }
    return crossStat;
  }

  /**
   * Compute the number of axis access
   * @param accessPublis the list of publications that have been accessed on specific time period
   * @param gSC the list of publication which are classified on an axis
   * @return the global number of access on a specific axis
   */
  private int computeAxisAccessStatistics(List<AccessPublicationVO> accessPublis,
      List<GlobalSilverContent> gSC) {
    int nbAxisAccess = 0;
    for (GlobalSilverContent curGSC : gSC) {
      String publicationId = curGSC.getId();
      String instanceId = curGSC.getInstanceId();
      // Compute statistics on read publications
      for (AccessPublicationVO accessPub : accessPublis) {
        if (accessPub.getForeignPK().getId().equals(publicationId) &&
            accessPub.getForeignPK().getInstanceId().equals(instanceId)) {
          nbAxisAccess += accessPub.getNbAccess();
        }
      }
    }
    return nbAxisAccess;
  }

  @SuppressWarnings("unchecked")
  private PeriodChart getMonthPeriodChartFrom(List<String> dates, List values) {
    PeriodChart chart = PeriodChart.withoutTitle();
    Iterator<String> itDates = dates.iterator();
    Iterator<Object> itValues = values.iterator();
    while (itDates.hasNext()) {
      final Object value = itValues.next();
      final Number chartValue;
      if (value instanceof Number) {
        chartValue = (Number) value;
      } else {
        chartValue = Long.valueOf(String.valueOf(value));
      }
      chart.forX(java.sql.Date.valueOf(itDates.next()), PeriodType.month).add(chartValue);
    }
    return chart;
  }

  @SuppressWarnings("unchecked")
  private PieChart getPieChartFrom(List<String> labels, List values) {
    PieChart chart = PieChart.withoutTitle();
    Iterator<String> itLabels = labels.iterator();
    Iterator<Long> itValues = values.iterator();
    while (itLabels.hasNext()) {
      chart.add(itLabels.next(), Long.valueOf(String.valueOf(itValues.next())));
    }
    return chart;
  }
}
