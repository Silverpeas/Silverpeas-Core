/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FormButtonsLine.java
 * 
 */

package com.stratelia.webactiv.util.viewGenerator.html.formPanes;

import java.util.Vector;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author frageade
 * @version
 */

public class FormButtonsLine extends FormLine {

  private Vector buttons;

  /**
   * Constructor declaration
   * 
   * 
   * @param nam
   * @param val
   * 
   * @see
   */
  public FormButtonsLine(String nam, String val) {
    super(nam, val);
    buttons = new Vector();
    setType("buttonLine");
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
  public FormButtonsLine(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    buttons = new Vector();
    setType("buttonLine");
  }

  /**
   * Method declaration
   * 
   * 
   * @param fb
   * 
   * @see
   */
  public void addButton(FormButton fb) {
    buttons.add(fb);
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
    String retour = "";

    if (!label.equals("")) {
      retour = retour + "\n<td>" + noNull(label) + "</td>";
    } else {
      retour = retour + "\n<td>&nbsp;</td>";
    }
    if (buttons.size() > 0) {
      retour = retour + "\n<td><table><tr>";
      for (int i = 0; i < buttons.size(); i++) {
        retour = retour + ((FormButton) buttons.elementAt(i)).print();
      }
      retour = retour + "\n</tr></table></td>";
    } else {
      retour = retour + "\n<td>&nbsp;</td>";
    }
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
    String retour = "";

    if (!label.equals("")) {
      retour = retour + "\n<td>" + label + "</td>";
    } else {
      retour = retour + "\n<td>&nbsp;</td>";
    }
    if (buttons.size() > 0) {
      retour = retour + "\n<td><table><tr>";
      for (int i = 0; i < buttons.size(); i++) {
        retour = retour + ((FormButton) buttons.elementAt(i)).printDemo();
      }
      retour = retour + "\n</tr></table></td>";
    } else {
      retour = retour + "\n<td>&nbsp;</td>";
    }
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
    String retour = "\n<field id=\"" + id + "\" type=\"buttonLine\">";

    retour = retour + "\n<name>" + name + "</name>";
    retour = retour + "\n<label>" + label + "</label>";
    retour = retour + "\n<value>" + value + "</value>";
    if (buttons.size() > 0) {
      retour = retour + "\n<actions>";
      for (int i = 0; i < buttons.size(); i++) {
        retour = retour + ((FormButton) buttons.elementAt(i)).toLineXML();
      }
      retour = retour + "\n</actions>";
    }
    retour = retour + "\n</field>";
    return retour;
  }

}
