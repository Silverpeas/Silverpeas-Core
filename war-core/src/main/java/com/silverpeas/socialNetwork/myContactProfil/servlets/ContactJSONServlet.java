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
package com.silverpeas.socialNetwork.myContactProfil.servlets;

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

public class ContactJSONServlet extends HttpServlet {

  public static final int DEFAULT_OFFSET = 0;
  public static final int DEFAULT_ELEMENT_PER_PAGE = 3;
  public int elements_per_page;
  String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
  String iconURL = m_context + "/socialNetwork/jsp/icons/";
  SocialInformationType type;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Map<Date, List<SocialInformation>> map = new LinkedHashMap<Date, List<SocialInformation>>();

    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
        "SilverSessionController");

    ResourceLocator multilang = new ResourceLocator(
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle", Locale.getDefault());

    String userId = m_MainSessionCtrl.getUserId();
    if (null != request.getParameter("userId")) {
      userId = request.getParameter("userId");
    }


    try {
      //recover the type
      type = SocialInformationType.valueOf(request.getParameter("type"));

      //recover the first Element
      int offset = DEFAULT_OFFSET;
      if (StringUtil.isInteger(request.getParameter("offset"))) {
        offset = Integer.parseInt(request.getParameter("offset"));
      }
      //recover the numbre elements per page
      int limit = DEFAULT_ELEMENT_PER_PAGE;
      if (StringUtil.isInteger(multilang.getString("profil.elements_per_page." + type.toString()))) {
        limit = Integer.parseInt(multilang.getString("profil.elements_per_page." + type.toString()));
      }

      map = new SocialNetworkService(userId).getSocialInformation(type, limit, offset * limit);

    } catch (Exception ex) {
      Logger.getLogger(ContactJSONServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    PrintWriter out = response.getWriter();
    out.println(toJsonS(map));

  }

  private JSONObject toJson(SocialInformation event) {
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    JSONObject valueObj = new JSONObject();
    valueObj.put("author", event.getAuthor());
    valueObj.put("title", event.getTitle());
    valueObj.put("description", event.getDescription() + " ");
    valueObj.put("hour", formatTime.format(event.getDate()));
    valueObj.put("url", m_context + event.getUrl());
    valueObj.put("icon",
        getIconUrl(SocialInformationType.valueOf(event.getType())) + event.getIcon());
    return valueObj;
  }

  private JSONArray toJsonS(Map<Date, List<SocialInformation>> map) {
    SimpleDateFormat formatDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
    JSONArray result = new JSONArray();
    for (Map.Entry<Date, List<SocialInformation>> entry : map.entrySet()) {
      JSONArray jsonArrayDateWithValues = new JSONArray();
      Object key = entry.getKey();

      JSONArray jsonArray = new JSONArray();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("day", formatDate.format(key));
      List<SocialInformation> events = entry.getValue();
      for (SocialInformation event : events) {
        jsonArray.put(toJson(event));
      }
      jsonArrayDateWithValues.put(jsonObject);
      jsonArrayDateWithValues.put(jsonArray);
      result.put(jsonArrayDateWithValues);
    }
    return result;
  }

  private String getIconUrl(SocialInformationType type) {
    String url = iconURL;
    if (type.equals(SocialInformationType.PHOTO)) {
      url = m_context;
    }
    return url;
  }
}
