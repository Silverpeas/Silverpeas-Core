/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.comment.service.notification;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentUserNotification;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A service dedicated to notify users about the adding of a comment to a contribution in
 * Silverpeas.
 * <p>
 * This service listens for creation events on comments to perform its task. For each
 * new comment a notification is sent to all users concerned by the comment. A user is concerned if
 * he has participated in the comment flow of the related contribution or if he's an author of the
 * contribution.
 * @author mmoquillon
 */
public class CommentUserNotificationService extends CDIResourceEventListener<CommentEvent> {

  /**
   * The suffix of the property valued with the subject of the notification message to send to the
   * users. This property must be defined in the resources of the Silverpeas component module. For
   * example, for a Silverpeas component classifieds, a property classifieds.commentAddingSubject
   * must be defined with the subject of the notification.
   */
  private static String SUBJECT_COMMENT_ADDING = "commentAddingSubject";

  private Map<String, ApplicationService> services = new ConcurrentHashMap<>();

  @Inject
  private CommentService commentService;

  @Override
  public void onCreation(final CommentEvent event) throws Exception {
    Comment comment = event.getTransition().getAfter();
    String componentInstanceId = comment.getComponentInstanceId();
    String componentName = getComponentName(componentInstanceId);
    if (isDefined(componentInstanceId)) {
      ApplicationService service = lookupComponentService(componentInstanceId);
      if (service != null) {
        try {
          SilverpeasContent commentedContent =
              service.getContentById(comment.getForeignKey().getId());
          final Set<String> recipients =
              getInterestedUsers(comment.getCreator().getId(), commentedContent);
          if (!recipients.isEmpty()) {
            Comment newComment =
                getCommentService().getComment(new CommentPK(comment.getId(), componentInstanceId));
            final NotificationMetaData notification = UserNotificationHelper.build(
                new CommentUserNotification(getCommentService(), newComment, commentedContent,
                    componentName + "." + SUBJECT_COMMENT_ADDING, service.
                    getComponentMessages(""), recipients));
            notifyUsers(notification);
          }
        } catch (Exception ex) {
          SilverLogger.getLogger(this).error(ex.getMessage(), ex);
        }
      }
    }
  }

  private ApplicationService lookupComponentService(String instanceId) {
    String componentServiceName = getComponentName(instanceId) + "Service";
    final ApplicationService[] service = {services.get(componentServiceName)};
    if (service[0] == null) {
      try {
        service[0] = ServiceProvider.getService(componentServiceName);
      } catch (IllegalStateException ex) {
        final Set<ApplicationService> availableServices =
            ServiceProvider.getAllServices(ApplicationService.class);
        availableServices.stream().filter(s -> s.isRelatedTo(instanceId)).findFirst()
            .ifPresent(s -> service[0] = s);
      }
      if (service[0] != null) {
        services.put(componentServiceName, service[0]);
      }
    }
    return service[0];
  }

  /**
   * Gets the name of the Silverpeas component to which the specified instance belongs.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return the unique name of the Silverpeas component.
   */
  private String getComponentName(String componentInstanceId) {
    return componentInstanceId.split("\\d+")[0];
  }

  /**
   * Gets the users that are interested by the adding or the removing of the specified comment and
   * that have enough privileges to access the commented content.
   * <p>
   * The interested users are the authors of the others comments on the content and the creator of
   * this content. The author of the added or removed comment isn't considered as interested by the
   * comment.
   * @param commentAuthorId the identifier of the author of the comment that is concerned by the
   * notification.
   * @param content the content that was commented by the specified comment.
   * @return a list with the identifier of the interested users.
   */
  private Set<String> getInterestedUsers(final String commentAuthorId, SilverpeasContent content) {
    Set<String> interestedUsers = new LinkedHashSet<String>();
    WAPrimaryKey pk = new ForeignPK(content.getId(), content.getComponentInstanceId());
    List<Comment> comments = getCommentService().getAllCommentsOnPublication(content.
        getContributionType(), pk);
    for (Comment aComment : comments) {
      UserDetail author = aComment.getCreator();
      if (!author.getId().equals(commentAuthorId) && canBeSent(content, author)) {
        interestedUsers.add(author.getId());
      }
    }
    UserDetail contentCreator = content.getCreator();
    if (!commentAuthorId.equals(contentCreator.getId()) && canBeSent(content, contentCreator)) {
      interestedUsers.add(contentCreator.getId());
    }
    return interestedUsers;
  }

  /**
   * Notifies the specified users, identified by their identifier, with the specified notification
   * information.
   * @param notification the notification information.
   * @throws NotificationManagerException if the
   * notification of the recipients fail.
   */
  protected void notifyUsers(final NotificationMetaData notification)
      throws NotificationManagerException {
    getNotificationSender(notification.getComponentId()).notifyUser(notification);
  }

  protected NotificationSender getNotificationSender(String componentInstanceId) {
    return new NotificationSender(componentInstanceId);
  }

  protected CommentService getCommentService() {
    return commentService;
  }

  private boolean canBeSent(SilverpeasContent content, UserDetail recipient) {
    return content.canBeAccessedBy(recipient);
  }
}
