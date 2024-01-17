/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.comment.test;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;

import java.util.Date;

/**
 * A comment builder dedicated to unit tests.
 */
public class CommentBuilder {

  private static final String RESOURCE_TYPE = "RtypeTest";
  private static final String RESOURCE_ID = "500";
  private static final String SPACE_ID = "Toto";
  private static final String COMPONENT_ID = "instanceId10";

  private int i = 0;

  public static CommentBuilder getBuilder() {
    return new CommentBuilder();
  }

  /**
   * Builds a comment with the specified author and with the specified comment text. All built
   * comments are about the same resource.
   * @param author the author of the comment.
   * @param text the text of the comment.
   * @return a Comment instance.
   */
  public Comment buildWith(final String author, final String text) {
    Date now = new Date();
    Comment comment = new Comment(new CommentId(COMPONENT_ID, String.valueOf(i++)), author,
        RESOURCE_TYPE, new ResourceReference(RESOURCE_ID, COMPONENT_ID), now);
    comment.setMessage(text);
    return comment;
  }

  /**
   * Gets a reference to the resource to which comments are built by all of the
   * CommentBuilder instances.
   * @return the primary key of the commented resource.
   */
  public static ResourceReference getResourceReference() {
    return new ResourceReference(RESOURCE_ID, COMPONENT_ID);
  }

  private CommentBuilder() {

  }
}
