/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormPassword.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author frageade
 * @version
 */

public class FormPassword extends FormLine {

  /**
   * Constructor declaration
   * @param nam
   * @param val
   *
   */
  public FormPassword(String nam, String val) {
    super(nam, val);
    setLabel(nam);
    setType("password");
  }

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @param lab
   *
   */
  public FormPassword(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    setType("password");
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String print() {
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "<td><input type=\"password\" name=\"" + name
        + "\" value=\"" + value + "\"></td>";
    return retour;
  }

  /**
   * Method declaration
   * @param nam
   * @param url
   * @param pc
   * @return
   *
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
   *
   */
  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelValue"));
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String printDemo() {
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "<td>" + value + "</td>";
    return retour;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n</field>";
    return retour;
  }

}
