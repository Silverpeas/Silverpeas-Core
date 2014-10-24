/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.socialnetwork.newsFeed.servlets;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.socialnetwork.myProfil.control.SocialNetworkService;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.util.EncodeHelper;
import org.silverpeas.util.JSONCodec;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsFeedJSONServlet extends HttpServlet {

  private static final long serialVersionUID = -7056446975706739300L;

  @Inject
  private OrganizationController organizationController;

  /**
   * servlet method for returning JSON format
   * @param request the http request
   * @param response the http response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    // view type is Wall or MyFeed or MyContactWall
    String view = request.getParameter("View");
    if (!StringUtil.isDefined(view)) {
      view = "Wall";
    }

    ResourceLocator multilang =
        new ResourceLocator("com.silverpeas.social.multilang.socialNetworkBundle",
            mainSessionCtrl.getFavoriteLanguage());

    ResourceLocator settings =
        new ResourceLocator("com.silverpeas.social.settings.socialNetworkSettings", "");
    int maxNbTries = settings.getInteger("newsFeed.maxNbTries", 10);
    int minNbDataBeforeNewTry = settings.getInteger("newsFeed.minNbDataBeforeNewTry", 15);

    if (StringUtil.getBooleanValue(request.getParameter("Init"))) {
      session.setAttribute("Silverpeas_NewsFeed_LastDate", new Date());
    }

    Map<Date, List<SocialInformation>> map = new LinkedHashMap<>();

    try {
      // recover the type
      SocialInformationType type = SocialInformationType.valueOf(request.getParameter("type"));

      String userId = mainSessionCtrl.getUserId();
      String anotherUserId = request.getParameter("userId");
      if (StringUtil.isDefined(anotherUserId) && !anotherUserId.equals(userId)) {
        view = "MyContactWall"; // forcing to display my contact wall, ensuring to not display feed
        RelationShipService rss = RelationShipService.get();
        if (!rss.isInRelationShip(Integer.parseInt(mainSessionCtrl.getUserId()),
            Integer.parseInt(anotherUserId))) {
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
    out.println(toJsonS(map, multilang));
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
    Map<Date, List<SocialInformation>> map = new LinkedHashMap<>();

    SocialNetworkService socialNetworkService = new SocialNetworkService(userId);

    if ("MyFeed".equals(view)) {
      // get all data from me and my contacts
      map = socialNetworkService.getSocialInformationOfMyContacts(type, begin, end);
    } else if ("MyContactWall".equals(view)) {
      // get all data from my contact
      if (StringUtil.isDefined(anotherUserId)) {
        map = socialNetworkService.getSocialInformationOfMyContact(anotherUserId, type, begin, end);
      }
    } else { // Wall
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
   * convert the SocialInformation to JSONObject
   * @param information the social information to convert
   * @return JSONObject
   */
  private Function<JSONCodec.JSONArray, JSONCodec.JSONArray> getJSONSocialInfo(
      SocialInformation information, ResourceLocator multilang) {
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

    return jsonSocialInfoA -> {
      jsonSocialInfoA.addJSONObject(jsonSocialInfo -> {
        UserDetail contactUser1 = UserDetail.getById(information.getAuthor());
        if (information.getType().equals(SocialInformationType.RELATIONSHIP.toString())) {
          UserDetail contactUser2 = UserDetail.getById(information.getTitle());
          jsonSocialInfo.put("type", information.getType());
          jsonSocialInfo.put("author", userDetailToJSON(contactUser1));
          jsonSocialInfo.put("title", userDetailToJSON(contactUser2));
          jsonSocialInfo.put("hour", formatTime.format(information.getDate()));
          jsonSocialInfo.put("url", URLManager.getApplicationURL() + information.getUrl());
          jsonSocialInfo.put("label", multilang
              .getStringWithParam("newsFeed.relationShip.label", contactUser2.getDisplayedName()));
        } else if (information.getType().endsWith(SocialInformationType.EVENT.toString())) {
          jsonSocialInfo.put("type", information.getType());
          jsonSocialInfo.put("author", userDetailToJSON(contactUser1));
          jsonSocialInfo.put("hour", formatTime.format(information.getDate()));
          jsonSocialInfo.put("url", URLManager.getApplicationURL() + information.getUrl());
          if (!information.isUpdated() &&
              information.getIcon().startsWith(information.getType() + "_private")) {
            jsonSocialInfo.put("title", multilang.getString("profil.icon.private.event"));
            jsonSocialInfo.put("description", "");
          } else {
            jsonSocialInfo.put("title", information.getTitle());
            jsonSocialInfo.put("description", information.getDescription());
          }
          jsonSocialInfo
              .put("label", multilang.getString("newsFeed." + information.getType().toLowerCase() +
                  ".label"));
          return jsonSocialInfo;
        }
        jsonSocialInfo.put("type", information.getType());
        jsonSocialInfo.put("author", userDetailToJSON(contactUser1));

        jsonSocialInfo.put("description",
            EncodeHelper.javaStringToHtmlParagraphe(information.getDescription()));

        if (information.getType().equals(SocialInformationType.STATUS.toString())) {
          jsonSocialInfo.put("title", multilang.getString("newsFeed.status.suffix"));
        } else {
          jsonSocialInfo.put("title", information.getTitle());
        }
        // if time not identified display string empty
        if ("00:00".equalsIgnoreCase(formatTime.format(information.getDate()))) {
          jsonSocialInfo.put("hour", "");
        } else {
          jsonSocialInfo.put("hour", formatTime.format(information.getDate()));
        }
        jsonSocialInfo.put("url", URLManager.getApplicationURL() + information.getUrl());
        jsonSocialInfo
            .put("label", multilang.getString("newsFeed." + information.getType().toLowerCase() +
                ".updated." + information.isUpdated()));
        return jsonSocialInfo;
      });
      return jsonSocialInfoA;
    };
  }

  /**
   * convert the Map of socialInformation to JSON String representation
   * @param map
   * @return JSONArray
   */
  private String toJsonS(Map<Date, List<SocialInformation>> map, ResourceLocator multilang) {
    SimpleDateFormat formatDate =
        new SimpleDateFormat("EEEE dd MMMM yyyy", new Locale(multilang.getLanguage()));
    return JSONCodec.encodeArray(jsonElt -> {
      for (Map.Entry<Date, List<SocialInformation>> entry : map.entrySet()) {
        jsonElt.addJSONObject(jsonDate -> {
          jsonDate.put("day", formatDate.format(entry.getKey()));
          for (SocialInformation si : entry.getValue()) {
            jsonDate.put("socialInfo", getJSONSocialInfo(si, multilang));
          }
          return jsonDate;
        });

      }
      return jsonElt;
    });

  }

  /**
   * convert a UserDetail to JSONObject
   * @param user the user detail
   * @return JSONObject
   */
  private Function<JSONCodec.JSONArray, JSONCodec.JSONArray> userDetailToJSON(UserDetail user) {
    return jsonAuthor -> {
      jsonAuthor.addJSONObject(jsonUser -> {
        jsonUser.put("id", user.getId());
        jsonUser.put("displayedName", user.getDisplayedName());
        jsonUser.put("profilPhoto", URLManager.getApplicationURL() + user.getSmallAvatar());
        return jsonUser;
      });
      return jsonAuthor;
    };
  }
}
