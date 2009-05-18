/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * IconPaneWA.java
 * 
 * Created on 12 decembre 2000, 11:47
 */

package com.stratelia.webactiv.util.viewGenerator.html.iconPanes;

import java.util.Vector;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.icons.Icon;
import com.stratelia.webactiv.util.viewGenerator.html.icons.IconWA;

/**
 * The default implementation of IconPane interface
 * @author neysseric
 * @version 1.0
 */
public abstract class AbstractIconPane implements IconPane
{

    private Vector          icons = null;
    private String          verticalWidth = "50px";
    private String          spacing = "20px";
    public final static int VERTICAL_PANE = 1;
    public final static int HORIZONTAL_PANE = 2;

    private int             viewType = HORIZONTAL_PANE;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public AbstractIconPane()
    {
        icons = new Vector();
    }

	private String getIconsPath()
    {
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
    public Icon addIcon()
    {
        Icon icon = new IconWA();

        icons.add(icon);
        return icon;
    }

	public Icon addEmptyIcon()
    {
        Icon icon = new IconWA(getIconsPath()+"/15px.gif", "");

        icons.add(icon);
        return icon;
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
     * @param space
     *
     * @see
     */
    public void setSpacing(String space)
    {
        this.spacing = space;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Vector getIcons()
    {
        return this.icons;
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
    public String getSpacing()
    {
        return this.spacing;
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
