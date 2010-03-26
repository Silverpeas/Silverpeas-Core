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
