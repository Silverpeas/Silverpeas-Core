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
 * FormSelect.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frageade
 * @version
 */

public class FormSelect extends FormLine {

  private int size;
  private int nbItems;
  private List<String> itemsLabels;
  private List<String> itemValues;
  private List<Boolean> itemsSelected;

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @see
   */
  public FormSelect(String nam, String val) {
    super(nam, val);
    setLabel(nam);
    size = 1;
    itemsLabels = new ArrayList<String>();
    itemValues = new ArrayList<String>();
    itemsSelected = new ArrayList<Boolean>();
    nbItems = 0;
    setType("select");
  }

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @param lab
   * @param siz
   * @see
   */
  public FormSelect(String nam, String val, String lab, int siz) {
    super(nam, val);
    setLabel(lab);
    size = siz;
    itemsLabels = new ArrayList<String>();
    itemValues = new ArrayList<String>();
    itemsSelected = new ArrayList<Boolean>();
    nbItems = 0;
    setType("select");
  }

  /**
   * Method declaration
   * @param itemsLabel
   * @param itemValue
   * @param selected
   * @see
   */
  public void addItem(String itemsLabel, String itemValue, boolean selected) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(selected);
    nbItems++;
  }

  /**
   * Method declaration
   * @param itemsLabel
   * @param itemValue
   * @see
   */
  public void addItem(String itemsLabel, String itemValue) {
    itemsLabels.add(itemsLabel);
    itemValues.add(itemValue);
    itemsSelected.add(Boolean.FALSE);
    nbItems++;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "\n<td><select name=\"" + name + "\" size=\""
        + String.valueOf(size) + "\">";
    for (int i = 0; i < nbItems; i++) {
      retour = retour + "\n<option value=\"" + itemValues.get(i)
          + "\"";
      if (itemsSelected.get(i)) {
        retour = retour + " selected ";
      }
      retour = retour + ">" + itemsLabels.get(i) + "</option>";
    }
    retour = retour + "\n</select></td>";
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
    setLabel(req.getParameter("configuratorLabelValue"));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printDemo() {
    String retour = "\n<td>" + label + "</td>";

    retour = retour + "<td>" + value + "</td>";
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
