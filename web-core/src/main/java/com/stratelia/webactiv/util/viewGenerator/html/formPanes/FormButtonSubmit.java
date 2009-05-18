/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * FormButtonSubmit.java
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

public class FormButtonSubmit extends FormButton
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
    public FormButtonSubmit(String nam, String val)
    {
        super(nam, val);
        setType("button");
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print()
    {
        String retour = "\n<td><input type=\"submit\" name=\"" + name + "\" value=\"" + value + "\"></td>";

        return retour;
    }

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
    public FormPane getDescriptor(String nam, String url, PageContext pc)
    {
        FormPaneWA fpw = new FormPaneWA(nam, url, pc);

        fpw.add(new FormLabel("configuratorTitle", "Configuration du FormLabel"));
        fpw.add(new FormTextField("configuratorLabelValue", "", "Entrez la valeur : "));
        fpw.add(new FormButtonSubmit("newConfiguratorSubmitButton", "Créer"));
        return fpw;
    }

    /**
     * Method declaration
     *
     *
     * @param req
     *
     * @see
     */
    public void getConfigurationByRequest(HttpServletRequest req) {}

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printDemo()
    {
        String retour = "\n<td><input type=\"submit\" name=\"" + name + "\" value=\"" + value + "\"></td>";

        return retour;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toXML()
    {
        String retour = "\n<field id=\"" + id + "\" type=\"label\">";

        retour = retour + "\n</field>";
        return retour;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toLineXML()
    {
        String retour = "\n<action id=\"" + id + "\" value=\"" + value + "\"/>";

        return retour;
    }

}
