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
package org.silverpeas.core.chat;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.util.Charsets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.silverpeas.kernel.util.StringUtil.normalizeByRemovingAccent;

/**
 * A Silverpeas user with an account in a Chat service. The Silverpeas Chat Engine uses the services
 * of an external chat server; so each user in Silverpeas has also an account in the chat server and
 * hence has a unique identifier in that chat service.
 * @author remipassmoilesel
 */
public class ChatUser extends UserDetail {
  private static final long serialVersionUID = 3020578029905358343L;

  private final User user;

  private ChatUser(final User user) {
    this.user = user;
  }

  /**
   * Gets a chat user from the specified Silverpeas unique identifier.
   * @param userId the unique identifier of a user in Silverpeas.
   * @return a user having an account in Silverpeas.
   */
  public static ChatUser getById(final String userId) {
    return new ChatUser(User.getById(userId));
  }

  /**
   * Gets the user behind the current request to Silverpeas and that have an account in the chat
   * service.
   * @return the current requester as a {@link ChatUser}.
   */
  public static ChatUser getCurrentRequester() {
    return new ChatUser(User.getCurrentRequester());
  }

  /**
   * Creates a new {@link ChatUser} from the specified Silverpeas user. It will convert the
   * specified user to a chat user.
   * @param user a Silverpeas user.
   * @return a Chat service user.
   */
  public static ChatUser fromUser(User user) {
    if (user instanceof ChatUser) {
      return (ChatUser) user;
    }
    return new ChatUser(user);
  }

  /**
   * Gets the unique identifier of this user in the chat service. It is made up of the user login
   * plus the domain of the chat service; they are concatenated with @ as separator.
   * @return the unique identifier of the user the the chat service.
   */
  public String getChatId() {
    String xmppDomain = getChatDomain();
    return getChatLogin() + "@" + xmppDomain;
  }

  /**
   * Gets the chat service login of this user. The login format is defined by the RFC-7622 in which
   * it represents the <code>localpart</code> field conforming the <code>UsernameCaseMapped</code>
   * profile of the PRECIS <code>IdentifierClass</code> defined in RFC-7613.
   * @return the login of the user in the chat service.
   */
  public String getChatLogin() {
    // take into account some aspects of the RFC-7622 defining the JID format
    String chatLogin = normalizeByRemovingAccent(getLogin().toLowerCase());
    int idx = chatLogin.indexOf("@");
    if (idx > 0) {
      ChatSettings.JidFormatPolicy policy = ChatSettings.get().getJidFormatPolicy();
      switch (policy) {
        case REMOVED:
          chatLogin = chatLogin.substring(0, idx);
          break;
        case SPECIFIC_CODE:
          chatLogin = chatLogin.replace("@", "0x40").replace("/", "0x2f");
          break;
        default:
          throw new SilverpeasRuntimeException("Invalid policy");
      }
      byte[] bytes = chatLogin.getBytes(Charsets.UTF_8);
      if (bytes.length > 1023) {
        chatLogin = new String(Arrays.copyOf(bytes, 1022), Charsets.UTF_8);
      }
    }
    return chatLogin.replaceAll("\\s", "");
  }

  /**
   * Gets the chat service password of this user.
   * @return the password associated with its login in the chat service.
   */
  public String getChatPassword() {
    return getToken();
  }

  /**
   * Gets the chat domain to which this user belongs.
   * The chat domain is read from the property file
   * {@code org/silverpeas/chat/settings/chat.properties} and it depends on the Silverpeas domain of
   * the user. If no chat domain is mapped to the Silverpeas domain of the user, then the default
   * one (the one set by the {@code chat.xmpp.domain.default} property) is returned.
   * @return the name of the chat domain of the user or an empty string if no chat domain is mapped
   * to the Silverpeas domain of the user.
   */
  public String getChatDomain() {
    ChatSettings settings = ChatServer.getChatSettings();
    return settings.getMappedXmppDomain(getDomainId());
  }

  /**
   * Is the chat service is enabled for this user? It is enabled if both the chat service is enabled
   * in Silverpeas, the user domain he belongs to is mapped to a chat domain in the chat
   * service and he belongs to a group of users in Silverpeas that is allowed to access the chat
   * service.
   * @return true if the chat is enabled for the user. False otherwise.
   */
  public boolean isChatEnabled() {
    ChatSettings settings = ChatServer.getChatSettings();
    String domainId = getChatDomain();
    if (!settings.isChatEnabled() || domainId.isEmpty()) {
      return false;
    }
    List<String> allowedGroupIds = settings.getAllowedUserGroups();
    if (!allowedGroupIds.isEmpty()) {
      String[] groupIds = OrganizationController.get().getAllGroupIdsOfUser(user.getId());
      return Stream.of(groupIds).anyMatch(allowedGroupIds::contains);
    }
    return true;
  }

  /**
   * Has the user already an account in the chat server?
   * @return true if {@link #isChatEnabled()} returns true and if the user has an account in the
   * chat server, false otherwise or if {@link ChatServerException} has been detected.
   */
  public boolean isRegistered() {
    try {
      return isChatEnabled() && ChatServer.get().isUserExisting(this);
    } catch (final ChatServerException e) {
      return false;
    }
  }

  /**
   * Is the user at least in one of the following groups of users in Silverpeas? This method is
   * mainly used to check the ACL on the chat client functionalities when bootstrapping later.
   * @param groupIds a list of unique identifiers of group of users in Silverpeas.
   * @return true if the user is at least in one of the specified groups.
   */
  public boolean isAtLeastInOneGroup(final List<String> groupIds) {
    String[] actualGroupIds = getOrganisationController().getAllGroupIdsOfUser(getId());
    return Stream.of(actualGroupIds).anyMatch(groupIds::contains);
  }

  @Override
  public String getId() {
    return user.getId();
  }

  @Override
  public String getDomainId() {
    return user.getDomainId();
  }

  @Override
  public boolean isDomainRestricted() {
    return user.isDomainRestricted();
  }

  @Override
  public boolean isDomainAdminRestricted() {
    return user.isDomainAdminRestricted();
  }

  @Override
  public String getLogin() {
    return user.getLogin();
  }

  @Override
  public String getLastName() {
    return user.getLastName();
  }

  @Override
  public String getFirstName() {
    return user.getFirstName();
  }

  @Override
  public String getEmailAddress() {
    return user.getEmailAddress();
  }

  @Override
  public Date getCreationDate() {
    return user.getCreationDate();
  }

  @Override
  public Date getSaveDate() {
    return user.getSaveDate();
  }

  @Override
  public int getVersion() {
    return user.getVersion();
  }

  @Override
  public Date getStateSaveDate() {
    return user.getStateSaveDate();
  }

  @Override
  public boolean isFullyDefined() {
    return user.isFullyDefined();
  }

  @Override
  public String getDisplayedName() {
    return user.getDisplayedName();
  }

  @Override
  public UserAccessLevel getAccessLevel() {
    return user.getAccessLevel();
  }

  @Override
  public boolean isAnonymous() {
    return user.isAnonymous();
  }

  @Override
  public boolean isSystem() {
    return user.isSystem();
  }

  @Override
  public boolean isBlanked() {
    return user.isBlanked();
  }

  @Override
  public boolean isAccessAdmin() {
    return user.isAccessAdmin();
  }

  @Override
  public boolean isAccessDomainManager() {
    return user.isAccessDomainManager();
  }

  @Override
  public boolean isAccessPdcManager() {
    return user.isAccessPdcManager();
  }

  @Override
  public boolean isAccessUser() {
    return user.isAccessUser();
  }

  @Override
  public boolean isAccessGuest() {
    return user.isAccessGuest();
  }

  @Override
  public boolean isAccessUnknown() {
    return user.isAccessUnknown();
  }

  @Override
  public boolean isPlayingAdminRole(final String instanceId) {
    return user.isPlayingAdminRole(instanceId);
  }

  @Override
  public UserState getState() {
    return user.getState();
  }

  @Override
  public boolean isActivatedState() {
    return user.isActivatedState();
  }

  @Override
  public boolean isValidState() {
    return user.isValidState();
  }

  @Override
  public boolean isDeletedState() {
    return user.isDeletedState();
  }

  @Override
  public boolean isRemovedState() {
    return user.isRemovedState();
  }

  @Override
  public boolean isBlockedState() {
    return user.isBlockedState();
  }

  @Override
  public boolean isDeactivatedState() {
    return user.isDeactivatedState();
  }

  @Override
  public boolean isExpiredState() {
    return user.isExpiredState();
  }

  @Override
  public boolean isConnected() {
    return user.isConnected();
  }

  @Override
  public UserPreferences getUserPreferences() {
    return user.getUserPreferences();
  }

  @Override
  public String getAvatar() {
    return  user.getAvatar();
  }

  @Override
  public String getSmallAvatar() {
    return user.getSmallAvatar();
  }

  @Override
  public String getStatus() {
    return user.getStatus();
  }

  @Override
  public String getDurationOfCurrentSession() {
    return user.getDurationOfCurrentSession();
  }

  @Override
  public boolean equals(final Object other) {
    return super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
