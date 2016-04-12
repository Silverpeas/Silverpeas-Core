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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.contribution.content.form.Field;
import org.apache.commons.fileupload.FileItem;

import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.GroupField;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;

/**
 * A GroupFieldDisplayer is an object which allow to select a group and display it in HTML and can
 * retrieve via HTTP any updated value.
 * <p/>
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class GroupFieldDisplayer extends AbstractFieldDisplayer<GroupField> {

  private final static int GROUP = 1;

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { GroupField.TYPE };
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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {
    String language = PagesContext.getLanguage();

    if (!GroupField.TYPE.equals(template.getTypeName())) {

    }
    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("   if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("      errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language))
          + "' " + Util.getString("GML.MustBeFilled", language)
          + "\\n\";");
      out.println("      errorNb++;");
      out.println("   }");
    }
    Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   */
  @Override
  public void display(PrintWriter out, GroupField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String language = pageContext.getLanguage();
    String selectGroupImg = Util.getIcon("groupPanel");
    String selectGroupLab = Util.getString("groupPanel", language);
    String deleteImg = Util.getIcon("delete");
    String deleteLab = Util.getString("clearGroup", language);

    String groupName = "";
    String groupId = "";
    StringBuilder html = new StringBuilder();

    String fieldName = template.getFieldName();

    if (!GroupField.TYPE.equals(field.getTypeName())) {

    } else {
      groupId = field.getGroupId();
    }
    if (!field.isNull()) {
      groupName = field.getValue();
    }
    html.append("<input type=\"hidden\"" + " id=\"").append(fieldName).append("\" name=\"").
        append(fieldName).append("\" value=\"").
        append(EncodeHelper.javaStringToHtmlString(groupId)).append("\" />");

    if (!template.isHidden()) {
      html.append("<input type=\"text\" disabled=\"disabled\" size=\"50\" " + " id=\"").
          append(fieldName).append("_name\" name=\"").append(fieldName).
          append("$$name\" value=\"").
          append(EncodeHelper.javaStringToHtmlString(groupName)).append("\" />");
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
      boolean groupsOfInstanceOnly =
          StringUtil.getBooleanValue(parameters.get("groupsOfInstanceOnly"));
      String roles = parameters.get("roles");
      if (StringUtil.isDefined(roles)) {
        groupsOfInstanceOnly = true;
      }
      html.append("&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('")
          .append(URLUtil.getApplicationURL())
          .append("/RselectionPeasWrapper/jsp/open" + "?formName=")
          .append(pageContext.getFormName())
          .append("&elementId=").append(fieldName)
          .append("&elementName=").append(fieldName).append("_name")
          .append("&selectable=").append(GROUP)
          .append("&selectedGroup=").append((groupId == null) ? "" : groupId)
          .append(groupsOfInstanceOnly ? "&instanceId=" + pageContext.getComponentId() : "")
          .append(StringUtil.isDefined(roles) ? "&roles=" + roles : "")
          .append("','selectGroup',800,600,'');\" >");
      html.append("<img src=\"").append(selectGroupImg).
          append("\" width=\"15\" height=\"15\" border=\"0\" alt=\"").append(selectGroupLab).
          append("\" align=\"top\" title=\"").append(selectGroupLab).append("\"/></a>");
      html.append("&nbsp;<a href=\"#\" onclick=\"javascript:" + "document.").
          append(pageContext.getFormName()).append(".").append(fieldName).
          append(".value='';" + "document.").append(pageContext.getFormName()).append(".").
          append(fieldName).append("$$name" + ".value='';" + "\">");
      html.append("<img src=\"").append(deleteImg).append(
          "\" width=\"15\" height=\"15\" border=\"0\" alt=\"").append(deleteLab).append(
          "\" align=\"top\" title=\"").append(deleteLab).append("\"/></a>");

      if (template.isMandatory() && pageContext.useMandatory()) {
        html.append(Util.getMandatorySnippet());
      }
    }

    out.println(html);
  }

  @Override
  public List<String> update(String newId, GroupField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (field.getTypeName().equals(GroupField.TYPE)) {
      if (!StringUtil.isDefined(newId)) {
        field.setNull();
      } else {
        field.setGroupId(newId);
      }
    } else {
      throw new FormException("GroupFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          GroupField.TYPE);
    }
    return new ArrayList<>();
  }

  /**
   * Method declaration
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public List<String> update(List<FileItem> items, GroupField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String itemName = template.getFieldName();
    String value = FileUploadUtil.getParameter(items, itemName);
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
        && !StringUtil.isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }
}
