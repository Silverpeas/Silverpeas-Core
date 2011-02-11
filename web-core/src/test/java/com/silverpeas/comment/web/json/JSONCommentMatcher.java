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
package com.silverpeas.comment.web.json;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.web.CommentEntity;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import static com.silverpeas.comment.web.json.JSONCommentFields.*;

/**
 * A matcher of JSON comment representation with a comment.
 */
public class JSONCommentMatcher extends BaseMatcher<String> {

  /**
   * Constructs a new JSON representation matcher with the specified comments.
   * @param theComments the comments to check the matching.
   * @return a matcher.
   */
  public static JSONCommentMatcher represents(final Comment... theComments) {
    return new JSONCommentMatcher(theComments);
  }

  /**
   * Constructs a new JSON representation matcher with the specified comment entities.
   * @param theComments the comment entities to check the matching.
   * @return a matcher.
   */
  public static JSONCommentMatcher represents(final CommentEntity... theComments) {
    Comment[] comments = new Comment[theComments.length];
    for (int i = 0; i < theComments.length; i++) {
      comments[i] = theComments[i].toComment();

    }
    return new JSONCommentMatcher(comments);
  }

  public static Comment[] anArrayOf(final Comment... comments) {
    return comments;
  }

  public static CommentEntity[] anArrayOf(final CommentEntity... comments) {
    return comments;
  }
  private final Comment[] comments;

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof String) {
      String json = (String) item;
      System.out.println("ACTUAL:\n" + json);
      if (comments.length > 1) {
        match = json.startsWith("[{") && json.endsWith("}]");
      } else {
        match = json.startsWith("{") && json.endsWith("}");
      }
      for (Comment comment : comments) {
        match &= json.contains(commentIdOf(comment)) &&
            json.contains(resourceIdOf(comment)) &&
            json.contains(componentIdOf(comment)) &&
            json.contains(textOf(comment)) &&
            json.contains(creationDateOf(comment)) &&
            json.contains(modificationDateOf( comment)) &&
            json.contains(authorIdOf(comment)) &&
            json.contains(authorAvatarOf(comment)) &&
            json.contains(authorNameOf(comment));
      }
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    StringBuilder builder = new StringBuilder();
    if (comments.length > 1) {
      builder.append("[");
    }
    for (int i = 0; i < comments.length; i++) {
      Comment comment = comments[i];
      if (i != 0) {
        builder.append(",");
      }
      builder.append("{").append(commentIdOf(comment)).append(",").append(resourceIdOf(
          comment)).append(",").append(textOf(comment)).append(",").append(authorIdOf(
          comment)).append(",").append(authorNameOf(comment)).append(",").
          append(authorAvatarOf(comment)).append(",").append(creationDateOf(comment)).append(",").
          append(modificationDateOf(comment)).append("}");
    }
    if (comments.length > 1) {
      builder.append("]");
    }
    System.out.println("EXPECTED:\n" + builder.toString());
    description.appendText(builder.toString());
  }

  private JSONCommentMatcher(final Comment... comments) {
    this.comments = comments.clone();
  }

  private static String commentIdOf(Comment theComment) {
    return "\"" + COMMENT_ID_FIELD + "\":\"" + theComment.getCommentPK().getId() + "\"";
  }

  private static String componentIdOf(Comment theComment) {
    return "\"" + COMPONENT_ID_FIELD + "\":\"" + theComment.getCommentPK().getInstanceId() + "\"";
  }

  private static String resourceIdOf(Comment theComment) {
    return "\"" + RESOURCE_ID_FIELD + "\":\"" + theComment.getForeignKey().getId() + "\"";
  }

  private static String textOf(Comment theComment) {
    return "\"" + TEXT_FIELD + "\":\"" + theComment.getMessage() + "\"";
  }

  private static String authorIdOf(final Comment theComment) {
    return "\"" + AUTHOR_ID_FIELD + "\":\"" + theComment.getOwnerDetail().getId() + "\"";
  }

  private static String authorAvatarOf(final Comment theComment) {
    return "\"" + AUTHOR_AVATAR_FIELD + "\":\"" + theComment.getOwnerDetail().getAvatar() + "\"";
  }

  private static String authorNameOf(final Comment theComment) {
    return "\"" + AUTHOR_NAME_FIELD + "\":\"" + theComment.getOwnerDetail().getDisplayedName()
        + "\"";
  }

  private static String creationDateOf(Comment theComment) {
    return "\"" + CREATION_DATE_FIELD + "\":\"" + theComment.getCreationDate() + "\"";
  }

  private static String modificationDateOf(Comment theComment) {
    return "\"" + MODIFICATION_DATE_FIELD + "\":\"" + theComment.getModificationDate() + "\"";
  }
}
