/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.comment.web;

import com.silverpeas.comment.model.Comment;
import org.apache.commons.lang3.time.FastDateFormat;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * A matcher between a web comment entity and a comment it should represent.
 */
public class CommentEntityMatcher extends BaseMatcher<CommentEntity> {

  private static final FastDateFormat dateFormat = FastDateFormat.getInstance("dd/MM/yyyy");

  private Comment comment;

  /**
   * Creates a new matcher with the specified comment.
   * @param theComment the comment to match.
   * @return a comment matcher.
   */
  public static CommentEntityMatcher matches(final Comment theComment) {
    return new CommentEntityMatcher(theComment);
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof CommentEntity) {
      CommentEntity actual = (CommentEntity) item;
      match = comment.getCommentPK().getId().equals(actual.getId()) &&
          comment.getCommentPK().getInstanceId().equals(actual.getComponentId()) &&
          comment.getForeignKey().getId().equals(actual.getResourceId()) &&
          comment.getResourceType().equals(actual.getResourceType()) &&
          dateFormat.format(comment.getCreationDate()).equals(actual.getCreationDate()) &&
          comment.getMessage().equals(actual.getText()) &&
          dateFormat.format(comment.getModificationDate()).equals(actual.getModificationDate()) &&
          comment.getOwnerDetail().getId().equals(actual.getAuthor().getId());
      if (!actual.getAuthor().getAvatar().isEmpty()) {
        match &= actual.getAuthor().getAvatar().endsWith(comment.getOwnerDetail().getAvatar());
      }
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(comment);
  }

  private CommentEntityMatcher(final Comment comment) {
    this.comment = comment;
  }
}
