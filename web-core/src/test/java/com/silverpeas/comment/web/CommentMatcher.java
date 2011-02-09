/*
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.comment.web;

import com.silverpeas.comment.model.Comment;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * A matcher of two comment objects. The comments match if some of their properties are equal.
 */
public class CommentMatcher extends BaseMatcher<CommentEntity> {

  private CommentEntity comment;

  /**
   * Creates a new matcher with the specified comment.
   * @param theComment the comment to match.
   * @return a comment matcher.
   */
  public static CommentMatcher matches(final CommentEntity theComment) {
    return new CommentMatcher(theComment);
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof CommentEntity) {
      CommentEntity actual = (CommentEntity) item;
      match = actual.getId().equals(comment.getId()) &&
          actual.getComponentId().equals(comment.getComponentId()) &&
          actual.getContentId().equals(comment.getContentId()) &&
          actual.getCreationDate().equals(comment.getCreationDate()) &&
          actual.getText().equals(comment.getText()) &&
          actual.getModificationDate().equals(comment.getModificationDate()) &&
          actual.getWriter().getId().equals(comment.getWriter().getId());
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(comment);
  }

  private CommentMatcher(final CommentEntity comment) {
    this.comment = comment;
  }



}
