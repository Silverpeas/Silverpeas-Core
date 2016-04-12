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

package org.silverpeas.web.socialnetwork.mycontactprofil.control;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserFull;

import java.sql.SQLException;
import java.util.List;

import org.silverpeas.core.socialnetwork.SocialNetworkException;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.ArrayList;

/**
 * @author Bensalem Nabil
 */
public class MyContactProfilSessionController extends AbstractComponentSessionController {

  private RelationShipService relationShipService = RelationShipService.get();

  /**
   * @param mainSessionCtrl
   * @param componentContext
   */
  public MyContactProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.social.multilang.socialNetworkBundle",
        "org.silverpeas.social.settings.socialNetworkIcons",
        "org.silverpeas.social.settings.socialNetworkSettings");
  }

  /**
   * get this user with full information
   * @param userId the user identifier
   * @return UserFull
   */
  public UserFull getUserFull(String userId) {

    return this.getOrganisationController().getUserFull(userId);
  }

  /**
   * this userId is in my Contacts
   * @return true if this user in my Contacts
   * @param userId : int userId
   */
  public boolean isInMyContact(String userId) throws SocialNetworkException {
    try {
      int id = Integer.parseInt(userId);
      return relationShipService.isInRelationShip(Integer.parseInt(this.getUserId()), id);
    } catch (SQLException ex) {
      throw new SocialNetworkException("ProfilSessionController.isInMyContact(String userId)",
          SilverpeasException.ERROR, "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * get all RelationShips ids for this user
   * @param userId : int myId
   * @return : List<String> of contact identifiers
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
   * get all RelationShips ids for this user
   * @return :List<String>
   * @param userId : int myId
   */
  public List<String> getCommonContactsIdsForUser(String userId) {
    try {
      return relationShipService
          .getAllCommonContactsIds(Integer.parseInt(userId), Integer.parseInt(this.getUserId()));
    } catch (SQLException ex) {
      SilverTrace.error("MyContactProfilSessionController",
          "MyContactProfilSessionController.getContactsForUser", "", ex);
    }
    return new ArrayList<String>();
  }
}
