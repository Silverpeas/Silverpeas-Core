/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.datereminder;

import java.util.ArrayList;
import java.util.Collection;

import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;

/**
 * Set parameters for user notifications sended automatically for date reminder.
 * @author Cécile Bonin
 */
public class PublicationDateReminderUserNotification
    extends AbstractTemplateUserNotificationBuilder<PersistentResourceDateReminder> {

  private final LocalizationBundle componentMessages;
  private final PublicationDetail pubDetail;

  public PublicationDateReminderUserNotification(
      final PersistentResourceDateReminder resourceDateReminder,
      final LocalizationBundle componentMessages) {
    super(resourceDateReminder, "dateReminder");
    PublicationNoteReference pubNoteReference = resourceDateReminder.getResource(PublicationNoteReference.class);
    this.pubDetail = pubNoteReference.getEntity();
    this.componentMessages = componentMessages;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "dateReminder.notifSubjectDateReminder";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    Collection<String> userIds = new ArrayList<String>();
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
    componentMessages.changeLocale(language);
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
  protected SilverpeasTemplate createTemplate() {
    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(getTemplatePath());
    return template;
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
  protected boolean isSendImmediatly() {
    return true;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.dateReminder.multilang.dateReminder";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "dateReminder.notifPublicationLinkLabel";
  }
}