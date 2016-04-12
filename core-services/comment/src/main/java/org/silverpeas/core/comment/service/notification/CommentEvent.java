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

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.notification.system.ResourceEvent;

import java.io.Serializable;

/**
 * An event of the life-cycle of comments. Such event is fired when a comment is added to a
 * contribution or when it is updated or when it is deleted.
 * @author mmoquillon
 */
public class CommentEvent extends AbstractResourceEvent<Comment> {

  public CommentEvent() {
    super();
  }

  /**
   * @see AbstractResourceEvent#AbstractResourceEvent(ResourceEvent.Type, Serializable[])
   */
  public CommentEvent(final Type type, Comment ... resource) {
    super(type, resource);
  }
}
