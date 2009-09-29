/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FormSelect.java
 * 
 */

package com.stratelia.webactiv.util.viewGenerator.html.formPanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

import java.util.Vector;

/**
 * 
 * @author frageade
 * @version
 */

public class FormSelect extends FormLine {

  private int size;
  private int nbItems;
  private Vector itemsLabels;
  private Vector itemValues;
  private Vector itemsSelected;

  /**
   * Constructor declaration
   * 
   * 
   * @param nam
   * @param val
   * 
   * @see
   */
  public FormSelect(String nam, String val) {
    super(nam, val);
    setLabel(nam);
    size = 1;
    itemsLabels = new Vector();
    itemValues = new Vector();
    itemsSelected = new Vector();
    nbItems = 0;
    setType("select");
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param nam
   * @param val
   * @param lab
   * @param siz
   * 
   * @see
   */
  public FormSelect(String nam, String val, String lab, int siz) {
    super(nam, val);
    setLabel(lab);
    size = siz;
    itemsLabels = new Vector();
    itemValues = new Vector();
    itemsSelected = new Vector();
    nbItems = 0;
    setType("select");
  }

  /**
   * Method declaration
   * 
   * 
   * @param itemsLabel
   * @param itemValue
   * @param selected
   * 
   * @see
   */
  public void addItem(String itemsLabel, String itemValue, boolean selected) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(new Boolean(selected));
    nbItems++;
  }

  /**
   * Method declaration
   * 
   * 
   * @param itemsLabel
   * @param itemValue
   * 
   * @see
   */
  public void addItem(String itemsLabel, String itemValue) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(new Boolean(false));
    nbItems++;
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
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "\n<td><select name=\"" + name + "\" size=\""
        + String.valueOf(size) + "\">";
    for (int i = 0; i < nbItems; i++) {
      retour = retour + "\n<option value=\"" + (String) itemValues.elementAt(i)
          + "\"";
      if (((Boolean) itemsSelected.elementAt(i)).booleanValue()) {
        retour = retour + " selected ";
      }
      retour = retour + ">" + (String) itemsLabels.elementAt(i) + "</option>";
    }
    retour = retour + "\n</select></td>";
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
  public String printDemo() {
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
  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n</field>";
    return retour;
  }

}
