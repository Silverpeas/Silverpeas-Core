/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Button.java
 * 
 * Created on 10 octobre 2000, 16:16
 */

package com.stratelia.webactiv.util.viewGenerator.html.icons;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * 
 * @author  neysseri
 * @version
 */
public interface Icon extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @param iconName
     * @param altText
     *
     * @see
     */
    public void setProperties(String iconName, String altText);

    /**
     * Method declaration
     *
     *
     * @param iconName
     * @param altText
     * @param action
     *
     * @see
     */
    public void setProperties(String iconName, String altText, String action);

    /**
     * Method declaration
     *
     *
     * @param iconName
     * @param altText
     * @param action
     * @param imagePath
     *
     * @see
     */
    public void setProperties(String iconName, String altText, String action, String imagePath);

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void setRootImagePath(String s);

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
