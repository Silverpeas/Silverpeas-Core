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

  public static Comment[] anArrayOf(final Comment... comments) {
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
        match &= json.contains(commentIdOf(comment)) && json.contains(resourceIdOf(comment)) && json.
            contains(componentIdOf(comment)) && json.contains(textOf(comment)) && json.contains(creationDateOf(
            comment)) && json.contains(modificationDateOf(
            comment)) && json.contains(writerOf(comment));
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
          comment)).append(",").append(textOf(comment)).append(",").append(writerOf(
          comment)).append(",").append(creationDateOf(
          comment)).append(",").append(modificationDateOf(comment)).append("}");
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
    return "\"" + COMMENT_ID + "\":\"" + theComment.getCommentPK().getId() + "\"";
  }

  private static String componentIdOf(Comment theComment) {
    return "\"" + COMPONENT_ID + "\":\"" + theComment.getCommentPK().getInstanceId() + "\"";
  }

  private static String resourceIdOf(Comment theComment) {
    return "\"" + RESOURCE_ID + "\":\"" + theComment.getForeignKey().getId() + "\"";
  }

  private static String textOf(Comment theComment) {
    return "\"" + TEXT + "\":\"" + theComment.getMessage() + "\"";
  }

  private static String writerOf(final Comment theComment) {
    return "\"" + WRITER + "\":{\"" + WRITER_ID + "\":\"" + theComment.getOwnerDetail().getId()
        + "\",\"" + WRITER_NAME + "\":\"" + theComment.getOwnerDetail().getDisplayedName() + "\""
        + ",\"" + WRITER_AVATAR + "\":\"" + theComment.getOwnerDetail().getAvatar() + "\"}";
  }

  private static String creationDateOf(Comment theComment) {
    return "\"" + CREATION_DATE + "\":\"" + theComment.getCreationDate() + "\"";
  }

  private static String modificationDateOf(Comment theComment) {
    return "\"" + MODIFICATION_DATE + "\":\"" + theComment.getModificationDate() + "\"";
  }
}
