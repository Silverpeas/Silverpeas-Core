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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;
import java.util.Optional;

/**
 * All the parameters required to send a notification with the {@link NotificationManager}
 * @author Thierry Leroi
 * @version %I%, %G%
 */
public class NotificationParameters {
  /**
   * The priority of the message is normal.
   */
  public static final int PRIORITY_NORMAL = 0;
  /**
   * The message is urgent.
   */
  public static final int PRIORITY_URGENT = 1;
  /**
   * The message is for an error.
   */
  public static final int PRIORITY_ERROR = 2;

  static final String USAGE_PRO = "addressUsePro";
  static final String USAGE_PERSO = "addressUsePerso";
  static final String USAGE_REP = "addressUseRep";
  static final String USAGE_URGENT = "addressUseUrgent";

  public static final int MAX_SIZE_TITLE = 1023; // Maximum size of the title in
  // tables SILVERMAIL and POPUP

  private int iMessagePriority = PRIORITY_NORMAL;
  private int addressId = BuiltInNotifAddress.COMPONENT_DEFINED.getId();
  private int iComponentInstance = -1;
  private int iFromUserId = -1;

  private String sTitle = "";
  private String senderName = "";
  private String sMessage = "";
  private Link link = Link.EMPTY_LINK;
  private String sSource = "";
  private String sSessionId = "";
  private String sOriginalExtraMessage = null;
  private boolean bAnswerAllowed = false;
  private boolean bSendImmediately = false;

  private Date dDate = new Date();
  private String sLanguage = null;

  private NotifAction eAction = null;
  private NotificationResourceData nNotificationResourceData = null;

  public int getMessagePriority() {
    return iMessagePriority;
  }

  public NotificationParameters setMessagePriority(final int iMessagePriority) {
    this.iMessagePriority = iMessagePriority;
    return this;
  }

  public int getAddressId() {
    return addressId;
  }

  public NotificationParameters setAddressId(final int notifMediaType) {
    this.addressId = notifMediaType;
    return this;
  }

  public boolean isAddressDefinedByComponent() {
    return getAddressId() == BuiltInNotifAddress.COMPONENT_DEFINED.getId();
  }

  public int getComponentInstance() {
    return iComponentInstance;
  }

  public NotificationParameters setComponentInstance(final int iComponentInstance) {
    this.iComponentInstance = iComponentInstance < 0 ? -1 : iComponentInstance;
    return this;
  }

  public boolean isComponentInstanceDefined() {
    return this.iComponentInstance != -1;
  }

  public int getFromUserId() {
    return iFromUserId;
  }

  public NotificationParameters setFromUserId(final int iFromUserId) {
    this.iFromUserId = iFromUserId < 0 ? -1 : iFromUserId;
    return this;
  }

  public boolean isFromUserIdDefined() {
    return iFromUserId != -1;
  }

  public String getTitle() {
    return sTitle;
  }

  public NotificationParameters setTitle(final String sTitle) {
    this.sTitle = sTitle == null ? "" : sTitle;
    return this;
  }

  public String getSenderName() {
    return senderName;
  }

  public NotificationParameters setSenderName(final String senderName) {
    this.senderName = senderName;
    return this;
  }

  public String getMessage() {
    return sMessage;
  }

  public NotificationParameters setMessage(final String sMessage) {
    this.sMessage = sMessage == null ? "" : sMessage;
    return this;
  }

  public Link getLink() {
    return link;
  }

  public NotificationParameters setLink(final Link link) {
    this.link = link;
    return this;
  }

  public String getSource() {
    return sSource;
  }

  public NotificationParameters setSource(final String sSource) {
    this.sSource = sSource;
    return this;
  }

  public String getSessionId() {
    return sSessionId;
  }

  public NotificationParameters setSessionId(final String sSessionId) {
    this.sSessionId = sSessionId;
    return this;
  }

  public String getOriginalExtraMessage() {
    return sOriginalExtraMessage;
  }

  public NotificationParameters setOriginalExtraMessage(final String sOriginalExtraMessage) {
    this.sOriginalExtraMessage = sOriginalExtraMessage;
    return this;
  }

  public boolean isAnswerAllowed() {
    return bAnswerAllowed;
  }

  public NotificationParameters setAnswerAllowed(final boolean bAnswerAllowed) {
    this.bAnswerAllowed = bAnswerAllowed;
    return this;
  }

  public boolean isSendImmediately() {
    return bSendImmediately;
  }

  public NotificationParameters setSendImmediately(final boolean bSendImmediately) {
    this.bSendImmediately = bSendImmediately;
    return this;
  }

  public Date getDate() {
    return dDate;
  }

  public NotificationParameters setDate(final Date dDate) {
    this.dDate = dDate;
    return this;
  }

  public String getLanguage() {
    return sLanguage;
  }

  public NotificationParameters setLanguage(final String sLanguage) {
    this.sLanguage = sLanguage;
    return this;
  }

  public NotifAction getAction() {
    return eAction;
  }

  public NotificationParameters setAction(final NotifAction eAction) {
    this.eAction = eAction;
    return this;
  }

  public NotificationResourceData getNotificationResourceData() {
    return nNotificationResourceData;
  }

  public NotificationParameters setNotificationResourceData(
      final NotificationResourceData nNotificationResourceData) {
    this.nNotificationResourceData = nNotificationResourceData;
    return this;
  }

  boolean isTitleExceedsMaxSize() {
    return getTitle().length() >= MAX_SIZE_TITLE;
  }

  void trace() {
    StringBuilder trace = new StringBuilder("Notification Parameters Dump: {");
    if (iMessagePriority == PRIORITY_NORMAL) {
      trace.append("MessagePriority: NORMAL, ");
    } else if (iMessagePriority == PRIORITY_URGENT) {
      trace.append("MessagePriority: URGENT, ");
    } else if (iMessagePriority == PRIORITY_ERROR) {
      trace.append("MessagePriority: ERROR, ");
    }
    Optional<BuiltInNotifAddress> mediaType = BuiltInNotifAddress.decode(addressId);
    trace.append("MediaType: ");
    if (mediaType.isPresent()) {
      trace.append(mediaType.get().name()).append(", ");
    } else {
      trace.append(addressId).append(", ");
    }
    trace.append("ComponentInstance: ").append(iComponentInstance);
    trace.append(", Title: ").append(sTitle);
    trace.append(", Message: ").append(sMessage);
    trace.append(", FromUserId: ").append(iFromUserId);
    trace.append(", FromSenderName: ").append(senderName);
    trace.append(", AnswerAllowed: ").append(bAnswerAllowed);
    trace.append(", SendImmediately: ").append(bSendImmediately);
    trace.append(", Source: ").append(sSource);
    trace.append(", SessionId: ").append(sSessionId);
    trace.append(", Date: ").append(dDate.toString());
    trace.append(", Action: ").append((eAction != null ? eAction.name() : "N/A"));
    trace.append("}");
    SilverLogger.getLogger(this).debug(trace.toString());
  }
}