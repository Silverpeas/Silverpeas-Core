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

package com.silverpeas.socialnetwork.newsFeed.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silverpeas.socialnetwork.myProfil.control.SocialNetworkService;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class NewsFeedJSONServlet extends HttpServlet {

  private static final long serialVersionUID = -7056446975706739300L;

  /**
   * servlet method for returning JSON format
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    String view = request.getParameter("View"); // Wall || Feed
    if (!StringUtil.isDefined(view)) {
      view = "Wall";
    }

    ResourceLocator multilang =
        new ResourceLocator("com.silverpeas.social.multilang.socialNetworkBundle",
        m_MainSessionCtrl.getFavoriteLanguage());

    ResourceLocator settings =
        new ResourceLocator("com.silverpeas.social.settings.socialNetworkSettings", "");
    int maxNbTries = settings.getInteger("newsFeed.maxNbTries", 10);
    int minNbDataBeforeNewTry = settings.getInteger("newsFeed.minNbDataBeforeNewTry", 15);

    if (StringUtil.getBooleanValue(request.getParameter("Init"))) {
      session.setAttribute("Silverpeas_NewsFeed_LastDate", new Date());
    }

    Map<Date, List<SocialInformation>> map = new LinkedHashMap<Date, List<SocialInformation>>();

    try {
      // recover the type
      SocialInformationType type = SocialInformationType.valueOf(request.getParameter("type"));

      String userId = m_MainSessionCtrl.getUserId();
      String anotherUserId = request.getParameter("userId");
      if (StringUtil.isDefined(anotherUserId) && !anotherUserId.equals(userId)) {
        view = "MyContactWall"; // forcing to display wall, ensuring to not display feed
        RelationShipService rss = new RelationShipService();
        if (!rss.isInRelationShip(Integer.parseInt(m_MainSessionCtrl.getUserId()), Integer
            .parseInt(anotherUserId))) {
          // Current user and target user are not in a relation
          return;
        }
      }

      com.silverpeas.calendar.Date[] period = getPeriod(session, settings);
      com.silverpeas.calendar.Date begin = period[0];
      com.silverpeas.calendar.Date end = period[1];

      map = getInformations(view, userId, type, anotherUserId, begin, end);

      int nbTries = 0;
      while (getNumberOfInformations(map) < minNbDataBeforeNewTry && nbTries < maxNbTries) {
        period = getPeriod(session, settings);

        map.putAll(getInformations(view, userId, type, anotherUserId, period[0], period[1]));
        nbTries++;
      }

    } catch (Exception ex) {
      Logger.getLogger(NewsFeedJSONServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    out.println(toJsonS(map, m_MainSessionCtrl.getOrganizationController(), multilang));
  }

  private com.silverpeas.calendar.Date[] getPeriod(HttpSession session, ResourceLocator settings) {
    int periodLength = settings.getInteger("newsFeed.period", 15);

    Date lastDate = (Date) session.getAttribute("Silverpeas_NewsFeed_LastDate");

    // process endDate
    com.silverpeas.calendar.Date begin = new com.silverpeas.calendar.Date(lastDate);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(lastDate);
    calendar.add(Calendar.DAY_OF_MONTH, 0 - periodLength);
    com.silverpeas.calendar.Date end = new com.silverpeas.calendar.Date(calendar.getTime());

    // prepare next startDate
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    session.setAttribute("Silverpeas_NewsFeed_LastDate", calendar.getTime());

    com.silverpeas.calendar.Date[] dates = new com.silverpeas.calendar.Date[2];
    dates[0] = begin;
    dates[1] = end;

    return dates;
  }

  private Map<Date, List<SocialInformation>> getInformations(String view, String userId,
      SocialInformationType type, String anotherUserId, Date begin, Date end) {
    Map<Date, List<SocialInformation>> map = new LinkedHashMap<Date, List<SocialInformation>>();

    SocialNetworkService socialNetworkService = new SocialNetworkService(userId);

    if ("MyFeed".equals(view)) {
      // get all data from me and my contacts
      map = socialNetworkService.getSocialInformationOfMyContacts(type, begin, end);
    } else if ("MyContactWall".equals(view)) {
      // get all data from my contact
      map = socialNetworkService.getSocialInformationOfMyContact(anotherUserId, type, begin, end);
    } else {
      // get all data from me
      map = socialNetworkService.getSocialInformation(type, begin, end);
    }
    return map;
  }

  private int getNumberOfInformations(Map<Date, List<SocialInformation>> map) {
    int nb = 0;
    for (Date date : map.keySet()) {
      nb += map.get(date).size();
    }
    return nb;
  }

  /**
   * convert the SocialInormation to JSONObject
   * @param information
   * @return JSONObject
   */
  private JSONObject toJson(SocialInformation information, OrganizationController oc,
      ResourceLocator multilang) {
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

    JSONObject valueObj = new JSONObject();
    UserDetail contactUser1 = oc.getUserDetail(information.getAuthor());
    if (information.getType().equals(SocialInformationType.RELATIONSHIP.toString())) {
      UserDetail contactUser2 = oc.getUserDetail(information.getTitle());
      valueObj.put("type", information.getType());
      valueObj.put("author", userDetailToJSON(contactUser1));
      valueObj.put("title", userDetailToJSON(contactUser2));
      valueObj.put("hour", formatTime.format(information.getDate()));
      valueObj.put("url", URLManager.getApplicationURL() + information.getUrl());
      valueObj.put("icon", URLManager.getApplicationURL() + contactUser2.getAvatar());
      valueObj.put("label", multilang.getStringWithParam("newsFeed.relationShip.label",
          contactUser2.getDisplayedName()));
      return valueObj;
    } else if (information.getType().endsWith(SocialInformationType.EVENT.toString())) {
      return eventSocialToJSON(information, contactUser1, multilang);
    }
    valueObj.put("type", information.getType());
    valueObj.put("author", userDetailToJSON(contactUser1));

    valueObj.put("description", EncodeHelper.javaStringToHtmlParagraphe(information
        .getDescription()));

    if (information.getType().equals(SocialInformationType.STATUS.toString())) {
      valueObj.put("title", multilang.getString("newsFeed.status.suffix"));
    } else {
      valueObj.put("title", information.getTitle());
    }
    // if time not identified display string empty
    if ("00:00".equalsIgnoreCase(formatTime.format(information.getDate()))) {
      valueObj.put("hour", "");
    } else {
      valueObj.put("hour", formatTime.format(information.getDate()));
    }
    valueObj.put("url", URLManager.getApplicationURL() + information.getUrl());
    valueObj.put("icon",
        getIconUrl(SocialInformationType.valueOf(information.getType())) + information.getIcon());
    valueObj.put("label", multilang.getString("newsFeed." + information.getType().toLowerCase() +
        ".updated." + information.isUpdeted()));

    return valueObj;
  }

  /**
   * convert the Map of socailInformation to JSONArray
   * @param Map<Date, List<SocialInformation>> map
   * @return JSONArray
   */
  private JSONArray toJsonS(Map<Date, List<SocialInformation>> map, OrganizationController oc,
      ResourceLocator multilang) {
    SimpleDateFormat formatDate =
        new SimpleDateFormat("EEEE dd MMMM yyyy", new Locale(multilang.getLanguage()));
    JSONArray result = new JSONArray();
    for (Map.Entry<Date, List<SocialInformation>> entry : map.entrySet()) {
      JSONArray jsonArrayDateWithValues = new JSONArray();
      Object key = entry.getKey();

      JSONArray jsonArray = new JSONArray();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("day", formatDate.format(key));
      List<SocialInformation> informations = entry.getValue();
      for (SocialInformation si : informations) {
        jsonArray.put(toJson(si, oc, multilang));
      }
      jsonArrayDateWithValues.put(jsonObject);
      jsonArrayDateWithValues.put(jsonArray);
      result.put(jsonArrayDateWithValues);
    }
    return result;
  }

  /**
   * convert socailInformationEvent to JSONObject
   * @param event
   * @return JSONObject
   */
  private JSONObject eventSocialToJSON(SocialInformation event, UserDetail contactUser,
      ResourceLocator multilang) {
    SimpleDateFormat formatTime =
        new SimpleDateFormat("HH:mm", new Locale(multilang.getLanguage()));
    JSONObject valueObj = new JSONObject();
    valueObj.put("type", event.getType());
    valueObj.put("author", userDetailToJSON(contactUser));
    valueObj.put("hour", formatTime.format(event.getDate()));
    valueObj.put("url", URLManager.getApplicationURL() + event.getUrl());
    valueObj.put("icon",
        getIconUrl(SocialInformationType.valueOf(event.getType())) + event.getIcon());
    if (!event.isUpdeted() && event.getIcon().startsWith(event.getType() + "_private")) {
      valueObj.put("title", multilang.getString("profil.icon.private.event"));
      valueObj.put("description", "");
    } else {
      valueObj.put("title", event.getTitle());
      valueObj.put("description", event.getDescription());
    }
    valueObj.put("label", multilang.getString("newsFeed." + event.getType().toLowerCase() +
        ".label"));

    return valueObj;
  }

  /**
   * convert the Map of SNContactUser to JSONArray
   * @param Map<Date, List<SocialInformation>> map
   * @return JSONArray
   */
  private JSONObject userDetailToJSON(UserDetail user) {
    JSONObject userJSON = new JSONObject();
    userJSON.put("id", user.getId());
    userJSON.put("displayedName", user.getDisplayedName());
    userJSON.put("profilPhoto", URLManager.getApplicationURL() + user.getAvatar());
    return userJSON;
  }

  /**
   * return the url of icon
   * @param SocialInformationType type
   * @return String
   */
  private String getIconUrl(SocialInformationType type) {
    String url = URLManager.getApplicationURL() + "/socialNetwork/jsp/icons/";
    if (type.equals(SocialInformationType.PHOTO)) {
      url = URLManager.getApplicationURL();
    }
    return url;
  }

}
