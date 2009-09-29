/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FormTextField.java
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

public class FormTextField extends FormLine {

  // contraintes
  private int nbCharMax;

  // constantes
  protected static int TOUS_CARACTERES = 0;
  protected static int BASIQUES_NON_ACCENTUES = 1;
  protected static int BASIQUES_ACCENTUES = 2;
  protected static int ETENDUS_NON_ACCENTUES = 3;
  protected static int ETENDUS_ACCENTUES = 4;

  protected static int NO_CONVERSION = 0;
  protected static int LOWER_CONVERSION = 1;
  protected static int UPPER_CONVERSION = 2;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public FormTextField() {
    super();
    setName("newFormTextField");
    setLabel("newFormTextField");
    setType("text");
    nbCharMax = 255;
    setDBEntry(true);
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
  public FormTextField(String nam, String val) {
    super(nam, val);
    setLabel("newFormTextField");
    setType("text");
    nbCharMax = 255;
    setDBEntry(true);
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
  public FormTextField(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    setType("text");
    nbCharMax = 255;
    setDBEntry(true);
  }

  /**
   * Method declaration
   * 
   * 
   * @param nb
   * 
   * @see
   */
  public void setNbCharMax(int nb) {
    nbCharMax = nb;
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
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;";
    retour = retour + "<input type=\"text\" name=\"" + name + "\" value=\""
        + noNull(value) + "\"></td>";
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

    fpw.add(new FormLabel("configuratorTitle", message
        .getString("TextFieldConfig"), ""));
    fpw.add(new FormTextField("configuratorLabelValue", "", message
        .getString("EnterLabelValue")
        + " : "));
    fpw.add(new FormTextField("configuratorDefaultValue", "", message
        .getString("EnterDefaultValue")
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
    setLabel(req.getParameter("configuratorLabelValue"));
    setValue(req.getParameter("configuratorDefaultValue"));
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
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;";
    retour = retour + "<input type=\"text\" name=\"" + name + "\" value=\""
        + value + "\"></td>";
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
    String retour = "\n<field id=\"" + id + "\" type=\"text\">";

    retour = retour + "\n<name>" + name + "</name>";
    retour = retour + "\n<label>" + label + "</label>";
    retour = retour + "\n<value>" + value + "</value>";
    retour = retour + "\n<size>" + String.valueOf(nbCharMax) + "</size>";
    retour = retour + "\n<dbtype>" + DBType + "</dbtype>";
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
  public String getDBColumnCreationRequest() {
    String result = id + " " + DBType;

    if (DBType.equals("character varying")) {
      result = result + "(" + String.valueOf(nbCharMax) + ")";
    }
    if (mandatory) {
      result = result + " NOT NULL, ";
    } else {
      result = result + " , ";
    }
    return result;
  }

}
