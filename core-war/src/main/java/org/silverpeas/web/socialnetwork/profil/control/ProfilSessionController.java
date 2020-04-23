/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.web.socialnetwork.profil.control;

import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

/**
 * @author Bensalem Nabil
 */
public class ProfilSessionController extends AbstractComponentSessionController {

  public ProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "org.silverpeas.social.multilang.socialNetworkBundle",
        "org.silverpeas.social.settings.socialNetworkIcons",
        "org.silverpeas.social.settings.socialNetworkSettings");
  }

  /*
   * this userId is in my Contacts
   * @param: int userId
   * @return true if this user in my Contacts
   */
  public boolean isInMyContact(String userId) {
    int id = Integer.parseInt(userId);
    return RelationShipService.get().isInRelationShip(Integer.parseInt(this.getUserId()), id);
  }

}