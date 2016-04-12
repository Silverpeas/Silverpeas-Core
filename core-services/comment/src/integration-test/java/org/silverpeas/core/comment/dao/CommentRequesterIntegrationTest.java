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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.comment.dao;

import org.silverpeas.core.comment.dao.jdbc.JDBCCommentRequester;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.test.WarBuilder4Comment;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.ForeignPK;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class CommentRequesterIntegrationTest {

  private final JDBCCommentRequester commentRequester = new JDBCCommentRequester();

  private static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/comment/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/comment/comment-dataset.sql";

  private final String DUMMY_COMMENT_ID = "newCommentId";
  private final String DUMMY_INSTANCE_ID = "newInstanceId";
  private final Date DUUMMY_DATE = java.sql.Date.valueOf("2015-01-01");

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  private Connection con;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Comment
        .onWarForTestClass(CommentRequesterIntegrationTest.class)
        .build();
  }

  @Before
  public void prepareTest() throws Exception {
    con = DBUtil.openConnection();
  }

  @After
  public void tearDown() {
    DBUtil.close(con);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testCreateComment() throws Exception {
    CommentPK pk = new CommentPK(null, null, "kmelia18");
    String resourceType = "RtypeTest";
    ForeignPK foreignKey = new ForeignPK("200", "kmelia18");
    UserDetail author = aUser();
    String message = "A dummy message";
    Date creationDate = aDate();
    Comment cmt =
        new Comment(pk, resourceType, foreignKey, author.getId(), message, creationDate, null);
    CommentPK result = commentRequester.saveComment(con, cmt);
    assertNotNull(result);
    assertEquals("kmelia18", result.getInstanceId());
    assertNotNull(result.getId());
    assertEquals("11", result.getId());
    Comment savedComment = commentRequester.getComment(con, result);
    assertNotNull(savedComment);
    assertEquals(resourceType, savedComment.getResourceType());
    assertEquals(author.getId(), String.valueOf(savedComment.getOwnerId()));
    assertEquals(author.getDisplayedName(), savedComment.getOwner());
    assertEquals(message, savedComment.getMessage());
    assertEquals(creationDate, savedComment.getCreationDate());
    assertNull(savedComment.getModificationDate());
  }

  @Test
  public void testGetLast2Comments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId10", 2);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    assertEquals(12, comments.get(0).getOwnerId());
    assertEquals("1002", comments.get(0).getId());
    assertEquals("500", comments.get(0).getForeignKey().getId());
    assertEquals("my comments are good", comments.get(0).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(0).getCreationDate());

    assertEquals(12, comments.get(1).getOwnerId());
    assertEquals("1001", comments.get(1).getId());
    assertEquals("500", comments.get(1).getForeignKey().getId());
    assertEquals("my comments are good", comments.get(1).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(1).getCreationDate());
  }

  @Test
  public void testGetLastComments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId10", 0);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    assertEquals(12, comments.get(0).getOwnerId());
    assertEquals("1002", comments.get(0).getId());
    assertEquals("500", comments.get(0).getForeignKey().getId());
    assertEquals("my comments are good", comments.get(0).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(0).getCreationDate());

    assertEquals(12, comments.get(1).getOwnerId());
    assertEquals("1001", comments.get(1).getId());
    assertEquals("500", comments.get(1).getForeignKey().getId());
    assertEquals("my comments are good", comments.get(1).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), comments.get(1).getCreationDate());

    assertEquals(10, comments.get(2).getOwnerId());
    assertEquals("1000", comments.get(2).getId());
    assertEquals("500", comments.get(2).getForeignKey().getId());
    assertEquals("my comments", comments.get(2).getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), comments.get(2).getCreationDate());
  }

  @Test
  public void testGetNoLastComments() throws Exception {
    List<Comment> comments = commentRequester.getLastComments(con, "instanceId1000", 0);
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetComment() throws Exception {
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("user10", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteComment() throws Exception {
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("user10", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    commentRequester.deleteComment(con, pk);
    result = commentRequester.getComment(con, pk);
    assertNull(result);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testUpdateComment() throws Exception {
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment comment = commentRequester.getComment(con, pk);
    assertNotNull(comment);
    assertEquals(10, comment.getOwnerId());
    assertEquals("user10", comment.getOwner());
    assertEquals("my comments", comment.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), comment.getCreationDate());
    assertNull(comment.getModificationDate());
    String newResourceType = "RtypeTestUpdate";
    String newMessage = "A dummy message";
    Date modificationDate = aDate();
    ForeignPK foreignKey = new ForeignPK(DUMMY_COMMENT_ID, DUMMY_INSTANCE_ID);
    comment.setMessage(newMessage);
    comment.setModificationDate(modificationDate);
    comment.setCreationDate(modificationDate);
    comment.setForeignKey(foreignKey);
    comment.setResourceType(newResourceType);
    commentRequester.updateComment(con, comment);
    Comment result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals("user10", result.getOwner());
    assertEquals(newMessage, result.getMessage());
    assertEquals(newResourceType, result.getResourceType());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(modificationDate, result.getModificationDate());
    assertNotNull(result.getForeignKey());
    assertEquals(foreignKey.getId(), result.getForeignKey().getId());
    assertEquals(pk, result.getCommentPK());
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testMoveComment() throws Exception {
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("user10", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals("500", result.getForeignKey().getId());
    assertEquals("instanceId10", result.getCommentPK().getInstanceId());
    assertEquals("1000", result.getCommentPK().getId());
    String srcResourceType = "RtypeTest";
    String targetResourceType = "RtypeTestTo";
    ForeignPK srcForeignKey = new ForeignPK(result.getForeignKey().getId(), "instanceId10");
    ForeignPK targetForeignKey = new ForeignPK(DUMMY_COMMENT_ID, DUMMY_INSTANCE_ID);
    commentRequester
        .moveComments(con, srcResourceType, srcForeignKey, targetResourceType, targetForeignKey);
    result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("user10", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(targetResourceType, result.getResourceType());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1000", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());

    pk = new CommentPK("1001", null, "instanceId10");
    result = commentRequester.getComment(con, pk);
    assertNotNull(result);
    assertEquals(12, result.getOwnerId());
    assertEquals("user12", result.getOwner());
    assertEquals("my comments are good", result.getMessage());
    assertEquals(targetResourceType, result.getResourceType());
    assertEquals(DateUtil.parseDate("2019/10/18"), result.getCreationDate());
    assertEquals(DateUtil.parseDate("2020/06/16"), result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1001", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());
  }

  /**
   * Test of getMostCommentedAllPublications method, of class JDBCCommentRequester.
   * @throws Exception
   */
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

  /**
   * Test of getCommentsCount method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetCommentsCount() throws Exception {
    ForeignPK foreignKey = new ForeignPK("500", "instanceId10");
    String srcResourceType = "RtypeTest";
    assertEquals(2, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));

    srcResourceType = "RtypeTestAutre";
    assertEquals(1, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));

    srcResourceType = "RtypeTestNull";
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));

    foreignKey.setComponentName("instanceId1");
    srcResourceType = "RtypeTestAutre";
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));

    foreignKey = new ForeignPK("50", "instanceId10");
    assertEquals(0, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));

    foreignKey.setId(null);
    assertEquals(1, commentRequester.getCommentsCount(con, srcResourceType, foreignKey));
  }

  /**
   * Test of getAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetAllComments() throws Exception {
    ForeignPK foreignKey = new ForeignPK("500", "instanceId10");
    String resourceType = "RtypeTest";
    List<Comment> comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    resourceType = "RtypeTestAutre";
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(1, comments.size());

    resourceType = "RtypeTestNull";
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    foreignKey.setComponentName("instanceId1");
    resourceType = "RtypeTestAutre";
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    foreignKey = new ForeignPK("50", "instanceId10");
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(0, comments.size());

    foreignKey.setId(null);
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(1, comments.size());

    foreignKey.setComponentName(null);
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    resourceType = null;
    foreignKey = new ForeignPK("500", "instanceId10");
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    foreignKey.setId(null);
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    foreignKey.setId("500");
    foreignKey.setComponentName(null);
    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(3, comments.size());

    boolean isIllegalArgumentException = false;
    foreignKey.setId(null);
    try {
      commentRequester.getAllComments(con, resourceType, foreignKey);
    } catch (IllegalArgumentException e) {
      isIllegalArgumentException = true;
    }
    assertTrue(isIllegalArgumentException);
  }

  /**
   * Test of deleteAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteAllComments() throws Exception {
    ForeignPK foreignKey = new ForeignPK("500", "instanceId10");
    String resourceType = "RtypeTest";
    List<Comment> comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(2, comments.size());

    final int nbDeletes = commentRequester.deleteAllComments(con, resourceType, foreignKey);
    assertEquals(2, nbDeletes);

    comments = commentRequester.getAllComments(con, resourceType, foreignKey);
    assertNotNull(comments);
    assertEquals(0, comments.size());
  }

  /**
   * Test of deleteAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
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

  /**
   * Test of deleteAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteAllCommentsOnResourceTypeOnly() throws Exception {
    String resourceType = "RtypeTest";

    int nbDeletes = commentRequester.deleteAllComments(con, resourceType, null);
    assertEquals(3, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, resourceType, null);
    assertEquals(0, nbDeletes);
  }

  /**
   * Test of deleteAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteAllCommentsOnResourceIdOnly() throws Exception {
    ForeignPK foreignKey = new ForeignPK("500");

    int nbDeletes = commentRequester.deleteAllComments(con, null, foreignKey);
    assertEquals(3, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, null, foreignKey);
    assertEquals(0, nbDeletes);
  }

  /**
   * Test of deleteAllComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteAllCommentsOnInstanceIdOnly() throws Exception {
    ForeignPK foreignKey = new ForeignPK(null, "instanceId20");

    int nbDeletes = commentRequester.deleteAllComments(con, null, foreignKey);
    assertEquals(2, nbDeletes);
    nbDeletes = commentRequester.deleteAllComments(con, null, foreignKey);
    assertEquals(0, nbDeletes);
  }

  /**
   * Test of getSocialInformationCommentsListByUserId method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetDataWithNoFiltersShouldNotWork() throws Exception {
    commentRequester.getSocialInformationComments(con, null, null, null, null);
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsReturningOneSocialInformationOfCreation()
      throws Exception {
    List<String> contactIds = asList("10");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(1));

    SocialInformation socialInformationOfCreation = socialInformationList.get(0);
    assertThat(socialInformationOfCreation.getTitle(), nullValue());
    assertThat(socialInformationOfCreation.getDescription(), is("my comments"));
    assertThat(socialInformationOfCreation.getAuthor(), is("10"));
    assertThat(socialInformationOfCreation.getUrl(), nullValue());
    assertThat(socialInformationOfCreation.getDate(),
        is((Date) java.sql.Date.valueOf("2019-10-15")));
    assertThat(socialInformationOfCreation.getType(),
        is(SocialInformationType.COMMENTPUBLICATION.name()));
    assertThat(socialInformationOfCreation.getIcon(), is("COMMENTPUBLICATION_new.gif"));
    assertThat(socialInformationOfCreation.isUpdated(), is(false));
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsForOneUser() throws Exception {
    List<String> contactIds = asList("12");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(6));

    SocialInformation socialInformationOfCreation = socialInformationList.get(0);
    assertThat(socialInformationOfCreation.getTitle(), nullValue());
    assertThat(socialInformationOfCreation.getDescription(), notNullValue());
    assertThat(socialInformationOfCreation.getAuthor(), is("12"));
    assertThat(socialInformationOfCreation.getUrl(), nullValue());
    assertThat(socialInformationOfCreation.getDate(),
        is((Date) java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationOfCreation.getType(),
        is(SocialInformationType.COMMENTPUBLICATION.name()));
    assertThat(socialInformationOfCreation.getIcon(), is("COMMENTPUBLICATION_update.gif"));
    assertThat(socialInformationOfCreation.isUpdated(), is(true));
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsFilteredOnUserId() throws Exception {
    List<String> contactIds = asList("12");

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(6));

    contactIds = asList("10", "12");

    socialInformationList =
        commentRequester.getSocialInformationComments(con, null, contactIds, null, null);
    assertThat(socialInformationList, hasSize(7));
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsFilteredOnResourceTypes() throws Exception {

    List<? extends SocialInformation> socialInformationList =
        commentRequester.getSocialInformationComments(con, asList("RtypeTest"), null, null, null);
    assertThat(socialInformationList, hasSize(3));

    socialInformationList = commentRequester
        .getSocialInformationComments(con, asList("RtypeTest", "RtypeTestAutre"), null, null, null);
    assertThat(socialInformationList, hasSize(5));
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsFilteredOnInstanceId() throws Exception {

    List<? extends SocialInformation> socialInformationList = commentRequester
        .getSocialInformationComments(con, null, null, asList("instanceId10"), null);
    assertThat(socialInformationList, hasSize(3));

    socialInformationList = commentRequester
        .getSocialInformationComments(con, null, null, asList("instanceId20"), null);
    assertThat(socialInformationList, hasSize(2));

    socialInformationList = commentRequester
        .getSocialInformationComments(con, null, null, asList("instanceId10", "instanceId20"),
            null);
    assertThat(socialInformationList, hasSize(5));
  }

  /**
   * Test of getSocialInformationComments method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsFilteredOnPeriod() throws Exception {

    // Period which the begin date equals the lowest date of registered comments and the end date
    // equals the greatest date of registered comments
    List<? extends SocialInformation> socialInformationList = commentRequester
        .getSocialInformationComments(con, null, null, null,
            Period.from(java.sql.Date.valueOf("2019-10-15"), java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationList, hasSize(7));

    // Period which the begin date and end date are both equal to the lowest date of registered
    // comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.from(java.sql.Date.valueOf("2019-10-15"), java.sql.Date.valueOf("2019-10-15")));
    assertThat(socialInformationList, hasSize(1));

    // Period which the begin date and end date are both equal to the common creation date of
    // registered comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.from(java.sql.Date.valueOf("2019-10-18"), java.sql.Date.valueOf("2019-10-18")));
    assertThat(socialInformationList, hasSize(6));

    // Period which the begin date and end date are both equal to the greatest date of registered
    // comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.from(java.sql.Date.valueOf("2020-06-16"), java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationList, hasSize(6));

    // Period which the begin date and end date are both greater than the greatest date of
    // registered comments
    socialInformationList = commentRequester.getSocialInformationComments(con, null, null, null,
        Period.from(java.sql.Date.valueOf("2020-06-17"), java.sql.Date.valueOf("2020-06-17")));
    assertThat(socialInformationList, empty());
  }

  /**
   * Test of getSocialInformationCommentsListByUserId method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetSocialInformationCommentsAndApplyingAllFilters() throws Exception {
    List<String> contactIds = asList("12");

    List<? extends SocialInformation> socialInformationList = commentRequester
        .getSocialInformationComments(con, asList("RtypeTest"), contactIds, null,
            Period.from(java.sql.Date.valueOf("2020-06-16"), java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationList, hasSize(2));

    socialInformationList = commentRequester
        .getSocialInformationComments(con, asList("RtypeTest", "RtypeTestAutre"), contactIds, null,
            Period.from(java.sql.Date.valueOf("2020-06-16"), java.sql.Date.valueOf("2020-06-16")));
    assertThat(socialInformationList, hasSize(4));

    // Period which the begin date and end date are both greater than the greatest date of
    // registered comments
    socialInformationList = commentRequester
        .getSocialInformationComments(con, asList("RtypeTest", "RtypeTestAutre"), contactIds, null,
            Period.from(java.sql.Date.valueOf("2020-06-17"), java.sql.Date.valueOf("2020-06-17")));
    assertThat(socialInformationList, empty());
  }

  private UserDetail aUser() {
    return UserDetail.getById("1");
  }

  private Date aDate() {
    return new org.silverpeas.core.date.Date(DUUMMY_DATE);
  }
}
