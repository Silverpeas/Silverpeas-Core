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

package org.silverpeas.core.questioncontainer.container.dao;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.container.model.Comment;
import org.silverpeas.core.questioncontainer.container.model.CommentPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class is made to access database only (table SB_QuestionContainer_QC and
 * SB_QuestionContainer_Comment)
 * @author neysseri
 */
public class QuestionContainerDAO {

  public static final String QUESTIONCONTAINERCOLUMNNAMES =
      "qcId, qcTitle, qcDescription, qcComment, qcCreatorId, qcCreationDate, qcBeginDate, " +
          "qcEndDate, qcIsClosed, qcNbVoters, qcNbQuestionsPage, qcNbMaxParticipations, " +
          "qcNbTriesBeforeSolution, qcMaxTime, instanceId, anonymous, resultMode, resultView";
  public static final String COMMENTCOLUMNNAMES =
      "commentId, commentFatherId, userId, commentComment, commentIsAnonymous, commentDate";

  private static final String SQL_GET_QUESTIONCONTAINER =
      "SELECT " + QUESTIONCONTAINERCOLUMNNAMES + " FROM SB_QuestionContainer_QC WHERE qcId = ? ";

  private static final String SQL_CLOSE_QUESTIONCONTAINER =
      "UPDATE SB_QuestionContainer_QC SET qcIsClosed = 1 , instanceId = ? WHERE qcId = ? ";

  private static final String SQL_OPEN_QUESTIONCONTAINER =
      "UPDATE SB_QuestionContainer_QC SET qcIsClosed = 0 , instanceId = ?" + " WHERE qcId = ? ";

  private static final String SQL_DELETE_COMMENT =
      "DELETE FROM SB_QuestionContainer_Comment WHERE commentFatherId = ? ";

  private static final String SQL_DELETE_ALL_QUESTIONCONTAINERS =
      "DELETE FROM SB_QuestionContainer_QC WHERE instanceId = ?";

  private static final String SQL_DELETE_ALL_COMMENTS =
      "DELETE FROM SB_QuestionContainer_Comment WHERE commentFatherId in (SELECT qcId FROM " +
          "SB_QuestionContainer_QC WHERE instanceId = ?)";

  // the date format used in database to represent a date
  private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  // if beginDate is null, it will be replace in database with it
  private static final String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private static final String nullEndDate = "9999/99/99";

  /**
   * Transform a ResultSet into a QuestionContainerHeader object
   * @param rs the ResultSet
   * @param questionContainerPK the question container Primary Key
   * @return a QuestionContainerHeader
   * @throws SQLException
   */
  static QuestionContainerHeader getQuestionContainerHeaderFromResultSet(ResultSet rs,
      QuestionContainerPK questionContainerPK) throws SQLException {
    String id = Integer.toString(rs.getInt(1));
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
    String instanceId = rs.getString(15);
    boolean anonymous = (rs.getInt(16) == 1);
    int resultMode = rs.getInt(17);
    int resultView = rs.getInt(18);

    questionContainerPK.setComponentName(instanceId);

    QuestionContainerHeader result =
        new QuestionContainerHeader(new QuestionContainerPK(id, questionContainerPK), title,
            description, comment, creatorId, creationDate, beginDate, endDate, closed, nbVoters,
            nbQuestionsPage, nbMaxParticipations, nbParticipationsBeforeSolution, maxTime,
            anonymous, resultMode, resultView);
    return result;
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @return a collection of Question Container Header
   * @throws SQLException
   */
  public static Collection<QuestionContainerHeader> getQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {


    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader;

    String selectStatement =
        "SELECT " + QUESTIONCONTAINERCOLUMNNAMES + " FROM " + questionContainerPK.getTableName() +
            " WHERE instanceId = '" + questionContainerPK.getComponentName() + "' " +
            " ORDER BY qcBeginDate DESC, qcEndDate DESC";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      List<QuestionContainerHeader> list = new ArrayList<>();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static Collection<QuestionContainerHeader> getQuestionContainers(Connection con,
      List<QuestionContainerPK> pks) throws SQLException {


    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader;
    QuestionContainerPK questionContainerPK = new QuestionContainerPK("unknown");
    List<QuestionContainerHeader> list = new ArrayList<>();
    StringBuilder whereClause = new StringBuilder();

    if (pks != null && !pks.isEmpty()) {
      Iterator<QuestionContainerPK> it = pks.iterator();
      QuestionContainerPK pk = null;

      whereClause.append("(");
      while (it.hasNext()) {
        pk = it.next();
        whereClause.append(" qcId = ").append(pk.getId());
        if (it.hasNext()) {
          whereClause.append(" or ");
        } else {
          whereClause.append(" ) ");
        }
      }

      String selectStatement =
          "select " + QUESTIONCONTAINERCOLUMNNAMES + " from " + questionContainerPK.getTableName() +
              " where " + whereClause.toString() + " and instanceId = '" + pk.getComponentName() +
              "' order by qcBeginDate DESC, qcEndDate DESC";
      Statement stmt = null;

      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery(selectStatement);
        while (rs.next()) {
          questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, pk);
          list.add(questionContainerHeader);
        }
      } finally {
        DBUtil.close(rs, stmt);
      }
    }
    return list;
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @return
   * @throws SQLException
   */
  public static Collection<QuestionContainerHeader> getOpenedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader;

    String selectStatement =
        "select " + QUESTIONCONTAINERCOLUMNNAMES + " from " + questionContainerPK.getTableName() +
            " where ? between qcBeginDate and qcEndDate " +
            " and qcIsClosed = 0 and instanceId = ? order by  qcBeginDate DESC, qcEndDate DESC";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, questionContainerPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<QuestionContainerHeader> list = new ArrayList<>();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con the database connection
   * @param questionContainerPK
   * @return
   * @throws SQLException
   */
  public static Collection<QuestionContainerHeader> getNotClosedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {


    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader;

    String selectStatement =
        "select " + QUESTIONCONTAINERCOLUMNNAMES + " from " + questionContainerPK.getTableName() +
            " where instanceId = '" + questionContainerPK.getComponentName() + "' " +
            " and qcIsClosed = 0 order by qcBeginDate DESC, qcEndDate DESC";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      List<QuestionContainerHeader> list = new ArrayList<>();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @return
   * @throws SQLException
   */
  public static Collection<QuestionContainerHeader> getClosedQuestionContainers(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {


    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader;

    String selectStatement =
        "select " + QUESTIONCONTAINERCOLUMNNAMES + " from " + questionContainerPK.getTableName() +
            " where ( ? > qcEndDate " + " or qcIsClosed = 1 )" + " and instanceId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, questionContainerPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<QuestionContainerHeader> list = new ArrayList<>();
      while (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
        list.add(questionContainerHeader);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param qcPK
   * @return
   * @throws SQLException
   */
  public static Collection<QuestionContainerHeader> getInWaitQuestionContainers(Connection con,
      QuestionContainerPK qcPK) throws SQLException {

    ResultSet rs = null;
    QuestionContainerHeader header;
    String selectStatement =
        "select " + QUESTIONCONTAINERCOLUMNNAMES + " from " + qcPK.getTableName() +
            " where ? < qcBeginDate and instanceId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, formatter.format(new java.util.Date()));
      prepStmt.setString(2, qcPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<QuestionContainerHeader> list = new ArrayList<>();
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
   * @param con the database connection
   * @param questionContainerPK
   * @return
   * @throws SQLException
   */
  public static QuestionContainerHeader getQuestionContainerHeader(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {

    ResultSet rs = null;
    QuestionContainerHeader questionContainerHeader = null;
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_GET_QUESTIONCONTAINER);
      prepStmt.setInt(1, Integer.parseInt(questionContainerPK.getId()));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        questionContainerHeader = getQuestionContainerHeaderFromResultSet(rs, questionContainerPK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return questionContainerHeader;
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @throws SQLException
   */
  public static void closeQuestionContainer(Connection con, QuestionContainerPK questionContainerPK)
      throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_CLOSE_QUESTIONCONTAINER);
      prepStmt.setString(1, questionContainerPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(questionContainerPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @throws SQLException
   */
  public static void openQuestionContainer(Connection con, QuestionContainerPK questionContainerPK)
      throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_OPEN_QUESTIONCONTAINER);
      prepStmt.setString(1, questionContainerPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(questionContainerPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param questionContainerHeader
   * @return
   * @throws SQLException
   */
  public static QuestionContainerPK createQuestionContainerHeader(Connection con,
      QuestionContainerHeader questionContainerHeader) throws SQLException {

    int newId;

    String insertStatement = "insert into " + questionContainerHeader.getPK().getTableName() +
        " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    try {
      /* Retrieve next sequence identifier */
      newId = DBUtil.getNextId(questionContainerHeader.getPK().getTableName(), "qcId");
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(
          "QuestionContainerDAO.createQuestionContainerHeader()", SilverpeasRuntimeException.ERROR,
          "root.EX_GET_NEXTID_FAILED", e);
    }

    QuestionContainerPK questionContainerPK = questionContainerHeader.getPK();

    questionContainerPK.setId(Integer.toString(newId));

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
      if ((questionContainerHeader.getEndDate() == null) ||
          (questionContainerHeader.getEndDate().length() < 10)) {
        prepStmt.setString(8, nullEndDate);
      } else {
        prepStmt.setString(8, questionContainerHeader.getEndDate());
      }
      prepStmt.setInt(9, 0);
      prepStmt.setInt(10, 0);
      prepStmt.setInt(11, questionContainerHeader.getNbQuestionsPerPage());
      prepStmt.setInt(12, questionContainerHeader.getNbMaxParticipations());
      prepStmt.setInt(13, questionContainerHeader.getNbParticipationsBeforeSolution());
      prepStmt.setInt(14, questionContainerHeader.getMaxTime());
      prepStmt.setString(15, questionContainerHeader.getPK().getComponentName());
      if (questionContainerHeader.isAnonymous()) {
        prepStmt.setInt(16, 1);
      } else {
        prepStmt.setInt(16, 0);
      }
      prepStmt.setInt(17, questionContainerHeader.getResultMode());
      prepStmt.setInt(18, questionContainerHeader.getResultView());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return questionContainerPK;
  }

  /**
   * @param con the database connection
   * @param questionContainerHeader
   * @throws SQLException
   */
  public static void updateQuestionContainerHeader(Connection con,
      QuestionContainerHeader questionContainerHeader) throws SQLException {


    String insertStatement =
        "update " + questionContainerHeader.getPK().getTableName() + " set qcTitle = ?," +
            " qcDescription = ?," + " qcComment = ?," + " qcBeginDate = ?," + " qcEndDate = ?," +
            " qcNbVoters = ?," + " qcNbQuestionsPage = ?," + " qcNbMaxParticipations = ?," +
            " qcNbTriesBeforeSolution = ?," + " qcMaxTime = ?, " + " instanceId = ?, " +
            " anonymous = ?, " + " resultMode = ?, " + " resultView = ?" + " where qcId = ?";

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
      prepStmt.setInt(9, questionContainerHeader.getNbParticipationsBeforeSolution());
      prepStmt.setInt(10, questionContainerHeader.getMaxTime());
      prepStmt.setString(11, questionContainerHeader.getPK().getComponentName());
      if (questionContainerHeader.isAnonymous()) {
        prepStmt.setInt(12, 1);
      } else {
        prepStmt.setInt(12, 0);
      }
      prepStmt.setInt(13, questionContainerHeader.getResultMode());
      prepStmt.setInt(14, questionContainerHeader.getResultView());
      prepStmt.setInt(15, Integer.parseInt(questionContainerHeader.getPK().getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @throws SQLException
   */
  public static void deleteQuestionContainerHeader(Connection con,
      QuestionContainerPK questionContainerPK) throws SQLException {


    String deleteStatement =
        "delete from " + questionContainerPK.getTableName() + " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(questionContainerPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param questionContainerPK
   * @throws SQLException
   */
  public static void addAVoter(Connection con, QuestionContainerPK questionContainerPK)
      throws SQLException {
    SilverTrace
        .info("questionContainer", "QuestionContainerDAO.addAVoter()", "root.MSG_GEN_ENTER_METHOD",
            "questionContainerPK = " + questionContainerPK);

    String updateStatement =
        "update " + questionContainerPK.getTableName() + " set qcNbVoters = qcNbVoters + 1 " +
            " where qcId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, Integer.parseInt(questionContainerPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param rs the ResultSet
   * @param qcPK the the question container identifier
   * @return a Comment from ResultSet
   * @throws SQLException
   */
  private static Comment getCommentFromResultSet(ResultSet rs, QuestionContainerPK qcPK)
      throws SQLException {

    String id = Integer.toString(rs.getInt(1));
    String userId = rs.getString(3);
    String comment = rs.getString(4);
    boolean isAnonymous = (rs.getInt(5) == 1);
    String date = rs.getString(6);
    Comment result = new Comment(new CommentPK(id, qcPK), qcPK, userId, comment, isAnonymous, date);
    return result;
  }

  /**
   * Add a comment
   * @param con the database connection
   * @param comment the comment to add
   * @throws SQLException
   */
  public static void addComment(Connection con, Comment comment) throws SQLException {
    SilverTrace
        .info("questionContainer", "QuestionContainerDAO.addComment()", "root.MSG_GEN_ENTER_METHOD",
            "comment = " + comment);
    QuestionContainerPK questionContainerPK = comment.getQuestionContainerPK();
    CommentPK commentPK = new CommentPK("unknown", questionContainerPK);
    int newId;

    String insertStatement =
        "insert into " + commentPK.getTableName() + " values(?, ?, ?, ?, ?, ?) ";

    try {
      newId = DBUtil.getNextId(commentPK.getTableName(), "commentId");
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException("QuestionContainerDAO.addComment()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(questionContainerPK.getId()));
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
   * Retrieve the list of comments from question container primary key
   * @param con the database connection
   * @param qcPK the question container identifier
   * @return a collection of comment
   * @throws SQLException
   */
  public static Collection<Comment> getComments(Connection con, QuestionContainerPK qcPK)
      throws SQLException {


    ResultSet rs = null;
    Comment comment;
    CommentPK commentPK = new CommentPK("unknown", qcPK);

    String selectStatement = "select " + COMMENTCOLUMNNAMES + " from " + commentPK.getTableName() +
        " where commentFatherId  = ? " + " order by commentDate DESC";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(qcPK.getId()));
      rs = prepStmt.executeQuery();
      List<Comment> list = new ArrayList<>();
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
   * Delete comments identified with given parameter question container identifier
   * @param con the database connection
   * @param qcPK the question container identifier
   * @throws SQLException
   */
  public static void deleteComments(Connection con, QuestionContainerPK qcPK) throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_DELETE_COMMENT);
      prepStmt.setInt(1, Integer.parseInt(qcPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Deletes all the question containers and as well their comments that were created for the
   * specified component instance.
   * @param con the connection to the data source in which are stored both the question containers
   * and the comments.
   * @param instanceId the unique identifier of the component instance.
   * @throws SQLException if an error occurs while deleting the question containers or their
   * comments.
   */
  public static void deleteAllQuestionContainersByInstanceId(Connection con, String instanceId)
      throws SQLException {
    try (PreparedStatement deletion = con.prepareStatement(SQL_DELETE_ALL_COMMENTS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
    try (PreparedStatement deletion = con.prepareStatement(SQL_DELETE_ALL_QUESTIONCONTAINERS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

}