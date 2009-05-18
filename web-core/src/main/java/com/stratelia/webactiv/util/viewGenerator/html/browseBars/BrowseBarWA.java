/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class BrowseBarWA extends AbstractBrowseBar
{

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public BrowseBarWA()
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
    private String displayLine()
    {
        StringBuffer line = new StringBuffer();
        String iconsPath = getIconsPath();
        int    nb = 0;

        if ((getPath() == null) && (getExtraInformation() == null))
            nb = 1;
        else
            nb = 3;
        line.append("<tr>");
        line.append("<td colspan=\"").append(nb).append("\" bgcolor=\"#000000\" width=\"100%\"><img src=\"").append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>");
        line.append("</tr>");
        return line.toString();
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
        StringBuffer result = new StringBuffer();
        String iconsPath = getIconsPath();
        String domainName = getDomainName();
        String componentName = getComponentName();
        String componentLink = getComponentLink();
        String information = getExtraInformation();
        String path = getPath();

        result.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n");
        result.append("<tr>\n");
        result.append("<td nowrap width=\"0%\" align=\"left\">");
        if (domainName != null)
        {
            result.append("<span class=\"domainName\">").append(domainName).append("</span> - ");
        }
        if (componentName != null)
        {
            if (componentLink != null)
            {
                result.append("<a href=\"").append(componentLink).append("\" class=\"hrefComponentName\">").append(componentName).append("</a>&nbsp;");
            }
            else
            {
                result.append("<span class=\"componentName\">").append(componentName).append("</span>&nbsp;");
            }
        }
        result.append("</td>\n");
        if ((path != null) || (information != null))
        {
            if ((domainName != null) || (componentName != null))
            {
                result.append("<td valign=\"bottom\" width=\"11\"><img src=\"").append(iconsPath).append("/1px.gif\" width=\"5\"><img src=\"").append(iconsPath).append("/pxn.gif\" width=\"1\" height=\"20\"><img src=\"").append(iconsPath).append("/1px.gif\" width=\"5\"></td>\n");
            }
            if (path != null)
            {
                if (information == null)
                {
                    result.append("<td nowrap width=\"100%\"><span class=\"txtnav\">").append(path).append("</span><img src=\"").append(iconsPath).append("/1px.gif\" width=\"5\"></td>\n");
                }
                else
                {
                    result.append("<td nowrap width=\"100%\"><span class=\"txtnav\">").append(path).append("</span><img src=\"").append(iconsPath).append("/1px.gif\" width=\"5\"><span class=\"txtnav\"> &gt; ").append(information).append("</span></td>\n");
                }
            }
            else
            {
                if (information != null)
                {
                    result.append("<td nowrap width=\"100%\"><span class=\"txtnav\">").append(information).append("</span></td>\n");
                }
            }
        }
        result.append("</tr>\n");
        result.append(displayLine());
        result.append("</table>\n");
        return result.toString();
    }

}