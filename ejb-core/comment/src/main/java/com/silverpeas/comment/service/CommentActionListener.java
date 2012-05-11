/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.comment.service;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import static com.silverpeas.notification.NotificationTopic.*;
import static com.silverpeas.comment.service.CommentActionNotifier.*;

/**
 * A Listener of actions on comments. The listener is a subscriber for notifications coming from the
 * actions on comments, so that it will be informed about them. All objects that whish to be
 * informed of the adding or removing of comments should extends this class.
 */
public abstract class CommentActionListener extends DefaultNotificationSubscriber {

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(TOPIC_NAME));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(TOPIC_NAME));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    try {
      Comment comment = (Comment) notification.getObject();
      if (notification instanceof CommentAddingNotification) {
        commentAdded(comment);
      } else if (notification instanceof CommentRemovalNotification) {
        commentRemoved(comment);
      }
    } catch (ClassCastException ex) {
      SilverTrace.error("comment", getClass().getSimpleName() + ".onNotification()",
          "comment.UNKNOWN_ACTION", ex);
    }
  }

  /**
   * A comment is added to a resource in a Silverpeas component instance. The implementer implements
   * this method to perform a computation when a comment is added.
   * @param addedComment the comment that is added.
   */
  public abstract void commentAdded(final Comment addedComment);

  /**
   * A comment is removed from a resource in a Silverpeas component instance. The implementer
   * implements this method to perform a computation when a comment is removed.
   * @param removedComment the comment that is removed.
   */
  public abstract void commentRemoved(final Comment removedComment);
}
