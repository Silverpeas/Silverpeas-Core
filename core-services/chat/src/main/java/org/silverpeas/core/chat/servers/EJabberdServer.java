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
package org.silverpeas.core.chat.servers;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.core.Response;
import java.util.function.Function;

/**
 * Adapter to a remote ejabberd service. It implements the mechanisms to access an ejabberd service
 * in order to register users and to create networking relationships.
 * @author mmoquillon
 */
@DefaultChatServer
public class EJabberdServer implements ChatServer {

  private SilverLogger logger = SilverLogger.getLogger(this);

  private final String url;

  private final HttpRequester requester;

  /**
   * Constructs a new instance.
   */
  public EJabberdServer() {
    SettingBundle settings = ChatServer.getChatSettings();
    this.url = settings.getString("chat.xmpp.restUrl");
    String key = settings.getString("chat.xmpp.restKey");
    this.requester = new HttpRequester("Bearer " + key);
  }

  @Override
  public void createUser(final User user) throws ChatServerException {
    SilverLogger.getLogger(this)
        .info("Register user {0} ({1})", user.getDisplayedName(), user.getId());
    ChatUser chatUser = ChatUser.fromUser(user);
    request("register",
        o -> o.put("user", chatUser.getChatLogin())
        .put("host", chatUser.getChatDomain())
        .put("password", chatUser.getChatPassword()));
    request("set_vcard",
        o -> o.put("user", chatUser.getChatLogin())
        .put("host", chatUser.getChatDomain())
        .put("name", "FN")
        .put("content", chatUser.getDisplayedName()));
  }

  @Override
  public void deleteUser(final User user) throws ChatServerException {
    SilverLogger.getLogger(this)
        .info("Unregister user {0} ({1})", user.getDisplayedName(), user.getId());
    ChatUser chatUser = ChatUser.fromUser(user);
    request("unregister",
        o -> o.put("username", chatUser.getChatLogin())
            .put("hostname", chatUser.getChatDomain()));
  }

  @Override
  public void createRelationShip(final User user1, final User user2) throws ChatServerException {
    SilverLogger.getLogger(this)
        .info("Add relationships between {0} ({1}) and {2} ({3})", user1.getDisplayedName(),
            user1.getId(), user2.getDisplayedName(), user2.getId());
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    request("add_rosteritem",
        o -> o.put("localuser", chatUser2.getChatLogin())
            .put("localserver", chatUser2.getChatDomain())
            .put("user", chatUser1.getChatLogin())
            .put("server", chatUser1.getChatDomain())
            .put("nick", chatUser1.getDisplayedName())
            .put("subs", "both"));
    request("add_rosteritem",
        o -> o.put("localuser", chatUser1.getChatLogin())
        .put("localserver", chatUser1.getChatDomain())
        .put("user", chatUser2.getChatLogin())
        .put("server", chatUser2.getChatDomain())
        .put("nick", chatUser2.getDisplayedName())
        .put("subs", "both"));
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) throws ChatServerException {
    SilverLogger.getLogger(this)
        .info("Delete relationships between {0} ({1}) and {2} ({3})", user1.getDisplayedName(),
            user1.getId(), user2.getDisplayedName(), user2.getId());
    ChatUser chatUser1 = ChatUser.fromUser(user1);
    ChatUser chatUser2 = ChatUser.fromUser(user2);
    request("delete_rosteritem",
        o -> o.put("localuser", chatUser2.getChatLogin())
            .put("localserver", chatUser2.getChatDomain())
            .put("user", chatUser1.getChatLogin())
            .put("server", chatUser1.getChatDomain()));
    request("delete_rosteritem",
        o -> o.put("localuser", chatUser1.getChatLogin())
        .put("localserver", chatUser1.getChatDomain())
        .put("user", chatUser2.getChatLogin())
        .put("server", chatUser2.getChatDomain()));
  }

  @Override
  public boolean isUserExisting(final User user) throws ChatServerException {
    ChatUser chatUser = ChatUser.fromUser(user);
    return request("check_account",
        o -> o.put("user", chatUser.getChatLogin())
            .put("host", chatUser.getChatDomain()),
        r -> r.readEntity(Integer.class) == 0);
  }

  private void request(String command,
      Function<JSONCodec.JSONObject, JSONCodec.JSONObject> argsProvider) {
    request(command, argsProvider, r -> null);
  }

  private <T> T request(String command,
      Function<JSONCodec.JSONObject, JSONCodec.JSONObject> argsProvider,
      Function<Response, T> responseProcessor) {
    Response response = null;
    try {
      response = this.requester.at(url, command).header("X-Admin", "true").post(argsProvider);

      if (response.getStatus() != 200) {
        logger.error("Failed to {0}: {1}", command, response.getStatusInfo().getReasonPhrase());
        if (response.getStatus() == 403) {
          throw new ChatServerException(
              "Access to " + command + " is forbidden! Please check your authentication token");
        } else if (response.getStatus() == 401) {
          throw new ChatServerException(
              "Access to " + command + " is forbidden! Please check your authorization to the " +
                  command + " resource");
        } else {
          throw new ChatServerException(response.getStatusInfo().getReasonPhrase() + ": " +
              response.readEntity(String.class));
        }
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

}
