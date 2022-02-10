/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.client.constant;

import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Channel used to notify users.
 * @author Yohann Chastagnier
 */
public enum NotifChannel {
  /**
   * The notification is sent by email.
   */
  SMTP(1, "BASIC_SMTP_MAIL", BuiltInNotifAddress.BASIC_SMTP),

  /**
   * The notification is sent by SMS. In that case, the address should be a phone number and
   * the remote server a SMS service of a telecommunication provider.
   */
  SMS(2, "BASIC_SMS", BuiltInNotifAddress.BASIC_SERVER),

  /**
   * The notification is sent to the recipient's web browser to be rendered within a popup.
   */
  POPUP(3, "BASIC_POPUP", BuiltInNotifAddress.BASIC_POPUP),

  /**
   * The notification is stored into Silverpeas to be rendered within the user's notifications box
   * in Silverpeas.
   */
  SILVERMAIL(4, "BASIC_SILVERMAIL", BuiltInNotifAddress.BASIC_SILVERMAIL),

  /**
   * The notification is sent to nowhere (it's lost).
   */
  REMOVE(5, "BASIC_REMOVE", BuiltInNotifAddress.BASIC_REMOVE),

  /**
   * The notification is sent to a remote server, whatever it is.
   */
  SERVER(6, "BASIC_SERVER", BuiltInNotifAddress.BASIC_SERVER);

  private final int id;
  private final String name;
  private final BuiltInNotifAddress type;

  NotifChannel(final int id, final String name, final BuiltInNotifAddress mediaType) {
    this.id = id;
    this.name = name;
    this.type = mediaType;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BuiltInNotifAddress getMediaType() {
    return type;
  }

  /**
   * Decodes the specified channel identifier to a well defined {@link NotifChannel} instance.
   * If the identifier doesn't match to an existing {@link NotifChannel} instance, then nothing
   * is returned.
   * @param id the unique identifier of a channel.
   * @return an optional {@link NotifChannel} instance corresponding to the given identifier. If
   * no such channel exists for the specified identifier, then nothing is returned.
   */
  public static Optional<NotifChannel> decode(final Integer id) {
    final Optional<NotifChannel> channel;
    if (id != null) {
      channel = Arrays.stream(NotifChannel.values()).filter(n -> n.id == id).findFirst();
    } else {
      channel = Optional.empty();
    }
    return channel;
  }

  /**
   * Decodes the specified channel name to the corresponding {@link NotifChannel} instance.
   * @param name the name of a channel.
   * @return an optional {@link NotifChannel} instance. If no such channel exists for the given
   * name, then nothing is returned.
   */
  public static Optional<NotifChannel> decode(final String name) {
    final Optional<NotifChannel> channel;
    if (StringUtil.isDefined(name)) {
      channel = Arrays.stream(NotifChannel.values()).filter(n -> n.name.equals(name)).findFirst();
    } else {
      channel = Optional.empty();
    }
    return channel;
  }

  public static Collection<Integer> toIds(final Collection<NotifChannel> notifChannels) {
    final Collection<Integer> result = new ArrayList<>();
    if (notifChannels != null) {
      for (final NotifChannel notifChannel : notifChannels) {
        result.add(notifChannel.getId());
      }
    }
    return result;
  }
}
