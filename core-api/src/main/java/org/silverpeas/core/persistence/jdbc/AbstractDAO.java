/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import static org.silverpeas.core.util.DateUtil.formatDate;

/**
 * @author mmoquillon
 */
public abstract class AbstractDAO {

  protected AbstractDAO() {
  }

  /**
   * Sets the date parameter to the specified prepared statement.
   * @param statement the prepared statement
   * @param idx the index of the parameter to set
   * @param date the date to set
   * @param defaultDate the date to set by default if the date above is null
   * @throws SQLException if an error occurs.
   */
  protected static void setDateParameter(final PreparedStatement statement, int idx, final Date date,
      final String defaultDate) throws SQLException {
    if (date == null) {
      statement.setString(idx, defaultDate);
    } else {
      statement.setString(idx, formatDate(date));
    }
  }

  /**
   * Sets the text parameter to the specified prepared statement.
   * @param statement the prepared statement
   * @param idx the index of the parameter to set
   * @param value the text to set
   * @param defaultValue the text to set by default if the above value is null.
   * @throws SQLException if an error occurs.
   */
  protected static void setStringParameter(final PreparedStatement statement, int idx,
      final String value, final String defaultValue) throws SQLException {
    if (StringUtil.isDefined(value)) {
      statement.setString(idx, value);
    } else {
      statement.setString(idx, defaultValue);
    }
  }

  /**
   * Decodes the specified String representation of a date to a {@link Date} object.
   * @param dateAsSqlString the textual representation of an SQL date.
   * @param nullValue the String representation of a null date. If the value above matches this
   * representation, then null is returned.
   * @return the decoded date.
   * @throws ParseException if the text doesn't represent a date as expected.
   */
  protected static Date asDate(final String dateAsSqlString, final String nullValue)
      throws ParseException {
    final Date date;
    if (nullValue.equals(dateAsSqlString)) {
      date = null;
    } else {
      date = DateUtil.parseDate(dateAsSqlString);
    }
    return date;
  }
}
  