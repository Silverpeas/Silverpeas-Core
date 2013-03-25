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

package com.stratelia.webactiv.util.questionContainer.ejb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

/**
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-question-container-embbed-datasource.xml" })
public class QuestionContainerDAOTest {

  public QuestionContainerDAOTest() {
  }

  @Inject
  private DataSource dataSource;

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet =
        new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
            QuestionContainerDAOTest.class.getResourceAsStream(
                "questioncontainer-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @BeforeClass
  public static void setUpClass() throws Exception {

  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getQuestionContainerHeaderFromResultSet method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetQuestionContainerHeaderFromResultSet() throws Exception {
//    ResultSet rs = null;
//    QuestionContainerPK questionContainerPK = null;
//    QuestionContainerHeader expResult = null;
    //    QuestionContainerHeader result =
    //        QuestionContainerDAO.getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
    //assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetQuestionContainers_Connection_QuestionContainerPK() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetQuestionContainers_Connection_List() throws Exception {
//    Connection con = null;
//    List<QuestionContainerPK> pks = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getQuestionContainers(con, pks);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getOpenedQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetOpenedQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getOpenedQuestionContainers(con, questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getNotClosedQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetNotClosedQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result =
//        QuestionContainerDAO.getNotClosedQuestionContainers(con, questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getClosedQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetClosedQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getClosedQuestionContainers(con, questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getInWaitQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetInWaitQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK qcPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getInWaitQuestionContainers(con, qcPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getQuestionContainerHeader method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetQuestionContainerHeader() throws Exception {
    String quizId = "2";
    String instanceId = "quizz83";
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(quizId, "", instanceId);
    QuestionContainerHeader result =
        QuestionContainerDAO.getQuestionContainerHeader(getConnection(), questionContainerPK);
    assertEquals("2012-01-12", result.getBeginDate());
    assertEquals("RAS", result.getComment());
    assertEquals("2012-01-12", result.getCreationDate());
    assertEquals("0", result.getCreatorId());
    assertEquals("Expression française sur les fruits et légumes", result.getDescription());
    assertEquals(0, result.getMaxTime());
    assertEquals("Quiz expression sur les légumes", result.getName());
    assertEquals(1, result.getNbMaxParticipations());
    assertEquals(0, result.getNbMaxPoints());
    assertEquals(1, result.getNbParticipationsBeforeSolution());
    assertEquals(1, result.getNbQuestionsPerPage());
    assertEquals(2, result.getNbVoters());
    assertEquals("Quiz expression sur les légumes", result.getTitle());
    assertEquals(1, result.getResultMode());
    assertEquals(4, result.getResultView());
  }

  /**
   * Test of closeQuestionContainer method, of class QuestionContainerDAO.
   */
  @Test
  public void testCloseQuestionContainer() throws Exception {
    String quizId = "1";
    String instanceId = "quizz83";
    Connection con = getConnection();
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(quizId, "", instanceId);
    QuestionContainerDAO.closeQuestionContainer(con, questionContainerPK);
    QuestionContainerHeader curQC =
        QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    assertEquals(curQC.isClosed(), true);
  }

  /**
   * Test of openQuestionContainer method, of class QuestionContainerDAO.
   */
  @Test
  public void testOpenQuestionContainer() throws Exception {
    String quizId = "3";
    String instanceId = "quizz83";
    Connection con = getConnection();
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(quizId, "", instanceId);
    QuestionContainerDAO.openQuestionContainer(con, questionContainerPK);
    QuestionContainerHeader curQC =
        QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    assertEquals(curQC.isClosed(), false);
  }

  /**
   * Test of createQuestionContainerHeader method, of class QuestionContainerDAO.
   */
  @Test
  public void testCreateQuestionContainerHeader() throws Exception {
//    Connection con = null;
//    QuestionContainerHeader questionContainerHeader = null;
//    QuestionContainerPK expResult = null;
//    QuestionContainerPK result =
//        QuestionContainerDAO.createQuestionContainerHeader(con, questionContainerHeader);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of updateQuestionContainerHeader method, of class QuestionContainerDAO.
   */
  @Test
  public void testUpdateQuestionContainerHeader() throws Exception {
//    Connection con = null;
//    QuestionContainerHeader questionContainerHeader = null;
//    QuestionContainerDAO.updateQuestionContainerHeader(con, questionContainerHeader);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of deleteQuestionContainerHeader method, of class QuestionContainerDAO.
   */
  @Test
  public void testDeleteQuestionContainerHeader() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    QuestionContainerDAO.deleteQuestionContainerHeader(con, questionContainerPK);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of addAVoter method, of class QuestionContainerDAO.
   */
  @Test
  public void testAddAVoter() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    QuestionContainerDAO.addAVoter(con, questionContainerPK);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of addComment method, of class QuestionContainerDAO.
   */
  @Test
  public void testAddComment() throws Exception {
//    Connection con = null;
//    Comment comment = null;
//    QuestionContainerDAO.addComment(con, comment);
    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
  }

  /**
   * Test of getComments method, of class QuestionContainerDAO.
   */
  @Test
  public void testGetComments() throws Exception {
//    Connection con = null;
//    QuestionContainerPK qcPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getComments(con, qcPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of deleteComments method, of class QuestionContainerDAO.
   */
  @Test
  public void testDeleteComments() throws Exception {
//    Connection con = null;
//    QuestionContainerPK qcPK = null;
//    QuestionContainerDAO.deleteComments(con, qcPK);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }
}
