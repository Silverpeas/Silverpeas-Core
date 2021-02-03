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
package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Collection;

/**
 * @author Nicolas Eysseric.
 */
public abstract class AbstractInvitationUserNotification
    extends AbstractTemplateUserNotificationBuilder<Invitation> {

  public AbstractInvitationUserNotification(Invitation invitation) {
    super(invitation);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.social.multilang.socialNetworkBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "socialNetwork";
  }

  @Override
  protected String getTemplateFileName() {
    if (isInvitation()) {
      return "sendInvitation";
    }
    return "acceptInvitation";
  }

  @Override
  protected String getSender() {
    if (isInvitation()) {
      return String.valueOf(getResource().getSenderId());
    }
    return String.valueOf(getResource().getReceiverId());
  }

  @Override
  protected String getBundleSubjectKey() {
    if (isInvitation()) {
      return "myProfile.invitations.notification.send.subject";
    }
    return "myProfile.invitations.notification.accept.subject";
  }

  /**
   * The title is the value of the property defined by {@link #getBundleSubjectKey()} in the
   * localization bundle referred by  {@link #getBundle()}. If no such property exists, then the
   * title is the first name of the notification sender.
   * @return the subject of the notification as defined in the localisation bundle returned by
   * the {@link #getBundle()} method.
   */
  @Override
  protected String getTitle(final String language) {
    return getBundle(language)
        .getStringWithParams(getBundleSubjectKey(), User.getById(getSender()).getFirstName());
  }

  private boolean isInvitation() {
    return NotifAction.PENDING_VALIDATION.equals(getAction());
  }

  @Override
  protected String getComponentInstanceId() {
    return null;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (isInvitation()) {
      return CollectionUtil.asList(String.valueOf(getResource().getReceiverId()));
    }
    return CollectionUtil.asList(String.valueOf(getResource().getSenderId()));
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }
}