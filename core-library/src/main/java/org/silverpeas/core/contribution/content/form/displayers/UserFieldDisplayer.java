/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.ecs.xhtml.div;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.html.plugin.UserGroupSelectProducer;
import org.silverpeas.core.util.WebEncodeHelper;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.SelectionType.USER;
import static org.silverpeas.core.html.plugin.UserGroupSelectProducer.withContainerId;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A UserFieldDisplayer is an object which can display a UserFiel in HTML and can retrieve via HTTP
 * any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class UserFieldDisplayer extends AbstractFieldDisplayer<UserField> {

  private static final int NB_HTML_ELEMENTS = 2;

  /**
   * The production is done from a static method because some other fields need to perform
   * exactly the same check.
   * @param out the writer into which the javascript is written.
   * @param template the field template data.
   * @param pagesContext the page context.
   */
  static void produceMandatoryCheck(final PrintWriter out, final FieldTemplate template,
      final PagesContext pagesContext) {
    final String language = pagesContext.getLanguage();
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '"
          + WebEncodeHelper.javaStringToJsString(template.getLabel(language))
          + "' " + Util.getString("GML.MustBeFilled", language)
          + "\\n\";");
      out.println("      errorNb++;");
      out.println("   }");
    }
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { UserField.TYPE };
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the fieldName is unknown by the template.</li>
   * <li>the field type is not a managed type.</li>
   * </ul>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    produceMandatoryCheck(out, template, pagesContext);
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws FormException if exists an error
   */
  @Override
  public void display(PrintWriter out, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    final boolean writable =
        !template.isHidden() && !template.isDisabled() && !template.isReadOnly();
    final String language = pageContext.getLanguage();
    final String selectUserLab = Util.getString("userPanel", language);
    final String deleteUserLab = Util.getString("clearUser", language);
    final String fieldName = template.getFieldName();
    final String rootContainerId = "select-user-group-" + fieldName;
    final String userId = field.getTypeName().equals(UserField.TYPE) ? field.getUserId() : "";

    final UserGroupSelectProducer selectUser = withContainerId(rootContainerId)
        .withUserInputName(fieldName)
        .selectionOf(USER)
        .multiple(false)
        .readOnly(!writable)
        .hidden(template.isHidden())
        .withUserIds(userId)
        .withUserPanelButtonLabel(selectUserLab)
        .withRemoveButtonLabel(deleteUserLab);
    if (writable) {
      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String roles = parameters.get("roles");
      boolean usersOfInstanceOnly =
          getBooleanValue(parameters.get("usersOfInstanceOnly")) || isDefined(roles);
      if (usersOfInstanceOnly) {
        selectUser.filterOnComponentId(pageContext.getComponentId());
      }
      selectUser.filterOnRoles(roles);
      selectUser.mandatory(template.isMandatory() && pageContext.useMandatory());
    }

    out.println(new div().setID(rootContainerId));
    out.println(selectUser.produce());
  }

  @Override
  public List<String> update(String newId, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (UserField.TYPE.equals(field.getTypeName())) {
      if (!isDefined(newId)) {
        field.setNull();
      } else {
        field.setUserId(newId);
      }
    } else {
      throw new FormException("UserFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          UserField.TYPE);
    }
    return Collections.emptyList();
  }

  /**
   * Method declaration
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pageContext) {
    return NB_HTML_ELEMENTS;
  }

}
