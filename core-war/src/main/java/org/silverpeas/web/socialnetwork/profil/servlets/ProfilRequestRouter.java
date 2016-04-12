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

package org.silverpeas.web.socialnetwork.profil.servlets;

import org.silverpeas.core.web.directory.model.Member;
import org.silverpeas.web.socialnetwork.profil.control.ProfilSessionController;
import org.silverpeas.core.socialnetwork.SocialNetworkException;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.logging.SilverLogger;


/**
 * @author azzedine
 */
public class ProfilRequestRouter extends ComponentRequestRouter<ProfilSessionController> {

  private static final long serialVersionUID = 1L;

  @Override
  public String getSessionControlBeanName() {
    return "profil";
  }

  @Override
  public ProfilSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ProfilSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(String function, ProfilSessionController profileSC,
      HttpRequest request) {
    String destination = "#";
    String userId = request.getParameter("userId");
    String m_context = request.getScheme() + "://" + request.getServerName() + ":" + request.
        getServerPort() + request.getContextPath();

    if (function.equalsIgnoreCase("Main")) {
      if (profileSC.getUserId().equals(userId)) {
        // go to my Profile
        destination = m_context + "/RMyProfil/jsp/MyInfos";
      } else if (isInMyContact(userId, profileSC)) {
        // this is one of my contacts
        destination = m_context + "/RContactProfile/jsp/Main?userId=" + userId;
      } else {
        // this is not one of my contacts
        request.setAttribute("userFull", profileSC.getUserFul(userId));
        request.setAttribute("Member", new Member(profileSC.getUserDetail(userId)));
        request.setAttribute("Settings", profileSC.getSettings());
        destination = "/socialNetwork/jsp/profil/profilPublic.jsp";
      }
    }
    return destination;
  }

  /**
   * return true if this userId is in my Contacts
   * @param: int userId
   * @return boolean
   */

  public boolean isInMyContact(String userId, ProfilSessionController profileSC) {
    try {
      return profileSC.isInMyContact(userId);
    } catch (SocialNetworkException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return false;
  }
}