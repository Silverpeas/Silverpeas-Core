/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * NavigationListWA2.java
 * 
 * Created on 28 mars 2001, 10:32
 */

package com.stratelia.webactiv.util.viewGenerator.html.navigationList;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author lloiseau
 * @version 1.0
 * modif. Marc Raverdy 24/07/2001
 */
public class NavigationListWA3 extends AbstractNavigationList
{

    /**
     * Creates new NavigationListWA3
     */
    public NavigationListWA3()
    {
        super();
    }

    /**
     * @return  the HTML code of the navigation list
     */
    public String print()
    {
        StringBuffer    result 		= new StringBuffer(500);
        String     		iconsPath 	= getIconsPath() + "/navigationList/";
        String     		title 		= getTitle();
        int        		nbCol 		= getNbcol();
        Collection 		items 		= getItems();
        boolean    		endRaw 		= false;
        int        		nbTd 		= 0;

        result.append("<CENTER>");
        result.append("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=intfdcolor>\n");
        result.append("<tr>\n");
        result.append("<td class=\"txtnav\" nowrap align=center>\n");
        result.append(title);
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");

        result.append("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=intfdcolor>\n");
        result.append("<tr><td>\n");
        result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" class=intfdcolor4>\n");

        Iterator i = items.iterator();
        int      j = 1;

        while (i.hasNext())
        {
            Item       item = (Item) i.next();
            Collection links = item.getLinks();

            if (j == 1)
            {
                result.append("<tr>\n");
                result.append("<td width=\"2%\">&nbsp;</td>\n");
                endRaw = false;
            }
            if (j <= nbCol)
            {
                result.append("<td valign=\"top\" width=\"").append((98 / nbCol)).append("%\">\n");
                result.append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
                result.append("\t\t\t\t<tr>\n");
                result.append("\t\t\t\t\t<td width=\"5\" valign=top><img src=\"").append(iconsPath).append("puce.gif\" border=\"0\">&nbsp;</td>\n");
                result.append("\t\t\t\t\t<td valign=top>\n");
                result.append("\t\t\t\t\t<a href=\"").append(item.getURL()).append("\"><B>").append(item.getLabel()).append("</B></a>");
                if (item.getNbelem() >= 0)
                {
                    result.append("<i>(").append(item.getNbelem()).append(")</i>\n");
                }
                if (item.getUniversalLink() != null)
                {
                	result.append("&nbsp;").append(item.getUniversalLink());
                }
                result.append("\t\t\t\t\t</td>\n");
                result.append("\t\t\t\t</tr>\n");
                if (item.getInfo() != null)
                {
                    result.append("\t\t\t\t<tr>\n");
                    result.append("\t\t\t\t\t<td>&nbsp;</td>\n");
                    result.append("\t\t\t\t\t<td>").append(item.getInfo()).append("</td>\n");
                    result.append("\t\t\t\t</tr>\n");
                }
                if (links != null)
                {
                    result.append("\t\t\t\t<tr>\n");
                    result.append("\t\t\t\t\t<td>&nbsp;</td>\n");
                    result.append("\t\t\t\t\t<td>");
                    Iterator k = links.iterator();

                    while (k.hasNext())
                    {
                        Link link = (Link) k.next();

                        result.append("\n\t\t<a href=\"").append(link.getURL()).append("\" class=\"txtnote\">").append(link.getLabel()).append("</a>&nbsp&nbsp");
                    }
                    result.append("</td>\n");
                    result.append("\t\t\t\t</tr>\n");
                }
                result.append("\t\t\t</table>\n");
                result.append("\n\t\t</td>");
                j++;
            }
            if (j > nbCol)
            {
                result.append("\t</tr>");
                endRaw = true;
                j = 1;
            }
        }
        if (!endRaw)
        {
            nbTd = nbCol - j + 1;
            int k = 1;

            while (k <= nbTd)
            {
                result.append("<td valign=\"top\">&nbsp;</td>\n");
                k++;
            }
            result.append("</tr>\n");
        }
        result.append("</table></td></tr></table>");
        result.append("</CENTER>");
        return result.toString();
    }

}