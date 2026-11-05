/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.comment.service;

import java.util.Set;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/** Native delivery for mentions newly added while editing a comment. */
public class CommentMentionNotification extends CommentUserNotification {
  private final CommentService comments;

  public CommentMentionNotification(CommentService comments, Comment comment,
      Contribution resource, Set<String> recipients) {
    super(comments, comment, resource, "comment.mentionSubject", comments.getComponentMessages(""), recipients);
    this.comments = comments;
  }

  @Override
  protected String getTitle(String language) {
    return comments.getComponentMessages(language).getString("comment.mentionSubject");
  }

  @Override
  protected NotifAction getAction() { return NotifAction.UPDATE; }
}
