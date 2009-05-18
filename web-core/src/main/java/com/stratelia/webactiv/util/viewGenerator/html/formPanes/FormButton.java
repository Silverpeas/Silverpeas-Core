/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * FormButton.java
 * 
 */

package com.stratelia.webactiv.util.viewGenerator.html.formPanes;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author  frageade
 * @version
 */
public abstract class FormButton extends FormLine
{

    /**
     * Constructor declaration
     *
     *
     * @param nam
     * @param val
     *
     * @see
     */
    public FormButton(String nam, String val)
    {
        super(nam, val);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String printDemo();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String toXML();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public abstract String toLineXML();

    /**
     * Method declaration
     *
     *
     * @param nam
     * @param url
     * @param pc
     *
     * @return
     *
     * @see
     */
    public abstract FormPane getDescriptor(String nam, String url, PageContext pc);

    /**
     * Method declaration
     *
     *
     * @param req
     *
     * @see
     */
    public abstract void getConfigurationByRequest(HttpServletRequest req);

}
