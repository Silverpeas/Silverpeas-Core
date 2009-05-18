/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.viewGenerator.html.calendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;

/**
 * Class declaration
 *
 *
 * @author
 */
public class CalendarWA1 extends AbstractCalendar
{
	public CalendarWA1(String context, String language, Date date)
	{
		super(context, language, date);
	}
	
    public String print()
    {
        StringBuffer 		result = new StringBuffer(255);
        List nonSelectableDays = getNonSelectableDays();
        boolean nonSelectable = isEmptyDayNonSelectable();
        
        int firstDayOfWeek = Integer.parseInt(settings.getString("GML.weekFirstDay"));

        if (!shortName)
        {
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"1\" CELLPADDING=\"2\">");
        }
        else
        {
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"1\">");
        }

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(getCurrentDate());
        
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
        
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // calcul du nombre de jour dans le mois
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        int numDays = calendar.get(Calendar.DAY_OF_MONTH);

        // calcul du jour de depart
        calendar.setTime(getCurrentDate());
        int startDay = 1;

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek)
        {
            calendar.add(Calendar.DATE, -1);
            startDay++;
        }

        if (monthVisible)
        {
            result.append("<TR class=\"txtnav2\"><TD COLSPAN=7>\n");
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"0\"><TR>");
            if (navigationBar)
            {
                result.append("<td class=\"intfdcolor3\" align=\"right\"><a href=\"javascript:onClick=gotoPreviousMonth()\" onMouseOut=\"MM_swapImgRestore()\" onMouseOver=\"MM_swapImage('fle-2','','").append(getContext()).append("icons/cal_fle-gon.gif',1)\"><img name=\"fle-2\" border=\"0\" src=\"").append(getContext()).append("icons/cal_fle-goff.gif\" width=\"8\" height=\"14\"></a></td> \n");
            }
            result.append("<TD class=\"intfdcolor3\" ALIGN=\"center\"><span class=txtNav4>").append(settings.getString("GML.mois" + month)).append(" ").append(year).append("</span></TD>");
            if (navigationBar)
            {
                result.append("<td class=\"intfdcolor3\" align=\"left\"><a href=\"javascript:onClick=gotoNextMonth()\" onMouseOut=\"MM_swapImgRestore()\" onMouseOver=\"MM_swapImage('fle-1','','").append(getContext()).append("icons/cal_fle-don.gif',1)\"><img name=\"fle-1\" border=\"0\" src=\"").append(getContext()).append("icons/cal_fle-doff.gif\" width=\"8\" height=\"14\"></a></td>\n");
            }
            result.append("</TR></TABLE>\n");
            result.append("</TD></tr>");
        }
        result.append("<TR class=\"intfdcolor2\">\n");

        do
        {
            if (shortName)
            {
                result.append("<TH ").append(weekDayStyle).append(">").append(settings.getString("GML.shortJour" + calendar.get(Calendar.DAY_OF_WEEK))).append("</TH>");
            }
            else
            {
                result.append("<TH ").append(weekDayStyle).append(">").append(settings.getString("GML.jour" + calendar.get(Calendar.DAY_OF_WEEK))).append("</TH>");
            }
            calendar.add(Calendar.DATE, 1);
        }
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

        result.append("</TR>\n");

        // put blank table entries for days of week before beginning of the month
        result.append("<TR>\n");
        int column = 0;

        for (int i = 0; i < startDay - 1; i++)
        {
            result.append("<TD ").append(monthDayStyle).append(" width=\"14%\">&nbsp;</TD>");
            column++;
        }

        // Record in HashSet all the days of the month with an event
        calendar.setTime(getCurrentDate());
    	String dayStyle = monthDayStyle;
        HashSet dayWithEvents = new HashSet();
    	
		Collection events = getEvents();
		if (events != null)
		{
			for (Iterator i = events.iterator(); i.hasNext();)
			{
				Event event = (Event) i.next();
		
				Calendar calendarEvents = Calendar.getInstance();
				calendarEvents.setTime(event.getStartDate());
				int currentMonth = calendar.get(Calendar.MONTH);
				
				while (calendarEvents.getTime().compareTo(event.getEndDate())<=0)
				{
					if (calendarEvents.get(Calendar.MONTH)==currentMonth)
					{
						int dayNumber = calendarEvents.get(Calendar.DAY_OF_MONTH);
						dayWithEvents.add(new Integer(dayNumber));
					}
					calendarEvents.add(Calendar.DATE, 1);
				}
			}
		}
        dayStyle = monthDayStyleEvent;

        boolean 			isSelectableDate 	= true;
        Date 				currentDate 		= null;
		String  			d					= null;
		for (int i = 1; i <= numDays; i++)
        {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
            
            // Write the day
            currentDate = calendar.getTime();
            d 			= DateUtil.getInputDate(currentDate, language);

            if (nonSelectableDays != null && nonSelectableDays.contains(currentDate))
            	isSelectableDate = false;
            else
				isSelectableDate = true;
            
            //If day has events
            dayStyle = monthDayStyle;
            boolean isSelectable = true;
            // si "nonSelectable = true" on fait la diférence entre les jours avec évènements et ceux sans évènements
            if (nonSelectable)
            	isSelectable = false;
            if (dayWithEvents.contains(new Integer(i)))
            {
            	dayStyle = monthDayStyleEvent;
            	isSelectable = true;
            }
            
            if (isSelectableDate)
            {
            	if (isSelectable)
            		result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\"><A ").append(dayStyle).append(" HREF=\"javascript:selectDay('").append(d).append("')\">").append(i).append("</A></TD>\n");
            	else
            		result.append("<TD width=\"14%\" ").append(monthDayStyle).append(" align=\"center\">").append(i).append("</TD>\n");
            }
            else
				result.append("<TD width=\"14%\" ").append(monthDayStyle).append(" align=\"center\">").append(i).append("</TD>\n");

            // Check for end of week/row
            if ((++column == 7) && (numDays > i))
            {
                result.append("</TR>\n<TR>");
                column = 0;
            }
        }
        for (int i = column; i <= 6; i++)
        {
            result.append("<TD ").append(monthDayStyle).append(">&nbsp;</TD>\n");
        }
        result.append("</TR></TABLE>\n");

        SilverTrace.debug("viewGenerator", "CalendarWA1.print()", "result="+result);

        return result.toString();
    }

}