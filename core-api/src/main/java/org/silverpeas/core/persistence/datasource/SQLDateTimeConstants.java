/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Some constants to take care when converting dates and date times to their database counterpart.
 * @author mmoquillon
 */
public class SQLDateTimeConstants {

  private static final LocalDateTime MIN = LocalDate.parse("0001-01-01").atStartOfDay();
  private static final LocalDateTime MAX = LocalDate.parse("9999-12-31").atStartOfDay();

  private SQLDateTimeConstants() {

  }

  /**
   * The minimum date for any of our supported data sources. Currently, it is based upon the more
   * limited ones, that are Oracle and MS-SQLServer.
   */
  public static final Date MIN_DATE = Date.valueOf(MIN.toLocalDate());

  /**
   * The maximum date for any of our supported data sources. Currently, it is based upon the more
   * limited ones, that are Oracle and MS-SQLServer.
   */
  public static final Date MAX_DATE = Date.valueOf(MAX.toLocalDate());

  /**
   * The minimum timestamp for any of our supported data sources. Currently, it is based upon the
   * more limited ones, that are Oracle and MS-SQLServer.
   */
  public static final Timestamp MIN_TIMESTAMP = Timestamp.valueOf(MIN);

  /**
   * The maximum timestamp for any of our supported data sources. Currently, it is based upon the
   * more limited ones, that are Oracle and MS-SQLServer.
   */
  public static final Timestamp MAX_TIMESTAMP = Timestamp.valueOf(MAX);
}
