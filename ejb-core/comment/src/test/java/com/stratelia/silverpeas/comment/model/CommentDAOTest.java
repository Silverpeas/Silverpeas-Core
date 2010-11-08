/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.comment.model;

import org.junit.Before;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.jcrutil.RandomGenerator;
import java.io.IOException;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.DateUtil;
import java.sql.Connection;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class CommentDAOTest extends AbstractJndiCase {
  

  public CommentDAOTest() {
  }
  
  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/stratelia/silverpeas/comment/model/comments-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }
  
  /**
   * Test of createComment method, of class CommentDAO.
   * @throws Exception 
   */
  @Test
  public void testCreateComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    CommentPK pk = new CommentPK(null, null, "kmelia18");
    ForeignPK foreignKey = new ForeignPK("200", "kmelia18");
    int ownerId = RandomGenerator.getRandomInt();
    String owner = RandomGenerator.getRandomString();
    String message = RandomGenerator.getRandomString();
    String creationDate = DateUtil.date2SQLDate(RandomGenerator.getRandomCalendar().getTime());
    Comment cmt = new Comment(pk, foreignKey, ownerId, owner, message, creationDate, null);
    CommentPK result = CommentDAO.createComment(con, cmt);
    assertNotNull(result);
    assertEquals("kmelia18", result.getInstanceId());
    assertNotNull(result.getId());
    assertEquals("11", result.getId());
    Comment savedComment = CommentDAO.getComment(con, result);
    assertNotNull(savedComment);
    assertEquals(ownerId, savedComment.getOwnerId());
    assertEquals("", savedComment.getOwner());
    assertEquals(message, savedComment.getMessage());
    assertEquals(creationDate, savedComment.getCreationDate());
    assertNull(savedComment.getModificationDate());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class CommentDAO.
   * @throws Exception 
   */
  @Test
  public void testGetComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals("15/10/2019", result.getCreationDate());
    assertNull(result.getModificationDate());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class CommentDAO.
   * @throws Exception 
   */
  @Test
  public void testDeleteComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals("15/10/2019", result.getCreationDate());
    assertNull(result.getModificationDate());
    CommentDAO.deleteComment(con, pk);
    result = CommentDAO.getComment(con, pk);
    assertNull(result);
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of createComment method, of class CommentDAO.
   * @throws Exception 
   */
  @Test
  public void testUpdateComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment comment = CommentDAO.getComment(con, pk);
    assertNotNull(comment);
    assertEquals(10, comment.getOwnerId());
    assertEquals("", comment.getOwner());
    assertEquals("my comments", comment.getMessage());
    assertEquals("15/10/2019", comment.getCreationDate());
    assertNull(comment.getModificationDate());
    String newMessage = RandomGenerator.getRandomString();
    int newOwnerId = RandomGenerator.getRandomInt();
    String modificationDate = DateUtil.date2SQLDate(RandomGenerator.getRandomCalendar().getTime());
    ForeignPK foreignKey = new ForeignPK(String.valueOf(RandomGenerator.getRandomInt()),
        "instanceId" + RandomGenerator.getRandomInt());
    comment.setMessage(newMessage);
    comment.setOwnerId(newOwnerId);
    comment.setModificationDate(modificationDate);
    comment.setCreationDate(modificationDate);
    comment.setForeignKey(foreignKey);
    CommentDAO.updateComment(con, comment);
    Comment result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(newOwnerId, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals(newMessage, result.getMessage());
    assertEquals("15/10/2019", result.getCreationDate());
    assertEquals(modificationDate, result.getModificationDate());
    assertNotNull(result.getForeignKey());
    assertEquals(foreignKey.getId(), result.getForeignKey().getId());
    assertEquals(pk, result.getCommentPK());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
  
  
  /**
   * Test of createComment method, of class CommentDAO.
   * @throws Exception 
   */
  @Test
  public void testMoveComment() throws Exception {
    IDatabaseConnection dbConnection = baseTest.getConnection();
    Connection con = dbConnection.getConnection();
    CommentPK pk = new CommentPK("1000", null, "instanceId10");
    Comment result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals("15/10/2019", result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals("500", result.getForeignKey().getId());
    assertEquals( "instanceId10", result.getCommentPK().getInstanceId());
    assertEquals( "1000", result.getCommentPK().getId());
    ForeignPK srcForeignKey = new ForeignPK(result.getForeignKey().getId(), "instanceId10");
    ForeignPK targetForeignKey = new ForeignPK(String.valueOf(RandomGenerator.getRandomInt()),
        "instanceId" + RandomGenerator.getRandomInt());
    CommentDAO.moveComments(con, srcForeignKey, targetForeignKey);
    result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(10, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments", result.getMessage());
    assertEquals("15/10/2019", result.getCreationDate());
    assertNull(result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1000", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());
    
    pk = new CommentPK("1001", null, "instanceId10");
    result = CommentDAO.getComment(con, pk);
    assertNotNull(result);
    assertEquals(12, result.getOwnerId());
    assertEquals("", result.getOwner());
    assertEquals("my comments are good", result.getMessage());
    assertEquals("18/10/2019", result.getCreationDate());
    assertEquals("16/06/2020", result.getModificationDate());
    assertEquals(targetForeignKey.getId(), result.getForeignKey().getId());
    assertNull(result.getForeignKey().getInstanceId());
    assertEquals("1001", result.getCommentPK().getId());
    assertEquals(targetForeignKey.getComponentName(), result.getCommentPK().getInstanceId());
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }
}