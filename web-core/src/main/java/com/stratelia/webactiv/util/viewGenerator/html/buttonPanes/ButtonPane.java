/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * BrowseBar.java
 * 
 * Created on 07 decembre 2000, 11:26
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttonPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import com.stratelia.webactiv.util.viewGenerator.html.buttons.Button;

/**
 * The Browse interface gives us the skeleton for all
 * funtionnalities we need to display typical WA browse bar
 * @author neysseri
 * @version 1.0
 */
public interface ButtonPane extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @param button
     *
     * @see
     */
    public void addButton(Button button);

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void setVerticalPosition();

    /**
     * Method declaration
     *
     *
     * @param width
     *
     * @see
     */
    public void setVerticalWidth(String width);

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void setHorizontalPosition();

    /**
     * Print the browseBar in an html format.
     * @return The html based line code
     */
    public String print();

}
