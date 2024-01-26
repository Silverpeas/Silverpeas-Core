package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.kernel.util.StringUtil;

/**
 * A notification address is an address to which a user can receive the notification messages
 * that are addressed to him. Such address is always related to a given notification channel
 * (see {@link org.silverpeas.core.notification.user.client.constant.NotifChannel}) and
 * it is defined by some properties: name, channel identifier, user identifier, user address itself,
 * and usage. The notification address is mainly used to represent a custom address of a given user;
 * therefore it doesn't concern the built-in addresses
 * (see {@link BuiltInNotifAddress}
 * @author mmoquillon
 */
public class NotificationAddress {

  private String id;
  private String name;
  private String channelId;
  private String userId;
  private String usage = NotificationParameters.USAGE_PRO;
  private String address;

  public String getId() {
    return id;
  }

  public NotificationAddress setId(final String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public NotificationAddress setName(final String name) {
    this.name = name;
    return this;
  }

  public String getChannelId() {
    return channelId;
  }

  public NotificationAddress setChannelId(final String channelId) {
    this.channelId = channelId;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public NotificationAddress setUserId(final String userId) {
    this.userId = userId;
    return this;
  }

  public String getUsage() {
    return usage;
  }

  public NotificationAddress setUsage(final String usage) {
    this.usage = StringUtil.isDefined(usage) ? usage : NotificationParameters.USAGE_PRO;
    return this;
  }

  public String getAddress() {
    return address;
  }

  public NotificationAddress setAddress(final String address) {
    this.address = address;
    return this;
  }

  int getRawId() {
    return Integer.parseInt(id);
  }

  int getRawChannelId() {
    return Integer.parseInt(channelId);
  }
}
  