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
 * FormSelect.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frageade
 */
public class FormSelect extends FormLine {

  private final int size;
  private int nbItems;
  private final List<String> itemsLabels;
  private final List<String> itemValues;
  private final List<Boolean> itemsSelected;

  public FormSelect(String nam, String val) {
    super(nam, val);
    setLabel(nam);
    size = 1;
    itemsLabels = new ArrayList<>();
    itemValues = new ArrayList<>();
    itemsSelected = new ArrayList<>();
    nbItems = 0;
    setType("select");
  }

  public FormSelect(String nam, String val, String lab, int siz) {
    super(nam, val);
    setLabel(lab);
    size = siz;
    itemsLabels = new ArrayList<>();
    itemValues = new ArrayList<>();
    itemsSelected = new ArrayList<>();
    nbItems = 0;
    setType("select");
  }

  public void addItem(String itemsLabel, String itemValue, boolean selected) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(selected);
    nbItems++;
  }

  public void addItem(String itemsLabel, String itemValue) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(Boolean.FALSE);
    nbItems++;
  }

  public String print() {
    StringBuilder retour = new StringBuilder("\n<td>").append(label).append("</td>");

    retour.append("\n<td><select name=\"")
        .append(name)
        .append("\" size=\"")
        .append(size)
        .append("\">");
    for (int i = 0; i < nbItems; i++) {
      retour.append("\n<option value=\"").append(itemValues.get(i)).append("\"");
      if (itemsSelected.get(i)) {
        retour.append(" selected ");
      }
      retour.append(">").append(itemsLabels.get(i)).append("</option>");
    }
    retour.append("\n</select></td>");
    return retour.toString();
  }

  public FormPane getDescriptor(String nam, String url, PageContext pc) {
    FormPaneWA fpw = new FormPaneWA(nam, url, pc);

    fpw.add(new FormLabel("configuratorTitle", "Configuration du FormLabel"));
    fpw.add(new FormTextField("configuratorLabelValue", "",
        "Entrez la valeur : "));
    fpw.add(new FormButtonSubmit("newConfiguratorSubmitButton", "Créer"));
    return fpw;
  }

  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelValue"));
  }

  public String printDemo() {
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "<td>" + value + "</td>";
    return retour;
  }

  public String toXML() {
    String retour = "\n<field id=\"" + id + "\" type=\"label\">";

    retour = retour + "\n</field>";
    return retour;
  }

}
