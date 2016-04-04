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

package org.silverpeas.core.questioncontainer.container.dao;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.test.BasicWarBuilder;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class QuestionContainerDAOTest extends DataSetTest {

  public static final Operation TABLES_CREATION =
      Operations.sql("CREATE TABLE SB_QuestionContainer_QC " +
          "( " +
          "  qcId int PRIMARY KEY NOT NULL, " +
          "  qcTitle varchar (1000)  NOT NULL , " +
          "  qcDescription varchar (2000)  NULL , " +
          "  qcComment varchar (2000)  NULL , " +
          "  qcCreatorId varchar (100)  NOT NULL , " +
          "  qcCreationDate varchar (10)  NOT NULL , " +
          "  qcBeginDate varchar (10)  NOT NULL , " +
          "  qcEndDate varchar (10)  NOT NULL , " +
          "  qcIsClosed int    NOT NULL , " +
          "  qcNbVoters int    NOT NULL , " +
          "  qcNbQuestionsPage int    NOT NULL , " +
          "  qcNbMaxParticipations int    NULL , " +
          "  qcNbTriesBeforeSolution int    NULL , " +
          "  qcMaxTime int    NULL , " +
          "  instanceId varchar (50)  NOT NULL , " +
          "  anonymous int   NOT NULL , " +
          "  resultMode int   NOT NULL , " +
          "  resultView int   NOT NULL " +
          ")");

  public static final Operation DROP_ALL =
      Operations.sql("DROP TABLE IF EXISTS SB_QuestionContainer_QC");

  public static final Operation INSERT_DATA = Operations.insertInto("SB_QuestionContainer_QC")
      .columns("qcId", "qcTitle", "qcDescription", "qcComment", "qcCreatorId", "qcCreationDate",
          "qcBeginDate", "qcEndDate", "qcIsClosed", "qcNbVoters", "qcNbQuestionsPage",
          "qcNbMaxParticipations", "qcNbTriesBeforeSolution", "qcMaxTime", "instanceId",
          "anonymous", "resultMode", "resultView")
      .values(1, "Quiz express", "Description express du quiz", "Remarque express", "0",
          "2012-01-13", "2012-01-13", "9999-99-99", 0, 1, 1, 10, 2, 0, "quizz83", 0, 1, 4)
      .values(2, "Quiz expression sur les légumes",
          "Expression française sur les fruits et légumes", "RAS", "0", "2012-01-12", "2012-01-12",
          "9999-99-99", 0, 2, 1, 1, 1, 0, "quizz83", 0, 1, 4)
      .values(3, "Quiz clos", "Description d'un quizz à ouvrir", "RAS", "0", "2012-01-12",
          "2012-01-12", "9999-99-99", 1, 2, 1, 1, 1, 0, "quizz83", 0, 1, 4).build();

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLES_CREATION, INSERT_DATA);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(QuestionContainerDAOTest.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.questioncontainer.container")
              .addPackages(true, "org.silverpeas.core.questioncontainer.question")
              .addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
        })
        .build();
  }

  /**
   * Test of getQuestionContainerHeaderFromResultSet method, of class QuestionContainerDAO.
   */
  @Test
  @Ignore
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
  @Ignore
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
  @Ignore
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
  @Ignore
  public void testGetOpenedQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getOpenedQuestionContainers(con,
// questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getNotClosedQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  @Ignore
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
  @Ignore
  public void testGetClosedQuestionContainers() throws Exception {
//    Connection con = null;
//    QuestionContainerPK questionContainerPK = null;
//    Collection expResult = null;
//    Collection result = QuestionContainerDAO.getClosedQuestionContainers(con,
// questionContainerPK);
//    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }

  /**
   * Test of getInWaitQuestionContainers method, of class QuestionContainerDAO.
   */
  @Test
  @Ignore
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
    final QuestionContainerHeader result;
    try (Connection con = getConnection()) {
      result = QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    }
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
    final QuestionContainerHeader curQC;
    try (Connection con = getConnection()) {
      QuestionContainerPK questionContainerPK = new QuestionContainerPK(quizId, "", instanceId);
      QuestionContainerDAO.closeQuestionContainer(con, questionContainerPK);
      curQC = QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    }
    assertEquals(curQC.isClosed(), true);
  }

  /**
   * Test of openQuestionContainer method, of class QuestionContainerDAO.
   */
  @Test
  public void testOpenQuestionContainer() throws Exception {
    String quizId = "3";
    String instanceId = "quizz83";
    final QuestionContainerHeader curQC;
    try (Connection con = getConnection()) {
      QuestionContainerPK questionContainerPK = new QuestionContainerPK(quizId, "", instanceId);
      QuestionContainerDAO.openQuestionContainer(con, questionContainerPK);
      curQC = QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    }
    assertEquals(curQC.isClosed(), false);
  }

  /**
   * Test of createQuestionContainerHeader method, of class QuestionContainerDAO.
   */
  @Test
  @Ignore
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
  @Ignore
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
  @Ignore
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
  @Ignore
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
  @Ignore
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
  @Ignore
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
  @Ignore
  public void testDeleteComments() throws Exception {
//    Connection con = null;
//    QuestionContainerPK qcPK = null;
//    QuestionContainerDAO.deleteComments(con, qcPK);
    // TODO review the generated test code and remove the default call to fail.
    // fail("The test case is a prototype.");
  }
}
