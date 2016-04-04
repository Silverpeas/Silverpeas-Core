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
 * Day.java
 * this object represent the day in the monthCalendar viewgenerator
 * @see org.silverpeas.core.web.util.viewgenerator.html.monthcalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */

package org.silverpeas.core.web.util.viewgenerator.html.monthcalendar;

import java.util.Calendar;
import java.util.Date;

/*
 * CVS Informations
 *
 * $Id: Day.java,v 1.2 2006/03/21 12:09:52 neysseri Exp $
 *
 * $Log: Day.java,v $
 * Revision 1.2  2006/03/21 12:09:52  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.5  2002/01/04 14:04:24  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * @author
 */
class Day {

  private Date date;

  private String name;

  private String numbers;

  private boolean isInThisMonth;

  /**
   * Creates new Day
   */
  public Day(Date date, String name, String numbers, boolean isInThisMonth) {
    this.date = date;

    this.name = name;
    this.numbers = numbers;
    this.isInThisMonth = isInThisMonth;
  }

  /**
   * Constructor declaration
   * @param date
   * @see
   */
  public Day(Date date) {
    this.date = date;
    this.name = null;
    this.numbers = null;
    this.isInThisMonth = false;

  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getNumbers() {
    return numbers;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getDate() {
    return date;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getIsInThisMonth() {
    return isInThisMonth;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "Date=" + date + "; name=" + name + "; isInThisMonth="
        + isInThisMonth;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isCurrentDay() {
    Calendar cal = Calendar.getInstance();
    cal.clear(Calendar.HOUR);
    cal.clear(Calendar.HOUR_OF_DAY);
    cal.clear(Calendar.MINUTE);
    cal.clear(Calendar.SECOND);
    cal.clear(Calendar.MILLISECOND);
    return date.equals(cal.getTime());
  }
}