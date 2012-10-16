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

package com.silverpeas.comment.service;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import javax.inject.Inject;

/**
 * A factory of CommentService objects. Its aim is to manage the life-cycle of such objects and so
 * to encapsulates from the CommentService client the adopted policy about that life-cycle.
 */
public class CommentServiceFactory {

  private static final CommentServiceFactory instance = new CommentServiceFactory();

  @Inject
  private CommentService commentService;

  /**
   * Gets an instance of this CommentServiceFactory class.
   * @return a CommentServiceFactory instance.
   */
  public static CommentServiceFactory getFactory() {
    return instance;
  }

  /**
   * Gets a CommentService instance.
   * @return a CommentService instance.
   */
  public CommentService getCommentService() {
    if (commentService == null) {
      SilverTrace.error("comment", getClass().getSimpleName() + ".getCommentService()",
          "EX_NO_MESSAGES", "IoC container not bootstrapped or no CommentService bean found!");
    }
    return commentService;
  }

  private CommentServiceFactory() {
  }
}
