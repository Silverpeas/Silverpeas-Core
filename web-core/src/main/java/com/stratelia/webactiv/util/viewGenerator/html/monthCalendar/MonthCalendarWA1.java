/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * MonthCalendarWA1.java
 * this class inplement monthCalendar viewGenerator
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */
package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;


/*
 * CVS Informations
 *
 * $Id: MonthCalendarWA1.java,v 1.14 2008/05/28 08:45:40 ehugonnet Exp $
 *
 * $Log: MonthCalendarWA1.java,v $
 * Revision 1.14  2008/05/28 08:45:40  ehugonnet
 * Imports inutiles
 *
 * Revision 1.13  2008/05/16 10:28:50  neysseri
 * no message
 *
 * Revision 1.12.2.1  2008/04/29 15:37:41  cbonin
 * evts périodiques
 *
 * Revision 1.11  2008/04/29 15:32:36  cbonin
 * evts périodiques
 *
 * Revision 1.10  2008/03/12 16:46:54  neysseri
 * no message
 *
 * Revision 1.9.6.1  2008/01/18 16:35:20  neysseri
 * no message
 *
 * Revision 1.9  2006/10/17 13:17:56  sfariello
 * Ajout paramètre d'instanciation pour affichage des week-end ou non
 *
 * Revision 1.8  2006/03/21 12:09:52  neysseri
 * no message
 *
 * Revision 1.7  2005/12/23 13:10:01  dlesimple
 * Agregation almanachs
 *
 * Revision 1.6.2.2  2005/12/23 12:24:00  dlesimple
 * Redirection vers le bon almanach
 *
 * Revision 1.6.2.1  2005/12/21 12:35:32  dlesimple
 * agregation d'almanachs
 *
 * Revision 1.6  2005/12/09 12:26:46  neysseri
 * no message
 *
 * Revision 1.5  2005/09/30 14:24:00  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.4  2005/05/17 19:15:23  neysseri
 * Correction de l'affichage des horaires d'un événement
 *
 * Revision 1.3  2005/04/14 18:35:44  neysseri
 * no message
 *
 * Revision 1.2  2004/06/24 17:16:38  neysseri
 * nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.9  2002/05/29 09:32:16  groccia
 * portage netscape
 *
 * Revision 1.8.10.1  2002/05/07 15:24:06  fsauvand
 * no message
 *
 * Revision 1.8  2002/04/19 13:49:09  fsauvand
 * no message
 *
 * Revision 1.6  2002/01/04 14:04:24  mmarengo
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
public class MonthCalendarWA1 extends AbstractMonthCalendar
{

    /**
     * Creates new SilverpeasCalendarWA1
     */
    public MonthCalendarWA1(String language)
    {
        super(language);
    }

    public MonthCalendarWA1(String language, int numbersDays)
    {
        super(language, numbersDays);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print()
    {
		ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.almanach.multilang.almanach", this.language);
        try
        {
            StringBuffer html = new StringBuffer();

            html.append("<TABLE cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"98%\" bgcolor=\"000000\"><TR><TD><TABLE cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");

            html.append(printDayOfWeek());

            int k = super.getNumbersWeekOfMonth();

            SilverTrace.info("viewgenerator", "MonthCalendarWA1.print()", "root.MSG_GEN_PARAM_VALUE", " Numbers week = " + k + ". ");
            for (int i = 1; i <= k; i++)
            {
                html.append(printNumberDayOfWeek(i));
                html.append(printWeek(i, message));
            }
            html.append("</TABLE></TD></TR></TABLE>");

            return html.toString();
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    private String printDayOfWeek()
    {
        String[] nameDay 			= super.getHeaderNameDay();
        int      numbersDayOfWeek 	= super.getNumbersDayOfWeek();

        StringBuffer   html = new StringBuffer("<tr class=\"intfdcolor51\">");

        for (int i = 0; i < numbersDayOfWeek; i++)
        {
            html.append(" <td width=\"14%\" align=\"center\"><span class=\"txtnav\">");
            html.append(nameDay[i]);
            html.append("</span></td>");
        }
        html.append("</tr>");
        return html.toString();
    }


    /**
     * Method declaration
     *
     *
     * @param week
     *
     * @return
     *
     * @throws Exception
     *
     * @see
     */
    private String printNumberDayOfWeek(int week) throws Exception
    {
        StringBuffer html = new StringBuffer();
		html.append("<tr>");
        int    numbersDayOfWeek = super.getNumbersDayOfWeek();
        Day[]  day = super.getDayOfWeek(week);

        SilverTrace.info("viewgenerator", "MonthCalendarWA1.printNumberDayOfWeek()", "root.MSG_GEN_PARAM_VALUE", " Week = " + week + ". ");
        for (int k = 0; k < numbersDayOfWeek; k++)
        {
            if (day[k].getIsInThisMonth())
            {
                if (day[k].isCurrentDay())
                {
                    html.append("<td class=\"intfdcolor52\">");
                }
                else
                {
                    html.append("<td class=\"intfdcolor\">");
                }

                html.append("&nbsp;").append("<a href=\"javascript: onClick=clickDay('").append(DateUtil.getInputDate(day[k].getDate(), super.language)).append("')\" class=\"almanachDay\" ").append("onFocus=\"this.blur()\">").append(day[k].getNumbers()).append("</a> </td>");
            }
            else
            {
                html.append("<td class=\"intfdcolor51\">&nbsp;").append(day[k].getNumbers()).append("</td>");
            }
        }
        html.append("</tr>");
        SilverTrace.info("viewgenerator", "MonthCalendarWA1.printNumberDayOfWeek()", "root.MSG_GEN_EXIT_METHOD");
        return html.toString();
    }




    /**
     * Method declaration
     *
     *
     * @param week
     *
     * @return
     *
     * @throws Exception
     *
     * @see
     */
    private String printWeek(int week, ResourceLocator message) throws Exception
    {
        StringBuffer html = new StringBuffer();

        int    numbersRowOfWeek = super.getNumbersOfRow(week);

        SilverTrace.info("viewgenerator", "MonthCalendarWA1.printWeek()", "root.MSG_GEN_PARAM_VALUE", " Week = " + (week - 1) + "); numbersRowOfWeek=" + numbersRowOfWeek + ". ");
        // pour chaque row de la semaine

        for (int i = 0; i < numbersRowOfWeek; i++)
        {
            html.append("<tr>");
            html.append(printRow(week, i, numbersRowOfWeek, message));
            html.append("</tr>");
        }
        SilverTrace.info("viewgenerator", "MonthCalendarWA1.printWeek()", "root.MSG_GEN_EXIT_METHOD");
        return html.toString();
    }


    /**
     * Method declaration
     *
     *
     * @param week
     * @param row
     * @param numbersRowOfWeek
     *
     * @return
     *
     * @see
     */
    private String printRow(int week, int row, int numbersRowOfWeek, ResourceLocator message)
    {
        SilverTrace.info("viewgenerator", "MonthCalendarWA1.printRow()", "root.MSG_GEN_PARAM_VALUE", " Week = " + week + "; numbersRowOfWeek=" + numbersRowOfWeek + ". ");
        StringBuffer html = new StringBuffer();

        int     numbersDayOfWeek = super.getNumbersDayOfWeek();
        Day[]   days = super.getDayOfWeek(week);

        // récupération des événements contenu dans la "row"
        Event[] evt = super.getEventOfRow(week, row);

        String  height = String.valueOf(70 / numbersRowOfWeek);

        if (evt == null)
        {
            for (int k = 0; k < numbersDayOfWeek; k++)
            {
                if (days[k].getIsInThisMonth())
                {
                    html.append("<td class=\"eventCells\"");
                }
                else
                {
                    html.append("<td class=\"intfdcolor51\"");
                }
                html.append(" height=\"70\">&nbsp;</td>");
            }
            return html.toString();
        }
        else
        {
        	SilverTrace.info("viewgenerator", "MonthCalendarWA1.printRow()", "root.MSG_GEN_PARAM_VALUE", " # of events = " + evt.length);

        	int     nbEvt = evt.length;
        	Day day;
            for (int k = 0; k < numbersDayOfWeek; k++)
            {
            	day = days[k];
                if (day.getIsInThisMonth())
                {
                    html.append("<td class=\"eventCells\"");
                }
                else
                {
                    html.append("<td class=\"intfdcolor51\"");
                }

                html.append(" height=\"").append(height).append("\"");

                boolean tdIsCreate = false;

                // contrôle pour chaque événement, s'il débute ou pas ce jour courant afin d'avoir le html approprié
                for (int z = 0; z < nbEvt; z++)
                {
                    if (evt[z].isInDay(days[k]))
                    {
                    	int colspan = evt[z].getSpanDay(days[k].getDate());

                        if (colspan > 1)
                        {
                            html.append(" colspan=\"" + String.valueOf(colspan) + "\">");
                            k += (colspan - 1);
                        }
                        else
                        {
                            html.append(">");
                        }

                        String title = EncodeHelper.javaStringToHtmlString(evt[z].getName());

                        if (title.length() > 30)
                        {
                            title = title.substring(0, 30) + "....";
                        }
                        if (evt[z].getColor() != null)
                            title = "<span style=\"color :" + evt[z].getColor() + "\">" + title + "</span>";

                        if (evt[z].getPriority() == 1)
                        {
							html.append("<img src=\"icons/urgent.gif\" align=\"absmiddle\" alt=\"").append(message.getString("important")).append("\" title=\"").append(message.getString("important")).append("\"> ");
                        }
                        html.append("<a href=\"javascript:onClick=clickEvent(").append(evt[z].getId()).append(", '");
                        html.append(DateUtil.date2SQLDate(day.getDate())).append("', '");
                        html.append(evt[z].getInstanceId()).append("')\" title=\"").append(EncodeHelper.javaStringToHtmlString(evt[z].getName())).append("\">").append(title).append("</a>");
						if (evt[z].getStartHour() != null && evt[z].getStartHour().length() > 0)
						{
							html.append("&nbsp;(").append(evt[z].getStartHour());
							if (evt[z].getEndHour() != null && evt[z].getEndHour().length()>0)
								html.append("-").append(evt[z].getEndHour());
							html.append(")");
						}
						html.append("</td>");
                        tdIsCreate = true;
                        break;
                    }
                }
                if (!tdIsCreate)
                {
                    html.append(">&nbsp;</td>");
                }
            }
            return html.toString();
        }
    }
}