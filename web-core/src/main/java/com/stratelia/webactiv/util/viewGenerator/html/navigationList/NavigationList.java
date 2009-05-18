/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * NavigationList.java
 * 
 * Created on 28 mars 2001, 09:07
 */

package com.stratelia.webactiv.util.viewGenerator.html.navigationList;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import java.util.Collection;

/**
 * NavigationList is an interface to be implemented by a graphic element
 * Very usefull to create a list of items in an html format.
 * @author lloiseau
 * @version 1.0
 */
public interface NavigationList extends SimpleGraphicElement
{
	/**
	 * Add an item with label, number of elements, information in the navigation list and an universal link
	 * @param label - string that describe the item
	 * @param nbelem - give the number of element contained by the item
	 * For exemple, if the item is a directory, "nbelem" is the number of files you can find in this directory
	 * @param info - It can be everything ... (only string)
	 * @param universalLink - a link as string containing an universal link
	 */
	public void addItem(String label, String URL, int nbelem, String info, String universalLink);

    /**
     * Add an item with label, number of elements and information in the navigation list
     * @param label string that describe the item
     * @param nbelem give the number of element contained by the item
     * For exemple, if the item is a directory, "nbelem" is the number of files you can find in this directory
     * @param info It can be everything ... (only string)
     */
    public void addItem(String label, String URL, int nbelem, String info);

    /**
     * Add an item with label, number of elements and sub links in the navigation list
     * @param label string that describe the item
     * @param nbelem give the number of element contained by the item
     * For exemple, if the item is a directory, "nbelem" is the number of files you can find in this directory
     */
    public void addItemSubItem(String label, String URL, int nbelem, Collection links);

    /**
     * Add an item with label and information in the navigation list
     * @param label string that describe the item
     * @param info It can be everything ... (only string)
     */
    public void addItem(String label, String URL, String info);

    /**
     * Add an item with label and information in the navigation list
     * @param label string that describe the item
     */
    public void addItem(String label, String URL);

    /**
     * Set the title of the NavigationList
     * @param title String that wil appear on the top of the NavigationList
     */
    public void setTitle(String title);

    /**
     * You can set  the number of columns you want for your list
     * Default is 3
     * @param col int Specify the number of column
     */
    public void setNbcol(int col);

    /**
     * Print the NavigationList in an html format
     * @return The NavigationList representation
     */
    public String print();



}
