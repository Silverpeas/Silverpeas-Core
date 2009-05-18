/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Link.java
 * 
 * Created on 17 avril 2001, 17:44
 */

package com.stratelia.webactiv.util.viewGenerator.html.navigationList;

/**
 * 
 * @author  lloiseau
 * @version 1.0
 */
public class Link extends Object
{

    private String label;
    private String URL;


    /**
     * Creates new Link
     */
    public Link(String label, String URL)
    {
        this.URL = URL;
        this.label = label;
    }

    // Return the label

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getLabel()
    {
        return label;
    }

    // To set a new label

    /**
     * Method declaration
     *
     *
     * @param label
     *
     * @see
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Return the URL

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getURL()
    {
        return URL;
    }

    // To set a new URL

    /**
     * Method declaration
     *
     *
     * @param URL
     *
     * @see
     */
    public void setURL(String URL)
    {
        this.URL = URL;
    }

}
