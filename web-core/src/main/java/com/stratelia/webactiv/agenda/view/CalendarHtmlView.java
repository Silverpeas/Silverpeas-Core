/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.agenda.view;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.SchedulableCount;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 *
 * $Id: CalendarHtmlView.java,v 1.10 2009/02/27 17:00:57 xdelorme Exp $
 *
 * $Log: CalendarHtmlView.java,v $
 * Revision 1.10  2009/02/27 17:00:57  xdelorme
 * lookPDA
 * - dans le cas pda, on n'utilise pas les onMouseOver sur le calendrier.
 *
 * Revision 1.9  2008/04/16 14:09:06  dlesimple
 * Calendrier: Bug nom du jour en gris si tous les memes jours du mois courant sont non ouvrés. (ex: L en gris si tous les Lundi non ouvrés, L en noir si au moins un Lundi est ouvré dans le mois)
 *
 * Revision 1.8  2008/04/16 07:23:19  neysseri
 * no message
 *
 * Revision 1.7.4.5  2008/04/15 09:19:33  neysseri
 * no message
 *
 * Revision 1.7.4.4  2008/04/08 15:14:44  dlesimple
 * Correction look jours ouvrés
 *
 * Revision 1.7.4.3  2008/03/31 11:43:38  dlesimple
 * Synchro feed rss + traces et messages
 *
 * Revision 1.7.4.2  2008/03/26 16:39:27  dlesimple
 * Gestion visibilité jours non ouvrés
 *
 * Revision 1.7.4.1  2008/03/25 15:21:36  dlesimple
 * Gestion des jours non ouvrés
 *
 * Revision 1.7  2007/04/20 14:10:15  neysseri
 * no message
 *
 * Revision 1.6  2006/06/30 15:12:58  dlesimple
 * Evènements sur plusieurs jours et/ou mois apparait maintenant bien surlignés
 * en vue par Mois ou Année
 *
 * Revision 1.5  2006/02/23 18:28:07  dlesimple
 * Agenda partagé
 *
 * Revision 1.4  2005/09/30 14:15:59  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.3  2004/12/22 15:18:31  neysseri
 * Possibilité d'indiquer les jours non sélectionnables
 * + nettoyage sources
 * + précompilation jsp
 *
 * Revision 1.2  2002/12/26 09:36:25  scotte
 * Correction : Ajouter l'année au calendrier général
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.4  2002/01/21 13:57:47  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 *
 * Revision 1.3  2002/01/18 15:43:18  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 *
 */

/**
 * Class declaration
 *
 *
 * @author
 */
public class CalendarHtmlView
{

    private Vector  scheduleCounts = new Vector();
    private boolean navigationBar = true;
    private boolean shortName = true;
    private boolean monthVisible = true;
    private String  weekDayStyle = "class=\"txtnav\""; 
    private String  dayOffStyle = "class=\"txtdayoff1\""; 
    private String  dayOffStyleDayView = "class=\"txtdayoff3\""; 
    private String  weekDayOffStyle = "class=\"txtdayoff2\""; 
    private String  monthDayStyle = "class=\"txtnav3\"";
    private String  monthDayStyleEvent = "class=\"intfdcolor6\"";
    private String	context = "";

	public CalendarHtmlView() 
	{
	}
		
    public CalendarHtmlView(String context) 
    {
    	this.context = context+URLManager.getURL(URLManager.CMP_AGENDA);
    }

    /**
     * Method declaration
     *
     *
     * @param scheduleCount
     *
     * @see
     */
    public void add(SchedulableCount scheduleCount)
    {
        scheduleCounts.add(scheduleCount);
    }

    /**
     * Method declaration
     *
     *
     * @param day
     *
     * @return
     *
     * @see
     */
    public SchedulableCount getSchedulableCount(int day)
    {
        String d = String.valueOf(day);

        if (d.length() == 1)
        {
            d = "0" + d;

        }
        for (int i = 0; i < scheduleCounts.size(); i++)
        {
            SchedulableCount count = (SchedulableCount) scheduleCounts.elementAt(i);

            if (count.getDay().endsWith(d))
            {
                return count;
            }
        }
        return null;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setWeekDayStyle(String value)
    {
        weekDayStyle = value;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setMonthDayStyle(String value)
    {
        monthDayStyle = value;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setMonthSelectedDayStyle(String value)
    {
        //monthSelectedDayStyle = value;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setMonthVisible(boolean value)
    {
        monthVisible = value;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setNavigationBar(boolean value)
    {
        navigationBar = value;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     *
     * @see
     */
    public void setShortName(boolean value)
    {
        shortName = value;
    }

    /**
     * Method declaration
     *
     *
     * @param date
     * @param agendaSessionController
     *
     * @return
     *
     * @see
     */
    
    public String getHtmlView(Date date, AgendaSessionController agendaSessionController)
    {
    	return getHtmlView(date, agendaSessionController, false);
    }
    
    /**
     * Method declaration
     *
     *
     * @param date
     * @param agendaSessionController
     *
     * @return
     *
     * @see
     */
    public String getPDAView(Date date, AgendaSessionController agendaSessionController)
    {
    	return getHtmlView(date, agendaSessionController, true);
    }
    
    /**
     * Fonction ajoutée pour génerer le calendar soit pour un PDA (sans onmouseover) soit pour un web classique. 
     *
     *
     * @param date
     * @param agendaSessionController
     * @param forPDA
     *
     * @return
     *
     * @see
     */
    public String getHtmlView(Date date, AgendaSessionController agendaSessionController, boolean forPda)
    {
    	boolean  viewByDay = (AgendaHtmlView.BYDAY == agendaSessionController.getCurrentDisplayType());

        StringBuffer 		result = new StringBuffer(255);
        List nonSelectableDays = agendaSessionController.getNonSelectableDays();
        List hiddenDays = null;
        try {
            hiddenDays			= agendaSessionController.getHolidaysDates();
        }
        catch (RemoteException e)
        {
            throw new AgendaRuntimeException("CalendarView.getHtmlView()", SilverpeasException.ERROR, "agenda.MSG_GET_DAYS_OFF_FAILED",e);
        }
        
        int firstDayOfWeek = Integer.parseInt(agendaSessionController.getString("weekFirstDay"));

        if (!shortName)
        {
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"1\" CELLPADDING=\"2\">");
        }
        else
        {
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"1\">");
        }

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        
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
        calendar.setTime(date);
        int startDay = 1;

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek)
        {
            calendar.add(Calendar.DATE, -1);
            startDay++;
        }

        //Display Months name
        if (monthVisible)
        {
            result.append("<TR class=\"txtnav2\"><TD COLSPAN=7>\n");
            result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"0\"><TR>");
            if (navigationBar)
            {
            	result.append("<td class=\"intfdcolor3\" align=\"right\"><a href=\"javascript:onClick=gotoPreviousMonth()\"");
                
                if (forPda)
                	result.append(" onMouseOut=\"MM_swapImgRestore()\" onMouseOver=\"MM_swapImage('fle-2','','").append(getContext()).append("icons/cal_fle-gon.gif',1)\"");
                
                result.append("><img name=\"fle-2\" border=\"0\" src=\"").append(getContext()).append("icons/cal_fle-goff.gif\" width=\"8\" height=\"14\"></a></td> \n");

            }
            result.append("<TD class=\"intfdcolor3\" ALIGN=\"center\"><span class=txtNav4>").append(agendaSessionController.getString("mois" + month)).append(" ").append(year).append("</span></TD>");
            if (navigationBar)
            {
                result.append("<td class=\"intfdcolor3\" align=\"left\"><a href=\"javascript:onClick=gotoNextMonth()\"");
                if (forPda)
                	result.append(" onMouseOut=\"MM_swapImgRestore()\" onMouseOver=\"MM_swapImage('fle-1','','").append(getContext()).append("icons/cal_fle-don.gif',1)\"");
                result.append("><img name=\"fle-1\" border=\"0\" src=\"").append(getContext()).append("icons/cal_fle-doff.gif\" width=\"8\" height=\"14\"></a></td>\n");
            }
            result.append("</TR></TABLE>\n");
            result.append("</TD></tr>");
        }
        result.append("<TR class=\"intfdcolor2\">\n");

        //Display Months days name
        do
        {
        	if (agendaSessionController.isSameDaysAreHolidays(calendar, month))
        		result.append("<TH ").append(weekDayOffStyle).append(">");
        	else
        		result.append("<TH ").append(weekDayStyle).append(">");
        		
            if (shortName)
            {
            	result.append(agendaSessionController.getString("shortJour" + calendar.get(Calendar.DAY_OF_WEEK)));
            }
            else
            {
                result.append(agendaSessionController.getString("jour" + calendar.get(Calendar.DAY_OF_WEEK)));
            }
            result.append("</TH>");
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
        calendar.setTime(date);
    	String dayStyle = monthDayStyle;
        HashSet dayWithEvents = new HashSet();
    	try
    	{
    		Collection events = agendaSessionController.getMonthSchedulables(date);
			for (Iterator i = events.iterator(); i.hasNext();)
			{
				JournalHeader event = (JournalHeader) i.next();

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
            dayStyle = monthDayStyleEvent;
        }
    	catch (RemoteException e)
    	{
            throw new AgendaRuntimeException("CalendarView.getHtmlView()", SilverpeasException.ERROR, "agenda.MSG_GET_USER_EVENT_BYDAY_FAILED",e);
        }

        boolean 			isSelectableDate 	= true;
        
        Date 				currentDate 		= null;
		String  			d					= null;
		SchedulableCount 	count 				= null; 
		for (int i = 1; i <= numDays; i++)
        {
	        boolean				isVisibleDate		= true;
            calendar.set(Calendar.DAY_OF_MONTH, i);
            
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
            
            // Write the day
            currentDate = calendar.getTime();
            d 			= DateUtil.getInputDate(currentDate, agendaSessionController.getLanguage());
            count 		= getSchedulableCount(i);

            if (hiddenDays != null)
            {
            	if (hiddenDays.contains(DateUtil.date2SQLDate(currentDate)))
            		isVisibleDate = false;
            }
            
            if (nonSelectableDays.contains(currentDate))
            	isSelectableDate = false;
            else
				isSelectableDate = true;
            
           	
            //If day has events
            dayStyle = monthDayStyle;
            if (dayWithEvents.contains(new Integer(i)))
            	dayStyle = monthDayStyleEvent;
            	
            if (count != null)
            {
                if (count.getCount() > 0)
                {
                	if (isVisibleDate)
                	{
	                	if (isSelectableDate)
	                    	result.append("<TD width=\"14%\" ").append(dayStyle).append(" align=\"center\"><A ").append(dayStyle).append(" HREF=\"javascript:selectDay('").append(d).append("')\">").append(i).append("</A></TD>\n");
	                    else
							result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">").append(i).append("</TD>\n");
                	}
                	else
                	{
                		//Day off
                		if (viewByDay)
                		{
                    		result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
                    		dayOffStyle = dayOffStyleDayView;
                		}
                		else
                			result.append("<TD width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
                		result.append("<span ").append(dayOffStyle).append(">").append(i).append("</span></TD>\n");
                	}
                }
                else
                {
                	if (isVisibleDate)
                	{
						if (isSelectableDate)
	                    	result.append("<TD width=\"14%\" ").append(dayStyle).append(" align=\"center\"><A ").append(dayStyle).append(" HREF=\"javascript:selectDay('").append(d).append("')\">").append(i).append("</A></TD>\n");
	                    else
							result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">").append(i).append("</TD>\n");
                	}
                	else
                	{
                		//Day off
                		if (viewByDay)
                		{
                    		result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
                    		dayOffStyle = dayOffStyleDayView;
                		}
                		else
                			result.append("<TD width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
                		result.append("<span ").append(dayOffStyle).append(">").append(i).append("</span></TD>\n");
                	}
                }
            }
            else
            {
            	if (isVisibleDate)
            	{
					if (isSelectableDate)
	                	result.append("<TD width=\"14%\" ").append(dayStyle).append(" align=\"center\"><A ").append(dayStyle).append(" HREF=\"javascript:selectDay('").append(d).append("')\">").append(i).append("</A></TD>\n");
	                else
						result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">").append(i).append("</TD>\n");
            	}
            	else
            	{
            		//Day off
            		if (viewByDay)
            		{
                		result.append("<TD width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
                		dayOffStyle = dayOffStyleDayView;
            		}
            		else
            			result.append("<TD width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
            		result.append("<span ").append(dayOffStyle).append(">").append(i).append("</span></TD>\n");
            	}
            }

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

        //SilverTrace.debug("agenda", "CalendarHtmlView.getHtmlView(Date, AgendaSessionController)", "result="+result);

        return result.toString();
    }
	/**
	 * @return
	 */
	public String getContext() {
		return context;
	}

}