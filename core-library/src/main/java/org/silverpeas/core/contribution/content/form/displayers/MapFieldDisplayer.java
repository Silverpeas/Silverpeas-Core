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
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.*;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;
import java.util.Map;

/**
 * An HTML displayer of map within a form field.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class MapFieldDisplayer extends AbstractTextFieldDisplayer {

  public static final String PARAM_MAP = "map";
  public static final String PARAM_HEIGHT = "height";
  public static final String PARAM_WIDTH = "width";
  public static final String PARAM_KIND = "kind";
  public static final String PARAM_ZOOM = "zoom";
  public static final String PARAM_ENLARGE = "enlarge";

  public static final String KIND_NORMAL = "m";
  public static final String KIND_SATELLITE = "k";
  public static final String KIND_HYBRID = "h";
  public static final String KIND_RELIEF = "t";
  private static final String AMP = "&amp;";

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (field == null) {
      return;
    }

    FieldProperties fieldProps = getFieldProperties(template, field, pageContext);
    if (!template.isHidden()) {
      if (template.isReadOnly()) {
        printOutReadOnlyField(fieldProps, out, pageContext);
      } else if (!template.isDisabled()) {
        printOutEditableField(fieldProps, out, pageContext);
      }
    }
  }

  private void printOutEditableField(FieldProperties fieldProps, PrintWriter out,
      PagesContext pageContext) {
    var template = fieldProps.getTemplate();

    input textInput = new input();
    textInput.setName(fieldProps.getFieldName());
    textInput.setID(fieldProps.getFieldName());
    textInput.setValue(fieldProps.getValue());
    textInput.setSize("50");
    textInput.setType(template.isHidden() ? input.hidden : input.text);
    if (template.isDisabled()) {
      textInput.setDisabled(true);
    } else if (template.isReadOnly()) {
      textInput.setReadOnly(true);
    }

    img image = getMandatoryIcon(template, pageContext);
    var elt = setImage(textInput, image);
    out.println(elt);
  }

  private void printOutReadOnlyField(FieldProperties fieldProps, PrintWriter out,
      PagesContext pageContext) {
    var parameters = fieldProps.getParameters();
    var value = fieldProps.getValue();

    StringBuilder src = new StringBuilder(50);
    src.append("https://maps.google.fr/maps?");
    src.append("hl=").append(pageContext.getLanguage()).append(AMP);
    src.append("source=embed&amp;");
    src.append("layer=c&amp;");
    src.append("t=").append(getParameterValue(parameters, PARAM_KIND, KIND_NORMAL))
        .append(AMP);
    src.append("q=").append(value).append(AMP);
    String zoom = getParameterValue(parameters, PARAM_ZOOM, null);
    if (StringUtil.isDefined(zoom)) {
      src.append("z=").append(zoom).append(AMP);
    }
    src.append("iwloc=dummy");
    String link = src.toString();

    a href = new a();
    href.setHref(link);
    href.setTarget("_blank");
    href.addElement(value);

    boolean map = StringUtil.getBooleanValue(getParameterValue(parameters, PARAM_MAP, "false"));
    boolean enlarge =
        StringUtil.getBooleanValue(getParameterValue(parameters, PARAM_ENLARGE, "false"));

    ElementContainer container = new ElementContainer();

    if (map) {
      iframe anIFrame = new iframe();
      anIFrame.addAttribute(PARAM_WIDTH, getParameterValue(parameters, PARAM_WIDTH, "425"));
      anIFrame.addAttribute(PARAM_HEIGHT, getParameterValue(parameters, PARAM_HEIGHT, "350"));
      anIFrame.setFrameBorder(false);
      anIFrame.setScrolling(frame.no);
      anIFrame.setMarginHeight(0);
      anIFrame.setMarginWidth(0);
      anIFrame.setSrc(link.replace("source", "output"));
      container.addElement(anIFrame);

      if (enlarge) {
        container.addElement("<br>");
        container.addElement("<small class=\"map-enlarge\">");
        container.addElement(href);
        container.addElement("</small>");
      }
    } else {
      container.addElement(href);
    }

    out.print(container);
  }

  private String getParameterValue(Map<String, String> parameters, String name, String defaultValue) {
    return parameters.getOrDefault(name, defaultValue);
  }

}