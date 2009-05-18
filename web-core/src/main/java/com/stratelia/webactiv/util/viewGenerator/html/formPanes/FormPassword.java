/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * FormPassword.java
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

public class FormPassword extends FormLine
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
    public FormPassword(String nam, String val)
    {
        super(nam, val);
        setLabel(nam);
        setType("password");
    }

    /**
     * Constructor declaration
     *
     *
     * @param nam
     * @param val
     * @param lab
     *
     * @see
     */
    public FormPassword(String nam, String val, String lab)
    {
        super(nam, val);
        setLabel(lab);
        setType("password");
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
        String retour = "\n<td>" + label + "</td>";

        retour = retour + "<td><input type=\"password\" name=\"" + name + "\" value=\"" + value + "\"></td>";
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
    public void getConfigurationByRequest(HttpServletRequest req)
    {
        setLabel(req.getParameter("configuratorLabelValue"));
    }

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
        String retour = "\n<td>" + label + "</td>";

        retour = retour + "<td>" + value + "</td>";
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

}
