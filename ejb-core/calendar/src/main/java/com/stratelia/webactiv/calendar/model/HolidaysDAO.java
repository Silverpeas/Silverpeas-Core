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

/*
 * Created on 25 oct. 2004
 *
 */
package com.stratelia.webactiv.calendar.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author neysseri
 */
public class HolidaysDAO {

  private final static String AGENDA_HOLIDAYS_TABLENAME = "sb_agenda_holidays";

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException, UtilException {
    SilverTrace.info("calendar", "HolidaysDAO.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", holiday.getDate().toString());

    if (!isHolidayDate(con, holiday)) {
      StringBuilder insertStatement = new StringBuilder(128);
      insertStatement.append("insert into ").append(AGENDA_HOLIDAYS_TABLENAME);
      insertStatement.append(" values ( ? , ? )");
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(insertStatement.toString());

        prepStmt.setInt(1, new Integer(holiday.getUserId()).intValue());
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
      prepStmt.setInt(2, new Integer(holiday.getUserId()).intValue());

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

    SilverTrace.info("calendar", "HolidaysDAO.isHolidayDate()",
        "root.MSG_GEN_PARAM_VALUE", "date = " + holiday.getDate().toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());

      stmt.setString(1, DateUtil.date2SQLDate(holiday.getDate()));
      stmt.setInt(2, new Integer(holiday.getUserId()).intValue());

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

    SilverTrace.info("calendar", "HolidaysDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, new Integer(userId).intValue());
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

    SilverTrace.info("calendar", "HolidaysDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "userId = " + userId + ", beginDate="
        + beginDate.toString() + ", endDate=" + endDate.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setInt(1, new Integer(userId).intValue());
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