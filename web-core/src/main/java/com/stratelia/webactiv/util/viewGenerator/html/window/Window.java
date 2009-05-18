/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Window.java
 * 
 * Created on 07 decembre 2000, 11:26
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane;
import com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar;

/**
 * The Window interface gives us the skeleton for all
 * funtionnalities we need to display typical WA window
 * @author neysseri
 * @version 1.0
 */
public interface Window extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @param gef
     *
     * @see
     */
    public void init(GraphicElementFactory gef);

    /**
     * Method declaration
     *
     *
     * @param body
     *
     * @see
     */
    public void addBody(String body);

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public BrowseBar getBrowseBar();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public OperationPane getOperationPane();

    /**
     * Method declaration
     *
     *
     * @param width
     *
     * @see
     */
    public void setWidth(String width);

    /**
     * Print the window in an html format.
     * The string result must be displayed between html tag <BODY> et </BODY>
     * @return The html based line code
     */
    public String print();

    /**
     * Print the beginning of the window in an html format.
     * @return The html based line code
     */
    public String printBefore();

    /**
     * Print the end of the window in an html format.
     * @return The html based line code
     */
    public String printAfter();

}
