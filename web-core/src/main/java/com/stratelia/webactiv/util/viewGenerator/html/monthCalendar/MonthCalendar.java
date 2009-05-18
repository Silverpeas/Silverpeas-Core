/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * SilverpeasCalendar.java
 * 
 * Created on 11 juin 2001, 14:38
 */

package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Date;
import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * 
 * @author  groccia
 * @version
 */
public interface MonthCalendar extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @param currentDate
     *
     * @see
     */
    public void setCurrentMonth(Date currentDate);

    /**
     * Method declaration
     *
     *
     * @param listEventMonth
     *
     * @see
     */
    public void addEvent(Vector listEventMonth);

    /**
     * Method declaration
     *
     *
     * @param eventMonth
     *
     * @see
     */
    public void addEvent(Event eventMonth);

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print();

}
