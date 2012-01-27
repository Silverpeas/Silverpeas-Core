/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.questionContainer.control;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.answer.control.AnswerBm;
import com.stratelia.webactiv.util.answer.control.AnswerBmHome;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.question.control.QuestionBm;
import com.stratelia.webactiv.util.question.control.QuestionBmHome;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.question.model.QuestionPK;
import com.stratelia.webactiv.util.questionContainer.ejb.QuestionContainerDAO;
import com.stratelia.webactiv.util.questionContainer.model.Comment;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerRuntimeException;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBm;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBmHome;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;
import com.stratelia.webactiv.util.score.control.ScoreBm;
import com.stratelia.webactiv.util.score.control.ScoreBmHome;
import com.stratelia.webactiv.util.score.model.ScoreDetail;
import com.stratelia.webactiv.util.score.model.ScorePK;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class declaration
 * @author neysseri
 */
public class QuestionContainerBmEJB implements QuestionContainerBmSkeleton, SessionBean {

  private static final long serialVersionUID = -2214591704695533730L;
  private QuestionBm currentQuestionBm = null;
  private QuestionResultBm currentQuestionResultBm = null;
  private AnswerBm currentAnswerBm = null;
  private ScoreBm currentScoreBm = null;

  // if beginDate is null, it will be replace in database with it
  private final static String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private final static String nullEndDate = "9999/99/99";
  private final static String dbName = JNDINames.QUESTIONCONTAINER_DATASOURCE;

  public QuestionContainerBmEJB() {
  }

  public Collection<QuestionContainerHeader> getQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> questionContainerHeaders = QuestionContainerDAO
          .getQuestionContainers(con, questionContainerPK);

      return this.setNbMaxPoints(questionContainerHeaders);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_LIST_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getQuestionContainerHeaders(
      List<QuestionContainerPK> pks) throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.getQuestionContainerHeaders()",
        "root.MSG_GEN_ENTER_METHOD", "pks = " + pks.toString());
    Connection con = null;
    try {
      con = getConnection();
      return QuestionContainerDAO.getQuestionContainers(con, pks);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainerHeaders()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_LIST_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  private Collection<QuestionContainerHeader> setNbMaxPoints(
      Collection<QuestionContainerHeader> questionContainerHeaders)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.setNbMaxPoints()", "root.MSG_GEN_ENTER_METHOD",
        "");

    QuestionBm questionBm = getQuestionBm();
    Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
    List<QuestionContainerHeader> result = new ArrayList<QuestionContainerHeader>();

    while (it.hasNext()) {
      int nbMaxPoints = 0;
      QuestionContainerHeader questionContainerHeader = it.next();
      QuestionPK questionPK = new QuestionPK(null, questionContainerHeader.getPK());
      Collection<Question> questions;
      try {
        questions =
            questionBm.getQuestionsByFatherPK(questionPK, questionContainerHeader.getPK().getId());
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.setNbMaxPoints()",
            SilverpeasRuntimeException.ERROR,
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

  private QuestionContainerHeader setNbMaxPoint(
      QuestionContainerHeader questionContainerHeader) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.setNbMaxPoint()", "root.MSG_GEN_ENTER_METHOD",
        "questionContainerHeader = " + questionContainerHeader);

    int nbMaxPoints = 0;
    QuestionBm questionBm = getQuestionBm();
    Collection<Question> questions;
    QuestionPK questionPK = new QuestionPK(null, questionContainerHeader
        .getPK());

    try {
      questions = questionBm.getQuestionsByFatherPK(questionPK,
          questionContainerHeader.getPK().getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.setNbMaxPoint()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONS_FAILED", e);
    }
    for (Question question : questions) {
      nbMaxPoints += question.getNbPointsMax();
    }
    questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    return questionContainerHeader;
  }

  public Collection<QuestionContainerHeader> getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getNotClosedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> questionContainerHeaders = QuestionContainerDAO
          .getNotClosedQuestionContainers(con, questionContainerPK);

      return this.setNbMaxPoints(questionContainerHeaders);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getNotClosedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_NOT_CLOSED_QUESTIONCONTAINERS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getOpenedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getOpenedQuestionContainers(con,
              questionContainerPK);

      return this.setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getOpenedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_OPENED_QUESTIONCONTAINERS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getOpenedQuestionContainersAndUserScores()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> questionContainerHeaders = QuestionContainerDAO
          .getOpenedQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<QuestionContainerHeader>(
          questionContainerHeaders.size());

      for (QuestionContainerHeader questionContainerHeader : questionContainerHeaders) {
        questionContainerHeader.setScores(this.getUserScoresByFatherId(
            questionContainerHeader.getPK(), userId));
        result.add(this.setNbMaxPoint(questionContainerHeader));
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getOpenedQuestionContainersAndUserScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_OPENED_QUESTIONCONTAINERS_AND_USER_SCORES_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainersWithScores()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> questionContainerHeaders = QuestionContainerDAO
          .getQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<QuestionContainerHeader>(
          questionContainerHeaders.size());
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
          "QuestionContainerBmEJB.getQuestionContainersWithScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINERS_AND_SCORES_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainersWithUserScores()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> questionContainerHeaders = QuestionContainerDAO
          .getQuestionContainers(con, questionContainerPK);
      Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
      List<QuestionContainerHeader> result = new ArrayList<QuestionContainerHeader>();

      while (it.hasNext()) {
        QuestionContainerHeader questionContainerHeader = it.next();
        Collection<ScoreDetail> scoreDetails = this.getUserScoresByFatherId(
            questionContainerHeader.getPK(), userId);

        if (scoreDetails != null) {
          questionContainerHeader.setScores(scoreDetails);
          result.add(this.setNbMaxPoint(questionContainerHeader));
        }
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainersWithUserScores()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINERS_AND_USER_SCORES_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getClosedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getClosedQuestionContainers(con,
              questionContainerPK);

      return this.setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getClosedQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_CLOSED_QUESTIONCONTAINERS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionContainerHeader> getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getInWaitQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getInWaitQuestionContainers(con,
              questionContainerPK);

      return this.setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getInWaitQuestionContainers()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_INWAIT_QUESTIONCONTAINERS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<ScoreDetail> getUserScoresByFatherId(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getUserScoresByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    Collection<ScoreDetail> scores;
    ScoreBm scoreBm = getScoreBm();
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreBm.getUserScoresByFatherId(scorePK, questionContainerPK
          .getId(), userId);
      if (scores != null) {
        SilverTrace.info("questionContainer",
            "QuestionContainerBmEJB.getUserScoresByFatherId()",
            "root.MSG_GEN_PARAM_VALUE", "Le score : Size=" + scores.size());
      } else {
        SilverTrace.info("questionContainer",
            "QuestionContainerBmEJB.getUserScoresByFatherId()",
            "root.MSG_GEN_PARAM_VALUE", "Le score : null");
      }
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getUserScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_SCORES_FAILED", e);
    }
    return scores;
  }

  public Collection<ScoreDetail> getBestScoresByFatherId(
      QuestionContainerPK questionContainerPK, int nbBestScores)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getBestScoresByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", nbBestScores = " + nbBestScores);
    Collection<ScoreDetail> scores;
    ScoreBm scoreBm = getScoreBm();
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreBm.getBestScoresByFatherId(scorePK, nbBestScores,
          questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getBestScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_BEST_SCORES_FAILED", e);
    }
  }

  public Collection<ScoreDetail> getWorstScoresByFatherId(
      QuestionContainerPK questionContainerPK, int nbScores)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getWorstScoresByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", nbScores = " + nbScores);
    Collection<ScoreDetail> scores;
    ScoreBm scoreBm = getScoreBm();
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreBm.getWorstScoresByFatherId(scorePK, nbScores,
          questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getWorstScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_WORST_SCORES_FAILED", e);
    }
  }

  public Collection<ScoreDetail> getScoresByFatherId(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.getScoresByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    Collection<ScoreDetail> scores;
    ScoreBm scoreBm = getScoreBm();
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreBm.getScoresByFatherId(scorePK, questionContainerPK.getId());
      if (scores != null) {
        SilverTrace.info("questionContainer", "QuestionContainerBmEJB.getScoresByFatherId()",
            "root.MSG_GEN_PARAM_VALUE", "Le score : Size=" + scores.size());
      } else {
        SilverTrace.info("questionContainer", "QuestionContainerBmEJB.getScoresByFatherId()",
            "root.MSG_GEN_PARAM_VALUE", "Le score : null");
      }
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_SCORES_FAILED", e);
    }
  }

  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.getAverageScoreByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    float averageScore;
    ScoreBm scoreBm = getScoreBm();
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      averageScore = scoreBm.getAverageScoreByFatherId(scorePK,
          questionContainerPK.getId());
      return averageScore;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getAverageScoreByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_AVERAGE_SCORE_FAILED", e);
    }
  }

  public QuestionContainerDetail getQuestionContainer(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    Connection con = null;
    Collection<Question> questions;
    Collection<Comment> comments = null;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes = null;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionBm questionBm = getQuestionBm();
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;

    try {
      questions = questionBm.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      for (Question question : questions) {
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_FAILED", e);
    }

    try {
      con = getConnection();
      userVotes = getUserVotesToQuestionContainer(userId, questionContainerPK);
    } finally {
      freeConnection(con);
    }

    try {
      con = getConnection();
      comments = getComments(questionContainerPK);
    } finally {
      freeConnection(con);
    }

    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainer()",
        "root.MSG_GEN_EXIT_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    return new QuestionContainerDetail(questionContainerHeader, questions,
        comments, userVotes);
  }

  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;
    QuestionContainerHeader questionContainerHeader = null;

    try {
      con = getConnection();
      questionContainerHeader = QuestionContainerDAO
          .getQuestionContainerHeader(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_FAILED", e);
    } finally {
      freeConnection(con);
    }

    return questionContainerHeader;
  }

  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId,
      int participationId) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainerByParticipationId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId
            + ", participationId = " + participationId);
    Collection<Question> questions;
    Collection<Comment> comments;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes = null;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionBm questionBm = getQuestionBm();
    QuestionResultBm questionResultBm = getQuestionResultBm();
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;

    try {
      questions = questionBm.getQuestionsByFatherPK(questionPK,
          questionContainerPK.getId());
      for (Question question : questions) {
        userVotes = questionResultBm.getUserQuestionResultsToQuestionByParticipation(userId,
            new ForeignPK(question.getPK()), participationId);
        question.setQuestionResults(userVotes);
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getQuestionContainerByParticipationId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_FAILED", e);
    }

    comments = getComments(questionContainerPK);

    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getQuestionContainerByParticipationId()",
        "root.MSG_GEN_EXIT_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId
            + ", participationId = " + participationId);
    return new QuestionContainerDetail(questionContainerHeader, questions,
        comments, userVotes);
  }

  public void closeQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.closeQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();

      // begin PDC integration
      QuestionContainerHeader qc =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, false);
      // end PDC integration
      QuestionContainerDAO.closeQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.closeQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.CLOSING_QUESTIONCONTAINER_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void openQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.openQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();

      // begin PDC integration
      QuestionContainerHeader qc = QuestionContainerDAO
          .getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, true);
      // end PDC integration

      QuestionContainerDAO.openQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.openQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.OPENING_QUESTIONCONTAINER_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public int getNbVotersByQuestionContainer(
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getNbVotersByQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    int nbVoters;

    ScorePK scorePK = new ScorePK("", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    ScoreBm scoreBm = getScoreBm();

    try {
      nbVoters = scoreBm.getNbVotersByFatherId(scorePK, questionContainerPK
          .getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getNbVotersByQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_NUMBER_OF_VOTES_FAILED",
          e);
    }
    return nbVoters;
  }

  public void recordReplyToQuestionContainerByUser(
      QuestionContainerPK questionContainerPK, String userId,
      Hashtable<String, Vector<String>> reply)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    SimpleDateFormat formatterDB = new java.text.SimpleDateFormat("yyyy/MM/dd");
    QuestionPK questionPK;
    AnswerPK answerPK;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    QuestionResult result;
    QuestionResultBm questionResultBm = getQuestionResultBm();
    AnswerBm answerBm = getAnswerBm();
    QuestionBm questionBm = getQuestionBm();
    ScoreBm scoreBm = getScoreBm();
    Answer answer;
    int participationId = scoreBm.getUserNbParticipationsByFatherId(scorePK,
        questionContainerPK.getId(), userId) + 1;
    int questionUserScore;
    int userScore = 0;

    Enumeration<String> keys = reply.keys();

    while (keys.hasMoreElements()) {
      questionUserScore = 0;
      String questionId = keys.nextElement();

      questionPK = new QuestionPK(questionId, questionContainerPK);
      Question question;

      try {
        question = questionBm.getQuestion(questionPK);
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
            SilverpeasRuntimeException.ERROR,
            "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
            e);
      }

      Vector<String> answers = reply.get(questionId);
      String answerId;
      int vectorSize = answers.size();
      int newVectorSize = vectorSize;
      int vectorBegin = 0;
      // Treatment of the first vector element
      // to know if the clue has been read
      String cluePenalty = answers.firstElement();
      int penaltyValue = 0;

      if (cluePenalty.startsWith("PC")) {
        // It's a clue penalty field
        penaltyValue = Integer.parseInt(cluePenalty.substring(2, cluePenalty.length()));
        vectorBegin = 1;
      }

      // Treatment of the last vector element
      // to know if the answer is opened
      String openedAnswer = answers.lastElement();

      if (openedAnswer.startsWith("OA")) {
        SilverTrace.info("questionContainer",
            "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
            "root.MSG_GEN_PARAM_VALUE", "It's an open answer !");
        // It's an open answer
        // Fetch the correspondant answerId
        answerId = answers.get(vectorSize - 2);
        openedAnswer = openedAnswer.substring(2, openedAnswer.length());

        // User Score for this question
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;

        newVectorSize = vectorSize - 2;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result = new QuestionResult(null, new ForeignPK(questionPK), answerPK,
            userId, openedAnswer);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        SilverTrace.info("questionContainer",
            "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
            "root.MSG_GEN_PARAM_VALUE", "answer.getNbPoints(): "
                + answer.getNbPoints() + ", penaltyValue=" + penaltyValue);
        try {
          questionResultBm.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
              e);
        }
        try {
          // Add this vote to the corresponding answer
          answerBm.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
              e);
        }
      }

      for (int i = vectorBegin; i < newVectorSize; i++) {
        answerId = answers.get(i);
        SilverTrace.info("questionContainer",
            "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
            "root.MSG_GEN_PARAM_VALUE", "It's a closed answer " + i);
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result = new QuestionResult(null, new ForeignPK(questionPK), answerPK,
            userId, null);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        try {
          questionResultBm.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
              e);
        }
        try {
          // Add this vote to the corresponding answer
          answerBm.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(
              "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
              SilverpeasRuntimeException.ERROR,
              "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
              e);
        }
      }
      SilverTrace.info("questionContainer",
          "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
          "root.MSG_GEN_PARAM_VALUE", "Question ptsmin ="
              + question.getNbPointsMin() + " - Question ptsmax ="
              + question.getNbPointsMax());
      if (question.getNbPointsMax() < questionUserScore) {
        questionUserScore = question.getNbPointsMax();
      } else if (question.getNbPointsMin() > questionUserScore) {
        questionUserScore = question.getNbPointsMin();
      }
      userScore += questionUserScore;
      SilverTrace.info("questionContainer",
          "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
          "root.MSG_GEN_PARAM_VALUE", "questionUserScore =" + questionUserScore
              + " - userScore =" + userScore);
    }

    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
        "root.MSG_GEN_PARAM_VALUE", "User Score =" + userScore);
    // Record the UserScore
    ScoreDetail scoreDetail = new ScoreDetail(scorePK, questionContainerPK
        .getId(), userId, participationId, formatterDB
        .format(new java.util.Date()), userScore, 0, "");

    scoreBm.addScore(scoreDetail);

    Connection con = null;

    try {
      con = getConnection();
      // Increment the number of voters
      QuestionContainerDAO.addAVoter(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.RECORDING_USER_RESPONSES_TO_QUESTIONCONTAINER_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public void recordReplyToQuestionContainerByUser(
      QuestionContainerPK questionContainerPK, String userId,
      Hashtable<String, Vector<String>> reply,
      String comment, boolean isAnonymousComment) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.recordReplyToQuestionContainerByUser()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId + ", comment = "
            + comment);
    recordReplyToQuestionContainerByUser(questionContainerPK, userId, reply);
    addComment(questionContainerPK, userId, comment, isAnonymousComment);
  }

  private void addComment(QuestionContainerPK questionContainerPK,
      String userId, String comment, boolean isAnonymousComment)
      throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.addComment()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK + ", userId = "
            + userId + ", comment = " + comment);
    Connection con = null;
    try {
      con = getConnection();
      Comment c = new Comment(null, questionContainerPK, userId, comment, isAnonymousComment, null);
      QuestionContainerDAO.addComment(con, c);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.addComment()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.ADDING_QUESTIONCONTAINER_COMMENT_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public QuestionContainerPK createQuestionContainer(QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId) throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.createQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK
            + ", questionContainerDetail = " + questionContainerDetail + ", userId = " + userId);
    Connection con = null;
    QuestionContainerHeader questionContainerHeader = questionContainerDetail.getHeader();
    questionContainerHeader.setPK(questionContainerPK);
    questionContainerHeader.setCreatorId(userId);
    try {
      con = getConnection();
      questionContainerPK = QuestionContainerDAO.createQuestionContainerHeader(
          con, questionContainerHeader);
      questionContainerHeader.setPK(questionContainerPK);
      QuestionContainerContentManager.createSilverContent(con,
          questionContainerHeader, userId, true);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.createQuestionContainer()",
          SilverpeasRuntimeException.ERROR, "questionContainer.CREATING_QUESTIONCONTAINER_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
    QuestionBm questionBm = getQuestionBm();
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    Collection<Question> questions = questionContainerDetail.getQuestions();
    List<Question> q = new ArrayList<Question>(questions.size());
    for (Question question : questions) {
      question.setPK(questionPK);
      q.add(question);
    }

    try {
      questionBm.createQuestions(q, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.createQuestionContainer()", SilverpeasRuntimeException.ERROR,
          "questionContainer.CREATING_QUESTIONCONTAINER_FAILED", e);
    }
    createIndex(questionContainerHeader);
    return questionContainerPK;
  }

  public void updateQuestionContainerHeader(QuestionContainerHeader questionContainerHeader)
      throws RemoteException {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.updateQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerHeader = " + questionContainerHeader);
    Connection con = null;

    try {
      con = getConnection();
      QuestionContainerDAO.updateQuestionContainerHeader(con, questionContainerHeader);
      // start PDC integration
      QuestionContainerContentManager.updateSilverContentVisibility(questionContainerHeader, true);
      // end PDC integration
      createIndex(questionContainerHeader);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.updateQuestionContainerHeader()",
          SilverpeasRuntimeException.ERROR, "questionContainer.UPDATING_QUESTIONCONTAINER_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection<Question> questions) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.updateQuestions()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    QuestionBm questionBm = getQuestionBm();
    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    for (Question question : questions) {
      question.setPK(questionPK);
    }
    try {
      // delete all old questions
      questionBm.deleteQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      // replace it with new ones
      questionBm.createQuestions(questions, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.updateQuestions()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.UPDATING_QUESTIONCONTAINER_QUESTIONS_FAILED", e);
    }
  }

  public void deleteVotes(QuestionContainerPK questionContainerPK) {
    SilverTrace.info("questionContainer", "QuestionContainerBmEJB.deleteVotes()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = " + questionContainerPK);
    Connection con = null;
    ScorePK scorePK = new ScorePK(questionContainerPK.getId(), questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionPK questionPK = new QuestionPK(questionContainerPK.getId(),
        questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionBm questionBm = getQuestionBm();
    ScoreBm scoreBm = getScoreBm();
    QuestionResultBm questionResultBm = getQuestionResultBm();

    try {
      QuestionContainerHeader qch = getQuestionContainerHeader(questionContainerPK);
      // mise a zero du nombre de participation
      qch.setNbVoters(0);
      updateQuestionContainerHeader(qch);

      con = getConnection();
      scoreBm.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());

      // suppression des commentaires
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      // get all questions to delete results
      Collection<Question> questions = questionBm.getQuestionsByFatherPK(questionPK,
          questionContainerPK.getId());
      if (questions != null && !questions.isEmpty()) {
        for (Question question : questions) {
          QuestionPK questionPKToDelete = question.getPK();
          // delete all results
          questionResultBm.deleteQuestionResultsToQuestion(new ForeignPK(questionPKToDelete));
          Collection<Answer> answers = question.getAnswers();
          Collection<Answer> newAnswers = new ArrayList<Answer>();
          for (Answer answer : answers) {
            answer.setNbVoters(0);
            newAnswers.add(answer);
          }
          question.setAnswers(newAnswers);
          questionBm.updateQuestion(question);
        }
      }

    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.deleteVotes()",
          SilverpeasRuntimeException.ERROR, "questionContainer.DELETING_QUESTIONCONTAINER_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.deleteQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    Connection con = null;
    ScorePK scorePK = new ScorePK(questionContainerPK.getId(),
        questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    QuestionPK questionPK = new QuestionPK(questionContainerPK.getId(),
        questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    QuestionBm questionBm = getQuestionBm();
    ScoreBm scoreBm = getScoreBm();

    try {
      scoreBm.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.DELETING_QUESTIONCONTAINER_FAILED", e);
    }
    try {
      questionBm.deleteQuestionsByFatherPK(questionPK, questionContainerPK
          .getId());
      deleteIndex(questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.DELETING_QUESTIONCONTAINER_FAILED", e);
    }

    try {
      con = getConnection();
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      QuestionContainerDAO.deleteQuestionContainerHeader(con,
          questionContainerPK);

      QuestionContainerContentManager.deleteSilverContent(con,
          questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.deleteQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.DELETING_QUESTIONCONTAINER_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  private Collection<Comment> getComments(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getComments()", "root.MSG_GEN_ENTER_METHOD",
        "questionContainerPK = " + questionContainerPK);
    Connection con = null;

    try {
      con = getConnection();
      return QuestionContainerDAO.getComments(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getComments()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_COMMENTS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionResult> getSuggestions(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getSuggestions()", "root.MSG_GEN_ENTER_METHOD",
        "questionContainerPK = " + questionContainerPK);
    Collection<QuestionResult> suggestions;
    QuestionPK questionPK = new QuestionPK(questionContainerPK.getId(),
        questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    QuestionResultBm questionResultBm = getQuestionResultBm();

    try {
      suggestions = questionResultBm.getQuestionResultToQuestion(new ForeignPK(
          questionPK));
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getSuggestions()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_SUGGESTIONS_FAILED", e);
    }
    return suggestions;
  }

  private Collection<QuestionResult> getUserVotesToQuestionContainer(String userId,
      QuestionContainerPK questionContainerPK) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getUserVotesToQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    Collection<QuestionResult> votes = null;
    QuestionPK questionPK = new QuestionPK("unknown", questionContainerPK
        .getSpace(), questionContainerPK.getComponentName());
    QuestionBm questionBm = getQuestionBm();
    Collection<Question> questions;

    try {
      questions = questionBm.getQuestionsByFatherPK(questionPK,
          questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getUserVotesToQuestionContainer()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_RESPONSES_FAILED",
          e);
    }
    for (Question question : questions) {
      QuestionResultBm questionResultBm = getQuestionResultBm();
      try {
        votes = questionResultBm.getUserQuestionResultsToQuestion(userId,
            new ForeignPK(question.getPK()));
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "QuestionContainerBmEJB.getUserVotesToQuestionContainer()",
            SilverpeasRuntimeException.ERROR,
            "questionContainer.GETTING_QUESTIONCONTAINER_USER_RESPONSES_FAILED", e);
      }
      if (!votes.isEmpty()) {
        break;
      }
    }
    return votes;
  }

  public float getAveragePoints(QuestionContainerPK questionContainerPK)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getAveragePoints()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK);
    float averagePoints;
    ScorePK scorePK = new ScorePK("", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    ScoreBm scoreBm = getScoreBm();

    try {
      averagePoints = scoreBm.getAverageScoreByFatherId(scorePK,
          questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getAveragePoints()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_AVERAGE_SCORES_FAILED",
          e);
    }
    return averagePoints;
  }

  public int getUserNbParticipationsByFatherId(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getUserNbParticipationsByFatherId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId);
    int nbPart;

    ScorePK scorePK = new ScorePK("", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    ScoreBm scoreBm = getScoreBm();

    try {
      nbPart = scoreBm.getUserNbParticipationsByFatherId(scorePK,
          questionContainerPK.getId(), userId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getUserNbParticipationsByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_NB_PARTICIPATION_TO_USER_FAILED",
          e);
    }
    return nbPart;
  }

  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId,
      int participationId) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getUserScoreByFatherIdAndParticipationId()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
            + questionContainerPK + ", userId = " + userId
            + ", participationId = " + participationId);
    ScoreDetail scoreDetail;

    ScorePK scorePK = new ScorePK("", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    ScoreBm scoreBm = getScoreBm();

    try {
      scoreDetail = scoreBm.getUserScoreByFatherIdAndParticipationId(scorePK,
          questionContainerPK.getId(), userId, participationId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getUserScoreByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.GETTING_QUESTIONCONTAINER_USER_SCORE_TO_A_PARTICIPATION_FAILED",
          e);
    }
    return scoreDetail;
  }

  public void updateScore(QuestionContainerPK questionContainerPK,
      ScoreDetail scoreDetail) throws RemoteException {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.updateScore()", "root.MSG_GEN_ENTER_METHOD",
        "questionContainerPK = " + questionContainerPK + ", scoreDetail = "
            + scoreDetail);
    ScoreBm scoreBm = getScoreBm();

    try {
      scoreBm.updateScore(scoreDetail);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.updateScore()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.UPDATING_QUESTIONCONTAINER_SCORE_FAILED", e);
    }
  }

  /**
   * Called on : - createQuestionContainer() - updateQuestionContainer()
   * @param header the question container to index.
   */
  private void createIndex(QuestionContainerHeader header) {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
        "header = " + header);
    FullIndexEntry indexEntry = null;

    if (header != null) {
      // Index the QuestionContainerHeader
      indexEntry = new FullIndexEntry(header.getPK().getComponentName(),
          "QuestionContainer", header.getPK().getId());
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
  public void deleteIndex(QuestionContainerPK pk) {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.deleteIndex()", "root.MSG_GEN_ENTER_METHOD",
        "questionContainerPK = " + pk);
    IndexEntryPK indexEntry = new IndexEntryPK(pk.getComponentName(),
        "QuestionContainer", pk.getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public int getSilverObjectId(QuestionContainerPK pk) {
    SilverTrace.info("questionContainer",
        "QuestionContainerBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pk = " + pk.toString());
    int silverObjectId;
    try {
      silverObjectId = QuestionContainerContentManager.getSilverObjectId(pk
          .getId(), pk.getComponentName());
      if (silverObjectId == -1) {
        QuestionContainerHeader questionContainerHeader = getQuestionContainerHeader(pk);
        silverObjectId = QuestionContainerContentManager.createSilverContent(
            null, questionContainerHeader, questionContainerHeader
                .getCreatorId(), true);
      }
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "questionContainer.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore) {
    List<StringBuffer> csvRows = new ArrayList<StringBuffer>();
    StringBuffer csvRow = new StringBuffer();
    OrganizationController orga = new OrganizationController();
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
              int nbUsers = getQuestionResultBm().getQuestionResultToQuestion(
                  new ForeignPK(question.getPK())).size();
              String percent = Math.round((answer.getNbVoters() * 100f) / nbUsers) + "%";
              addCSVValue(csvRow, question.getLabel(), answer.getLabel(), percent, addScore, answer
                  .getNbPoints());
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
            QuestionContainerPK qcPK =
                new QuestionContainerPK(id, question.getPK().getSpaceId(), question.getPK()
                    .getInstanceId());
            Collection<QuestionResult> openAnswers = getSuggestions(qcPK);
            for (QuestionResult qR : openAnswers) {
              addCSVValue(csvRow, question.getLabel(), qR.getOpenedAnswer(), orga.getUserDetail(
                  qR.getUserId()).getDisplayedName(), false, 0);
            }
          } else {
            // question fermée
            Collection<Answer> answers = question.getAnswers();
            for (Answer answer : answers) {
              Collection<String> users =
                  getQuestionResultBm().getUsersByAnswer(answer.getPK().getId());
              for (String user : users) {
                addCSVValue(csvRow, question.getLabel(), answer.getLabel(), orga.getUserDetail(
                    user).getDisplayedName(), addScore, answer.getNbPoints());
              }
            }
          }
        }
      }
      csvRows.add(csvRow);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return writeCSVFile(csvRows);
  }

  private void addCSVValue(StringBuffer row, String questionLabel, String answerLabel,
      String value, boolean addScore, int nbPoints) {
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
      e.printStackTrace();
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.flush();
          fileOutput.close();
        } catch (IOException e) {
          csvFilename = null;
          e.printStackTrace();
        }
      }
    }
    return csvFilename;
  }

  /**
   * @return a QuesiotnBM
   */
  private QuestionBm getQuestionBm() {
    if (currentQuestionBm == null) {
      try {
        QuestionBmHome questionBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.QUESTIONBM_EJBHOME,
            QuestionBmHome.class);
        currentQuestionBm = questionBmHome.create();
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "QuestionContainerBmEJB.getQuestionBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = Question", e);
      }
    }
    return currentQuestionBm;
  }

  private AnswerBm getAnswerBm() {
    if (currentAnswerBm == null) {
      try {
        AnswerBmHome answerBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.ANSWERBM_EJBHOME,
            AnswerBmHome.class);
        currentAnswerBm = answerBmHome.create();
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(
            "QuestionContainerBmEJB.getQuestionBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = Answer", e);
      }
    }
    return currentAnswerBm;
  }

  private QuestionResultBm getQuestionResultBm() {
    if (currentQuestionResultBm == null) {
      try {
        QuestionResultBmHome questionResultBmHome = EJBUtilitaire.getEJBObjectRef(
            JNDINames.QUESTIONRESULTBM_EJBHOME, QuestionResultBmHome.class);
        currentQuestionResultBm = questionResultBmHome.create();
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.getQuestionResultBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = QuestionResult", e);
      }
    }
    return currentQuestionResultBm;
  }

  private ScoreBm getScoreBm() {
    if (currentScoreBm == null) {
      try {
        ScoreBmHome scoreBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.SCOREBM_EJBHOME,
            ScoreBmHome.class);
        currentScoreBm = scoreBmHome.create();
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.getScoreBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = Score", e);
      }
    }
    return currentScoreBm;
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("QuestionContainerBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("questionContainer", "QuestionContainerBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail) {
    String htmlPath;
    try {
      QuestionContainerHeader questionHeader = questionDetail.getHeader();
      QuestionContainerPK pk = questionHeader.getPK();
      htmlPath = getSpacesPath(pk.getInstanceId()) + getComponentLabel(pk.getInstanceId()) + " > "
          + questionHeader.getName();
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerBmEJB.getHTMLQuestionPath()",
          SilverpeasRuntimeException.ERROR,
          "survey.IMPOSSIBLE_D_OBTENIR_LE_PATH", e);
    }
    return htmlPath;
  }

  private String getSpacesPath(String componentId) {
    String spacesPath = "";
    List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(componentId);
    for (SpaceInst spaceInst : spaces) {
      spacesPath += spaceInst.getName();
      spacesPath += " > ";
    }
    return spacesPath;
  }

  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganizationController()
        .getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  private OrganizationController getOrganizationController() {
    return new OrganizationController();
  }

  /**************************************************************************************************************************/
  /* EJB METHODS */

  /**
   * ***********************************************************************************************
   * ***********************
   */

  public void ejbCreate() {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }

}