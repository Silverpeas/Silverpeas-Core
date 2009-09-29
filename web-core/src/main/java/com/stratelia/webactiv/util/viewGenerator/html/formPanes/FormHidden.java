/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FormHidden.java
 * 
 */

package com.stratelia.webactiv.util.viewGenerator.html.formPanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author frageade
 * @version
 */

public class FormHidden extends FormLine {

  /**
   * Constructor declaration
   * 
   * 
   * @param nam
   * @param val
   * 
   * @see
   */
  public FormHidden(String nam, String val) {
    super(nam, val);
    setType("hidden");
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    String retour = "\n<input type=\"hidden\" name=\"" + name + "\" value=\""
        + value + "\">";

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
  public FormPane getDescriptor(String nam, String url, PageContext pc) {
    FormPaneWA fpw = new FormPaneWA(nam, url, pc);

    fpw.add(new FormLabel("configuratorTitle", "Configuration du FormLabel"));
    fpw.add(new FormTextField("configuratorLabelValue", "",
        "Entrez la valeur : "));
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
  public void getConfigurationByRequest(HttpServletRequest req) {
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printDemo() {
    String retour = "";

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
  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n</field>";
    return retour;
  }

}
