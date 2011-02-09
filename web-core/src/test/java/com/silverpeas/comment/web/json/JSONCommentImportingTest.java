/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withReader Free/Libre
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
 * along withReader this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.web.json;

import java.util.List;
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.web.CommentEntity;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.comment.web.json.JSONCommentFields.*;
import static com.silverpeas.comment.web.CommentMatcher.*;
import static com.silverpeas.export.ImportDescriptor.*;

/**
 * Unit tests on the importing from JSON of comment instances.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-comment-webservice.xml")
public class JSONCommentImportingTest {

  private static final String commentId = "2";
  private static final String componentId = "kmelia2";
  private static final String publicationId = "2";
  private static final String userId = "3";
  private static final String userName = "toto";
  private static int counter = 0;
  @Inject
  private JSONCommentImporter jsonImporter;

  public JSONCommentImportingTest() {
  }

  @Before
  public void checkDependenciesInjection() {
    assertNotNull(jsonImporter);
  }

  @Test
  public void importFromJSONASingleComment() throws Exception {
    Comment aComment = aCommentOf(aUserWithId(userId));
    String theCommentInJson = aJSONRepresentationOf(aComment);
    System.out.println("JSON:\n" + theCommentInJson);
    StringReader reader = new StringReader(theCommentInJson);
    List<CommentEntity> comments = jsonImporter.importFrom(withReader(reader));
    assertThat(comments, hasSize(1));
    assertThat(comments.get(0), matches(CommentEntity.fromComment(aComment)));
  }

  @Test
  public void importFromJSONSeveralComments() throws Exception {
    Comment aComment1 = aCommentOf(aUserWithId(userId));
    Comment aComment2 = aCommentOf(aUserWithId(userId + "1"));
    Comment aComment3 = aCommentOf(aUserWithId(userId + "2"));
    String theCommentInJson = aJSONRepresentationOf(aComment1, aComment2, aComment3);
    System.out.println("JSON:\n" + theCommentInJson);
    StringReader reader = new StringReader(theCommentInJson);
    List<CommentEntity> comments = jsonImporter.importFrom(withReader(reader));
    assertThat(comments, hasSize(3));
    assertThat(comments.get(0), matches(CommentEntity.fromComment(aComment1)));
    assertThat(comments.get(1), matches(CommentEntity.fromComment(aComment2)));
    assertThat(comments.get(2), matches(CommentEntity.fromComment(aComment3)));
  }

  private UserDetail aUserWithId(String id) {
    UserDetail writer = new UserDetail();
    writer.setId(id);
    writer.setFirstName("Toto");
    writer.setLastName("Chez-les-" + id + "-papoos");
    writer.setLogin("toto" + id);
    return writer;
  }

  private Comment aCommentOf(UserDetail user) {
    String offset = String.valueOf(counter++);
    Comment aComment = new Comment(new CommentPK(commentId + offset, componentId),
        new PublicationPK(publicationId, componentId), Integer.valueOf(user.getId()),
        user.getDisplayedName(), "Ceci est un commentaire " + offset, "11/12/2002", "11/12/2002");
    aComment.setOwnerDetail(user);
    return aComment;
  }

  private String aJSONRepresentationOf(Comment... comments) throws IOException {
    List<Map<String, Object>> arrayOfComments = new ArrayList<Map<String, Object>>();
    for (Comment aComment : comments) {
      Map<String, Object> comment = new HashMap<String, Object>();
      Map<String, String> user = new HashMap<String, String>();

      UserDetail userDetail = aComment.getOwnerDetail();
      user.put(WRITER_ID, userDetail.getId());
      user.put(WRITER_NAME, userDetail.getDisplayedName());

      comment.put(COMMENT_URI, "http://localhost/silverpeas/comments/" +
          aComment.getCommentPK().getId());
      comment.put(WRITER, user);
      comment.put(COMMENT_ID, aComment.getCommentPK().getId());
      comment.put(COMPONENT_ID, aComment.getCommentPK().getInstanceId());
      comment.put(RESOURCE_ID, aComment.getForeignKey().getId());
      comment.put(TEXT, aComment.getMessage());
      comment.put(CREATION_DATE, aComment.getCreationDate());
      comment.put(MODIFICATION_DATE, aComment.getModificationDate());

      arrayOfComments.add(comment);
    }

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    if (arrayOfComments.size() == 1) {
      mapper.writeValue(writer, arrayOfComments.get(0));
    } else if (arrayOfComments.size() > 1) {
      mapper.writeValue(writer, arrayOfComments);
    }

    return writer.toString();
  }
}