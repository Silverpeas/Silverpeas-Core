package org.silverpeas.core.chat;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundleList;

import java.util.Date;

/**
 * A Silverpeas user with an account in a Chat service. The Silverpeas Chat Engine uses the services
 * of an external chat server; so each user in Silverpeas has also an account in the chat server and
 * hence has a unique identifier in that chat service.
 * @author remipassmoilesel
 */
public class ChatUser extends UserDetail {

  private final User user;

  /**
   * Gets a chat user from the specified Silverpeas unique identifier.
   * @param userId the unique identifier of a user in Silverpeas.
   * @return a user having an account in Silverpeas.
   */
  public static ChatUser getById(final String userId) {
    return new ChatUser(User.getById(userId));
  }

  /**
   * Gets the user behing the current request to Silverpeas and that have an account in the chat
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
    return new ChatUser(user);
  }

  private ChatUser(final User user) {
    this.user = user;
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
   * Gets the chat service login of this user.
   * @return the login of the user in the chat service.
   */
  public String getChatLogin() {
    return getLogin().replaceAll("@\\w+\\.\\w+$", "").toLowerCase();
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
   *
   * The chat domain is read from the property file
   * {@code org/silverpeas/chat/settings/chat.properties} and it depends on the Silverpeas domain of
   * the user. If no chat domain is mapped to the Silverpeas domain of the user, then the default
   * one (the one mapped with the Silverpeas domain with the unique identifier 0) is returned.
   * @return the chat domain of the user.
   */
  public String getChatDomain() {
    SettingBundle settings = ChatServer.getChatSettings();
    String domain = settings.getString("chat.xmpp.domain." + getDomainId(), "");
    if (domain.isEmpty()) {
      domain = settings.getString("chat.xmpp.domain.0");
    }
    return domain;
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
  public String geteMail() {
    return user.geteMail();
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
  public boolean isAccessAdmin() {
    return user.isAccessAdmin();
  }

  @Override
  public boolean isAccessDomainManager() {
    return user.isAccessDomainManager();
  }

  @Override
  public boolean isAccessSpaceManager() {
    return user.isAccessSpaceManager();
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
}
