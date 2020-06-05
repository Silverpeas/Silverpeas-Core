/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.comment.service;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.LocalizationBundle;

import java.util.Collection;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class CommentUserNotification
    extends AbstractTemplateUserNotificationBuilder<Contribution>
    implements FallbackToCoreTemplatePathBehavior {

  /**
   * The name of the attribute in a notification message that refers the comment responsable of the triggering of this
   * service.
   */
  public static final String NOTIFICATION_COMMENT_ATTRIBUTE = "comment";

  /**
   * Template path.
   */
  private static final String TEMPLATE_PATH = "comment";

  /**
   * If no property with the subject of the notification message is defined in a Silverpeas component, then the below
   * default property is taken.
   */
  private static final String DEFAULT_SUBJECT_COMMENT_ADDING = "comments.commentAddingSubject";

  /**
   * The name of the attribute in a notification message that refers the content commented by the comment responsable of
   * the triggering of this service.
   */
  private static final String NOTIFICATION_CONTENT_ATTRIBUTE = "content";

  private final CommentService commentService;
  private final String subjectKey;
  private final LocalizationBundle componentMessages;
  private final Comment comment;
  private final Set<String> recipients;

  public CommentUserNotification(final CommentService commentService, final Comment comment,
      final Contribution commentedContent, final String subjectKey,
      final LocalizationBundle componentMessages, final Set<String> recipients) {
    super(commentedContent);
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

  /**
   * The title is the either the default subject of the notifications as defined in the bundle
   * returned by {@link #getBundle()} method and by the property given by the
   * {@link #getBundleSubjectKey()} method or the default subject for the notification about the
   * comments.
   * @param language the ISO-631 code of the language. It is here not taken into account. Only the
   * the locale of the component messages bundle is taken into account.
   * @return the subject of the notification.
   */
  @Override
  protected String getTitle(final String language) {
    final String subject;
    if (componentMessages.containsKey(getBundleSubjectKey())) {
      subject = componentMessages.getString(getBundleSubjectKey());
    } else {
      subject = "";
    }
    return isDefined(subject) ? subject :
        commentService.getComponentMessages(componentMessages.getLocale().getLanguage())
            .getString(DEFAULT_SUBJECT_COMMENT_ADDING);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return recipients;
  }

  @Override
  protected void perform(final Contribution resource) {
    getNotificationMetaData().setOriginalExtraMessage(comment.getMessage());
  }

  @Override
  protected void performTemplateData(final String language, final Contribution resource,
      final SilverpeasTemplate template) {
    componentMessages.changeLocale(language);
    getNotificationMetaData().addLanguage(language, getTitle(), "");
    template.setAttribute(NOTIFICATION_CONTENT_ATTRIBUTE, getResource());
    template.setAttribute(NOTIFICATION_COMMENT_ATTRIBUTE, comment);
  }

  @Override
  protected void performNotificationResource(final String language, final Contribution resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
  }

  @Override
  protected String getTemplatePath() {
    return TEMPLATE_PATH;
  }

  @Override
  protected String getTemplateFileName() {
    return "commented";
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
  protected boolean isSendImmediately() {
    return (getResource() instanceof SilverpeasToolContent);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.util.comment.multilang.comment";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "comment.notifCommentLinkLabel";
  }
}
