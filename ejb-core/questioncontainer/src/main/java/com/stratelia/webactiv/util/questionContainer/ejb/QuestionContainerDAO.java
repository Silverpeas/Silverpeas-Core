/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

// TODO : reporter dans CVS (done)

package com.stratelia.webactiv.util.questionContainer.ejb;

import java.sql.*;
import java.util.*;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.questionContainer.model.*;
import com.stratelia.webactiv.util.exception.*;
import com.stratelia.silverpeas.silvertrace.*;

/*
 * CVS Informations
 *
 * $Id: QuestionContainerDAO.java,v 1.6 2006/11/15 14:09:19 sfariello Exp $
 *
 * $Log: QuestionContainerDAO.java,v $
 * Revision 1.6.4.3  2009/07/10 13:08:48  sfariello
 * Modification de la mise à jour des question pour la modification à tout moment des enquêtes
 *
 * Revision 1.6.4.2  2009/06/26 09:32:19  sfariello
 * no message
 *
 * Revision 1.6.4.1  2009/06/22 08:08:15  sfariello
 * Ajout modification de l'enquête à tout moment
 *
 * Revision 1.6  2006/11/15 14:09:19  sfariello
 * no message
 *
 * Revision 1.5  2004/06/22 15:42:07  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.4  2003/11/25 08:31:25  cbonin
 * no message
 *
 * Revision 1.3  2003/11/24 10:50:17  cbonin
 * no message
 *
 * Revision 1.2  2002/11/18 11:19:37  neysseri
 * PdcVisibility branch merging
 *
 * Revision 1.1.1.1.16.1  2002/11/14 17:35:41  neysseri
 * Adding of survey as a new content
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.13  2002/01/04 14:22:51  neysseri
 * no message
 *
 */

/**
 * This class is made to access database only (table SB_QuestionContainer_QC et
 * SB_QuestionContainer_Comment)
 * @author neysseri
 */
public class QuestionContainerDAO {

  public static final String QUESTIONCONTAINERCOLUMNNAMES =
      "qcId, qcTitle, qcDescription, qcComment, qcCreatorId, qcCreationDate, qcBeginDate, qcEndDate, qcIsClosed, qcNbVoters, qcNbQuestionsPage, qcNbMaxParticipations, qcNbTriesBeforeSolution, qcMaxTime, anonymous, instanceId";
  public static final String COMMENTCOLUMNNAMES =
      "commentId, commentFatherId, userId, commentComment, commentIsAnonymous, commentDate";

  // the date format used in database to represent a date
  private static java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
      "yyyy/MM/dd");

  // if beginDate is null, it will be replace in database with it
  private static String nullBeginDate = new String("0000/00/00");
  // if endDate is null, it will be replace in database with it
  private static String nullEndDate = new String("9999/99/99");

  /**
   * Method declaration
   * @param rs
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static QuestionContainerHeader getQuestionContainerHeaderFromResultSet(
      ResultSet rs, QuestionContainerPK questionContainerPK)
      throws SQLException {
    String id = new Integer(rs.getInt(1)).toString();
    String title = rs.getString(2);
    String description = rs.getString(3);
    String comment = rs.getString(4);
    String creatorId = rs.getString(5);
    String creationDate = rs.getString(6);
    String beginDate = rs.getString(7);

    if (beginDate.equals(nullBeginDate)) {
      beginDate = null;
    }
    String endDate = rs.getString(8);

    if (endDate.equals(nullEndDate)) {
      endDate = null;
    }
    int isClosed = rs.getInt(9);
    boolean closed = false;

    if (isClosed == 1) {
      closed = true;
    }
    int nbVoters = rs.getInt(10);
    int nbQuestionsPage = rs.getInt(11);
    int nbMaxParticipations = rs.getInt(12);
    int nbParticipationsBeforeSolution = rs.getInt(13);
    int maxTime = rs.getInt(14);
    boolean anonymous = (rs.getInt(15) == 1);
    String instanceId = rs.getString(16);

    questionContainerPK.setComponentName(instanceId);

    QuestionContainerHeader result = new QuestionContainerHeader(
        new QuestionContainerPK(id, questionContainerPK), title, description,
        comment, creatorId, creationDate, beginDate, endDate, closed, nbVoters,
        nbQuestionsPage, nbMaxParticipations, nbParticipationsBeforeSolution,
        maxTime, anonymous);
    return result;
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;

    String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
        + " from " + questionContainerPK.getTableName()
        + " where instanceId = '" + questionContainerPK.getComponentName()
        + "' " + " order by qcBeginDate DESC, qcEndDate DESC";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      ArrayList list = new ArrayList();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
            questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static Collection getQuestionContainers(Connection con, ArrayList pks)
      throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "pks = " + pks.toString());

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;
    QuestionContainerPK questionContainerPK = new QuestionContainerPK("unknown");
    ArrayList list = new ArrayList();
    StringBuffer whereClause = new StringBuffer();

    if (pks != null && pks.size() > 0) {
      Iterator it = pks.iterator();
      QuestionContainerPK pk = null;

      whereClause.append("(");
      while (it.hasNext()) {
        pk = (QuestionContainerPK) it.next();
        whereClause.append(" qcId = ").append(pk.getId());
        if (it.hasNext()) {
          whereClause.append(" or ");
        } else {
          whereClause.append(" ) ");
        }
      }

      String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
          + " from " + questionContainerPK.getTableName() + " where "
          + whereClause.toString() + " and instanceId = '"
          + pk.getComponentName()
          + "' order by qcBeginDate DESC, qcEndDate DESC";
      Statement stmt = null;

      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery(selectStatement);
        while (rs.next()) {
          questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
              pk);
          list.add(questionContainerHeader);
        }
      } finally {
        DBUtil.close(rs, stmt);
      }
    }
    return list;
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getOpenedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getOpenedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);
    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;

    String selectStatement = "select "
        + QUESTIONCONTAINERCOLUMNNAMES
        + " from "
        + questionContainerPK.getTableName()
        + " where ? between qcBeginDate and qcEndDate "
        + " and qcIsClosed = 0 and instanceId = ? order by  qcBeginDate DESC, qcEndDate DESC";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, questionContainerPK.getComponentName());
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
            questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getNotClosedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getNotClosedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;

    String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
        + " from " + questionContainerPK.getTableName()
        + " where instanceId = '" + questionContainerPK.getComponentName()
        + "' "
        + " and qcIsClosed = 0 order by qcBeginDate DESC, qcEndDate DESC";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      ArrayList list = new ArrayList();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
            questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getClosedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getClosedQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;

    String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
        + " from " + questionContainerPK.getTableName()
        + " where ( ? > qcEndDate " + " or qcIsClosed = 1 )"
        + " and instanceId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, questionContainerPK.getComponentName());
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
            questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param qcPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getInWaitQuestionContainers(Connection con,
      QuestionContainerPK qcPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getInWaitQuestionContainers()",
        "root.MSG_GEN_ENTER_METHOD", "qcPK = " + qcPK);

    ResultSet rs = null;
    QuestionContainerHeader header = null;

    String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
        + " from " + qcPK.getTableName()
        + " where ? < qcBeginDate and instanceId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, qcPK.getComponentName());
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        header = getQuestionContainerHeaderFromResultSet(rs, qcPK);
        list.add(header);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @return
   * @throws SQLException
   * @see
   */
  public static QuestionContainerHeader getQuestionContainerHeader(
      Connection con, QuestionContainerPK questionContainerPK)
      throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);
    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;

    String selectStatement = "select " + QUESTIONCONTAINERCOLUMNNAMES
        + " from " + questionContainerPK.getTableName() + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(questionContainerPK.getId()).intValue());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs,
            questionContainerPK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return questionContainerHeader;
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @throws SQLException
   * @see
   */
  public static void closeQuestionContainer(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.closeQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    String updateStatement = "update " + questionContainerPK.getTableName()
        + " set qcIsClosed = 1 , instanceId = ?" + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, questionContainerPK.getComponentName());
      prepStmt.setInt(2, new Integer(questionContainerPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @throws SQLException
   * @see
   */
  public static void openQuestionContainer(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.openQuestionContainer()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    String updateStatement = "update " + questionContainerPK.getTableName()
        + " set qcIsClosed = 0 , instanceId = ?" + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, questionContainerPK.getComponentName());
      prepStmt.setInt(2, new Integer(questionContainerPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerHeader
   * @return
   * @throws SQLException
   * @see
   */
  public static QuestionContainerPK createQuestionContainerHeader(
      Connection con, QuestionContainerHeader questionContainerHeader)
      throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.createQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerHeader = "
        + questionContainerHeader);
    int newId = 0;

    String insertStatement = "insert into "
        + questionContainerHeader.getPK().getTableName()
        + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(questionContainerHeader.getPK().getTableName(),
          new String("qcId"));
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerDAO.createQuestionContainerHeader()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    QuestionContainerPK questionContainerPK = questionContainerHeader.getPK();

    questionContainerPK.setId(new Integer(newId).toString());

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, questionContainerHeader.getTitle());
      prepStmt.setString(3, questionContainerHeader.getDescription());
      prepStmt.setString(4, questionContainerHeader.getComment());
      prepStmt.setString(5, questionContainerHeader.getCreatorId());
      prepStmt.setString(6, formatter.format(new java.util.Date()));
      if (questionContainerHeader.getBeginDate() == null) {
        prepStmt.setString(7, nullBeginDate);
      } else {
        prepStmt.setString(7, questionContainerHeader.getBeginDate());
      }
      if ((questionContainerHeader.getEndDate() == null)
          || (questionContainerHeader.getEndDate().length() < 10)) {
        prepStmt.setString(8, nullEndDate);
      } else {
        prepStmt.setString(8, questionContainerHeader.getEndDate());
      }
      prepStmt.setInt(9, 0);
      prepStmt.setInt(10, 0);
      prepStmt.setInt(11, questionContainerHeader.getNbQuestionsPerPage());
      prepStmt.setInt(12, questionContainerHeader.getNbMaxParticipations());
      prepStmt.setInt(13, questionContainerHeader
          .getNbParticipationsBeforeSolution());
      prepStmt.setInt(14, questionContainerHeader.getMaxTime());
      prepStmt
          .setString(15, questionContainerHeader.getPK().getComponentName());
      if (questionContainerHeader.isAnonymous()) {
        prepStmt.setInt(16, 1);
      } else {
        prepStmt.setInt(16, 0);
      }

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return questionContainerPK;
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerHeader
   * @throws SQLException
   * @see
   */
  public static void updateQuestionContainerHeader(Connection con,
      QuestionContainerHeader questionContainerHeader) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.updateQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerHeader = "
        + questionContainerHeader);

    String insertStatement = "update "
        + questionContainerHeader.getPK().getTableName() + " set qcTitle = ?,"
        + " qcDescription = ?," + " qcComment = ?," + " qcBeginDate = ?,"
        + " qcEndDate = ?," + " qcNbVoters = ?," + " qcNbQuestionsPage = ?,"
        + " qcNbMaxParticipations = ?," + " qcNbTriesBeforeSolution = ?,"
        + " qcMaxTime = ?, " + " instanceId = ?, " + " anonymous = ?"
        + " where qcId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, questionContainerHeader.getTitle());
      prepStmt.setString(2, questionContainerHeader.getDescription());
      prepStmt.setString(3, questionContainerHeader.getComment());
      if (questionContainerHeader.getBeginDate() == null) {
        prepStmt.setString(4, nullBeginDate);
      } else {
        prepStmt.setString(4, questionContainerHeader.getBeginDate());
      }
      if (questionContainerHeader.getEndDate() == null) {
        prepStmt.setString(5, nullEndDate);
      } else {
        prepStmt.setString(5, questionContainerHeader.getEndDate());
      }
      prepStmt.setInt(6, questionContainerHeader.getNbVoters());
      prepStmt.setInt(7, questionContainerHeader.getNbQuestionsPerPage());
      prepStmt.setInt(8, questionContainerHeader.getNbMaxParticipations());
      prepStmt.setInt(9, questionContainerHeader
          .getNbParticipationsBeforeSolution());
      prepStmt.setInt(10, questionContainerHeader.getMaxTime());
      prepStmt
          .setString(11, questionContainerHeader.getPK().getComponentName());
      if (questionContainerHeader.isAnonymous()) {
        prepStmt.setInt(12, 1);
      } else {
        prepStmt.setInt(12, 0);
      }
      prepStmt.setInt(13, new Integer(questionContainerHeader.getPK().getId())
          .intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @throws SQLException
   * @see
   */
  public static void deleteQuestionContainerHeader(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.deleteQuestionContainerHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    String deleteStatement = "delete from "
        + questionContainerPK.getTableName() + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, new Integer(questionContainerPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param questionContainerPK
   * @throws SQLException
   * @see
   */
  public static void addAVoter(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {
    SilverTrace.info("questionContainer", "QuestionContainerDAO.addAVoter()",
        "root.MSG_GEN_ENTER_METHOD", "questionContainerPK = "
        + questionContainerPK);

    String updateStatement = "update " + questionContainerPK.getTableName()
        + " set qcNbVoters = qcNbVoters + 1 " + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, new Integer(questionContainerPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param rs
   * @param qcPK
   * @return
   * @throws SQLException
   * @see
   */
  private static Comment getCommentFromResultSet(ResultSet rs,
      QuestionContainerPK qcPK) throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.getCommentFromResultSet()",
        "root.MSG_GEN_ENTER_METHOD", "qcPK = " + qcPK);
    String id = new Integer(rs.getInt(1)).toString();
    String userId = rs.getString(3);
    String comment = rs.getString(4);
    boolean isAnonymous = (rs.getInt(5) == 1);
    String date = rs.getString(6);
    Comment result = new Comment(new CommentPK(id, qcPK), qcPK, userId,
        comment, isAnonymous, date);
    return result;
  }

  /**
   * Method declaration
   * @param con
   * @param comment
   * @throws SQLException
   * @see
   */
  public static void addComment(Connection con, Comment comment)
      throws SQLException {
    SilverTrace.info("questionContainer", "QuestionContainerDAO.addComment()",
        "root.MSG_GEN_ENTER_METHOD", "comment = " + comment);
    QuestionContainerPK questionContainerPK = comment.getQuestionContainerPK();
    CommentPK commentPK = new CommentPK("unknown", questionContainerPK);
    int newId = 0;

    String insertStatement = "insert into " + commentPK.getTableName()
        + " values(?, ?, ?, ?, ?, ?) ";

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(commentPK.getTableName(),
          new String("commentId"));
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerDAO.addComment()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, new Integer(questionContainerPK.getId()).intValue());
      prepStmt.setString(3, comment.getUserId());
      prepStmt.setString(4, comment.getComment());
      if (comment.isAnonymous()) {
        prepStmt.setInt(5, 1);
      } else {
        prepStmt.setInt(5, 0);
      }
      prepStmt.setString(6, formatter.format(new java.util.Date()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param qcPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getComments(Connection con, QuestionContainerPK qcPK)
      throws SQLException {
    SilverTrace.info("questionContainer", "QuestionContainerDAO.getComments()",
        "root.MSG_GEN_ENTER_METHOD", "qcPK = " + qcPK);

    ResultSet rs = null;
    Comment comment = null;
    CommentPK commentPK = new CommentPK("unknown", qcPK);

    String selectStatement = "select " + COMMENTCOLUMNNAMES + " from "
        + commentPK.getTableName() + " where commentFatherId  = ? "
        + " order by commentDate DESC";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(qcPK.getId()).intValue());
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        comment = getCommentFromResultSet(rs, qcPK);
        list.add(comment);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param qcPK
   * @throws SQLException
   * @see
   */
  public static void deleteComments(Connection con, QuestionContainerPK qcPK)
      throws SQLException {
    SilverTrace.info("questionContainer",
        "QuestionContainerDAO.deleteComments()", "root.MSG_GEN_ENTER_METHOD",
        "qcPK = " + qcPK);
    CommentPK commentPK = new CommentPK("unknown", qcPK);

    String deleteStatement = "delete from " + commentPK.getTableName()
        + " where commentFatherId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, new Integer(qcPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}