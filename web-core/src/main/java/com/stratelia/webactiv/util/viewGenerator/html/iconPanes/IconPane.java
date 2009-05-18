/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * IconPane.java
 * 
 * Created on 12 decembre 2000, 11:47
 */

package com.stratelia.webactiv.util.viewGenerator.html.iconPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import com.stratelia.webactiv.util.viewGenerator.html.icons.Icon;

/**
 * @author neysseri
 * @version 1.0
 */
public interface IconPane extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Icon addIcon();

	/**
     * Return an icon using the 1px image
     *
     * @return an Icon
     *
     * @see
     */
    public Icon addEmptyIcon();

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
     * Method declaration
     *
     *
     * @param space
     *
     * @see
     */
    public void setSpacing(String space);

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print();

}
