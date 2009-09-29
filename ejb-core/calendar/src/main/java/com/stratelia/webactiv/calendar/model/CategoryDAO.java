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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
//TODO : reporter dans CVS (done)
package com.stratelia.webactiv.calendar.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;

public class CategoryDAO {

  public static final String CATEGORYCOLUMNNAMES = "CalendarCategory.categoryId, CalendarCategory.name";

  // public static final String CATEGORYJOURNALASSOCOLUMNNAMES =
  // "journalId, categoryId";

  public static Category getCategoryFromResultSet(ResultSet rs)
      throws SQLException {
    String categoryId = rs.getString(1);
    String name = rs.getString(2);
    Category result = new Category(categoryId, name);
    return result;
  }

  public static Collection getJournalCategories(Connection con, String journalId)
      throws SQLException {
    String selectStatement = "select "
        + CategoryDAO.CATEGORYCOLUMNNAMES
        + " from CalendarCategory, CalendarJournalCategory "
        + " where journalId = ? and CalendarCategory.categoryId = CalendarJournalCategory.categoryId";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(journalId).intValue());
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        Category category = getCategoryFromResultSet(rs);
        list.add(category);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

  }

  public static Category getCategory(Connection con, String categoryId)
      throws SQLException {
    String selectStatement = "select " + CategoryDAO.CATEGORYCOLUMNNAMES
        + " from CalendarCategory " + " where categoryId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      Category category = null;
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, categoryId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        category = getCategoryFromResultSet(rs);
      }
      return category;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getAllCategories(Connection con) throws SQLException {
    String selectStatement = "select " + CategoryDAO.CATEGORYCOLUMNNAMES
        + " from CalendarCategory ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    ArrayList list = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      list = new ArrayList();
      while (rs.next()) {
        Category category = getCategoryFromResultSet(rs);
        list.add(category);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static void addJournalCategory(Connection con, String journalId,
      String categoryId) throws SQLException {
    PreparedStatement prepStmt = null;

    try {
      String insertStatement = "insert into CalendarJournalCategory (journalId, categoryId) "
          + " values (?, ?)";
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(journalId).intValue());
      prepStmt.setString(2, categoryId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeJournalCategory(Connection con, String journalId,
      String categoryId) throws SQLException {
    PreparedStatement prepStmt = null;

    try {
      String statement = "delete from CalendarJournalCategory "
          + "where journalId = ? and categoryId = ?";
      prepStmt = con.prepareStatement(statement);
      prepStmt.setInt(1, new Integer(journalId).intValue());
      prepStmt.setString(2, categoryId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeJournal(Connection con, String id)
      throws SQLException {
    PreparedStatement prepStmt = null;

    try {
      String statement = "delete from CalendarJournalCategory "
          + "where journalId = ?";
      prepStmt = con.prepareStatement(statement);
      prepStmt.setInt(1, new Integer(id).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}
