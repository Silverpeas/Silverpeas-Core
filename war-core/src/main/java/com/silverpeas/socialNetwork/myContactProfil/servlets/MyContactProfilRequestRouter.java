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



import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.socialNetwork.myContactProfil.control.MyContactProfilSessionController;
import com.silverpeas.socialNetwork.user.model.SNFullUser;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;

import com.stratelia.webactiv.beans.admin.UserFull;


import java.util.ArrayList;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author azzedine
 */
public class MyContactProfilRequestRouter extends ComponentRequestRouter {

  @Override
  public String getSessionControlBeanName() {
    return "myContactProfil";
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyContactProfilSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "#";

    MyContactProfilSessionController MyContactProfillSC = (MyContactProfilSessionController) componentSC;
    String userId = request.getParameter("userId");
    SNFullUser snUserFull = new SNFullUser(userId);
    if (function.equalsIgnoreCase("MyEvents")) {
      try {
        request.setAttribute("type", SocialInformationType.EVENT);
      } catch (Exception ex) {
        Logger.getLogger(MyContactProfilRequestRouter.class.getName()).log(Level.SEVERE, null, ex);
      }
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("ALL")) {
      request.setAttribute("type", SocialInformationType.ALL);
     destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("MyPhotos")) {
      request.setAttribute("type", SocialInformationType.PHOTO);
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("MyPubs")) {
      request.setAttribute("type", SocialInformationType.PUBLICATION);
      destination = "/socialNetwork/jsp/myContactProfil/profilTemplate.jsp";
    } else if (function.equalsIgnoreCase("MyInfos") || function.equalsIgnoreCase("Main")) {

      UserFull uf = snUserFull.getUserFull();
      
      request.setAttribute("specificLabels", uf.getSpecificLabels(componentSC.getLanguage()));
      String[] array = uf.getPropertiesNames();
      List<String> propertiesKey = new ArrayList<String>();
      List<String> propertiesValue = new ArrayList<String>();
      List<String> properties = new ArrayList<String>();
      for (int i = 0; i < array.length; i++) {
        if (!array[i].startsWith("password")) {
          properties.add(array[i]);
          propertiesKey.add(uf.getSpecificLabel(MyContactProfillSC.getLanguage(), array[i]));
          propertiesValue.add(uf.getValue(array[i]));
        }
      }
      request.setAttribute("properties", properties);
      request.setAttribute("propertiesKey", propertiesKey);
      request.setAttribute("propertiesValue", propertiesValue);
      destination = "/socialNetwork/jsp/myContactProfil/infosTemplate.jsp";
    }
    request.setAttribute("snUserFull", snUserFull);
    return destination;
  }
}
