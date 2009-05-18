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
public abstract class AbstractButtonPane implements ButtonPane
{

    private Vector          buttons = null;
    private String          verticalWidth = "50px";
    public final static int VERTICAL_PANE = 1;
    public final static int HORIZONTAL_PANE = 2;

    private int             viewType = HORIZONTAL_PANE;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public AbstractButtonPane()
    {
        buttons = new Vector();
    }

    /**
     * Method declaration
     *
     *
     * @param button
     *
     * @see
     */
    public void addButton(Button button)
    {
        buttons.add(button);
    }

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void setVerticalPosition()
    {
        viewType = VERTICAL_PANE;
    }

    /**
     * Method declaration
     *
     *
     * @param width
     *
     * @see
     */
    public void setVerticalWidth(String width)
    {
        verticalWidth = width;
    }

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void setHorizontalPosition()
    {
        viewType = HORIZONTAL_PANE;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Vector getButtons()
    {
        return this.buttons;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public int getViewType()
    {
        return this.viewType;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getVerticalWidth()
    {
        return this.verticalWidth;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String horizontalPrint();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String verticalPrint();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String print();
}
