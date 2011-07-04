/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.web.mock.DefaultCommentServiceMock;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Resources required by all the unit tests on the comment web resource.
 */
@Named
public class CommentTestResources {

  public static final String JAVA_PACKAGE = "com.silverpeas.comment.web";
  public static final String SPRING_CONTEXT = "spring-comment-webservice.xml";
  public static final String COMPONENT_INSTANCE_ID = "kmelia2";
  public static final String CONTENT_ID = "1";
  public static final String RESOURCE_PATH = "comments/" + COMPONENT_INSTANCE_ID + "/"
          + CONTENT_ID;
  @Autowired
  private DefaultCommentServiceMock commentService;

  /**
   * Gets the comment service used in tests.
   * @return the comment service used in tests.
   */
  public CommentService getCommentService() {
    return commentService;
  }

  /**
   * Gets a comment builder for the specified user.
   * @param user the user for which a comment builder is provided.
   * @return a comment builder.
   */
  public static CommentBuilder theUser(final UserDetail user) {
    return new CommentBuilder().withUser(user);
  }

  public void save(final Comment ... comments) {
    for (Comment comment : comments) {
      commentService.createComment(comment);
    }
  }
}
