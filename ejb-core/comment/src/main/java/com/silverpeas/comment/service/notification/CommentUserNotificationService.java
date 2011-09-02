/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.service.notification;

import com.silverpeas.util.template.SilverpeasTemplateFactory;
import java.util.Properties;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.silverpeas.util.template.SilverpeasTemplate;
import java.util.HashMap;
import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentActionListener;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import static com.silverpeas.util.StringUtil.*;

/**
 * It is a service dedicated to notify the concerning users about the adding or the removal of
 * the comments on a resource managed by a given Silverpeas component instance.
 * 
 * This service is managed by the IoC container.
 * 
 * All Silverpeas components interested to provide this feature should register themselves with this
 * service. The registration is done by specifying the name of the component and by passing a
 * ResourceInfoGetter object dedicated to the registered component. So, each time an event about
 * a comment is received, from the component instance within which the comment is added or removed,
 * the name of the Silverpeas component can be get and then the ResourceInfoGetter object can be
 * retrieved amoung which enough information about the commented resource can be get to send a
 * notification to the interested users.
 */
@Named
public class CommentUserNotificationService extends CommentActionListener {

  /**
   * The suffix of the property valued with the subject of the notification message to send to the
   * users.
   * This property must be defined in the resources of the Silverpeas component module.
   * For example, for a Silverpeas component classifieds, a property classifieds.commentAddingSubject
   * must be defined with the subject of the notification.
   */
  public static final String SUBJECT_COMMENT_ADDING = "commentAddingSubject";
  /**
   * If no property with the subject of the notification message is defined in a Silverpeas component,
   * then the below default property is taken.
   */
  protected static final String DEFAULT_SUBJECT_COMMENT_ADDING = "comments.commentAddingSubject";
  /**
   * The name of the attribute in a notification message that refers the comment responsable of
   * the triggering of this service.
   */
  protected static final String NOTIFICATION_COMMENT_ATTRIBUTE = "comment";
  /**
   * The name of the attribute in a notification message that refers the content commented by the
   * comment responsable of the triggering of this service.
   */
  protected static final String NOTIFICATION_CONTENT_ATTRIBUTE = "content";
  @Inject
  private CommentService commentService;
  private Map<String, SilverpeasComponentService> register =
          new ConcurrentHashMap<String, SilverpeasComponentService>();

  /**
   * Registers the specified Silverpeas component so that the comments created or removed in an
   * instance of the component will be treated by this service. The registration is done by
   * specifying the unique name of the Silverpeas component and a ResourceInfoGetter object from
   * which information about the commented resource can be get.
   * 
   * A ResourceInfoGetter object, specific to the Silverpeas component, must be passed so that for
   * each received event about a comment handled in an instance of the component, information about
   * the commented resource can be get in order to send a well-formed notification to the users
   * interested by the event about the comment.
   * @param component the name of the Silverpeas component (it must be unique).
   * @param getter the ResourceInfoGetter object specific to the registered Silverpeas Component 
   */
  public void register(String component, final SilverpeasComponentService service) {
    if (!isDefined(component) || service == null) {
      throw new IllegalArgumentException(
              "Either the component name or the component service is null or invalid");
    }
    register.put(component, service);
  }

  /**
   * Unregisters the specified component.
   * The comments handled within the specified comment won't be more treated by this service. The
   * users won't be any more notified about them.
   * @param component the name of the Silverpeas component to unregister (it must be unique).
   */
  public void unregister(String component) {
    register.remove(component);
  }

  /**
   * Sends a notification to the users that are concerned by the specified new comment on a given
   * resource (publication, blog article, ...).
   * The concerned users are thoses that commented the resource plus the author of the resource (in
   * the case he's not the author of this new comment); the author of the comment isn't notified by
   * its own comment.
   * @param newComment the new comment.
   */
  @Override
  public void commentAdded(Comment newComment) {
    String componentInstanceId = newComment.getCommentPK().getInstanceId();
    if (isDefined(componentInstanceId)) {
      String component = getComponentName(componentInstanceId);
      if (register.containsKey(component)) {
        SilverpeasComponentService service = register.get(component);
        try {
          SilverpeasContent commentedContent =
                  service.getContent(newComment.getForeignKey().getId());
          Set<String> recipients = getInterestedUsers(newComment.getCreator(), commentedContent);
          NotificationMetaData notification = createNotification(
                  component + "." + SUBJECT_COMMENT_ADDING,
                  newComment,
                  commentedContent,
                  service.getComponentMessages(""));
          notifyUsers(recipients, notification);
        } catch (Exception ex) {
          Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
      }
    }
  }

  /**
   * No notifications are sent to the users when a comment is removed. This feature isn't currently
   * implemented.
   * @param removedComment the comment that is removed.
   */
  @Override
  public void commentRemoved(Comment removedComment) {
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
   * Gets the users that are interested by the adding or the removing of the specified comment.
   *
   * The interested users are the authors of the others comments on the content and the
   * creator of this content. The author of the added or removed comment isn't considered
   * as interested by the comment.
   * @param theComment the comment that is added or removed.
   * @param getter an object with which the service can ask for information about the commented
   * resource.
   * @return a list with the identifier of the interested users.
   */
  private Set<String> getInterestedUsers(final UserDetail commentAuthor, SilverpeasContent content) {
    Set<String> interestedUsers = new LinkedHashSet<String>();
    WAPrimaryKey pk = new ForeignPK(content.getId(), content.getComponentInstanceId());
    List<Comment> comments = getCommentService().getAllCommentsOnPublication(pk);
    for (Comment aComment : comments) {
      UserDetail author = aComment.getCreator();
      if (!author.getId().equals(commentAuthor.getId())) {
        interestedUsers.add(author.getId());
      }
    }
    UserDetail contentCreator = content.getCreator();
    if (!commentAuthor.getId().equals(contentCreator.getId())) {
      interestedUsers.add(contentCreator.getId());
    }
    return interestedUsers;
  }

  /**
   * Notifies the specified users, identified by their identifier, with the specified notification
   * information.
   * @param recipients the recipients of the notification.
   * @param notification the notification information.
   * @throws NotificationManagerException if the notification of the recipients fail.
   */
  protected void notifyUsers(final Set<String> recipients, final NotificationMetaData notification)
          throws NotificationManagerException {
    for (String recipient : recipients) {
      notification.addUserRecipient(new UserRecipient(recipient));
    }
    NotificationSender sender = getNotificationSender(notification.getComponentId());
    sender.notifyUser(notification);
  }

  protected NotificationSender getNotificationSender(String componentInstanceId) {
    return new NotificationSender(componentInstanceId);
  }

  protected CommentService getCommentService() {
    return commentService;
  }

  private NotificationMetaData createNotification(String subjectKey, final Comment aComment,
          final SilverpeasContent content, final ResourceLocator messages) {
    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            getNotificationSubject(subjectKey, messages), templates, "commented");
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      messages.setLanguage(language);
      SilverpeasTemplate template = aNotificationTemplate(aComment, content);
      templates.put(language, template);
      notifMetaData.addLanguage(language, getNotificationSubject(subjectKey, messages), "");
    }
    notifMetaData.setLink(content.getURL());
    notifMetaData.setComponentId(aComment.getCommentPK().getInstanceId());
    notifMetaData.setSender(aComment.getCreator().getId());

    return notifMetaData;
  }

  private String getNotificationSubject(String subjectKey, final ResourceLocator componentMessages) {
    String subject = componentMessages.getString(subjectKey);
    if (!isDefined(subject)) {
      subject = getCommentService().getComponentMessages(componentMessages.getLanguage()).getString(
              DEFAULT_SUBJECT_COMMENT_ADDING);
    }
    return subject;
  }

  /**
   * Creates a notification message template for the commented Silverpeas content.
   * @param comment the comment for which a notification has to be sent to some users.
   * @param commentedContent the content that is commented.
   * @return a SilverpeasTemplate instance.
   */
  private SilverpeasTemplate aNotificationTemplate(final Comment comment,
          final SilverpeasContent commentedContent) {
    ResourceLocator settings = getCommentService().getComponentSettings();
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
            settings.getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
            settings.getString("customersTemplatePath"));

    SilverpeasTemplate template =
            SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
    template.setAttribute(NOTIFICATION_CONTENT_ATTRIBUTE, commentedContent);
    template.setAttribute(NOTIFICATION_COMMENT_ATTRIBUTE, comment);
    return template;
  }
}
