/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttonPanes;

import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.buttons.Button;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class ButtonPaneWA2 extends AbstractButtonPane
{

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public ButtonPaneWA2()
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
        StringBuffer result = new StringBuffer();
        Vector buttons = getButtons();

        result.append("<TABLE border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><TR>");
        result.append("<TD width=\"100\">&nbsp;</TD>");
        if (buttons.size() > 0)
        {
            result.append("<TD>");
            result.append(((Button) buttons.elementAt(0)).print());
            result.append("</TD>");
        }
        for (int i = 1; i < buttons.size(); i++)
        {
            result.append("<TD>&nbsp;</TD>");
            result.append("<TD>");
            result.append(((Button) buttons.elementAt(i)).print());
            result.append("</TD>");
        }
        result.append("<TD width=\"100\">&nbsp;</TD>");
        result.append("</TR></TABLE>");

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
    public String verticalPrint()
    {
        StringBuffer result = new StringBuffer();
        Vector buttons = getButtons();
        String verticalWidth = getVerticalWidth();

        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"").append(verticalWidth).append("\">");
        result.append("<tr>");
        result.append("<td width=\"").append(verticalWidth).append("\">");
        for (int i = 0; i < buttons.size(); i++)
        {
            result.append(((Button) buttons.elementAt(i)).print());
        }
        result.append("</td>");
        result.append("</tr>");
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
        if (getViewType() == VERTICAL_PANE)
        {
            return verticalPrint();
        }
        else
        {
            return horizontalPrint();
        }
    }

}