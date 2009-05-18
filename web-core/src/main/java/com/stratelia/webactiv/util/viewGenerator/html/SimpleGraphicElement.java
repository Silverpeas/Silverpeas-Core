/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * SimpleGraphicElement.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html;

/**
 * All graphic elements from the viewGenerator package have to implement this
 * interface. It will enable an object (button, tab ...) to be printed in an
 * html format.
 * @author neysseri
 * @version 1.0
 */
public interface SimpleGraphicElement
{

    /**
     * Print an html representation for this object.
     * @return The html representation for this object.
     */
    public String print();

}
