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
package com.silverpeas.socialNetwork.myProfil.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.silverpeas.socialNetwork.SocialNetworkException;
import java.util.List;


import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private AdminController m_AdminCtrl = null;
  private long domainActions = -1;

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle",
        "com.silverpeas.socialNetwork.settings.socialNetworkIcons",
        "com.silverpeas.socialNetwork.settings.socialNetworkSettings");
    m_AdminCtrl = new AdminController(getUserId());
  }

  public Hashtable getSocialInformation(SocialInformationType type, int limit, int offset) throws
      CalendarException, UtilException {

    List list = null;
    switch (type) {
      case EVENT:
        // list = new SocialEvent().getSocialInformationsList(getUserId(), null, limit, offset);
        break;
      default:
      //    list = new SocialEvent().getSocialInformationsList(getUserId(), null, limit, offset);
    }
    return null;
  }

  public UserFull getUserFul(String userId) {

    return this.getOrganizationController().getUserFull(userId);
  }
  /*
   * modify the properties of user
   *
   * @param: String idUser, Hashtable<String, String> properties
   */

  public void modifyUser(String idUser, Hashtable<String, String> properties) throws
      SocialNetworkException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null) {
      throw new SocialNetworkException(
          "MyProfilSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }

    


      // process extra properties
      Set<String> keys = properties.keySet();
      Iterator<String> iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = iKeys.next();
        value = properties.get(key);

        theModifiedUser.setValue(key, value);
      }

      idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
      if (idRet == null || idRet.length() <= 0) {
        throw new SocialNetworkException(
            "MyProfilSessionController.modifyUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
            + idUser);
      }
   
  }

  
}
