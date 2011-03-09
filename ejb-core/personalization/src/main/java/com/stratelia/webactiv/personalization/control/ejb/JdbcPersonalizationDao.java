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
package com.stratelia.webactiv.personalization.control.ejb;

import com.silverpeas.personalization.dao.PersonalizationDao;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.personalization.model.PersonalizeDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Class declaration
 * @author
 */
public class JdbcPersonalizationDao implements PersonalizationDao {

    /**
   * Method declaration
   * @param rs
   * @return
   * @throws SQLException
   * @see
   */
  private PersonalizeDetail getPersonalizeDetailFromResultSet(
      ResultSet rs) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.getPersonalizeDetailFromResultSet()",
        "root.MSG_GEN_ENTER_METHOD");
    String languages = rs.getString(2);
    String look = rs.getString(3);
    String personalWS = rs.getString(4);
    int thesaurus = rs.getInt(5);
    int dragAndDrop = rs.getInt(6);
    int onlineEditing = rs.getInt(7);
    int webdavEditing = rs.getInt(8);
    boolean thesaurusStatus = false;
    boolean dragDropStatus = false;
    boolean onlineEditingStatus = false;
    boolean webdavEditingStatus = false;
    if (thesaurus == 1)
      thesaurusStatus = true;
    if (dragAndDrop == 1)
      dragDropStatus = true;
    if (onlineEditing == 1)
      onlineEditingStatus = true;
    if (webdavEditing == 1)
      webdavEditingStatus = true;

    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.getPersonalizeDetailFromResultSet()",
        "root.MSG_GEN_PARAM_VALUE", "thesaurusStatus = "
        + new Boolean(thesaurusStatus).toString() + ", dragDropStatus = "
        + new Boolean(dragDropStatus).toString()
        + ", onlineEditingStatus = "
        + new Boolean(onlineEditingStatus).toString());
    PersonalizeDetail result = new PersonalizeDetail(languages,
        look, personalWS, thesaurusStatus, dragDropStatus, onlineEditingStatus,
        webdavEditingStatus);
    return result;
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @return
   * @throws SQLException
   * @see
   */
  @Override
  public PersonalizeDetail getPersonalizeDetail(Connection con,
      String userId) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.getPersonalizeDetail()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    ResultSet rs = null;
    PersonalizeDetail personalizeDetail = null;
    String selectStatement = "select " + PERSONALCOLUMNNAMES + " from "
        + PERSONALTABLENAME + " where id = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        personalizeDetail = getPersonalizeDetailFromResultSet(rs);
      }
      return personalizeDetail;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param language
   * @throws SQLException
   * @see
   */
  @Override
  public void setLanguage(Connection con, String userId,
      String language) throws SQLException {
    SilverTrace.info("personalization", "JdbcPersonalizationDao.setLanguage()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME
        + " set languages = ? " + " where id = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, language);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  @Override
  public void setPersonalWorkSpace(Connection con, String userId,
      String spaceId) throws SQLException {
    String updateStatement = "update " + PERSONALTABLENAME
        + " set personalWSpace = ? where id = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateStatement);
      if (StringUtil.isDefined(spaceId)) {
        prepStmt.setString(1, spaceId);
      } else {
        prepStmt.setNull(1, Types.VARCHAR);
      }
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param look
   * @throws SQLException
   * @see
   */
  @Override
  public void setFavoriteLook(Connection con, String userId, String look)
      throws SQLException {
    SilverTrace.info("personalization", "JdbcPersonalizationDao.setLook()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME + " set look = ? "
        + " where id = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, look);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param thesaurusStatus
   * @throws SQLException
   * @see
   */
  @Override
  public void setThesaurusStatus(Connection con, String userId,
      boolean thesaurusStatus) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.setThesaurusStatus()", "root.MSG_GEN_ENTER_METHOD",
        "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME
        + " set thesaurusStatus = ? " + " where id = ? ";
    PreparedStatement prepStmt = null;
    int thesaurus = thesaurusStatus ? 1 : 0;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, thesaurus);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param dragAndDropStatus
   * @throws SQLException
   * @see
   */
  @Override
  public  void setDragAndDropStatus(Connection con, String userId,
      boolean dragAndDropStatus) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.setDragAndDropStatus()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME
        + " set dragAndDropStatus = ? " + " where id = ? ";
    PreparedStatement prepStmt = null;
    int dragDrop = dragAndDropStatus ? 1 : 0;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, dragDrop);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param onlineEditingStatus
   * @throws SQLException
   * @see
   */
  @Override
  public void setOnlineEditingStatus(Connection con, String userId,
      boolean onlineEditingStatus) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.setOnlineEditingStatus()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME
        + " set onlineEditingStatus = ? " + " where id = ? ";
    PreparedStatement prepStmt = null;
    int onlineEditing = onlineEditingStatus ? 1 : 0;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, onlineEditing);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param webdavEditingStatus
   * @throws SQLException
   * @see
   */
  @Override
  public void setWebdavEditingStatus(Connection con, String userId,
      boolean webdavEditingStatus) throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.setWebdavEditingStatus()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "update " + PERSONALTABLENAME
        + " set webdavEditingStatus = ? " + " where id = ? ";
    PreparedStatement prepStmt = null;
    int webdavEditing = webdavEditingStatus ? 1 : 0;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, webdavEditing);
      prepStmt.setString(2, userId);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param userId
   * @param language
   * @param look
   * @param thesaurusStatus
   * @throws SQLException
   * @see
   */
  @Override
  public void insertPersonalizeDetail(Connection con, String userId,
      String language, String look, String defaultPersonalWSId,
      boolean thesaurusStatus, boolean dragAndDropStatus,
      boolean onlineEditingStatus, boolean webdavEditingStatus)
      throws SQLException {
    SilverTrace.info("personalization",
        "JdbcPersonalizationDao.insertPersonalizeDetail()",
        "root.MSG_GEN_ENTER_METHOD", "userId =" + userId);
    String updateStatement = "insert into " + PERSONALTABLENAME
        + " values (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;

    int thesaurus = thesaurusStatus ? 1 : 0;
    int dragAndDrop = dragAndDropStatus ? 1 : 0;
    int onlineEditing = onlineEditingStatus ? 1 : 0;
    int webdavEditing = webdavEditingStatus ? 1 : 0;
    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, language);
      prepStmt.setString(3, look);
      prepStmt.setString(4, defaultPersonalWSId);
      prepStmt.setInt(5, thesaurus);
      prepStmt.setInt(6, dragAndDrop);
      prepStmt.setInt(7, onlineEditing);
      prepStmt.setInt(8, webdavEditing);
      prepStmt.executeUpdate();
    } finally {
      if (prepStmt != null) {
        prepStmt.close();
      }
    }
  }

}
