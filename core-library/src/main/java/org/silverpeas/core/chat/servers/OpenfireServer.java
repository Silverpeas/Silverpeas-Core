package org.silverpeas.core.chat.servers;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Function;

/**
 * <p>Openfire server management service</p>
 * <p>It implements the access mechanism to the XMPP server OpenFire.</p>
 * @author remipassmoilesel
 */
public class OpenfireServer implements ChatServer {

  private static final String USER_AGENT = "Silverpeas chat client";

  private SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * URL target of the REST requests.
   */
  private final String url;

  /**
   * Authentication key
   */
  private final String key;

  public OpenfireServer() {
    SettingBundle settings = ChatServer.getChatSettings();
    this.url = settings.getString("chat.xmpp.restUrl") + "/users";
    this.key = settings.getString("chat.xmpp.restKey");
  }

  @Override
  public void createUser(final User user) throws ChatServerException {
    // get cross domain login and password
    ChatUser chatUser = ChatUser.fromUser(user);
    try (HttpRequester requester = request(url)) {
      final Response response = requester.post(o -> {
        o.put("username", chatUser.getChatLogin())
            .put("password", chatUser.getChatPassword())
            .put("name", chatUser.getDisplayedName());
        if (StringUtil.isDefined(chatUser.geteMail())) {
          o.put("email", chatUser.geteMail());
        }
        return o;
      });

      if (response.getStatus() != 201) {
        logger.error("Error while creating XMPP user: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnAdding("XMPP user", chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while creating XMPP user: {0}", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnAdding("XMPP user", chatUser.getId()), e);
    }
  }

  @Override
  public void deleteUser(final User user) throws ChatServerException {
    ChatUser chatUser = ChatUser.fromUser(user);
    try (HttpRequester requester = request(url, chatUser.getChatLogin())) {
      final Response response = requester.delete();

      if (response.getStatus() != 200) {
        logger.error("Error while deleting XMPP user: {0}",
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnDeleting("XMPP user", chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error("Error while deleting XMPP user: {0}", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnDeleting("XMPP user", chatUser.getId()), e);
    }
  }

  @Override
  public void createRelationShip(final User user1, final User user2) throws ChatServerException {
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    try (HttpRequester requester = request(url, chatUser1.getChatLogin(), "roster")) {
      final Response response =
          requester.post(o -> o.put("jid", chatUser2.getChatId()).put("subscriptionType", "3"));

      if (response.getStatus() == 409) {
        logger.warn("The XMPP relationship between users {0} and {1} already exists!",
            chatUser1.getId(), chatUser2.getId());
      } else if (response.getStatus() != 201) {
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
    }
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) throws ChatServerException {
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);

    try (HttpRequester requester = request(chatUser1.getChatLogin(), "roster",
        chatUser2.getChatId())) {
      final Response response = requester.delete();

      if (response.getStatus() != 200) {
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
    }
  }

  @Override
  public boolean isUserExisting(final User user) {
    ChatUser chatUser = ChatUser.fromUser(user);
    try (HttpRequester requester = request(url, chatUser.getChatLogin())) {
      final Response response = requester.head();
      return response.getStatus() == 200;

    } catch (Exception e) {
      logger.error("Error while checking XMPP user: ", e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnGetting("XMPP user", chatUser.getId()));
    }
  }

  private HttpRequester request(String url, String... path) {
    return new HttpRequester(url, path);
  }

  private class HttpRequester implements AutoCloseable {

    private Client client = ClientBuilder.newClient();
    private Invocation.Builder builder;

    public HttpRequester(String url, String... path) {
      WebTarget target = client.target(url);
      for (String aPath : path) {
        target = target.path(aPath);
      }
      builder = target.request(MediaType.APPLICATION_JSON_TYPE)
          .acceptEncoding("UTF-8")
          .header("User-Agent", USER_AGENT)
          .header("Authorization", key);
    }

    @Override
    public void close() throws Exception {
      client.close();
    }

    public Response post(Function<JSONCodec.JSONObject, JSONCodec.JSONObject> entityBuilder) {
      return builder.post(
          Entity.entity(JSONCodec.encodeObject(entityBuilder), MediaType.APPLICATION_JSON_TYPE));
    }

    public Response delete() {
      return builder.delete();
    }

    public Response head() {
      return builder.head();
    }
  }

}
