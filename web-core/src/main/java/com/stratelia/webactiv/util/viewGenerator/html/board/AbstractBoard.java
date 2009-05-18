/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Board.java
 * 
 * Created on 27 march 2001, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.board;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * 
 * @author  lloiseau
 * @version 1.0
 */
public abstract class AbstractBoard implements Board
{

    //private String  iconsPath = null;
    private String  body = null;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public AbstractBoard() {}

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getIconsPath()
    {
        /*if (iconsPath == null)
        {
            ResourceLocator generalSettings = new ResourceLocator("com.stratelia.webactiv.general", "fr");

            iconsPath = generalSettings.getString("ApplicationURL") + GraphicElementFactory.getSettings().getString("IconsPath");
        }
        return iconsPath;*/
		return GraphicElementFactory.getIconsPath();
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String print();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String printBefore();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String printAfter();

    /**
     * add a string on the top of the frame.
     */
    public void addBody(String body)
    {
        this.body = body;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getBody()
    {
        return this.body;
    }
}
