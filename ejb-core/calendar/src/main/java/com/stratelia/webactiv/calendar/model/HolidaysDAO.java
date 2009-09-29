/*
 * Created on 25 oct. 2004
 *
 */
package com.stratelia.webactiv.calendar.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author neysseri
 * 
 */
public class HolidaysDAO {

  // the date format used in database to represent a date
  private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
  private final static String AGENDA_HOLIDAYS_TABLENAME = "sb_agenda_holidays";

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException, UtilException {
    SilverTrace.info("calendar", "HolidaysDAO.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", holiday.getDate().toString());

    if (!isHolidayDate(con, holiday)) {
      StringBuffer insertStatement = new StringBuffer(128);
      insertStatement.append("insert into ").append(AGENDA_HOLIDAYS_TABLENAME);
      insertStatement.append(" values ( ? , ? )");
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(insertStatement.toString());

        prepStmt.setInt(1, new Integer(holiday.getUserId()).intValue());
        prepStmt.setString(2, formatter.format(holiday.getDate()));

        prepStmt.executeUpdate();
      } finally {
        DBUtil.close(prepStmt);
      }
    }
  }

  public static void removeHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
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
    StringBuffer query = new StringBuffer(128);
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

  public static List getHolidayDates(Connection con, String userId)
      throws SQLException {
    List holidayDates = new ArrayList();
    StringBuffer query = new StringBuffer(128);
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
        // holidayDates.add(dbDate2Date(rs.getString("holidayDate"),
        // "holidayDate"));
        holidayDates.add(rs.getString("holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static List getHolidayDates(Connection con, String userId,
      Date beginDate, Date endDate) throws SQLException {
    List holidayDates = new ArrayList();
    StringBuffer query = new StringBuffer(128);
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
        // holidayDates.add(dbDate2Date(rs.getString("holidayDate"),
        // "holidayDate"));
        holidayDates.add(rs.getString("holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

}