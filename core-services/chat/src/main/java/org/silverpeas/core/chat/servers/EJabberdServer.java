/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.chat.servers;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.JSONCodec.JSONObject;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.core.Response;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Adapter to a remote ejabberd service. It implements the mechanisms to access an ejabberd service
 * in order to register users and to create networking relationships.
 * @author mmoquillon
 */
@DefaultChatServer
@Service
public class EJabberdServer implements ChatServer {

  private static final String HOST_ATTR = "host";
  private static final String SERVER_ATTR = "server";
  private static final String LOCAL_SERVER_ATTR = "localserver";
  private static final String PASS_ATTR = "password";
  private static final String USER_ATTR = "user";
  private static final String LOCAL_USER_ATTR = "localuser";
  private static final String NICK_ATTR = "nick";
  private static final String SUBSCRIPTION_ATTR = "subs";
  private static final String NAME_ATTR = "name";
  private static final String CONTENT_ATTR = "content";
  private static final String GROUP_ATTR = "group";

  private SilverLogger logger = SilverLogger.getLogger(this);

  private final String url;

  private final HttpRequester requester;

  /**
   * Constructs a new instance.
   */
  public EJabberdServer() {
    ChatSettings settings = ChatServer.getChatSettings();
    this.url = settings.getRestApiUrl();
    String key = settings.getRestApiAuthToken();
    this.requester = new HttpRequester("Bearer " + key);
  }

  @Override
  public void createUser(final User user) {
    SilverLogger.getLogger(this)
        .debug("Register user {0} ({1})", user.getDisplayedName(), user.getId());
    ChatUser chatUser = ChatUser.fromUser(user);
    request("register", o ->
        o.put(USER_ATTR, chatUser.getChatLogin())
            .put(HOST_ATTR, chatUser.getChatDomain())
            .put(PASS_ATTR, chatUser.getChatPassword()));
    request("set_vcard", o ->
        o.put(USER_ATTR, chatUser.getChatLogin())
            .put(HOST_ATTR, chatUser.getChatDomain())
            .put(NAME_ATTR, "FN")
            .put(CONTENT_ATTR, chatUser.getDisplayedName()));
  }

  @Override
  public void deleteUser(final User user) {
    SilverLogger.getLogger(this)
        .debug("Unregister user {0} ({1})", user.getDisplayedName(), user.getId());
    ChatUser chatUser = ChatUser.fromUser(user);
    request("unregister", o -> o.put(USER_ATTR, chatUser.getChatLogin())
        .put(HOST_ATTR, chatUser.getChatDomain()));
  }

  @Override
  public void createRelationShip(final User user1, final User user2) {
    SilverLogger.getLogger(this)
        .debug("Add relationships between {0} ({1}) and {2} ({3})", user1.getDisplayedName(),
            user1.getId(), user2.getDisplayedName(), user2.getId());
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    final String command = "add_rosteritem";
    request(command, o ->
        o.put(LOCAL_USER_ATTR, chatUser2.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser2.getChatDomain())
            .put(USER_ATTR, chatUser1.getChatLogin())
            .put(SERVER_ATTR, chatUser1.getChatDomain())
            .put(NICK_ATTR, chatUser1.getDisplayedName())
            .put(GROUP_ATTR, "")
            .put(SUBSCRIPTION_ATTR, "to"));
    request(command, o ->
        o.put(LOCAL_USER_ATTR, chatUser1.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser1.getChatDomain())
            .put(USER_ATTR, chatUser2.getChatLogin())
            .put(SERVER_ATTR, chatUser2.getChatDomain())
            .put(NICK_ATTR, chatUser2.getDisplayedName())
            .put(GROUP_ATTR, "")
            .put(SUBSCRIPTION_ATTR, "to"));
    request(command, o ->
        o.put(LOCAL_USER_ATTR, chatUser2.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser2.getChatDomain())
            .put(USER_ATTR, chatUser1.getChatLogin())
            .put(SERVER_ATTR, chatUser1.getChatDomain())
            .put(NICK_ATTR, chatUser1.getDisplayedName())
            .put(GROUP_ATTR, "")
            .put(SUBSCRIPTION_ATTR, "both"));
    request(command, o ->
        o.put(LOCAL_USER_ATTR, chatUser1.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser1.getChatDomain())
            .put(USER_ATTR, chatUser2.getChatLogin())
            .put(SERVER_ATTR, chatUser2.getChatDomain())
            .put(NICK_ATTR, chatUser2.getDisplayedName())
            .put(GROUP_ATTR, "")
            .put(SUBSCRIPTION_ATTR, "both"));
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) {
    SilverLogger.getLogger(this)
        .debug("Delete relationships between {0} ({1}) and {2} ({3})", user1.getDisplayedName(),
            user1.getId(), user2.getDisplayedName(), user2.getId());
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    final String command = "delete_rosteritem";
    request(command,
        o -> o.put(LOCAL_USER_ATTR, chatUser2.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser2.getChatDomain())
            .put(USER_ATTR, chatUser1.getChatLogin())
            .put(SERVER_ATTR, chatUser1.getChatDomain()));
    request(command,
        o -> o.put(LOCAL_USER_ATTR, chatUser1.getChatLogin())
            .put(LOCAL_SERVER_ATTR, chatUser1.getChatDomain())
            .put(USER_ATTR, chatUser2.getChatLogin())
            .put(SERVER_ATTR, chatUser2.getChatDomain()));
  }

  @Override
  public boolean isUserExisting(final User user) {
    ChatUser chatUser = ChatUser.fromUser(user);
    return request("check_account", o ->
            o.put(USER_ATTR, chatUser.getChatLogin()).put(HOST_ATTR, chatUser.getChatDomain()),
        r -> r.readEntity(Integer.class) == 0);
  }

  private void request(String command, UnaryOperator<JSONObject> argsProvider) {
    request(command, argsProvider, r -> null);
  }

  private <T> T request(String command, UnaryOperator<JSONObject> argsProvider,
      Function<Response, T> responseProcessor) {
    Response response = null;
    try {
      response = this.requester.at(url, command).header("X-Admin", "true").post(argsProvider);

      if (response.getStatus() != HttpRequester.STATUS_OK) {
        processError(command, response);
      }
      return responseProcessor.apply(response);
    } catch (Exception e) {
      throw new ChatServerException("Error while performing " + command + ": " + e.getMessage(), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private void processError(final String command, final Response response) {
    logger.error("Failed to {0}: {1}", command, response.getStatusInfo().getReasonPhrase());
    if (response.getStatus() == HttpRequester.STATUS_FORBIDDEN) {
      throw new ChatServerException(
          "Access to " + command + " is forbidden! Please check your authentication token");
    } else if (response.getStatus() == HttpRequester.STATUS_UNAUTHORIZED) {
      throw new ChatServerException(
          "Access to " + command + " is forbidden! Please check your authorization to the " +
              command + " resource");
    } else {
      throw new ChatServerException(response.getStatusInfo().getReasonPhrase() + ": " +
          response.readEntity(String.class));
    }
  }

}
