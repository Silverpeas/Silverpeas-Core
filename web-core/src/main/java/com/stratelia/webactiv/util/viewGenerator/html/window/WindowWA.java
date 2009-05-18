/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * WindowWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

/**
 * The default implementation of Window interface
 * @author neysseri
 * @version 1.0
 */
public class WindowWA extends AbstractWindow
{

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public WindowWA()
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
    public String printBefore()
    {
        StringBuffer	result		= new StringBuffer();
        String			width		= getWidth();

        int    nbCols = 1;

        if (getOperationPane().nbOperations() > 0)
            nbCols = 2;

		result.append("<table width=\"").append(width).append("\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
        result.append("<tr><td colspan=\"").append(nbCols).append("\">");
        result.append(getBrowseBar().print());
        result.append("</td></tr>");
        result.append("<tr><td width=\"100%\" valign=\"top\">");
        result.append("<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"5\"><tr><td align=\"center\" valign=\"top\">");
        return result.toString();
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printAfter()
    {
        StringBuffer	result		= new StringBuffer();
        String			iconsPath	= getIconsPath();

        int    nbCols = 1;

        if (getOperationPane().nbOperations() > 0)
            nbCols = 2;

		result.append("</td></tr></table>");
        result.append("</td>");
        if (getOperationPane().nbOperations() > 0)
        {
            result.append("<td valign=\"top\" align=\"right\">");
            result.append(getOperationPane().print());
            result.append("</td>");
        }
        result.append("</tr>");
        result.append("<tr><td>&nbsp;</td></tr>");
        result.append("<tr><td colspan=\"").append(nbCols).append("\" bgcolor=\"000000\"><img src=\"").append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\" border=\"0\"></td></tr>");
        result.append("</table>");

        return result.toString();
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

        result.append(printBefore());
        result.append(getBody());
        result.append(printAfter());

        return result.toString();
    }

}