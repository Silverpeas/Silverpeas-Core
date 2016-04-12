/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.questioncontainer.container.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.questioncontainer.container.dao.QuestionContainerDAO;
import org.silverpeas.core.questioncontainer.container.model.Comment;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.answer.service.AnswerService;
import org.silverpeas.core.questioncontainer.answer.dao.AnswerDAO;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.questioncontainer.question.service.QuestionService;
import org.silverpeas.core.questioncontainer.question.dao.QuestionDAO;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;
import org.silverpeas.core.questioncontainer.result.dao.QuestionResultDAO;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.score.service.ScoreService;
import org.silverpeas.core.questioncontainer.score.dao.ScoreDAO;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.questioncontainer.score.model.ScorePK;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stateless service to manage question container.
 * @author neysseri
 */
@Singleton
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class DefaultQuestionContainerService
    implements QuestionContainerService, ComponentInstanceDeletion {

  private QuestionService questionService;
  private QuestionResultService questionResultService;
  private AnswerService answerService;
  private ScoreService scoreService;
  // if beginDate is null, it will be replace in database with it
  private static final String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private static final String nullEndDate = "9999/99/99";

  public DefaultQuestionContainerService() {
    questionService = QuestionService.get();
    questionResultService = QuestionResultService.get();
    answerService = AnswerService.get();
    scoreService = ScoreService.get();
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainers(
      QuestionContainerPK questionContainerPK) {

    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
      return this.setNbMaxPoints(questionContainerHeaders);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainers()",
          SilverpeasRuntimeException.ERROR,

          "questionContainer.GETTING_QUESTIONCONTAINER_LIST_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainerHeaders(
      List<QuestionContainerPK> pks) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getQuestionContainerHeaders()",
            "root.MSG_GEN_ENTER_METHOD", "pks = " + pks.toString());
    try (Connection con = getConnection()) {
      return QuestionContainerDAO.getQuestionContainers(con, pks);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainerHeaders()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_LIST_FAILED", e);
    }
  }

  private Collection<QuestionContainerHeader> setNbMaxPoints(
      Collection<QuestionContainerHeader> questionContainerHeaders) {

    QuestionService questionService = this.questionService;
    Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
    List<QuestionContainerHeader> result = new ArrayList<>();

    while (it.hasNext()) {
      int nbMaxPoints = 0;
      QuestionContainerHeader questionContainerHeader = it.next();
      QuestionPK questionPK = new QuestionPK(null, questionContainerHeader.getPK());
      Collection<Question> questions;
      try {
        questions =
            questionService.getQuestionsByFatherPK(questionPK, questionContainerHeader.getPK().getId());
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "DefaultQuestionContainerService.setNbMaxPoints()", SilverpeasRuntimeException.ERROR,
            "questionContainer.SETTING_NB_MAX_POINTS_TO_QUESTIONCONTAINER_FAILED", e);
      }
      for (Question question : questions) {
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
      result.add(questionContainerHeader);
    }
    return result;
  }

  private QuestionContainerHeader setNbMaxPoint(QuestionContainerHeader questionContainerHeader) {

    int nbMaxPoints = 0;
    QuestionService questionService = this.questionService;
    Collection<Question> questions;
    QuestionPK questionPK = new QuestionPK(null, questionContainerHeader.getPK());
    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerHeader.getPK().
          getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.setNbMaxPoint()",
          SilverpeasRuntimeException.ERROR, "questionContainer.GETTING_QUESTIONS_FAILED", e);
    }
    for (Question question : questions) {
      nbMaxPoints += question.getNbPointsMax();
    }
    questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    return questionContainerHeader;
  }

  @Override
  public Collection<QuestionContainerHeader> getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getNotClosedQuestionContainers(con, questionContainerPK);
      return this.setNbMaxPoints(questionContainerHeaders);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getNotClosedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_NOT_CLOSED_QUESTIONCONTAINERS_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getOpenedQuestionContainers()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getOpenedQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getOpenedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_OPENED_QUESTIONCONTAINERS_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId) {
    try (Connection con = getConnection()) {
    Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getOpenedQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<>(questionContainerHeaders.size());
      for (QuestionContainerHeader questionContainerHeader : questionContainerHeaders) {
        questionContainerHeader
            .setScores(this.getUserScoresByFatherId(questionContainerHeader.getPK(), userId));
        result.add(setNbMaxPoint(questionContainerHeader));
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getOpenedQuestionContainersAndUserScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_OPENED_QUESTIONCONTAINERS_AND_USER_SCORES_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK) {
    SilverTrace.
        info("questionContainer",
            "DefaultQuestionContainerService.getQuestionContainersWithScores()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<>(questionContainerHeaders.size());
      for (QuestionContainerHeader questionContainerHeader : questionContainerHeaders) {
        Collection<ScoreDetail> scoreDetails = getScoresByFatherId(questionContainerHeader.getPK());
        if (scoreDetails != null) {
          questionContainerHeader.setScores(scoreDetails);
          result.add(setNbMaxPoint(questionContainerHeader));
        }
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainersWithScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINERS_AND_SCORES_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
      Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
      List<QuestionContainerHeader> result = new ArrayList<>();

      while (it.hasNext()) {
        QuestionContainerHeader questionContainerHeader = it.next();
        Collection<ScoreDetail> scoreDetails =
            this.getUserScoresByFatherId(questionContainerHeader.getPK(), userId);
        if (scoreDetails != null) {
          questionContainerHeader.setScores(scoreDetails);
          result.add(this.setNbMaxPoint(questionContainerHeader));
        }
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainersWithUserScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINERS_AND_USER_SCORES_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getClosedQuestionContainers()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getClosedQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getClosedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_CLOSED_QUESTIONCONTAINERS_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getInWaitQuestionContainers()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getInWaitQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getInWaitQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_INWAIT_QUESTIONCONTAINERS_FAILED", e);
    }
  }

  @Override
  public Collection<ScoreDetail> getUserScoresByFatherId(QuestionContainerPK questionContainerPK,
      String userId) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getUserScoresByFatherId()",
            "root.MSG_GEN_ENTER_METHOD",
            "questionContainerPK = " + questionContainerPK + ", userId = " + userId);
    Collection<ScoreDetail> scores;
    ScoreService scoreService = this.scoreService;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreService.getUserScoresByFatherId(scorePK, questionContainerPK.getId(), userId);
      if (scores != null) {
        SilverTrace
            .info("questionContainer", "DefaultQuestionContainerService.getUserScoresByFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "Le score : Size=" + scores.size());
      } else {
        SilverTrace
            .info("questionContainer", "DefaultQuestionContainerService.getUserScoresByFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "Le score : null");
      }
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getUserScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_SCORES_FAILED", e);
    }
    return scores;
  }

  @Override
  public Collection<ScoreDetail> getBestScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbBestScores) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getBestScoresByFatherId()",
            "root.MSG_GEN_ENTER_METHOD",
            "questionContainerPK = " + questionContainerPK + ", nbBestScores = " + nbBestScores);
    Collection<ScoreDetail> scores;
    ScoreService scoreService = this.scoreService;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      scores = scoreService.getBestScoresByFatherId(scorePK, nbBestScores, questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getBestScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_BEST_SCORES_FAILED", e);
    }
  }

  @Override
  public Collection<ScoreDetail> getWorstScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbScores) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getWorstScoresByFatherId()",
            "root.MSG_GEN_ENTER_METHOD",
            "questionContainerPK = " + questionContainerPK + ", nbScores = " + nbScores);
    Collection<ScoreDetail> scores;
    ScoreService scoreService = this.scoreService;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      scores = scoreService.getWorstScoresByFatherId(scorePK, nbScores, questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getWorstScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_WORST_SCORES_FAILED", e);
    }
  }

  @Override
  public Collection<ScoreDetail> getScoresByFatherId(QuestionContainerPK questionContainerPK) {

    Collection<ScoreDetail> scores;
    ScoreService scoreService = this.scoreService;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      scores = scoreService.getScoresByFatherId(scorePK, questionContainerPK.getId());
      if (scores != null) {
        SilverTrace
            .info("questionContainer", "DefaultQuestionContainerService.getScoresByFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "Le score : Size=" + scores.size());
      } else {
        SilverTrace
            .info("questionContainer", "DefaultQuestionContainerService.getScoresByFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "Le score : null");
      }
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getScoresByFatherId()", SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_SCORES_FAILED", e);
    }
  }

  @Override
  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getAverageScoreByFatherId()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    float averageScore;
    ScoreService scoreService = this.scoreService;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      averageScore = scoreService.getAverageScoreByFatherId(scorePK, questionContainerPK.getId());
      return averageScore;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getAverageScoreByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_AVERAGE_SCORE_FAILED", e);
    }
  }

  @Override
  public QuestionContainerDetail getQuestionContainer(QuestionContainerPK questionContainerPK,
      String userId) {
    Collection<Question> questions;
    Collection<Comment> comments;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionService questionService = this.questionService;
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;
    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      for (Question question : questions) {
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.GETTING_QUESTIONCONTAINER_FAILED",
          e);
    }
    userVotes = getUserVotesToQuestionContainer(userId, questionContainerPK);
    comments = getComments(questionContainerPK);

    return new QuestionContainerDetail(questionContainerHeader, questions, comments, userVotes);
  }

  @Override
  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.getQuestionContainerHeader()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    QuestionContainerHeader questionContainerHeader = null;
    try (Connection con = getConnection()) {

      questionContainerHeader =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.GETTING_QUESTIONCONTAINER_FAILED",
          e);
    }
    return questionContainerHeader;
  }

  @Override
  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId) {
    Collection<Question> questions;
    Collection<Comment> comments;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes = null;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionService questionService = this.questionService;
    QuestionResultService questionResultService = this.questionResultService;
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;

    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      for (Question question : questions) {
        userVotes = questionResultService.getUserQuestionResultsToQuestionByParticipation(userId,
            new ForeignPK(question.getPK()), participationId);
        question.setQuestionResults(userVotes);
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getQuestionContainerByParticipationId()",
          SilverpeasRuntimeException.ERROR, "questionContainer.GETTING_QUESTIONCONTAINER_FAILED",
          e);
    }

    comments = getComments(questionContainerPK);

    return new QuestionContainerDetail(questionContainerHeader, questions, comments, userVotes);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void closeQuestionContainer(QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.closeQuestionContainer()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    try (Connection con = getConnection()) {

      // begin PDC integration
      QuestionContainerHeader qc =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, false);
      // end PDC integration
      QuestionContainerDAO.closeQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.closeQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.CLOSING_QUESTIONCONTAINER_FAILED",
          e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void openQuestionContainer(QuestionContainerPK questionContainerPK) {

    try (Connection con = getConnection()) {
      // begin PDC integration
      QuestionContainerHeader qc =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, true);
      // end PDC integration
      QuestionContainerDAO.openQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.openQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.OPENING_QUESTIONCONTAINER_FAILED",
          e);
    }
  }

  @Override
  public int getNbVotersByQuestionContainer(QuestionContainerPK questionContainerPK) {
    int nbVoters;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    ScoreService scoreService = this.scoreService;
    try {
      nbVoters = scoreService.getNbVotersByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getNbVotersByQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_NUMBER_OF_VOTES_FAILED", e);
    }
    return nbVoters;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply) {
    SimpleDateFormat formatterDB = new java.text.SimpleDateFormat("yyyy/MM/dd");
    QuestionPK questionPK;
    AnswerPK answerPK;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    QuestionResult result;
    QuestionResultService questionResultService = this.questionResultService;
    AnswerService answerService = this.answerService;
    QuestionService questionService = this.questionService;
    ScoreService scoreService = this.scoreService;
    Answer answer;
    int participationId =
        scoreService.getUserNbParticipationsByFatherId(scorePK, questionContainerPK.getId(), userId) + 1;
    int questionUserScore;
    int userScore = 0;

    for (String questionId : reply.keySet()) {
      questionUserScore = 0;

      questionPK = new QuestionPK(questionId, questionContainerPK);
      Question question;

      try {
        question = questionService.getQuestion(questionPK);
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
            SilverpeasRuntimeException.ERROR,
            "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
      }

      List<String> answers = reply.get(questionId);
      String answerId;
      int vectorSize = answers.size();
      int newVectorSize = vectorSize;
      int vectorBegin = 0;
      // Treatment of the first vector element to know if the clue has been read
      String cluePenalty = answers.get(0);
      int penaltyValue = 0;

      if (cluePenalty.startsWith("PC")) {
        // It's a clue penalty field
        penaltyValue = Integer.parseInt(cluePenalty.substring(2, cluePenalty.length()));
        vectorBegin = 1;
      }

      // Treatment of the last vector element to know if the answer is opened
      String openedAnswer = answers.get(vectorSize - 1);

      if (openedAnswer.startsWith("OA")) {
        // It's an open answer, Fetch the matching answerId
        answerId = answers.get(vectorSize - 2);
        openedAnswer = openedAnswer.substring(2, openedAnswer.length());

        // User Score for this question
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;

        newVectorSize = vectorSize - 2;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result =
            new QuestionResult(null, new ForeignPK(questionPK), answerPK, userId, openedAnswer);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        try {
          questionResultService.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
        }
        try {
          // Add this vote to the corresponding answer
          answerService.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
        }
      }

      for (int i = vectorBegin; i < newVectorSize; i++) {
        answerId = answers.get(i);
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result = new QuestionResult(null, new ForeignPK(questionPK), answerPK, userId, null);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        try {
          questionResultService.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
        }
        try {
          // Add this vote to the corresponding answer
          answerService.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
        }
      }
      if (question.getNbPointsMax() < questionUserScore) {
        questionUserScore = question.getNbPointsMax();
      } else if (question.getNbPointsMin() > questionUserScore) {
        questionUserScore = question.getNbPointsMin();
      }
      userScore += questionUserScore;
    }

    // Record the UserScore
    ScoreDetail scoreDetail =
        new ScoreDetail(scorePK, questionContainerPK.getId(), userId, participationId,
            formatterDB.format(new java.util.Date()), userScore, 0, "");

    scoreService.addScore(scoreDetail);

    try (Connection con = getConnection()) {
      // Increment the number of voters
      QuestionContainerDAO.addAVoter(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.recordReplyToQuestionContainerByUser()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED", e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply, String comment, boolean isAnonymousComment) {
    recordReplyToQuestionContainerByUser(questionContainerPK, userId, reply);
    addComment(questionContainerPK, userId, comment, isAnonymousComment);
  }

  private void addComment(QuestionContainerPK questionContainerPK, String userId, String comment,
      boolean isAnonymousComment) {
    try (Connection con = getConnection()) {
      Comment c = new Comment(null, questionContainerPK, userId, comment, isAnonymousComment, null);
      QuestionContainerDAO.addComment(con, c);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.addComment()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.ADDING_QUESTIONCONTAINER_COMMENT_FAILED", e);
    }
  }

  @Override
  public QuestionContainerPK createQuestionContainer(QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.createQuestionContainer()",
            "root.MSG_GEN_ENTER_METHOD",
            "questionContainerPK = " + questionContainerPK + ", questionContainerDetail = " +
                questionContainerDetail + ", userId = " + userId);
    QuestionContainerHeader questionContainerHeader = questionContainerDetail.getHeader();
    questionContainerHeader.setPK(questionContainerPK);
    questionContainerHeader.setCreatorId(userId);
    try (Connection con = getConnection()) {
      questionContainerPK =
          QuestionContainerDAO.createQuestionContainerHeader(con, questionContainerHeader);
      questionContainerHeader.setPK(questionContainerPK);
      QuestionContainerContentManager
          .createSilverContent(con, questionContainerHeader, userId, true);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.createQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.CREATING_QUESTIONCONTAINER_FAILED",
          e);
    }
    QuestionService questionService = this.questionService;
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    Collection<Question> questions = questionContainerDetail.getQuestions();
    List<Question> q = new ArrayList<>(questions.size());
    for (Question question : questions) {
      question.setPK(questionPK);
      q.add(question);
    }

    try {
      questionService.createQuestions(q, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.createQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.CREATING_QUESTIONCONTAINER_FAILED",
          e);
    }
    createIndex(questionContainerHeader);
    return questionContainerPK;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void updateQuestionContainerHeader(QuestionContainerHeader questionContainerHeader) {
    try (Connection con = getConnection()) {
      QuestionContainerDAO.updateQuestionContainerHeader(con, questionContainerHeader);
      // start PDC integration
      QuestionContainerContentManager.updateSilverContentVisibility(questionContainerHeader, true);
      // end PDC integration
      createIndex(questionContainerHeader);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.updateQuestionContainerHeader()",
          SilverpeasRuntimeException.ERROR, "questionContainer.UPDATING_QUESTIONCONTAINER_FAILED",
          e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection<Question> questions) {

    QuestionService questionService = this.questionService;
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    for (Question question : questions) {
      question.setPK(questionPK);
    }
    try {
      // delete all old questions
      questionService.deleteQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      // replace it with new ones
      questionService.createQuestions(questions, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.updateQuestions()", SilverpeasRuntimeException.ERROR,
          "questionContainer.UPDATING_QUESTIONCONTAINER_QUESTIONS_FAILED", e);
    }
  }

  @Override
  public void deleteVotes(QuestionContainerPK questionContainerPK) {

    ScorePK scorePK = new ScorePK(questionContainerPK.getId(), questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());
    QuestionService questionService = this.questionService;
    ScoreService scoreService = this.scoreService;
    QuestionResultService questionResultService = this.questionResultService;

    try (Connection con = getConnection()) {
      QuestionContainerHeader qch = getQuestionContainerHeader(questionContainerPK);
      // mise a zero du nombre de participation
      qch.setNbVoters(0);
      updateQuestionContainerHeader(qch);
      scoreService.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());

      // delete comments
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      // get all questions to delete results
      Collection<Question> questions =
          questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      if (questions != null && !questions.isEmpty()) {
        for (Question question : questions) {
          QuestionPK questionPKToDelete = question.getPK();
          // delete all results
          questionResultService.deleteQuestionResultsToQuestion(new ForeignPK(questionPKToDelete));
          Collection<Answer> answers = question.getAnswers();
          Collection<Answer> newAnswers = new ArrayList<>();
          for (Answer answer : answers) {
            answer.setNbVoters(0);
            newAnswers.add(answer);
          }
          question.setAnswers(newAnswers);
          questionService.updateQuestion(question);
        }
      }

    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.deleteVotes()",
          SilverpeasRuntimeException.ERROR, "questionContainer.DELETING_QUESTIONCONTAINER_FAILED",
          e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK) {
    SilverTrace
        .info("questionContainer", "DefaultQuestionContainerService.deleteQuestionContainer()",
            "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    ScorePK scorePK = new ScorePK(questionContainerPK.getId(), questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());
    try {
      scoreService.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.DELETING_QUESTIONCONTAINER_FAILED",
          e);
    }
    try {
      questionService.deleteQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      deleteIndex(questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.DELETING_QUESTIONCONTAINER_FAILED",
          e);
    }
    try (Connection con = getConnection()) {
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      QuestionContainerDAO.deleteQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.deleteSilverContent(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.DELETING_QUESTIONCONTAINER_FAILED",
          e);
    }
  }

  private Collection<Comment> getComments(QuestionContainerPK questionContainerPK) {

    try (Connection con = getConnection()) {
      return QuestionContainerDAO.getComments(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.getComments()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_COMMENTS_FAILED", e);
    }
  }

  @Override
  public Collection<QuestionResult> getSuggestions(QuestionContainerPK questionContainerPK) {

    Collection<QuestionResult> suggestions;
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());
    QuestionResultService questionResultService = this.questionResultService;

    try {
      suggestions = questionResultService.getQuestionResultToQuestion(new ForeignPK(questionPK));
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getSuggestions()", SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_SUGGESTIONS_FAILED", e);
    }
    return suggestions;
  }

  @Override
  public QuestionResult getSuggestion(String userId, QuestionPK questionPK, AnswerPK answerPK) {
    QuestionResult suggestion;
    QuestionResultService questionResultService = this.questionResultService;

    try {
      suggestion =
          questionResultService.getUserAnswerToQuestion(userId, new ForeignPK(questionPK), answerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.getSuggestion()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_SUGGESTIONS_FAILED", e);
    }
    return suggestion;
  }

  private Collection<QuestionResult> getUserVotesToQuestionContainer(String userId,
      QuestionContainerPK questionContainerPK) {
    Collection<QuestionResult> votes = null;
    QuestionPK questionPK = new QuestionPK("unknown", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionService questionService = this.questionService;
    Collection<Question> questions;

    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getUserVotesToQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_RESPONSES_FAILED", e);
    }
    for (Question question : questions) {
      QuestionResultService questionResultService = this.questionResultService;
      try {
        votes = questionResultService
            .getUserQuestionResultsToQuestion(userId, new ForeignPK(question.getPK()));
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "DefaultQuestionContainerService.getUserVotesToQuestionContainer()",
            SilverpeasRuntimeException.ERROR,
            "questionContainer.GETTING_QUESTIONCONTAINER_USER_RESPONSES_FAILED", e);
      }
      if (!votes.isEmpty()) {
        break;
      }
    }
    return votes;
  }

  @Override
  public float getAveragePoints(QuestionContainerPK questionContainerPK) {

    float averagePoints;
    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    ScoreService scoreService = this.scoreService;

    try {
      averagePoints = scoreService.getAverageScoreByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getAveragePoints()", SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_AVERAGE_SCORES_FAILED", e);
    }
    return averagePoints;
  }

  @Override
  public int getUserNbParticipationsByFatherId(QuestionContainerPK questionContainerPK,
      String userId) {
    int nbPart;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    ScoreService scoreService = this.scoreService;

    try {
      nbPart =
          scoreService.getUserNbParticipationsByFatherId(scorePK, questionContainerPK.getId(), userId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getUserNbParticipationsByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_NB_PARTICIPATION_TO_USER_FAILED", e);
    }
    return nbPart;
  }

  @Override
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId) {
    ScoreDetail scoreDetail;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    ScoreService scoreService = this.scoreService;

    try {
      scoreDetail = scoreService
          .getUserScoreByFatherIdAndParticipationId(scorePK, questionContainerPK.getId(), userId,
              participationId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getUserScoreByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_SCORE_TO_A_PARTICIPATION_FAILED", e);
    }
    return scoreDetail;
  }

  @Override
  public void updateScore(QuestionContainerPK questionContainerPK, ScoreDetail scoreDetail) {
    try {
      scoreService.updateScore(scoreDetail);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.updateScore()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.UPDATING_QUESTIONCONTAINER_SCORE_FAILED", e);
    }
  }

  /**
   * Called on :
   * <ul>
   * <li>createQuestionContainer()</li>
   * <li>updateQuestionContainer()</li>
   * </ul>
   * @param header the question container to index.
   */
  private void createIndex(QuestionContainerHeader header) {

    FullIndexEntry indexEntry = null;

    if (header != null) {
      // Index the QuestionContainerHeader
      indexEntry = new FullIndexEntry(header.getPK().getComponentName(), "QuestionContainer",
          header.getPK().getId());
      indexEntry.setTitle(header.getTitle());
      indexEntry.setPreView(header.getDescription());
      indexEntry.setCreationDate(header.getCreationDate());
      indexEntry.setCreationUser(header.getCreatorId());
      if (header.getBeginDate() == null) {
        indexEntry.setStartDate(nullBeginDate);
      } else {
        indexEntry.setStartDate(header.getBeginDate());
      }
      if (header.getEndDate() == null) {
        indexEntry.setEndDate(nullEndDate);
      } else {
        indexEntry.setEndDate(header.getEndDate());
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Called on : - deleteQuestionContainer()
   */
  @Override
  public void deleteIndex(QuestionContainerPK pk) {

    IndexEntryKey indexEntry =
        new IndexEntryKey(pk.getComponentName(), "QuestionContainer", pk.getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public int getSilverObjectId(QuestionContainerPK pk) {

    int silverObjectId;
    try {
      silverObjectId =
          QuestionContainerContentManager.getSilverObjectId(pk.getId(), pk.getComponentName());
      if (silverObjectId == -1) {
        QuestionContainerHeader questionContainerHeader = getQuestionContainerHeader(pk);
        silverObjectId = QuestionContainerContentManager
            .createSilverContent(null, questionContainerHeader,
                questionContainerHeader.getCreatorId(), true);
      }
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getSilverObjectId()", SilverpeasRuntimeException.ERROR,
          "questionContainer.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  @Override
  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore) {
    List<StringBuffer> csvRows = new ArrayList<>();
    StringBuffer csvRow = new StringBuffer();
    OrganizationController orga = getOrganisationController();
    try {
      if (questionContainer.getHeader().isAnonymous()) {
        // anonymes
        Collection<Question> questions = questionContainer.getQuestions();
        for (Question question : questions) {
          if (question.isOpenStyle()) {
            // question ouverte
            String id = question.getPK().getId();
            QuestionContainerPK qcPK = new QuestionContainerPK(id, question.getPK().getSpaceId(),
                question.getPK().getInstanceId());
            Collection<QuestionResult> openAnswers = getSuggestions(qcPK);
            for (QuestionResult qR : openAnswers) {
              addCSVValue(csvRow, question.getLabel(), qR.getOpenedAnswer(), "", false, 0);
            }
          } else {
            // question fermée
            Collection<Answer> answers = question.getAnswers();
            for (Answer answer : answers) {
              int nbUsers = questionResultService
                  .getQuestionResultToQuestion(new ForeignPK(question.getPK())).size();
              String percent = Math.round((answer.getNbVoters() * 100f) / nbUsers) + "%";
              addCSVValue(csvRow, question.getLabel(), answer.getLabel(), percent, addScore,
                  answer.getNbPoints());
            }
          }
        }
      } else {
        // pour les enquêtes non anonymes
        Collection<Question> questions = questionContainer.getQuestions();
        for (Question question : questions) {
          if (question.isOpenStyle()) {
            // question ouverte
            String id = question.getPK().getId();
            QuestionContainerPK qcPK = new QuestionContainerPK(id, question.getPK().getSpaceId(),
                question.getPK().getInstanceId());
            Collection<QuestionResult> openAnswers = getSuggestions(qcPK);
            for (QuestionResult qR : openAnswers) {
              addCSVValue(csvRow, question.getLabel(), qR.getOpenedAnswer(),
                  orga.getUserDetail(qR.getUserId()).getDisplayedName(), false, 0);
            }
          } else {
            // question fermée
            Collection<Answer> answers = question.getAnswers();
            for (Answer answer : answers) {
              Collection<String> users =
                  questionResultService.getUsersByAnswer(answer.getPK().getId());
              for (String user : users) {
                // suggestion
                if (answer.isOpened()) {
                  QuestionResult openAnswer = getSuggestion(user, question.getPK(), answer.getPK());
                  addCSVValue(csvRow, question.getLabel(),
                      answer.getLabel() + " : " + openAnswer.getOpenedAnswer(),
                      orga.getUserDetail(user).getDisplayedName(), addScore, answer.getNbPoints());
                } else {
                  addCSVValue(csvRow, question.getLabel(), answer.getLabel(),
                      orga.getUserDetail(user).getDisplayedName(), addScore, answer.getNbPoints());
                }
              }
            }
          }
        }
      }
      csvRows.add(csvRow);
    } catch (Exception e) {
      SilverTrace.error("questionContainer", getClass().getSimpleName() + ".exportCSV()",
          "root.EX_NO_MESSAGE", e);
    }
    return writeCSVFile(csvRows);
  }

  private void addCSVValue(StringBuffer row, String questionLabel, String answerLabel, String value,
      boolean addScore, int nbPoints) {
    row.append("\"");
    if (questionLabel != null) {
      row.append(questionLabel.replaceAll("\"", "\"\"")).append("\"").append(";");
    }
    if (answerLabel != null) {
      row.append("\"").append(answerLabel.replaceAll("\"", "\"\"")).append("\"").append(";");
    }
    if (value != null) {
      row.append("\"").append(value.replaceAll("\"", "\"\"")).append("\"");
    }
    if (addScore) {
      row.append(";");
      row.append("\"").append(nbPoints).append("\"");
    }
    row.append(System.getProperty("line.separator"));
  }

  private String writeCSVFile(List<StringBuffer> csvRows) {
    FileOutputStream fileOutput = null;
    String csvFilename = new Date().getTime() + ".csv";
    try {
      fileOutput = new FileOutputStream(FileRepositoryManager.getTemporaryPath() + csvFilename);
      for (StringBuffer csvRow : csvRows) {
        fileOutput.write(csvRow.toString().getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (Exception e) {
      csvFilename = null;
      SilverTrace.error("questionContainer", getClass().getSimpleName() + ".writeCSVFile()",
          "root.EX_NO_MESSAGE", e);
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.flush();
          fileOutput.close();
        } catch (IOException e) {
          csvFilename = null;
          SilverTrace.error("questionContainer", getClass().getSimpleName() + ".writeCSVFile()",
              "root.EX_NO_MESSAGE", e);
        }
      }
    }
    return csvFilename;
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("DefaultQuestionContainerService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail) {
    String htmlPath;
    try {
      QuestionContainerHeader questionHeader = questionDetail.getHeader();
      QuestionContainerPK pk = questionHeader.getPK();
      htmlPath = getSpacesPath(pk.getInstanceId()) + getComponentLabel(pk.getInstanceId()) + " > " +
          questionHeader.getName();
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "DefaultQuestionContainerService.getHTMLQuestionPath()", SilverpeasRuntimeException.ERROR,
          "survey.IMPOSSIBLE_D_OBTENIR_LE_PATH", e);
    }
    return htmlPath;
  }

  private String getSpacesPath(String componentId) {
    StringBuilder spacesPath = new StringBuilder();
    List<SpaceInst> spaces = getOrganisationController().getSpacePathToComponent(componentId);
    for (SpaceInst spaceInst : spaces) {
      spacesPath.append(spaceInst.getName());
      spacesPath.append(" > ");
    }
    return spacesPath.toString();
  }

  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  private OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      AnswerDAO.deleteAnswersToAllQuestions(connection, componentInstanceId);
      QuestionResultDAO.setDeleteAllQuestionResultsByInstanceId(connection, componentInstanceId);
      ScoreDAO.deleteAllScoresByInstanceId(connection, componentInstanceId);
      QuestionDAO.deleteAllQuestionsByInstanceId(connection, componentInstanceId);
      QuestionContainerDAO.deleteAllQuestionContainersByInstanceId(connection, componentInstanceId);
    } catch (Exception e) {

    }
  }
}
