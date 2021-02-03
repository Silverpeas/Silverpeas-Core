/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.socialnetwork.newsfeed.servlets;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONObject;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator;
import org.silverpeas.web.socialnetwork.myprofil.control.SocialNetworkService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.time.ZoneId.systemDefault;

public class NewsFeedJSONServlet extends HttpServlet {

  private static final long serialVersionUID = -7056446975706739300L;
  private static final String SILVERPEAS_NEWS_FEED_LAST_DATE = "Silverpeas_NewsFeed_LastDate";
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "description";
  private static final String LABEL = "label";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    // view type is Wall or MyFeed or MyContactWall
    String view = request.getParameter("View");
    if (!StringUtil.isDefined(view)) {
      view = "Wall";
    }

    LocalizationBundle multilang =
        ResourceLocator.getLocalizationBundle("org.silverpeas.social.multilang.socialNetworkBundle",
            mainSessionCtrl.getFavoriteLanguage());

    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.social.settings.socialNetworkSettings");
    int maxNbTries = settings.getInteger("newsFeed.maxNbTries", 10);
    int minNbDataBeforeNewTry = settings.getInteger("newsFeed.minNbDataBeforeNewTry", 15);

    if (StringUtil.getBooleanValue(request.getParameter("Init"))) {
      session.setAttribute(SILVERPEAS_NEWS_FEED_LAST_DATE, LocalDate.now());
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

      Period period = getPeriod(session, settings);

      map = getInformation(view, userId, type, anotherUserId, period);

      int nbTries = 0;
      while (getNumberOfInformations(map) < minNbDataBeforeNewTry && nbTries < maxNbTries) {
        period = getPeriod(session, settings);

        map.putAll(getInformation(view, userId, type, anotherUserId, period));
        nbTries++;
      }

    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    try {
      PrintWriter out = response.getWriter();
      out.println(toJsonS(map, multilang));
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private Period getPeriod(HttpSession session, SettingBundle settings) {
    final int periodLength = settings.getInteger("newsFeed.period", 15);
    final LocalDate lastDate = (LocalDate) session.getAttribute(SILVERPEAS_NEWS_FEED_LAST_DATE);
    final OffsetDateTime begin = lastDate.minusDays(periodLength).atStartOfDay(systemDefault()).toOffsetDateTime();
    final OffsetDateTime end = lastDate.plusDays(1).atStartOfDay(systemDefault()).minusSeconds(1).toOffsetDateTime();
    session.setAttribute(SILVERPEAS_NEWS_FEED_LAST_DATE, begin.minusDays(1).toLocalDate());
    return Period.between(begin, end);
  }

  private Map<Date, List<SocialInformation>> getInformation(String view, String userId,
      SocialInformationType type, String anotherUserId, Period period) {
    Map<Date, List<SocialInformation>> map = new LinkedHashMap<>();

    SocialNetworkService socialNetworkService = new SocialNetworkService(userId);

    if ("MyFeed".equals(view)) {
      // get all data from me and my contacts
      map = socialNetworkService.getSocialInformationOfMyContacts(type, period);
    } else if ("MyContactWall".equals(view)) {
      // get all data from my contact
      if (StringUtil.isDefined(anotherUserId)) {
        map = socialNetworkService.getSocialInformationOfMyContact(anotherUserId, type, period);
      }
    } else { // Wall
      // get all data from me
      map = socialNetworkService.getSocialInformation(type, period);
    }
    return map;
  }

  private int getNumberOfInformations(Map<Date, List<SocialInformation>> map) {
    int nb = 0;
    for (Map.Entry<Date,List<SocialInformation>> entry: map.entrySet()) {
      nb += entry.getValue().size();
    }
    return nb;
  }

  /**
   * convert the SocialInformation to JSONObject
   * @param information the social information to convert
   * @return JSONObject
   */
  private UnaryOperator<JSONObject> getJSONSocialInfo(
      SocialInformation information, LocalizationBundle multilang) {
    return jsonSocialInfo -> {
      fillJsonSocialCommonInfo(information, jsonSocialInfo);
      if (information.getType().equals(SocialInformationType.RELATIONSHIP.toString())) {
        final UserDetail contactUser2 = UserDetail.getById(information.getTitle());
        jsonSocialInfo.putJSONObject(TITLE, userDetailToJSON(contactUser2));
        jsonSocialInfo.put(LABEL, multilang.getStringWithParams("newsFeed.relationShip.label",
            UserNameGenerator.toString(contactUser2, "-1")));
      } else if (information.getType().endsWith(SocialInformationType.EVENT.toString())) {
        if (!information.isUpdated() &&
            information.getIcon().startsWith(information.getType() + "_private")) {
          jsonSocialInfo.put(TITLE, multilang.getString("profil.icon.private.event"));
          jsonSocialInfo.put(DESCRIPTION, "");
        } else {
          jsonSocialInfo.put(TITLE, information.getTitle());
          jsonSocialInfo.put(DESCRIPTION, information.getDescription());
        }
        jsonSocialInfo
            .put(LABEL, multilang.getString("newsFeed." + information.getType().toLowerCase() +
                ".label"));
      } else if (information.getType().equals(SocialInformationType.STATUS.toString())) {
        jsonSocialInfo.put(TITLE, multilang.getString("newsFeed.status.suffix"));
      } else {
        jsonSocialInfo
            .put(LABEL, multilang.getString("newsFeed." + information.getType().toLowerCase() +
                ".updated." + information.isUpdated()));
      }
      return jsonSocialInfo;
    };
  }

  private void fillJsonSocialCommonInfo(final SocialInformation information,
      final JSONObject jsonSocialInfo) {
    final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    jsonSocialInfo.put("type", information.getType());
    final UserDetail contactUser1 = UserDetail.getById(information.getAuthor());
    jsonSocialInfo.putJSONObject("author", userDetailToJSON(contactUser1));
    jsonSocialInfo.put(TITLE, information.getTitle());
    jsonSocialInfo.put(DESCRIPTION,
        WebEncodeHelper.javaStringToHtmlParagraphe(information.getDescription()));
    // if time not identified display string empty
    if ("00:00".equalsIgnoreCase(formatTime.format(information.getDate()))) {
      jsonSocialInfo.put("hour", "");
    } else {
      jsonSocialInfo.put("hour", formatTime.format(information.getDate()));
    }
    String url = information.getUrl();
    if (StringUtil.isDefined(url)) {
      url = url.contains(URLUtil.getApplicationURL()) ? url :URLUtil.getApplicationURL() + url;
    } else {
      url = "javascript:void(0)";
    }
    jsonSocialInfo.put("url", url);
  }

  /**
   * convert the Map of socialInformation to JSON String representation
   * @param map
   * @return JSONArray
   */
  private String toJsonS(Map<Date, List<SocialInformation>> map, LocalizationBundle multilang) {
    SimpleDateFormat formatDate =
        new SimpleDateFormat("EEEE dd MMMM yyyy", multilang.getLocale());
    return JSONCodec.encodeArray(jsonRoot -> {
      for (Map.Entry<Date, List<SocialInformation>> entry : map.entrySet()) {
        jsonRoot.addJSONArray(json -> {
          json.addJSONObject(jsonDate -> {
            jsonDate.put("day", formatDate.format(entry.getKey()));
            return jsonDate;
          });
          json.addJSONArray(listElt -> {
            for (SocialInformation si : entry.getValue()) {
              listElt.addJSONObject(getJSONSocialInfo(si, multilang));
            }
            return listElt;
          });
          return json;
        });
      }
      return jsonRoot;
    });

  }

  /**
   * convert a UserDetail to JSONObject
   * @param user the user detail
   * @return JSONObject
   */
  private UnaryOperator<JSONObject> userDetailToJSON(UserDetail user) {
    return jsonUser -> {
        jsonUser.put("id", user.getId());
        jsonUser.put("displayedName", user.getDisplayedName());
        jsonUser.put("profilPhoto", URLUtil.getApplicationURL() + user.getSmallAvatar());
        return jsonUser;
      };
  }
}
