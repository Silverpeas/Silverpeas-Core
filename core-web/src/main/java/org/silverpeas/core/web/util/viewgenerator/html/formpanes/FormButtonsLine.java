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
 * FormButtonsLine.java
 *
 */
package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

/**
 * @author frageade
 */
public class FormButtonsLine extends FormLine {

  private final List<FormButton> buttons;

  public FormButtonsLine(String nam, String val) {
    super(nam, val);
    buttons = new ArrayList<>();
    setType("buttonLine");
  }

  public FormButtonsLine(String nam, String val, String lab) {
    super(nam, val);
    setLabel(lab);
    buttons = new ArrayList<>();
    setType("buttonLine");
  }

  public void addButton(FormButton fb) {
    buttons.add(fb);
  }

  @Override
  public String print() {
    StringBuilder buttonLine = new StringBuilder();

    if (!label.isEmpty()) {
      buttonLine.append("\n<td>").append(noNull(label)).append("</td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    if (!buttons.isEmpty()) {
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

  @Override
  public FormPane getDescriptor(String nam, String url, PageContext pc) {
    FormPaneWA fpw = new FormPaneWA(nam, url, pc);

    fpw.add(new FormLabel("configuratorTitle", "Configuration du FormLabel"));
    fpw.add(new FormTextField("configuratorLabelValue", "",
        "Entrez la valeur : "));
    fpw.add(new FormButtonSubmit("newConfiguratorSubmitButton", "Créer"));
    return fpw;
  }

  @Override
  public void getConfigurationByRequest(HttpServletRequest req) {
    setLabel(req.getParameter("configuratorLabelValue"));
  }

  @Override
  public String printDemo() {
    StringBuilder buttonLine = new StringBuilder();

    if (!label.isEmpty()) {
      buttonLine.append("\n<td>").append(label).append("</td>");
    } else {
      buttonLine.append("\n<td>&nbsp;</td>");
    }
    if (!buttons.isEmpty()) {
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

  @Override
  public String toXML() {
    StringBuilder buttonLine = new StringBuilder();
    buttonLine.append("\n<field id=\"").append(id).append("\" type=\"buttonLine\">").
        append("\n<name>").append(name).append("</name>").
        append("\n<label>").append(label).append("</label>").
        append("\n<value>").append(value).append("</value>");
    if (!buttons.isEmpty()) {
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
