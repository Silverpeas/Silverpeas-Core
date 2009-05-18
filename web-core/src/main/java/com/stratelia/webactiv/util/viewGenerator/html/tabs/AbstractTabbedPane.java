/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * TabbedPane.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * 
 * @author  squere
 * @version
 */
public abstract class AbstractTabbedPane implements TabbedPane
{

    public static final int RIGHT = 0;
    public static final int LEFT = 1;

    private Vector          tabLines = null;  // A collection tabs vector
    private int             nbLines = 1;
    // private Collection tabs = null;
    private int             indentation = RIGHT;
    //private String          iconsPath = null;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public AbstractTabbedPane() {}

    /**
     * Method declaration
     *
     *
     * @param nbLines
     *
     * @see
     */
    public void init(int nbLines)
    {
        Vector tabLines = new Vector(2, 1);

        for (int i = 1; i <= nbLines; i++)
        {
            tabLines.add(new ArrayList());
        }
        this.nbLines = nbLines;
        this.tabLines = tabLines;
    }

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
    public void addTab(String label, String action, boolean disabled)
    {
        Vector     tabLines = getTabLines();
        Collection tabs = (Collection) tabLines.get(0);

        tabs.add(new Tab(label, action, disabled));
    }

    /**
     * Method declaration
     *
     *
     * @param label
     * @param action
     * @param disabled
     * @param enabled
     *
     * @see
     */
    public void addTab(String label, String action, boolean disabled, boolean enabled)
    {
        Vector     tabLines = getTabLines();
        Collection tabs = (Collection) tabLines.get(0);

        tabs.add(new Tab(label, action, disabled, enabled));
    }

    /**
     * Method declaration
     *
     *
     * @param label
     * @param action
     * @param disabled
     * @param nbLines
     *
     * @see
     */
    public void addTab(String label, String action, boolean disabled, int nbLines)
    {
        Vector     tabLines = getTabLines();
        Collection tabs = null;

        if (nbLines <= 0)
        {
            tabs = (Collection) tabLines.get(0);
        }
        else
        {
            tabs = (Collection) tabLines.get(nbLines - 1);
        }
        tabs.add(new Tab(label, action, disabled));
    }

    /**
     * Method declaration
     *
     *
     * @param label
     * @param action
     * @param disabled
     * @param enabled
     * @param nbLines
     *
     * @see
     */
    public void addTab(String label, String action, boolean disabled, boolean enabled, int nbLines)
    {
        Vector     tabLines = getTabLines();
        Collection tabs = null;

        if (nbLines <= 0)
        {
            tabs = (Collection) tabLines.get(0);
        }
        else
        {
            tabs = (Collection) tabLines.get(nbLines - 1);
        }
        tabs.add(new Tab(label, action, disabled, enabled));
    }

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
    public Vector getTabLines()
    {
        return this.tabLines;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public int getNbLines()
    {
        return this.nbLines;
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

    /* onglet cale a gauche */

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void setIndentationLeft()
    {
        indentation = LEFT;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public int getIndentation()
    {
        return this.indentation;
    }

    /* cas ou les pages JSP ne soient pas toutes au meme niveau */
    /* DEPRECATED */

    /**
     * Method declaration
     *
     *
     * @param level
     *
     * @see
     */
    public void setLevelRootImage(int level)
    {
        // this.levelPath = level;
    }

}
