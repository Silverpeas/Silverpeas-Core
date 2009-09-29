/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * Day.java
 * this object represent the day in the monthCalendar viewGenerator
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */

package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Calendar;
import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

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
 * 
 * 
 * @author
 */
class Day extends Object {

  private Date date = null;

  private String name = null;

  private String numbers = null;

  private boolean isInThisMonth = false;

  /**
   * Creates new Day
   */
  public Day(Date date, String name, String numbers, boolean isInThisMonth) {
    this.date = date;
    SilverTrace.info("viewgenerator", "Day()", "root.MSG_GEN_PARAM_VALUE",
        "date = " + date.toString());

    this.name = name;
    this.numbers = numbers;
    this.isInThisMonth = isInThisMonth;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param date
   * 
   * @see
   */
  public Day(Date date) {
    this.date = date;
    SilverTrace.info("viewgenerator", "Day(date)", "root.MSG_GEN_PARAM_VALUE",
        "date = " + date.toString());
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getNumbers() {
    return numbers;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Date getDate() {
    return date;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getIsInThisMonth() {
    return isInThisMonth;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String toString() {
    return "Date=" + date + "; name=" + name + "; isInThisMonth="
        + isInThisMonth;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean isCurrentDay() {
    Calendar cal = Calendar.getInstance();

    cal.clear(Calendar.HOUR);
    cal.clear(Calendar.HOUR_OF_DAY);
    cal.clear(Calendar.MINUTE);
    cal.clear(Calendar.SECOND);
    cal.clear(Calendar.MILLISECOND);

    if (date.compareTo(cal.getTime()) == 0)
      return true;
    else
      return false;
  }
}