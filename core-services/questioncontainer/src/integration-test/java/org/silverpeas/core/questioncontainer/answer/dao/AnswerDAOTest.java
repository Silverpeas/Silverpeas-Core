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

package org.silverpeas.core.questioncontainer.answer.dao;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;

import java.sql.Connection;
import java.util.Collection;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class AnswerDAOTest extends DataSetTest {

  public static final Operation TABLES_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS SB_Question_Answer" +
          "(" +
          "answerId int PRIMARY KEY NOT NULL ," +
          "questionId  int  NOT NULL ," +
          "answerLabel  varchar (1000) NULL," +
          "answerNbPoints  int  NULL ," +
          " answerIsSolution int  NOT NULL ," +
          " answerComment  varchar (2000) NULL ," +
          " answerNbVoters  int  NOT NULL ," +
          " answerIsOpened  int  NOT NULL ," +
          " answerImage  varchar (100) NULL ," +
          " answerQuestionLink varchar (100) NULL" +
          ")");

  public static final Operation DROP_ALL =
      Operations.sql("DROP TABLE IF EXISTS SB_Question_Answer");

  public static final Operation INSERT_DATA = Operations.insertInto("SB_Question_Answer")
      .columns("answerid", "questionid", "answerlabel", "answernbpoints", "answerissolution",
          "answercomment", "answernbvoters", "answerisopened", "answerimage", "answerquestionlink")
      .values(1015, 177, "Pommes", 3, 1, "", 1, 0, null, null)
      .values(1016, 177, "Choux", 0, 0, "", 0, 0, null, null)
      .values(1017, 177, "Prunes", 0, 0, "", 0, 0, null, null)
      .values(1018, 178, "Pêche", 0, 0, "", 0, 0, null, null)
      .values(1019, 178, "Poire", 3, 1, "", 1, 0, null, null)
      .values(1020, 178, "Fraise", 0, 0, "", 0, 0, null, null)
      .values(1021, 179, "Tomate", 0, 0, "", 0, 0, null, null)
      .values(1022, 179, "Pomme de terre", 0, 0, "", 0, 0, null, null)
      .values(1023, 179, "Patate", 3, 1, "", 1, 0, null, null).build();

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLES_CREATION, INSERT_DATA);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(AnswerDAOTest.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> war.addPackages(true, "org.silverpeas.core.questioncontainer.answer")
            .addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF"))
        .build();
  }

  /**
   * Test of getAnswersByQuestionPK method, of class AnswerDAO.
   */
  @Test
  public void testGetAnswersByQuestionPK() throws Exception {
    Connection con = getConnection();
    String questionId = "177";
    String componentId = "quizz83";
    ForeignPK questionPK = new ForeignPK(questionId, componentId);
    try {
      Collection<Answer> result = AnswerDAO.getAnswersByQuestionPK(con, questionPK);
      Assert.assertEquals(3, result.size());
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of recordThisAnswerAsVote method, of class AnswerDAO.
   */
  @Test
  @Ignore
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
  @Ignore
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
  @Ignore
  public void testAddAnswerToAQuestion() throws Exception {
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
    Connection con = getConnection();
    ForeignPK foreignPK = new ForeignPK("178", "quizz83");
    try (Connection otherCon = getConnection()) {
      AnswerDAO.deleteAnswersToAQuestion(otherCon, foreignPK);
      Collection<Answer> answers = AnswerDAO.getAnswersByQuestionPK(con, foreignPK);
      Assert.assertEquals(0, answers.size());
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteAnswerToAQuestion method, of class AnswerDAO.
   */
  @Test
  public void testDeleteAnswerToAQuestion() throws Exception {
    Connection con = getConnection();
    String answerId = "1018";
    ForeignPK foreignPK = new ForeignPK("178", "quizz83");
    try (Connection otherCon = getConnection()) {
      AnswerDAO.deleteAnswerToAQuestion(otherCon, foreignPK, answerId);
      Collection<Answer> answers = AnswerDAO.getAnswersByQuestionPK(con, foreignPK);
      Assert.assertEquals(2, answers.size());
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateAnswerToAQuestion method, of class AnswerDAO.
   */
  @Test
  @Ignore
  public void testUpdateAnswerToAQuestion() throws Exception {
    Connection con = getConnection();
    ForeignPK questionPK = null;
    Answer answer = null;
    //AnswerDAO.updateAnswerToAQuestion(con, questionPK, answer);
    // TODO review the generated test code and remove the default call to fail.
    //fail("The test case is a prototype.");
  }
}
