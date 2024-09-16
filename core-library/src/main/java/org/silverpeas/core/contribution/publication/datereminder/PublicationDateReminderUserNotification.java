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
package org.silverpeas.core.contribution.publication.datereminder;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Set parameters for user notifications sended automatically for date reminder.
 * @author Cécile Bonin
 */
public class PublicationDateReminderUserNotification
    extends AbstractTemplateUserNotificationBuilder<PersistentResourceDateReminder>
    implements FallbackToCoreTemplatePathBehavior {

  private final PublicationDetail pubDetail;

  PublicationDateReminderUserNotification(
      final PersistentResourceDateReminder resourceDateReminder) {
    super(resourceDateReminder);
    PublicationNoteReference pubNoteReference = resourceDateReminder.getResource(PublicationNoteReference.class);
    this.pubDetail = pubNoteReference != null ? pubNoteReference.getEntity() : null;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "dateReminder.notifSubjectDateReminder";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    Collection<String> userIds = new ArrayList<>();
    String creatorId = this.pubDetail.getCreatorId();
    userIds.add(creatorId);
    String updaterId = this.pubDetail.getUpdaterId();
    if(!creatorId.equals(updaterId)) {
      userIds.add(updaterId);
    }
    return userIds;
  }

  @Override
  protected void performTemplateData(final String language, final PersistentResourceDateReminder resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getBundle(language).getString(
        getBundleSubjectKey()), "");
    template.setAttribute("resourceTitle", this.pubDetail.getName(language));
    template.setAttribute("resourceDesc", this.pubDetail.getDescription(language));
    template.setAttribute("resourceNote", resource.getDateReminder().getMessage());
    String updatedId = this.pubDetail.getUpdaterId();
    template.setAttribute("senderName", UserDetail.getById(updatedId).getDisplayedName());
  }

  @Override
  protected void performNotificationResource(final String language, final PersistentResourceDateReminder resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(this.pubDetail.getTitle());
    notificationResourceData.setResourceDescription(this.pubDetail.getDescription());
  }

  @Override
  protected String getResourceURL(final PersistentResourceDateReminder resource) {
    return URLUtil.getSearchResultURL(this.pubDetail);
  }

  @Override
  protected String getTemplatePath() {
    return "dateReminder";
  }

  @Override
  protected String getTemplateFileName() {
    return "dateReminder";
  }

  @Override
  protected NotifAction getAction() {
    return null;
  }

  @Override
  protected String getComponentInstanceId() {
    return this.pubDetail.getPK().getInstanceId();
  }

  @Override
  protected String getSender() {
    // Empty is here returned, because the notification is from the platform and not from an
    // other user.
    return "";
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.dateReminder.multilang.dateReminder";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "dateReminder.notifPublicationLinkLabel";
  }
}