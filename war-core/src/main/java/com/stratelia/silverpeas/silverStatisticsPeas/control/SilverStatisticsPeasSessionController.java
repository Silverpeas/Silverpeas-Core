/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.nonAxisChart.PieChart2D;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * @author
 */
public class SilverStatisticsPeasSessionController extends AbstractComponentSessionController {
  public static final String SPACE_ADMIN = "SpaceAdmin";
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

  /** current stats list */
  private Vector currentStats = new Vector();

  private Vector path = new Vector();

  private Collection yearsConnection = null;
  private Collection yearsAccess = null;
  private Collection yearsVolume = null;

  ResourceLocator generalMessage = GeneralPropertiesManager
      .getGeneralMultilang(getLanguage());

  Admin admin = new Admin();

  // init attributes
  private void initYears() {

    try {
      yearsConnection = SilverStatisticsPeasDAOConnexion.getYears();
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }

    try {
      yearsAccess = SilverStatisticsPeasDAOAccesVolume
          .getYears(SilverStatisticsPeasDAOAccesVolume.TYPE_ACCES);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOAccesVolume.getYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }

    try {
      yearsVolume = SilverStatisticsPeasDAOAccesVolume
          .getYears(SilverStatisticsPeasDAOAccesVolume.TYPE_VOLUME);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOAccesVolume.getYears",
          "root.EX_SQL_QUERY_FAILED", e);
    }
  }

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SilverStatisticsPeasSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "com.stratelia.silverpeas.silverStatisticsPeas.multilang.silverStatisticsBundle",
        "com.stratelia.silverpeas.silverStatisticsPeas.settings.silverStatisticsIcons");
    setComponentRootName(URLManager.CMP_SILVERSTATISTICSPEAS);
    initYears();
  }

  public String getUserProfile() {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.getUserProfile()",
        "root.MSG_GEN_ENTER_METHOD");
    String userProfile = getUserDetail().getAccessLevel();
    AdminController ac = new AdminController(getUserId());
    if (!userProfile.equals("A")
        && ac.getUserManageableSpaceRootIds(getUserId()) != null) {
      userProfile = SPACE_ADMIN;
    }
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.getUserProfile()",
        "root.MSG_GEN_PARAM_VALUE", "userProfile=" + userProfile);
    return userProfile;
  }

  public Collection getConnectedUsersList() {
    return SessionManager.getInstance().getConnectedUsersList();
  }

  public Collection getStatsConnexionAllAll(String dateBegin, String dateEnd) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllAll(dateBegin,
          dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getStatsConnexionAllAll()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  /**
   * @param collection
   * @return
   */
  private double[] buildDoubleArrayFromStringCollection(Collection collection) {
    double[] result = new double[collection.size()];
    int i = 0;
    Iterator it = collection.iterator();
    while (it.hasNext()) {
      String value = (String) it.next();
      result[i++] = Double.parseDouble(value);
    }

    return result;
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

    return dateFormate += " " + annee;
  }

  /**
   * @return
   */
  public AxisChart getDistinctUserConnectionsChart(String dateBegin,
      String dateEnd) {
    AxisChart axisChart = null;
    try {

      Collection[] statsUsers = SilverStatisticsPeasDAOConnexion.getStatsUser(
          dateBegin, dateEnd); // new Collection[]{dates, counts};
      Collection listDate = statsUsers[0];
      Iterator itDates = listDate.iterator();
      String[] dates = new String[statsUsers[0].size()]; // au format MM-AAAA
      String date;
      String annee;
      String mois;
      int i = 0;
      while (itDates.hasNext()) {
        date = (String) itDates.next();
        annee = date.substring(0, 4);
        mois = date.substring(5, 7);
        dates[i] = mois + "-" + annee;
        i++;
      }

      // title
      String title = this
          .getString("silverStatisticsPeas.ConnectionNumberOfDistinctUsers")
          + " ";
      mois = dateBegin.substring(5, 7);
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

      axisChart = ChartUtil.buildBarAxisChart(generalMessage
          .getString("GML.date"), generalMessage.getString("GML.users"), title,
          dates, buildDoubleArrayFromStringCollection(statsUsers[1]));

    } catch (Exception se) {
      SilverTrace
          .error(
          "silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getDistinctUserConnectionsChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  /**
   * @return
   */
  public AxisChart getUserConnectionsChart(String dateBegin, String dateEnd) {
    AxisChart axisChart = null;
    try {
      Collection[] statsConnection = SilverStatisticsPeasDAOConnexion
          .getStatsConnexion(dateBegin, dateEnd);
      Collection listDate = statsConnection[0];
      Iterator itDates = listDate.iterator();
      String[] dates = new String[statsConnection[0].size()]; // au format
      // MM-AAAA
      String date;
      String annee;
      String mois;
      int i = 0;
      while (itDates.hasNext()) {
        date = (String) itDates.next();
        annee = date.substring(0, 4);
        mois = date.substring(5, 7);
        dates[i] = mois + "-" + annee;
        i++;
      }

      // title
      String title = this.getString("silverStatisticsPeas.LoginNumber") + " ";
      mois = dateBegin.substring(5, 7);
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

      axisChart = ChartUtil.buildBarAxisChart(generalMessage
          .getString("GML.date"), this
          .getString("silverStatisticsPeas.Connections"), title, dates,
          buildDoubleArrayFromStringCollection(statsConnection[1]));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public Collection getStatsConnexionAllUser(String dateBegin, String dateEnd,
      String idUser) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser(dateBegin,
          dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  /**
   * @return
   */
  public AxisChart getUserConnectionsUserChart(String dateBegin,
      String dateEnd, String idUser) {
    AxisChart axisChart = null;
    try {
      UserDetail userDetail = admin.getUserDetail(idUser);
      String lastName = "";
      if (userDetail != null)
        lastName = userDetail.getLastName();
      Collection[] statsConnection = SilverStatisticsPeasDAOConnexion
          .getStatsUserConnexion(dateBegin, dateEnd, idUser);
      Collection listDate = statsConnection[0];
      Iterator itDates = listDate.iterator();
      String[] dates = new String[statsConnection[0].size()]; // au format
      // MM-AAAA
      String date;
      String annee;
      String mois;
      int i = 0;
      while (itDates.hasNext()) {
        date = (String) itDates.next();
        annee = date.substring(0, 4);
        mois = date.substring(5, 7);
        dates[i] = mois + "-" + annee;
        i++;
      }

      // title
      String title = this.getString("silverStatisticsPeas.LoginNumber") + " "
          + this.getString("silverStatisticsPeas.OfUser") + " " + lastName
          + " ";
      mois = dateBegin.substring(5, 7);
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

      axisChart = ChartUtil.buildBarAxisChart(generalMessage
          .getString("GML.date"), this
          .getString("silverStatisticsPeas.Connections"), title, dates,
          buildDoubleArrayFromStringCollection(statsConnection[1]));

    } catch (Exception se) {
      SilverTrace
          .error(
          "silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsUserChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  // donne les stats pour un groupe seulement cad 2 info, la collection contient
  // donc un seul element
  public Collection getStatsConnexionAllGroup(String dateBegin, String dateEnd,
      String idGroup) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup(dateBegin,
          dateEnd, Integer.parseInt(idGroup));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getStatsConnexionAllGroup()",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  /**
   * @return
   */
  public AxisChart getUserConnectionsGroupChart(String dateBegin,
      String dateEnd, String idGroup) {
    AxisChart axisChart = null;
    try {
      Collection[] statsConnection = SilverStatisticsPeasDAOConnexion
          .getStatsGroupConnexion(dateBegin, dateEnd, idGroup);
      Collection listDate = statsConnection[0];
      Iterator itDates = listDate.iterator();
      String[] dates = new String[statsConnection[0].size()]; // au format
      // MM-AAAA
      String date;
      String annee;
      String mois;
      int i = 0;
      while (itDates.hasNext()) {
        date = (String) itDates.next();
        annee = date.substring(0, 4);
        mois = date.substring(5, 7);
        dates[i] = mois + "-" + annee;
        i++;
      }

      // title
      String title = this.getString("silverStatisticsPeas.LoginNumber") + " "
          + this.getString("silverStatisticsPeas.OfGroup") + " "
          + admin.getGroupName(idGroup) + " ";
      mois = dateBegin.substring(5, 7);
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

      axisChart = ChartUtil.buildBarAxisChart(generalMessage
          .getString("GML.date"), this
          .getString("silverStatisticsPeas.Connections"), title, dates,
          buildDoubleArrayFromStringCollection(statsConnection[1]));

    } catch (Exception se) {
      SilverTrace
          .error(
          "silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsGroupChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  // donne pour chaque groupe ses stats cad 3 infos par groupe, la collection
  // contient auant d'elements que de groupes
  public Collection getStatsConnexionGroupAll(String dateBegin, String dateEnd) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll(dateBegin,
          dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  // donne pour un chaque groupe d'un user les stats cad 3 infos par groupe, la
  // collection contient autant d'elements
  // que de groupes dont le user fait parti
  public Collection getStatsConnexionGroupUser(String dateBegin,
      String dateEnd, String idUser) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupUser(
          dateBegin, dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupUser",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  // donne pour chaque user ses stats, cad 3 infos, la collection contient
  // autant d'elements que de users
  public Collection getStatsConnexionUserAll(String dateBegin, String dateEnd) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll(dateBegin,
          dateEnd);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  // donne pour chaque user d'un groupe ses stats, cad 3 infos, la collection
  // contient autant d'elements que de users dans le groupe
  public Collection getStatsConnexionUserUser(String dateBegin, String dateEnd,
      String idUser) {
    Collection c = null;
    try {
      c = SilverStatisticsPeasDAOConnexion.getStatsConnexionUserUser(dateBegin,
          dateEnd, Integer.parseInt(idUser));
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserUser",
          "root.EX_SQL_QUERY_FAILED", e);
    }
    return c;
  }

  protected Selection communInitUserPanel(String compoName, String operation) {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = getString("silverStatisticsPeas.statistics");// getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(),
        m_context + getComponentUrl() + compoName);
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
    SessionManager.getInstance().removeSession(sessionId);
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
      notifMetaData.addUserRecipient(userId);

      notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
          notifMetaData);
    } catch (Exception ex) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.NotifySession",
          "root.EX_SQL_QUERY_FAILED", ex);
    }
  }

  /**
   * Méthode d'envoi de notification aux utilisateurs connectés
   * @param listUserDetail - liste des utilisateurs conenctés
   * @param message
   */
  public void notifyAllSessions(Collection listUserDetail, String message) {
    List notifiedUsers = new ArrayList();

    if (listUserDetail != null) {
      Iterator itListUserDetail = listUserDetail.iterator();
      while (itListUserDetail.hasNext()) {
        SessionInfo sessionInfo = (SessionInfo) itListUserDetail.next();
        if (!notifiedUsers.contains(sessionInfo.m_User.getId())
            || sessionInfo.m_User.isAccessGuest()) {
          notifySession(sessionInfo.m_User.getId(), message);
          notifiedUsers.add(sessionInfo.m_User.getId());
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
      setFilterLib(getOrganizationController().getGroup(theGroup).getName());
    } else if (theUser != null && theUser.length() != 0) {
      setFilterType("1");
      setFilterId(theUser);
      setFilterLib(getOrganizationController().getUserDetail(theUser)
          .getLastName());
    }
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourUserPanel()",
        "filterType=" + filterType);
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourUserPanel()", "filterId="
        + filterId);
  }

  /**
   * @return
   */
  /**
   * @return
   */
  public AxisChart getUserConnectionsFqChart(String dateBegin, String dateEnd,
      String statDetail) {
    AxisChart axisChart = null;
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

      Collection[] statsUsersFq = SilverStatisticsPeasDAOConnexion
          .getStatsUserFq(dateBegin, dateEnd, minFreq, maxFreq);
      Collection listDate = statsUsersFq[0];
      Iterator itDates = listDate.iterator();
      String[] dates = new String[statsUsersFq[0].size()]; // au format MM-AAAA
      String date;
      String annee;
      String mois;
      int i = 0;
      while (itDates.hasNext()) {
        date = (String) itDates.next();
        annee = date.substring(0, 4);
        mois = date.substring(5, 7);
        dates[i] = mois + "-" + annee;
        i++;
      }

      // title
      String title = this
          .getString("silverStatisticsPeas.ConnectionNumberOfDistinctUsers")
          + " ";
      title += this.getString("silverStatisticsPeas.From") + " " + minFreq
          + " " + this.getString("silverStatisticsPeas.To") + " " + maxFreq
          + " " + this.getString("silverStatisticsPeas.Times") + " ";

      mois = dateBegin.substring(5, 7);
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

      axisChart = ChartUtil.buildBarAxisChart(generalMessage
          .getString("GML.date"), generalMessage.getString("GML.users"), title,
          dates, buildDoubleArrayFromStringCollection(statsUsersFq[1]));

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getUserConnectionsFqChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public String initAccessUserPanelGroup() {
    Selection sel = communInitUserPanel("AccessCallUserPanelGroup",
        "AccessReturnFromUserPanelGroup");
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
      setAccessFilterLibGroup(getOrganizationController().getGroup(theGroup)
          .getName());
    }
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourAccessUserPanelGroup()",
        "accessFilterIdGroup=" + accessFilterIdGroup);
  }

  public String initAccessUserPanelUser() {
    Selection sel = communInitUserPanel("AccessCallUserPanelUser",
        "AccessReturnFromUserPanelUser");
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
      setAccessFilterLibUser(getOrganizationController().getUserDetail(theUser)
          .getLastName());
    }
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourAccessUserPanelUser()",
        "accessFilterIdUser=" + accessFilterIdUser);
  }

  public String initVolumeUserPanelGroup() {
    Selection sel = communInitUserPanel("VolumeCallUserPanelGroup",
        "VolumeReturnFromUserPanelGroup");
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
      setAccessFilterLibGroup(getOrganizationController().getGroup(theGroup)
          .getName());
    }
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourAccessUserPanelGroup()",
        "accessFilterIdGroup=" + accessFilterIdGroup);
  }

  public String initVolumeUserPanelUser() {
    Selection sel = communInitUserPanel("VolumeCallUserPanelUser",
        "VolumeReturnFromUserPanelUser");
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
      setAccessFilterLibUser(getOrganizationController().getUserDetail(theUser)
          .getLastName());
    }
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.retourAccessUserPanelUser()",
        "accessFilterIdUser=" + accessFilterIdUser);
  }

  /**
   * @return
   */
  public PieChart2D getUserVentilChart(String dateStat, String filterIdGroup,
      String filterIdUser, String spaceId) {
    UserPieChartBuilder userBuilder = new UserPieChartBuilder(dateStat,
        formatDate(dateStat), getUserId(), filterIdGroup, filterIdUser,
        spaceId, this.getMultilang(), getOrganizationController());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, getUserId(), currentStats);
  }

  /**
   * @return
   */
  public AxisChart getEvolutionUserChart(String entite, String entiteId,
      String filterLibGroup, String filterIdGroup, String filterLibUser,
      String filterIdUser) {
    AxisChart axisChart = null;
    try {
      currentStats.clear();
      Collection statsUserAccess = SilverStatisticsPeasDAOAccesVolume
          .getStatsUserEvolution(entite, entiteId, filterIdGroup, filterIdUser);
      Iterator itStats = statsUserAccess.iterator();
      String[] values;
      String[] dates = new String[statsUserAccess.size()]; // au format MM-AAAA
      String annee;
      String mois;
      double[] nbAccess = new double[statsUserAccess.size()];
      int i = 0;
      while (itStats.hasNext()) {
        values = (String[]) itStats.next();
        annee = values[0].substring(0, 4);
        mois = values[0].substring(5, 7);
        dates[i] = mois + "-" + annee;
        nbAccess[i] = Double.parseDouble(values[1]);
        currentStats.add(new String[] { values[0], values[1] });

        i++;
      }

      String title = this.getString("silverStatisticsPeas.EvolutionAccessDeb");
      if ("SPACE".equals(entite)) {
        SpaceInstLight space = admin.getSpaceInstLightById(entiteId);
        if (!filterIdGroup.equals("") && filterIdUser.equals("")) {
          title += " "
              + this.getString("silverStatisticsPeas.EvolutionAccessGroup")
              + " " + filterLibGroup;
        }
        if (!filterIdUser.equals("")) {
          title += " "
              + this.getString("silverStatisticsPeas.EvolutionAccessUser")
              + " " + filterLibUser;
        }
        title += " "
            + this.getString("silverStatisticsPeas.EvolutionAccessSpace")
            + " [" + space.getName() + "]";
      } else {// CMP
        ComponentInstLight cmp = admin.getComponentInstLight(entiteId);
        if (!filterIdGroup.equals("") && filterIdUser.equals("")) {
          title += " "
              + this.getString("silverStatisticsPeas.EvolutionAccessGroup")
              + " " + filterLibGroup;
        }
        if (!filterIdUser.equals("")) {
          title += " "
              + this.getString("silverStatisticsPeas.EvolutionAccessUser")
              + " " + filterLibUser;
        }
        title += " "
            + this.getString("silverStatisticsPeas.EvolutionAccessService")
            + " " + cmp.getLabel();
      }

      axisChart = ChartUtil
          .buildLineAxisChart(generalMessage.getString("GML.date"), this
          .getString("silverStatisticsPeas.Access"), title, dates, nbAccess);

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
    if ((spaceId != null) && (spaceId.length() > 0) && (!spaceId.equals("WA0"))) {
      try {
        SpaceInstLight space = admin.getSpaceInstLightById(spaceId);
        path.insertElementAt(new String[] { spaceId, space.getName() }, 0);
        buildPath("WA" + space.getFatherId());
      } catch (AdminException e) {
        SilverTrace.error("silverStatisticsPeas",
            "SilverStatisticsPeasSessionController.buildPath",
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
  public PieChart2D getVolumeServicesChart() {
    PieChart2D pieChart = null;
    try {
      Collection[] statsKMsInstances = SilverStatisticsPeasDAOVolumeServices
          .getStatsInstancesServices();

      // build stats array collection
      currentStats.clear();
      String[] kms = (String[]) statsKMsInstances[0].toArray(new String[0]);
      String[] counts = (String[]) statsKMsInstances[1].toArray(new String[0]);
      for (int i = 0; i < kms.length; i++) {
        currentStats.add(new String[] { kms[i], counts[i] });
      }

      pieChart = ChartUtil.buildPieChart(this
          .getString("silverStatisticsPeas.ServicesNumber"),
          buildDoubleArrayFromStringCollection(statsKMsInstances[1]),
          (String[]) statsKMsInstances[0].toArray(new String[0]));
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
  public PieChart2D getPubliVentilChart(String dateStat, String filterIdGroup,
      String filterIdUser, String spaceId) {
    PubliPieChartBuilder publiBuilder = new PubliPieChartBuilder(dateStat,
        formatDate(dateStat), getUserId(), filterIdGroup, filterIdUser,
        spaceId, getOrganizationController(), this.getMultilang());
    resetPath(spaceId);
    return publiBuilder.getChart(spaceId, getUserId(), currentStats);
  }

  /**
   * @return
   */
  public PieChart2D getDocsVentilChart(String spaceId) {
    DocPieChartBuilder userBuilder = new DocPieChartBuilder(getUserId(),
        spaceId, this.getMultilang());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, getUserId(), currentStats);
  }

  /**
   * @return
   */
  public PieChart2D getDocsSizeVentilChart(String spaceId) {
    DocSizePieChartBuilder userBuilder = new DocSizePieChartBuilder(
        getUserId(), spaceId, this.getMultilang());
    resetPath(spaceId);
    return userBuilder.getChart(spaceId, getUserId(), currentStats);
  }

  /**
   * @return
   */
  public AxisChart getEvolutionDocsSizeChart() {
    AxisChart axisChart = null;
    try {
      currentStats.clear();
      Collection statsDocsSize = SilverStatisticsPeasDAOVolumeServer
          .getStatsVolumeServer();
      Iterator itStats = statsDocsSize.iterator();
      String[] values;
      String annee;
      String mois;
      String[] dates = new String[statsDocsSize.size()];
      double[] size = new double[statsDocsSize.size()];
      int i = 0;
      while (itStats.hasNext()) {
        values = (String[]) itStats.next();
        annee = values[0].substring(0, 4);
        mois = values[0].substring(5, 7);
        dates[i] = mois + "-" + annee;
        size[i] = Double.parseDouble(values[2]) / 1024; // size en Mo

        currentStats.add(new String[] { values[0], values[1], values[2] });
        i++;
      }

      axisChart = ChartUtil.buildLineAxisChart(generalMessage
          .getString("GML.date"), generalMessage.getString("GML.size")
          + " (Mo)", this
          .getString("silverStatisticsPeas.EvolutionAttachmentsTotalSize"),
          dates, size);

    } catch (Exception se) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasSessionController.getEvolutionDocsSizeChart()",
          "root.EX_SQL_QUERY_FAILED", se);
    }

    return axisChart;
  }

  public Collection getYearConnection(String yearValue) {
    return getYear(yearValue, yearsConnection);
  }

  public Collection getYearAccess(String yearValue) {
    return getYear(yearValue, yearsAccess);
  }

  public Collection getYearVolume(String yearValue) {
    return getYear(yearValue, yearsVolume);
  }

  private Collection getYear(String yearValue, Collection years) {
    ArrayList myList = new ArrayList();
    String stat[] = null;

    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.getYear()", "yearValue="
        + yearValue);

    if (years != null) {
      Iterator it = years.iterator();
      int i = 0;
      while (it.hasNext()) {
        stat = new String[2];
        stat[INDICE_VALUE] = (String) it.next();
        stat[INDICE_LIB] = stat[INDICE_VALUE];

        if (stat[INDICE_VALUE].equals(yearValue)) {
          stat[INDICE_VALUE] += " selected";
        }
        myList.add(stat);
        i++;
      }
    }

    return myList;
  }

  public Collection getMonth(String monthValue) {
    ArrayList myList = new ArrayList();
    String stat[] = null;

    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasSessionController.getMonth()", "monthValue="
        + monthValue);
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

  public Collection getDetail(String value) {
    ArrayList myList = new ArrayList();
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

  public Collection getFrequenceDetail(String value) {
    ArrayList myList = new ArrayList();
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
  public Vector getCurrentStats() {
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
  public Vector getPath() {
    return path;
  }
}
