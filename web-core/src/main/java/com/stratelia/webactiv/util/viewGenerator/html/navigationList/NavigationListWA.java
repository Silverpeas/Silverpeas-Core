/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * NavigationListWA.java
 * 
 * Created on 28 mars 2001, 10:32
 */

package com.stratelia.webactiv.util.viewGenerator.html.navigationList;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author lloiseau
 * @version 1.0
 */
public class NavigationListWA extends AbstractNavigationList
{

    /**
     * Creates new NavigationListWA
     */
    public NavigationListWA()
    {
        super();
    }

    /**
     * @return  the HTML code of the navigation list
     */

    public String print()
    {
        String     result = null;
        String     iconsPath = getIconsPath() + "/navigationList/";
        String     title = getTitle();
        int        nbCol = getNbcol();
        Collection items = getItems();
        boolean    endRaw = false;
        int        nbTd = 0;

        result = "\n";
        result += "<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"2\" class=\"intfdcolor5\">\n";
        result += "<tr>\n";
        result += "<td class=\"intfdcolor\">\n";
        result += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\">\n";
        result += "<tr>\n";
        result += "<td colspan=" + (nbCol + 1) + " class=\"intfdcolor51\" height=\"20\">\n";
        result += "<div align=\"center\" class=\"txtnav\">";
        result += title;
        result += "</div>\n";
        result += "</td>\n";
        result += "</tr>\n";

        Iterator i = items.iterator();
        int      j = 1;

        while (i.hasNext())
        {
            Item       item = (Item) i.next();
            Collection links = item.getLinks();

            if (j == 1)
            {
                result += "<tr>\n";
                result += "<td width=\"5%\" class=\"intfdcolor4\">&nbsp;</td>\n";
                endRaw = false;
            }
            if (j <= nbCol)
            {
                result += "<td class=\"intfdcolor4\" valign=\"top\" align=\"left\">\n";
                result += "<img src=\"" + iconsPath + "puce.gif\" border=\"0\">&nbsp;\n";
                result += "<a href=\"" + item.getURL() + "\" class=\"a\">";
                result += item.getLabel() + "</a>";
                if (item.getNbelem() >= 0)
                {
                    result += "<i>(" + item.getNbelem() + ")</i>\n";
                }
                if (item.getInfo() != null)
                {
                    result += "\t\t<br>" + item.getInfo();
                }
                if (links != null)
                {
                    result += "\t\t<br>";
                    Iterator k = links.iterator();

                    while (k.hasNext())
                    {
                        Link link = (Link) k.next();

                        result += "\n\t\t<a href=\"" + link.getURL() + "\" class=\"txtnote\">" + link.getLabel() + "</a>&nbsp&nbsp";
                    }
                }
                result += "\n\t\t</td>";
                j++;
            }
            if (j > nbCol)
            {
                result += "\t</tr>";
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
                result += "<td class=\"intfdcolor4\" valign=\"top\">&nbsp;</td>\n";
                k++;
            }
            result += "</tr>\n";
        }
        result += "</table></td></tr></table>";
        return result;
    }

}
