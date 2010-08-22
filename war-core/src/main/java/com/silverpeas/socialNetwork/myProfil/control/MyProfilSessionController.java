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
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.silverpeas.socialNetwork.SocialNetworkException;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import java.util.List;


import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private AdminController m_AdminCtrl = null;
  private RelationShipService relationShipService = new RelationShipService();

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle",
        "com.silverpeas.socialNetwork.settings.socialNetworkIcons",
        "com.silverpeas.socialNetwork.settings.socialNetworkSettings");
    m_AdminCtrl = new AdminController(getUserId());
  }

  /**
   * get all  RelationShips ids for this user
   * @return:List<String>
   * @param: int myId
   *
   */
  public List<String> getContactsIdsForUser(String userId) {
    try {
      return relationShipService.getMyContactsIds(Integer.parseInt(userId));
    } catch (SQLException ex) {
      SilverTrace.error("MyContactProfilSessionController",
          "MyContactProfilSessionController.getContactsForUser", "", ex);
    }
    return new ArrayList<String>();
  }
/**
 * get this user with full information
 * @param userId
 * @return UserFull
 */
  public UserFull getUserFul(String userId) {
    return this.getOrganizationController().getUserFull(userId);
  }
  /**
   * update the properties of user
   * @param idUser
   * @param properties
   * @throws SocialNetworkException
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
