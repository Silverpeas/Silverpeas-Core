/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.socialnetwork.myContactProfil.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.directory.model.Member;
import com.silverpeas.socialnetwork.myContactProfil.control.MyContactProfilSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author azzedine
 */
public class MyContactProfilRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
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
    MyContactProfilSessionController sc = (MyContactProfilSessionController) componentSC;
    String userId = request.getParameter("userId");
    if (function.equalsIgnoreCase("Infos")) {
      request.setAttribute("View", function);
      destination = "/socialnetwork/jsp/myContactProfil/myContactProfile.jsp";
    } else if ("Main".equalsIgnoreCase(function)) {
      request.setAttribute("View", "Wall");
      destination = "/socialnetwork/jsp/myContactProfil/myContactProfile.jsp";
    }

    request.setAttribute("UserFull", sc.getUserFull(userId));
    request.setAttribute("Member", new Member(sc.getUserDetail(userId)));
    List<String> contactIds = sc.getContactsIdsForUser(userId);
    request.setAttribute("Contacts", chooseContactsToDisplay(contactIds, sc));
    request.setAttribute("ContactsNumber", contactIds.size());
    contactIds = sc.getCommonContactsIdsForUser(userId);
    request.setAttribute("CommonContacts", chooseContactsToDisplay(contactIds, sc));
    request.setAttribute("CommonContactsNumber", contactIds.size());
    return destination;
  }

  /**
   * methode to choose (x) contacts for display it in the page profil x is the number of contacts
   * the methode use Random rule
   * @param contactIds
   * @return List<SNContactUser>
   */
  private List<UserDetail> chooseContactsToDisplay(List<String> contactIds,
      MyContactProfilSessionController sc) {
    List<UserDetail> contacts = new ArrayList<UserDetail>();
    int numberOfContactsTodisplay =
        sc.getSettings().getInteger("numberOfContactsTodisplay", NUMBER_CONTACTS_TO_DISPLAY);
    if (contactIds.size() <= numberOfContactsTodisplay) {
      for (String contactId : contactIds) {
        contacts.add(sc.getUserDetail(contactId));
      }
    } else {
      Random random = new Random();
      int indexContactsChoosed = (random.nextInt(contactIds.size()));
      for (int i = 0; i < numberOfContactsTodisplay; i++) {
        String contactId = contactIds.get((indexContactsChoosed + i) % numberOfContactsTodisplay);
        contacts.add(sc.getUserDetail(contactId));
      }
    }

    return contacts;
  }
}
