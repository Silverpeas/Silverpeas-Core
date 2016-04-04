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

/*
 * Created on 25 oct. 2004
 *
 */
package org.silverpeas.core.calendar.service;

import org.silverpeas.core.calendar.model.HolidayDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author neysseri
 */
public class HolidaysDAO {

  private final static String AGENDA_HOLIDAYS_TABLENAME = "sb_agenda_holidays";

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException, UtilException {


    if (!isHolidayDate(con, holiday)) {
      StringBuilder insertStatement = new StringBuilder(128);
      insertStatement.append("insert into ").append(AGENDA_HOLIDAYS_TABLENAME);
      insertStatement.append(" values ( ? , ? )");
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(insertStatement.toString());

        prepStmt.setInt(1, Integer.parseInt(holiday.getUserId()));
        prepStmt.setString(2, DateUtil.date2SQLDate(holiday.getDate()));

        prepStmt.executeUpdate();
      } finally {
        DBUtil.close(prepStmt);
      }
    }
  }

  public static void removeHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(AGENDA_HOLIDAYS_TABLENAME);
    deleteStatement.append(" where holidayDate = ? ");
    deleteStatement.append(" and userId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());

      prepStmt.setString(1, DateUtil.date2SQLDate(holiday.getDate()));
      prepStmt.setInt(2, Integer.parseInt(holiday.getUserId()));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static boolean isHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuilder query = new StringBuilder(128);
    query.append("select * ");
    query.append("from ").append(AGENDA_HOLIDAYS_TABLENAME);
    query.append(" where holidayDate = ? ");
    query.append(" and userId = ? ");



    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());

      stmt.setString(1, DateUtil.date2SQLDate(holiday.getDate()));
      stmt.setInt(2, Integer.parseInt(holiday.getUserId()));

      rs = stmt.executeQuery();

      return rs.next();
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<String> getHolidayDates(Connection con, String userId)
      throws SQLException {
    List<String> holidayDates = new ArrayList<String>();
    StringBuilder query = new StringBuilder(128);
    query.append("select * ");
    query.append("from ").append(AGENDA_HOLIDAYS_TABLENAME);
    query.append(" where userId = ? ");
    query.append("order by holidayDate ASC");



    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, Integer.parseInt(userId));
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(rs.getString("holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static List<String> getHolidayDates(Connection con, String userId,
      Date beginDate, Date endDate) throws SQLException {
    List<String> holidayDates = new ArrayList<String>();
    StringBuilder query = new StringBuilder(128);
    query.append("select * ");
    query.append("from ").append(AGENDA_HOLIDAYS_TABLENAME);
    query.append(" where userId = ? ");
    query.append(" and ? <= holidayDate ");
    query.append(" and holidayDate <= ? ");
    query.append("order by holidayDate ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, Integer.parseInt(userId));
      stmt.setString(2, DateUtil.date2SQLDate(beginDate));
      stmt.setString(3, DateUtil.date2SQLDate(endDate));
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(rs.getString("holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

}