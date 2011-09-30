/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.comment.dao;

import java.util.Date;
import com.silverpeas.comment.dao.jdbc.JDBCCommentRequester;
import com.silverpeas.comment.mock.OrganizationControllerMocking;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.jcrutil.RandomGenerator;
import java.io.IOException;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import java.sql.Connection;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/spring-comment-dao.xml")
public class CommentDAOTest {

  private JDBCCommentRequester commentDAO = new JDBCCommentRequester();
  protected static SilverpeasJndiCase baseTest;
  
  @Inject
  private OrganizationControllerMocking organizationController;

  public CommentDAOTest() {
  }
  
  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName(RandomGenerator.getRandomString());
    user.setLastName((RandomGenerator.getRandomString()));
    user.setId(String.valueOf(RandomGenerator.getRandomInt()));
    organizationController.saveUser(user);
    return user;
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/silverpeas/comment/dao/comments-dataset.xml",
            "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    AbstractJndiCase.executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }
  
  @Before
  public void prepareTest() throws Exception {
    baseTest.setUp();
    UserDetail user = new UserDetail();
    user.setId("10");
    organizationController.saveUser(user);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testCreateComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
    CommentPK pk = new CommentPK(null, null, "kmelia18");
    ForeignPK foreignKey = new ForeignPK("200", "kmelia18");
    UserDetail author = aUser();
    String message = RandomGenerator.getRandomString();
    Date creationDate = aDate();
    Comment cmt = new Comment(pk, foreignKey, author.getId(), message, creationDate, null);
    CommentPK result = commentDAO.saveComment(con, cmt);
    assertNotNull(result);
    assertEquals("kmelia18", result.getInstanceId());
    assertNotNull(result.getId());
    assertEquals("11", result.getId());
    Comment savedComment = commentDAO.getComment(con, result);
    assertNotNull(savedComment);
    assertEquals(author.getId(), String.valueOf(savedComment.getOwnerId()));
    assertEquals(author.getDisplayedName(), savedComment.getOwner());
    assertEquals(message, savedComment.getMessage());
    assertEquals(creationDate, savedComment.getCreationDate());
    assertNull(savedComment.getModificationDate());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testGetComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testDeleteComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    commentDAO.deleteComment(con, pk);
    result = commentDAO.getComment(con, pk);
    assertNull(result);
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testUpdateComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment comment = commentDAO.getComment(con, pk);
    assertNotNull(comment);
    assertEquals(10, comment.getOwnerId());
    assertEquals("", comment.getOwner());
    assertEquals("my comments", comment.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), comment.getCreationDate());
    assertNull(comment.getModificationDate());
    String newMessage = RandomGenerator.getRandomString();
    Date modificationDate = aDate();
    ForeignPK foreignKey = new ForeignPK(String.valueOf(RandomGenerator.getRandomInt()),
            "instanceId" + RandomGenerator.getRandomInt());
    comment.setMessage(newMessage);
    comment.setModificationDate(modificationDate);
    comment.setCreationDate(modificationDate);
    comment.setForeignKey(foreignKey);
    commentDAO.updateComment(con, comment);
    Comment result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals("", result.getOwner());
    assertEquals(newMessage, result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertEquals(modificationDate, result.getModificationDate());
    assertNotNull(result.getForeignKey());
    assertEquals(foreignKey.getId(), result.getForeignKey().getId());
    assertEquals(pk, result.getCommentPK());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class JDBCCommentRequester.
   * @throws Exception
   */
  @Test
  public void testMoveComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals("500", result.getForeignKey().getId());
    assertEquals("instanceId10", result.getCommentPK().getInstanceId());
    assertEquals("1000", result.getCommentPK().getId());
    ForeignPK srcForeignKey = new ForeignPK(result.getForeignKey().getId(), "instanceId10");
    ForeignPK targetForeignKey = new ForeignPK(String.valueOf(RandomGenerator.getRandomInt()),
            "instanceId" + RandomGenerator.getRandomInt());
    commentDAO.moveComments(con, srcForeignKey, targetForeignKey);
    result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/15"), result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1000", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());

    pk = new CommentPK("1001", null, "instanceId10");
    result = commentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(12, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments are good", result.getMessage());
    assertEquals(DateUtil.parseDate("2019/10/18"), result.getCreationDate());
    assertEquals(DateUtil.parseDate("2020/06/16"), result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1001", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  private Date aDate() {
    com.silverpeas.calendar.Date date = new com.silverpeas.calendar.Date(RandomGenerator.
            getRandomCalendar().getTime());
    return date;
  }
}