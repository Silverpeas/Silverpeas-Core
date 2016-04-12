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
import org.silverpeas.core.contribution.content.form.field.MultipleUserField;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;

/**
 * A MultipleUserFieldDisplayer is an object which can display a MultipleUserField in HTML and can
 * retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class MultipleUserFieldDisplayer extends AbstractFieldDisplayer<MultipleUserField> {

  static final private String ROWS_DEFAULT_VALUE = "5";
  static final private String COLS_DEFAULT_VALUE = "50";

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{MultipleUserField.TYPE};
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
  public void displayScripts(PrintWriter out, FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {
    String language = PagesContext.getLanguage();

    if (!template.getTypeName().equals(MultipleUserField.TYPE)) {


    }
    if (template.isMandatory()) {
      StringBuilder html = new StringBuilder();
      html.append("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      html.append("      errorMsg+=\"  - '")
          .append(EncodeHelper.javaStringToJsString(template.getLabel(language)))
          .append("' ").append(Util.getString("GML.MustBeFilled", language))
          .append("\\n\";");
      html.append("      errorNb++;");
      html.append("   }");

      out.println(html.toString());
    }

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
  public void display(PrintWriter out, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String language = pageContext.getLanguage();
    String selectUserImg = Util.getIcon("userPanel");
    String selectUserLab = Util.getString("userPanel", language);

    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
    String rows =
        (parameters.containsKey("rows")) ? parameters.get("rows") : ROWS_DEFAULT_VALUE;
    String cols =
        (parameters.containsKey("cols")) ? parameters.get("cols") : COLS_DEFAULT_VALUE;
    boolean usersOfInstanceOnly = StringUtil.getBooleanValue(parameters.get("usersOfInstanceOnly"));
    String roles = parameters.get("roles");
    if (StringUtil.isDefined(roles)) {
      usersOfInstanceOnly = true;
    }

    String userNames = "";
    String userIds = "";
    StringBuilder html = new StringBuilder();

    String fieldName = template.getFieldName();

    if (!field.getTypeName().equals(MultipleUserField.TYPE)) {

    } else {
      userIds = field.getStringValue();
    }
    if (!field.isNull()) {
      userNames = field.getValue();
    }
    html.append("<input type=\"hidden\" id=\"").append(fieldName).append("\" name=\"")
        .append(fieldName)
        .append("\" value=\"").append(EncodeHelper.javaStringToHtmlString(userIds)).append("\" />");

    if (!template.isHidden()) {
      html.append("<textarea id=\"").append(fieldName).append("_name\" name=\"").append(fieldName)
          .append("$$name\" disabled=\"disabled\" rows=\"").append(rows).append("\" cols=\"")
          .append(cols)
          .append("\">")
          .append(EncodeHelper.javaStringToHtmlString(userNames)).append("</textarea>");
    }

    if (!template.isHidden() && !template.isDisabled()
        && !template.isReadOnly()) {
      html.append("&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('")
          .append(URLUtil.getApplicationURL())
          .append("/RselectionPeasWrapper/jsp/open?formName=").append(pageContext.getFormName())
          .append("&elementId=").append(fieldName)
          .append("&elementName=").append(fieldName).append("$$name")
          .append("&selectedUsers=").append(userIds == null ? "" : userIds)
          .append(usersOfInstanceOnly ? "&instanceId=" + pageContext.getComponentId() : "")
          .append(StringUtil.isDefined(roles) ? "&roles=" + roles : "")
          .append("&selectionMultiple=true")
          .append("','selectUser',800,600,'');\" >");

      html.append("<img src=\"").append(selectUserImg).append(
          "\" width=\"15\" height=\"15\" border=\"0\" alt=\"")
          .append(selectUserLab).append("\" align=\"absmiddle\" title=\"").append(selectUserLab)
          .append("\"/></a>");

      if (template.isMandatory()) {
        html.append(Util.getMandatorySnippet());
      }
    }

    out.println(html.toString());
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(String newIds, MultipleUserField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (MultipleUserField.TYPE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(newIds)) {
        field.setNull();
      } else {
        field.setStringValue(newIds);
      }
    } else {
      throw new FormException("UserFieldDisplayer.update",
          "form.EX_NOT_CORRECT_VALUE", UserField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> update(List<FileItem> items, MultipleUserField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES && !StringUtil.
        isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template,
      PagesContext pagesContext) {
    return 2;
  }
}
