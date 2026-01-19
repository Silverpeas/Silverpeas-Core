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
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.test.BasicWarBuilder;

import java.sql.Connection;

import static org.junit.Assert.*;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class QuestionContainerDAOIT extends DataSetTest {

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
    return BasicWarBuilder.onWarForTestClass(QuestionContainerDAOIT.class)
        .addMavenDependencies("org.silverpeas.core:silverpeas-core-api")
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addAsResource("org/silverpeas/util/logging")
        .addAsResource("org/silverpeas/jobStartPagePeas/settings/jobStartPagePeasSettings.properties")
        .testFocusedOn(war ->
            war.addPackages(true, "org.silverpeas.core.questioncontainer.container")
            .addPackages(true, "org.silverpeas.core.questioncontainer.question"))
        .build();
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
    assertEquals(java.sql.Date.valueOf("2012-01-12"), result.getCreationDate());
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
    assertTrue(curQC.isClosed());
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
    assertFalse(curQC.isClosed());
  }

}
