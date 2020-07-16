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
package org.silverpeas.core.chat.servers;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.ChatServerException;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.core.Response;

/**
 * <p>Openfire server management service</p>
 * <p>It implements the access mechanism to the XMPP server OpenFire.</p>
 * @author remipassmoilesel
 */
@Service
public class OpenfireServer implements ChatServer {

  private static final String XMPP_USER = "XMPP user";
  private static final String XMPP_RELATIONSHIP = "XMPP relationship";
  private static final String BETWEEN = " <-> ";
  private static final String ERROR_USER_ADD_PATTERN = "Error while creating XMPP user: {0}";
  private static final String ERROR_USER_DEL_PATTERN = "Error while deleting XMPP user: {0}";
  private static final String ERROR_RELATIONSHIP_ADD_PATTERN =
      "Error while creating XMPP relationship: {0}";
  private static final String ERROR_RELATIONSHIP_DEL_PATTERN =
      "Error while deleting XMPP relationship: {0}";
  
  private SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * URL target of the REST requests.
   */
  private final String url;

  private final HttpRequester theRequester;

  public OpenfireServer() {
    ChatSettings settings = ChatServer.getChatSettings();
    this.url = settings.getRestApiUrl() + "/users";
    String key = settings.getRestApiAuthToken();
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
        logger.error(ERROR_USER_ADD_PATTERN,
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnAdding(XMPP_USER, chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error(ERROR_USER_ADD_PATTERN, e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnAdding(XMPP_USER, chatUser.getId()), e);
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
        logger.error(ERROR_USER_DEL_PATTERN,
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnDeleting(XMPP_USER, chatUser.getId()));
      }
    } catch (Exception e) {
      logger.error(ERROR_USER_DEL_PATTERN, e.getMessage());
      throw new ChatServerException(
          SilverpeasExceptionMessages.failureOnDeleting(XMPP_USER, chatUser.getId()), e);
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
        logger.error(ERROR_RELATIONSHIP_ADD_PATTERN,
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnAdding(XMPP_RELATIONSHIP,
                chatUser1.getId() + BETWEEN + chatUser2.getId()));
      }
    } catch (Exception e) {
      logger.error(ERROR_RELATIONSHIP_ADD_PATTERN, e.getMessage());
      throw new ChatServerException(SilverpeasExceptionMessages.failureOnAdding(XMPP_RELATIONSHIP,
          chatUser1.getId() + BETWEEN + chatUser2.getId()), e);
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
        logger.error(ERROR_RELATIONSHIP_DEL_PATTERN,
            response.getStatusInfo().getReasonPhrase());
        throw new ChatServerException(
            SilverpeasExceptionMessages.failureOnDeleting(XMPP_RELATIONSHIP,
                chatUser1.getId() + BETWEEN + chatUser2.getId()));
      }
    } catch (Exception e) {
      logger.error(ERROR_RELATIONSHIP_ADD_PATTERN, e.getMessage());
      throw new ChatServerException(SilverpeasExceptionMessages.failureOnAdding(XMPP_RELATIONSHIP,
          chatUser1.getId() + BETWEEN + chatUser2.getId()), e);
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
          SilverpeasExceptionMessages.failureOnGetting(XMPP_USER, chatUser.getId()));
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
