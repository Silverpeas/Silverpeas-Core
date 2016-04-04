/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormTextField.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
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
   * @param nam
   * @param val
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
   * @param nam
   * @param val
   * @param lab
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
   * @param nb
   * @see
   */
  public void setNbCharMax(int nb) {
    nbCharMax = nb;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String retour =
        "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + noNull(label);
    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;";
    retour = retour + "<input type=\"text\" name=\"" + name + "\" value=\""
        + noNull(value) + "\"></td>";
    return retour;
  }

  /**
   * Method declaration
   * @param nam
   * @param url
   * @param pc
   * @return
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
   * @param req
   * @see
   */
  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelValue"));
    setValue(req.getParameter("configuratorDefaultValue"));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printDemo() {
    String retour =
        "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + label;

    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;";
    retour = retour + "<input type=\"text\" name=\"" + name + "\" value=\""
        + value + "\"></td>";
    return retour;
  }

  /**
   * Method declaration
   * @return
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
   * @return
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
