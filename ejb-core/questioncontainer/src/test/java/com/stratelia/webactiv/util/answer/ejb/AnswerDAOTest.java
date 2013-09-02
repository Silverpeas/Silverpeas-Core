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

package com.stratelia.webactiv.util.answer.ejb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

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

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;

/**
 * @author ebonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-answer-embbed-datasource.xml" })
public class AnswerDAOTest {

  public AnswerDAOTest() {
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
            AnswerDAOTest.class.getResourceAsStream(
                "answer-dataset.xml")));
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
   * Test of getAnswersByQuestionPK method, of class AnswerDAO.
   */
  @Test
  public void testGetAnswersByQuestionPK() throws Exception {
    String questionId = "177";
    String componentId = "quizz83";
    ForeignPK questionPK = new ForeignPK(questionId, componentId);
    Collection<Answer> result = AnswerDAO.getAnswersByQuestionPK(getConnection(), questionPK);
    assertEquals(3, result.size());
  }

  /**
   * Test of recordThisAnswerAsVote method, of class AnswerDAO.
   */
  @Test
  public void testRecordThisAnswerAsVote() throws Exception {
    Connection con = null;
    ForeignPK questionPK = null;
    AnswerPK answerPK = null;
    //AnswerDAO.recordThisAnswerAsVote(con, questionPK, answerPK);
    // TODO review the generated test code and remove the default call to fail.
    //fail("The test case is a prototype.");
  }

  /**
   * Test of addAnswersToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testAddAnswersToAQuestion() throws Exception {
    Connection con = null;
    Collection<Answer> answers = null;
    ForeignPK questionPK = null;
    AnswerDAO.addAnswersToAQuestion(con, answers, questionPK);
    // TODO review the generated test code and remove the default call to fail.
    //fail("The test case is a prototype.");
  }

  /**
   * Test of addAnswerToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testAddAnswerToAQuestion() throws Exception {
    System.out.println("addAnswerToAQuestion");
    Connection con = null;
    Answer answer = null;
    ForeignPK questionPK = null;
    //AnswerDAO.addAnswerToAQuestion(con, answer, questionPK);
    // TODO review the generated test code and remove the default call to fail.
    //fail("The test case is a prototype.");
  }

  /**
   * Test of deleteAnswersToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testDeleteAnswersToAQuestion() throws Exception {
    ForeignPK questionPK = new ForeignPK("178", "quizz83");
    AnswerDAO.deleteAnswersToAQuestion(getConnection(), questionPK);
    Collection<Answer> answers = AnswerDAO.getAnswersByQuestionPK(getConnection(), questionPK);
    assertEquals(0, answers.size());
  }

  /**
   * Test of deleteAnswerToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testDeleteAnswerToAQuestion() throws Exception {
    String answerId = "1018";
    ForeignPK questionPK = new ForeignPK("178", "quizz83");
    AnswerDAO.deleteAnswerToAQuestion(getConnection(), questionPK, answerId);
    Collection<Answer> answers = AnswerDAO.getAnswersByQuestionPK(getConnection(), questionPK);
    assertEquals(2, answers.size());
  }

  /**
   * Test of updateAnswerToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testUpdateAnswerToAQuestion() throws Exception {
    Connection con = null;
    ForeignPK questionPK = null;
    Answer answer = null;
    //AnswerDAO.updateAnswerToAQuestion(con, questionPK, answer);
    // TODO review the generated test code and remove the default call to fail.
    //fail("The test case is a prototype.");
  }
}
