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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.socialNetwork.myProfil.servlets;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.myProfil.control.SocialNetworkService;
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONObject;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.socialNetwork.user.model.SNContactUser;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;

public class JSONServlet extends HttpServlet {

  public static final String AVATAR_EXTENTION = ".jpg";
  public static final int DEFAULT_OFFSET = 0;
  public static final int DEFAULT_ELEMENT_PER_PAGE = 3;
  private static final long serialVersionUID = -843491398398079951L;
  private static final ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.socialNetwork.settings.socialNetworkSettings", "");
  public static int elements_per_page = DEFAULT_ELEMENT_PER_PAGE;

  static {
    if (StringUtil.isInteger(settings.getString("profil.elements_per_page"))) {
      elements_per_page = Integer.parseInt(settings.getString("profil.elements_per_page"));
    }
  }
  static String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
      "ApplicationURL");
  static String iconURL = m_context + "/socialNetwork/jsp/icons/";
  static SocialInformationType type;
  private static SocialNetworkService socialNetworkService = new SocialNetworkService();

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
    Locale locale = request.getLocale();
    String userId = m_MainSessionCtrl.getUserId();
    socialNetworkService.setMyId(userId);
    String action = request.getParameter("Action");
    if ("getLastStatus".equalsIgnoreCase(action)) {
      String status = socialNetworkService.getLastStatusService();
      JSONObject jsonStatus = new JSONObject();
      jsonStatus.put("status", status);
      PrintWriter out = response.getWriter();
      out.println(jsonStatus);
    } else if ("updateStatus".equalsIgnoreCase(action)) {
      String status = request.getParameter("status");
      //if status equal null or empty so don't do update status and do get Last status
      if (StringUtil.isDefined(status)) {
        status = socialNetworkService.changeStatusService(status);
      } else {
        status = socialNetworkService.getLastStatusService();
      }
      JSONObject jsonStatus = new JSONObject();
      jsonStatus.put("status", status);
      PrintWriter out = response.getWriter();
      out.println(jsonStatus);
    } else {
      Map<Date, List<SocialInformation>> map = new LinkedHashMap<Date, List<SocialInformation>>();
      ResourceLocator multilang = new ResourceLocator(
          "com.silverpeas.socialNetwork.multilang.socialNetworkBundle", locale.getLanguage());
      try {
        //recover the type
        type = SocialInformationType.valueOf(request.getParameter("type"));
        //recover the first Element
        int paginationIndex = DEFAULT_OFFSET;
        if (StringUtil.isInteger(request.getParameter("offset"))) {
          paginationIndex = Integer.parseInt(request.getParameter("offset"));
        }
        socialNetworkService.setElementPerPage(elements_per_page);
        map = socialNetworkService.getSocialInformation(type, elements_per_page, paginationIndex);

      } catch (Exception ex) {
        Logger.getLogger(JSONServlet.class.getName()).log(Level.SEVERE, null, ex);
      }
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      out.println(toJsonS(map, locale));

    }
  }

  /**
   * convert the SocialInormation to JSONObject
   * @param event
   * @return JSONObject
   */
  private JSONObject toJson(SocialInformation event, Locale locale) {
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", locale);

    JSONObject valueObj = new JSONObject();
    if (event.getType().equals(SocialInformationType.RELATIONSHIP.toString())) {
      SNContactUser contactUser2 = new SNContactUser(event.getTitle());
      valueObj.put("type", event.getType());
      valueObj.put("author", new SNContactUser(event.getAuthor()).getFirstName());
      valueObj.put("title", contactUser2.getLastName() + " " + contactUser2.getFirstName());
      valueObj.put("description", event.getDescription());
      valueObj.put("hour", formatTime.format(event.getDate()));
      valueObj.put("url", m_context + event.getUrl());
      valueObj.put("icon", m_context + contactUser2.getProfilPhoto());
      return valueObj;
    }
    valueObj.put("type", event.getType());
    valueObj.put("author", event.getAuthor());
    if (event.getType().equals(SocialInformationType.STATUS.toString())) {
      SNContactUser contactUser = new SNContactUser(event.getTitle());
      valueObj.put("title", contactUser.getFirstName());
    } else {
      valueObj.put("title", event.getTitle());
    }
    //if time not identified display string empty
    if ("00:00".equalsIgnoreCase(formatTime.format(event.getDate()))) {
      valueObj.put("hour", "");
    } else {
      valueObj.put("hour", formatTime.format(event.getDate()));
    }
    valueObj.put("description", event.getDescription() + " ");
    valueObj.put("hour", formatTime.format(event.getDate()));
    valueObj.put("url", m_context + event.getUrl());
    valueObj.put("icon",
        getIconUrl(SocialInformationType.valueOf(event.getType())) + event.getIcon());
    return valueObj;
  }

  /**
   * convert the Map of socailInformation to JSONArray
   * @param Map<Date, List<SocialInformation>> map
   * @return JSONArray
   */
  private JSONArray toJsonS(Map<Date, List<SocialInformation>> map, Locale locale) {
    SimpleDateFormat formatDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", locale);
    JSONArray result = new JSONArray();
    for (Map.Entry<Date, List<SocialInformation>> entry : map.entrySet()) {
      JSONArray jsonArrayDateWithValues = new JSONArray();
      Object key = entry.getKey();
      JSONArray jsonArray = new JSONArray();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("day", formatDate.format(key));
      List<SocialInformation> events = entry.getValue();
      for (SocialInformation event : events) {
        jsonArray.put(toJson(event, locale));
      }
      jsonArrayDateWithValues.put(jsonObject);
      jsonArrayDateWithValues.put(jsonArray);
      result.put(jsonArrayDateWithValues);
    }
    return result;
  }

  /**
   * return the url of icon
   * @param SocialInformationType type
   * @return String
   */
  private String getIconUrl(SocialInformationType type) {
    String url = iconURL;
    if (SocialInformationType.PHOTO.equals(type)) {
      url = m_context;
    }
    return url;
  }

  /**
   * return the title when the type is RELATIONSHIP
   * @param SocialInformationType type
   * @return String
   */
  private String getTitle(SocialInformation event) {
    return event.getTitle();
  }
}
