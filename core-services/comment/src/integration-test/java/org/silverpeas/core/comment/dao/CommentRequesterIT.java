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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.comment.dao;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.comment.dao.jdbc.JDBCCommentRequester;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.test.WarBuilder4Comment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.DateUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class CommentRequesterIT {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/comment/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/comment/comment-dataset.sql";

  private static final String DUMMY_COMMENT_ID = "newCommentId";
  private static final String DUMMY_INSTANCE_ID = "newInstanceId";
  private static final Date DUUMMY_DATE = java.sql.Date.valueOf("2015-01-01");

  @Inject
  private JDBCCommentRequester commentRequester;


  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  private Connection con;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Comment.onWarForTestClass(CommentRequesterIT.class).build();
  }

  @Before
  public void prepareTest() throws Exception {
    con = DBUtil.openConnection();
  }

  @After
  public void tearDown() {
    DBUtil.close(con);
  }

  @Test
  public void testCreateComment() throws Exception {
    CommentId id = new CommentId("kmelia18", null);
    String resourceType = "RtypeTest";
    ResourceReference resourceRef = new ResourceReference("200", "kmelia18");
    UserDetail author = aUser();
    String message = "A dummy message";
    Date creationDate = aDate();
    Comment cmt = new Comment(id, author.getId(), resourceType, resourceRef, creationDate);
    cmt.setMessage(message);
    Comment result = commentRequester.saveComment(con, cmt);
    assertNotNull(result);
    assertEquals("kmelia18", result.getComponentInstanceId());
    assertNotNull(result.getIdentifier().getLocalId());
    assertEquals("11", result.getIdentifier().getLocalId());
    Comment savedComment = commentRequester.getComment(con, result.getIdentifier());
    assertNotNull(savedComment);
    assertEquals(resourceType, savedComment.getResourceType());
    assertEquals(author.getId(), savedComment.getCreatorId());
    assertEquals(message, savedComment.getMessage());
    assertEquals(creationDate, savedComment.getCreationDate());
    assertEquals(creationDate, savedComment.getLastUpdateDate());
  }

  @Test
  public void testGetLast2Comments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId10", 2);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    assertEquals("12", comments.get(0).getCreatorId());
    assertEquals("1002", comments.get(0).getId());
    assertEquals("500", comments.get(0).getResourceReference().getLocalId());
    assertEquals("my comments are good", comments.get(0).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(0).getCreationDate());

    assertEquals("12", comments.get(1).getCreatorId());
    assertEquals("1001", comments.get(1).getId());
    assertEquals("500", comments.get(1).getResourceReference().getLocalId());
    assertEquals("my comments are good", comments.get(1).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(1).getCreationDate());
  }

  @Test
  public void testGetLastComments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId10", 0);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    assertEquals("12", comments.get(0).getCreatorId());
    assertEquals("1002", comments.get(0).getId());
    assertEquals("500", comments.get(0).getResourceReference().getLocalId());
    assertEquals("my comments are good", comments.get(0).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(0).getCreationDate());

    assertEquals("12", comments.get(1).getCreatorId());
    assertEquals("1001", comments.get(1).getId());
    assertEquals("500", comments.get(1).getResourceReference().getLocalId());
    assertEquals("my comments are good", comments.get(1).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(1).getCreationDate());

    assertEquals("10", comments.get(2).getCreatorId());
    assertEquals("1000", comments.get(2).getId());
    assertEquals("500", comments.get(2).getResourceReference().getLocalId());
    assertEquals("my comments", comments.get(2).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), comments.get(2).getCreationDate());
  }

  @Test
  public void testGetNoLastComments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId1000", 0);
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

 @Test
  public void testGetComment() throws Exception {
    CommentId id = new CommentId("instanceId10", "1000");
    Comment result = commentRequester.getComment(con, id);
    assertNotNull(result);
    assertEquals("10", result.getCreatorId());
    assertEquals("user10", result.getCreator().getDisplayedName());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(result.getCreationDate(), result.getLastUpdateDate());
  }

  @Test
  public void testDeleteComment() throws Exception {
    CommentId id = new CommentId("instanceId10", "1000");
    Comment result = commentRequester.getComment(con, id);
    assertNotNull(result);
    assertEquals("10", result.getCreatorId());
    assertEquals("user10", result.getCreator().getDisplayedName());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(result.getCreationDate(), result.getLastUpdateDate());
    commentRequester.deleteComment(con, id);
    result = commentRequester.getComment(con, id);
    assertNull(result);
  }

  @Test
  public void testUpdateComment() throws Exception {
    CommentId id = new CommentId("instanceId10", "1000");
    Comment comment = commentRequester.getComment(con, id);
    assertNotNull(comment);
    assertEquals("10", comment.getCreatorId());
    assertEquals("user10", comment.getCreator().getDisplayedName());
    assertEquals("my comments", comment.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), comment.getCreationDate());
    assertEquals(comment.getCreationDate(), comment.getLastUpdateDate());

    String newMessage = "A dummy message";
    Date modificationDate = aDate();
    comment.setMessage(newMessage);
    comment.setLastUpdateDate(modificationDate);
    commentRequester.updateComment(con, comment);
    Comment result = commentRequester.getComment(con, id);
    assertNotNull(result);
    assertEquals("user10", result.getCreator().getDisplayedName());
    assertEquals(newMessage, result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(modificationDate, result.getLastUpdateDate());
    assertEquals(id, result.getIdentifier());
  }

  @Test
  public void testMoveComment() throws Exception {
    // get the comment and check his attributes
    CommentId id = new CommentId("instanceId10", "1000");
    Comment result = commentRequester.getComment(con, id);
    assertNotNull(result);
    assertEquals("10", result.getCreatorId());
    assertEquals("user10", result.getCreator().getDisplayedName());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(result.getCreationDate(), result.getLastUpdateDate());
    assertEquals("500", result.getResourceReference().getLocalId());
    assertEquals("instanceId10", result.getIdentifier().getComponentInstanceId());
    assertEquals("1000", result.getIdentifier().getLocalId());

    // move to another resource of different type
    String srcResourceType = result.getResourceType();
    String targetResourceType = "RtypeTestTo";
    ResourceReference targetResourceRef = new ResourceReference(DUMMY_COMMENT_ID, DUMMY_INSTANCE_ID);
    commentRequester.moveComments(con, srcResourceType, result.getResourceReference(),
        targetResourceType, targetResourceRef);
    result = commentRequester.getComment(con, id);
    assertNotNull(result);
    assertEquals("10", result.getCreatorId());
    assertEquals("user10", result.getCreator().getDisplayedName());
    assertEquals("my comments", result.getMessage());
    assertEquals(targetResourceType, result.getResourceType());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(result.getCreationDate(), result.getLastUpdateDate());
    assertEquals(targetResourceRef.getLocalId(), result.getResourceReference().getLocalId());
    assertEquals(targetResourceRef.getComponentInstanceId(), result.getResourceReference().getComponentInstanceId());
    assertEquals("1000", result.getIdentifier().getLocalId());
    assertEquals(targetResourceRef.getComponentInstanceId(), result.getIdentifier().getComponentInstanceId());

    CommentId newId = new CommentId("instanceId10", "1001");
    result = commentRequester.getComment(con, newId);
    assertNotNull(result);
    assertEquals("12", result.getCreatorId());
    assertEquals("user12", result.getCreator().getDisplayedName());
    assertEquals("my comments are good", result.getMessage());
    assertEquals(targetResourceType, result.getResourceType());
    assertEquals(DateUtil.parseDate("2019/10/18"), result.getCreationDate());
    assertEquals(DateUtil.parseDate("2020/06/16"), result.getLastUpdateDate());
    assertEquals(targetResourceRef.getLocalId(), result.getResourceReference().getLocalId());
    assertEquals(targetResourceRef.getComponentInstanceId(), result.getResourceReference().getComponentInstanceId());
    assertEquals("1001", result.getIdentifier().getLocalId());
    assertEquals(targetResourceRef.getComponentInstanceId(), result.getIdentifier().getComponentInstanceId());
  }

  @Test
  public void testGetMostCommentedAllPublications() throws Exception {
    List<CommentedPublicationInfo> result =
        commentRequester.getMostCommentedAllPublications(con, null);
    assertNotNull(result);
    assertEquals(6, result.size());
    assertEquals(2, result.get(0).getCommentCount());
    assertEquals(1, result.get(1).getCommentCount());
  }

  @Test
  public void testGetMostCommentedAllPublicationsForAGivenResourceType() throws Exception {
    List<CommentedPublicationInfo> result =
        commentRequester.getMostCommentedAllPublications(con, "RtypeUniqueTest");
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getCommentCount());
    assertEquals(1, result.get(1).getCommentCount());
  }

  @Test
  public void testGetCommentsCount() throws Exception {
    ResourceReference resourceRef = new ResourceReference("500", "instanceId10");
    String srcResourceType = "RtypeTest";
    assertEquals(2, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));

    srcResourceType = "RtypeTestAutre";
    assertEquals(1, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));

    srcResourceType = "RtypeTestNull";
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));

    resourceRef = new ResourceReference(resourceRef.getLocalId(), "instanceId1");
    srcResourceType = "RtypeTestAutre";
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));

    resourceRef = new ResourceReference("50", "instanceId10");
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));

    resourceRef = new ResourceReference(null, resourceRef.getComponentInstanceId());
    assertEquals(1, commentRequester.getCommentsCount(con, srcResourceType, resourceRef));
  }

  @Test
  public void testGetAllComments() throws Exception {
    ResourceReference resourceRef = new ResourceReference("500", "instanceId10");
    String resourceType = "RtypeTest";
    List<Comment> comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    resourceType = "RtypeTestAutre";
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(1, comments.size());

    resourceType = "RtypeTestNull";
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    resourceRef = new ResourceReference(resourceRef.getLocalId(), "instanceId1");
    resourceType = "RtypeTestAutre";
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    resourceRef = new ResourceReference("50", "instanceId10");
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    resourceRef = new ResourceReference(null, resourceRef.getComponentInstanceId());
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(1, comments.size());

    resourceRef = new ResourceReference(null, null);
    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    resourceRef = new ResourceReference("500", "instanceId10");
    comments = commentRequester.getAllComments(con, null, resourceRef);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    resourceRef = new ResourceReference(null, resourceRef.getComponentInstanceId());
    comments = commentRequester.getAllComments(con, null, resourceRef);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    resourceRef = new ResourceReference("500", null);
    comments = commentRequester.getAllComments(con, null, resourceRef);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    boolean isIllegalArgumentException = false;
    resourceRef = new ResourceReference(null, null);
    try {
      commentRequester.getAllComments(con, null, resourceRef);
    } catch (IllegalArgumentException e) {
      isIllegalArgumentException = true;
    }
    assertTrue(isIllegalArgumentException);
  }

  @Test
  public void testDeleteAllComments() throws Exception {
    ResourceReference resourceRef = new ResourceReference("500", "instanceId10");
    String resourceType = "RtypeTest";
    List<Comment> comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    final int nbDeletes = commentRequester.deleteAllComments(con, resourceType, resourceRef);
    assertEquals(2, nbDeletes);

    comments = commentRequester.getAllComments(con, resourceType, resourceRef);
    assertNotNull(comments);
    assertEquals(0, comments.size());
  }

  @Test
  public void testDeleteAllCommentsOnNullValues() throws Exception {
    boolean isIllegalArgumentException = false;
    try {
      commentRequester.deleteAllComments(con, null, null);
    } catch (IllegalArgumentException e) {
      isIllegalArgumentException = true;
    }
    assertTrue(isIllegalArgumentException);
  }

  @Test
  public void testDeleteAllCommentsOnResourceTypeOnly() throws Exception {
    String resourceType = "RtypeTest";

    int nbDeletes = commentRequester.deleteAllComments(con, resourceType, null);
    assertEquals(3, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, resourceType, null);
    assertEquals(0, nbDeletes);
  }

  @Test
  public void testDeleteAllCommentsOnResourceIdOnly() throws Exception {
    ResourceReference resourceRef = new ResourceReference("500");

    int nbDeletes = commentRequester.deleteAllComments(con, null, resourceRef);
    assertEquals(3, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, null, resourceRef);
    assertEquals(0, nbDeletes);
  }

  @Test
  public void testDeleteAllCommentsOnInstanceIdOnly() throws Exception {
    ResourceReference resourceRef = new ResourceReference(null, "instanceId20");

    int nbDeletes = commentRequester.deleteAllComments(con, null, resourceRef);
    assertEquals(2, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, null, resourceRef);
    assertEquals(0, nbDeletes);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDataWithNoFiltersShouldNotWork() throws Exception {
    commentRequester.getSocialInformationComments(con, null, null, null, null);
  }

  @Test
  public void testGetSocialInformationCommentsReturningOneSocialInformationOfCreation()
      throws Exception {
    List<String> contactIds = List.of("10");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(1));

    SocialInformation socialInformationOfCreation = socialInformationList.get(0);
    assertThat(socialInformationOfCreation.getTitle(), nullValue());
    assertThat(socialInformationOfCreation.getDescription(), is("my comments"));
    assertThat(socialInformationOfCreation.getAuthor(), is("10"));
    assertThat(socialInformationOfCreation.getUrl(), nullValue());
    assertThat(socialInformationOfCreation.getDate(),
        is(java.sql.Date.valueOf("2019-10-15")));
    assertThat(socialInformationOfCreation.getType(),
        is(SocialInformationType.COMMENTPUBLICATION.name()));
    assertThat(socialInformationOfCreation.getIcon(), is("COMMENTPUBLICATION_new.gif"));
    assertThat(socialInformationOfCreation.isUpdated(), is(false));
  }

  @Test
  public void testGetSocialInformationCommentsForOneUser() throws Exception {
    List<String> contactIds = List.of("12");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(6));

    SocialInformation socialInformationOfCreation = socialInformationList.get(0);
    assertThat(socialInformationOfCreation.getTitle(), nullValue());
    assertThat(socialInformationOfCreation.getDescription(), notNullValue());
    assertThat(socialInformationOfCreation.getAuthor(), is("12"));
    assertThat(socialInformationOfCreation.getUrl(), nullValue());
    assertThat(socialInformationOfCreation.getDate(),
        is(java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationOfCreation.getType(),
        is(SocialInformationType.COMMENTPUBLICATION.name()));
    assertThat(socialInformationOfCreation.getIcon(), is("COMMENTPUBLICATION_update.gif"));
    assertThat(socialInformationOfCreation.isUpdated(), is(true));
  }

  @Test
  public void testGetSocialInformationCommentsFilteredOnUserId() throws Exception {
    List<String> contactIds = List.of("12");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(6));

    contactIds = List.of("10", "12");

    socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(7));
  }

  @Test
  public void testGetSocialInformationCommentsFilteredOnResourceTypes() throws Exception {

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, List.of("RtypeTest"), null, null, null);
    assertThat(socialInformationList, hasSize(3));

    socialInformationList =
        commentRequester.getSocialInformationComments(con, List.of("RtypeTest", "RtypeTestAutre"),
            null, null, null);
    assertThat(socialInformationList, hasSize(5));
  }

  @Test
  public void testGetSocialInformationCommentsFilteredOnInstanceId() throws Exception {

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, null, List.of("instanceId10"),
            null);
    assertThat(socialInformationList, hasSize(3));

    socialInformationList =
        commentRequester.getSocialInformationComments(con, null, null, List.of("instanceId20"),
            null);
    assertThat(socialInformationList, hasSize(2));

    socialInformationList = commentRequester.getSocialInformationComments(con, null, null,
        List.of("instanceId10", "instanceId20"), null);
    assertThat(socialInformationList, hasSize(5));
  }

  @Test
  public void testGetSocialInformationCommentsFilteredOnPeriod() throws Exception {

    // Period which the begin date equals the lowest date of registered comments and the end date
    // equals the greatest date of registered comments
    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, null, null,
            Period.between(LocalDate.parse("2019-10-15"), LocalDate.parse("2020-06-16")));
    assertThat(socialInformationList, hasSize(7));

    // Period which the begin date and end date are both equal to the lowest date of registered
    // comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.between(LocalDate.parse("2019-10-15"), LocalDate.parse("2019-10-15")));
    assertThat(socialInformationList, hasSize(1));

    // Period which the begin date and end date are both equal to the common creation date of
    // registered comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.between(LocalDate.parse("2019-10-18"), LocalDate.parse("2019-10-18")));
    assertThat(socialInformationList, hasSize(6));

    // Period which the begin date and end date are both equal to the greatest date of registered
    // comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.between(LocalDate.parse("2020-06-16"), LocalDate.parse("2020-06-16")));
    assertThat(socialInformationList, hasSize(6));

    // Period which the begin date and end date are both greater than the greatest date of
    // registered comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.between(LocalDate.parse("2020-06-17"), LocalDate.parse("2020-06-17")));
    assertThat(socialInformationList, empty());
  }

  @Test
  public void testGetSocialInformationCommentsAndApplyingAllFilters() throws Exception {
    List<String> contactIds = List.of("12");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, List.of("RtypeTest"), contactIds, null,
            Period.between(LocalDate.parse("2020-06-16"), LocalDate.parse("2020-06-16")));
    assertThat(socialInformationList, hasSize(2));

    socialInformationList =
        commentRequester.getSocialInformationComments(con, List.of("RtypeTest", "RtypeTestAutre"),
            contactIds, null,
            Period.between(LocalDate.parse("2020-06-16"), LocalDate.parse("2020-06-16")));
    assertThat(socialInformationList, hasSize(4));

    // Period which the begin date and end date are both greater than the greatest date of
    // registered comments
    socialInformationList =
        commentRequester.getSocialInformationComments(con, List.of("RtypeTest", "RtypeTestAutre"),
            contactIds, null,
            Period.between(LocalDate.parse("2020-06-17"), LocalDate.parse("2020-06-17")));
    assertThat(socialInformationList, empty());
  }

  private UserDetail aUser() {
    return UserDetail.getById("1");
  }

  private Date aDate() {
    return DUUMMY_DATE;
  }
}
