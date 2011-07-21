/*
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.personalization.service.MockablePersonalizationService;
import javax.inject.Inject;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.web.mock.DefaultCommentServiceMock;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.Date;
import com.silverpeas.rest.RESTWebServiceTest;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * A builder of Sivlerpeas objects required for test fixture preparation.
 */
public abstract class BaseCommentResourceTest extends RESTWebServiceTest {

  protected static final String COMPONENT_INSTANCE_ID = "kmelia2";
  protected static final String CONTENT_ID = "1";
  protected static final String RESOURCE_PATH = "comments/" + COMPONENT_INSTANCE_ID + "/" + CONTENT_ID;
  @Autowired
  private DefaultCommentServiceMock commentService;
  @Inject
  private MockablePersonalizationService personalisationService;

  /**
   * Gets the comment service used in tests.
   * @return the comment service used in tests.
   */
  public CommentService getCommentService() {
    return commentService;
  }

  public BaseCommentResourceTest() {
    super("com.silverpeas.comment.web", "spring-comment-webservice.xml");    
  }

  @Before
  public void checkCommentServiceMocking() {
    assertNotNull(commentService);
    assertNotNull(personalisationService);
    PersonalizationService mockService = mock(PersonalizationService.class);
    UserPreferences settings = new UserPreferences();
    settings.setLanguage("fr");
    when(mockService.getUserSettings(anyString())).thenReturn(settings);
    personalisationService.setPersonalizationService(mockService);
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
              resourceId, componentId), Integer.valueOf(user.getId()), user.getDisplayedName(),
              theText,
              now.toString(), now.toString());
      comment.setOwnerDetail(user);
      return comment;
    }

    public Comment andSaveItWithAsText(String theText) {
      Comment comment = withAsText(theText);
      commentService.createComment(comment);
      return comment;
    }
  }
}
