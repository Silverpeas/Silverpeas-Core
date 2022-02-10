/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.portlets;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyContactsPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    RelationShipService relationShipService = RelationShipService.get();
    List<String> listContactIds = relationShipService.getMyContactsIds(
        Integer.parseInt(UserDetail.getCurrentRequester().getId()));

    List<UserDetail> contactsConnected = new ArrayList<>();
    List<User> contactsMemberConnected = new ArrayList<>();
    List<UserDetail> contactsNotConnected = new ArrayList<>();
    if(listContactIds != null) {
      for(String userId : listContactIds) {
        UserDetail userDetail = UserDetail.getById(userId);

        if(userDetail.isConnected()) {
          contactsConnected.add(userDetail);
        } else {
          contactsNotConnected.add(userDetail);
        }

      }
    }
    // sort the list alphabetically
    Collections.sort(contactsConnected);
    Collections.sort(contactsNotConnected);

    for(UserDetail contact : contactsConnected) {
      contactsMemberConnected.add(ChatUser.fromUser(contact));
    }

    request.setAttribute("ContactsConnected", contactsMemberConnected);
    request.setAttribute("ContactsNotConnected", contactsNotConnected);

    include(request, response, "portlet.jsp");
  }

  /**
   * Include "help" JSP.
   */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response) throws PortletException {
    include(request, response, "help.jsp");
  }

  /**
   * Include a page.
   */
  private void include(RenderRequest request, RenderResponse response, String pageName)
      throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher =
          getPortletContext().getRequestDispatcher("/portlets/jsp/myContacts/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
