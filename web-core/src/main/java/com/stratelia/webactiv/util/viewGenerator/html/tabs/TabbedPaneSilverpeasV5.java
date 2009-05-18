/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * TabbedPaneSogreah.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/**
 * 
 * @author  squere
 * @version
 */
public class TabbedPaneSilverpeasV5 extends AbstractTabbedPane
{

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public TabbedPaneSilverpeasV5()
    {
        super();
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
        StringBuffer	result		= new StringBuffer();
        String						iconsPath = getIconsPath();
        Vector						tabLines = getTabLines();
        Collection				tabs = null;

        int        nbLines = tabLines.size();
        int        incr = nbLines - 1;

        for (int j = 0; j < nbLines; j++)
        {
            tabs = (Collection) tabLines.get(j);
            result.append("<table id=\"tabbedPane\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td align=\"right\" width=\"100%\">");
            result.append( printTabLine(tabs));
            result.append( "</td><td><img src=\"").append(iconsPath).append("/tabs/1px.gif\" width=\"").append(incr * 17).append("\" height=\"1\"></td></tr></table>");
            incr--;
        }
		result.append("<table id=\"sousTabbedPane\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td width=\"100%\" class=\"sousOnglets\">");
        result.append( "<img src=\"").append(iconsPath).append("/tabs/1px.gif\" width=\"1\" height=\"1\"></td></tr></table>");
        return result.toString();
    }


    /**
     * Method declaration
     *
     *
     * @param tabs
     *
     * @return
     *
     * @see
     */
    private String printTabLine(Collection tabs)
    {

        StringBuffer	result		= new StringBuffer();
        String iconsPath = getIconsPath();
        int    indentation = getIndentation();

        result.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
        result.append( "<tr align=\"right\">");
        if (indentation == RIGHT)
        {
            result.append( "<td width=\"100%\">&nbsp;</td>\n");
        }
        Iterator i = tabs.iterator();

        while (i.hasNext())
        {
            Tab    tab = (Tab) i.next();
            String style = null;
			String styleGauche = null;
			String styleDroite = null;

			if (tab.getSelected())
            {
                style = "milieuOngletOn";
				styleGauche = "gaucheOngletOn";
				styleDroite = "droiteOngletOn";
            }
            else
            {
                style = "milieuOngletOff";
				styleGauche = "gaucheOngletOff";
				styleDroite = "droiteOngletOff";
            }

            if (tab.getEnabled())
            {
                result.append( "<td align=center nowrap class=").append(styleGauche).append("><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
				result.append( "<td align=center nowrap class=").append(style).append("><a href=\"").append(tab.getAction()).append("\">&nbsp;").append(tab.getLabel()).append("&nbsp;</td>\n");
				result.append( "<td align=center nowrap class=").append(styleDroite).append("><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");            
			}
			else
            {
                result.append( "<td align=center nowrap class=").append(styleGauche).append("><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
				result.append( "<td align=center nowrap class=").append(style).append( ">").append(tab.getLabel()).append("</td>\n");
				result.append( "<td align=center nowrap class=").append(styleDroite).append("><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");            
            }
        }

        if (indentation == LEFT)
        {
            result.append( "<td width=\"100%\">&nbsp;</td>\n");

        }
        result.append( "<td><img src=\"").append(iconsPath).append("/tabs/1px.gif\" width=13></td></tr>\n");
        result.append( "</table>\n");
        return result.toString();
    }

}
