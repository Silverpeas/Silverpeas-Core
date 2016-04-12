/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormButtonsLine.java
 *
 */
package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * @author frageade
 * @version
 */
public class FormButtonsLine extends FormLine {

  private List<FormButton> buttons;

  /**
   * Constructor declaration
   *
   * @param nam
   * @param val
   * @see
   */
  public FormButtonsLine(String nam, String val) {
    super(nam, val);
    buttons = new ArrayList<FormButton>();
    setType("buttonLine");
  }

  /**
   * Constructor declaration
   *
   * @param nam
   * @param val
   * @param lab
   * @see
   */
  public FormButtonsLine(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    buttons = new ArrayList<FormButton>();
    setType("buttonLine");
  }

  /**
   * Method declaration
   *
   * @param fb
   * @see
   */
  public void addButton(FormButton fb) {
    buttons.add(fb);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String print() {
    StringBuilder buttonLine = new StringBuilder();

    if (!label.equals("")) {
      buttonLine.append("\n<td>").append(noNull(label)).append("</td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    if (buttons.size() > 0) {
      buttonLine.append("\n<td><table><tr>");
      for (FormButton button : buttons) {
        buttonLine.append(button.print());
      }
      buttonLine.append("\n</tr></table></td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    return buttonLine.toString();
  }

  /**
   * Method declaration
   *
   * @param nam
   * @param url
   * @param pc
   * @return
   * @see
   */
  @Override
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
   * @param req
   * @see
   */
  @Override
  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelValue"));
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String printDemo() {
    StringBuilder buttonLine = new StringBuilder();

    if (!label.equals("")) {
      buttonLine.append("\n<td>").append(label).append("</td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    if (buttons.size() > 0) {
      buttonLine.append("\n<td><table><tr>");
      for (FormButton button : buttons) {
        buttonLine.append(button.printDemo());
      }
      buttonLine.append("\n</tr></table></td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    return buttonLine.toString();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String toXML() {
    StringBuilder buttonLine = new StringBuilder();
    buttonLine.append("\n<field id=\"").append(id).append("\" type=\"buttonLine\">").
        append("\n<name>").append(name).append("</name>").
        append("\n<label>").append(label).append("</label>").
        append("\n<value>").append(value).append("</value>");
    if (buttons.size() > 0) {
      buttonLine.append("\n<actions>");
      for (FormButton button : buttons) {
        buttonLine.append(button.toLineXML());
      }
      buttonLine.append("\n</actions>");
    }
    buttonLine.append("\n</field>");
    return buttonLine.toString();
  }
}
