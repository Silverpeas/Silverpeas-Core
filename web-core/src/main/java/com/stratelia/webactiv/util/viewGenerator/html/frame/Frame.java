/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Frame.java
 * 
 * Created on 27 march 2001, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * Frame is an interface to be implemented by a graphic element
 * to print a frame in an html format.
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public interface Frame extends SimpleGraphicElement
{

    /**
     * add a title to the frame.
     */
    public void addTitle(String title);

    /**
     * add a string on the top of the frame.
     */
    public void addTop(String top);

    /**
     * add a string on the bottom of the frame.
     */
    public void addBottom(String bottom);

    /**
     * Print the Frame in an html format
     * @return The Frame representation
     */
    public String print();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printBefore();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printMiddle();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printAfter();
}
