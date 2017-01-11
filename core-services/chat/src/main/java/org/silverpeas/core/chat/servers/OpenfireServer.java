package org.silverpeas.core.chat.servers;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.core.Response;

/**
 * <p>Openfire server management service</p>
 * <p>It implements the access mechanism to the XMPP server OpenFire.</p>
 * @author remipassmoilesel
 */
public class OpenfireServer implements ChatServer {

  private SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * URL target of the REST requests.
   */
  private final String url;

  private final HttpRequester theRequester;

  public OpenfireServer() {
    SettingBundle settings = ChatServer.getChatSettings();
    this.url = settings.getString("chat.xmpp.restUrl") + "/users";
    String key = settings.getString("chat.xmpp.restKey");
    this.theRequester = new HttpRequester(key);
  }

  @Override
  public void createUser(final User user) {
    // get cross domain login and password
    ChatUser chatUser = ChatUser.fromUser(user);
    Response response = null;
    try {
      HttpRequester requester = request(url);
      response = requester.post(o -> {
        o.put("username", chatUser.getChatLogin())
            .put("password", chatUser.getChatPassword())
            .put("name", chatUser.getDisplayedName());
        if (StringUtil.isDefined(chatUser.geteMail())) {
          o.put("email", chatUser.geteMail());
        }
        return o;
      });

      if (response.getStatus() != HttpRequester.STATUS_OK) {
        logger.error("Error while creating XMPP user: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnAdding("XMPP user", chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while creating XMPP user: {0}", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnAdding("XMPP user", chatUser.getId()), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public void deleteUser(final User user) {
    ChatUser chatUser = ChatUser.fromUser(user);
    Response response = null;
    try {
      HttpRequester requester = request(url, chatUser.getChatLogin());
      response = requester.delete();

      if (response.getStatus() != HttpRequester.STATUS_OK) {
        logger.error("Error while deleting XMPP user: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnDeleting("XMPP user", chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while deleting XMPP user: {0}", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnDeleting("XMPP user", chatUser.getId()), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public void createRelationShip(final User user1, final User user2) {
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    Response response = null;
    try {
      HttpRequester requester = request(url, chatUser1.getChatLogin(), "roster");
      response =
          requester.post(o -> o.put("jid", chatUser2.getChatId()).put("subscriptionType", "3"));

      if (response.getStatus() == HttpRequester.STATUS_CONFLICT) {
        logger.warn("The XMPP relationship between users {0} and {1} already exists!",
            chatUser1.getId(), chatUser2.getId());
      } else if (response.getStatus() != HttpRequester.STATUS_CREATED) {
        logger.error("Error while creating XMPP relationship: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnAdding("XMPP relationship",
                chatUser1.getId() + " <-> " + chatUser2.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while creating XMPP relationship: {0}", e.getMessage());
      throw new ChatServerException(SilverpeasExceptionMessages.failureOnAdding("XMPP relationship",
          chatUser1.getId() + " <-> " + chatUser2.getId()), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) {
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);

    Response response = null;
    try {
      HttpRequester requester = request(chatUser1.getChatLogin(), "roster", chatUser2.getChatId());
      response = requester.delete();

      if (response.getStatus() != HttpRequester.STATUS_OK) {
        logger.error("Error while deleting XMPP relationship: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnDeleting("XMPP relationship",
                chatUser1.getId() + " <-> " + chatUser2.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while creating XMPP relationship: {0}", e.getMessage());
      throw new ChatServerException(SilverpeasExceptionMessages.failureOnAdding("XMPP relationship",
          chatUser1.getId() + " <-> " + chatUser2.getId()), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public boolean isUserExisting(final User user) {
    ChatUser chatUser = ChatUser.fromUser(user);
    Response response = null;
    try {
      HttpRequester requester = request(url, chatUser.getChatLogin());
      response = requester.head();
      return response.getStatus() == HttpRequester.STATUS_OK;

    } catch (Exception e) {
      logger.error("Error while checking XMPP user: ", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnGetting("XMPP user", chatUser.getId()));
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private HttpRequester request(String url, String... path) {
    return theRequester.at(url, path);
  }

}
