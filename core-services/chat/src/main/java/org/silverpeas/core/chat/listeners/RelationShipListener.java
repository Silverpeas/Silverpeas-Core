package org.silverpeas.core.chat.listeners;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.socialnetwork.relationship.RelationShip;
import org.silverpeas.core.socialnetwork.relationship.RelationShipEvent;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;

/**
 * Listen relationship modifications to clone them in the Chat server
 * @author remipassmoilesel
 */
public class RelationShipListener extends CDIResourceEventListener<RelationShipEvent> {

  private SilverLogger logger = SilverLogger.getLogger(this);

  @Inject
  @DefaultChatServer
  private ChatServer server;

  @Override
  public void onCreation(final RelationShipEvent event) throws Exception {
    final RelationShip rs = event.getTransition().getAfter();

    UserDetail uf1 = UserDetail.getById(String.valueOf(rs.getUser1Id()));
    UserDetail uf2 = UserDetail.getById(String.valueOf(rs.getUser2Id()));

    server.createRelationShip(uf1, uf2);

    logger.info("Chat relationship between {0} and {1} has been created", uf1.getId(), uf2.getId());
  }

  @Override
  public void onDeletion(final RelationShipEvent event) throws Exception {
    final RelationShip rs = event.getTransition().getBefore();

    UserDetail uf1 = UserDetail.getById(String.valueOf(rs.getUser1Id()));
    UserDetail uf2 = UserDetail.getById(String.valueOf(rs.getUser2Id()));

    server.deleteRelationShip(uf1, uf2);

    logger.info("Chat relationship between {0} and {1} has been deleted", uf1.getId(), uf2.getId());
  }

  @Override
  public boolean isEnabled() {
    return server.isAvailable();
  }
}
