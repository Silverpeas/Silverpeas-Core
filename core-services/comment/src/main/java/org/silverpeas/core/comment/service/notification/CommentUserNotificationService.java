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
package org.silverpeas.core.comment.service.notification;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentUserNotification;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A service dedicated to notify users about the adding of a comment to a contribution in
 * Silverpeas.
 * <p>
 * This service listens for creation events on comments to perform its task. For each new comment a
 * notification is sent to all users concerned by the comment. A user is concerned if he has
 * participated in the comment flow of the related contribution or if he's an author of the
 * contribution.
 * @author mmoquillon
 */
@Service
public class CommentUserNotificationService extends CDIResourceEventListener<CommentEvent> {

  /**
   * The suffix of the property valued with the subject of the notification message to send to the
   * users. This property must be defined in the resources of the Silverpeas component module. For
   * example, for a Silverpeas component classifieds, a property classifieds.commentAddingSubject
   * must be defined with the subject of the notification.
   */
  private static final String SUBJECT_COMMENT_ADDING = "commentAddingSubject";

  @Inject
  private CommentService commentService;

  @Override
  public void onCreation(final CommentEvent event) throws Exception {
    Comment comment = event.getTransition().getAfter();
    String componentInstanceId = comment.getComponentInstanceId();
    if (isDefined(componentInstanceId)) {
      Optional<ApplicationService> mayBeService = ApplicationServiceProvider.get()
          .getApplicationServiceById(componentInstanceId);
      mayBeService.ifPresent(service -> {
        try {
          ContributionIdentifier contributionId = ContributionIdentifier.from(
              comment.getResourceReference(), comment.getResourceType());
          Contribution commentedContent = service.getContributionById(contributionId)
              .orElseThrow(
                  () -> new NotFoundException("No such contribution " + contributionId.asString()));
          final Set<String> recipients = getInterestedUsers(comment.getCreator()
              .getId(), commentedContent);
          if (!recipients.isEmpty()) {
            String componentName = getComponentName(componentInstanceId);
            Comment newComment =
                getCommentService().getComment(new CommentId(componentInstanceId, comment.getId()));
            final NotificationMetaData notification = UserNotificationHelper.build(
                new CommentUserNotification(getCommentService(), newComment, commentedContent,
                    componentName + "." + SUBJECT_COMMENT_ADDING, service.getComponentMessages(""),
                    recipients));
            notifyUsers(notification);
          }
        } catch (Exception ex) {
          SilverLogger.getLogger(this).error(ex.getMessage(), ex);
        }
      });
    }
  }

  /**
   * Gets the users that are interested in the adding or the removing of the specified comment and
   * that have enough privileges to access the commented content.
   * <p>
   * The interested users are the authors of the others comments on the content and the creator of
   * this content. The author of the added or removed comment isn't considered as interested in the
   * comment.
   * @param commentAuthorId the identifier of the author of the comment that is concerned by the
   * notification.
   * @param contribution the content that was commented by the specified comment.
   * @return a list with the identifier of the interested users.
   */
  private Set<String> getInterestedUsers(final String commentAuthorId, Contribution contribution) {
    Set<String> interestedUsers = new LinkedHashSet<>();
    ResourceReference ref = new ResourceReference(contribution.getIdentifier()
        .getLocalId(), contribution.getIdentifier()
        .getComponentInstanceId());
    List<Comment> comments =
        getCommentService().getAllCommentsOnResource(contribution.getContributionType(), ref);
    for (Comment aComment : comments) {
      User author = aComment.getCreator();
      if (!author.getId()
          .equals(commentAuthorId) && canBeSent(contribution, author)) {
        interestedUsers.add(author.getId());
      }
    }
    User contentCreator = contribution.getCreator();
    if (!commentAuthorId.equals(contentCreator.getId()) && canBeSent(contribution, contentCreator)) {
      interestedUsers.add(contentCreator.getId());
    }
    User contentUpdater = contribution.getLastUpdater();
    if (contentUpdater != null && !contentUpdater.getId()
        .equals(contentCreator.getId()) && !commentAuthorId.equals(contentUpdater.getId()) &&
        canBeSent(contribution, contentUpdater)) {
      interestedUsers.add(contentUpdater.getId());
    }
    return interestedUsers;
  }

  /**
   * Notifies the specified users, identified by their identifier, with the specified notification
   * information.
   * @param notification the notification information.
   * @throws NotificationException if the notification of the recipients fail.
   */
  protected void notifyUsers(final NotificationMetaData notification) throws NotificationException {
    getNotificationSender(notification.getComponentId()).notifyUser(notification);
  }

  protected NotificationSender getNotificationSender(String componentInstanceId) {
    return new NotificationSender(componentInstanceId);
  }

  protected CommentService getCommentService() {
    return commentService;
  }

  private boolean canBeSent(Contribution contribution, User recipient) {
    return contribution.canBeAccessedBy(recipient);
  }

  private String getComponentName(String componentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getComponentName(componentInstanceId);
  }

}
