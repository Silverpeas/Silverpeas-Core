/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * SilverpeasCalendar.java
 * 
 * Created on 11 juin 2001, 14:38
 */

package com.stratelia.webactiv.util.viewGenerator.html.calendar;

import java.util.List;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;

/**
 * 
 * @author groccia
 * @version
 */
public interface Calendar extends SimpleGraphicElement {
  public void setEvents(List events);

  public void addEvent(Event event);

  public void setWeekDayStyle(String value);

  public void setMonthDayStyle(String value);

  public void setMonthVisible(boolean value);

  public void setNavigationBar(boolean value);

  public void setShortName(boolean value);

  public void setNonSelectableDays(List nonSelectableDays);

  public void setEmptyDayNonSelectable(boolean nonSelectable);

  public String print();

}
