/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.socialnetwork.mycontactprofil.control;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.util.List;

/**
 * @author Bensalem Nabil
 */
public class MyContactProfilSessionController extends AbstractComponentSessionController {

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
  public boolean isInMyContact(String userId) {
    int id = Integer.parseInt(userId);
    return getRelationShipService().isInRelationShip(Integer.parseInt(this.getUserId()), id);
  }

  /**
   * get all RelationShips ids for this user
   * @param userId : int myId
   * @return : List<String> of contact identifiers
   */
  public List<String> getContactsIdsForUser(String userId) {
    return getRelationShipService().getMyContactsIds(Integer.parseInt(userId));
  }

  /**
   * get all RelationShips ids for this user
   * @return :List<String>
   * @param userId : int myId
   */
  public List<String> getCommonContactsIdsForUser(String userId) {
    return getRelationShipService().getAllCommonContactsIds(Integer.parseInt(userId),
        Integer.parseInt(this.getUserId()));
  }

  private RelationShipService getRelationShipService() {
    return RelationShipService.get();
  }
}
