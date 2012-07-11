/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.socialnetwork.profil.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserFull;
import java.sql.SQLException;

import com.silverpeas.socialnetwork.SocialNetworkException;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author Bensalem Nabil
 */
public class ProfilSessionController extends AbstractComponentSessionController {

  public ProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.social.multilang.socialNetworkBundle",
        "com.silverpeas.social.settings.socialNetworkIcons",
        "com.silverpeas.social.settings.socialNetworkSettings");
  }

  /**
   * get this user with full information
   * @param userId
   * @return UserFull
   */
  public UserFull getUserFul(String userId) {
    return this.getOrganizationController().getUserFull(userId);
  }

  /*
   * this userId is in my Contacts
   * @param: int userId
   * @return true if this user in my Contacts
   */
  public boolean isInMyContact(String userId) throws SocialNetworkException {
    try {
      int id = Integer.parseInt(userId);
      return new RelationShipService().isInRelationShip(Integer.parseInt(this.getUserId()), id);
    } catch (SQLException ex) {
      throw new SocialNetworkException(
          "ProfilSessionController.isInMyContact(String userId)",
          SilverpeasException.ERROR, "root.EX_NO_MESSAGE", ex);
    }
  }
}
