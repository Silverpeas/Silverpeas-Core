/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
 * FormHidden.java
 * 
 */

package com.stratelia.webactiv.util.viewGenerator.html.formPanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author frageade
 * @version
 */

public class FormHidden extends FormLine {

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @see
   */
  public FormHidden(String nam, String val) {
    super(nam, val);
    setType("hidden");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String retour = "\n<input type=\"hidden\" name=\"" + name + "\" value=\""
        + value + "\">";

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

    fpw.add(new FormLabel("configuratorTitle", "Configuration du FormLabel"));
    fpw.add(new FormTextField("configuratorLabelValue", "",
        "Entrez la valeur : "));
    fpw.add(new FormButtonSubmit("newConfiguratorSubmitButton", "Créer"));
    return fpw;
  }

  /**
   * Method declaration
   * @param req
   * @see
   */
  public void getConfigurationByRequest(HttpServletRequest req) {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printDemo() {
    String retour = "";

    return retour;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n</field>";
    return retour;
  }

}
