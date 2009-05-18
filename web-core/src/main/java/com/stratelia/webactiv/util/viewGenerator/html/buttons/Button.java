/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Button.java
 * 
 * Created on 10 octobre 2000, 16:16
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttons;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * 
 * @author  neysseri
 * @version
 */
public interface Button extends SimpleGraphicElement
{

    /**
     * Method declaration
     *
     *
     * @param label
     * @param action
     * @param disabled
     *
     * @see
     */
    public void init(String label, String action, boolean disabled);

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print();

    /*
     * @deprecated
     */

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void setRootImagePath(String s);

}
