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
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.silverpeas.comment.web.mock.AccessControllerMock;
import com.silverpeas.comment.web.mock.CommentServiceMock;
import com.stratelia.silverpeas.peasCore.SessionManagement;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * A builder of Sivlerpeas objects required for test fixture preparation.
 */
public abstract class BaseCommentResourceTest extends AbstractSpringAwareJerseyTest {

  protected static final String HEADER_SESSION_KEY = "X-Silverpeas-SessionKey";
  protected static final String COMPONENT_ID = "kmelia2";
  protected static final String CONTENT_ID = "1";
  protected static final String RESOURCE_PATH = "comments/" + COMPONENT_ID + "/" + CONTENT_ID;

  @Autowired
  private SessionManagement sessionManager;
  @Autowired
  private AccessControllerMock accessController;
  @Autowired
  private CommentServiceMock commentService;

  public BaseCommentResourceTest() {
    super(new WebAppDescriptor.Builder("com.silverpeas.comment.web").contextPath("silverpeas").
        contextParam("contextConfigLocation", "classpath:/spring-comment-webservice.xml").
        requestListenerClass(org.springframework.web.context.request.RequestContextListener.class).
        servletClass(SpringServlet.class).contextListenerClass(ContextLoaderListener.class).
        build());
  }

  @Before
  public void checkDependencyInjection() {
    assertNotNull(sessionManager);
    assertNotNull(accessController);
    assertNotNull(commentService);
  }

  /**
   * Authenticates the user to use in the tests.
   * @param theUser the user to authenticate.
   * @return the key of the opened session.
   */
  protected String authenticate(final UserDetail theUser) {
    commentService.addUserForComments(theUser);
    HttpSession httpSession = mock(HttpSession.class);
    when(httpSession.getId()).thenReturn(UUID.randomUUID().toString());
    SessionInfo session = new SessionInfo(httpSession, "localhost", theUser);
    return sessionManager.openSession(session);
  }

  /**
   * Denies the access to the silverpeas resources to all users.
   */
  protected void denieAuthorizationToUsers() {
    accessController.setAuthorization(false);
  }

  /**
   * Creates a new user.
   * @return a new user.
   */
  protected UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId("2");
    return user;
  }

  /**
   * Gets a comment builder for the specified user.
   * @param user the user for which a comment builder is provided.
   * @return a comment builder.
   */
  protected CommentBuilder aUser(final UserDetail user) {
    return new CommentBuilder().withUser(user);
  }

  /**
   * The builder of comments for testing purpose.
   */
  protected class CommentBuilder {

    private String resourceId;
    private String componentId;
    private UserDetail user;

    public CommentBuilder withUser(final UserDetail user) {
      this.user = user;
      return this;
    }

    public CommentBuilder commentTheResource(String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public CommentBuilder inComponent(String componentId) {
      this.componentId = componentId;
      return this;
    }

    public Comment withAsText(String theText) {
      Date now = new Date();
      Comment comment = new Comment(new CommentPK("", componentId), new PublicationPK(
          resourceId, componentId), Integer.valueOf(user.getId()), user.getDisplayedName(), theText, now.
          toString(), now.toString());
      comment.setOwnerDetail(user);
      commentService.createComment(comment);
      return comment;
    }
  }
}
