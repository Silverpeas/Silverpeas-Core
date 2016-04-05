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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.apache.ecs.html.Input;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.util.EncodeHelper;

import java.util.Collections;
import java.util.List;

/**
 * The UniqueIdFieldDisplayer displays a unique id as string in a read-only mode Unique id is the
 * result of the new Date().getTime() operation. A suffix can be added by using the "suffix"
 * parameter (value "userid")
 * @author Nicolas EYSSERIC
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class UniqueIdFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  public UniqueIdFieldDisplayer() {
  }

  public String[] getManagedTypes() {
    return new String[] { TextField.TYPE };
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    if (!template.getTypeName().equals(TextField.TYPE)) {

    }
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext)
      throws FormException {
    if (field == null) {
      return;
    }

    if (!TextField.TYPE.equals(field.getTypeName())) {

    }

    String fieldName = template.getFieldName();

    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    String defaultValue = Long.toString(new Date().getTime());
    String suffix = parameters.get("suffix");
    if ("userid".equalsIgnoreCase(suffix)) {
      defaultValue += "-" + pageContext.getUserId();
    }

    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    Input input = new Input();
    input.setName(fieldName);
    input.setID(fieldName);
    input.setValue(EncodeHelper.javaStringToHtmlString(value));
    input.setType(template.isHidden() ? Input.hidden : Input.text);
    input.setSize(parameters.containsKey("size") ? parameters.get("size") : "50");
    input.setReadOnly(true);

    out.println(input.toString());
  }

  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pagesContext)
      throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("UniqueIdFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("UniqueIdFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
