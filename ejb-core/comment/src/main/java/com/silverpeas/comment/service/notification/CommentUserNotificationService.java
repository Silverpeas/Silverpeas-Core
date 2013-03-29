/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.model.Comment;

/**
 * It is a service dedicated to notify the concerning users about the adding or the removal of the
 * comments on a resource managed by a given Silverpeas component instance. This service is managed
 * by the IoC container. All Silverpeas components interested to provide this feature should
 * register themselves with this service. The registration is done by specifying the name of the
 * component and by passing a ResourceInfoGetter object dedicated to the registered component. So,
 * each time an event about a comment is received, from the component instance within which the
 * comment is added or removed, the name of the Silverpeas component can be get and then the
 * ResourceInfoGetter object can be retrieved amoung which enough information about the commented
 * resource can be get to send a notification to the interested users.
 */
public interface CommentUserNotificationService {

  /**
   * The suffix of the property valued with the subject of the notification message to send to the
   * users. This property must be defined in the resources of the Silverpeas component module. For
   * example, for a Silverpeas component classifieds, a property classifieds.commentAddingSubject
   * must be defined with the subject of the notification.
   */
  String SUBJECT_COMMENT_ADDING = "commentAddingSubject";

  /**
   * Sends a notification to the users that are concerned by the specified new comment on a given
   * resource (publication, blog article, ...). The concerned users are thoses that commented the
   * resource plus the author of the resource (in the case he's not the author of this new comment);
   * the author of the comment isn't notified by its own comment.
   *
   * @param newComment the new comment.
   */
  void commentAdded(Comment newComment);

  /**
   * No notifications are sent to the users when a comment is removed. This feature isn't currently
   * implemented.
   *
   * @param removedComment the comment that is removed.
   */
  void commentRemoved(Comment removedComment);

  /**
   * Registers the specified Silverpeas component so that the comments created or removed in an
   * instance of the component will be treated by this service. The registration is done by
   * specifying the unique name of the Silverpeas component and a ResourceInfoGetter object from
   * which information about the commented resource can be get. A ResourceInfoGetter object,
   * specific to the Silverpeas component, must be passed so that for each received event about a
   * comment handled in an instance of the component, information about the commented resource can
   * be get in order to send a well-formed notification to the users interested by the event about
   * the comment.
   *
   * @param component the name of the Silverpeas component (it must be unique).
   * @param getter the ResourceInfoGetter object specific to the registered Silverpeas Component
   */
  void register(String component,
      final SilverpeasComponentService<? extends SilverpeasContent> service);

  /**
   * Unregisters the specified component. The comments handled within the specified comment won't be
   * more treated by this service. The users won't be any more notified about them.
   *
   * @param component the name of the Silverpeas component to unregister (it must be unique).
   */
  void unregister(String component);
}
