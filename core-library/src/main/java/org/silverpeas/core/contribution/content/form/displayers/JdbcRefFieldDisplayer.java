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

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.JdbcRefField;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.URLUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.silverpeas.core.util.logging.SilverLogger;

public class JdbcRefFieldDisplayer extends AbstractFieldDisplayer<JdbcRefField> {

  @Override
  public void display(PrintWriter out, JdbcRefField field, FieldTemplate template,
      PagesContext pagesContext)
      throws FormException {
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();

    if (!JdbcRefField.TYPE.equals(field.getTypeName())) {

    }

    ElementContainer container = new ElementContainer();

    Input input = new Input();
    input.setName(fieldName);
    input.setValue(EncodeHelper.javaStringToHtmlString(!field.isNull() ? field.getValue(language)
        : ""));
    input.setType(Input.text);
    input.setSize(50);
    if (template.isDisabled()) {
      input.setDisabled(true);
    } else if (template.isReadOnly()) {
      input.setReadOnly(true);
    }
    container.addElement(input);

    if (template.isMandatory()) {
      container.addElement("&nbsp;");

      IMG img = new IMG();
      img.setSrc(Util.getIcon("mandatoryField"));
      img.setWidth(5);
      img.setHeight(5);
      img.setBorder(0);
      container.addElement(img);
    }

    container.addElement("&nbsp;");
    A link = new A();
    link.setHref("#");

    StringBuilder onclick = new StringBuilder(200).append("javascript:SP_openWindow('")
        .append(URLUtil.getApplicationURL()).append("/RselectionPeas/jsp/Main")
        .append("?SelectionType=JdbcConnector")
        .append("&formIndex=").append(pagesContext.getFormIndex());
    final String[] parametersKeys = {"beanName", "componentId", "method", "tableName"};
    Map<String, String> parameters = template.getParameters(language);
    for (final String parametersKey : parametersKeys) {
      if (parameters.containsKey(parametersKey)) {
        onclick.append("&").append(parametersKey).append("=").append(parameters.get(parametersKey));
      }
    }
    StringTokenizer columnsNamesSt =
        new StringTokenizer(parameters.get("columnsNames"), "#");
    StringTokenizer fieldsNamesSt =
        new StringTokenizer(parameters.get("fieldsNames"), "#");
    int index = 0;
    while (columnsNamesSt.hasMoreTokens()) {
      onclick.append("&columnName").append(index).append("=").append(columnsNamesSt.nextToken())
          .append("&fieldName").append(index).append("=").append(fieldsNamesSt.nextToken());
      index++;
    }
    onclick.append("', 'win_").append(fieldName).append("', 800, 600, 'scrollbars=yes');");
    link.setOnClick(onclick.toString());
    IMG linkImg = new IMG();
    linkImg.setSrc(Util.getIcon("jdbc"));
    linkImg.setWidth(15);
    linkImg.setHeight(15);
    linkImg.setBorder(0);
    String selectLabel = Util.getString("SelectValue", language);
    linkImg.setAlt(selectLabel);
    linkImg.setTitle(selectLabel);
    linkImg.setAlign(AlignType.absmiddle);
    link.addElement(linkImg);
    container.addElement(link);

    out.println(container.toString());
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    String language = pagesContext.getLanguage();
    String label = template.getLabel(language);

    if (!template.getTypeName().equals(JdbcRefField.TYPE)) {
      SilverLogger.getLogger(this).info("{0} isn't of the correct type {1}",
          template.getFieldName(), JdbcRefField.TYPE);
    }

    if (template.isMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("     errorMsg += \"  - '" + label + "' " + Util.getString("GML.MustBeFilled",
          language) + "\\n\";");
      out.println("     errorNb++;");
      out.println("   }");
    }
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public List<String> update(String value, JdbcRefField field, FieldTemplate template,
      PagesContext pagesContext)
      throws FormException {
    if (!JdbcRefField.TYPE.equals(field.getTypeName())) {
      throw new FormException("JdbcRefFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          JdbcRefField.TYPE);
    }
    if (field.acceptValue(value, pagesContext.getLanguage())) {
      field.setValue(value, pagesContext.getLanguage());
    } else {
      throw new FormException("JdbcRefFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          JdbcRefField.TYPE);
    }
    return new ArrayList<>();
  }
}
