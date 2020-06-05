/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.socialnetwork;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.socialnetwork.relationship.RelationShip;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.List;

/**
 * Listens events about the deletion of a user in Silverpeas to clean up all the data relative
 * to the deleted user in the Silverpeas social network service.
 * @author mmoquillon
 */
public class SocialNetworkUserListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  private SocialNetworkService socialNetworkService;
  @Inject
  private InvitationService invitationService;
  @Inject
  private RelationShipService relationShipService;

  @Override
  public void onDeletion(final UserEvent event) throws Exception {
    UserDetail user = event.getTransition().getBefore();
    SilverLogger.getLogger(this)
        .debug("Delete all the social network data of user {0}", user.getId());

    List<RelationShip> relationShips =
        relationShipService.getAllMyRelationShips(Integer.valueOf(user.getId()));
    relationShips.forEach(
        relationShip -> relationShipService.removeRelationShip(relationShip.getUser1Id(),
            relationShip.getUser2Id()));

    invitationService.deleteAllMyInvitations(user.getId());
    socialNetworkService.removeAllExternalAccount(user.getId());
  }
}
