/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FormLabel.java
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

public class FormLabel extends FormLine {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public FormLabel() {
    super();
    setName("newFormLabel");
    setType("label");
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param nam
   * @param val
   * 
   * @see
   */
  public FormLabel(String nam, String val) {
    super(nam, val);
    setType("label");
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
  public FormLabel(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    setType("label");
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
    String retour = "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + noNull(label);

    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;"
        + noNull(value) + "</td>";
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

    fpw.add(new FormLabel("configuratorTitle",
        message.getString("LabelConfig"), ""));
    fpw.add(new FormTextField("configuratorLabelTitle", "", message
        .getString("EnterTitle")
        + " : "));
    fpw.add(new FormTextField("configuratorLabelValue", "", message
        .getString("EnterValue")
        + " : "));
    fpw.add(new FormButtonSubmit("newConfiguratorSubmitButton", message
        .getString("Create")));
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
    setLabel(req.getParameter("configuratorLabelTitle"));
    setValue(req.getParameter("configuratorLabelValue"));
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
    String retour = "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + label;

    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;"
        + value + "</td>";
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

    retour = retour + "\n<name>" + name + "</name>";
    retour = retour + "\n<label>" + label + "</label>";
    retour = retour + "\n<value>" + value + "</value>";
    retour = retour + "\n</field>";
    return retour;
  }

}
