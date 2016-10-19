package org.silverpeas.core.chat.listeners;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;

/**
 * Listen user modifications to clone them in Chat server
 *
 * @author remipassmoilesel
 */
public class ChatUserEventListener extends CDIResourceEventListener<UserEvent> {

  private SilverLogger logger = SilverLogger.getLogger(this);

  @Inject
  private ChatServer server;

  @Override
  public void onCreation(final UserEvent event) throws Exception {
    UserDetail detail = event.getTransition().getAfter();
    server.createUser(detail);
    logger.info("Xmpp account have been created for user {0}", detail.getId());
  }

  @Override
  public void onDeletion(final UserEvent event) throws Exception {
    UserDetail detail = event.getTransition().getBefore();
    server.deleteUser(detail);
    logger.info("Xmpp account have been deleted for user {0}", detail.getId());
  }

}
