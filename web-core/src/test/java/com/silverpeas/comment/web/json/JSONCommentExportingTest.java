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
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.web.json;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.MockablePersonalizationService;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.web.CommentEntity;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static com.silverpeas.comment.web.json.JSONCommentMatcher.*;
import static com.silverpeas.export.ExportDescriptor.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the exporting in JSON of comment instances.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-comment-webservice.xml")
public class JSONCommentExportingTest {

  private static final String commentId = "2";
  private static final String componentId = "kmelia2";
  private static final String publicationId = "2";
  private static final String userId = "3";
  private static final String userName = "toto";
  private static int counter = 0;
  @Inject
  private JSONCommentExporter jsonExporter;
  @Inject
  private MockablePersonalizationService personalisationService;

  public JSONCommentExportingTest() {
  }

  @Before
  public void checkDependenciesInjection() {
    assertNotNull(jsonExporter);
    assertNotNull(personalisationService);
    PersonalizationService mockService = mock(PersonalizationService.class);
    UserPreferences settings = new UserPreferences();
    settings.setLanguage("fr");
    when(mockService.getUserSettings(anyString())).thenReturn(settings);
    personalisationService.setPersonalizationService(mockService);
  }

  @Test
  public void exportACommentInJSON() throws Exception {
    CommentEntity aComment = aCommentOf(aUserWithId(userId));
    StringWriter writer = new StringWriter();
    jsonExporter.export(withWriter(writer), aComment);
    System.out.println(writer.toString());
    assertThat(writer.toString(), represents(aComment));
  }

  @Test
  public void exportSeveralCommentsInJSON() throws Exception {
    CommentEntity aComment1 = aCommentOf(aUserWithId(userId));
    CommentEntity aComment2 = aCommentOf(aUserWithId(userId + "1"));
    CommentEntity aComment3 = aCommentOf(aUserWithId(userId));

    StringWriter writer = new StringWriter();
    jsonExporter.export(withWriter(writer), aComment1, aComment2, aComment3);
    assertThat(writer.toString(), represents(anArrayOf(aComment1, aComment2, aComment3)));
  }

  private UserDetail aUserWithId(String id) {
    UserDetail writer = new UserDetail();
    writer.setId(id);
    writer.setFirstName("Toto");
    writer.setLastName("Chez-les-" + id + "-papoos");
    writer.setLogin("toto" + id);
    return writer;
  }

  private CommentEntity aCommentOf(UserDetail user) throws URISyntaxException {
    String offset = String.valueOf(counter++);
    Comment aComment = new Comment(new CommentPK(commentId + offset, componentId),
            new PublicationPK(publicationId, componentId), Integer.valueOf(user.getId()),
            user.getDisplayedName(), "Ceci est un commentaire " + offset, "2002/11/12", "2002/11/12");
    aComment.setOwnerDetail(user);
    CommentEntity entity = CommentEntity.fromComment(aComment).
            withURI(new URI("http://localhost/silverpeas/services/comments/" + commentId));
    return entity;
  }
}
