/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.frame;
import org.apache.ecs.xhtml.iframe;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.Map;

/**
 * A UserFieldDisplayer is an object which can display a UserFiel in HTML and can retrieve via HTTP
 * any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class MapFieldDisplayer extends AbstractTextFieldDisplayer {

  public final static String PARAM_MAP = "map";
  public final static String PARAM_HEIGHT = "height";
  public final static String PARAM_WIDTH = "width";
  public final static String PARAM_KIND = "kind";
  public final static String PARAM_ZOOM = "zoom";
  public final static String PARAM_ENLARGE = "enlarge";

  public final static String KIND_NORMAL = "m";
  public final static String KIND_SATELLITE = "k";
  public final static String KIND_HYBRID = "h";
  public final static String KIND_RELIEF = "t";

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (field == null) {
      return;
    }

    if (!field.getTypeName().equals(TextField.TYPE)) {

    }

    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    String value = field.getValue();
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    if (!template.isHidden()) {
      if (template.isReadOnly()) {
        StringBuilder src = new StringBuilder(50);
        src.append("https://maps.google.fr/maps?");
        src.append("hl=").append(pageContext.getLanguage()).append("&amp;");
        src.append("source=embed&amp;");
        src.append("layer=c&amp;");
        src.append("t=").append(getParameterValue(parameters, PARAM_KIND, KIND_NORMAL))
            .append("&amp;");
        src.append("q=").append(value).append("&amp;");
        String zoom = getParameterValue(parameters, PARAM_ZOOM, null);
        if (StringUtil.isDefined(zoom)) {
          src.append("z=").append(zoom).append("&amp;");
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
          anIFrame.addAttribute("width", getParameterValue(parameters, PARAM_WIDTH, "425"));
          anIFrame.addAttribute("height", getParameterValue(parameters, PARAM_HEIGHT, "350"));
          anIFrame.setFrameBorder(false);
          anIFrame.setScrolling(frame.no);
          anIFrame.setMarginHeight(0);
          anIFrame.setMarginWidth(0);
          anIFrame.setSrc(link.replace("source", "output"));
          container.addElement(anIFrame);

          if (enlarge) {
            container.addElement("<br/>");
            container.addElement("<small class=\"map-enlarge\">");
            container.addElement(href);
            container.addElement("</small>");
          }
        } else {
          container.addElement(href);
        }

        out.print(container.toString());
      } else if (!template.isDisabled()) {

        input textInput = new input();
        textInput.setName(fieldName);
        textInput.setID(fieldName);
        textInput.setValue(value);
        textInput.setSize("50");
        textInput.setType(template.isHidden() ? input.hidden : input.text);
        if (template.isDisabled()) {
          textInput.setDisabled(true);
        } else if (template.isReadOnly()) {
          textInput.setReadOnly(true);
        }

        img image = null;
        if (template.isMandatory() && pageContext.useMandatory()) {
          image = new img();
          image.setSrc(Util.getIcon("mandatoryField"));
          image.setWidth(5);
          image.setHeight(5);
          image.setBorder(0);
        }

        if (image != null) {
          ElementContainer container = new ElementContainer();
          container.addElement(textInput);
          container.addElement("&nbsp;");
          container.addElement(image);
          out.println(container.toString());
        } else {
          out.println(textInput.toString());
        }

      }
    }
  }

  private String getParameterValue(Map<String, String> parameters, String name, String defaultValue) {
    return parameters.containsKey(name) ? parameters.get(name) : defaultValue;
  }

}