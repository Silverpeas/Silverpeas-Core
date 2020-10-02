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
package org.silverpeas.core.chat.listeners;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.socialnetwork.relationship.RelationShip;
import org.silverpeas.core.socialnetwork.relationship.RelationShipEvent;

import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * Listen relationship modifications to clone them in the Chat server
 * @author remipassmoilesel
 */
@Service
public class RelationShipListener extends CDIResourceEventListener<RelationShipEvent> {

  @Inject
  @DefaultChatServer
  private ChatServer server;

  @Override
  public void onCreation(final RelationShipEvent event) throws Exception {
    final RelationShip rs = event.getTransition().getAfter();
    final User uf1 = User.getById(String.valueOf(rs.getUser1Id()));
    final User uf2 = User.getById(String.valueOf(rs.getUser2Id()));
    if (isRelationshipMappable(uf1, uf2)) {
      server.createRelationShip(uf1, uf2);
      logger.debug("Chat relationship between {0} and {1} has been created", uf1.getId(), uf2.getId());
    } else {
      logger.debug("No chat relationship can be created between user {0} and {1}", uf1.getId(), uf2.getId());
    }
  }

  @Override
  public void onDeletion(final RelationShipEvent event) throws Exception {
    final RelationShip rs = event.getTransition().getBefore();
    final User uf1 = User.getById(String.valueOf(rs.getUser1Id()));
    final User uf2 = User.getById(String.valueOf(rs.getUser2Id()));
    if (isRelationshipMappable(uf1, uf2)) {
      server.deleteRelationShip(uf1, uf2);
      logger.debug("Chat relationship between {0} and {1} has been deleted", uf1.getId(),
          uf2.getId());
    } else {
      logger.debug("No chat relationship can be deleted between user {0} and {1}", uf1.getId(),
          uf2.getId());
    }
  }

  @Override
  public boolean isEnabled() {
    return ChatServer.isEnabled();
  }

  private boolean isRelationshipMappable(final User uf1, final User uf2) {
    return Stream.of(uf1, uf2)
        .filter(u -> {
          final boolean domainMapped = server.isUserDomainSupported(u.getDomainId());
          if (!domainMapped) {
            logger.debug(
                "No chat relationship can be handled for user {0} as its domain is not mapped " +
                    "with a chat server", u.getId());
          }
          return domainMapped;
        })
        .count() == 2;
  }
}
