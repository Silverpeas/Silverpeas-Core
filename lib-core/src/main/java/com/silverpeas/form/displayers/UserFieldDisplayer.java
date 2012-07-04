/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.displayers;

import java.io.PrintWriter;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

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
   * @return
   */
  public String[] getManagedTypes() {
    return new String[] { UserField.TYPE };
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
   * @param out
   * @param template
   * @param pagesContext
   * @throws java.io.IOException
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    String language = pagesContext.getLanguage();

    if (!UserField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "TextFieldDisplayer.displayScripts",
          "form.INFO_NOT_CORRECT_TYPE", UserField.TYPE);
    }
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language))
          + "' " + Util.getString("GML.MustBeFilled", language)
          + "\\n \";");
      out.println("      errorNb++;");
      out.println("   }");
    }

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   * @param out
   * @param field
   * @param PagesContext
   * @param template
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    SilverTrace.info("form", "UserFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.getFieldName() + ", value = " + field.getValue()
        + ", fieldType = " + field.getTypeName());

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
      SilverTrace.info("form", "UserFieldDisplayer.display",
          "form.INFO_NOT_CORRECT_TYPE", UserField.TYPE);
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
          + URLManager.getApplicationURL() + "/RselectionPeasWrapper/jsp/open"
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
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pageContext) {
    return 2;
  }

  @Override
  public List<String> update(List<FileItem> items, UserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
        && !StringUtil.isDefined(value)) {
      return new ArrayList<String>();
    }
    return update(value, field, template, pageContext);
  }
}
