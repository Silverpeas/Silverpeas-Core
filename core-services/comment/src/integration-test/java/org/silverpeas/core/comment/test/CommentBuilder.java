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

package org.silverpeas.core.comment.test;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.ForeignPK;

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
    return new Comment(new CommentPK(String.valueOf(i++), COMPONENT_ID), RESOURCE_TYPE,
        new CommentPK(RESOURCE_ID, SPACE_ID, COMPONENT_ID), 1, author, text, now, now);
  }

  /**
   * Builds a comment with the specified author and with the specified comment text. The publication
   * on which the comment is is not set; the comment is orphelan.
   * @param author the author of the comment.
   * @param text the text of the comment.
   * @return a Comment instance.
   */
  public Comment buildOrphelanWith(final String author, final String text) {
    Date now = new Date();
    return new Comment(new CommentPK(String.valueOf(i++)), RESOURCE_TYPE,
        new CommentPK(RESOURCE_ID), 1, author, text, now, now);
  }

  /**
   * Gets the unique identifier of the resource to which comments are built by all of the
   * CommentBuilder instances.
   * @return the primary key of the commented resource.
   */
  public static ForeignPK getResourcePrimaryPK() {
    return new ForeignPK(RESOURCE_ID, COMPONENT_ID);
  }

  private CommentBuilder() {

  }
}
