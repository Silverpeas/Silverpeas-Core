/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * IconPaneWA.java
 * 
 * Created on 12 decembre 2000, 11:47
 */

package com.stratelia.webactiv.util.viewGenerator.html.iconPanes;

import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.icons.Icon;

/**
 * The default implementation of IconPane interface
 * @author neysseric
 * @version 1.0
 */
public class IconPaneWA extends AbstractIconPane
{

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public IconPaneWA()
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
    public String horizontalPrint()
    {
        String result = "";
        Vector icons = getIcons();
        String spacing = getSpacing();

        result += "<TABLE border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><TR>";
        if (icons.size() > 0)
        {
            result += "<TD>";
            result += ((Icon) icons.elementAt(0)).print();
            result += "</TD>";
        }
        for (int i = 1; i < icons.size(); i++)
        {
            result += "<TD width=\"" + spacing + "\">&nbsp;</TD>";
            result += "<TD>";
            result += ((Icon) icons.elementAt(i)).print();
            result += "</TD>";
        }
        result += "</TR></TABLE>";

        return result;
    }


    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String verticalPrint()
    {
        String result = "";
        Vector icons = getIcons();
        String verticalWidth = getVerticalWidth();
        String spacing = getSpacing();

        result += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"" + verticalWidth + "\">";
        if (icons.size() > 0)
        {
            result += "<TR><TD>";
            result += ((Icon) icons.elementAt(0)).print();
            result += "</TD></TR>";
        }
        for (int i = 1; i < icons.size(); i++)
        {
            result += "<TR><TD height=\"" + spacing + "\">&nbsp;</TD></TR>";
            result += "<TR><TD>";
            result += ((Icon) icons.elementAt(i)).print();
            result += "</TD></TR>";
        }
        result += "</table>";

        return result;
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
        int    viewType = getViewType();

        if (viewType == VERTICAL_PANE)
        {
            return verticalPrint();
        }
        else
        {
            return horizontalPrint();
        }
    }

}
