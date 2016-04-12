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
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A UserFieldDisplayer is an object which can display a UserFiel in HTML and can retrieve via HTTP
 * any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class UserFieldDisplayer extends AbstractFieldDisplayer<UserField> {

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
    String language = pagesContext.getLanguage();

    if (!UserField.TYPE.equals(template.getTypeName())) {

    }
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language))
          + "' " + Util.getString("GML.MustBeFilled", language)
          + "\\n\";");
      out.println("      errorNb++;");
      out.println("   }");
    }

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

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
  public void display(PrintWriter out, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String language = pageContext.getLanguage();
    String selectUserImg = Util.getIcon("userPanel");
    String selectUserLab = Util.getString("userPanel", language);
    String deleteUserImg = Util.getIcon("delete");
    String deleteUserLab = Util.getString("clearUser", language);

    String userName = "";
    String userId = "";
    String html = "";

    String fieldName = template.getFieldName();

    if (!field.getTypeName().equals(UserField.TYPE)) {

    } else {
      userId = field.getUserId();
    }
    if (!field.isNull()) {
      userName = field.getValue();
    }
    html +=
        "<input type=\"hidden\"" + " id=\"" + fieldName + "\" name=\"" + fieldName + "\" value=\""
        + EncodeHelper.javaStringToHtmlString(userId) + "\"/>";

    if (!template.isHidden()) {
      html +=
          "<input type=\"text\" disabled=\"disabled\" size=\"50\" "
          + "id=\"" + fieldName + "_name\" name=\"" + fieldName + "$$name\" value=\""
          + EncodeHelper.javaStringToHtmlString(userName) + "\"/>";
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {

      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      String roles = parameters.get("roles");
      boolean usersOfInstanceOnly =
          StringUtil.getBooleanValue(parameters.get("usersOfInstanceOnly"));
      if (StringUtil.isDefined(roles)) {
        usersOfInstanceOnly = true;
      }

      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('"
          + URLUtil.getApplicationURL() + "/RselectionPeasWrapper/jsp/open"
          + "?formName=" + pageContext.getFormName()
          + "&elementId=" + fieldName
          + "&elementName=" + fieldName + "_name"
          + "&selectedUser=" + ((userId == null) ? "" : userId);
      if (usersOfInstanceOnly) {
        html += "&instanceId=" + pageContext.getComponentId();
      }
      if (StringUtil.isDefined(roles)) {
        html += "&roles=" + roles;
      }
      html += "','selectUser',800,600,'');\" >";
      html += "<img src=\""
          + selectUserImg
          + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
          + selectUserLab + "\" align=\"top\" title=\""
          + selectUserLab + "\"/></a>";
      html +=
          "&nbsp;<a href=\"#\" onclick=\"javascript:"
          + "document." + pageContext.getFormName() + "." + fieldName + ".value='';"
          + "document." + pageContext.getFormName() + "." + fieldName + "$$name"
          + ".value='';"
          + "\">";
      html += "<img src=\""
          + deleteUserImg
          + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
          + deleteUserLab + "\" align=\"top\" title=\""
          + deleteUserLab + "\"/></a>";

      if (template.isMandatory() && pageContext.useMandatory()) {
        html += Util.getMandatorySnippet();
      }
    }

    out.println(html);
  }

  @Override
  public List<String> update(String newId, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    if (UserField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newId)) {
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
    return 2;
  }

}
