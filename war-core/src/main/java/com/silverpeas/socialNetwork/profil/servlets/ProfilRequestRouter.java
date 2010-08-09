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
package com.silverpeas.socialNetwork.profil.servlets;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.socialNetwork.SocialNetworkException;
import com.silverpeas.socialNetwork.profil.control.ProfilSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;

/**
 *
 * @author azzedine
 */
public class ProfilRequestRouter extends ComponentRequestRouter {

  private ProfilSessionController myProfilSC;
  private String m_context="http://localhost:8000/silverpeas/";
  

  @Override
  public String getSessionControlBeanName() {
    return "profil";
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {


    return new ProfilSessionController(mainSessionCtrl, componentContext);

  }

  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "#";

    myProfilSC = (ProfilSessionController) componentSC;
    String userId = request.getParameter("userId");
   if (function.equalsIgnoreCase("Main")) {

     
      if (myProfilSC.getUserId().equals(userId)) {//go to my Profile

        destination =m_context+"RMyProfil/jsp/MyInfos";

      } else if (isInMyContact(userId)) {// this is  in my contacts


        destination = m_context + "RmyContactProfil/jsp/MyInfos?userId="+userId;

      } else {// this is not in my contacts
        request.setAttribute("userFull", myProfilSC.getUserFul(userId));
        request.setAttribute("Settings", myProfilSC.getSettings());
        destination = "/socialNetwork/jsp/profil/profilPublic.jsp";

      }
    }
    return destination;
  }
  /*
   * this userId is in my Contacts
   * @param: int userId
   * @return true if this user  in my Contacts
   */
  /*
   * this userId is in my Contacts
   * @param: int userId
   * @return true if this user  in my Contacts
   */

  public boolean isInMyContact(String userId) {
    try {
      return myProfilSC.isInMyContact(userId);
    } catch (SocialNetworkException ex) {
      Logger.getLogger(ProfilRequestRouter.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
}
