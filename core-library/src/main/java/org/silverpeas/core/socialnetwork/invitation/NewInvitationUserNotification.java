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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

/**
 * Created by Nicolas on 03/02/2017.
 */
public class NewInvitationUserNotification extends AbstractInvitationUserNotification {

  public NewInvitationUserNotification(Invitation invitation) {
    super(invitation);
  }

  @Override
  protected void performTemplateData(final String language, final Invitation resource,
      final SilverpeasTemplate template) {

    UserFull sender = UserFull.getById(String.valueOf(resource.getSenderId()));
    template.setAttribute("senderUser", sender);
    template.setAttribute("userName", sender.getDisplayedName());
    template.setAttribute("senderMessage", resource.getMessage());
  }

  @Override
  protected void performNotificationResource(final String language, final Invitation resource,
      final NotificationResourceData notificationResourceData) {
    String url = URLUtil.getURL(URLUtil.CMP_MYPROFILE, null, null)
        + "MyInvitations";
    getNotificationMetaData().setLink(url);
    getNotificationMetaData().setOriginalExtraMessage(resource.getMessage());
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "myProfile.invitations.notification.notifLinkLabel";
  }

}