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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.socialNetwork.myContactProfil.control.MyContactProfilSessionController;
import com.silverpeas.socialNetwork.user.model.SNFullUser;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 *
 * @author azzedine
 */
public class MyContactProfilRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
  private MyContactProfilSessionController myContactProfillSC;
  private final int NUMBER_CONTACTS_TO_DISPLAY = 3;

  /**
   * get Session ControlBeanName
   * @return String
   */
  @Override
  public String getSessionControlBeanName() {
    return "myContactProfil";
  }
/**
 * create ComponentSession Controller
 * @param mainSessionCtrl
 * @param componentContext
 * @return ComponentSessionController
 */
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyContactProfilSessionController(mainSessionCtrl, componentContext);
  }

  /**
   *get Destination
   * @param function
   * @param componentSC
   * @param request
   * @return
   */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "#";
    myContactProfillSC = (MyContactProfilSessionController) componentSC;
    String userId = request.getParameter("userId");
    SNFullUser snUserFull = new SNFullUser(userId);
    if (function.equalsIgnoreCase("Infos")) {

      request.setAttribute("UserFull", myContactProfillSC.getUserFull(userId));
      request.setAttribute("View", function);
      
      destination = "/socialNetwork/jsp/myContactProfil/myContactProfile.jsp";

    } else if (function.equalsIgnoreCase("MyEvents")) {
      try {
        request.setAttribute("type", SocialInformationType.EVENT);
      } catch (Exception ex) {
        Logger.getLogger(MyContactProfilRequestRouter.class.getName()).log(Level.SEVERE, null, ex);
      }
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("ALL") || function.equalsIgnoreCase("Main")) {
      request.setAttribute("type", SocialInformationType.ALL);
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("MyPhotos")) {
      request.setAttribute("type", SocialInformationType.PHOTO);
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("MyPubs")) {
      request.setAttribute("type", SocialInformationType.PUBLICATION);
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    }

    request.setAttribute("snUserFull", snUserFull);
    List<String> contactIds = myContactProfillSC.getContactsIdsForUser(userId);
    request.setAttribute("Contacts", chooseContactsToDisplay(contactIds));
    request.setAttribute("ContactsNumber", contactIds.size());
    contactIds = myContactProfillSC.getCommonContactsIdsForUser(userId);
    request.setAttribute("CommonContacts", chooseContactsToDisplay(contactIds));
    request.setAttribute("CommonContactsNumber", contactIds.size());
    return destination;
  }

  /**
   * methode to choose (x) contacts for display it in the page profil
   * x is the number of contacts
   * the methode use Random rule
   * @param contactIds
   * @return List<SNContactUser>
   */
  private List<UserDetail> chooseContactsToDisplay(List<String> contactIds) {
    int numberOfContactsTodisplay;
    List<UserDetail> contacts = new ArrayList<UserDetail>();
    try {
      numberOfContactsTodisplay = Integer.parseInt(myContactProfillSC.getSettings().getString(
          "numberOfContactsTodisplay"));
    } catch (NumberFormatException ex) {
      numberOfContactsTodisplay = NUMBER_CONTACTS_TO_DISPLAY;
    }
    if (contactIds.size() <= numberOfContactsTodisplay) {
      for (String contactId : contactIds) {
        contacts.add(myContactProfillSC.getUserDetail(contactId));
      }
    } else {
      Random random = new Random();
      int indexContactsChoosed = (random.nextInt(contactIds.size()));
      for (int i = 0; i < numberOfContactsTodisplay; i++) {
        String contactId = contactIds.get((indexContactsChoosed + i) % numberOfContactsTodisplay);
        contacts.add(myContactProfillSC.getUserDetail(contactId));
      }
    }

    return contacts;
  }
}
