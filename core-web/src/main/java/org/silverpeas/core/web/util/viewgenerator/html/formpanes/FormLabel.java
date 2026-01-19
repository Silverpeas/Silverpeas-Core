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
 * FormLabel.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author frageade
 */
public class FormLabel extends FormLine {

  public FormLabel() {
    super();
    setName("newFormLabel");
    setType("label");
  }

  public FormLabel(String nam, String val) {
    super(nam, val);
    setType("label");
  }

  public FormLabel(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    setType("label");
  }

  public String print() {
    String retour =
        "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + noNull(label);

    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;"
        + noNull(value) + "</td>";
    return retour;
  }

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

  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelTitle"));
    setValue(req.getParameter("configuratorLabelValue"));
  }

  public String printDemo() {
    String retour =
        "\n<td class=\"couleurFondCadre\" align=\"right\" width=\"50%\"><span class=\"txtnote\">"
        + label;

    retour = retour + "&nbsp;</span></td>";
    retour = retour + "<td class=\"couleurFondCadre\" width=\"50%\">&nbsp;"
        + value + "</td>";
    return retour;
  }

  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n<name>" + name + "</name>";
    retour = retour + "\n<label>" + label + "</label>";
    retour = retour + "\n<value>" + value + "</value>";
    retour = retour + "\n</field>";
    return retour;
  }

}
