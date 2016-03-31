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
package org.silverpeas.core.comment.service;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class CommentUserNotification extends AbstractTemplateUserNotificationBuilder<SilverpeasContent> {

  /**
   * If no property with the subject of the notification message is defined in a Silverpeas component, then the below
   * default property is taken.
   */
  public static final String DEFAULT_SUBJECT_COMMENT_ADDING = "comments.commentAddingSubject";

  /**
   * The name of the attribute in a notification message that refers the comment responsable of the triggering of this
   * service.
   */
  public static final String NOTIFICATION_COMMENT_ATTRIBUTE = "comment";

  /**
   * The name of the attribute in a notification message that refers the content commented by the comment responsable of
   * the triggering of this service.
   */
  public static final String NOTIFICATION_CONTENT_ATTRIBUTE = "content";

  private final CommentService commentService;
  private final String subjectKey;
  private final LocalizationBundle componentMessages;
  private final Comment comment;
  private final Set<String> recipients;

  public CommentUserNotification(final CommentService commentService, final Comment comment,
      final SilverpeasContent commentedContent, final String subjectKey,
      final LocalizationBundle componentMessages, final Set<String> recipients) {
    super(commentedContent, null, "commented");
    this.commentService = commentService;
    this.comment = comment;
    this.subjectKey = subjectKey;
    this.componentMessages = componentMessages;
    this.recipients = recipients;
  }

  @Override
  protected String getBundleSubjectKey() {
    return subjectKey;
  }

  @Override
  protected String getTitle() {
    String subject = componentMessages.getString(getBundleSubjectKey());
    if (!isDefined(subject)) {
      subject =
          commentService.getComponentMessages(componentMessages.getLocale().getLanguage())
              .getString(DEFAULT_SUBJECT_COMMENT_ADDING);
    }
    return subject;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return recipients;
  }

  @Override
  protected void perform(final SilverpeasContent resource) {
    getNotificationMetaData().setOriginalExtraMessage(comment.getMessage());
  }

  @Override
  protected void performTemplateData(final String language, final SilverpeasContent resource,
      final SilverpeasTemplate template) {
    componentMessages.changeLocale(language);
    getNotificationMetaData().addLanguage(language, getTitle(), "");
  }

  @Override
  protected void performNotificationResource(final String language, final SilverpeasContent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
  }

  @Override
  protected SilverpeasTemplate createTemplate() {
    final SettingBundle settings = commentService.getComponentSettings();
    final Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        settings.getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        settings.getString("customersTemplatePath"));

    final SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
    template.setAttribute(NOTIFICATION_CONTENT_ATTRIBUTE, getResource());
    template.setAttribute(NOTIFICATION_COMMENT_ATTRIBUTE, comment);
    return template;
  }

  @Override
  protected String getTemplatePath() {
    return null;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.COMMENT;
  }

  @Override
  protected String getComponentInstanceId() {
    return comment.getCommentPK().getInstanceId();
  }

  @Override
  protected String getSender() {
    return comment.getCreator().getId();
  }

  @Override
  protected boolean isSendImmediatly() {
    return (getResource() instanceof SilverpeasToolContent);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.util.comment.multilang.comment";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "comment.notifCommentLinkLabel";
  }
}
