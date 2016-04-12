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
import org.silverpeas.core.contribution.content.form.field.AccessPathField;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A AccessPathFieldDisplayer is an object which can display in a HTML field the current access
 * path
 * of the form (space > subSpace > service > theme > subTheme) to a end user and can retrieve via
 * HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class AccessPathFieldDisplayer extends AbstractFieldDisplayer<AccessPathField> {

  /**
   * Constructeur
   */
  public AccessPathFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{AccessPathField.TYPE};
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {
    // not applicable
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, AccessPathField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String value = null;
    StringBuilder html = new StringBuilder();

    String fieldName = template.getFieldName();
    String currentAccessPath = "";
    if (!AccessPathField.TYPE.equals(field.getTypeName())) {

    } else {
      currentAccessPath = field
          .getAccessPath(PagesContext.getComponentId(), PagesContext.getNodeId(),
              PagesContext.getContentLanguage());
    }

    if (!field.isNull()) {
      value = field.getValue(PagesContext.getLanguage());
    }
    html.append("<input id=\"").append(fieldName).append("\" name=\"").append(fieldName)
        .append("\" type=\"text\" size=\"80\"");
    if (value != null) {
      html.append(" value=\"").append(value).append("\"");
    } else {
      html.append(" value=\"").append(currentAccessPath).append("\"");
    }
    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled=\"disabled\"");
    }
    html.append("/>\n");

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
        !template.isHidden() && PagesContext.useMandatory()) {
      html.append(Util.getMandatorySnippet());
    }
    out.println(html);
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request. @throw FormException if the field type is not a managed type. @throw
   * FormException
   * if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(String newValue, AccessPathField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {

    if (!AccessPathField.TYPE.equals(field.getTypeName())) {
      throw new FormException("AccessPathFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          AccessPathField.TYPE);
    }

    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("AccessPathFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          AccessPathField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return false;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
