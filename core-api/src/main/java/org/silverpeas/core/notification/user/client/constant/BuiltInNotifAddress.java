/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import java.util.Arrays;
import java.util.Optional;

/**
 * Definition of the builtin addresses that are used in the notification to the users. Each of them
 * are related to a given notification channel (see {@link NotifChannel}). That means that for each
 * {@link NotifChannel} there is one and a unique one notification address definition. Hence a
 * notification address is also always related to the channel through which the notifications are
 * sent.
 * <p>
 * Among the builtin addresses there is also some predefined addresses that are not true
 * notification addresses but aliases to
 * a true address that requires to be computed according to the concerned user:
 * <ul>
 *   <li>{@link BuiltInNotifAddress#DEFAULT} is an alias to the first address set as default by
 *   the user,</li>
 *   <li>{@link BuiltInNotifAddress#COMPONENT_DEFINED} is an alias to the address that is defined
 *   by the user for all notifications sent through a given component instance</li>.
 * </ul>
 * The two above aliases can designate not only a builtin address but also a custom address created
 * by the user himself.
 * </p>
 * @author Yohann Chastagnier
 */
public enum BuiltInNotifAddress {

  /**
   * Refers the first notification address set as default in the notification preferences of the
   * recipient user.
   */
  DEFAULT(-1),

  /**
   * Refers the notification address that was specifically set by the reciepient user for
   * notifications sent within a given component instance.
   */
  COMPONENT_DEFINED(-2),

  /**
   * Builtin address related to the notification channel {@link NotifChannel#POPUP}.
   */
  BASIC_POPUP(-10),

  /**
   * Builtin address related to the notification channel {@link NotifChannel#REMOVE}.
   */
  BASIC_REMOVE(-11),

  /**
   * Builtin address related to the notification channel {@link NotifChannel#SILVERMAIL}.
   */
  BASIC_SILVERMAIL(-12),

  /**
   * Builtin address related to the notification channel {@link NotifChannel#SMTP}. For such a
   * channel, the builtin address is the email address of the recipient user as set in his
   * profile (beware, his profile isn't his notification preferences).
   */
  BASIC_SMTP(-13),

  /**
   * Builtin address related to the notification channel {@link NotifChannel#SERVER}. This channel
   * refers any remote server to which a notification is sent, whatever it is.
   */
  BASIC_SERVER(-14);

  private final int id;

  BuiltInNotifAddress(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  /**
   * Decodes the specified media type identifier to a {@link BuiltInNotifAddress} instance.
   * @param id an identifier of a media type.
   * @return an optional {@link BuiltInNotifAddress} instance corresponding to the specified
   * identifier.
   */
  public static Optional<BuiltInNotifAddress> decode(final Integer id) {
    final Optional<BuiltInNotifAddress> mediaType;
    if (id != null) {
      mediaType = Arrays.stream(BuiltInNotifAddress.values()).filter(n -> n.id == id).findFirst();
    } else {
      mediaType = Optional.empty();
    }
    return mediaType;
  }

}
